package com.market.download.userDownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.market.download.common.DownBaseInfo;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;

public class DataHolder {
    private static final String TAG = "DataHolder";
    
    private static DataHolder mSelf = null;
    
    public static DataHolder getHolder(Context context) {
        if (mSelf == null) {
            mSelf = new DataHolder(context);
        }
        return mSelf;
    }
    
    
    private ArrayList<String> mAvailableArray;
    private ArrayList<String> mPausedArray;
    
    private HashMap<String, DownloadEventInfo> mThirdDownEventMap;
    
    private ConcurrentHashMap<String, DownloadEventInfo> mAllDownloadEventMap;
    
    private ListenerManager mListenManager;

    private DownStorage mDownStorage;
    
    private Context mContext;
    
    DataHolder(Context context) {
        mContext = context;
        mAllDownloadEventMap = new ConcurrentHashMap<String, DownloadEventInfo>();
        
        mAvailableArray = new ArrayList<String>();
        mPausedArray = new ArrayList<String>();

        mThirdDownEventMap = new HashMap<String, DownloadEventInfo>();
        
        mListenManager = ListenerManager.getInstance(context);
        
        mDownStorage = DownStorage.getInstance(context);
    }
    
    
    public DownloadEventInfo checkDownloadEvent(DownloadEventInfo eventInfo) {
        boolean refreshDownloadView = false;
        String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
        DownloadEventInfo downInfo = mAllDownloadEventMap.get(eventSignal);
        if (downInfo == null) {
            refreshDownloadView = true;
            downInfo = eventInfo;
            mAllDownloadEventMap.put(eventSignal, downInfo);
            
        } else {
            int state = downInfo.getCurrState();
            if (state >= DownBaseInfo.STATE_DOWNLOAD_COMPLETE) {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo pInfo = null;
                try {
                    pInfo = pm.getPackageInfo(downInfo.getPkgName(), 0);
                } catch (NameNotFoundException e) {
                    //no this application on mobile set
                }
                
                if (pInfo != null && pInfo.versionCode >= downInfo.getVersionCode()) {
                    //installed version is same or high when new one, return null;
                    return null;
                    
                } else {
                    if (!downInfo.getApkFile().exists()) {
                        readyToDownload(downInfo);
                    }
                }
            }
        }
        
        addToList(mAvailableArray, downInfo.getPkgName(), downInfo.getVersionCode());
        mListenManager.newDownloadEventAdded(eventInfo, refreshDownloadView);
        return downInfo;
    }
    
    
    private void downInfoChanged(DownloadEventInfo eventInfo) {
        synchronized (mDownStorage) {
            if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                Util.log(TAG, "eventInfoChanged", "package name:" + eventInfo.getPkgName()  + ", this event has been canceled, do not save");
                return;
            }
            
            mDownStorage.savaEventInfo(eventInfo);
        }
    }
    
    
    private boolean isExistInList(ArrayList<String> arrayList, String pkgName, int verCode) {
        boolean isContain = false;
        synchronized (arrayList) {
            String name = null;
            String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
            for (int i = 0; i < arrayList.size(); i++) {
                name = arrayList.get(i);
                if (name.equals(eventSignal)) {
                    isContain = true;
                }
            }
        }

        return isContain;
    }
    
    
    private void addToList(ArrayList<String> arrayList, String pkgName, int verCode) {
        synchronized (arrayList) {
            if (!isExistInList(arrayList, pkgName, verCode)) {
                String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
                arrayList.add(eventSignal);
            }
        }
    }
    
    
    private void removeFromList(ArrayList<String> arrayList, String pkgName, int verCode) {
        synchronized (arrayList) {
            String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
            String name = null;
            for (int i = 0; i < arrayList.size(); i++) {
                name = arrayList.get(i);
                if (name.equals(eventSignal)) {
                    arrayList.remove(i);
                    break;
                }
            }
        }
    }
    
    
    public DownloadEventInfo getEventInfo(String pkgName, int verCode) {
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        return getEventInfo(eventSignal);
    }

    
    private DownloadEventInfo getEventInfo(String eventSignal) {
        DownloadEventInfo eventInfo = mAllDownloadEventMap.get(eventSignal);
        if (eventInfo != null) {
            return eventInfo;
        }

        eventInfo = mDownStorage.getEventInfo(eventSignal);
        if (eventInfo != null) {
            if(eventInfo.getEventArray() != DownloadEventInfo.ARRAY_BACKGROUND) {
                mAllDownloadEventMap.put(DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode()), eventInfo);
                return eventInfo;
            }
        }

        return null;
    }


    private void removeEventInfo(String pkgName, int verCode) {
        Util.log(TAG, "removeSavedEventInfo", "package name:" + pkgName + "version code:" + verCode);
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        synchronized (mDownStorage) {
            mDownStorage.removeEventInfo(eventSignal);
        }
        DownloadEventInfo eventInfo = mAllDownloadEventMap.remove(eventSignal);
        if (eventInfo != null) {
            removeFromThirdDownloadMap(eventInfo);
        }
        removeFromList(mAvailableArray, pkgName, verCode);
        removeFromList(mPausedArray, pkgName, verCode);
        if (verCode <= 0) {
            mAllDownloadEventMap.remove(pkgName); // for old version sync
        }
        
        mListenManager.sendRefreshViewBroadcast();
    }
    
    
    /**
     * change download info state when it's waiting to download apk
     * @param info
     */
    public void readyToDownload(DownloadEventInfo info) {
        info.readyToDownload();
        downInfoChanged(info);
        
        removeFromList(mPausedArray, info.getPkgName(), info.getVersionCode());
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_APK_WAIT_DOWNLOAD);
    }
    
    
    /**
     * change download info state before apk file 
     * @param info
     */
    public void downloadStarted(DownloadEventInfo info) {
        info.downloading();
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_APK_DOWNLOADING);
    }
    
    
    /**
     * save download info and notify listener after total size got
     * @param info
     */
    public void totalSizeGot(DownloadEventInfo info) {
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_APK_WAIT_DOWNLOAD);
    }
    
    
    /**
     * notify listener download progress update
     * @param info
     */
    public void downloadProgressChange(DownloadEventInfo info) {
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE);
    }
    
    
    /**
     * change download info state if download apk failed
     * @param info
     * @param msgWhat   root cause
     */
    public void downloadFailed(DownloadEventInfo info, int msgWhat) {
        info.downloadFailed();
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, msgWhat);
    }
    
    
    /**
     * change download info state if user pause download
     * @param info
     */
    public void downloadPaused(DownloadEventInfo info) {
        info.downloadPause();
        downInfoChanged(info);
        
        removeFromList(mAvailableArray, info.getPkgName(), info.getVersionCode());
        addToList(mPausedArray, info.getPkgName(), info.getVersionCode());
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_APK_PAUSE);
    }
    
    
    /**
     * change download info state after download apk success
     * @param info
     * @return if apk file is usable, return true, unless return false
     */
    public boolean downloadComplete(DownloadEventInfo info) {
        info.downloadComplete(mContext);
        if (!Util.isApkFileUsable(mContext, info.getApkFile(), info.getVersionCode())) {
            info.getApkFile().delete();
            downloadFailed(info, DownloadMsg.MSG_FILE_NOT_USABLE);
            return false;
        }
        
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_DOWNLOAD_COMPLETE);
        removeFromList(mAvailableArray, info.getPkgName(), info.getVersionCode());
        return true;
    }
    
    
    /**
     * change download info state before start install apk
     * @param info
     */
    public void readyToInstall(DownloadEventInfo info) {
        info.installingApk();
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_INSTALLING);
    }
    
    
    /**
     * change download info state if silent install apk failed
     * @param info
     */
    public void installFailed(DownloadEventInfo info) {
        info.installFailed();
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_INSTALL_FAILED);
    }
    
    
    /**
     * change download info state if install apk success
     * @param info
     */
    public void installSuccess(DownloadEventInfo info) {
        info.installed();
        downInfoChanged(info);
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_INSTALLED);
    }
    
    
    /**
     * remove download info is user cancel
     * @param info
     */
    public void eventCancel(DownloadEventInfo info) {
        info.cancelEvent();
        removeEventInfo(info.getPkgName(), info.getVersionCode());
        
        mListenManager.downInfoChanged(info, DownloadMsg.MSG_APK_CANCEL);
    }
    
    
    
    
    
    private void syncToThirdDownloadMap(DownloadEventInfo eventInfo) {
        if (eventInfo.getDownloadFlag().equals(ReportFlag.FROM_THIRD_DOWNLOAD)) {
            DownloadEventInfo thirdDownEventInfo = mThirdDownEventMap.get(eventInfo.getDownloadUrl());
            if (thirdDownEventInfo == null) {
                mThirdDownEventMap.put(eventInfo.getDownloadUrl(), eventInfo);
            } else {
                eventInfo.setVersionCode(thirdDownEventInfo.getVersionCode());
            }
        }
    }
    
    
    private void removeFromThirdDownloadMap(DownloadEventInfo eventInfo) {
        mThirdDownEventMap.remove(eventInfo.getDownloadUrl());
    }
    
    
    public DownloadEventInfo getExistThirdDownloadInfo(DownloadEventInfo eventInfo) {
        if (eventInfo.getDownloadFlag().equals(ReportFlag.FROM_THIRD_DOWNLOAD)) {
            return mThirdDownEventMap.get(eventInfo.getDownloadUrl());
        }
        
        return null;
    }
}
