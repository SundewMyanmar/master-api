package com.sdm.master.service;

import com.sdm.core.SecurityProperties;
import com.sdm.core.component.WebMailManager;
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
import com.sdm.master.request.RegistrationRequest;
import io.grpc.netty.shaded.io.netty.util.internal.StringUtil;
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

import java.util.*;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

    private UserEntity setToken(UserEntity user) {
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

    @Transactional
    public String generateJWT(UserEntity user, String deviceId, String deviceOS, String fireBaseToken) {
        TokenEntity token = tokenRepository.findByDeviceIdAndDeviceOs(
            deviceId, deviceOS).orElseGet(() -> {
            TokenEntity newToken = new TokenEntity();
            newToken.setId(UUID.randomUUID().toString());
            return newToken;
        });

        token.setUserId(user.getId());
        token.setDeviceId(deviceId);
        token.setDeviceOs(deviceOS);

        if (fireBaseToken != null && !fireBaseToken.isEmpty()) {
            token.setFirebaseToken(fireBaseToken);
        }

        token.setTokenExpired(getTokenExpired());
        token.setLastLogin(new Date());
        tokenRepository.save(token);

        String compactJWT = Jwts.builder().setId(Long.toString(token.getUserId()))
            .setSubject(token.getId())
            .setIssuer(token.getDeviceId())
            .setIssuedAt(new Date())
            .setExpiration(token.getTokenExpired())
            .compressWith(CompressionCodecs.DEFLATE)
            .signWith(SignatureAlgorithm.HS512, securityProperties.getJwtKey()).compact();

        user.setCurrentToken(compactJWT);
        return compactJWT;
    }

    @Transactional
    public ResponseEntity authByPassword(AuthRequest request, boolean isClean) {
        String password = securityManager.hashString(request.getPassword());
        UserEntity authUser = userRepository.authByPassword(request.getUser(), password)
            .orElseThrow(() -> new GeneralException(
                HttpStatus.UNAUTHORIZED, "Opp! Request email or password is something wrong"));

        //If cleantoken, delete all old token
        if (isClean) {
            tokenRepository.deleteInBulkByUserId(authUser.getId());
        }

        // Generate and store JWT
        this.generateJWT(authUser, request.getDeviceId(),
            request.getDeviceOS(), request.getFirebaseToken());

        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity registerByUserAndEmail(RegistrationRequest request) {
        //Check user by user name
        userRepository.findByUserNameOrEmail(request.getUserName(), request.getEmail())
            .ifPresent(user -> {
                if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this email");
                } else if (user.getUserName().equalsIgnoreCase(request.getUserName())) {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, "Sorry! someone already registered with this username");
                }
            });

        UserEntity.Status status = securityProperties.isRequireConfirm() ? UserEntity.Status.PENDING : UserEntity.Status.ACTIVE;
        String password = securityManager.hashString(request.getPassword());
        UserEntity newUser = new UserEntity(request.getEmail(), request.getUserName(), request.getDisplayName(),
            password, status);
        userRepository.save(newUser);

        // Generate and store JWT
        this.generateJWT(newUser, request.getDeviceId(),
            request.getDeviceOS(), request.getFirebaseToken());

        return new ResponseEntity(newUser, HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity anonymousAuth(AnonymousRequest request) {
        Random rnd = new Random();
        int size = rnd.nextInt((MAX_PASSWORD - MIN_PASSWORD) + 1) + MIN_PASSWORD;

        String userName = request.getOs() + "_"
            + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
            + Globalizer.generateToken("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 8);

        String passwordChars = securityProperties.getTokenChars() + "!@#$%^&*_+=abcdefghijklmnopqrstuvwxyz";
        String rawPassword = Globalizer.generateToken(passwordChars, size);
        String password = securityManager.hashString(rawPassword);


        //Check Device Registration
        UserEntity newUser = new UserEntity(userName, "Unknown", password, UserEntity.Status.ACTIVE);
        UserEntity authUser = tokenRepository.findByDeviceIdAndDeviceOs(request.getUniqueId(), request.getOs())
            .map(token -> {
                UserEntity user = userRepository.findById(token.getUserId())
                    .orElse(newUser);
                return user;
            }).orElse(newUser);

        //User create / update
        setAnonymousExtras(request, authUser);
        userRepository.save(authUser);

        //Token create / update
        this.generateJWT(authUser, request.getUniqueId(), request.getOs(), request.getFirebaseToken());

        return ResponseEntity.ok(authUser);
    }
}
