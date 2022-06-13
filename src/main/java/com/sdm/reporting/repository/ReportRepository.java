package com.sdm.reporting.repository;

import com.sdm.core.db.repository.DefaultRepository;
import com.sdm.reporting.model.Report;

import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends DefaultRepository<Report, String> {
}
