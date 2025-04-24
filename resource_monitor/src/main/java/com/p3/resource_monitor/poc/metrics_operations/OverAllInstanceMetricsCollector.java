package com.p3.resource_monitor.poc.metrics_operations;

import static com.p3.resource_monitor.poc.metrics_operations.MetricUtils.fetchPid;
import static com.p3.resource_monitor.poc.metrics_operations.MetricUtils.getRealIpAddress;

import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;
import com.p3.resource_monitor.poc.persistance.repos.InstanceMetricsRepository;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverAllInstanceMetricsCollector {

  private final InstanceRepository instanceRepository;
  private final InstanceMetricsRepository instanceMetricsRepository;
  MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

  // This uses the taskExecutor defined above
  @Scheduled(cron = "*/1 * * * * *")
  public void collectMetrics() throws Exception {
    try {
      log.info("Collecting system metrics...");

      String ipAddress = getRealIpAddress();
      String instanceName = "RESOURCE-MONITOR";

      List<Instance> instanceByIpAddressAndInstanceName =
          instanceRepository.findInstanceByIpAddressAndInstanceName(ipAddress, instanceName);

      if (CollectionUtils.isEmpty(instanceByIpAddressAndInstanceName)) {
        log.warn(
            "No matching instance found for instanceName: {}, IP: {}", instanceName, ipAddress);
        return;
      }
      for (Instance instance : instanceByIpAddressAndInstanceName) {
        if (Objects.nonNull(instance.getPid())) {
          initMetricsCalculations(instance.getPid(), instance);
        } else {
          instance.setPid(fetchPid(instance.getPort()));
          instanceRepository.save(instance);
          initMetricsCalculations(instance.getPid(), instance);
        }
      }

    } catch (Exception e) {
      log.error("Error collecting system metrics: {}", e.getMessage(), e);
      throw new Exception("Error collecting system metrics: " + e.getMessage());
    }
  }

  private void initMetricsCalculations(Integer pid, Instance instance) throws InterruptedException {
    SystemInfo systemInfo = new SystemInfo();
    List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();

    OperatingSystem os = systemInfo.getOperatingSystem();
    OSProcess process = os.getProcess(pid);

    if (process == null) {
      System.out.println("No process found with PID: " + pid);
      return;
    }
    process.updateAttributes();
    OSProcess oldProcess = os.getProcess(pid);
    Thread.sleep(100);
    OSProcess newProcess = os.getProcess(pid);


    CentralProcessor processor = systemInfo.getHardware().getProcessor();
    int logicalProcessorCount = processor.getLogicalProcessorCount();

    double cpuLoad = 100 * newProcess.getProcessCpuLoadBetweenTicks(oldProcess);
    double normalizedCpuLoad = cpuLoad / logicalProcessorCount;

    MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
    long heapMemory = heapUsage.getUsed() / (1024 * 1024);

    long bytesRead = process.getBytesRead();
    long bytesWritten = process.getBytesWritten();
    double diskReadMB = bytesRead / 1e6;
    double diskWriteMB = bytesWritten / 1e6;

    long uptimeMillis = process.getUpTime();
    String uptime = String.format("%d sec", uptimeMillis / 1000);

    long bytesSentPerSec = networkIFs.stream().mapToLong(NetworkIF::getBytesSent).sum();
    long bytesReceivedPerSec = networkIFs.stream().mapToLong(NetworkIF::getBytesRecv).sum();
    double totalSentMB = bytesSentPerSec / 1e6;
    double totalReceivedMB = bytesReceivedPerSec / 1e6;

    InstanceMetrics processMetrics =
        InstanceMetrics.builder()
            .instance(instance)
            .TotalNetworkSending(String.format("%.2f Mib", totalSentMB))
            .TotalNetworkReceive(String.format("%.2f Mib", totalReceivedMB))
            .cpu(String.format("%.2f%%", normalizedCpuLoad))
            .memory("Heap Memory(MB): " + heapMemory)
            .disk(String.format("Read: %.2f MiB, Write: %.2f MiB", diskReadMB, diskWriteMB))
            .instanceRunningTime(uptime)
            .timestamp(Instant.now())
            .build();

    log.info("Instance metrics: {}", processMetrics);
    instanceMetricsRepository.save(processMetrics);
  }

  @Scheduled(cron = "*/5 * * * * *")
  public void cleanOldMetrics() {
    Instant cutoffTime = Instant.now().minusSeconds(300);
    instanceMetricsRepository.deleteMetricsOlderThan(cutoffTime);
  }
}
