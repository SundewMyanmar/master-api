package com.sdm.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "system.RequestInfo")
@Table(name = "tbl_system_clients")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo implements Serializable {

    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)", nullable = false)
    private String id;
    @Column(nullable = false)
    private String remoteAddress;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRequestAt;
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date blockedExpiry;

    public ClientInfo(String id, String remoteAddress) {
        this.id = id;
        this.remoteAddress = remoteAddress;
    }
}
