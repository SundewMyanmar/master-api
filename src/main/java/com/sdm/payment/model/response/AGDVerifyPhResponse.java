package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AGDVerifyPhResponse implements Serializable {
    private static final long serialVersionUID = 7834493800619589652L;

    @JsonProperty("RespDescription")
    private String RespDescription;

    @JsonProperty("RespCode")
    private String RespCode;

    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString(){return RespDescription+RespCode;}
}
