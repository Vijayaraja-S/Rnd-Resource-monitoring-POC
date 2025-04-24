package com.p3.resource_monitor.poc.test;

import java.lang.management.*;
import java.util.List;

public class JVMMonitoringApp {
    public static void main(String[] args) throws InterruptedException {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        OperatingSystemMXBean osMXBean =  ManagementFactory.getOperatingSystemMXBean();
        OperatingSystemMXBean osBean =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        while (true) {
            System.out.println("=== JVM Monitoring Metrics ===");

            // Memory
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();

            System.out.println("Heap Memory Used (MB): " + (heapUsage.getUsed() / (1024 * 1024)));
            System.out.println("Heap Memory Max (MB): " + (heapUsage.getMax() / (1024 * 1024)));
            System.out.println("Non-Heap Memory Used (MB): " + (nonHeapUsage.getUsed() / (1024 * 1024)));
            // Threads
            System.out.println("Thread Count: " + threadMXBean.getThreadCount());
            System.out.println("Daemon Thread Count: " + threadMXBean.getDaemonThreadCount());

            // GC
            for (GarbageCollectorMXBean gc : gcMXBeans) {
                System.out.println("GC Name: " + gc.getName());
                System.out.println("GC Collection Count: " + gc.getCollectionCount());
                System.out.println("GC Collection Time (ms): " + gc.getCollectionTime());
            }

            // Class loading
            System.out.println("Loaded Class Count: " + classLoadingMXBean.getLoadedClassCount());
            System.out.println("Total Loaded: " + classLoadingMXBean.getTotalLoadedClassCount());
            System.out.println("Unloaded Class Count: " + classLoadingMXBean.getUnloadedClassCount());

            // CPU (JVM process load)
            System.out.println("Available Processors: " + osMXBean.getAvailableProcessors());
            System.out.println("System Load Average (last minute): " + osMXBean.getSystemLoadAverage());


            double cpuLoad = osMXBean.getSystemLoadAverage(); // returns value between 0.0 and 1.0

            System.out.printf("CPU Usage: %.2f%%\n", cpuLoad * 100);
            System.out.printf("Heap Used: %.2f MB\n", heapUsage.getUsed() / 1024.0 / 1024);

            System.out.println("=====================================");
            Thread.sleep(5000); // Refresh every 5 seconds
        }
    }
}
