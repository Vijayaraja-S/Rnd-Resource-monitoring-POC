package com.p3.resource_monitor.poc.service;

import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;

import java.util.List;

public interface InstanceMetricsService {

  List<InstanceMetrics> getLastOneHourMetrics(String instanceId);
}
