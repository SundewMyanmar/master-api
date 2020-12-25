package com.sdm.payment.model.response.onepay;

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
public class OnePayResponseDirectPaymentResponse implements Serializable {
    private static final long serialVersionUID = 51001676634845132L;

    public enum DataType {
        Data,
        Link
    }

    @JsonProperty("ReferIntegrationID")
    private String ReferIntegrationID;

    @Enumerated(EnumType.STRING)
    @JsonProperty("DataType")
    private OnePayResponseDirectPaymentResponse.DataType DataType;

    @JsonProperty("ConfrimationUrl")
    private String ConfrimationUrl;

    @JsonProperty("ItemListJsonStr")
    private String ItemListJsonStr;

    @JsonProperty("RespDescription")
    private String RespDescription;

    @JsonProperty("RespCode")
    private String RespCode;

    @JsonProperty("HashValue")
    private String HashValue;

    public String getSignatureString() {
        return RespCode + RespDescription + ItemListJsonStr + ReferIntegrationID + DataType + ConfrimationUrl;
    }
}
