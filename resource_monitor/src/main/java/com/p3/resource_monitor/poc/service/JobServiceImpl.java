package com.p3.resource_monitor.poc.service;

import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import com.p3.resource_monitor.poc.persistance.models.Job;
import com.p3.resource_monitor.poc.persistance.repos.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
  private final JobRepository jobRepository;
  private final InstanceRepository instanceRepository;

  @Override
  public String initJob(String path, String instanceId) {
    Job job =
        Job.builder()
            .jobType("CSV_MERGE")
            .startTime(Instant.now())
            .status("READY")
            .jobInput(path)
            .instance(instanceRepository.findById(instanceId).orElseThrow())
            .build();
    jobRepository.save(job);
    return job.getId();
  }

  @Scheduled(fixedRate = 2000)
  public void processReadyJobs() {
    List<Job> readyJobs = jobRepository.findByStatus("READY");

    for (Job job : readyJobs) {
      job.setStatus("IN_PROGRESS");
      job.setStartTime(Instant.now());
      jobRepository.save(job);
      ExecutorService executorService = Executors.newFixedThreadPool(4);


      executorService.submit(() -> handleJob(job));
    }
  }
  private void handleJob(Job job) {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Path folderPath = Paths.get(job.getJobInput());
    File[] files = folderPath.toFile().listFiles((dir, name) -> name.endsWith(".csv"));

    Path outputFile = folderPath.resolve("output_" + job.getId() + ".txt");

    long bytesWritten = 0;
    int fileCount = 0;

    try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
      List<Future<Long>> futures = new ArrayList<>();

        assert files != null;
        for (File csv : files) {
        fileCount++;
        futures.add(executorService.submit(() -> {
          long localBytes = 0;
          try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            String line;
            while ((line = reader.readLine()) != null) {
              writer.write(line);
              writer.newLine();
              localBytes += line.getBytes().length;
            }
          }
          return localBytes;
        }));
      }

      for (Future<Long> future : futures) {
        bytesWritten += future.get();
      }

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