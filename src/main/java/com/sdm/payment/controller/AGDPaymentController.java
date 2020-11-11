package com.sdm.payment.controller;

import com.sdm.payment.model.request.AGDResponseDirectPaymentRequest;
import com.sdm.payment.model.response.*;
import com.sdm.payment.service.AGDPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/agd/payments")
public class AGDPaymentController {
    /**
     * ONEPAY
     * AGD WALLET USER ID PHONE IS FOR EG./ 959421114920
     * TODO: TRANSACTION CANCEL,TIMEOUT, SYSTEM_ERROR
     */
    @Autowired
    AGDPaymentService agdPaymentService;

    @GetMapping("/public/test")
    public ResponseEntity<?> test()throws IOException{
        return null;
    }

    @PostMapping("/direct/payment")
    public ResponseEntity<?> AGDDirectPayment() throws IOException{
        AGDVerifyPhResponse phResponse =agdPaymentService.verifyPhone("959421114920");
        //TODO: record sequence no in order invoice
        String sequenceNo= UUID.randomUUID().toString().replace("-", "");
        AGDDirectPaymentResponse enResponse=agdPaymentService.directPayment("TEST01",sequenceNo,"1000","Testing","959421114920");
        return ResponseEntity.ok(enResponse);
    }

    @PostMapping("/public/direct/callback")
    public AGDResponseDirectPaymentResponse AGDEnquiryCallback(@Valid @RequestBody AGDResponseDirectPaymentRequest request) throws IOException {
        return agdPaymentService.directPaymentCallback(request);
    }

    @GetMapping("/check/{id}/transaction")
    public ResponseEntity<?> AGDCheckTransactionStatus(@PathVariable("id")String id)throws IOException{
        AGDCheckTransactionResponse transactionResponse =agdPaymentService.checkTransactionStatus(id);
        return ResponseEntity.ok(transactionResponse);
    }
}
