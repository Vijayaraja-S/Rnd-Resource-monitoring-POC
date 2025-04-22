package com.p3.resource_monitor.poc.metrics_operations;

import com.netflix.discovery.DiscoveryClient;
import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;
import jakarta.annotation.PostConstruct;
import java.net.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import static com.p3.resource_monitor.poc.metrics_operations.Utils.getRealIpAddress;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstanceRegistrar {

  private final InstanceRepository instanceRepository;
  private DiscoveryClient discoveryClient;

  @PostConstruct
  public void registerInstance() {
    try {


      SystemInfo systemInfo = new SystemInfo();
      String hostName = InetAddress.getLocalHost().getHostName();
      String ipAddress = getRealIpAddress();
      String osName = systemInfo.getOperatingSystem().getFamily();

      boolean exists = instanceRepository.existsByIpAddressAndHostName(ipAddress, hostName);

      if (!exists) {
        Instance instance =
            Instance.builder().hostName(hostName).ipAddress(ipAddress).name(osName).build();

        instanceRepository.save(instance);
        log.info("✅ Instance registered: {}", instance);
      } else {
        log.info("ℹ️ Instance already registered.");
      }

    } catch (Exception e) {
      log.error("❌ Failed to register instance.");
      e.printStackTrace();
    }
  }
}
