package com.example.test1;

import static com.example.test1.utils.APP_LISTS.getAppList;
import static com.example.test1.utils.IMEIUtil.getIMEI;
import static com.example.test1.utils.MAC.getMachineHardwareAddress;
import static com.example.test1.utils.MyLog.Log_FileWrite_zzzccc;
import static com.example.test1.utils.MyLog.Log_zzzccc;
import static com.example.test1.utils.SSID_BSSID_ICCID.getSSID_BSSID_ICCID;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.test1.utils.ContextUtils;
import com.example.test1.utils.MyLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class CoreCrack  implements IXposedHookLoadPackage {
    ClassLoader mclassloader = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("The packagename is " + lpparam.packageName);

        //Native hook
        if (lpparam.packageName.equals("com.kwai.m2u")){
            XposedBridge.log("start inject!" + lpparam.packageName);
            mclassloader = lpparam.classLoader;
            //String path = "/data/app/com.kwai.m2u-ZDgHCJ2CZUOz2lbupTBj3g==/lib/arm64/libtest1.so";
            //XposedHelpers.callMethod(Runtime.getRuntime(), "nativeLoad", path, mclassloader);
            XposedHelpers.findAndHookMethod(System.class, "loadLibrary",String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String libraryPath = (String) param.args[0];
                    //String path = getSoPath();
                    XposedBridge.log("load "+libraryPath);
                    String path = "/data/app/com.kwai.m2u-ZDgHCJ2CZUOz2lbupTBj3g==/lib/arm64/libtest1.so";
                    if(libraryPath.equals("kmalloc_loader")){
                        XposedBridge.log("zzzccc get zzzccc.so path: " + path);
                        int version = android.os.Build.VERSION.SDK_INT;
                        if (!path.equals("")){
                            if (version >= 28) {
                                XposedBridge.log("zzzccc start inject zzzccc.so");
                                try{
                                    XposedHelpers.callMethod(Runtime.getRuntime(), "nativeLoad", path, mclassloader);
                                }catch (Exception e){
                                    Log.e("zzzccc","injected failed");
                                }
                            } else {
                                XposedHelpers.callMethod(Runtime.getRuntime(), "doLoad", path, mclassloader);
                            }
                        }
                    }

                }
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("after" );
                    super.afterHookedMethod(param);

                }
            });
        }

        //Java主动调用
        // Hook方法是为了找到合适的时机进行操作，这里以app加载为例
        if (!lpparam.packageName.equals("com.zzzccc.hook_test")) {
            return;
        }

        // 找到合适的类和方法进行hook，这里以Activity.onCreate为例
        //final Class<?> activityClass = XposedHelpers.findClass(ContextCompat.class.getName(), lpparam.classLoader);
        XposedHelpers.findAndHookMethod(ContextCompat.class.getName(), lpparam.classLoader,"checkSelfPermission", Context.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // Log or modify the permission check
                Context context = (Context) param.args[0];
                String permission = (String) param.args[1];
                // Log or modify the permission check
                Log_zzzccc("Original permission check for: " + permission);
                //Log_FileWrite_zzzccc(context,"Original permission check for: " + permission);
                Integer result = (Integer) param.getResult();
                Log_zzzccc("Permission check result was: " + result);
                //Log_FileWrite_zzzccc(context,"Permission check result was: " + result);
                // 获取Android-ID
                String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                Log_zzzccc("Original Android ID: " + androidId);
                Log_FileWrite_zzzccc(context,"Original Android ID: " + androidId);
                // 在这里读取MAC地址文件
                String macAddress = getMachineHardwareAddress();
                Log_zzzccc("MAC Address: " + macAddress);
                Log_FileWrite_zzzccc(context,"MAC Address: " + macAddress);
                //获取版本号
                Log_zzzccc("The VERSION: "+ Build.VERSION.SDK_INT);
                Log_FileWrite_zzzccc(context,"The VERSION: "+ Build.VERSION.SDK_INT);
                //获取imei与meid即序列号
                getIMEI(context);
                //获取ssid,bssid,iccid
                getSSID_BSSID_ICCID(context);
                //获取已安装应用信息
                getAppList(context);
            }
        });

        XposedHelpers.findAndHookMethod("androidx.core.app.ActivityCompat", lpparam.classLoader, "requestPermissions", Activity.class, String[].class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity activity = (Activity) param.args[0];
                String[] permission = (String[]) param.args[1];
                int requestCode = (int) param.args[2];
                Context context = activity;
                // 记录请求的权限和请求码
                Log_zzzccc("Hooked requestPermissions with request code: " + requestCode);
                // Log or modify the permission check
                Log_zzzccc("Original permission check for: " + permission);
                //Log_FileWrite_zzzccc(context,"Original permission check for: " + permission);
                Integer result = (Integer) param.getResult();
                Log_zzzccc("Permission check result was: " + result);
                //Log_FileWrite_zzzccc(context,"Permission check result was: " + result);
                // 获取Android-ID
                String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                Log_zzzccc("Original Android ID: " + androidId);
                Log_FileWrite_zzzccc(context,"Original Android ID: " + androidId);
                // 在这里读取MAC地址文件
                String macAddress = getMachineHardwareAddress();
                Log_zzzccc("MAC Address: " + macAddress);
                Log_FileWrite_zzzccc(context,"MAC Address: " + macAddress);
                //获取版本号
                Log_zzzccc("The VERSION: "+ Build.VERSION.SDK_INT);
                Log_FileWrite_zzzccc(context,"The VERSION: "+ Build.VERSION.SDK_INT);
                //获取imei与meid即序列号
                getIMEI(context);
                //获取ssid,bssid,iccid
                getSSID_BSSID_ICCID(context);
                //获取已安装应用信息
                getAppList(context);
            }
        });


    }
    private String getSoPath() throws InterruptedException {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                InputStream inputStream = null;
                Reader reader = null;
                BufferedReader bufferedReader = null;
                Thread.sleep(500);
                try {
                    File file=new File("/sdcard/"+"zzzcccXposed.txt");
                    inputStream = new FileInputStream(file);
                    reader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(reader);
                    StringBuilder result = new StringBuilder();
                    String temp;
                    while ((temp = bufferedReader.readLine()) != null) {
                        result.append(temp);
                    }
                    XposedBridge.log("read so path is " + result.toString());
                    return result.toString();

                } catch (Exception e) {
                    Log.d("zzzccc","打开失败");
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
        return "";
    }

    }


