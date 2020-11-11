package com.sdm.payment.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABResponsePaymentResponse implements Serializable {
    private static final long serialVersionUID = -8851914916375938381L;

    public enum DataType{
        Data,
        Link
    }
    /*
    Sequence Number for Transaction
     */
    @JsonProperty("ReferIntegrationID")
    private String ReferIntegrationID;
    /*
    Data or Link, It depend on merchant
    response data. If response data is Json data,
    pass Data. If response data is Url Link, pass
    Link.
     */
    @Enumerated(EnumType.STRING)
    @JsonProperty("DataType")
    private DataType DataType;
    /*
    To redirect for order confirmation. Eg :
    http://{merchantname}.com/Confrimation
    Page
     */
    @JsonProperty("ConfrimationUrl")
    private String ConfrimationUrl;
    /*
    Item list for order detail Eg:
    "{"ItemId":"002","Quantity":"1","EachPrice":"
    3000"},{"ItemId":"001","Quantity":"2","EachP
    rice":"5000"}"
     */
    @JsonProperty("ItemListJsonStr")
    private String ItemListJsonStr;
    /*
    Response Description eg.
    Success , Fail
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
    Value must be Upper Case. HMACSHA1
    cryptographic hash value of: RespCode +
    RespDescription + ItemListJsonStr +
    ReferIntegrationID + DataType +
    ConfrimationUrl
     */
    @JsonProperty("HashValue")
    private String HashValue;

    @JsonIgnore
    public String getSignatureString(){
        return RespCode+RespDescription+ItemListJsonStr+ReferIntegrationID+DataType+ConfrimationUrl;
    }
}
