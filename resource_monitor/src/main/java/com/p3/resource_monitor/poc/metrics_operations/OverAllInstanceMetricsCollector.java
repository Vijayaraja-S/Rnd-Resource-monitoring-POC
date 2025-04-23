package com.p3.resource_monitor.poc.metrics_operations;

import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import com.p3.resource_monitor.poc.persistance.repos.InstanceMetricsRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.Instant;
import java.util.Enumeration;
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

  private final SystemInfo systemInfo = new SystemInfo();
  private final OperatingSystem os = systemInfo.getOperatingSystem();

  @Scheduled(fixedRate = 2000)
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

    double cpuLoad = 100 * newProcess.getProcessCpuLoadBetweenTicks(oldProcess);

    long totalVirtualMemory = systemInfo.getHardware().getMemory().getTotal();
    long usedMemory = process.getResidentSetSize();
    double usedMemoryPercent = (double) usedMemory / totalVirtualMemory * 100;
    double usedMemoryGB = usedMemory / 1e9;
    double totalMemoryGB = totalVirtualMemory / 1e9;

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
            .cpu(String.format("%.2f%%", cpuLoad))
            .memory(
                String.format(
                    "%.2f%% used (%.2f / %.2f GiB)",
                    usedMemoryPercent, usedMemoryGB, totalMemoryGB))
            .disk(String.format("Read: %.2f MiB, Write: %.2f MiB", diskReadMB, diskWriteMB))
            .instanceRunningTime(uptime)
            .timestamp(Instant.now())
            .build();

    log.info("Instance metrics: {}", processMetrics);
    instanceMetricsRepository.save(processMetrics);
  }

  private Integer fetchPid(Integer port) throws IOException {
    ProcessBuilder builder = new ProcessBuilder("lsof", "-i", ":" + port);
    Process process = builder.start();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("java") || line.toLowerCase().contains("java")) {
          String[] parts = line.trim().split("\\s+");
          System.out.println("PID using port " + port + ": " + parts[1]);
          return Integer.parseInt(parts[1]);
        }
      }
      throw new IOException("No process found on port " + port);
    }
  }

  public String getRealIpAddress() throws SocketException, UnknownHostException {
    Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

    while (networkInterfaces.hasMoreElements()) {
      NetworkInterface ni = networkInterfaces.nextElement();
      if (ni.isLoopback() || !ni.isUp()) continue;

      Enumeration<InetAddress> addresses = ni.getInetAddresses();
      while (addresses.hasMoreElements()) {
        InetAddress addr = addresses.nextElement();
        if (addr instanceof Inet4Address
            && !addr.isLoopbackAddress()
            && addr.isSiteLocalAddress()) {
          return addr.getHostAddress();
        }
      }
    }

    return InetAddress.getLocalHost().getHostAddress();
  }

  @Scheduled(fixedRate = 5000)
  public void cleanOldMetrics() {
    Instant cutoffTime = Instant.now().minusSeconds(60);
    instanceMetricsRepository.deleteMetricsOlderThan(cutoffTime);
  }
}
