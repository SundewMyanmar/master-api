package com.sdm.auth.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.config.properties.AppleProperties;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.HttpRequestManager;
import com.sdm.core.util.LocaleManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.*;

@Service
@Log4j2
public class AppleAuthService implements SocialAuthService {

    private final String APPLE_ID_URL = "https://appleid.apple.com";
    private final AppleProperties properties;

    @Autowired
    private HttpRequestManager httpRequestManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private LocaleManager localeManager;

    public AppleAuthService(AppleProperties properties) {
        this.properties = properties;
    }

    private String generateClientSecret() {
        try {
            String authKey = Files.readString(Path.of(properties.getApiKey()));
            authKey = authKey.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(authKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

            Date issuedAt = new Date();
            Date expired = Globalizer.addDate(issuedAt, Duration.ofMinutes(20));

            return Jwts.builder()
                    .setSubject(properties.getAppId())
                    .setIssuer(properties.getTeamId())
                    .setAudience(APPLE_ID_URL)
                    .setIssuedAt(issuedAt)
                    .setExpiration(expired)
                    .signWith(privateKey, SignatureAlgorithm.ES256).compact();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid Key File!");
        }
    }

    private JsonObject verifyAuthToken(String authorizationCode) {
        try {
            URL verifyURL = new URL(String.join("/", APPLE_ID_URL, "auth", "token"));
            String clientSecret = this.generateClientSecret();

            Map<String, String> body = new HashMap<>();
            body.put("client_id", properties.getAppId());
            body.put("client_secret", clientSecret);
            body.put("code", authorizationCode);
            body.put("grant_type", "authorization_code");

            HttpResponse serverResponse = httpRequestManager.formPostRequest(verifyURL, body, true);
            JsonObject json = new Gson().fromJson(serverResponse.getBody(), JsonObject.class);

            if (!json.has("id_token")) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("apple-linked-failed"));
            }

            String[] chunks = json.get("id_token").getAsString().split("\\.");
            if (chunks.length < 3) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("apple-linked-failed"));
            }
            String payload = new String(Base64.getDecoder().decode(chunks[1]));
            json = new Gson().fromJson(payload, JsonObject.class);
            /* Validate Access Token */
            if (!json.has("sub")) {
                throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("apple-linked-failed"));
            }

            return json;
        } catch (MalformedURLException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    private User createUser(JsonObject payload, String displayName) {
        String appleId = payload.get("sub").getAsString();
        String phoneNumber = "APL_" + appleId;
        String email = payload.has("email") ? payload.get("email").getAsString() : appleId + "@appleId.com";
        String rawPassword = Globalizer.randomPassword(AuthService.MIN_PASSWORD, AuthService.MAX_PASSWORD);
        String password = securityManager.hashString(rawPassword);

        //Get Back Old User Data With Email
        User userEntity = userRepository.findFirstByEmailAndEmailIsNotNull(email)
                .orElse(new User(email, phoneNumber, displayName, password, User.Status.ACTIVE));

        userEntity.setAppleId(appleId);

        Integer clientRole = securityManager.getProperties().getClientRole();
        Optional<Role> role = roleRepository.findById(clientRole);
        if (role.isPresent()) {
            userEntity.setRoles(Set.of(role.get()));
        }

        return userEntity;
    }

    @Override
    @Transactional
    public ResponseEntity<User> auth(OAuth2Request request) {
        JsonObject payload = verifyAuthToken(request.getAccessToken());
        String appleId = payload.get("sub").getAsString();
        User authUser = userRepository.findFirstByAppleId(appleId)
                .orElseGet(() -> createUser(payload, request.getDisplayName()));

        if (Globalizer.isNullOrEmpty(authUser.getAppleId())) {
            authUser.setAppleId(appleId);
        }

        userRepository.save(authUser);
        jwtService.createToken(authUser, request, httpServletRequest);

        return ResponseEntity.ok(authUser);
    }

    @Override
    @Transactional
    public ResponseEntity<User> link(String accessId, User user) {
        //Check User by Apple Id
        User authUser = userRepository.findFirstByAppleId(accessId)
                .orElseGet(() -> user);

        if (!authUser.getId().equals(user.getId())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("apple-linked-failed"));
        }

        if (Globalizer.isNullOrEmpty(user.getAppleId())) {
            user.setAppleId(accessId);
        } else {
            user.setAppleId(null);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
}
