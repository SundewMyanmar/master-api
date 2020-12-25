package com.sdm.payment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sdm.payment.model.request.cbpay.CBResponsePaymentOrderRequest;
import com.sdm.payment.model.response.cbpay.CBCheckPaymentStatusResponse;
import com.sdm.payment.model.response.cbpay.CBPaymentOrderResponse;
import com.sdm.payment.model.response.cbpay.CBResponsePaymentOrderResponse;
import com.sdm.payment.service.CBPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/cb/payments")
public class CBPaymentController {
    /**
     * CBPAY
     * TODO: TRANSACTION CANCEL,TIMEOUT, SYSTEM_ERROR
     * <p>
     * Payment expired 5 min
     * callback က real-time တစ်ကြိမ်ပဲ ခေါ်ပါတယ် အစ်ကို ... timeout ဖြစ်သွားခဲ့ရင်တော့ user ကို merchant app ဘက်မှာ order ပြန်ကြည့်တဲ့နေရာမျိုးမှာ e.g. "update order status" button နှိပ်ခိုင်းပြီး Check Payment Status ကို ခေါ်ပေးရပါမယ်
     */

    @Autowired
    CBPaymentService cbPaymentService;

    @PostMapping("/check/{id}/deeplink")
    public ResponseEntity<?> CBDeepLink(@PathVariable("id") String id) {
        return ResponseEntity.ok(cbPaymentService.getDeepLink(id));
    }

    @PostMapping("/payment/order")
    public ResponseEntity<?> CBPaymentOrder() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        CBPaymentOrderResponse pResponse = cbPaymentService.paymentOrder("1", "TEST-1", "1000.00");
        return ResponseEntity.ok(pResponse);
    }

    @PostMapping("/check/{id}/order")
    public ResponseEntity<?> CBCheckOrderStatus(@PathVariable("id") String generateRefOrder) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        CBCheckPaymentStatusResponse response = cbPaymentService.checkPaymentStatus("1", generateRefOrder);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/order/callback")
    public CBResponsePaymentOrderResponse CBPaymentOrderNotify(@Valid @RequestBody CBResponsePaymentOrderRequest request) throws JsonProcessingException, NoSuchAlgorithmException {
        return cbPaymentService.paymentOrderNotifyCallback(request, "1");
//        CBResponsePaymentOrderResponse response=new CBResponsePaymentOrderResponse("0000","OPERATION SUCCESS");
//        return response;
    }
}
