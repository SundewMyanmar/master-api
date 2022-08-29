package com.sdm.payment.model.request.uabpay;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UABPayChangePasswordRequest implements Serializable {
    private static final long serialVersionUID = 6480859385129164689L;
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
    @JsonProperty("CurrentPassword")
    private String CurrentPassword;

    /*
    Merchant user new password that merchant user want to change
     */
    @NotBlank
    @Size(min = 1, max = 16)
    @JsonProperty("NewPassword")
    private String NewPassword;
}
