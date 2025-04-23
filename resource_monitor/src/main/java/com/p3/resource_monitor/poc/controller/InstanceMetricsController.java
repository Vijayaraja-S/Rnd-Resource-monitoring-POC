package com.p3.resource_monitor.poc.controller;

import com.p3.resource_monitor.poc.persistance.models.InstanceMetrics;

import com.p3.resource_monitor.poc.service.InstanceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class InstanceMetricsController {

    private final InstanceMetricsService instanceMetricsService;

    @GetMapping("/last-hour/{instanceId}")
    public List<InstanceMetrics> getMetricsForLastHour(@PathVariable String instanceId) {
        return instanceMetricsService.getLastOneHourMetrics(instanceId);
    }
}
