package com.p3.resource_monitor.poc.controller;

import com.p3.resource_monitor.poc.persistance.models.JobMetrics;
import com.p3.resource_monitor.poc.service.JobMetricsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-metrics")
@RequiredArgsConstructor
public class JobMetricsController {
  private final JobMetricsService metricsService;

  @GetMapping("/job/{jobId}")
  public List<JobMetrics> getMetricsByJobId(@PathVariable String jobId) {
    return metricsService.getMetricsByJobId(jobId);
  }
}
