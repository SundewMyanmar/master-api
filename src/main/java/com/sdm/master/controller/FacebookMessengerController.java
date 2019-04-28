package com.sdm.master.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sdm.core.config.FacebookProperties;
import com.sdm.master.service.FBMessengerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/facebook/messenger")
public class FacebookMessengerController {

    private static final Logger LOG = LoggerFactory.getLogger(FacebookMessengerController.class);

    @Autowired
    FacebookProperties properties;

    @Autowired
    FBMessengerService messengerService;

    @GetMapping("/")
    public ResponseEntity verifyWebhook(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verify_token, @RequestParam("hub.challenge") String challenge) {
        if (mode.equalsIgnoreCase("subscribe") && verify_token.equalsIgnoreCase(this.properties.getWebhookToken())) {
            LOG.info("Facebook messenger platform verification success <" + challenge + ">.");
            return ResponseEntity.ok(challenge);
        }

        LOG.warn("Facebook messenger platform verification failed.");
        return ResponseEntity.status(403).build();
    }

    @PostMapping("/")
    public ResponseEntity messageReceiver(@RequestBody String request) {
        LOG.info("Received from Facebook => " + request);
        JsonObject body = new Gson().fromJson(request, JsonObject.class);
        if (body.has("object") && !body.get("object").isJsonNull()
                && body.get("object").getAsString().equalsIgnoreCase("page")) {
            if (body.has("entry") && !body.get("entry").isJsonNull()) {

                JsonArray entries = body.get("entry").getAsJsonArray();
                for (int i = 0; i < entries.size(); i++) {
                    messengerService.messageAnaylsis(entries.get(i).getAsJsonObject());
                }
            }
        } else {
            LOG.warn("Invalid request or object type");
        }

        return ResponseEntity.ok().build();
    }
}