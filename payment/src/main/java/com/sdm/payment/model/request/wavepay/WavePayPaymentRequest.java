package com.sdm.payment.model.request.wavepay;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WavePayPaymentRequest implements Serializable {
    private static final long serialVersionUID = 2995205108119596244L;

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("merchant_reference_id")
    private String merchantReferenceId;

    @JsonProperty("frontend_result_url")
    private String frontendResultUrl;

    @JsonProperty("backend_result_url")
    private String backendResultUrl;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("time_to_live_in_seconds")
    private String timeToLiveInSeconds;

    @JsonProperty("payment_description")
    private String paymentDescription;

    @JsonProperty("merchant_name")
    private String merchantName;

    /**
     * $items=json_encode([
     * ['name'=>'Product 1','amount'=>1000]
     * ['name'=>'Product 2','amount'=>2000]
     * ])
     */
    @JsonProperty("items")
    private String items;

    @JsonProperty("hash")
    private String hash;

    @JsonIgnore
    public String getSignatureString() {
        return timeToLiveInSeconds + merchantId + orderId + amount + backendResultUrl + merchantReferenceId;
    }
}
