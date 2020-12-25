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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/auth/activate").toUriString();
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

    public ResponseEntity<User> resetPasswordByOtp(ChangePasswordRequest changePasswordRequest, ActivateRequest activateRequest) {
        User user = userRepository.checkOTP(activateRequest.getUser(), activateRequest.getToken())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                        "Your OTP is invalid. Pls try to contact admin team."));

        if (user.getOtpExpired().before(new Date()) || !user.getOtpToken().equals(activateRequest.getToken())) {
            user.setOtpToken(null);
            user.setOtpExpired(null);
            userRepository.save(user);
            throw new GeneralException(HttpStatus.NOT_ACCEPTABLE,
                    "Your OTP is invalid. Pls try to contact admin team.");
        }

        String newPassword = securityManager.hashString(changePasswordRequest.getNewPassword());
        user.setPassword(newPassword);
        user.setOtpToken(null);
        user.setOtpExpired(null);
        userRepository.save(user);

        return ResponseEntity.ok(user);
    }

    public ResponseEntity<MessageResponse> forgetPassword(ForgetPasswordRequest request) {
        User user = userRepository.findFirstByPhoneNumberAndEmail(request.getPhoneNumber(), request.getEmail())
                .orElseThrow(() -> new GeneralException(HttpStatus.NOT_ACCEPTABLE, "Invalid phone number (or) email address."));
        try {
            mailService.forgetPasswordLink(user, request.getCallback());
            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "send_otp", "We send the reset password link to your e-mail.", null));
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage());
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    @Transactional
    public ResponseEntity<User> authByPassword(AuthRequest request) {
        String password = securityManager.hashString(request.getPassword());
        User authUser = userRepository.authByPassword(request.getUser(), password)
                .orElseThrow(() -> {
                    increaseFailedCount();
                    return new GeneralException(HttpStatus.UNAUTHORIZED,
                            "Opp! request email or password is something wrong");
                });

        jwtService.createToken(authUser, request, httpServletRequest);

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
