package com.sdm.payment.model.response.uabpay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABPayCheckTransactionResponse implements Serializable {
    private static final long serialVersionUID = -531872567386124703L;
    /*
            Response Code
            eg. 000, 001,002
             */
    @JsonProperty("RespCode")
    private String RespCode;

    /*
    Response Code
    eg. 000 - Success , 888 – Reversal
    Success, 778 – Transaction Count
    Exceeded , 777 – Invalid Reference
    No , 779 – Transaction Fail, etc
     */
    @JsonProperty("RespDescription")
    private String RespDescription;

    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash
    value of:
    RespDescription + RespCode
     */
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return RespDescription + RespCode;
    }
}
