package com.zhuoyi.market.cleanTrash;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.market.download.util.Util;
import com.zhuoyi.market.constant.SharedPrefDefine;
import com.zhuoyi.market.manager.MarketNotificationManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.TextUtils;

public class TrashControl {
    private static final String TAG = "TrashControl";
    
    public static final long KB_BYTES = 1024;
    public static final long MB_BYTES = 1024 * 1024;
    public static final long GB_BYTES = 1024 * 1024 * 1024;
    
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;                     //24 hours
    private static final long CHECK_TRASH_INTERVAL_MILLIS = 4 * 60 * 60 * 1000;         //4 hours
    public static final long START_MARKET_CHECK_DELAY_MILLIS = 10 * 60 * 1000;          //10 minutes
    
    private static final String START_CHARS = "<";
    private static final String END_CHARS = ">";
    private static final String SPLIT_CHARS = "\\|";
    
    private Context mContext;
    private String mSdcardPath;
    
    private int mAvailProgress;
    
    private HashMap<String, SystemCache> mSystemCacheMap;
    private ArrayList<SystemCache> mSystemCacheList;
    private long mTotalSystemTrashSize = 0;
    private long mSelectedSysTrashSize = 0;
    
    private HashMap<String, AppCache> mTrashCacheInfoMap;
    private ArrayList<AppCache> mTrashCacheInfoList;
    private long mTotalTrashCacheSize = 0;
    private long mSelectedTrashCacheSize = 0;
    
    private HashMap<String, ApkTrash> mTrashApkDirMap;
    private ArrayList<ApkTrash> mTrashApkDirList;
    private long mTotalTrashApkSize = 0;
    private long mSelectedTrashApkSize = 0;
    
    private static TrashControl mSelf = null;
    
    private static final Object mSyncKey = new Object();
    
    public static TrashControl get(Context context) {
        if (mSelf == null) {
            mSelf = new TrashControl(context);
        }
        
        return mSelf;
    }
    
    
    private boolean isChecking = false;
    private WeakReference<Handler> mActivityHandler = null;
    
    TrashControl(Context context) {
        mContext = context;
        mSystemCacheMap = new HashMap<String, SystemCache>();
        mTrashCacheInfoMap = new HashMap<String, AppCache>();
        mTrashApkDirMap = new HashMap<String, ApkTrash>();
        
        mSystemCacheList = new ArrayList<SystemCache>();
        mTrashCacheInfoList = new ArrayList<AppCache>();
        mTrashApkDirList = new ArrayList<ApkTrash>();
        
        mSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    }
    
    
    private void initTrash() {
        mTotalSystemTrashSize = 0;
        mTotalTrashCacheSize = 0;
        mTotalTrashApkSize = 0;
        
        mSelectedSysTrashSize = 0;
        mSelectedTrashCacheSize = 0;
        mSelectedTrashApkSize = 0;
        
        mSystemCacheMap.clear();
        mTrashCacheInfoMap.clear();
        mTrashApkDirMap.clear();
        
        mSystemCacheList.clear();
        mTrashCacheInfoList.clear();
        mTrashApkDirList.clear();
    }
    
    
    public void setActivityHandler(Handler handler) {
        if (handler != null) {
            mActivityHandler = new WeakReference<Handler>(handler);
        } else {
            mActivityHandler = null;
        }
    }
    
    
    private void sendMsgToActivityHandler(int msg) {
        if (mActivityHandler != null && mActivityHandler.get() != null) {
            //send message to activity
            try {
                mActivityHandler.get().sendEmptyMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public boolean isActivityShowNow() {
        if (mActivityHandler != null && mActivityHandler.get() != null) {
            return true;
        }
        
        return false;
    }
    
    
    public boolean isCheckingTrashStatusNow() {
        return isChecking;
    }
    
    
    public String getDisplaySizeText(long size) {
        String sizeStr = null;
        if (size < KB_BYTES) {
//            float byteSize = ((float)(size * 100 / KB_BYTES)) / 100;
//            sizeStr = Float.toString(byteSize) + "KB";
            sizeStr = Long.toString(size) + "B";
        } else if (size < MB_BYTES) {
            long kbSize = size / KB_BYTES;
            sizeStr = Long.toString(kbSize) + "K";
        } else if (size < GB_BYTES) {
            long mbSize = size / MB_BYTES;
            sizeStr = Long.toString(mbSize) + "M";
        } else {
            float gbSize = ((float)(size * 100 / GB_BYTES)) / 100;
            sizeStr = Float.toString(gbSize) + "G";
        }
        
        return sizeStr;
    }
    
    
    public void deleteSelectTrashFile() {
        final long startMillis = System.currentTimeMillis();
        new Thread () {
            public void run() {
                synchronized (mSyncKey) {
                    for (SystemCache cache : mSystemCacheList) {
                        if (cache.selected) {
                            for (TrashCacheInfo info : cache.cacheList) {
                                deleteCacheDirFile(new File(info.cachePath));
                            }
                        }
                    }
                    
                    for (AppCache appCache : mTrashCacheInfoList) {
                        if (appCache.selected) {
                            for (TrashCacheInfo info : appCache.cacheList) {
                                deleteCacheDirFile(new File(info.cachePath));
                            }
                        }
                    }
                    
                    for (ApkTrash apkCache : mTrashApkDirList) {
                        if (apkCache.selected) {
                            for (TrashApkDirInfo info : apkCache.apkDirInfos) {
                                deleteApkInDir(new File(info.dirPath));
                            }
                        }
                    }
                    
                    initTrash();
                }
                long runMillis = System.currentTimeMillis() - startMillis;
                if (runMillis < 2000) {
                    try {
                        sleep(2000 - runMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                initTrash();
                sendMsgToActivityHandler(TrashActivity.MSG_TRASH_CLEAN_FINISH);
            }
        }.start();
    }
    
    
    public interface CheckTrashCallback {
        void checkTrashFinish();
    }
    
    
    public void checkTrashStatus(final long minCheckTime, final boolean notify, final CheckTrashCallback callback) {
        if (isChecking) {
            return;
        }
        
        if (notify && isActivityShowNow()) {
            //it's background check now, and activity is shown, do nothing but set the next alarm
            setNextCheckTrashAlarm(System.currentTimeMillis() + CHECK_TRASH_INTERVAL_MILLIS);
            return;
        }
        
        isChecking = true;
        final long startMillis = System.currentTimeMillis();
        new Thread() {
            public void run() {
                synchronized (mSyncKey) {
                    initTrash();
                    syncSystemTrash();
                    syncTrashCacheList();
                    syncApkDirs();
                }
                
                StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
//                long blockSize = (long) statfs.getBlockSize();
                long availBlockCount = (long) statfs.getAvailableBlocks();
                long allBlockCount = (long) statfs.getBlockCount();
                
                mAvailProgress = (int)(availBlockCount * 100 / allBlockCount);
                long totalTrashSize = getTotalTrashSize();
                Util.log(TAG, "checkTrashStatus", "sdcard available progress:" + mAvailProgress + ", total trash size:" + totalTrashSize);
                
                if (notify) {
                    boolean isNotified = false;
                    MarketNotificationManager mnm = MarketNotificationManager.get();
                    isNotified = mnm.notifyTrashClean(getTotalTrashSize(), mAvailProgress);
                    if (isNotified) {
                        long tomorrowZeroMillis = getTomorrowZeroMillis();
                        saveNotifiedTime(tomorrowZeroMillis);
                        setNextCheckTrashAlarm(tomorrowZeroMillis);
                    } else {
                        setNextCheckTrashAlarm(System.currentTimeMillis() + CHECK_TRASH_INTERVAL_MILLIS);
                    }
                }
                
                long runMillis = System.currentTimeMillis() - startMillis;
                
                if (runMillis < minCheckTime) {
                    try {
                        sleep(minCheckTime - runMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isChecking = false;
                sendMsgToActivityHandler(TrashActivity.MSG_CHECK_TRASH_FINISH);
                if (callback != null) {
                    callback.checkTrashFinish();
                }
            }
        }.start();
    }
    
    
    public int getSdcardAvailProgress() {
        return mAvailProgress;
    }
    
    
    public long getTotalTrashSize() {
        return mTotalSystemTrashSize + mTotalTrashCacheSize + mTotalTrashApkSize;
    }
    
    
    public long getSysTotalTrashSize() {
        return mTotalSystemTrashSize;
    }
    
    
    public long getTotalCacheSize() {
        return mTotalTrashCacheSize;
    }
    
    
    public long getTotalTrashApkSize() {
        return mTotalTrashApkSize;
    }
    
    
    public long getSelectedTrashSize() {
        return mSelectedSysTrashSize + mSelectedTrashCacheSize + mSelectedTrashApkSize;
    }
    
    
    public long getSelectedSysTrashSize() {
        return mSelectedSysTrashSize;
    }
    
    
    public long getSelectedTrashCacheSize() {
        return mSelectedTrashCacheSize;
    }
    
    
    public long getSelectedTrashApkSize() {
        return mSelectedTrashApkSize;
    }
    
    
    public void setNextCheckTrashAlarm(long alarmTime) {
        Intent startIntent = new Intent(mContext, TrashService.class);
        startIntent.setPackage(mContext.getPackageName());
        startIntent.putExtra(TrashService.EXTRA_FROM_KEY, TrashService.FROM_ALARM_TIME);
        if (android.os.Build.VERSION.SDK_INT >= 12) {
            startIntent.setFlags(32);
        }
        PendingIntent pIntent = PendingIntent.getService(mContext, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, alarmTime, pIntent);
    }
    
    
    public boolean isNeedNotifiedToday() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.TRASH_CLEAN, Context.MODE_PRIVATE);
        long notifiedTime = sp.getLong("NotifiedTime", 0);
        if (System.currentTimeMillis() > notifiedTime) {
            return true;
        } else {
            setNextCheckTrashAlarm(notifiedTime);
            return false;
        }
    }
    
    
    private void saveNotifiedTime(long millis) {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.TRASH_CLEAN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("NotifiedTime", millis);
        editor.commit();
    }
    
    
    private long getTomorrowZeroMillis() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() + ONE_DAY_MILLIS);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return cal.getTimeInMillis();
    }
    
    
    /*
     * start for system cache
     */
    private void syncSystemTrash() {
        try {
            InputStreamReader inputReader = new InputStreamReader(mContext.getAssets().open("systemTrash"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String lineStr = null;
            String appName = null;
            long cacheSize = 0;
            ArrayList<TrashCacheInfo> cacheInfoList = null;
            while((lineStr = bufReader.readLine()) != null) {
                if (lineStr.startsWith(START_CHARS)) {
                    appName = lineStr.substring(START_CHARS.length());
                    
                } else if (lineStr.startsWith(END_CHARS)) {
                    if (cacheInfoList != null && cacheInfoList.size() != 0) {
                        SystemCache sysCache = new SystemCache();
                        sysCache.cacheName = appName;
                        sysCache.cacheList = cacheInfoList;
                        sysCache.cacheSize = cacheSize;
                        sysCache.selected = true;       //default selected
                        
                        mSystemCacheMap.put(appName, sysCache);
                        mSystemCacheList.add(sysCache);
                        cacheInfoList = null;
                        cacheSize = 0;
                    }
                    
                } else if (!TextUtils.isEmpty(lineStr)) {
                    if (cacheInfoList == null) {
                        cacheInfoList = new ArrayList<TrashCacheInfo>();
                    }
                    String[] infoList = lineStr.split(SPLIT_CHARS);
                    TrashCacheInfo cacheInfo = new TrashCacheInfo();
                    cacheInfo.cachePath = mSdcardPath + infoList[0];
                    cacheInfo.description = infoList[1];
                    if (!TextUtils.isEmpty(cacheInfo.cachePath.trim())) {
                        File cacheFile = new File(cacheInfo.cachePath);
                        if (cacheFile.exists()) {
                            cacheInfo.cacheSize = getFileSize(cacheFile);
                            cacheInfoList.add(cacheInfo);
                            mTotalSystemTrashSize += cacheInfo.cacheSize;
                            cacheSize += cacheInfo.cacheSize;
                        }
                    }
                    
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSelectedSysTrashSize = mTotalSystemTrashSize;
    }
    
    
    public class SystemCache {
        public String cacheName;
        public ArrayList<TrashCacheInfo> cacheList;
        public long cacheSize;
        public boolean selected = true;
    }
    
    
    public void changeSystemCacheSelect() {
        if (mSelectedSysTrashSize == mTotalSystemTrashSize) {
            mSelectedSysTrashSize = 0;
            for (SystemCache cache : mSystemCacheList) {
                cache.selected = false;
            }
        } else {
            mSelectedSysTrashSize = mTotalSystemTrashSize;
            for (SystemCache cache : mSystemCacheList) {
                cache.selected = true;
            }
        }
        sendMsgToActivityHandler(TrashActivity.MSG_TRASH_SIZE_CHANGED);
    }
    
    
    /*
     * start for trash cache dirs
     */
    
    public class AppCache {
        public String appName;
        public String pkgName;
        public long cacheSize;
        public ArrayList<TrashCacheInfo> cacheList;
        public boolean selected = true;
    }
    
    class TrashCacheInfo {
        public String description;
        public String cachePath;
        public long cacheSize;
    }
    
    public void changeAllAppCacheSelect() {
        if (mSelectedTrashCacheSize == mTotalTrashCacheSize) {
            mSelectedTrashCacheSize = 0;
            for (AppCache cache : mTrashCacheInfoList) {
                cache.selected = false;
            }
        } else {
            mSelectedTrashCacheSize = mTotalTrashCacheSize;
            for (AppCache cache : mTrashCacheInfoList) {
                cache.selected = true;
            } 
        }
        
        sendMsgToActivityHandler(TrashActivity.MSG_TRASH_SIZE_CHANGED);
    }
    
    public void changeAppCacheSelect(String pkgName) {
        AppCache appCache = mTrashCacheInfoMap.get(pkgName);
        if (appCache != null) {
            if (appCache.selected) {
                mSelectedTrashCacheSize -= appCache.cacheSize;
                appCache.selected = false;
            } else {
                mSelectedTrashCacheSize += appCache.cacheSize;
                appCache.selected = true;
            }
            sendMsgToActivityHandler(TrashActivity.MSG_TRASH_SIZE_CHANGED);
        }
    }
    
    private void syncTrashCacheList() {
        try {
            InputStreamReader inputReader = new InputStreamReader(mContext.getAssets().open("trashCaches"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String lineStr = null;
            String appName = null;
            String pkgName = null;
            long cacheSize = 0;
            ArrayList<TrashCacheInfo> cacheInfoList = null;
            while((lineStr = bufReader.readLine()) != null) {
                if (lineStr.startsWith(START_CHARS)) {
                    String pkgAndName = lineStr.substring(START_CHARS.length());
                    String[] infoList = pkgAndName.split(SPLIT_CHARS);
                    pkgName = infoList[0];
                    appName = infoList[1];
                    
                } else if (lineStr.startsWith(END_CHARS)) {
                    if (cacheInfoList != null && cacheInfoList.size() != 0) {
                        AppCache appCache = new AppCache();
                        appCache.appName = appName;
                        appCache.pkgName = pkgName;
                        appCache.cacheList = cacheInfoList;
                        appCache.cacheSize = cacheSize;
                        appCache.selected = true;
                        
                        mTrashCacheInfoMap.put(pkgName, appCache);
                        mTrashCacheInfoList.add(appCache);
                        cacheInfoList = null;
                        cacheSize = 0;
                    }
                    
                } else if (!TextUtils.isEmpty(lineStr)) {
                    if (cacheInfoList == null) {
                        cacheInfoList = new ArrayList<TrashCacheInfo>();
                    }
                    String[] infoList = lineStr.split(SPLIT_CHARS);
                    TrashCacheInfo cacheInfo = new TrashCacheInfo();
                    cacheInfo.cachePath = mSdcardPath + infoList[0];
                    cacheInfo.description = infoList[1];
                    if (!TextUtils.isEmpty(cacheInfo.cachePath.trim())) {
                        File cacheFile = new File(cacheInfo.cachePath);
                        if (cacheFile.exists()) {
                            cacheInfo.cacheSize = getFileSize(cacheFile);
                            if (cacheInfo.cacheSize > 0) {
                                cacheInfoList.add(cacheInfo);
                                mTotalTrashCacheSize += cacheInfo.cacheSize;
                                cacheSize += cacheInfo.cacheSize;
                            }
                        }
                    }
                    
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSelectedTrashCacheSize = mTotalTrashCacheSize;
    }
    
    
    public ArrayList<AppCache> getAppCacheInfoList() {
        return mTrashCacheInfoList;
    }
    
    
    private long getFileSize(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSize(flist[i]);
            } else {
                size = size + flist[i].length();
            }
        }
        return size;
    }
    
    
    private void deleteCacheDirFile(File delFile) {
        if (delFile.isDirectory()) {
            File[] files = delFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    deleteCacheDirFile(file);
                }
            }
        }
        
        delFile.delete();
    }
    
    
    
    /*
     * start for trash apk file
     */
    public class ApkTrash {
        public String appName;
        public String pkgName;
        public long trashSize;
        public ArrayList<TrashApkDirInfo> apkDirInfos;
        public boolean selected;
    }
    
    
    class TrashApkDirInfo {
        public String dirPath;
        public long apksSize;
    }
    
    public void changeAllApkTrashSelect() {
        if (mSelectedTrashApkSize == mTotalTrashApkSize) {
            mSelectedTrashApkSize = 0;
            for (ApkTrash trash : mTrashApkDirList) {
                trash.selected = false;
            }
        } else {
            mSelectedTrashApkSize = mTotalTrashApkSize;
            for (ApkTrash trash : mTrashApkDirList) {
                trash.selected = true;
            } 
        }
        sendMsgToActivityHandler(TrashActivity.MSG_TRASH_SIZE_CHANGED);
    }
    
    public void changeApkTrashSelect(String pkgName) {
        ApkTrash trash = mTrashApkDirMap.get(pkgName);
        if (trash != null) {
            if (trash.selected) {
                mSelectedTrashApkSize -= trash.trashSize;
                trash.selected = false;
            } else {
                mSelectedTrashApkSize += trash.trashSize;
                trash.selected = true;
            }
            
            sendMsgToActivityHandler(TrashActivity.MSG_TRASH_SIZE_CHANGED);
        }
    }
    
    private void syncApkDirs() {
        try {
            InputStreamReader inputReader = new InputStreamReader(mContext.getAssets().open("trashApkDirs"));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String lineStr = null;
            String appName = null;
            String pkgName = null;
            long cacheSize = 0;
            ArrayList<TrashApkDirInfo> apkDirList = null;
            while((lineStr = bufReader.readLine()) != null) {
                if (lineStr.startsWith(START_CHARS)) {
                    String pkgAndName = lineStr.substring(START_CHARS.length());
                    String[] infoList = pkgAndName.split(SPLIT_CHARS);
                    pkgName = infoList[0];
                    appName = infoList[1];
                    
                } else if (lineStr.startsWith(END_CHARS)) {
                    if (apkDirList != null && apkDirList.size() != 0) {
                        ApkTrash apkTrash = new ApkTrash();
                        apkTrash.apkDirInfos = apkDirList;
                        apkTrash.appName = appName;
                        apkTrash.pkgName = pkgName;
                        apkTrash.trashSize = cacheSize;
                        apkTrash.selected = true;
                        
                        mTrashApkDirMap.put(pkgName, apkTrash);
                        mTrashApkDirList.add(apkTrash);
                        apkDirList = null;
                        cacheSize = 0;
                    }
                    
                } else if (!TextUtils.isEmpty(lineStr)) {
                    if (apkDirList == null) {
                        apkDirList = new ArrayList<TrashApkDirInfo>();
                    }
                    if (!TextUtils.isEmpty(lineStr.trim())) {
                        File apkDir = new File(mSdcardPath + lineStr);
                        if (apkDir.exists()) {
                            TrashApkDirInfo apkDirInfo = new TrashApkDirInfo();
                            apkDirInfo.dirPath = apkDir.getAbsolutePath();
                            apkDirInfo.apksSize = getApkSizeInDir(apkDir);
                            if (apkDirInfo.apksSize > 0) {
                                apkDirList.add(apkDirInfo);
                                mTotalTrashApkSize += apkDirInfo.apksSize;
                                cacheSize += apkDirInfo.apksSize;
                            }
                        }
                    }
                    
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSelectedTrashApkSize = mTotalTrashApkSize;
    }
    
    
    public ArrayList<ApkTrash> getApkDirsList() {
        return mTrashApkDirList;
    }
    
    
    private long getApkSizeInDir(File f) {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getApkSizeInDir(flist[i]);
            } else if (flist[i].getName().toLowerCase().endsWith(".apk")) {
                size = size + flist[i].length();
            }
        }
        return size;
    }
    
    
    private void deleteApkInDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File oneFile : files) {
                    deleteApkInDir(oneFile);
                }
            }
            
        } else if (file.getName().toLowerCase().endsWith(".apk")) {
            file.delete();
        }
    }
}
