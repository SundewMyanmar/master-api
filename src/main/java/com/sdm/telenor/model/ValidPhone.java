package com.sdm.telenor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "system.ValidPhone")
@Table(name = "tbl_system_valid_phones")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidPhone implements Serializable {
    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String phone;
    @JsonIgnore
    @Column(nullable = false)
    private String secret;
    @Column(nullable = false)
    private boolean verified;
    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date verifiedAt;

    public ValidPhone(String phone) {
        this.phone = phone;
        this.verified = false;
    }
}
