package com.example.test1.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import de.robv.android.xposed.XposedBridge;

public class MAC {
    public static String getMachineHardwareAddress() {
        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String hardWareAddress = null;
        NetworkInterface iF = null;
        if (interfaces == null) {
            return null;
        }
        while (((Enumeration<?>) interfaces).hasMoreElements()) {
            iF = interfaces.nextElement();
            try {
                hardWareAddress = bytesToString(iF.getHardwareAddress());
                if (hardWareAddress != null)
                    break;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return hardWareAddress;
    }
    /***
     * byte转为String
     *
     * @param bytes
     * @return
     */
    private static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(String.format("%02X:", b));
        }
        if (buf.length()>0) {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }
    private String readMacAddress(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String mac = reader.readLine();
            reader.close();
            return mac != null ? mac.trim() : "Unavailable";
        } catch (Exception e) {
            XposedBridge.log(e.toString());
            return "Error";
        }
    }
}