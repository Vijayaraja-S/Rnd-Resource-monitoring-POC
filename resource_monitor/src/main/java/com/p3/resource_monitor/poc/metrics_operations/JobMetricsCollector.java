package com.p3.resource_monitor.poc.metrics_operations;

import com.p3.resource_monitor.poc.persistance.models.Job;
import com.p3.resource_monitor.poc.persistance.models.JobMetrics;
import com.p3.resource_monitor.poc.persistance.repos.JobMetricsRepository;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobMetricsCollector {

    private final JobMetricsRepository jobMetricsRepository;

    private final Map<String, ScheduledFuture<?>> jobMetricFutures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private final SystemInfo systemInfo = new SystemInfo();
    private final OperatingSystem os = systemInfo.getOperatingSystem();

    public void startCollecting(Job job, int pid) {
        Runnable task = () -> collectAndSaveMetrics(job, pid);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
        jobMetricFutures.put(job.getId(), future);
        log.info("Started collecting metrics for job: {}", job.getId());
    }

    public void stopCollecting(Job job) {
        ScheduledFuture<?> future = jobMetricFutures.remove(job.getId());
        if (future != null) {
            future.cancel(true);
            log.info("Stopped collecting metrics for job: {}", job.getId());
        }
    }

    private void collectAndSaveMetrics(Job job, int pid) {
        try {
            OSProcess process = os.getProcess(pid);
            if (process == null) {
                log.warn("No process found with PID: {}", pid);
                return;
            }

            process.updateAttributes();
            OSProcess oldProcess = os.getProcess(pid);
            Thread.sleep(100);  // simulate tick gap
            OSProcess newProcess = os.getProcess(pid);

            double cpuLoad = 100 * newProcess.getProcessCpuLoadBetweenTicks(oldProcess);
            long usedMemory = process.getResidentSetSize();
            long totalMemory = systemInfo.getHardware().getMemory().getTotal();
            double usedMemoryPercent = (double) usedMemory / totalMemory * 100;

            long bytesRead = process.getBytesRead();
            long bytesWritten = process.getBytesWritten();

            List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
            long bytesSent = networkIFs.stream().mapToLong(NetworkIF::getBytesSent).sum();
            long bytesReceived = networkIFs.stream().mapToLong(NetworkIF::getBytesRecv).sum();

            JobMetrics metrics = JobMetrics.builder()
                    .job(job)
                    .cpu(String.format("%.2f%%", cpuLoad))
                    .memory(String.format("%.2f%%", usedMemoryPercent))
                    .disk(String.format("Read: %.2f MiB, Write: %.2f MiB", bytesRead / 1e6, bytesWritten / 1e6))
                    .network(String.format("Sent: %.2f MiB, Received: %.2f MiB", bytesSent / 1e6, bytesReceived / 1e6))
                    .timestamp(Instant.now())
                    .build();

            jobMetricsRepository.save(metrics);
        } catch (Exception e) {
            log.error("Error collecting job metrics for job {}: {}", job.getId(), e.getMessage(), e);
        }
    }
}
