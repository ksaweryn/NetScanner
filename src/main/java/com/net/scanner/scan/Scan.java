/*
 * Copyrigth (c) 2017 Javier Borja to present.
 * All rights reserved.
 */
package com.net.scanner.scan;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Javier Borja
 * @date Dec 27, 2017
 */
public class Scan {

    public static void main(final String... args) {
        
        //TODO get subnet from my IP
        List<String> ips = checkHosts("192.168.1");
        final int timeout = 200;
        long startTime = System.currentTimeMillis();
        final List<Future<Boolean>> futures = new ArrayList<>();
        for (String ip : ips) {
            System.out.println("Analyzing ip: " + ip);
            final ExecutorService es = Executors.newFixedThreadPool(20);
            for (int port = 1; port <= 65535; port++) {
                futures.add(portIsOpen(es, ip, port, timeout));
            }
            es.shutdown();
        }
        int openPorts = 0;

        for (Future<Boolean> f : futures) {

            try {
                if (f.get()) {
                    openPorts++;
                }
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(Scan.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        System.out.println("There is/are " + openPorts + " open port(s)");
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Time in secs " + elapsedTime / 1000);

    }

    public static Future<Boolean> portIsOpen(final ExecutorService es, final String ip, final int port, final int timeout) {
        return es.submit(() -> {
            Socket socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(ip, port), timeout);
                System.out.println("Opened port " + port);
                socket.close();
                return true;
            } catch (IOException ex) {
                return false;
            }
        });
    }

    private static List<String> checkHosts(String subnet) {
        int timeout = 2000;
        List<String> ips = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            String host = subnet + "." + i;
            try {
                if (InetAddress.getByName(host).isReachable(timeout)) {
                    System.out.println(host + " is reachable");
                    ips.add(host);
                }
            } catch (UnknownHostException ex) {
                Logger.getLogger(Scan.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Scan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ips;
    }

}
