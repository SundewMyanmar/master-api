package com.sdm.core.controller;

import com.sdm.Constants;
import com.sdm.core.component.VelocityTemplateManager;
import com.sdm.core.component.WebMailManager;
import com.sdm.core.model.MailHeader;
import com.sdm.core.model.response.MessageModel;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.MyanmarFontManager;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class RootController implements ErrorController {

    @Autowired
    SecurityManager securityManager;

    @Autowired
    protected VelocityTemplateManager templateManager;

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping("")
    public ResponseEntity welcomeUser() {
        MessageModel message = MessageModel.createMessage("Welcome!", "Never give up to be a warrior.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/error")
    public ResponseEntity handleError(HttpServletRequest request) {
        try {
            int code = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            HttpStatus status = HttpStatus.valueOf(code);
            String message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE).toString();

            return ResponseEntity.ok(MessageModel.createMessage(status, status.series().name(), message));
        }catch(Exception error){
            return ResponseEntity.ok(MessageModel.createMessage("ERROR", "Our Engineers are on it!"));
        }
    }

    @GetMapping("/public/privacy")
    public String privacyPolicy(){
        Map<String, Object> data = new HashMap<>();
        data.put("title", Constants.APP_NAME);
        data.put("email", Constants.INFO_MAIL);
        data.put("date", Globalizer.getDateString("MMMM, dd YYYY", new Date()));

        return templateManager.buildTemplate("privacy-policy.vm", data);
    }

    @GetMapping("/util/jwtKey")
    public ResponseEntity generateJwtKey() {
        String generated = securityManager.generateJWTKey();
        return ResponseEntity.ok(MessageModel.createMessage("JWT_KEY", generated));
    }

    @GetMapping("/util/salt")
    public ResponseEntity generateSalt() {
        String generated = securityManager.generateSalt();
        return ResponseEntity.ok(MessageModel.createMessage("ENCRYPT_SALT", generated));
    }

    @GetMapping("/util/passwordGenerate/{len}")
    public ResponseEntity generateRandomLetter(@PathVariable("len") int len) {
        String generated = securityManager.randomPassword(len);
        return ResponseEntity.ok(MessageModel.createMessage("GENERATE", generated));
    }

    @GetMapping("/util/mmConverter")
    public ResponseEntity langConverter(@RequestParam("input") String input) {
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

        MessageModel message = new MessageModel(HttpStatus.BAD_REQUEST, "No! It is not myanmar font.");
        return new ResponseEntity(message, message.getStatus());
    }
}
