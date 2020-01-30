package com.sdm.facebook.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sdm.core.config.properties.FacebookProperties;
import com.sdm.facebook.service.MessengerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/facebook/messenger")
public class MessengerController {

    private static final Logger LOG = LoggerFactory.getLogger(MessengerController.class);

    @Autowired
    FacebookProperties properties;

    @Autowired
    MessengerService messengerService;

    @GetMapping("")
    public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") String mode,
                                                @RequestParam("hub.verify_token") String verify_token, @RequestParam("hub.challenge") String challenge) {
        if (mode.equalsIgnoreCase("subscribe") && verify_token.equalsIgnoreCase(this.properties.getWebhookToken())) {
            LOG.info("Facebook messenger platform verification success <" + challenge + ">.");
            return ResponseEntity.ok(challenge);
        }

        LOG.warn("Facebook messenger platform verification failed.");
        return ResponseEntity.status(403).build();
    }

    @PostMapping("")
    public ResponseEntity<Void> messageReceiver(@RequestBody String request, @RequestHeader(HttpHeaders.USER_AGENT) String userAgent) {
        LOG.info("Received from Facebook => " + request);
        JsonObject body = new Gson().fromJson(request, JsonObject.class);
        if (body.has("object") && !body.get("object").isJsonNull()
                && body.get("object").getAsString().equalsIgnoreCase("page")) {
            if (body.has("entry") && !body.get("entry").isJsonNull()) {

                JsonArray entries = body.get("entry").getAsJsonArray();
                for (int i = 0; i < entries.size(); i++) {
                    messengerService.messageAnaylsis(entries.get(i).getAsJsonObject(), userAgent);
                }
            }
        } else {
            LOG.warn("Invalid request or object type");
        }

        return ResponseEntity.ok().build();
    }


}

