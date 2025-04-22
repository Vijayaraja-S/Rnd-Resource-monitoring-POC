package com.p3.resource_monitor.poc.persistance.repos;


import com.p3.resource_monitor.poc.persistance.models.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByJobType(String jobType);
    List<Job> findByStartTimeBetween(Instant start, Instant end);

    List<Job> findByStatus(String ready);

}