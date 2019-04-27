package com.sdm.master.controller;

import com.sdm.core.component.FBGraphManager;
import com.sdm.core.config.FacebookProperties;
import com.sdm.core.model.facebook.MessageBuilder;
import com.sdm.core.model.facebook.webhook.MessengerEntry;

import org.json.JSONArray;
import org.json.JSONObject;
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
    FBGraphManager manager;

    @GetMapping("/")
    public ResponseEntity activateMessenger(@RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verify_token, 
            @RequestParam("hub.challenge") String challenge) {
        if(mode.equalsIgnoreCase("subscribe") && verify_token.equalsIgnoreCase(this.properties.getWebhookToken())){
            LOG.info("Facebook messenger platform verification success <" + challenge + ">.");
            return ResponseEntity.ok(challenge);
        }

        LOG.warn("Facebook messenger platform verification failed.");
        return ResponseEntity.status(403).build();
    }

    @PostMapping("/")
    public ResponseEntity messengerWebHook(@RequestBody String request) {
        LOG.info("Received from Facebook => " + request);
        JSONObject body = new JSONObject(request);
        if(body.has("object") && body.getString("object").equalsIgnoreCase("page")){
            if (body.has("entry")) {
                JSONArray entries = body.getJSONArray("entry");
                LOG.info("Entry from Facebook => " + entries);
                for (int i = 0; i < entries.length(); i++) {
                    MessengerEntry entry = new MessengerEntry();
                    entry.deserialize(entries.getJSONObject(i));
                    String sender = entry.getMessages().get(0).getSenderId();
                    
                    //Send Message
                    manager.sendText(sender, "Thank you for your message!");
                }
            }
        }else{
            LOG.warn("Invalid request or object type");
        }

        return ResponseEntity.ok().build();
    }
}