package com.p3.resource_monitor.poc.persistance.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;


@Entity
@Table(name = "instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instance {
    @Id
    @UuidGenerator
    private String id;
    private String name;
    private String ipAddress;
    private String hostName;

    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL)
    private List<Job> jobs;

}