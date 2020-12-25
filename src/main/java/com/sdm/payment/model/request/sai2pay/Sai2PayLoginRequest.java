package com.sdm.payment.model.request.sai2pay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sai2PayLoginRequest implements Serializable {
    private static final long serialVersionUID = 7564661178584803850L;
    /*
        Merchant user id from Uabpay system
         */
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("UserName")
    private String UserName;

    /*
    Merchant user current password from Uabpay system
     */
    @NotBlank
    @Size(min = 1, max = 16)
    @JsonProperty("Password")
    private String Password;
}
