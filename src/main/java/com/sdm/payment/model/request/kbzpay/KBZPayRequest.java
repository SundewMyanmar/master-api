package com.sdm.payment.model.request.kbzpay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KBZPayRequest implements Serializable {
    private static final long serialVersionUID = -2426992327806665614L;

    @JsonProperty("timestamp")
    private String timestamp;

    /*
    Merchant Callback Url
     */
    @JsonProperty("notify_url")
    private String notifyUrl;

    /*
    method	String	Yes	kbz.payment.precreate		Interface name: kbz.payment.precreate
     */
    @JsonProperty("method")
    private String method;

    /*
    Random character string containing a maximum of 32 characters, including uppercase letters, lowercase letters, digits, but not special characters
     */
    @JsonProperty("nonce_str")
    private String nonceStr;

    /*
    Signature type. Currently, only the value SHA256 is supported.
     */
    @JsonProperty("sign_type")
    private String signType;

    /*
    Service parameter set signature, which is case insensitive. (For details about the signature algorithm, see Signature Description.)
     */
    @JsonProperty("sign")
    private String sign;

    /*
        1.0	Interface version number.
     */
    @JsonProperty("version")
    private String version;

    /*
    Service parameter set. For detail, see below table.
     */
    @JsonProperty("biz_content")
    private KBZPayBizContent bizContent;
}
