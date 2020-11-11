package com.sdm.payment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.prefs.BackingStoreException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MPUPaymentRequest implements Serializable {
    private static final long serialVersionUID = 585237841948213076L;

    @Size(min = 1, max = 5)
    @JsonProperty("Version")
    private String Version;

    @Size(min = 1, max = 15)
    @JsonProperty("merchantID")
    private String merchantID;

    @Size(min = 1, max = 20)
    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @Size(min=1, max=50)
    @JsonProperty("productDesc")
    private String productDesc;

    /**
     * The amount needs to be
     * padded with ‘0’ from the
     * left and include no decimal
     * point.
     * Example: 1.00 =
     * 000000000100,
     * 1.5 = 000000000150
     * Currency exponent follows
     * standard ISO4217 currency
     * codes
     */
    @JsonProperty("amount")
    private Long amount;

    /**
     * Standard ISO4217
     * Currency Codes
     */
    @Size(min=1, max=3)
    @JsonProperty("currencyCode")
    private Long currencyCode;

    /**
     * Merchant can distinct the
     * transaction by adding
     * category code.
     */
    @Size(min=1,max=20)
    @JsonProperty("categoryCode")
    private String categoryCode;

    /**
     * (Optional) MPU system will
     * response back to merchant
     * whatever information
     * include in request message
     * of this field
     */
    @Size(min=1,max=150)
    @JsonProperty("userDefined1")
    private String userDefined1;

    @Size(min=1,max=150)
    @JsonProperty("userDefined2")
    private String userDefined2;

    @Size(min=1,max=150)
    @JsonProperty("userDefined3")
    private String userDefined3;

    @Size(min=1,max=150)
    @JsonProperty("cardInfo")
    private String cardInfo;

    @Size(min=1,max=255)
    @JsonProperty("FrontendURL")
    private String FrontendURL;

    @Size(min=1,max=255)
    @JsonProperty("BackendURL")
    private String BackendURL;

    @Size(min=1,max=150)
    @JsonProperty("hashValue")
    private String hashValue;

    public String getSignatureString(){
        return Version+merchantID+invoiceNo+productDesc+amount+currencyCode+categoryCode+userDefined1+userDefined2+userDefined3+cardInfo+FrontendURL+ BackendURL;
    }
}
