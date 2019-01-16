package com.sdm.core.controller;

import com.sdm.core.model.response.MessageModel;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.MyanmarFontManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    SecurityManager securityManager;

    @GetMapping("/")
    public ResponseEntity welcomeUser() {
        MessageModel message = MessageModel.createMessage("Welcome!", "Never give up to be a warrior.");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/jwtKey")
    public ResponseEntity generateJwtKey() {
        String generated = securityManager.generateJWTKey();
        return ResponseEntity.ok(generated);
    }

    @GetMapping("/salt")
    public ResponseEntity generateSalt() {
        String generated = securityManager.generateSalt();
        return ResponseEntity.ok(generated);
    }

    @GetMapping("/passwordGenerate/{len}")
    public ResponseEntity generateRandomLetter(@PathVariable("len") int len) {
        String generated = securityManager.randomPassword(len);
        return ResponseEntity.ok(generated);
    }

    @GetMapping("/mmConverter")
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
