package com.p3.resource_monitor.poc.service.impl;

import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.Job;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import com.p3.resource_monitor.poc.service.InstanceService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InstanceServiceImpl implements InstanceService {

  private final InstanceRepository instanceRepository;

  public List<Instance> getAllInstances() {
    return instanceRepository.findAll();
  }

  public Instance getInstanceById(String id) {
    return instanceRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Instance not found with id: " + id));
  }

  public List<Job> getJobsByInstanceId(String instanceId) {
    Optional<Instance> instance = instanceRepository.findById(instanceId);
    if (instance.isEmpty()) {
      throw new EntityNotFoundException("Instance not found with instanceId: " + instanceId);
    }
    return instance.get().getJobs();
  }
}
