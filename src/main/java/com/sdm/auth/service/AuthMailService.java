package com.sdm.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdm.admin.model.User;
import com.sdm.auth.model.request.ActivateRequest;
import com.sdm.core.model.MailHeader;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.WebMailManager;
import com.sdm.core.util.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthMailService {

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    SecurityManager securityManager;

    @Autowired
    WebMailManager mailManager;

    private static final int OTP_LENGTH = 8;

    private void sendOtpMail(User user, String template, String subject, String otpLink) {
        Map<String, Object> data = new HashMap<>();
        data.put("expire", user.getOtpExpired());
        data.put("user", user.getDisplayName());
        data.put("token_url", ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(otpLink)
                .toUriString());
        data.put("current_year", Globalizer.getDateString("yyyy", new Date()));

        mailManager.sendByTemplate(new MailHeader(user.getEmail(), subject),
                template, data);
    }

    private ActivateRequest buildRequest(User user) {
        //setToken
        String otpToken = Globalizer.generateToken(securityManager.getProperties().getTokenChars(), OTP_LENGTH);

        long minutes = securityManager.getProperties().getOtpLife().toMinutes();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, Integer.parseInt(String.valueOf(minutes)));

        //Set Otp Info in User
        user.setOtpToken(otpToken);
        user.setOtpExpired(cal.getTime());

        // Create Activate Request
        ActivateRequest request = new ActivateRequest();
        request.setToken(user.getOtpToken());
        if (!user.getPhoneNumber().isEmpty()) {
            request.setUser(user.getPhoneNumber());
        } else {
            request.setUser(user.getEmail());
        }

        return request;
    }

    public void forgetPasswordLink(User user) throws JsonProcessingException {
        ActivateRequest request = buildRequest(user);
        String token = securityManager.base64Encode(jacksonObjectMapper.writeValueAsString(request));

        // Send mail with Forget Password Link
        this.sendOtpMail(user, "mail/forget-password.vm",
                "Forget password activation link.",
                "/auth/resetPassword/?token=" + token);
    }

    public void activateLink(User user) throws JsonProcessingException {
        ActivateRequest request = buildRequest(user);
        String token = securityManager.base64Encode(jacksonObjectMapper.writeValueAsString(request));
        // Send mail with activation link
        this.sendOtpMail(user, "mail/auth-activate.vm",
                "Activate your account.",
                "/auth/activate/?token=" + token);
    }

    public void welcomeUser(User user, String rawPassword, String title) {

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("email", user.getEmail());
        data.put("name", user.getDisplayName());
        data.put("password", rawPassword);
        data.put("current_year", Globalizer.getDateString("yyyy", new Date()));
        mailManager.sendByTemplate(new MailHeader(user.getEmail(), "Your account info!"),
                "mail/create-user.vm", data);
    }
}
