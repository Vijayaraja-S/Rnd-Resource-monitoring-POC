package com.p3.resource_monitor.poc.metrics_operations;

import java.net.*;
import java.util.Enumeration;

public class Utils {

    public static String getRealIpAddress() throws SocketException, UnknownHostException {
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
}