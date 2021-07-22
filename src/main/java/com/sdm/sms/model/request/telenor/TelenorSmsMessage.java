package com.sdm.sms.model.request.telenor;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelenorSmsMessage implements Serializable {
    private static final long serialVersionUID = -3350987264290484293L;

    @Enumerated(EnumType.STRING)
    @JsonProperty("type")
    private MessageType type;

    @JsonProperty("sendTime")
    private String sendTime;

    @JsonProperty("content")
    private String content;

    /**
     * name, value collection
     */
    @JsonProperty("characteristic")
    private List<Map<String, String>> characteristic;

    /**
     * @type,name
     */
    @JsonProperty("sender")
    private Map<String, String> sender;

    /**
     * @type,phoneNumber
     */
    @JsonProperty("receiver")
    private List<Map<String, String>> receiver;
}
