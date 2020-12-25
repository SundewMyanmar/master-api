package com.sdm.notification.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends DefaultRepository<Notification, String> {
    @Query("SELECT n FROM #{#entityName} n WHERE n.user.id=:userId")
    Page<Notification> pagingByUser(Pageable pageable, @Param("userId") int userId);

    @Query("SELECT n FROM #{#entityName} n WHERE n.user.id=:userId AND n.category = :category")
    Page<Notification> pagingByUserAndCategory(Pageable pageable, @Param("userId") int userId, @Param("category") String category);

    @Query("SELECT COUNT(n) FROM #{#entityName} n WHERE n.user.id=:userId AND n.readAt IS NULL")
    Integer unreadCount(@Param("userId") int userId);

    @Modifying
    @Query(value = "UPDATE tbl_notifications n SET n.read_at = NOW() WHERE n.user_id=:userId AND n.read_at IS NULL", nativeQuery = true)
    void readAllNotifications(@Param("userId") int userId);

    @Modifying
    @Query(value = "UPDATE tbl_notifications n SET n.read_at = NOW() WHERE n.user_id = :userId AND n.category = :category AND n.read_at IS NULL", nativeQuery = true)
    void readAllNotificationsByCategory(@Param("userId") int userId, @Param("category") String category);
}
