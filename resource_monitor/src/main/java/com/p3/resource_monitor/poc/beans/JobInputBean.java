package com.p3.resource_monitor.poc.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobInputBean {
    private String connectionType;
    private String outputDir;
    private ConnectionBean connection;
}
