package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.Constants;
import com.sdm.admin.model.User;
import com.sdm.admin.repository.RoleRepository;
import com.sdm.admin.repository.UserRepository;
import com.sdm.auth.model.request.*;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.file.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;

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
    private MfaService mfaService;

    @Autowired
    FileService fileService;

    public static final int MAX_PASSWORD = 32;
    public static final int MIN_PASSWORD = 16;

    private int increaseFailedCount() {
        Integer count = (Integer) session.getAttribute(Constants.SESSION.AUTH_FAILED_COUNT);
        if (count == null) {
            count = 0;
        }
        count++;
        session.setAttribute(Constants.SESSION.AUTH_FAILED_COUNT, count);
        return count;
    }

    private String getActivateCallbackURL() {
        return Globalizer.getCurrentContextPath("/auth/activate", true);
    }

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

    private User createAnonymousUser(AnonymousRequest request) {
        String userName = request.getDeviceOS() + "_"
                + Globalizer.getDateString("yyyyMMddHHmmss", new Date()) + "_"
                + Globalizer.generateToken(Constants.Auth.GENERATED_TOKEN_CHARS, 8);

        String passwordChars = Globalizer.randomPassword(MIN_PASSWORD, MAX_PASSWORD);
        String password = securityManager.hashString(passwordChars);
        return new User(userName, "Anonymous", password, User.Status.ACTIVE);
    }

    @Transactional
    public ResponseEntity<MessageResponse> accountActivation(ActivateRequest request) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Your OTP is invalid. Pls try to contact admin team."));

        //Resend OTP to User
        if (user.getOtpExpired().before(new Date())) {
            try {
                this.mailService.activateLink(user, getActivateCallbackURL());
            } catch (JsonProcessingException ex) {
                throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
            }
            userRepository.save(user);
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Sorry! Your token has expired. We send new token to your email.");
        }

        user.setOtpToken(null);
        user.setOtpExpired(null);

        if (securityManager.getProperties().isRequireConfirm()) {
            user.setStatus(User.Status.ACTIVE);
        }
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "activation_success", "Your account is ready.", null));
    }

    @Transactional
    private User resetPasswordByToken(ActivateRequest request, String password) {
        User user = userRepository.checkOTP(request.getUser(), request.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Your OTP is invalid. Pls try to contact admin team."));

        if (user.getOtpExpired().before(new Date()) || !user.getOtpToken().equals(request.getToken())) {
            user.setOtpToken(null);
            user.setOtpExpired(null);
            userRepository.save(user);
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Your OTP is invalid. Pls try to contact admin team.");
        }

        String newPassword = securityManager.hashString(password);
        user.setPassword(newPassword);
        user.setOtpToken(null);
        user.setOtpExpired(null);
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

    public void enableMFA(User user) throws GeneralSecurityException {
        String totp = mfaService.generateCurrentTOTP(user.getMfaSecret(), user.getMfaType());
        switch (user.getMfaType()) {
            case SMS:
                //TODO: SMS
                break;
            case EMAIL:
                mailService.enableMFA(user, totp);
                break;
        }
    }

    public void verifyMFA(User user, String totp) throws GeneralSecurityException {
        String appTotp = mfaService.generateCurrentTOTP(user.getMfaSecret(), MfaService.TotpType.APP);
        String emailTotp = mfaService.generateCurrentTOTP(user.getMfaSecret(), MfaService.TotpType.EMAIL);
        String smsTotp = mfaService.generateCurrentTOTP(user.getMfaSecret(), MfaService.TotpType.SMS);

        if (!Arrays.asList(appTotp, emailTotp, smsTotp).contains(totp)) {
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Sorry! invalid otp code.");
        }
    }

    public ResponseEntity<MessageResponse> forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findFirstByPhoneNumberAndEmail(request.getPhoneNumber(), request.getEmail())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Invalid phone number (or) email address."));
        try {
            String url = request.getCallback();
            if (Globalizer.isNullOrEmpty(url)) {
                url = Globalizer.getCurrentContextPath("/auth/resetPassword", true);
            }

            mailService.forgetPasswordLink(user, url);
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "SUCCESS", "We send the reset password link to your e-mail.", null));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    private User authByPassword(String user, String rawPassword) {
        String password = securityManager.hashString(rawPassword);
        User authUser = userRepository.authByPassword(user, password)
                .orElseThrow(() -> {
                    increaseFailedCount();
                    return new GeneralException(HttpStatus.UNAUTHORIZED,
                            "Opp! request email or password is something wrong");
                });
        return authUser;
    }

    @Transactional
    public ResponseEntity<User> authByPasswordAndMfa(AuthRequest request) throws GeneralSecurityException {
        User authUser = authByPassword(request.getUser(), request.getPassword());
        this.verifyMFA(authUser, request.getMfa());
        jwtService.createToken(authUser, request, httpServletRequest);
        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity<User> authByPassword(AuthRequest request) throws GeneralSecurityException {
        User authUser = authByPassword(request.getUser(), request.getPassword());

        if (!authUser.isMfaEnabled())
            jwtService.createToken(authUser, request, httpServletRequest);
        else {
            this.enableMFA(authUser);
            throw new GeneralException(HttpStatus.NON_AUTHORITATIVE_INFORMATION, "Sorry! please log in with totp token.");
        }

        return ResponseEntity.ok(authUser);
    }

    @Transactional
    public ResponseEntity<User> registerByUserAndEmail(RegistrationRequest request) {
        //Check user by user name
        userRepository.findFirstByPhoneNumberOrEmail(request.getPhoneNumber(), request.getEmail())
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
