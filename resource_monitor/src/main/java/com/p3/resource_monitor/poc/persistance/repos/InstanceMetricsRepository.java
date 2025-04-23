package com.p3.resource_monitor.poc.persistance.repos;

import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface InstanceMetricsRepository extends JpaRepository<InstanceMetrics, String> {

  @Modifying
  @Transactional
  @Query("DELETE FROM InstanceMetrics m WHERE m.timestamp < :cutoffTime")
  void deleteMetricsOlderThan(@Param("cutoffTime") Instant cutoffTime);

  @Query(
      "SELECT m FROM InstanceMetrics m WHERE m.instance.id = :instanceId AND m.timestamp >= :from ORDER BY m.timestamp ASC")
  List<InstanceMetrics> findMetricsForLastHour(
      @Param("instanceId") String instanceId, @Param("from") Instant from);
}
