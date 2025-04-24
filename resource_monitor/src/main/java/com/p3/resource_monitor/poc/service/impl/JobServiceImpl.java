package com.p3.resource_monitor.poc.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.p3.resource_monitor.poc.Extraction.ProcessExtraction;
import com.p3.resource_monitor.poc.beans.JobInputBean;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.p3.resource_monitor.poc.metrics_operations.MetricUtils.getRealIpAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
  private final JobRepository jobRepository;
  private final InstanceRepository instanceRepository;

  @Override
  public String initJob(JobInputBean jobInputBean, String instanceId) {
    Job job =
        Job.builder()
            .jobType("EXTRACTION")
            .startTime(Instant.now())
            .status("READY")
            .jobInput(new ObjectMapper().convertValue(jobInputBean, String.class).getBytes())
            .instance(instanceRepository.findById(instanceId).orElseThrow())
            .build();
    jobRepository.save(job);
    return job.getId();
  }

  @Override
  public List<Job> getJobsByInstanceId(String instanceId) {
    return jobRepository.findByInstance_Id(instanceId);
  }

  @Scheduled(fixedRate = 3000)
  public void processReadyJobs() throws SocketException, UnknownHostException {
    String currentIp = getRealIpAddress();
    int currentPort = Integer.parseInt(System.getProperty("server.port"));

    log.info("Processing ready jobs for IP: {}, Port: {}", currentIp, currentPort);

    List<Job> readyJobs =
        jobRepository.findByStatus("READY").stream()
            .filter(
                job ->
                    job.getInstance().getIpAddress().equals(currentIp)
                        && job.getInstance().getPort() == currentPort)
            .toList();

    if (readyJobs.isEmpty()) {
      return;
    }
    ExecutorService executorService = Executors.newFixedThreadPool(readyJobs.size());
    List<Future<?>> futures = new ArrayList<>();
    for (Job job : readyJobs) {
      Future<?> future = executorService.submit(() -> handleJob(job));
      futures.add(future);
    }
  }

  private void handleJob(Job job) {
    try {
      String jobInput = new String(job.getJobInput(), StandardCharsets.UTF_8);
      JobInputBean jobInputBean = new Gson().fromJson(jobInput, JobInputBean.class);
      new ProcessExtraction().extraction(jobInputBean);
      job.setStatus("COMPLETED");
    } catch (Exception e) {
      job.setStatus("FAILED");
      e.printStackTrace();
    } finally {
      job.setEndTime(Instant.now());
      jobRepository.save(job);
    }
  }
}
