package com.p3.resource_monitor.poc.controller;

import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.persistance.models.Job;
import com.p3.resource_monitor.poc.service.JobService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("api/job")
@RequiredArgsConstructor
public class JobController {
  private final JobService jobService;

  @PostMapping("/init/{instanceId}")
  public String initJob(@RequestBody JobInputBean jobInputBean, @PathVariable String instanceId) {
    return jobService.initJob(jobInputBean,instanceId);
  }

  @GetMapping("/instance/{instanceId}")
  public List<Job> getJobsByInstance(@PathVariable String instanceId) {
    return jobService.getJobsByInstanceId(instanceId);
  }
}