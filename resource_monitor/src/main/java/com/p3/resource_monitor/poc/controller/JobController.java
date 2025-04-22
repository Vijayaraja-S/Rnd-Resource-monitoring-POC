package com.p3.resource_monitor.poc.controller;

import com.p3.resource_monitor.poc.service.JobService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/job")
@RequiredArgsConstructor
public class JobController {
  private final JobService jobService;

  @PostMapping("/init/{path}/{instanceId}")
  public String initJob(@PathVariable String path, @PathVariable String instanceId) {
    return jobService.initJob(path,instanceId);
  }
}