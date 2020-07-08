package com.sdm.core.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.util.BarCodeManager;
import com.sdm.core.util.MyanmarFontManager;
import com.sdm.core.util.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/util")
public class UtilController {
    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PropertyConfig appConfig;

    @Autowired
    private BarCodeManager barCodeManager;

    @GetMapping("/jwtKey")
    public ResponseEntity<MessageResponse> generateJwtKey() {
        String generated = securityManager.generateJWTKey();
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "JWT_KEY", generated, null));
    }

    @GetMapping("/salt")
    public ResponseEntity<MessageResponse> generateSalt() {
        String generated = securityManager.generateSalt();
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "ENCRYPT_SALT", generated, null));
    }

    @GetMapping("/generate/{len}")
    public ResponseEntity<MessageResponse> generateRandomLetter(@PathVariable("len") int len) {
        String generated = securityManager.randomPassword(len);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "GENERATE", generated, null));
    }

    @GetMapping("/encryptProperty")
    public ResponseEntity<MessageResponse> encryptProperty(@RequestParam("input") String input) {
        String encrypted = "ENC(" + appConfig.stringEncryptor().encrypt(input) + ")";
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "ENCRYPTED", encrypted, null));
    }

    @GetMapping("/barcode/{type}")
    public ResponseEntity<?> generateBarcode(
            @PathVariable("type") BarcodeFormat format,
            @RequestParam("input") String input,
            @RequestParam(value = "width", required = false, defaultValue = "128") int width,
            @RequestParam(value = "height", required = false, defaultValue = "48") int height) throws IOException, WriterException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (format == BarcodeFormat.QR_CODE) {
            barCodeManager.createQR(outputStream, input, width);
        } else {
            barCodeManager.createBarcode(outputStream, format, input, width, height);
        }

        String filename = input + ".png";
        String attachment = "attachment; filename=\"" + filename + "\"";
        Resource resource = new ByteArrayResource(outputStream.toByteArray());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }

    @GetMapping("/mmConverter")
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
