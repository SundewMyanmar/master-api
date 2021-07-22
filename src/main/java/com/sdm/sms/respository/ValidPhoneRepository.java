package com.sdm.sms.respository;

import com.sdm.sms.model.ValidPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidPhoneRepository extends JpaRepository<ValidPhone, String> {
}
