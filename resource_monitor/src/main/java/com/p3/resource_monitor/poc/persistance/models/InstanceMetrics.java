package com.p3.resource_monitor.poc.persistance.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(name = "instance_metrics")
public class InstanceMetrics {
    @Id
    @UuidGenerator
    private String id;
    private String cpu;
    private String memory;
    private String disk;
    private String TotalNetworkSending;
    private String TotalNetworkReceive;
    private String instanceRunningTime;
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "instance_id", nullable = false)
    private Instance instance;
}