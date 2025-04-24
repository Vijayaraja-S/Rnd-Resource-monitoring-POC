package com.p3.resource_monitor.poc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.p3.resource_monitor.poc.beans.JobInputBean;
import com.p3.resource_monitor.poc.persistance.models.Job;

import java.util.List;

public interface JobService {
    String initJob(JobInputBean jobInputBean, String instanceId) throws JsonProcessingException;

    List<Job> getJobsByInstanceId(String instanceId);
}