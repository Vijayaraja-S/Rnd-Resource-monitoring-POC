package com.p3.resource_monitor.poc.controller;

import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.Job;

import com.p3.resource_monitor.poc.service.impl.InstanceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

  private final InstanceServiceImpl instanceServiceImpl;

  @GetMapping
  public ResponseEntity<List<Instance>> getAllInstances() {
    return ResponseEntity.ok(instanceServiceImpl.getAllInstances());
  }

  @GetMapping("/{id}")
  public ResponseEntity<Instance> getInstanceById(@PathVariable String id) {
    return ResponseEntity.ok(instanceServiceImpl.getInstanceById(id));
  }

  @GetMapping("/jobs/{instanceId}")
  public ResponseEntity<List<Job>> getJobsByInstanceId(@PathVariable String instanceId) {
    return ResponseEntity.ok(instanceServiceImpl.getJobsByInstanceId(instanceId));
  }
}
