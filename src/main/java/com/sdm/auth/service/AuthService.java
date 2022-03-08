package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.admin.model.Role;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.MultiFactorAuth;
import com.sdm.auth.model.request.*;
import com.sdm.core.Constants;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import com.sdm.storage.service.FileService;
import com.sdm.telenor.model.request.telenor.MessageType;
import com.sdm.telenor.service.PhoneVerificationService;
import com.sdm.telenor.service.TelenorSmsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Service
@Log4j2
public class AuthService {
    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthMailService mailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HttpSession session;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private MultiFactorAuthService mfaService;

    @Autowired
    private TelenorSmsService smsService;

    @Autowired
    private PhoneVerificationService phoneVerificationService;

    @Autowired
    private LocaleManager localeManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    FileService fileService;

    public static final int MAX_PASSWORD = 32;
    public static final int MIN_PASSWORD = 16;

    private void increaseFailedCount() {
        Integer count = (Integer) session.getAttribute(Constants.SESSION.AUTH_FAILED_COUNT);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(Constants.SESSION.AUTH_FAILED_COUNT, count);
    }

    private String getActivateCallbackURL() {
        return Globalizer.getCurrentContextPath("/auth/activate", true);
    }

    private void setAnonymousExtras(AnonymousRequest request, User user) {
        if (!Globalizer.isNullOrEmpty(request.getBrand())) {
            user.addExtra("brand", request.getBrand());
        }

        if (!Globalizer.isNullOrEmpty(request.getCarrier())) {
            user.addExtra("carrier", request.getCarrier());
        }

        if (!Globalizer.isNullOrEmpty(request.getManufacture())) {
            user.addExtra("manufacture", request.getManufacture());
        }
    }

    private User createAnonymousUser(AnonymousRequest request) {
        String userName = request.getDeviceOS() + "_"
                + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
                + Globalizer.generateToken(Constants.Auth.GENERATED_TOKEN_CHARS, 8);

        String passwordChars = Globalizer.randomPassword(MIN_PASSWORD, MAX_PASSWORD);
        String password = passwordEncoder.encode(passwordChars);
        return new User(userName, "Anonymous", password, User.Status.ACTIVE);
    }

    @Transactional
    public ResponseEntity<MessageResponse> accountActivation(ActivateRequest request) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST,
                        localeManager.getMessage("invalid-otp-code")));

        //Resend OTP to User
        if (user.getActivateTokenExpire().before(new Date())) {
            try {
                this.mailService.activateLink(user, getActivateCallbackURL());
            } catch (JsonProcessingException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
            userRepository.save(user);
            throw new GeneralException(HttpStatus.BAD_REQUEST,
                    localeManager.getMessage("otp-expired"));
        }

        user.setActivateToken(null);
        user.setActivateTokenExpire(null);

        if (securityManager.getProperties().isRequireConfirm()) {
            user.setStatus(User.Status.ACTIVE);
        }
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, localeManager.getMessage("success"), localeManager.getMessage("account-activation-success"), null));
    }

    public boolean checkSMSOTP(ActivateRequest request) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST,
                        localeManager.getMessage("invalid-otp-code")));

        return user.getActivateTokenExpire().after(new Date()) && user.getActivateToken().equals(request.getToken());
    }

    @Transactional
    private User resetPasswordByToken(ActivateRequest request, String password) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST,
                        localeManager.getMessage("invalid-otp-code")));

        if (user.getActivateTokenExpire().before(new Date()) || !user.getActivateToken().equals(request.getToken())) {
            user.setActivateToken(null);
            user.setActivateTokenExpire(null);
            userRepository.save(user);
            throw new GeneralException(HttpStatus.BAD_REQUEST,
                    localeManager.getMessage("invalid-otp-code"));
        }

        String newPassword = passwordEncoder.encode(password);
        user.setPassword(newPassword);
        user.setActivateToken(null);
        user.setActivateTokenExpire(null);
        userRepository.save(user);

        return user;
    }

    public ResponseEntity<User> resetPasswordJson(ChangePasswordRequest changePasswordRequest, ActivateRequest activateRequest) {
        User user = resetPasswordByToken(activateRequest, changePasswordRequest.getNewPassword());
        return ResponseEntity.ok(user);
    }

    public void resetPasswordMail(ActivateRequest request) {
        String genPassword = Globalizer.randomPassword(10, 14);
        User user = resetPasswordByToken(request, genPassword);
        mailService.welcomeUser(user, genPassword, "Generated New Password");
    }

    public ResponseEntity<MessageResponse> forgetPassword(ForgetPasswordRequest request) {
        String phoneNumber = Globalizer.cleanPhoneNo(request.getPhoneNumber());
        if (Globalizer.isNullOrEmpty(request.getEmail()) && !Globalizer.isNullOrEmpty(request.getPhoneNumber())) {
            User user = userRepository.findFirstByPhoneNumberAndPhoneNumberIsNotNull(phoneNumber)
                    .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-registration-phone")));

            String generateOTP = Globalizer.generateToken("0123456789", 6);
            user.setActivateToken(generateOTP);
            user.setActivateTokenExpire(Globalizer.addDate(new Date(), Duration.ofMinutes(5)));
            userRepository.save(user);

            try {
                smsService.sendMessage(localeManager.getMessage("send-verification-code", generateOTP),
                        new String[]{user.getPhoneNumber()}, MessageType.MULTILINGUAL);
            } catch (IOException e) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
            }
            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, localeManager.getMessage("success"), localeManager.getMessage("sent-verification-code-to", request.getPhoneNumber()), null));
        } else {
            User user = userRepository.findFirstByPhoneNumberAndEmail(phoneNumber, request.getEmail())
                    .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-registration-user")));
            String url = request.getCallback();
            if (Globalizer.isNullOrEmpty(url)) {
                url = Globalizer.getCurrentContextPath("/auth/resetPassword", true);
            }

            try {
                mailService.forgetPasswordLink(user, url);
            } catch (JsonProcessingException e) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
            }
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, localeManager.getMessage("success"), localeManager.getMessage("sent-reset-password-link"), null));
        }
    }

    @Transactional
    private User authByPassword(String user, String rawPassword) {
        User authUser;
        if (Globalizer.isPhoneNo(user)) {
            user = Globalizer.cleanPhoneNo(user);
            authUser = userRepository.findFirstByPhoneNumberAndPhoneNumberIsNotNull(user)
                    .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-registration-phone")));
        } else {
            //Check User
            authUser = userRepository.findFirstByEmailAndEmailIsNotNull(user)
                    .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-registration-email")));
        }

        if (!passwordEncoder.matches(rawPassword, authUser.getPassword())) {
            increaseFailedCount();
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("auth-by-password-failed"));
        }

        return authUser;
    }

    @Transactional
    public ResponseEntity<User> authByPassword(AuthRequest request) {
        User authUser = authByPassword(request.getUser(), request.getPassword());

        MultiFactorAuth mfa = mfaService.authMfa(authUser.getId(), request.getMfaKey(), request.isMfaAuto());
        authUser.setMfa(mfa);
        if (mfa != null && Globalizer.isNullOrEmpty(request.getMfaCode())) {
            if (!mfa.getType().equals(MultiFactorAuth.Type.APP)) {
                mfaService.sendMfaCode(mfa);
            }
            return new ResponseEntity<>(authUser, HttpStatus.PARTIAL_CONTENT);
        }

        if (mfa != null && !mfaService.verify(authUser.getId(), request.getMfaCode(), request.getMfaKey())) {
            throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("invalid-otp-code"));
        }

        jwtService.createToken(authUser, request, httpServletRequest);
        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity<User> userRegistration(RegistrationRequest request) {
        String phoneNumber = Globalizer.cleanPhoneNo(request.getPhoneNumber());
        //Check user by phone number
        userRepository.findFirstByPhoneNumberAndPhoneNumberIsNotNull(phoneNumber)
                .ifPresent(user -> {
                    throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("already-registered-phone"));
                });

        if (!Globalizer.isNullOrEmpty(request.getEmail())) {
            userRepository.findFirstByEmailAndEmailIsNotNull(request.getEmail())
                    .ifPresent(user -> {
                        throw new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("already-registered-email"));
                    });
        }

        boolean needConfirm = securityManager.getProperties().isRequireConfirm();
        User.Status status = needConfirm ? User.Status.PENDING : User.Status.ACTIVE;
        String password = passwordEncoder.encode(request.getPassword());
        User newUser = new User(phoneNumber, request.getDisplayName(),
                password, status);

        if (!Globalizer.isNullOrEmpty(request.getEmail())) {
            newUser.setEmail(request.getEmail());
        }


        //set register User to default client role
        Integer clientRole = securityManager.getProperties().getClientRole();
        Optional<Role> role = roleRepository.findById(clientRole);
        if (role.isPresent()) {
            newUser.setRoles(Set.of(role.get()));
        }

        userRepository.save(newUser);
        if (needConfirm) {
            try {
                mailService.activateLink(newUser, getActivateCallbackURL());
            } catch (JsonProcessingException ex) {
                log.warn(ex.getLocalizedMessage());
            }
        } else {
            jwtService.createToken(newUser, request, httpServletRequest);
        }

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }
}
