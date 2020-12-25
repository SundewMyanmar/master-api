package com.sdm.payment.model.request.cbpay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBPaymentOrderRequest implements Serializable {
    private static final long serialVersionUID = -1781618366910410437L;

    public enum TransactionType {
        WEB("0"), /*Transaction from Web*/
        MOBILE("1"); /*Transaction from Mobile Application*/

        private String value;

        TransactionType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Size(min = 1, max = 1024)
    @JsonProperty("authenToken")
    private String authenToken;

    @Size(min = 1, max = 45)
    @JsonProperty("ecommerceId")
    private String ecommerceId;

    @Size(min = 1, max = 45)
    @JsonProperty("subMerId")
    private String subMerId;

    @Enumerated(EnumType.STRING)
    @JsonProperty("transactionType")
    private TransactionType transactionType;

    @Size(min = 1, max = 45)
    @JsonProperty("orderId")
    private String orderId;

    @Size(min = 1, max = 200)
    @JsonProperty("orderDetails")
    private String orderDetails;

    @Size(min = 1, max = 15)
    @JsonProperty("amount")
    private String amount;

    @Size(min = 1, max = 3)
    @JsonProperty("currency")
    private String currency;

    @Size(min = 1, max = 200)
    @JsonProperty("notifyUrl")
    private String notifyUrl;

    @Size(min = 1, max = 200)
    @JsonProperty("signature")
    private String signature;

    public String getTransactionType() {
        return this.transactionType.getValue();
    }

    @JsonIgnore
    public String getSignatureString() {
        return authenToken + "&" + ecommerceId + "&" + subMerId + "&" + orderId + "&" + amount + "&" + currency;
    }
}
