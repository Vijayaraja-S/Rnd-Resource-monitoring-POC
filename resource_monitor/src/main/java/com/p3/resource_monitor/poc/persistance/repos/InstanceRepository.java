package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.Instance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface InstanceRepository extends JpaRepository<Instance, String> {
    boolean existsByIpAddressAndInstanceNameAndPort(String ipAddress, String instanceName, Integer port);

    List<Instance> findInstanceByIpAddressAndInstanceName(String ipAddress, String INSTANCE_NAME);

}
