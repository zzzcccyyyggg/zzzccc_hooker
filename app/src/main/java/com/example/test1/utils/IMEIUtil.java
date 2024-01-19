package com.example.test1.utils;

import static com.example.test1.utils.MyLog.Log_FileWrite_zzzccc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.example.test1.utils.MyLog;

import java.lang.reflect.Method;
public class IMEIUtil {

    /**
     * 获取默认的imei  一般都是IMEI
     *
     * @param context
     * @return
     */
    public static void getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //Android+
        if (telephonyManager != null) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                try{
                    String serialNumber = Build.getSerial();
                    Log.e("LSPosed_zzzcccxposed","serialNumber: "+serialNumber);
                    Log_FileWrite_zzzccc(context,"serialNumber: "+serialNumber);
                }catch (SecurityException e){
                    Log.e("LSPosed_zzzcccxposed","serialNumber: cant get");
                    Log_FileWrite_zzzccc(context,"serialNumber: cant get");
                    e.printStackTrace();
                }
            }
            // API 26+
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT<=Build.VERSION_CODES.Q) {
                String imei = telephonyManager.getImei();
                String meid = telephonyManager.getMeid();
                Log.e("LSPosed_zzzcccxposed","imei: "+ imei);
                Log.e("LSPosed_zzzcccxposed","meid: "+ meid);
                Log_FileWrite_zzzccc(context,"imei: "+ imei);
                Log_FileWrite_zzzccc(context,"meid: "+ meid);
                // 使用获取到的IMEI
            } else {
                // API 25及以下
                String imei = telephonyManager.getDeviceId();
                Log.e("LSPosed_zzzcccxposed","imei is "+ imei);
                Log_FileWrite_zzzccc(context,"imei is "+ imei);
                // 使用获取到的IMEI
            }
        }
    }
}