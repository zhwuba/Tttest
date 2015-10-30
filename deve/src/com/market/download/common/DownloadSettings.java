package com.market.download.common;

import com.market.download.util.Util;
import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;

public class DownloadSettings {
    public static final String TAG = "Settings";

    public static final String IMEI_DEFAULT = "111111111111111";
    public static final String IMSI_DEFAULT = "123456789012345";

    
    // private static final String KEY_MAX_DOWNLOAD_NUM = "maxDownNum";
    private static final String KEY_BG_INSTALL_FLAG = "bgInstallFlag";


    public static boolean getBgInstallFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_BG_INSTALL_FLAG, true);
    }

    public static void setBgInstallFlag(Context context, boolean bgInstallFlag) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_BG_INSTALL_FLAG, bgInstallFlag);
        editor.commit();
    }

    private static final String KEY_WIFI_VER = "wifiVer";
    private static final String KEY_WIFI_ENABLE = "wifiEnable";
    private static final String KEY_WIFI_EXECTIME = "wifiExectime";
    private static final String KEY_WIFI_EXPIRE = "wifiExpire";
    private static final String KEY_WIFI_FGFLAG = "wifiFgflag";

    public static void setWifiDownConfig(Context context, int version, boolean enable, String exectime, int expire, boolean fgflag) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_WIFI_VER, version);
        editor.putBoolean(KEY_WIFI_ENABLE, enable);
        if (exectime != null) {
            editor.putString(KEY_WIFI_EXECTIME, exectime);
        }
        long currMillis = System.currentTimeMillis();
        long expireMillis = currMillis + (expire *24 * 60 * 60 * 1000);			//days
        editor.putLong(KEY_WIFI_EXPIRE, expireMillis);
        editor.putBoolean(KEY_WIFI_FGFLAG, fgflag);
        editor.commit();
    }

    public static class WifiDownConfig {
        public int version;
        public boolean enable;
        public String exectime;
        public long expireMillis;
        public boolean fgflag;
    }

    public static WifiDownConfig getWifiDownConfig(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        WifiDownConfig config = new WifiDownConfig();
        config.version = sp.getInt(KEY_WIFI_VER, 0);
        config.enable = sp.getBoolean(KEY_WIFI_ENABLE, true);
        config.exectime = sp.getString(KEY_WIFI_EXECTIME, null);
        config.expireMillis = sp.getLong(KEY_WIFI_EXPIRE, 0);
        config.fgflag = sp.getBoolean(KEY_WIFI_FGFLAG, false);
        return config;
    }

    /*
     * start for get update list periodicity
     */
    public static void setGetUpdateMillis(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("getUpdateMillis", System.currentTimeMillis());
        editor.commit();
    }

    private static final long GET_UPDATE_PERIOD_MILLIS = 2 * 24 * 60 * 60 * 1000;

    public static boolean isTimeToGetUpdate(Context context) {
        boolean flag = false;
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        long getMillis = sp.getLong("getUpdateMillis", 0);
        if (getMillis == 0) {
            flag = true;

        } else {
            if ((System.currentTimeMillis() - getMillis) >= GET_UPDATE_PERIOD_MILLIS) {
                flag = true;
            }
        }

        return flag;
    }

    /*
     * start for download and install update app
     */
    public static void setUserUpdateAutoFlag(Context context, boolean flag) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("UserUpdateAutoFlag", flag);
        editor.commit();

//        if (flag) {
//            DownloadManager.startServiceAutoUpdate(context);
//        }
    }

    public static boolean getUserUpdateAutoFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        return sp.getBoolean("UserUpdateAutoFlag", true);
    }
    
    
    public static void setServerOpenUserUpdateFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("hasDoneUserUpdateAutoFlag", true);
        editor.commit();
    }
    
    
    public static boolean hasServiceOpenUserUpdateFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        return sp.getBoolean("hasDoneUserUpdateAutoFlag", false);
    }
    
    
    public static long setFirstGetUpdateListTime(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        long firstTime = sp.getLong("firstGetUpdateListTime", 0);
        if (firstTime != 0) {
            return firstTime;
        }
        
        firstTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("firstGetUpdateListTime", firstTime);
        editor.commit();
        return firstTime;
    }
    

    public static void setUpdateAutoFlag(Context context, int flag) {
        Util.log(TAG, "setUpdateAutoFlag", "auto update flag:" + flag);
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("updateAutoFlag", flag);
        editor.commit();
    }
    

    public static int getUpdateAutoFlag(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SETTING, Context.MODE_PRIVATE);
        return sp.getInt("updateAutoFlag", 0);
    }
    
}
