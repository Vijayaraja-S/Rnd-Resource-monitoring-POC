package com.p3.resource_monitor.poc.persistance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "job_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMetrics {
    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    private String cpu;
    private String memory;
    private String network;
    private String disk;

    private Instant timestamp;
}