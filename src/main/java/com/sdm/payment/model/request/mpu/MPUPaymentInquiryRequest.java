package com.sdm.payment.model.request.mpu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentInquiryRequest implements Serializable {
    private static final long serialVersionUID = 648822106775232011L;

    @Size(min = 1, max = 15)
    @JsonProperty("merchantID")
    private String merchantID;

    @Size(min = 1, max = 20)
    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("actionType")
    private String actionType;

    @JsonProperty("hashValue")
    private String hashValue;

    @JsonIgnore
    private String getSignatureString() {
        return merchantID + invoiceNo + actionType;
    }
}
