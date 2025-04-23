package com.p3.resource_monitor.poc.service.impl;

import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;
import com.p3.resource_monitor.poc.persistance.repos.InstanceMetricsRepository;
import com.p3.resource_monitor.poc.service.InstanceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstanceMetricsServiceImpl implements InstanceMetricsService {

  private final InstanceMetricsRepository instanceMetricsRepository;

  @Override
  public List<InstanceMetrics> getLastOneHourMetrics(String instanceId) {
    Instant oneHourAgo = Instant.now().minusSeconds(3600);
    return instanceMetricsRepository.findMetricsForLastHour(instanceId, oneHourAgo);
  }
}
