package com.sdm.master.entity;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Filterable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "MessengerLogEntity")
@Table(name = "tbl_messenger_logs")
public class MessengerLogEntity extends DefaultEntity implements Serializable {
    private static final long serialVersionUID = -4217111486935913447L;

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

    public MessengerLogEntity() {

    }

    public MessengerLogEntity(String senderId, String state, String messageType, String message, String payload, Date messageTime) {
        this.id = id;
        this.senderId = senderId;
        this.state = state;
        this.messageType = messageType;
        this.message = message;
        this.payload = payload;
        this.messageTime = messageTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessengerLogEntity other = (MessengerLogEntity) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
