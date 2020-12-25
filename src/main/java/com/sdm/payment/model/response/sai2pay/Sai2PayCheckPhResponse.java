package com.sdm.payment.model.response.sai2pay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sai2PayCheckPhResponse implements Serializable {
    private static final long serialVersionUID = 5508031558925997409L;
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

    /*
    User name of phone number
    Eg. Kyaw Kyaw
     */
    @JsonProperty("UserName")
    private String UserName;

    /*
    1 is Low KYC User of Wallet System.
    2 is Full KYC User of Wallet System.
    Eg. 1 or 2
     */
    @JsonProperty("Level")
    private String Level;

    /*
    NRC and Passport Of Full KYC User. If user’s
    level is 1. NRC or Passport will be null or
    empty.
     */
    @JsonProperty("NRCPassport")
    private String NRCPassport;

    /*
    Value must be Upper Case. For 1.0,
    HMACSHA1 cryptographic hash value
    of:
    RespDescription + RespCode +UserName
     */
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString() {
        return RespDescription + RespCode + UserName;
    }
}
