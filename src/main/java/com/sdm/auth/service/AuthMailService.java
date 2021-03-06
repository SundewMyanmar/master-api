package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.Constants;
import com.sdm.admin.model.User;
import com.sdm.auth.model.request.ActivateRequest;
import com.sdm.core.config.properties.SecurityProperties;
import com.sdm.core.model.MailHeader;
import com.sdm.core.security.AESManager;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.service.MailService;
import com.sdm.core.util.Globalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;

@Service
public class AuthMailService {

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    AESManager aesManager;

    @Autowired
    MailService mailManager;

    @Autowired
    SecurityProperties securityProperties;

    private static final int OTP_LENGTH = 8;

    private void sendOtpMail(User user, String template, String subject, String otpLink) {
        Map<String, Object> data = Map.of("expire", user.getOtpExpired(), "user", user.getDisplayName(), "token_url",
                otpLink, "current_year", Globalizer.getDateString("yyyy", new Date()));

        mailManager.sendByTemplate(new MailHeader(user.getEmail(), subject), template, data);
    }

    private ActivateRequest buildRequest(User user) {
        // setToken
        String otpToken = Globalizer.generateToken(Constants.Auth.GENERATED_TOKEN_CHARS, OTP_LENGTH);
        Date expiredDate = Globalizer.addDate(new Date(), securityManager.getProperties().getOtpLife());

        // Set Otp Info in User
        user.setOtpToken(otpToken);
        user.setOtpExpired(expiredDate);

        // Create Activate Request
        ActivateRequest request = new ActivateRequest();
        request.setToken(user.getOtpToken());
        if (!StringUtils.isEmpty(user.getPhoneNumber())) {
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
        String token = aesManager.encrypt(jacksonObjectMapper.writeValueAsString(request), securityProperties.getEncryptSalt());

        String link = contextPath + "?token=" + Globalizer.encodeUrl(token) + "&user=" + Globalizer.encodeUrl(user.getEmail());

        // Send mail with Forget Password Link
        this.sendOtpMail(user, "mail/forget-password",
                "Forget password activation link.",
                link);
    }

    @Async
    public void enableMFA(User user, String otp) {
        Map<String, Object> data = Map.of("user", user.getDisplayName(), "current_year", Globalizer.getDateString("yyyy", new Date()),
                "expire", user.getMfaType().value() / 60, "otp", otp);

        mailManager.sendByTemplate(new MailHeader(user.getEmail(), "Two Factor Verification"), "mail/mfa-verify", data);
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
