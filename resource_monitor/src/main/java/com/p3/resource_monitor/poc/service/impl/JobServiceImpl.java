package com.p3.resource_monitor.poc.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.p3.resource_monitor.poc.Extraction.ProcessExtraction;
import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.metrics_operations.JobMetricsCollector;
import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.Job;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import com.p3.resource_monitor.poc.persistance.repos.JobRepository;
import com.p3.resource_monitor.poc.service.JobService;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.p3.resource_monitor.poc.metrics_operations.MetricUtils.fetchPid;
import static com.p3.resource_monitor.poc.metrics_operations.MetricUtils.getRealIpAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
  private final JobRepository jobRepository;
  private final InstanceRepository instanceRepository;
  private final JobMetricsCollector jobMetricsCollector;

  @Autowired private Environment environment;

  @Override
  public String initJob(JobInputBean jobInputBean, String instanceId)
      throws JsonProcessingException {
    Job job =
        Job.builder()
            .jobType("EXTRACTION")
            .startTime(Instant.now())
            .status("READY")
            .jobInput(
                new ObjectMapper().writeValueAsString(jobInputBean).getBytes())
            .instance(instanceRepository.findById(instanceId).orElseThrow())
            .build();
    jobRepository.save(job);
    return job.getId();
  }

  @Override
  public List<Job> getJobsByInstanceId(String instanceId) {
    return jobRepository.findByInstance_Id(instanceId);
  }

  @Scheduled(cron = "*/3 * * * * *")
  public void processReadyJobs() throws SocketException, UnknownHostException {
    String currentIp = getRealIpAddress();
    String property = environment.getProperty("server.port");
    int currentPort = Integer.parseInt(Objects.requireNonNull(property));

    log.info("Processing ready jobs for IP: {}, Port: {}", currentIp, currentPort);

    List<Job> ready = jobRepository.findByStatusWithInstance("READY");

    List<Job> readyJobs = new ArrayList<>();
    for (Job job : ready) {
      Instance instance = job.getInstance();
      if (instance.getIpAddress().equals(currentIp) && instance.getPort() == currentPort) {
        readyJobs.add(job);
      }
    }

    if (readyJobs.isEmpty()) {
      log.info("No ready jobs found for IP: {}, Port: {}", currentIp, currentPort);
      return;
    }
    ExecutorService executorService = Executors.newFixedThreadPool(readyJobs.size());
    log.info("job running in  IP: {}, Port: {}", currentIp, currentPort);
    List<Future<?>> futures = new ArrayList<>();
    for (Job job : readyJobs) {
      Future<?> future = executorService.submit(() -> handleJob(job));
      futures.add(future);
    }
  }

  private void handleJob(Job job) {
    try {
      Integer pid = fetchPid(job.getInstance().getPort());
      jobMetricsCollector.startCollecting(job, pid);
      String jobInput = new String(job.getJobInput(), StandardCharsets.UTF_8);
      JobInputBean jobInputBean = new Gson().fromJson(jobInput, JobInputBean.class);
      new ProcessExtraction().extraction(jobInputBean);
      job.setStatus("COMPLETED");

      log.info("Job completed: {}", job);
    } catch (Exception e) {
      job.setStatus("FAILED");
      e.printStackTrace();
    } finally {
      job.setEndTime(Instant.now());
      jobRepository.save(job);

      jobMetricsCollector.stopCollecting(job);
    }
  }
}
