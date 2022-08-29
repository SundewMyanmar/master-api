package com.sdm.payment.controller;

import com.sdm.core.controller.DefaultController;
import com.sdm.core.exception.GeneralException;
import com.sdm.payment.model.request.mpu.MPUPayment;
import com.sdm.payment.repository.MPUPaymentRepository;
import com.sdm.payment.service.MPUPaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/public/payments/mpu")
@Log4j2
public class MPUPaymentController extends DefaultController {
    @Autowired
    private MPUPaymentRepository repository;

    @Autowired
    private MPUPaymentService paymentService;

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView paymentRequest(@PathVariable("id") String id,
                                       @DefaultValue("") @RequestParam(value = "callback", required = false) String callbackUrl) {
        MPUPayment request = repository.findById(id)
                .orElseThrow(() -> new GeneralException(HttpStatus.BAD_REQUEST, localeManager.getMessage("no-data")));
        return paymentService.buildModelAndView(request, callbackUrl);
    }
}
