package com.sdm.core.model.facebook;

import com.sdm.core.model.facebook.type.WebViewType;

import org.json.JSONObject;

public class Button {
    /**
     * The call button dials a phone number when tapped. Phone number should be in
     * the format +<COUNTRY_CODE><PHONE_NUMBER>, e.g. +15105559999.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/call
     * 
     * @param title
     * @param number
     * @return JSONObject
     */
    public static JSONObject call(String title, String number) {
        JSONObject button = new JSONObject();
        button.put("type", "phone_number");
        button.put("title", title);
        button.put("payload", number);
        return button;
    }

    /**
     * The game play button launches an Instant Game that is associated with your
     * Facebook Page. To customize how your game is opened, you can set a payload
     * property in the request that will be sent to the game on launch, as well as
     * an optional game_metadata.player_id or game_metadata.context_id property,
     * which allows your bot to start the game in a specific context against a
     * single player or an existing group.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/game-play
     * 
     * @param title
     * @param data
     * @param playerId
     * @param contextId
     * @return JSONObject
     */
    public static JSONObject gameplay(String title, String data, String playerId, String contextId) {
        JSONObject button = new JSONObject();
        button.put("type", "game_play");
        button.put("title", title);
        button.put("payload", data);

        if (playerId != null || contextId != null) {
            JSONObject meta = new JSONObject();
            if (playerId != null && playerId.length() > 0) {
                meta.put("player_id", playerId);
            }

            if (contextId != null && contextId.length() > 0) {
                meta.put("context_id", contextId);
            }
            button.put("game_metadata", meta);
        }
        return button;
    }

    /**
     * The game play button launches an Instant Game that is associated with your
     * Facebook Page. To customize how your game is opened, you can set a payload
     * property in the request that will be sent to the game on launch, as well as
     * an optional game_metadata.player_id or game_metadata.context_id property,
     * which allows your bot to start the game in a specific context against a
     * single player or an existing group.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/game-play
     * 
     * @param title
     * @param data
     * @return
     */
    public static JSONObject gameplay(String title, String data) {
        return gameplay(title, data, null, null);
    }

    /**
     * The log in button is used in the account linking flow, which lets you link
     * the message recipient's identity on Messenger with their account on your site
     * by directing them to your web-based login flow for authentication.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/login
     * 
     * @param callbackURL
     * @return
     */
    public static JSONObject accountLink(String callbackURL) {
        JSONObject button = new JSONObject();
        button.put("type", "account_link");
        button.put("url", callbackURL);
        return button;
    }

    /**
     * The log out button is used in the account linking flow to unlink the message
     * recipient's identity on Messenger with their account on your site.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/logout
     * 
     * @return
     */
    public static JSONObject accountUnlink() {
        JSONObject button = new JSONObject();
        button.put("type", "account_unlink");
        return button;
    }

    /**
     * The postback button sends a messaging_postbacks event to your webhook with
     * the string set in the payload property. This allows you to take an arbitrary
     * actions when the button is tapped. For example, you might display a list of
     * products, then send the product ID in the postback to your webhook, where it
     * can be used to query your database and return the product details as a
     * structured message.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/postback
     * 
     * @param title
     * @param value
     * @return
     */
    public static JSONObject postBack(String title, String value) {
        JSONObject button = new JSONObject();
        button.put("type", "web_url");
        button.put("title", title);
        button.put("payload", value);
        return button;
    }

    /**
     * The URL Button opens a web page in the Messenger webview. This allows you to
     * enrich the conversation with a web-based experience, where you have the full
     * development flexibility of the web. For example, you might display a product
     * summary in-conversation, then use the URL button to open the full product
     * page on your website.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/url
     * 
     * @param title
     * @param url
     * @return
     */
    public static JSONObject url(String title, String webURL, String fallbackUrl, WebViewType type,
            boolean messengerExt, boolean hideShareButton) {
        JSONObject button = new JSONObject();
        button.put("type", "web_url");
        button.put("title", title);
        button.put("url", webURL);

        if (fallbackUrl != null && fallbackUrl.length() > 0) {
            button.put("fallback_url", fallbackUrl);
        }

        if (type != null) {
            button.put("webview_height_ratio", type.toString());
        }

        if (messengerExt) {
            button.put("messenger_extensions", messengerExt);
        }

        if (hideShareButton) {
            button.put("webview_share_button", "hide");
        }

        return button;
    }

    /**
     * The URL Button opens a web page in the Messenger webview. This allows you to
     * enrich the conversation with a web-based experience, where you have the full
     * development flexibility of the web. For example, you might display a product
     * summary in-conversation, then use the URL button to open the full product
     * page on your website.
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/url
     * 
     * @param title
     * @param url
     * @return
     */
    public static JSONObject url(String title, String webURL) {
        return url(title, webURL, null, null, false, false);
    }

    /**
     * The share button allows the message recipient to share the content of a
     * message you sent with others on Messenger. The name and icon of your Page
     * appear as an attribution at the top of the shared content. The attribution
     * opens a conversation with your bot when tapped. Ref =>
     * https://developers.facebook.com/docs/messenger-platform/reference/buttons/share
     * 
     * @param templatePayload
     * @return
     */
    public static JSONObject share(JSONObject templatePayload) {
        JSONObject button = new JSONObject();
        button.put("type", "element_share");

        JSONObject attachment = new JSONObject();
        attachment.put("type", "template");
        attachment.put("payload", templatePayload);

        JSONObject shareContent = new JSONObject();
        shareContent.put("attachment", attachment);

        button.put("share_contents", shareContent);
        return button;
    }
}