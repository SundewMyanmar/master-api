package com.sdm.sms.controller;

import com.sdm.core.controller.DefaultController;
import com.sdm.core.model.response.MessageResponse;
import com.sdm.sms.model.request.telenor.TelenorTokenSetting;
import com.sdm.sms.service.TelenorSmsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Log4j2
@Controller
@RequestMapping("/public/sms")
public class TelenorSmsController extends DefaultController {
    @Autowired
    private TelenorSmsService telenorSmsService;

    @GetMapping("/telenor/loadToken")
    public ResponseEntity<MessageResponse> loadToken() {
//        try {
//            this.telenorSmsService.requestCode();
//            return ResponseEntity.ok(new MessageResponse("Request to telenor gateway server."));
//        } catch (MalformedURLException | JsonProcessingException e) {
//            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
//        }
        return null;
    }

    @GetMapping("/telenor/sendMessage")
    public ResponseEntity<?> testSendMessage(@RequestParam("content") String message, @RequestParam("phone") String[] phones) throws IOException {
//        Map<String,String> result=telenorSmsService.sendMessage(
//                "Hello Mingalar par! Welcome, +Telenor ဟယ်လို လင်း မြန်မာ+"
//                ,phones, MessageType.MULTILINGUAL);
//        return ResponseEntity.ok(result);
        return null;
    }

    @GetMapping("/telenor/callback")
    public ResponseEntity<TelenorTokenSetting> callback(@DefaultValue("") @RequestParam("code") String code,
                                                        @DefaultValue("") @RequestParam("scope") String scope) {
//        try {
//            log.info("TELENOR_CALLBACK => "+code+", "+scope);
//            TelenorTokenSetting result=this.telenorSmsService.requestAccessToken(code);
//            return ResponseEntity.ok(result);
//        } catch (IOException e) {
//            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
//        }
        return null;
    }
}
