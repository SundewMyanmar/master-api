package com.sdm.facebook.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sdm.core.util.FBGraphManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MessengerService {

    @Autowired
    FBGraphManager graphManager;

    /**
     * https://developers.facebook.com/docs/messenger-platform/reference/webhook-events
     *
     * @param pageEntry
     */
    public void messageAnaylsis(JsonObject pageEntry, String userAgent) {
        String pageId = pageEntry.get("id").toString();
        long entryTime = pageEntry.get("time").getAsLong();

        JsonArray messaging = pageEntry.get("messaging").getAsJsonArray();

        for (int i = 0; i < messaging.size(); i++) {
            JsonObject body = messaging.get(i).getAsJsonObject();

            String senderId = body.get("sender").getAsJsonObject().get("id").getAsString();
            String recipientId = body.get("recipient").getAsJsonObject().get("id").getAsString();

            if (body.has("timestamp")) {
                long messageTime = body.get("timestamp").getAsLong();
            }

            if (body.has("message")) {
                JsonObject message = body.get("message").getAsJsonObject();
                if (message.has("is_echo") && message.get("is_echo").getAsBoolean()) {

                }
            } else if (body.has("delivery")) {

            } else if (body.has("read")) {

            } else if (body.has("postback")) {

            } else if (body.has("optin")) {

            } else if (body.has("referral")) {

            } else if (body.has("payment")) {

            } else if (body.has("checkout_update")) {

            } else if (body.has("pre_checkout")) {

            } else if (body.has("account_linking")) {

            } else if (body.has("game_play")) {

            } else if (body.has("pass_thread_control")) {

            } else if (body.has("policy-enforcement")) {

            }
        }
    }
}