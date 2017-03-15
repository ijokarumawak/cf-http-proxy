package com.hortonworks.cf.http.proxy;

import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class HttpProxy {

    public static void main(String[] args) {

        final String portStr = System.getenv("PORT");
        System.out.println("$PORT=" + portStr);
        int proxyServerPort = Integer.parseInt(portStr);
        System.out.println("Starting a proxy server on port " + proxyServerPort);
        DefaultHttpProxyServer.bootstrap()
                .withPort(proxyServerPort)
                .withAllowLocalOnly(false)
                .start();
        System.out.println("A proxy server started successfully on port " + proxyServerPort);
    }

}
