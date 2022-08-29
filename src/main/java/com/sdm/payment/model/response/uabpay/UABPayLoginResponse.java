package com.sdm.payment.model.response.uabpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABPayLoginResponse implements Serializable {
    private static final long serialVersionUID = 5696801204272102054L;
    /*
        Response Description
        eg. Success , Fail
         */
    @JsonProperty("RespDescription")
    private String RespDescription;

    /*
    Response Code
    eg. 000, 001,002
     */
    @JsonProperty("RespCode")
    private String RespCode;

    @JsonProperty("Token")
    private String Token;

    @JsonProperty("ExpexpiredSeconds")
    private int ExpexpiredSeconds;
}
