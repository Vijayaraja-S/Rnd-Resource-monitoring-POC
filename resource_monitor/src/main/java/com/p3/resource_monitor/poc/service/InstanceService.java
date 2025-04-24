package com.p3.resource_monitor.poc.service;

import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.Job;

import java.util.List;

public interface InstanceService {
  List<Instance> getAllInstances();

  Instance getInstanceById(String id);

  List<Job> getJobsByInstanceId(String instanceId);
}
