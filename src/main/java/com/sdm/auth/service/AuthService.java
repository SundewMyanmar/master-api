package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.Token;
import com.sdm.auth.model.request.*;
import com.sdm.auth.repository.TokenRepository;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.FBGraphManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.VelocityTemplateManager;
import com.sdm.core.util.security.SecurityManager;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private FBGraphManager facebookGraphManager;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private VelocityTemplateManager templateManager;

    @Autowired
    private AuthMailService mailService;

    private static final String FB_AUTH_FIELDS = "id,name,email,gender,age_range";

    private static final int MAX_PASSWORD = 32;
    private static final int MIN_PASSWORD = 16;

    private void setAnonymousExtras(AnonymousRequest request, User user) {
        if (!StringUtils.isEmpty(request.getBrand())) {
            user.addExtra("brand", request.getBrand());
        }

        if (!StringUtils.isEmpty(request.getCarrier())) {
            user.addExtra("carrier", request.getCarrier());
        }

        if (!StringUtils.isEmpty(request.getManufacture())) {
            user.addExtra("manufacture", request.getManufacture());
        }
    }

    private Date getTokenExpired() {
        long days = securityManager.getProperties().getAuthTokenLife().toDays();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(String.valueOf(days)));
        return cal.getTime();
    }

    private User createAnonymousUser(AnonymousRequest request) {
        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;

        String userName = request.getDeviceOS() + "_"
                + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
                + Globalizer.generateToken("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 8);

        String passwordChars = securityManager.getProperties().getTokenChars() + "!@#$%^&*_+=abcdefghijklmnopqrstuvwxyz";
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);
        return new User(userName, "Anonymous", password, User.Status.ACTIVE);
    }

    @Transactional
    public String generateJWT(Token token, String userAgent) {
        String id = tokenRepository.findByDeviceId(token.getDeviceId())
                .map(existToken -> existToken.getId())
                .orElseGet(() -> UUID.randomUUID().toString());

        token.setId(id);

        token.setTokenExpired(getTokenExpired());
        token.setLastLogin(new Date());
        tokenRepository.save(token);

        String compactJWT = Jwts.builder().setId(token.getId())
                .setSubject(Integer.toString(token.getUser().getId()))
                .setIssuer(userAgent)
                .setIssuedAt(new Date())
                .setExpiration(token.getTokenExpired())
                .claim("deviceId", token.getDeviceId())
                .claim("deviceOs", token.getDeviceOs())
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS512, securityManager.getProperties().getJwtKey()).compact();

        return compactJWT;
    }

    private String createToken(User user, TokenInfo tokenInfo, String userAgent) {
        Token token = tokenRepository.findByDeviceId(tokenInfo.getDeviceId())
                .orElseGet(() -> new Token());

        token.setUser(user);
        token.setDeviceId(tokenInfo.getDeviceId());
        token.setDeviceOs(tokenInfo.getDeviceOS());
        if (tokenInfo.getFirebaseToken() != null && !tokenInfo.getFirebaseToken().isEmpty()) {
            token.setFirebaseToken(tokenInfo.getFirebaseToken());
        }

        // Generate and store JWT
        String tokenString = this.generateJWT(token, userAgent);
        user.setCurrentToken(tokenString);

        return tokenString;
    }

    private Optional<User> checkOtp(ActivateRequest request, boolean autoResend) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Invalid request token."));

        if (user.getOtpExpired().before(new Date())) {
            if (autoResend) {
                try {
                    this.mailService.activateLink(user);
                } catch (JsonProcessingException ex) {
                    throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
                }
                userRepository.save(user);
            }
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Sorry! Your token has expired. We send new token to your email.");
        }

        user.setOtpToken(null);
        user.setOtpExpired(null);
        if (securityManager.getProperties().isRequireConfirm()) {
            user.setStatus(User.Status.ACTIVE);
        }
        userRepository.save(user);
        return Optional.of(user);
    }

    @Transactional
    public ResponseEntity otpActivation(ActivateRequest request) {
        if (this.checkOtp(request, true).isPresent()) {
            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "activation_success", "Your temporary request is ok.", null));
        }
        throw new GeneralException(HttpStatus.BAD_REQUEST, "Your otp is invalid. Pls try to contact admin team.");
    }

    public ResponseEntity resetPasswordWithOtp(ChangePasswordRequest changePasswordRequest, ActivateRequest activateRequest) {
        User user = this.checkOtp(activateRequest, false).orElseThrow(() ->
                new GeneralException(HttpStatus.UNAUTHORIZED, "There is no user (or) otp is wrong. Pls try again.")
        );

        User authUser = userRepository.authByPassword(changePasswordRequest.getUser(), user.getPassword()).orElseThrow(
                () -> new GeneralException(HttpStatus.UNAUTHORIZED, "Sorry! you old password are not correct."));

        if (!authUser.getId().equals(user.getId())) {
            throw new GeneralException(HttpStatus.UNAUTHORIZED,
                    "There is no user (or) otp is wrong. Pls try again.");
        }

        String newPassword = securityManager.hashString(changePasswordRequest.getNewPassword());
        user.setPassword(newPassword);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    public ResponseEntity forgetPassword(String phoneOrEmail) {
        try {
            User user = userRepository.findByPhoneNumberOrEmail(phoneOrEmail, phoneOrEmail)
                    .orElseThrow(() -> new GeneralException(HttpStatus.NO_CONTENT, "Invalid phone number (or) email address."));

            mailService.forgetPasswordLink(user);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "send_otp", "We send the reset password link to your e-mail.", null));
        } catch (JsonProcessingException ex) {
            logger.error(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    public ResponseEntity authByPassword(AuthRequest request, String userAgent) {
        String password = securityManager.hashString(request.getPassword());
        User authUser = userRepository.authByPassword(request.getUser(), password)
                .orElseThrow(() -> new GeneralException(
                        HttpStatus.UNAUTHORIZED, "Opp! request email or password is something wrong"));

        this.createToken(authUser, request, userAgent);

        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity registerByUserAndEmail(RegistrationRequest request, String userAgent) {
        //Check user by user name
        userRepository.findByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail())
                .ifPresent(user -> {
                    if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email.");
                    } else if (user.getPhoneNumber().equalsIgnoreCase(request.getPhoneNumber())) {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this phone number.");
                    }
                });

        boolean needConfirm = securityManager.getProperties().isRequireConfirm();
        User.Status status = needConfirm ? User.Status.PENDING : User.Status.ACTIVE;
        String password = securityManager.hashString(request.getPassword());
        User newUser = new User(request.getEmail(), request.getPhoneNumber(), request.getDisplayName(),
                password, status);
        userRepository.save(newUser);
        if (needConfirm) {

        }

        this.createToken(newUser, request, userAgent);

        return new ResponseEntity(newUser, HttpStatus.CREATED);
    }


    @Transactional
    public ResponseEntity anonymousAuth(AnonymousRequest request, String userAgent) {
        //Check Device Registration
        User authUser = tokenRepository.findByDeviceId(request.getDeviceId())
                .map(token -> token.getUser()).orElseGet(() -> this.createAnonymousUser(request));

        //User create / update
        setAnonymousExtras(request, authUser);
        userRepository.save(authUser);

        this.createToken(authUser, request, userAgent);

        return ResponseEntity.ok(authUser);
    }


    public User createFacebookUser(JsonObject profileObj) {
        Optional<User> dbEntity;
        User userEntity;

        String userName = "FB" + profileObj.get("id").getAsString();
        String displayName = profileObj.get("name").getAsString();

        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;
        String passwordChars = securityManager.getProperties().getTokenChars() + "!@#$%^&*_+=abcdefghijklmnopqrstuvwxyz";
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);

        if (profileObj.has("email") && !profileObj.get("email").isJsonNull()) {
            String email = profileObj.get("email").getAsString();
            String phone = email;
            if (profileObj.has("phone")) {
                phone = profileObj.get("phone").getAsString();
            }

            //Get Back Old User Data With Email
            dbEntity = userRepository.findByPhoneNumberOrEmail(phone, email);

            if (dbEntity.isPresent()) {
                userEntity = dbEntity.get();
                userEntity.setDisplayName(displayName);
            } else {
                userEntity = new User(userName, displayName, password, User.Status.ACTIVE);
                userEntity.setEmail(profileObj.get("email").getAsString());
            }
        } else {
            userEntity = new User(userName, displayName, password, User.Status.ACTIVE);
            //if no email
            userEntity.setEmail(profileObj.get("id").getAsString() + "@facebook.com");
        }

        if (profileObj.has("gender") && !profileObj.get("gender").isJsonNull()) {
            userEntity.addExtra("gender", profileObj.get("gender").getAsString());
        }

        if (profileObj.has("age_range") && !profileObj.get("age_range").isJsonNull()) {
            JsonObject obj = profileObj.get("age_range").getAsJsonObject();

            userEntity.addExtra("age_range", obj.toString());
        }

        userEntity.setFacebookId(profileObj.get("id").getAsString());
        return userRepository.save(userEntity);
    }

    @Transactional
    public ResponseEntity facebookAuth(FacebookAuthRequest request, String userAgent) {
        JsonObject facebookProfile = facebookGraphManager.checkFacebookToken(request.getAccessToken(), FB_AUTH_FIELDS, userAgent);
        String id = facebookProfile.get("id").getAsString();

        //Check User by FacebookId
        User authUser = userRepository.findByFacebookId(id)
                .orElseGet(() -> this.createFacebookUser(facebookProfile));

        if (authUser.getFacebookId().equalsIgnoreCase(facebookProfile.get("id").getAsString())) {
            this.createToken(authUser, request, userAgent);
        } else {
            throw new GeneralException(HttpStatus.UNAUTHORIZED, "Invalid Access Token!");
        }

        return ResponseEntity.ok(authUser);
    }
}
