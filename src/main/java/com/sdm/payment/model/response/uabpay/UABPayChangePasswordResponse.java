package com.sdm.payment.model.response.uabpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABPayChangePasswordResponse implements Serializable {
    private static final long serialVersionUID = -2851600776746090994L;
    /*
        Response Code
        eg. 000, 001,002
         */
    @JsonProperty("RespCode")
    private String RespCode;

    /*
    Response Description eg. Success , Fail
     */
    @JsonProperty("RespCode")
    private String RespDescription;
}
