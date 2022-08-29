package com.sdm.payment.model.request.cbpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBCheckPaymentStatusRequest implements Serializable {
    private static final long serialVersionUID = 1827445601358066162L;

    @Size(min = 1, max = 45)
    @JsonProperty("orderId")
    private String orderId;

    @Size(min = 1, max = 45)
    @JsonProperty("ecommerceId")
    private String ecommerceId;

    @Size(min = 1, max = 128)
    @JsonProperty("generateRefOrder")
    private String generateRefOrder;
}
