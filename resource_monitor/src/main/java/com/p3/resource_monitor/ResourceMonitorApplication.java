package com.p3.resource_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ResourceMonitorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ResourceMonitorApplication.class, args);
  }
}
