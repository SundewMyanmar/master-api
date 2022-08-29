package com.sdm.payment.model.request.mpu;

import com.sdm.core.model.DefaultEntity;

import org.hibernate.annotations.Where;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Entity(name = "MPUPaymentEntity")
@Table(name = "tbl_payment_mpu_requests")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Log4j2
public class MPUPayment extends DefaultEntity {
    private static final long serialVersionUID = 585237841948213076L;

    @Id
    @Column(columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;
    @Size(min = 1, max = 15)
    @Column
    @HttpParameter("MerchantID")
    private String merchantID;
    @Size(min = 1, max = 20)
    @Column
    @HttpParameter("invoiceNo")
    private String invoiceNo;
    @Size(min = 1, max = 50)
    @HttpParameter("productDesc")
    @Column
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
    @HttpParameter("amount")
    @Column
    private String amount;
    @HttpParameter("CurrencyCode")
    @Column
    private Long currency;
    /**
     * Merchant can distinct the
     * transaction by adding
     * category code.
     */
    @Size(min = 1, max = 20)
    @HttpParameter("categoryCode")
    @Column
    private String category;
    /**
     * (Optional) MPU system will
     * response back to merchant
     * whatever information
     * include in request message
     * of this field
     */
    @Size(min = 1, max = 150)
    @HttpParameter("userDefined1")
    @Column
    private String userDefined1;
    @Size(min = 1, max = 150)
    @HttpParameter("userDefined2")
    @Column
    private String userDefined2;
    @Size(min = 1, max = 150)
    @HttpParameter("userDefined3")
    @Column
    private String userDefined3;
    @Transient
    @Size(min = 1, max = 255)
    @HttpParameter("cardInfo")
    @Column
    private String cardInfo;
    @Transient
    @Size(min = 1, max = 255)
    @HttpParameter("FrontendURL")
    @Column
    private String frontendURL;
    @Transient
    @Size(min = 1, max = 255)
    @HttpParameter("BackendURL")
    @Column
    private String backendURL;
    @Transient
    @Size(min = 1, max = 150)
    @HttpParameter("hashValue")
    private String hashValue;

    public MPUPayment(@Size(min = 1, max = 20) String invoiceNo, @Size(min = 1, max = 50) String productDesc, Long amount) {
        this.id = UUID.randomUUID().toString();
        this.invoiceNo = invoiceNo;
        this.productDesc = productDesc;
        setAmount(amount);
        this.currency = CurrencyCode.MMK.getValue();
    }

    public void setAmount(Long amount) {
        this.amount = String.format("%010d00", amount);
    }

}
