package com.p3.resource_monitor.poc.metrics_operations;


import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import com.p3.resource_monitor.poc.persistance.models.SystemMetrics;
import com.p3.resource_monitor.poc.persistance.repos.SystemMetricsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;
import java.time.Instant;
import java.util.List;

import static com.p3.resource_monitor.poc.metrics_operations.Utils.getRealIpAddress;


@Component
@RequiredArgsConstructor
@Slf4j
public class OverAllSystemMetricsCollector {

  private final InstanceRepository instanceRepository;
  private final SystemMetricsRepository systemMetricsRepository;

  private final SystemInfo systemInfo = new SystemInfo();
  private final OperatingSystem os = systemInfo.getOperatingSystem();

  @PostConstruct
  public void init() {
    log.info("MetricsCollector initialized for machine: {}", os.toString());
  }

  @Scheduled(fixedRate = 2000)
  public void collectMetrics() {
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      String ipAddress = getRealIpAddress();

      Instance instance =
          instanceRepository.findAll().stream()
              .filter(
                  i ->
                      i.getHostName().equalsIgnoreCase(hostName)
                          && i.getIpAddress().equals(ipAddress))
              .findFirst()
              .orElse(null);

      if (instance == null) {
        log.warn("No matching instance found for host: {}, IP: {}", hostName, ipAddress);
        return;
      }

      SystemInfo systemInfo = new SystemInfo();
      CentralProcessor processor = systemInfo.getHardware().getProcessor();
      GlobalMemory memory = systemInfo.getHardware().getMemory();
      List<NetworkIF> networkIFs = systemInfo.getHardware().getNetworkIFs();
      for (NetworkIF networkIF : networkIFs) {
        networkIF.getBytesSent();
      }


      double cpuLoad = processor.getSystemCpuLoad(1000) * 100;

      // Memory Usage in Percentage
      long totalMemory = memory.getTotal();
      long availableMemory = memory.getAvailable();
      long usedMemory = totalMemory - availableMemory;
      double memoryUsedPercent = (double) usedMemory / totalMemory * 100;

      // Memory in GB (Optional)
      double usedMemoryGB = usedMemory / 1e9;
      double totalMemoryGB = totalMemory / 1e9;

      // Network in MB
      long bytesSentPerSec = networkIFs.stream().mapToLong(NetworkIF::getBytesSent).sum();
      long bytesReceivedPerSec = networkIFs.stream().mapToLong(NetworkIF::getBytesRecv).sum();
      double totalSentMB = bytesSentPerSec / 1e6;
      double totalReceivedMB = bytesReceivedPerSec / 1e6;




      long totalDiskBytes = 0;
      long usableDiskBytes = 0;

      for (HWDiskStore disk : systemInfo.getHardware().getDiskStores()) {
        for (HWPartition partition : disk.getPartitions()) {
          File partitionFile = new File(partition.getMountPoint());
          if (partitionFile.exists()) {
            totalDiskBytes += partitionFile.getTotalSpace();
            usableDiskBytes += partitionFile.getUsableSpace();
          }
        }
      }
      double totalDiskGB = totalDiskBytes / 1e9;
      long usedDiskBytes = totalDiskBytes - usableDiskBytes;
      double usedDiskPercent = (double) usedDiskBytes / totalDiskBytes * 100;


      SystemMetrics systemMetrics =
          SystemMetrics.builder()
              .cpu(String.format("%.2f%%", cpuLoad))
              .memory(
                  String.format(
                      "%.2f%% used (%.2f / %.2f Gib)",
                      memoryUsedPercent, usedMemoryGB, totalMemoryGB))
              .TotalNetworkSending(String.format("%.2f Mib", totalSentMB))
              .TotalNetworkReceive(String.format("%.2f Mib", totalReceivedMB))
//                  .networkSending(String.format("%.2f kb/s", sentKBps))
//                  .networkReceiving(String.format("%.2f kb/s", receivedKBps ))
              .disk(String.format("%.2f%%", usedDiskPercent))
              .timestamp(Instant.now())
              .instance(instance)
              .build();
      systemMetricsRepository.save(systemMetrics);
    } catch (Exception e) {
      log.error("Error collecting system metrics", e);
    }
  }

  @Scheduled(fixedRate = 5000)
  public void cleanOldMetrics() {
    Instant cutoffTime = Instant.now().minusSeconds(60);
    systemMetricsRepository.deleteMetricsOlderThan(cutoffTime);
  }
}