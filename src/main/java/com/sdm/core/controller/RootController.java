package com.sdm.core.controller;

import com.sdm.Constants;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.MyanmarFontManager;
import com.sdm.core.util.security.SecurityManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Controller
@Log4j2
public class RootController implements ErrorController {

    @Autowired
    SecurityManager securityManager;

    @Autowired
    private PropertyConfig appConfig;

    @Autowired
    private SpringTemplateEngine templateEngine;


    @SuppressWarnings("unchecked")
    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("")
    public ResponseEntity<MessageResponse> welcome() {
        MessageResponse message = new MessageResponse("Welcome!", "Never give up to be a warrior.");
        return ResponseEntity.ok(message);
    }

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<MessageResponse> handleError(HttpServletRequest request) {
        MessageResponse response = new MessageResponse();
        try {
            Map<String, Object> detail = new HashMap<>();
            int code = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            HttpStatus status = HttpStatus.valueOf(code);
            response.setStatus(status);
            response.setTitle(status.getReasonPhrase());

            String message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString();
            Exception ex = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            if (ex != null) {
                if (StringUtils.isEmpty(message)) {
                    message = ex.getLocalizedMessage();
                } else {
                    detail.put("exception", ex.getMessage());
                }
            }
            response.setMessage(message);

            Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            if (path != null) {
                detail.put("path", path.toString());
            }

            Object query = request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
            if (query != null) {
                detail.put("query", query.toString());
            }

            response.setDetails(detail);
        } catch (Exception error) {
            response.setTitle("SYSTEM_ERROR");
            response.setMessage(error.getLocalizedMessage());
        }
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping("/public/privacy")
    public String privacyPolicy() {
        Context context = new Context();
        context.setVariables(Map.of("title", Constants.APP_NAME, "email", Constants.INFO_MAIL, "today", Calendar.getInstance()));
        return templateEngine.process("privacy-policy", context);
    }

    @GetMapping("/util/jwtKey")
    public ResponseEntity<MessageResponse> generateJwtKey() {
        String generated = securityManager.generateJWTKey();
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "JWT_KEY", generated, null));
    }

    @GetMapping("/util/salt")
    public ResponseEntity<MessageResponse> generateSalt() {
        String generated = securityManager.generateSalt();
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "ENCRYPT_SALT", generated, null));
    }

    @GetMapping("/util/generate/{len}")
    public ResponseEntity<MessageResponse> generateRandomLetter(@PathVariable("len") int len) {
        String generated = securityManager.randomPassword(len);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "GENERATE", generated, null));
    }

    @GetMapping("/util/encryptProperty")
    public ResponseEntity<MessageResponse> encryptProperty(@RequestParam("input") String input) {
        String encrypted = "ENC(" + appConfig.stringEncryptor().encrypt(input) + ")";
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "ENCRYPTED", encrypted, null));
    }

    @GetMapping("/util/mmConverter")
    public ResponseEntity<Object> langConverter(@RequestParam("input") String input) {
        HashMap<String, String> content = new HashMap<>();
        if (MyanmarFontManager.isMyanmar(input)) {
            String msgString = "Yes! It is myanmar";
            if (!MyanmarFontManager.isZawgyi(input)) {
                msgString += " unicode font.";
                content.put("unicode", input);
                content.put("zawgyi", MyanmarFontManager.toZawgyi(input));
            } else if (MyanmarFontManager.isZawgyi(input)) {
                msgString += " zawgyi font.";
                content.put("zawgyi", input);
                content.put("unicode", MyanmarFontManager.toUnicode(input));
            }
            content.put("message", msgString);
            return ResponseEntity.ok(content);
        }

        MessageResponse message = new MessageResponse(HttpStatus.BAD_REQUEST, "No! It is not myanmar font.");
        return ResponseEntity.ok(message);
    }

}
