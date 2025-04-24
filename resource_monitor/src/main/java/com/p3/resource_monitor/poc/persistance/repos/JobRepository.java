package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {
     List<Job> findByInstance_Id(String instanceId);

    @Query("SELECT j FROM Job j JOIN FETCH j.instance WHERE j.status = :status")
    List<Job> findByStatusWithInstance(@Param("status") String status);
}