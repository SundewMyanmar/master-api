package com.sdm.core.controller;

import com.sdm.Constants;
import com.sdm.core.config.PropertyConfig;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.core.security.SecurityManager;
import com.sdm.core.util.Globalizer;
import com.sdm.core.util.LocaleManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
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

    @Autowired
    private LocaleManager localeManager;

    @GetMapping("")
    public ResponseEntity<MessageResponse> welcome() {
        MessageResponse message = new MessageResponse(localeManager.getMessage("welcome-title"), localeManager.getMessage("welcome-message"));
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
                if (Globalizer.isNullOrEmpty(message)) {
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

    @GetMapping(value = "/public/privacy", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView privacyPolicy() {
        ModelAndView response = new ModelAndView("privacy-policy");
        response.addAllObjects(Map.of("title", Constants.APP_NAME, "email", Constants.INFO_MAIL, "today", Calendar.getInstance()));
        return response;
    }
}
