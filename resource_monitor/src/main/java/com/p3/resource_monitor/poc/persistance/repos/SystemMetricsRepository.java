package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.SystemMetrics;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, String> {
    List<SystemMetrics> findByInstanceId(String instanceId);
    List<SystemMetrics> findByTimestampBetween(Instant from, Instant to);

    @Modifying
    @Transactional
    @Query("DELETE FROM SystemMetrics m WHERE m.timestamp < :cutoffTime")
    void deleteMetricsOlderThan(@Param("cutoffTime") Instant cutoffTime);
}