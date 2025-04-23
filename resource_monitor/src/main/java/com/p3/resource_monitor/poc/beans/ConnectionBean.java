package com.p3.resource_monitor.poc.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionBean {
    private String username;
    private String password;
    private String host;
    private Integer port;
    private String database;
}
