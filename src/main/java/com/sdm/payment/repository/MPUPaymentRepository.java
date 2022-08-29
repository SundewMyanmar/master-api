package com.sdm.payment.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.payment.model.request.mpu.MPUPayment;

import org.springframework.stereotype.Repository;

@Repository
public interface MPUPaymentRepository extends DefaultRepository<MPUPayment, String> {
}
