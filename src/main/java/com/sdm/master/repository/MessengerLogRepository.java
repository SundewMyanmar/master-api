package com.sdm.master.repository;

import com.sdm.core.repository.DefaultRepository;
import com.sdm.master.entity.MessengerLogEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MessengerLogRepository extends DefaultRepository<MessengerLogEntity,Integer> {
    @Query(nativeQuery = true,value = "SELECT m.* FROM tbl_messenger_logs m WHERE m.sender_id=:senderId AND DATE(m.message_time)=DATE(:date) order by m.id DESC LIMIT 1")
    Optional<MessengerLogEntity> getLastMsgLogBySenderIdAndDate(@Param("senderId") String senderId,@Param("date") Date date);
}
