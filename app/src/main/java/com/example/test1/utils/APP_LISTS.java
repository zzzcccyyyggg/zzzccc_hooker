package com.example.test1.utils;

import static com.example.test1.utils.MyLog.Log_FileWrite_zzzccc;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.example.test1.utils.MyLog;
import java.util.List;

public class APP_LISTS {
    public static void getAppList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : apps) {
            // 获取应用的名称
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            // 获取应用的包名
            String packageName = appInfo.packageName;
            // 打印到日志或者处理获取到的应用名称和包名
            Log.e("installed app","App Name: " + appName + ", Package Name: " + packageName);
            Log_FileWrite_zzzccc(context,"App Name: " + appName + ", Package Name: " + packageName);
        }
    }
}
