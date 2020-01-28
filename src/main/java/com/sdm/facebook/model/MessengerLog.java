package com.sdm.facebook.model;

import com.sdm.core.model.annotation.Filterable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "facebook.MessengerLogEntity")
@Table(name = "tbl_facebook_messenger_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessengerLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "sender_id", columnDefinition = "VARCHAR(100)", length = 100, nullable = false)
    private String senderId;

    @Column(name = "state", columnDefinition = "VARCHAR(50)", length = 50)
    private String state;

    @Filterable
    @Column(name = "message_type", columnDefinition = "VARCHAR(50)", length = 50)
    private String messageType;

    @Filterable
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "message_time", length = 19)
    private Date messageTime;
}
