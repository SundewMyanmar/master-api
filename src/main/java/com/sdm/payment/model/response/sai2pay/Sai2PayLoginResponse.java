package com.sdm.payment.model.response.sai2pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sai2PayLoginResponse implements Serializable {
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
