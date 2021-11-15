package com.sdm.telenor.respository;

import com.sdm.telenor.model.ValidPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidPhoneRepository extends JpaRepository<ValidPhone, String> {
}
