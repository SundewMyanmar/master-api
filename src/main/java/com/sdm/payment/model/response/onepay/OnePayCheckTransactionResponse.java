package com.sdm.payment.model.response.onepay;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnePayCheckTransactionResponse implements Serializable {
    private static final long serialVersionUID = 2397143231442035508L;

    @JsonProperty("RespDescription")
    private String RespDescription;

    @JsonProperty("RespCode")
    private String RespCode;

    @JsonProperty("HashValue")
    private String HashValue;

    public String getSignatureString() {
        return RespDescription + RespCode;
    }
}
