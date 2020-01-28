package com.sdm.core.controller;

import com.sdm.Constants;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.MyanmarFontManager;
import com.sdm.core.util.VelocityTemplateManager;
import com.sdm.core.util.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RootController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    SecurityManager securityManager;

    @Autowired
    protected VelocityTemplateManager templateManager;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("")
    public ResponseEntity<MessageResponse> welcomeUser() {
        MessageResponse message = new MessageResponse(HttpStatus.OK, "Welcome!", "Never give up to be a warrior.", null);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/error")
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
                if (message == null || message.length() <= 0) {
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
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/privacy")
    public String privacyPolicy() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", Constants.APP_NAME);
        data.put("email", Constants.INFO_MAIL);
        data.put("date", Globalizer.getDateString("MMMM, dd YYYY", new Date()));

        return templateManager.buildTemplate("privacy-policy.vm", data);
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

    @GetMapping("/util/passwordGenerate/{len}")
    public ResponseEntity<MessageResponse> generateRandomLetter(@PathVariable("len") int len) {
        String generated = securityManager.randomPassword(len);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "GENERATE", generated, null));
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
