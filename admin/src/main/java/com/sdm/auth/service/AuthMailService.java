package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.admin.model.User;
import com.sdm.auth.model.request.ActivateRequest;
import com.sdm.core.Constants;
import com.sdm.core.model.MailHeader;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.service.MailService;
import com.sdm.core.util.Globalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Service
public class AuthMailService {
    private static final int OTP_LENGTH = 8;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    MailService mailManager;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    private void sendOtpMail(User user, String template, String subject, String otpLink) {
        Map<String, Object> data = Map.of("expire", user.getActivateTokenExpire(), "user", user.getDisplayName(), "token_url",
                otpLink, "current_year", Globalizer.getDateString("yyyy", new Date()));

        mailManager.sendByTemplate(new MailHeader(user.getEmail(), subject), template, data);
    }

    private ActivateRequest buildRequest(User user) {
        // setToken
        String activateToken = Globalizer.generateToken(Constants.Auth.GENERATED_TOKEN_CHARS, OTP_LENGTH);
        Date expiredDate = Globalizer.addDate(new Date(), Duration.ofMinutes(10));

        // Set Otp Info in User
        user.setActivateToken(activateToken);
        user.setActivateTokenExpire(expiredDate);

        // Create Activate Request
        ActivateRequest request = new ActivateRequest();
        request.setToken(user.getActivateToken());
        if (!Globalizer.isNullOrEmpty(user.getPhoneNumber())) {
            request.setUser(user.getPhoneNumber());
        } else {
            request.setUser(user.getEmail());
        }

        return request;
    }

    @Async
    public void forgetPasswordLink(User user, String contextPath)
            throws JsonProcessingException {
        ActivateRequest request = buildRequest(user);
        String token = securityManager.aesEncrypt(jacksonObjectMapper.writeValueAsString(request));

        String link = contextPath + "?token=" + Globalizer.encodeUrl(token) + "&user=" + Globalizer.encodeUrl(user.getEmail());

        // Send mail with Forget Password Link
        this.sendOtpMail(user, "mail/forget-password",
                "Forget password activation link.",
                link);
    }

    @Async
    public void sendMfa(String email, String code, Date expire) {
        Map<String, Object> data = Map.of("current_year", Globalizer.getDateString("yyyy", new Date()),
                "expire", expire, "otp", code);

        mailManager.sendByTemplate(new MailHeader(email, "Two Factor Verification"), "mail/mfa-verify", data);
    }

    @Async
    public void activateLink(User user, String callbackUrl) throws JsonProcessingException {
        ActivateRequest request = buildRequest(user);
        String token = securityManager.base64Encode(jacksonObjectMapper.writeValueAsString(request));

        String link = callbackUrl + "?token=" + token;
        // Send mail with activation link
        this.sendOtpMail(user, "mail/auth-activate",
                "Activate your account.", link);
    }

    @Async
    public void welcomeUser(User user, String rawPassword, String title) {
        Map<String, Object> data = Map.of(
                "title", title,
                "email", user.getEmail(),
                "name", user.getDisplayName(),
                "password", rawPassword,
                "current_year", Globalizer.getDateString("yyyy", new Date())
        );
        mailManager.sendByTemplate(new MailHeader(user.getEmail(), "Your account info!"),
                "mail/create-user", data);
    }
}
