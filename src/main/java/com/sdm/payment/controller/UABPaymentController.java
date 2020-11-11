package com.sdm.payment.controller;

import com.sdm.payment.model.request.UABResponsePaymentRequest;
import com.sdm.payment.model.response.*;
import com.sdm.payment.service.UABPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/uab/payments")
public class UABPaymentController {
    /**
     * SAISAIPAY
     * UAB WALLET USER ID PHONE IS FOR EG./ 09421114920
     * TODO: TRANSACTION CANCEL,TIMEOUT, SYSTEM_ERROR
     */
    @Autowired
    UABPaymentService uabPaymentService;

    @GetMapping("/public/test")
    public ResponseEntity<?> test() throws IOException {
        UABLoginResponse response=uabPaymentService.login();
        UABCheckPhResponse phResponse =uabPaymentService.checkPhone("09797804283",response.getToken());
        return ResponseEntity.ok(phResponse);
    }

    @PostMapping("/enquiry/payment")
    public ResponseEntity<?> UABEnquiryPayment() throws IOException{
        UABLoginResponse response=uabPaymentService.login();
        UABCheckPhResponse phResponse =uabPaymentService.checkPhone("09797804283",response.getToken());
        //TODO: record sequence no in order invoice
        String sequenceNo=UUID.randomUUID().toString().replace("-", "");
        UABEnquiryPaymentResponse enResponse=uabPaymentService.enquiryPayment("TEST01",sequenceNo,"1000","Testing","09797804283",response.getToken());
        return ResponseEntity.ok(enResponse);
    }

    @PostMapping("/public/enquiry/callback")
    public UABResponsePaymentResponse UABEnquiryCallback(@Valid @RequestBody UABResponsePaymentRequest request) throws IOException {
        return uabPaymentService.enquiryPaymentCallback(request);
    }

    @GetMapping("/check/{id}/transaction")
    public ResponseEntity<?> UABCheckTransactionStatus(@PathVariable("id")String id)throws IOException{
        UABLoginResponse response=uabPaymentService.login();
        UABCheckTransactionResponse transactionResponse =uabPaymentService.checkTransactionStatus(id,response.getToken());
        return ResponseEntity.ok(transactionResponse);
    }
}
