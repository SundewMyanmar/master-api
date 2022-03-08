package com.sdm.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.OAuth2Request;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.storage.model.File;
import com.sdm.storage.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class GoogleAuthService implements SocialAuthService {
    private final String GOOGLE_CLIENT_SECRET = "classpath:google.json";

    @Autowired
    FileService fileService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Check Google Account
     * Set path to the Web application client_secret_*.json file you downloaded from the
     * Google API Console: https://console.developers.google.com/apis/credentials
     * You can also find your Web application client ID and client secret from the
     * console and specify them directly when you create the GoogleAuthorizationCodeTokenRequest object.
     *
     * @param authCode
     * @return
     * @throws IOException
     */
    public GoogleIdToken.Payload checkGoogle(String authCode) {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        try {
            java.io.File jsonSecret = ResourceUtils.getFile(GOOGLE_CLIENT_SECRET);
            try (FileReader secretReader = new FileReader(jsonSecret)) {
                GoogleClientSecrets clientSecret = GoogleClientSecrets.load(jsonFactory, secretReader);
                NetHttpTransport transport = new NetHttpTransport();
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                        .setAudience(List.of(clientSecret.getDetails().getClientId()))
                        .build();
                GoogleIdToken idToken = verifier.verify(authCode);
                if (idToken == null) {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-google-account"));
                }

                return idToken.getPayload();
            } catch (IOException | GeneralSecurityException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
        } catch (FileNotFoundException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    public File createProfileImage(String pictureUrl) {
        /*
            Sample Url
            Get Large 512 Image
            https://lh3.googleusercontent.com/a-/AOh14GgjXuK3-1PHnT89zU6gQXkxn1W_CjZt4AUBbo5t_g=s96-c
         */
        if (pictureUrl.contains("=s96-c"))
            pictureUrl = pictureUrl.replace("=s96-c", "=s512-c");

        if (pictureUrl != null) {
            try {
                return fileService.create(pictureUrl, false, true, null);
            } catch (IOException e) {
                log.error("GOOGLE_IMAGE_FAIL>>>" + e.getLocalizedMessage());
            }
        }
        return null;
    }

    /**
     * Get User Info from Google Payload
     *
     * @param payload GooglePayload
     */
    @Transactional
    private User createUser(GoogleIdToken.Payload payload) {
        String userId = payload.getSubject();
        String phoneNumber = "GL_" + userId;
        String email = payload.getEmail();
        String displayName = (String) payload.get("name");
        String rawPassword = Globalizer.randomPassword(AuthService.MIN_PASSWORD, AuthService.MAX_PASSWORD);
        String password = passwordEncoder.encode(rawPassword);
        File profilePicture = this.createProfileImage(payload.get("picture").toString());

        if (!payload.getEmailVerified()) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("verify-your-email"));
        }

        //Check User Data

        //Get Back Old User Data With Email
        User userEntity = userRepository.findFirstByEmailAndEmailIsNotNull(email)
                .orElse(new User(email, phoneNumber, displayName, password, User.Status.ACTIVE));

        if (Globalizer.isNullOrEmpty(userEntity.getGoogleId())) {
            userEntity.setGoogleId(userId);
        }

        if (Globalizer.isNullOrEmpty(userEntity.getProfileImage())) {
            if (profilePicture != null) {
                userEntity.setProfileImage(profilePicture);
            }
        }

        Integer clientRole = securityManager.getProperties().getClientRole();
        Optional<Role> role = roleRepository.findById(clientRole);
        if (role.isPresent()) {
            userEntity.setRoles(Set.of(role.get()));
        }

        return userRepository.save(userEntity);
    }

    @Override
    public ResponseEntity<User> auth(OAuth2Request request) {
        final GoogleIdToken.Payload payload = this.checkGoogle(request.getAccessToken());
        String email = payload.getEmail();
        String googleId = payload.getSubject();

        User authUser = userRepository.findFirstByGoogleId(googleId)
                .orElseGet(() -> userRepository.findFirstByEmailAndEmailIsNotNull(email)
                        .orElseGet(() -> this.createUser(payload)));

        if (Globalizer.isNullOrEmpty(authUser.getGoogleId())) {
            authUser.setGoogleId(googleId);
        }

        if (!authUser.getEmail().equalsIgnoreCase(email)) {
            authUser.setEmail(email);
        }

        if (Globalizer.isNullOrEmpty(authUser.getProfileImage())) {
            String profilePic = (String) payload.get("picture");
            File profilePicture = this.createProfileImage(profilePic);
            if (profilePicture != null) {
                authUser.setProfileImage(profilePicture);
            }
        }

        userRepository.save(authUser);

        if (authUser.getGoogleId().equalsIgnoreCase(payload.getSubject())) {
            jwtService.createToken(authUser, request, httpServletRequest);
        } else {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("google-linked-failed"));
        }

        return ResponseEntity.ok(authUser);
    }

    @Override
    public ResponseEntity<User> link(String accessId, User user) {
        //Check User by GoogleId
        User authUser = userRepository.findFirstByGoogleId(accessId)
                .orElseGet(() -> user);

        if (!authUser.getId().equals(user.getId())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("google-already-linked"));
        }

        if (Globalizer.isNullOrEmpty(user.getGoogleId())) {
            user.setGoogleId(accessId);
        } else {
            user.setGoogleId(null);
        }

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

}
