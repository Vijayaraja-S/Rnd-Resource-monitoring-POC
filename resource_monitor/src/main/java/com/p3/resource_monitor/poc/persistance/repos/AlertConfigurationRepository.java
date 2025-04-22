package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, String> {
    AlertConfiguration findByInstanceId(String instanceId);
}