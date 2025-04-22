package com.p3.resource_monitor.poc.persistance.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Table(name = "alert_configuration")
public class AlertConfiguration {
    @Id
    @UuidGenerator
    private String id;
    private String cpuThreshold;
    private String storageThreshold;
    private String memoryThreshold;
    private String networkThreshold;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "instance_id", referencedColumnName = "id")
    private Instance instance;
}