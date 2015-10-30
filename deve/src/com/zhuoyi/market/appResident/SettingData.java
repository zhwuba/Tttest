package com.zhuoyi.market.appResident;

import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingData {
    
    public static final int MAX_DOWNLOAD_NUM = 3;
    
    public static int mDownloadMaxNum = 3;
    public static boolean mDeleteInstallPackage = false;
    public static boolean mNoShowImage = false;
    public static boolean mIsNotify = false;
    /**
     * 目前保存的稍后下载设置，true表示稍后在wifi时下载，false表示直接下载
     */
    public static boolean mDelayDownload = false;
    /**
     * 目前保存的稍后下载不再提醒状态，稍后下载设置后，该值默认为false，即不再显示提醒dialog
     */
    public static boolean mDelayNotify = true;
    /**
     * 在内存中缓存稍后下载提醒dialog是否要显示，true表示要显示一次，false表示在市场此次退出之前不再显示
     */
    private static boolean mDelayNotifyDis = true;
    /**
     * 当用户没有勾选不再提醒的对话框时，市场退出之前，统一使用该次选择，false表示直接下载，true表示稍后下载
     */
    private static boolean mDelayDownTmp = false;
    
    /**
     * 初始化设置数据
     */
    public static void initializeParams(Context context) {
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        mDownloadMaxNum = Integer.valueOf(sprefs.getString("download_maxnum", "3"));
        mDeleteInstallPackage = sprefs.getBoolean("delete_package", true);
        mNoShowImage = sprefs.getBoolean("showImage", false);
        mIsNotify = sprefs.getBoolean("have_notify", true);
        mDelayDownload = sprefs.getBoolean("delayDownload", false);
        mDelayNotify = sprefs.getBoolean("delayNotify", true);
    }
    
    
    public static void setDownloadMaxNum(Context context, int count) {
        mDownloadMaxNum = count;
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putString("download_maxnum", Integer.toString(count));
        editor.commit();
    }
    
    
    public static void setDeleteInstallPkg(Context context, boolean delete) {
        mDeleteInstallPackage = delete;
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean("delete_package", delete);
        editor.commit();
    }
    
    
    public static void setNoShowImage(Context context, boolean show) {
        mNoShowImage = show;
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean("showImage", show);
        editor.commit();
    }
    
    
    public static void setIsNotify(Context context, boolean notify) {
        mIsNotify = notify;
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean("have_notify", notify);
        editor.commit();
    }
    
    
    public static void setDelayDownload(Context context, boolean delayFlag) {
        mDelayDownload = delayFlag;
        mDelayNotify = false;
        SharedPreferences sprefs = context.getSharedPreferences(SharedPrefDefine.ACTIVITY_SETTING, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean("delayDownload", delayFlag);
        editor.putBoolean("delayNotify", false);
        
        editor.commit();
    }
    
    
    public static boolean isNeedToDisDelayDialog() {
        if (!mDelayNotify) {
            return false;
        }
        
        return mDelayNotifyDis;
    }
    
    
    public static void setTmpDelayDownFlag(boolean flag) {
        mDelayDownTmp = flag;
        mDelayNotifyDis = false;
    }
    
    
    public static void resetDelayDownFlag() {
        mDelayNotifyDis = true;
    }
    
    
    public static boolean isDelayDownloadNow() {
        if (!mDelayNotify) {
            return mDelayDownload;
        } else {
            return mDelayDownTmp;
        }
    }
}
