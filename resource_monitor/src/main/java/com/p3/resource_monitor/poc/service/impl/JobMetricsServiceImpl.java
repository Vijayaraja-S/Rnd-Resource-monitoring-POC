package com.p3.resource_monitor.poc.service.impl;

import com.p3.resource_monitor.poc.persistance.models.JobMetrics;
import com.p3.resource_monitor.poc.persistance.repos.JobMetricsRepository;
import com.p3.resource_monitor.poc.service.JobMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobMetricsServiceImpl implements JobMetricsService {

  private final JobMetricsRepository metricsRepository;

  @Override
  public List<JobMetrics> getMetricsByJobId(String jobId) {
    return metricsRepository.findByJob_Id(jobId);
  }
}
