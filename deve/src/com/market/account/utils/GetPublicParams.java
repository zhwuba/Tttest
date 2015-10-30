package com.market.account.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class GetPublicParams {
    public static HashMap<String, String> getPublicParaForPush(Context context, String pName, int id) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // get IMEI
        String imei = tm.getDeviceId();

        // get lcd resolution
        WindowManager winManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        winManager.getDefaultDisplay().getMetrics(outMetrics);
        String lcdResolution = Integer.toString(outMetrics.widthPixels) + "x" + Integer.toString(outMetrics.heightPixels);
        String lcdResolution_wh = "&w=" + Integer.toString(outMetrics.widthPixels) + "&h=" + Integer.toString(outMetrics.heightPixels);
        // get sdcard status
        String sdcardStatus = Environment.getExternalStorageState();
        if (sdcardStatus.equals(Environment.MEDIA_MOUNTED) || sdcardStatus.equals(Environment.MEDIA_SHARED)) {
            sdcardStatus = "1";
        }
        else {
            sdcardStatus = "0";
        }

        // get network type
        String NTstr = null;
        // TelephonyManager tm = TelephonyManager.getDefault();
        int networkType = getAvailableNetWorkType(context);
        if (networkType == ConnectivityManager.TYPE_MOBILE) {
            int gprsType = tm.getNetworkType();
            if (gprsType == TelephonyManager.NETWORK_TYPE_UMTS) {
                NTstr = "3g";
            }
            else {
                NTstr = "2g";
            }
        }
        else  {
            NTstr = "wifi";
        }

        // get imsi
        String imsiStr = tm.getSubscriberId();

        // get android version
        String googleVersion = Build.VERSION.RELEASE;

        // get g-sensor status
        String gSensorStatus = "0";
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> gSensorList = sensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (gSensorList.size() > 0)  {
            gSensorStatus = "1";
        }

        // batch
        String batch = android.os.Build.HARDWARE;
        // SoftVersion
        String softVersion = android.os.Build.DISPLAY;
        // android version
        String androidVersion = android.os.Build.VERSION.RELEASE;
        // mem
        String mem = getTotalMemory(context);
        // versionCode
        String versionCode = getVersionCode(context, pName) + "";
        // td
        String td = getTD(context, id);
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("iccid", tm.getSimSerialNumber());
        hm.put("uuid", getMyUUID(context));
        hm.put("imei", imei);
        hm.put("imsi", imsiStr);
        hm.put("lcd", lcdResolution);
        hm.put("sdcardStatus", sdcardStatus);
        hm.put("NTstr", NTstr);
        hm.put("gSensorStatus", gSensorStatus);
        hm.put("batch", batch);
        hm.put("softVersion", softVersion);
        hm.put("androidVersion", androidVersion);
        hm.put("versionCode", versionCode);
        hm.put("lcdResolution_wh", lcdResolution_wh);
        hm.put("td", td != null ? td.trim() : null);
        return hm;
    }

    public static int getAvailableNetWorkType(Context context) {

        int NO_NETWORK_AVAILABLE = -1;
        int netWorkType = NO_NETWORK_AVAILABLE;
        Log.i("getAva", "getAvailableNetWorkType enter");
        try {
            ConnectivityManager connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connetManager == null)  {
                Log.e("getAva", "getAvailableNetWorkType, connetManager == null");
                return NO_NETWORK_AVAILABLE;
            }
            NetworkInfo[] infos = connetManager.getAllNetworkInfo();
            if (infos == null) {
                return NO_NETWORK_AVAILABLE;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++)  {
                if (infos[i].isConnected() && infos[i].isAvailable()) {
                    netWorkType = infos[i].getType();
                    Log.i("getAva", "getAvailableNetWorkType, netWorkType = " + netWorkType);
                    break;
                }
            }
        }
        catch (Exception e) {
            Log.e("getAva", "getAvailableNetWorkType exception");
            e.printStackTrace();
        }

        return netWorkType;
    }

    public static String getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try
        {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);

            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString)
            {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘1024转换为Byte
            localBufferedReader.close();

        }
        catch (IOException e) {
        }
        return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或MB，内存大小规格化
    }

    // 应用的版本号
    public static int getVersionCode(Context context, String pName) {

        int versionCode = 0;

        try {

            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
            versionCode = pinfo.versionCode;

        }
        catch (NameNotFoundException e)
        {

        }
        return versionCode;
    }

    public static String getTD(Context context, int id) {

        String td;
        InputStream is = context.getResources().openRawResource(id);
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = null;
        try {
            buffer = new byte[is.available()];
            dis.readFully(buffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally  {

            try {
                dis.close();
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        td = new String(buffer);
        return td;
    }

    private static String getMyUUID(Context context)  {

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;

        tmDevice = "" + tm.getDeviceId();

        tmSerial = "" + tm.getSimSerialNumber();

        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

        String uniqueId = deviceUuid.toString();

        return uniqueId;

    }
}
