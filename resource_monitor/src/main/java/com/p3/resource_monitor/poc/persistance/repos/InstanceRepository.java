package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.Instance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceRepository extends JpaRepository<Instance, String> {
    boolean existsByIpAddressAndHostName(String ipAddress, String hostName);
}