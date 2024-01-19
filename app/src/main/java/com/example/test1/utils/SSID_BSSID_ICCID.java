package com.example.test1.utils;

import static com.example.test1.utils.MyLog.Log_FileWrite_zzzccc;
import static com.example.test1.utils.MyLog.Log_zzzccc;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class SSID_BSSID_ICCID {
    public static void getSSID_BSSID_ICCID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // 获取SSID可能需要定位权限，并且可能返回 "<unknown ssid>" 如果权限不足 Android 10之后无权限获取ssid
        String ssid = wifiInfo.getSSID();
        if (ssid != null) {
            // 注意SSID被双引号包围，如果需要可以去掉
            ssid = ssid.substring(1, ssid.length() - 1);
            Log_zzzccc("ssid: "+ssid);
            Log_FileWrite_zzzccc(context,"ssid: "+ssid);
        }
        String bssid = wifiInfo.getBSSID();
        if (bssid != null){
            Log_zzzccc("bssid: "+bssid);
            Log_FileWrite_zzzccc(context,"bssid: "+bssid);
        }

        try{
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(telephonyManager.getSimState()== TelephonyManager.SIM_STATE_ABSENT){
                Log_zzzccc("SIM card: not exit");
                Log_FileWrite_zzzccc(context,"SIM card: not exit");
            }
            else{
                String iccid = telephonyManager.getSimSerialNumber();
                if(iccid!=null){
                    Log_zzzccc("iccid: "+iccid);
                    Log_FileWrite_zzzccc(context,"iccid: "+iccid);
                }
            }
        }catch (SecurityException e){
            Log_zzzccc("Can not ge iccid !");
            Log_FileWrite_zzzccc(context,"Can not ge iccid !");
        }

    }
}
