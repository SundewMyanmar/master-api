package com.sdm.facebook.repository;

import com.sdm.facebook.model.MessengerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MessengerLogRepository extends JpaRepository<MessengerLog, Integer> {
    @Query(nativeQuery = true, value = "SELECT m.* FROM tbl_facebook_messenger_logs m WHERE m.sender_id=:senderId AND DATE(m.messageTime)=DATE(:date) order by m.id DESC LIMIT 1")
    Optional<MessengerLog> getLastMsgLogBySenderIdAndDate(@Param("senderId") String senderId, @Param("date") Date date);
}
