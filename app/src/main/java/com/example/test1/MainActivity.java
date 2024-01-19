package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 检查权限是否已被授予
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 如果没有授予权限，需要请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // 获取SSID可能需要定位权限，并且可能返回 "<unknown ssid>" 如果权限不足 Android 10之后无权限获取ssid
            String ssid = wifiInfo.getSSID();
            if (ssid != null) {
                // 注意SSID被双引号包围，如果需要可以去掉
                ssid = ssid.substring(1, ssid.length() - 1);
                Log.e("LSPosed","ssid: "+ssid);
            }
            String bssid = wifiInfo.getBSSID();
            if (bssid != null){
                Log.e("LSPosed","bssid: "+bssid);
            }

            try{
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if(telephonyManager.getSimState()== TelephonyManager.SIM_STATE_ABSENT){
                    Log.e("LSPosed","SIM card: not exit");
                }
                else{
                    String iccid = telephonyManager.getSimSerialNumber();
                    if(iccid!=null){
                        Log.e("LSPosed","iccid: "+iccid);
                    }
                }
            }catch (SecurityException e){
                Log.e("LSPosed","Can not ge iccid !");
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
            // REQUEST_CODE 是你定义的整数，用于 onRequestPermissionsResult 回调
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            //Android+
            if (telephonyManager != null) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    try{
                        String serialNumber = Build.getSerial();
                        Log.e("LSPosed","serialNumber: "+serialNumber);
                    }catch (SecurityException e){
                        Log.e("LSPosed","serialNumber: cant get");
                        e.printStackTrace();
                    }
                }
                // API 26+
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT<=Build.VERSION_CODES.Q) {
                    String imei = telephonyManager.getImei();
                    String meid = telephonyManager.getMeid();
                    Log.e("LSPosed","imei: "+ imei);
                    Log.e("LSPosed","meid: "+ meid);
                    // 使用获取到的IMEI
                } else {
                    // API 25及以下
                    String imei = telephonyManager.getDeviceId();
                    Log.e("LSPosed","imei is "+ imei);
                    // 使用获取到的IMEI
                }
            }
            // 使用获取到的ICCID
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PRECISE_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PRECISE_PHONE_STATE}, REQUEST_CODE);
            // REQUEST_CODE 是你定义的整数，用于 onRequestPermissionsResult 回调
        } else {
        }

        Button button = findViewById(R.id.init_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSoPath();
            }
        });
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {"android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private void initSoPath() {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk <= 29){
            //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "请开通相关权限，否则无法正常使用本应用！", Toast.LENGTH_SHORT).show();
                }
                //申请权限
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            } else {
                writeSdcard();
            }
        }
        else {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            } else {
                writeSdcard();
            }
        }
    }

    private void writeSdcard()  {
        String text = "";
        PackageManager pm = getPackageManager();
        List<PackageInfo> pkgList = pm.getInstalledPackages(0);
        if(pkgList.size() > 0) {
            for (PackageInfo pi : pkgList) {
                //   /data/app/~~cFiynmB1ZhW3l4ffMY7duw==/com.mdcg.guaguaxposed-zyuZcPG2uq6jw8Lc7DT40A==/base.apk
                if (pi.applicationInfo.publicSourceDir.indexOf("com.example.test1") != -1) {
                    text = pi.applicationInfo.publicSourceDir.replace("base.apk", "lib/arm64/libtest1.so");
                    Log.d("zzzccc",text);
                }
            }
        }

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file1=new File("/sdcard/","zzzcccXposed2.txt");
                if (!file1.exists()){
                    try {
                        file1.createNewFile();
                        file1.setExecutable(true, false);
                        file1.setReadable(true, false);
                        file1.setWritable(true, false);
                    } catch (IOException e) {
                        Log.d("zzzccc","创建失败");
                        e.printStackTrace();
                    }
                }
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file1);
                    fileOutputStream.write(text.getBytes());
                    Log.d("zzzccc","写入"+text);
                    Toast.makeText(this, "初始化成功！", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                            Log.d("zzzccc","文件关闭成功");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}