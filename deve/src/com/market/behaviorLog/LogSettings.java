package com.market.behaviorLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.zhuoyi.market.constant.SharedPrefDefine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class LogSettings {
    private static final String TAG = "LogSettings";
    
    private static int FLAG_UPLOAD_WHEN_EXIT = 0x01;
    private static int FLAG_UPLOAD_IN_TIME = 0x02;
    
    private static int mLogSwitch = -1;
    private static UploadScheme mScheme = null;
    
    public static void setLogSwitch(Context context, int logSwitch) {
        mLogSwitch = logSwitch;
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("logSwitch", logSwitch);
        editor.commit();
    }
    
    
    public static boolean isLogSwitchOpen(Context context) {
        /*/
        return true;
        /*/
        if (mLogSwitch == -1) {
            SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG_SET, Context.MODE_WORLD_READABLE);
            mLogSwitch = sp.getInt("logSwitch", -1);
        }
        
        if (mLogSwitch == 1) {
            return true;
        } else {
            return false;
        }
        //*/
    }
    
    
    public static boolean isSchemeExist(Context context) {
        if (mScheme == null) {
            return false;
        }
        
        return true;
    }
    
    
    public static UploadScheme getUploadScheme(Context context) {
        if (mScheme == null) {
            SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG_SET, Context.MODE_PRIVATE);
            mScheme = new UploadScheme();
            mScheme.scheme = sp.getInt("scheme", 0);
            mScheme.freeTime = sp.getLong("freeTime", 0);
            mScheme.freeNet = sp.getInt("freeNet", 1);              //default wifi
            mScheme.freeDelay = sp.getInt("freeDelay", 30);         //default 30 minutes
            mScheme.maxSize = sp.getInt("maxSize", 50);             //default 50 KB
            mScheme.failNum = sp.getInt("failNum", 6);              //default 6 fail count
            mScheme.failedNum = sp.getInt("failedNum", 0);
        }
        
        return mScheme;
    }
    
    
    public static void saveUploadScheme(Context context,
                                        int scheme,
                                        long freeTime,
                                        int freeNet,
                                        int freeDelay,
                                        int maxSize,
                                        int failNum) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("scheme", scheme);
        editor.putLong("freeTime", freeTime);
        editor.putInt("freeNet", freeNet);
        editor.putInt("freeDelay", freeDelay);
        editor.putInt("maxSize", maxSize);
        editor.putInt("failNum", failNum);
        editor.commit();
        
        if (mScheme == null) {
            mScheme = new UploadScheme();
        }
        
        mScheme.scheme = scheme;
        mScheme.freeTime = freeTime;
        mScheme.freeNet = freeNet;
        mScheme.freeDelay = freeDelay;
        mScheme.maxSize = maxSize;
        mScheme.failNum = failNum;
        
        setAlarmToUpload(context, false);
    }
    
    
    public static class UploadScheme {
        public int scheme;
        public long freeTime;
        public int freeNet;
        public int freeDelay;
        public int maxSize;
        public int failNum;
        public int failedNum;
    }
    
    
    public static boolean isUploadSchemeGot(Context context) {
        UploadScheme upScheme = getUploadScheme(context);
        if (upScheme.scheme > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public static boolean isUploadWhenExit(Context context) {
        UploadScheme upScheme = getUploadScheme(context);
        if((upScheme.scheme & FLAG_UPLOAD_WHEN_EXIT) > 0 || upScheme.scheme == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public static boolean isUploadInTime(Context context) {
        UploadScheme upScheme = getUploadScheme(context);
        if((upScheme.scheme & FLAG_UPLOAD_IN_TIME) > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    public static boolean isUploadOnWifiConnect(Context context) {
        UploadScheme upScheme = getUploadScheme(context);
        if (upScheme.failedNum >= upScheme.failNum) {
            return true;
        }
        
        File zipFile = new File(LogStorage.getInstance(context).getLogZipFilePath());
        if (zipFile.length() >= (upScheme.maxSize * 1024)) {
            return true;
        }
        
        return false;
    }
    
    
    public static void setAlarmToUpload(Context context, boolean failDelay) {
        UploadScheme upScheme = getUploadScheme(context);
        if((upScheme.scheme & FLAG_UPLOAD_IN_TIME) > 0) {
            Calendar scheCal = Calendar.getInstance();
            if (failDelay) {
                scheCal.setTimeInMillis(upScheme.freeTime + (upScheme.freeDelay * 60 * 1000));
            } else {
                scheCal.setTimeInMillis(upScheme.freeTime);
            }
            
            Calendar alarmCal = Calendar.getInstance();
            alarmCal.set(Calendar.HOUR_OF_DAY, scheCal.get(Calendar.HOUR_OF_DAY));
            alarmCal.set(Calendar.MINUTE, scheCal.get(Calendar.MINUTE));
            alarmCal.set(Calendar.SECOND, scheCal.get(Calendar.SECOND));
            
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, LogService.class);
            intent.putExtra(LogService.EXTRA_MSG, LogService.MSG_ALARM_TO_UPLOAD);
            
            PendingIntent pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            
            long millis = alarmCal.getTimeInMillis() + 24 * 60 * 60 * 1000;
            //log code
//            Date date = new Date(millis);
//            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
//            LogUtil.log(TAG, "setAlarmToUpload", "alarm time:" + format.format(date));
            am.set(AlarmManager.RTC_WAKEUP, millis, pIntent);
        }
    }
    
    
    public static void uploadFailedCountAdd(Context context) {
        UploadScheme upScheme = getUploadScheme(context);
        upScheme.failedNum++;
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG_SET, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("failedNum", upScheme.failedNum);
        editor.commit();
    } 
}
