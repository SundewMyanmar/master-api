package com.sdm.notification.model;

import com.sdm.core.model.DefaultEntity;
import com.sdm.core.model.annotation.Searchable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity(name = "Notification")
@Table(name = "tbl_notifications")
@Where(clause = "deleted_at IS NULL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends DefaultEntity implements Serializable {
    @Id
    @Column(columnDefinition = "char(36)", length = 36, unique = true, nullable = false)
    private String id;

    public Notification(String title, String description, String category, Integer userId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.userId = userId;
    }

    @Searchable
    @Column(columnDefinition = "TEXT", nullable = true)
    private String fcmIds;

    @Searchable
    @NotBlank
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String title;

    @Searchable
    @Size(max = 500)
    @Column(length = 500, nullable = false)
    private String description;

    @Searchable
    @Size(max = 255)
    @Column
    private String category;

    @Searchable
    @Size(max = 1000)
    @Column(length = 1000)
    private String imageUrl;

    @Searchable
    @Size(max = 100)
    @Column(length = 100)
    private String topic;

    @Column
    private Integer userId;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date readAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "name")
    @CollectionTable(name = "tbl_notification_data",
            joinColumns = @JoinColumn(name = "notificationId"))
    private Map<String, String> data = new HashMap<>();

    public void putData(String key, String value) {
        data.put(key, value);
    }

    /**
     * Image must be public
     *
     * @return
     */
    public com.google.firebase.messaging.Notification buildFCM() {
        return com.google.firebase.messaging.Notification.builder()
                .setTitle(this.title)
                .setBody(this.description)
                .setImage(getImageUrl())
                .build();
    }
}
