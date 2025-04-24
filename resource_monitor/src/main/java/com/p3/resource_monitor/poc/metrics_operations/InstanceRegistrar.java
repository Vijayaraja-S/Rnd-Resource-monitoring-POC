package com.p3.resource_monitor.poc.metrics_operations;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import com.p3.resource_monitor.poc.persistance.models.Instance;
import com.p3.resource_monitor.poc.persistance.repos.InstanceRepository;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InstanceRegistrar {

  private final InstanceRepository instanceRepository;
  private final DiscoveryClient discoveryClient;
  private final EurekaClient client;
  @Async("taskExecutor")
  @Scheduled(cron = "*/2 * * * * *")
  public void registerInstance(){
    try {
      log.info("Registering instance...");
      List<String> serviceList = discoveryClient.getServices();
      List<Instance> instancesList = new ArrayList<>();

      for (String s : serviceList) {
        Applications applications = client.getApplications(s);
        for (Application registeredApplication : applications.getRegisteredApplications()) {
          List<InstanceInfo> instances = registeredApplication.getInstances();
          for (InstanceInfo info : instances) {
            log.info("Instance found: {}", info);
            Instance.InstanceBuilder builder = Instance.builder();
            instancesList.add(
                builder
                    .instanceName(info.getAppName())
                    .instanceId(info.getId())
                    .ipAddress(info.getIPAddr())
                    .port(info.getPort())
                    .build());
          }
        }
      }

      for (Instance instance : instancesList) {
        boolean exists =
            instanceRepository.existsByIpAddressAndInstanceNameAndPort(
                instance.getIpAddress(), instance.getInstanceName(), instance.getPort());

        if (!exists) {
          instanceRepository.save(instance);
          log.info("Instance registered: {}", instance);
        } else {
          log.info("ℹ️ Instance already registered.");
        }
      }
    } catch (Exception e) {
      log.error("Failed to register instance.");
      e.printStackTrace();
    }
  }
}
