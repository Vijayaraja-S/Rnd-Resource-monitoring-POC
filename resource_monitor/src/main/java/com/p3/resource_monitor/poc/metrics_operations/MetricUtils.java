package com.p3.resource_monitor.poc.metrics_operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;

public class MetricUtils {
    public static  String getRealIpAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            if (ni.isLoopback() || !ni.isUp()) continue;

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address
                        && !addr.isLoopbackAddress()
                        && addr.isSiteLocalAddress()) {
                    return addr.getHostAddress();
                }
            }
        }

        return InetAddress.getLocalHost().getHostAddress();
    }
    public static Integer fetchPid(Integer port) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("lsof", "-i", ":" + port);
        Process process = builder.start();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("java") || line.toLowerCase().contains("java")) {
                    String[] parts = line.trim().split("\\s+");
                    System.out.println("PID using port " + port + ": " + parts[1]);
                    return Integer.parseInt(parts[1]);
                }
            }
            throw new IOException("No process found on port " + port);
        }
    }
}
