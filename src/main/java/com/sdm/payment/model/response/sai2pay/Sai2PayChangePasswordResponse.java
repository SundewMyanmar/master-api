package com.sdm.payment.model.response.sai2pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sai2PayChangePasswordResponse implements Serializable {
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
