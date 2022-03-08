package com.sdm.core.controller;

import com.sdm.core.Constants;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.exception.GeneralException;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.NoSuchAlgorithmException;
import java.util.Set;

@Log4j2
@Controller
@RequestMapping("/setup")
public class SetupController {
    private final Set<String> ENCRYPT_FIELDS = Set.of(
            "dbUser", "dbPassword",
            "mailUser", "mailPassword"
    );
    @Autowired
    SecurityManager securityManager;

    @Autowired
    private PropertyConfig appConfig;

    @GetMapping("")
    public ModelAndView getIndex() {
        ModelAndView response = new ModelAndView("setup/index");
        response.addObject("title", Constants.APP_NAME);
        return response;
    }

    @PostMapping("")
    public ModelAndView postIndex(@RequestBody MultiValueMap<String, String> formData) {
        ModelAndView response = new ModelAndView("setup/generate");
        response.addObject("title", Constants.APP_NAME);
        try {
            response.addObject("secretKey", securityManager.generateAESKey(256));
            response.addObject("secretIv", securityManager.generateIv());
        } catch (NoSuchAlgorithmException e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
        response.addObject("jwtKey", securityManager.generateJWTKey());
        for (String key : formData.keySet()) {
            String value = formData.getFirst(key);
            if (!Globalizer.isNullOrEmpty(value) && ENCRYPT_FIELDS.contains(key)) {
                value = "ENC(" + appConfig.stringEncryptor().encrypt(value) + ")";
            }
            response.addObject(key, value);
        }
        return response;
    }
}
