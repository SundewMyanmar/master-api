package com.sdm.core.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.BarCodeManager;
import com.sdm.core.util.Globalizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/util")
public class UtilController {
    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PropertyConfig appConfig;

    @Autowired
    private BarCodeManager barCodeManager;

    @GetMapping("/generate/{len}")
    public ResponseEntity<MessageResponse> generateRandomLetter(@PathVariable("len") int len) {
        String generated = Globalizer.randomPassword(len);
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK, "GENERATE", generated, null));
    }

    @GetMapping("/barcode/{type}")
    public ResponseEntity<?> generateBarcode(
            @PathVariable("type") BarcodeFormat format,
            @RequestParam("input") String input,
            @RequestParam(value = "width", required = false, defaultValue = "128") int width,
            @RequestParam(value = "height", required = false, defaultValue = "48") int height,
            @RequestParam(value = "noMargin", required = false, defaultValue = "false") boolean noMargin) throws IOException, WriterException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (format == BarcodeFormat.QR_CODE) {
            barCodeManager.createQR(outputStream, input, width, noMargin);
        } else {
            barCodeManager.createBarcode(outputStream, format, input, width, height, noMargin);
        }

        String filename = input + ".png";
        String attachment = "attachment; filename=\"" + filename + "\"";
        Resource resource = new ByteArrayResource(outputStream.toByteArray());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment)
                .body(resource);
    }
}
