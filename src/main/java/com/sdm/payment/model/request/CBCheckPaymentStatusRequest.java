package com.sdm.payment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CBCheckPaymentStatusRequest implements Serializable {
    private static final long serialVersionUID = 1827445601358066162L;

    @Size(min = 1,max = 45)
    @JsonProperty("orderId")
    private String orderId;

    @Size(min = 1,max = 45)
    @JsonProperty("ecommerceId")
    private String ecommerceId;

    @Size(min = 1,max = 128)
    @JsonProperty("generateRefOrder")
    private String generateRefOrder;
}
