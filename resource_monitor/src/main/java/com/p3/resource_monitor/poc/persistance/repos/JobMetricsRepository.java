package com.p3.resource_monitor.poc.persistance.repos;

import com.p3.resource_monitor.poc.persistance.models.JobMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JobMetricsRepository extends JpaRepository<JobMetrics, String> {
    List<JobMetrics> findByJob_Id(String jobId);
}