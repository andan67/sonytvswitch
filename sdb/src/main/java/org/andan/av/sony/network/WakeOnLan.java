package org.andan.av.sony.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WakeOnLan {

    public static final int PORT = 9;

    private static Matcher matcher;

    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private static Pattern pattern=Pattern.compile(IPADDRESS_PATTERN);

    public static boolean validate(final String ipAddress){
        matcher = pattern.matcher(ipAddress);
        return matcher.matches();
    }

    public static void main(String[] args) {

        // check ip pattern
        try {
            InetAddress address = InetAddress.getByName("192.168.178.27");
            System.out.println(address.getHostAddress());
            if(validate(address.getHostAddress())) {
                String broadcastAddress = address.getHostAddress()
                        .substring(0, address.getHostAddress().lastIndexOf(".")) + ".255";
                System.out.println(broadcastAddress);
            }
        } catch (UnknownHostException ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(validate("192.168.178.255"));
        wakeOnLan("AndreasSonyBraviaTV", "D8:D4:3C:4D:56:3D");
    }

    public static int wakeOnLan(String host, String macStr) {

        if (host == null || host.isEmpty() || macStr == null || macStr.isEmpty()) {
            return -1;
        }

        try {
            InetAddress hostAddress = InetAddress.getByName(host);
            String broadcastIP = null;
            if(validate(hostAddress.getHostAddress())) {
                broadcastIP = hostAddress.getHostAddress()
                        .substring(0, hostAddress.getHostAddress().lastIndexOf(".")) + ".255";
            }
            else {
                throw new Exception("No valid IP4 address");
            }

            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress broadcastAddress = InetAddress.getByName(broadcastIP);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, broadcastAddress, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            System.out.println("Wake-on-LAN packet sent.");
        } catch (Exception e) {
            System.out.println("Failed to send Wake-on-LAN packet: + e");
            return -1;
        }
        return 0;
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}

