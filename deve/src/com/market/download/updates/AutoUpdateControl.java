package com.market.download.updates;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.market.download.common.DownloadSettings;
import com.market.download.common.RunTask;
import com.market.download.common.TaskThread;
import com.market.download.userDownload.DownStorage;
import com.market.download.userDownload.DownloadPool;
import com.market.download.util.NetworkType;
import com.market.download.util.Util;
import com.market.updateSelf.UpdateManager;
import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;

public class AutoUpdateControl {
    private static final String TAG = "AutoUpdateControl";
    
    private Context mContext;
    
    private HashMap<String, AutoUpdateEventInfo> mAppUpdateMap;
    private ArrayList<String> mAppUpdateArray;
    private AppUpdateManager mAppUpManager;
    private AutoUpdateStorage mStorage;
    private DownloadPool mDownloadPool;
    
    private TaskThread mUpdateTaskThread;
    private ConcurrentHashMap<String, AutoUpdateTask> mUpdateTaskMap;
    
    private AutoUpTaskCb mTaskCallback;
    private AutoUpThreadCb mThreadCallback;
    
    private boolean isUpdateDownloading = false;

    private final Object UPDATE_THREAD_SYNC_KEY = new Object();
    
    public AutoUpdateControl(Context context, DownloadPool downPool) {
        mContext = context;
        mDownloadPool = downPool;
        
        mAppUpdateArray = new ArrayList<String>();
        mAppUpdateMap = new HashMap<String, AutoUpdateEventInfo>();
        
        mUpdateTaskMap = new ConcurrentHashMap<String, AutoUpdateTask>();
        
        mAppUpManager = AppUpdateManager.getInstance(mContext);
        
        mTaskCallback = new AutoUpTaskCb();
        mThreadCallback = new AutoUpThreadCb();
        
        mStorage = new AutoUpdateStorage(context);
        
        initAutoUpdateArray();
    }
    
    
    private boolean isContainInArrayList(ArrayList<String> arrayList, String pkgName, int verCode) {
        boolean isContain = false;
        synchronized (arrayList) {
            String name = null;
            String eventSignal = null;
            for (int i = 0; i < arrayList.size(); i++) {
                name = arrayList.get(i);
                eventSignal = DownStorage.getEventSignal(pkgName, verCode);
                if (name.equals(eventSignal)) {
                    isContain = true;
                }
            }
        }

        return isContain;
    }
    
    
    private void initAutoUpdateArray() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.UPDATE_APP_AUTO, Context.MODE_PRIVATE);
        Map<String, ?> allMap = sp.getAll();
        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            String eventSignal = null;
            AutoUpdateEventInfo eventInfo = null;
            File apkFile = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                eventSignal = (String) entry.getKey();
                try {
                    eventInfo = AutoUpdateEventInfo.parserSaveString(infoStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    iter.remove();
                    continue;
                }
                if (eventInfo.getUpdateFlag() != AutoUpdateEventInfo.UP_FLAG_CLOSE) {
                    mAppUpdateArray.add(0, eventSignal);
                } else {
                    mAppUpdateArray.add(eventSignal);
                }
                mAppUpdateMap.put(eventSignal, eventInfo);
            }
        }
    }
    
    
    public void clearStorage() {
        mStorage.clearStorage();
    }
    
    
    public void addAppUpdateEvent(AutoUpdateEventInfo eventInfo) {
        String pkgName = eventInfo.getPkgName();

        int verCode = eventInfo.getVersionCode();
        String eventSignal = DownStorage.getEventSignal(pkgName, eventInfo.getVersionCode());
        if (!isContainInArrayList(mAppUpdateArray, pkgName, verCode)) {
            if (eventInfo.getUpdateFlag() != AutoUpdateEventInfo.UP_FLAG_CLOSE) {
                mAppUpdateArray.add(0, eventSignal);
            } else {
                mAppUpdateArray.add(eventSignal);
            }
            mAppUpdateMap.put(eventSignal, eventInfo);
            mStorage.saveAutoUpdateEventInfo(eventInfo);
        }

    }

//    private void addToUpdateListEnd(AutoUpdateEventInfo eventInfo) {
//        String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
//        mAppUpdateArray.add(eventSignal);
//        mAppUpdateMap.put(eventSignal, eventInfo);
//        mStorage.saveAutoUpdateEventInfo(eventInfo);
//    }
    

    public void removeFromAppUpdateList(String pkgName, int verCode) {
        synchronized (mAppUpdateArray) {
            String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
            String name = null;
            for (int i = 0; i < mAppUpdateArray.size(); i++) {
                name = mAppUpdateArray.get(i);
                if (name.equals(eventSignal)) {
                    mAppUpdateArray.remove(i);
                }
            }
            mAppUpdateMap.remove(eventSignal);
            mStorage.removeAutoUpdateEventInfo(eventSignal);
        }
    }
    
    
    public void pauseAutoUpdates() {
        mUpdateTaskMap.clear();
        if (mUpdateTaskThread != null) {
            AutoUpdateTask task = (AutoUpdateTask)mUpdateTaskThread.getCurrRunTask();
            if (task != null) {
                task.invalidateTask();
            }
            mUpdateTaskThread.stopThread();
        }
    }
    
    
    public boolean startAutoUpdates() {
        if (!NetworkType.isWifiAvailable(mContext)) {
            Util.log(TAG, "startAutoUpdates", "is not wifi connect now");
            return false;
            
        } else if (mAppUpdateArray == null || mAppUpdateArray.size() <= 0) {
            Util.log(TAG, "startAutoUpdates", "mAppUpdateArray is empty");
            return false;
            
        } else if (!Util.isBatteryStatusOKey(mContext)) {
            return false;
        }
        
        final boolean userUpdateFlag = DownloadSettings.getUserUpdateAutoFlag(mContext);
        int serverUpdateFlag = DownloadSettings.getUpdateAutoFlag(mContext);
        if(!userUpdateFlag && serverUpdateFlag == 0) {
            Util.log(TAG, "startAutoUpdates", "update flag is off");
            return false;
        }
        
        synchronized(UPDATE_THREAD_SYNC_KEY) {
            if (DownloadPool.getCurrDownloadingNum() > 0 || isUpdateDownloading) {
                Util.log(TAG, "startAutoUpdates", "foreground downloading, do not start background download");
                return false;
            }else {
                isUpdateDownloading = true;
            }
        }
        
        boolean startFlag = false;
        
        Util.log(TAG, "startAutoUpdates", "mAppUpdateArray size : " + mAppUpdateArray.size());
        for (int i=0; i < mAppUpdateArray.size(); i++) {
            String eventSig = mAppUpdateArray.get(i);
            AutoUpdateEventInfo eventInfo = mAppUpdateMap.get(eventSig);
            if (eventInfo == null) {
                mAppUpdateArray.remove(i);
                i--;
                continue;
            }
            
            PackageInfo pkgInfo = Util.getPackageInfo(mContext, eventInfo.getPkgName());
            if (pkgInfo == null || pkgInfo.versionCode >= eventInfo.getVersionCode()) {
                mAppUpdateArray.remove(i);
                mAppUpdateMap.remove(eventSig);
                mStorage.removeAutoUpdateEventInfo(eventSig);
                i--;
                continue;
            }
            
            if (AppUpdateManager.containsIgnoreApp(mContext, eventInfo.getPkgName())) {
                continue;
            }
            
            int updateFlag = eventInfo.getUpdateFlag();
            if (!userUpdateFlag && updateFlag == AutoUpdateEventInfo.UP_FLAG_CLOSE) {
                continue;
            }
            
//            if (mUpdateTaskMap.size() >= 3) {
//                break;
//            }
            
            if (!mUpdateTaskMap.contains(eventInfo.getPkgName())) {
                Util.log(TAG, "startAutoUpdates", "add one task: " + eventInfo.getPkgName());
                mUpdateTaskMap.put(eventInfo.getPkgName(), new AutoUpdateTask(mContext, eventInfo, mTaskCallback));
                startFlag = true;
            }
        }
        
        if (mUpdateTaskMap.size() > 0) {
            if (mUpdateTaskThread == null || !mUpdateTaskThread.isThreadAlive()) {
                mUpdateTaskThread = new TaskThread(mThreadCallback);
                mUpdateTaskThread.start();
            } else {
                isUpdateDownloading = false;
            }
        } else {
            isUpdateDownloading = false;
        }
        
        return startFlag;
    }
    
    
    private class AutoUpTaskCb implements AutoUpdateTask.TaskCallback {

        @Override
        public void removeAutoUpdateInfo(AutoUpdateEventInfo info) {
            removeFromAppUpdateList(info.getPkgName(), info.getVersionCode());
        }


        @Override
        public void installFile(AutoUpdateEventInfo eventInfo) {
            UpdateAppDisplayInfo updateDisInfo = mAppUpManager.getUpdateAppInfo(eventInfo.getPkgName());
            if (updateDisInfo == null) {
                return;
            }
            
            mDownloadPool.addDataAfterAutoUpdate(eventInfo);
            
            mAppUpManager.syncInfoAfterAutoDownload(updateDisInfo);
            
            final boolean userUpdateFlag = DownloadSettings.getUserUpdateAutoFlag(mContext);
            if (!userUpdateFlag && eventInfo.getUpdateFlag() == AutoUpdateEventInfo.UP_FLAG_INSTALL) {
                if(!AppUpdateManager.containsIgnoreApp(mContext, eventInfo.getPkgName())) {     //应用在忽略列表中，不安装应用
                    installApk(eventInfo);
                }
            }
            
            removeFromAppUpdateList(eventInfo.getPkgName(), eventInfo.getVersionCode());
        }
        
    };
    
    
    private class AutoUpThreadCb implements TaskThread.ThreadCallback {
        @Override
        public RunTask getTopRunTask() {
            Iterator iter = mUpdateTaskMap.entrySet().iterator();
            if (iter.hasNext() && Util.isBatteryStatusOKey(mContext)) {
                Map.Entry entry = (Map.Entry) iter.next();
                AutoUpdateTask task = (AutoUpdateTask)entry.getValue();
                return task;
            } else {
                return null;
            }
        }

        @Override
        public void removeTopRunTask() {
            Iterator iter = mUpdateTaskMap.entrySet().iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }

        @Override
        public void threadFinished(TaskThread downThread) {
            isUpdateDownloading = false;
//            mUpdateTaskThread = null;
        }

        @Override
        public void watchDog(TaskThread downThread) {
            // TODO Auto-generated method stub
            
        }
        
    };
    
    
    private void installApk(final AutoUpdateEventInfo eventInfo) {
        //自更新安装
        if (mContext.getPackageName().equals(eventInfo.getPkgName())) {
            UpdateManager.installSelfUpdateApk(mContext);
            return;
        }
        
        //应用更新安装
        AppUpdateManager.installApk(eventInfo.getApkFile(), mContext, eventInfo, false, "download");
    }
    
    
//    private void reportInstallResult(final AutoUpdateEventInfo eventInfo) {
//        ReportManager rm = ReportManager.getInstance(mContext);
//        rm.reportInstallResult(eventInfo.getDownloadFlag(),
//                               eventInfo.getTopicId(),
//                               eventInfo.getAppName(),
//                               eventInfo.getPkgName(),
//                               Integer.toString(eventInfo.getAppId()),
//                               eventInfo.getVersionCode());
//    }
}
