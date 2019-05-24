package com.sdm.master.service;

import java.util.*;

import com.google.gson.JsonObject;
import com.sdm.core.component.FBGraphManager;
import com.sdm.core.component.FirebaseManager;
import com.sdm.core.component.WebMailManager;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.MailHeader;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.master.entity.TokenEntity;
import com.sdm.master.entity.UserEntity;
import com.sdm.master.repository.TokenRepository;
import com.sdm.master.repository.UserRepository;
import com.sdm.master.request.AnonymousRequest;
import com.sdm.master.request.AuthRequest;
import com.sdm.master.request.FacebookAuthRequest;
import com.sdm.master.request.RegistrationRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    FBGraphManager facebookGraphManager;

    @Autowired
    SecurityProperties securityProperties;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    WebMailManager mailManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenRepository tokenRepository;

    private static final String FB_AUTH_FIELDS = "id,name,email,gender,age_range";

    private static final int MAX_PASSWORD = 32;
    private static final int MIN_PASSWORD = 16;

    private void setAnonymousExtras(AnonymousRequest request, UserEntity user) {
        if (!StringUtil.isNullOrEmpty(request.getBrand())) {
            user.addExtra("brand", request.getBrand());
        }

        if (!StringUtil.isNullOrEmpty(request.getCarrier())) {
            user.addExtra("carrier", request.getCarrier());
        }

        if (!StringUtil.isNullOrEmpty(request.getManufacture())) {
            user.addExtra("manufacture", request.getManufacture());
        }
    }

    private UserEntity setOTP(UserEntity user) {
        user.setOtpToken(Globalizer.generateToken(
            securityProperties.getTokenChars(), UserEntity.TOKEN_LENGTH));
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, securityProperties.getOtpLife());
        user.setOtpExpired(cal.getTime());
        return user;
    }

    private void sendWelcomeUser(UserEntity user, String rawPassword, String title) {

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("email", user.getEmail());
        data.put("name", user.getDisplayName());
        data.put("password", rawPassword);
        data.put("current_year", Globalizer.getDateString("yyyy", new Date()));

        mailManager.sendByTemplate(new MailHeader(user.getEmail(), title),
            "mail/create-user.vm", data);
    }

    private Date getTokenExpired() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, securityProperties.getAuthTokenLife());
        return cal.getTime();
    }

    private UserEntity createAnonymousUser(AnonymousRequest request) {
        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;

        String userName = request.getDeviceOS() + "_"
            + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
            + Globalizer.generateToken("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 8);

        String passwordChars = securityProperties.getTokenChars() + "!@#$%^&*_+=abcdefghijklmnopqrstuvwxyz";
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);
        return new UserEntity(userName, "Anonymous", password, UserEntity.Status.ACTIVE);
    }

    @Transactional
    public String generateJWT(TokenEntity token, String userAgent) {
        String id = tokenRepository.findByDeviceIdAndDeviceOs(
            token.getDeviceId(), token.getDeviceOs())
            .map(existToken -> existToken.getId())
            .orElseGet(() -> UUID.randomUUID().toString());

        token.setId(id);

        token.setTokenExpired(getTokenExpired());
        token.setLastLogin(new Date());
        tokenRepository.save(token);

        String compactJWT = Jwts.builder().setId(token.getId())
            .setSubject(Long.toString(token.getUser().getId()))
            .setIssuer(userAgent)
            .setIssuedAt(new Date())
            .setExpiration(token.getTokenExpired())
            .claim("device_id", token.getDeviceId())
            .claim("device_os", token.getDeviceOs())
            .compressWith(CompressionCodecs.DEFLATE)
            .signWith(SignatureAlgorithm.HS512, securityProperties.getJwtKey()).compact();

        return compactJWT;
    }

    private String createToken(UserEntity user, AuthRequest request, String userAgent) {
        TokenEntity token = tokenRepository.findByDeviceIdAndDeviceOs(request.getDeviceId(), request.getDeviceOS())
            .orElseGet(() -> new TokenEntity());

        token.setUser(user);
        token.setDeviceId(request.getDeviceId());
        token.setDeviceOs(request.getDeviceOS());
        if (request.getFirebaseToken() != null && !request.getFirebaseToken().isEmpty()) {
            token.setFirebaseToken(request.getFirebaseToken());
        }

        // Generate and store JWT
        String tokenString = this.generateJWT(token, userAgent);
        user.setCurrentToken(tokenString);

        return tokenString;
    }

    private String createToken(UserEntity user, FacebookAuthRequest request, String userAgent) {
        TokenEntity token = tokenRepository.findByDeviceIdAndDeviceOs(request.getDeviceId(), request.getDeviceOS())
                .orElseGet(() -> new TokenEntity());

        token.setUser(user);
        token.setDeviceId(request.getDeviceId());
        token.setDeviceOs(request.getDeviceOS());
        if (request.getFirebaseToken() != null && !request.getFirebaseToken().isEmpty()) {
            token.setFirebaseToken(request.getFirebaseToken());
        }

        // Generate and store JWT
        String tokenString = this.generateJWT(token, userAgent);
        user.setCurrentToken(tokenString);

        return tokenString;
    }

    @Transactional
    public ResponseEntity authByPassword(AuthRequest request, String userAgent) {
        String password = securityManager.hashString(request.getPassword());
        UserEntity authUser = userRepository.authByPassword(request.getUser(), password)
            .orElseThrow(() -> new GeneralException(
                HttpStatus.UNAUTHORIZED, "Opp! request email or password is something wrong"));

        this.createToken(authUser, request, userAgent);

        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity registerByUserAndEmail(RegistrationRequest request, String userAgent) {
        //Check user by user name
        userRepository.findByUserNameOrEmail(request.getUser(), request.getEmail())
            .ifPresent(user -> {
                if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email");
                } else if (user.getUserName().equalsIgnoreCase(request.getUser())) {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this username");
                }
            });

        UserEntity.Status status = securityProperties.isRequireConfirm() ? UserEntity.Status.PENDING : UserEntity.Status.ACTIVE;
        String password = securityManager.hashString(request.getPassword());
        UserEntity newUser = new UserEntity(request.getEmail(), request.getUser(), request.getDisplayName(),
            password, status);
        userRepository.save(newUser);

        this.createToken(newUser, request, userAgent);

        return new ResponseEntity(newUser, HttpStatus.CREATED);
    }


    @Transactional
    public ResponseEntity anonymousAuth(AnonymousRequest request, String userAgent) {
        //Check Device Registration
        UserEntity authUser = tokenRepository.findByDeviceIdAndDeviceOs(request.getDeviceId(), request.getDeviceOS())
            .map(token -> token.getUser()).orElseGet(() -> this.createAnonymousUser(request));

        //User create / update
        setAnonymousExtras(request, authUser);
        userRepository.save(authUser);

        this.createToken(authUser, request, userAgent);

        return ResponseEntity.ok(authUser);
    }


    public UserEntity createFacebookUser(JsonObject profileObj) {
        Optional<UserEntity> dbEntity;
        UserEntity userEntity;

        String userName = "FB" + profileObj.get("id").getAsString();
        String displayName = profileObj.get("name").getAsString();

        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;
        String passwordChars = securityProperties.getTokenChars() + "!@#$%^&*_+=abcdefghijklmnopqrstuvwxyz";
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);

        if (profileObj.has("email") && !profileObj.get("email").isJsonNull()){
            String email=profileObj.get("email").getAsString();

            //Get Back Old User Data With Email
            dbEntity =userRepository.findByUserNameOrEmail(email,email);

            if(dbEntity.isPresent()){
                userEntity=dbEntity.get();
                userEntity.setDisplayName(displayName);
            }else{
                userEntity = new UserEntity(userName, displayName, password, UserEntity.Status.ACTIVE);
                userEntity.setEmail(profileObj.get("email").getAsString());
            }
        }else{
            userEntity = new UserEntity(userName, displayName, password, UserEntity.Status.ACTIVE);
            //if no email
            userEntity.setEmail(profileObj.get("id").getAsString()+"@facebook.com");
        }

        if (profileObj.has("gender") && !profileObj.get("gender").isJsonNull()){
            userEntity.addExtra("gender",profileObj.get("gender").getAsString());
        }

        if (profileObj.has("age_range") && !profileObj.get("age_range").isJsonNull()){
            JsonObject obj=profileObj.get("age_range").getAsJsonObject();

            userEntity.addExtra("age_range",obj.toString());
        }

        userEntity.setFacebookId(profileObj.get("id").getAsString());
        return userRepository.save(userEntity);
    }

    @Transactional
    public ResponseEntity facebookAuth(FacebookAuthRequest request, String userAgent) {
        JsonObject facebookProfile = facebookGraphManager.checkFacebookToken(request.getAccessToken(), FB_AUTH_FIELDS, userAgent);
        String id = facebookProfile.get("id").getAsString();

        //Check User by FacebookId
        UserEntity authUser = userRepository.findByFacebookId(id)
            .orElseGet(() -> this.createFacebookUser(facebookProfile));

        if (authUser.getFacebookId().equalsIgnoreCase(facebookProfile.get("id").getAsString())) {
            this.createToken(authUser, request, userAgent);
        } else {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
        }

        return ResponseEntity.ok(authUser);
    }
}
