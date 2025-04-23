package com.p3.resource_monitor.poc.service;

import com.p3.resource_monitor.poc.persistance.models.JobMetrics;

import java.util.List;

public interface JobMetricsService {
    List<JobMetrics> getMetricsByJobId(String jobId);

}
