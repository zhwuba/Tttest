package com.market.download.userDownload;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.zhuoyi.market.R;
import com.market.download.common.DownBaseInfo;
import com.market.download.common.DownloadSettings;
import com.market.download.common.InstallControl;
import com.market.download.common.SilentInstallTask;
import com.market.download.service.DownloadService;
import com.market.download.updates.AppUpdateManager;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.util.NetworkType;
import com.market.download.util.NotifyUtil;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;
import com.market.statistics.ReportManager;
import com.market.updateSelf.UpdateManager;
import com.zhuoyi.market.appManage.download.DownloadView;
import com.zhuoyi.market.appResident.SettingData;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class DownloadPool {
    public static final String TAG = "DownloadPool";

    private static final long PASS_TIME_AFTER_BOOT = 10 * 60 * 1000;
    
    private ArrayList<String> mAvailableArray;
    private ArrayList<String> mWaitingArray;
    private ArrayList<String> mPausedArray;

    private DownStorage mDownStorage;

    private DownloadManager mDownManager;
    private Context mContext;
    private PackageManager mPkgManager;

    private static WeakReference<DownloadPool> mWeakSelf = null;
    
    public static WeakReference<DownloadPool> getWeakInstance() {
        return mWeakSelf;
    }
    
    
    public static int getDownAndWaitNum() {
        if (mWeakSelf == null || mWeakSelf.get() == null) {
            return 0;
        }
        DownloadPool downPool = mWeakSelf.get();
        int waitNum = downPool.getWaitDownNum();
        int downNum = 0;
        for (int i=0; i < mThreadArray.size(); i++) {
            DownThread downThread = mThreadArray.get(i);
            if (downThread.mCurrDownEventInfo == null || downThread.mCurrDownEventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                continue;
            }
            downNum++;
        }
        
        int num = downNum + waitNum;
        return num;
    }
    
    
    DownloadPool(Context context, DownloadManager downManager) {
        mDownManager = downManager;
        mContext = context;

        mPkgManager = (PackageManager) context.getPackageManager();

        mDownStorage = DownStorage.getInstance(mContext);
        
        mAvailableArray = new ArrayList<String>();
        mWaitingArray = new ArrayList<String>();
        mPausedArray = new ArrayList<String>();

        mThreadArray.clear();
        mThreadCheckTimeMap = new HashMap<Thread, Long>();
        
        initDownloadPool();
        if (NetworkType.isNetworkAvailable(mContext) && SystemClock.elapsedRealtime() > PASS_TIME_AFTER_BOOT) {
            autoContinueDownload();
        }
        mWeakSelf = new WeakReference<DownloadPool>(this);
    }

    public static int getCurrDownloadingNum() {
        return mThreadArray.size();
    }

    
    private void addToAvailableArray(DownloadEventInfo eventInfo) {
    	synchronized (mAvailableArray) {
    		if (!isContainInArrayList(mAvailableArray, eventInfo.getPkgName(), eventInfo.getVersionCode())) {
    			String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
    			mAvailableArray.add(eventSignal);
    		}
    	}
    }
    
    
    private void removeFromAvailableArray (DownloadEventInfo eventInfo) {
    	synchronized (mAvailableArray) {
    		String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
            String name = null;
            for (int i = 0; i < mAvailableArray.size(); i++) {
                name = mAvailableArray.get(i);
                if (name.equals(eventSignal)) {
                	mAvailableArray.remove(i);
                }
            }
    	}
    }
    

    private void addToDownloadingArray(DownloadEventInfo eventInfo) {
        addToAllDownloadMap(eventInfo, true);
    }
    

    private void addToWaitingArray(DownloadEventInfo eventInfo, boolean notify) {
        if (eventInfo.isOnlyDownInWifi() && !NetworkType.isWifiAvailable(mContext)) {
        	return;
        }
    	
    	DownThread thread = null;
    	for (int i=0; i < mThreadArray.size(); i++) {
    		thread = mThreadArray.get(i);
    		if(thread.mCurrDownEventInfo == eventInfo && thread.isThreadAlive()) {
    			return;
    		}
    	}
        eventInfo.readyToDownload();
        synchronized (mWaitingArray) {
            if (!isContainInArrayList(mWaitingArray, eventInfo.getPkgName(), eventInfo.getVersionCode())) {
                String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
                insertListWithSort(mWaitingArray, eventSignal);
            }
        }

        addToAllDownloadMap(eventInfo, notify);

        ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_WAIT_DOWNLOAD);
        mDownManager.notifyUpdateEventInfoChanged(eventInfo);
    }
    
    
    private void insertListWithSort(ArrayList<String> sortList, String insertInfo) {
        int sortSize = sortList.size();
        DownloadEventInfo insertEventInfo = mAllDownloadEventMap.get(insertInfo);
        DownloadEventInfo sortEventInfo = null;
        for (int i=0; i<sortSize; i++) {
            sortEventInfo = mAllDownloadEventMap.get(sortList.get(i));
            if (sortEventInfo.getSortTime() > insertEventInfo.getSortTime()) {
                sortList.add(i, insertInfo);
                return;
            }
        }

        sortList.add(insertInfo);
    }


    private void removeFromWaitingArray(DownloadEventInfo eventInfo) {
        synchronized (mWaitingArray) {
            String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
            String name = null;
            for (int i = 0; i < mWaitingArray.size(); i++) {
                name = mWaitingArray.get(i);
                if (name.equals(eventSignal)) {
                    mWaitingArray.remove(i);
                }
            }
        }
    }
    
    
    public int getWaitDownNum() {
        return mWaitingArray.size();
    }
    
    
    private DownloadEventInfo getNextDownloadEventInfo() {
        synchronized (mWaitingArray) {
            String eventSignal = null;
            DownloadEventInfo eventInfo = null;
            boolean isWifiNow = false;
            for (int i = 0; i < mWaitingArray.size(); i++) {
                eventSignal = mWaitingArray.get(i);
                eventInfo = mAllDownloadEventMap.get(eventSignal);
                if (eventInfo == null) {
                    mWaitingArray.remove(i);
                    i--;
                } else {
                    isWifiNow = NetworkType.isWifiAvailable(mContext);
                    if (!eventInfo.isOnlyDownInWifi() || isWifiNow) {
                        mWaitingArray.remove(i);
                        return eventInfo;
                    } else {
                    	eventInfo.downloadFailed();
                    	downloadEventInfoChanged(eventInfo);
                    	ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
                    	mWaitingArray.remove(i);
                    	i--;
                    }
                }
            }
            
            return null;
        }
    }
    

    private void addToPausedArray(DownloadEventInfo eventInfo) {
    	removeFromAvailableArray(eventInfo);
        synchronized (mPausedArray) {
            if (!isContainInArrayList(mPausedArray, eventInfo.getPkgName(), eventInfo.getVersionCode())) {
                String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
                mPausedArray.add(eventSignal);
            }
        }

        addToAllDownloadMap(eventInfo, true);
    }

    private void removeFromPausedArray(DownloadEventInfo eventInfo) {
        synchronized (mPausedArray) {
            String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
            String name = null;
            for (int i = 0; i < mPausedArray.size(); i++) {
                name = mPausedArray.get(i);
                if (name.equals(eventSignal)) {
                    mPausedArray.remove(i);
                }
            }
        }
    }

    public void addToCompleteArray(DownloadEventInfo eventInfo) {
    	removeFromAvailableArray(eventInfo);

        addToAllDownloadMap(eventInfo, true);
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

    public static class DownloadRes {
        public static final int HTTP_ERROR = 0;
        public static final int DOWNLOAD_COMPLETE = 1;
        public static final int SDCARD_LOST = 2;
        public static final int NO_ENOUGH_SPACE = 3;
        public static final int USER_PAUSE = 4;
        public static final int USER_CANCEL = 5;
        public static final int FILE_NOT_FOUND = 6;
        public static final int THREAD_INTERRUPT = 7;
    }

    public void pauseDownloadEvent(String pkgName, int verCode) {
        DownloadEventInfo eventInfo = getEventInfo(pkgName, verCode);

        if (eventInfo == null) {
            return;
        }
        if (eventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOADING) {
            eventInfo.downloadPause();
        } else {
            eventInfo.downloadPause();
            removeFromWaitingArray(eventInfo);
            addToPausedArray(eventInfo);
            downloadEventInfoChanged(eventInfo);
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_PAUSE);
        }
    }

    public void addDownloadEvent(DownloadEventInfo eventInfo) {
    	syncToThirdDownloadMap(eventInfo);
    	
        String pkgName = eventInfo.getPkgName();
        int verCode = eventInfo.getVersionCode();
        String infoSignal = DownStorage.getEventSignal(pkgName, verCode);
        if (!mAllDownloadEventMap.containsKey(infoSignal)) {
            mAllDownloadEventMap.put(DownStorage.getEventSignal(pkgName, verCode), eventInfo);
        } else {
            eventInfo = mAllDownloadEventMap.get(infoSignal);
            eventInfo.setOnlyDownInWifi(NetworkType.isWifiAvailable(mContext));
        }
        
        final File apkFile = eventInfo.getApkFile();
        if (apkFile.exists()) {
        	if (Util.isApkFileUsable(mContext, apkFile, verCode)) {
	            eventInfo.downloadComplete(mContext);
	            addToCompleteArray(eventInfo);
	            downloadEventInfoChanged(eventInfo);
	            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_COMPLETE);
	            installApk(eventInfo, true);
	            return;
        	} else {
        	    apkFile.delete();
        	}
        }
        
        addToAvailableArray(eventInfo);
        addToWaitingArray(eventInfo, true);
        removeFromPausedArray(eventInfo);

        downloadEventInfoChanged(eventInfo);
        startDownloadApk(false, true);
    }

    
    public boolean isExistEvent(String pkgName, int verCode) {
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        DownloadEventInfo eventInfo = mAllDownloadEventMap.get(eventSignal);
        if (eventInfo != null) {
            int eventArray = eventInfo.getEventArray();
            if (eventArray == DownloadEventInfo.ARRAY_COMPLETE) {
                File downFile = eventInfo.getApkFile();
                if (downFile.exists()) {
                    return true;
                } else {
                    if (eventInfo.isRenameingTmpFile()) {
                        return true;
                    }
                    removeSavedEventInfo(pkgName, verCode);
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    
    public void networkDisconnect() {
    	DownloadEventInfo eventInfo = null;
    	if (mThreadArray.size() == 0) {
    		String eventSignal = null;
    		synchronized (mWaitingArray) {
	            while (mWaitingArray.size() > 0) {
	            	eventSignal = mWaitingArray.get(0);
	            	eventInfo = mAllDownloadEventMap.get(eventSignal);
	            	if (eventInfo != null) {
	    	        	eventInfo.networkDisconnect();
	    	        	downloadEventInfoChanged(eventInfo);
	    	        	ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
	            	}
	            	mWaitingArray.remove(0);
	            }
    		}
    		return;				//no download thread now
    	}
    	
    	DownloadService.cancelCheckDownThreadMsg();
        
        DownThread downThread = null;
        for (int i=0; i < mThreadArray.size(); i++) {
        	downThread = mThreadArray.get(i);
        	eventInfo = downThread.mCurrDownEventInfo;
        	if(eventInfo != null) {
        		eventInfo.networkDisconnect();
        		downloadEventInfoChanged(eventInfo);
                ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
        		downThread.interrupt();
        	}
        }
        
        synchronized (mWaitingArray) {
            String eventSignal = null;
            while (mWaitingArray.size() > 0) {
            	eventSignal = mWaitingArray.get(0);
            	eventInfo = mAllDownloadEventMap.get(eventSignal);
            	if (eventInfo != null) {
    	        	eventInfo.networkDisconnect();
    	        	downloadEventInfoChanged(eventInfo);
    	        	ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
            	}
            	mWaitingArray.remove(0);
            }
        }
        
        mThreadArray.clear();
        mThreadCheckTimeMap.clear();
        
    }

    public void autoContinueDownload() {
    	String eventSignal = null;
    	DownloadEventInfo eventInfo = null;
    	boolean isOnlyDownloadInWifi = true;
    	synchronized (mAvailableArray) {
	    	for (int i=0; i < mAvailableArray.size(); i++) {
	    		eventSignal = mAvailableArray.get(i);
	    		eventInfo = mAllDownloadEventMap.get(eventSignal);
	    		if (eventInfo != null) {
	    		    if (isOnlyDownloadInWifi) {
	    		        isOnlyDownloadInWifi = eventInfo.isOnlyDownInWifi();
	    		    }
	    			addToWaitingArray(eventInfo, false);
	    		} else {
	    			mAvailableArray.remove(i);
	    			i--;
	    		}
	    	}
    	}
    	
    	boolean curNetWifi = NetworkType.isWifiAvailable(mContext);
    	boolean needNotify = true;
    	if (!curNetWifi && isOnlyDownloadInWifi) {
    	    needNotify = false;  
    	}
    	startDownloadApk(true, needNotify);
    }

    public void cancelEvent(DownloadEventInfo eventInfo, boolean delFile) {
        String pkgName = eventInfo.getPkgName();
        int verCode = eventInfo.getVersionCode();
        removeFromAvailableArray(eventInfo);
        removeFromWaitingArray(eventInfo);
        removeFromPausedArray(eventInfo);
        removeSavedEventInfo(pkgName, verCode);
        
        File file = new File(eventInfo.getDownloadFilePath());
        file.delete();
        
        if (delFile) {
            file = eventInfo.getApkFile();
            file.delete();
        }
        
        ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_CANCEL);
    }

    private int downloadApk(DownloadEventInfo eventInfo) {
        int downRes = -1;
        if (eventInfo.getApkFile().exists()) {
            downRes = DownloadRes.DOWNLOAD_COMPLETE;
            
        } else {
        	if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
        		return DownloadRes.USER_CANCEL;
        		
        	} else if (eventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
        		return DownloadRes.USER_PAUSE;
        		
        	}
        	
            addToDownloadingArray(eventInfo);
            eventInfo.downloading();
            downloadEventInfoChanged(eventInfo);
            
            downRes = mDownManager.downloadApk(eventInfo);

            if (eventInfo.getCurrState() == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
                Util.log(TAG, "downloadApk", "network disconnect, will stop download thread");
                eventInfo.downloadFailed();
                downloadEventInfoChanged(eventInfo);
                ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
                return -1;
            } else if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                downRes = DownloadRes.USER_CANCEL;
            }
        }

        switch (downRes) {
        case DownloadRes.HTTP_ERROR:
        case DownloadRes.THREAD_INTERRUPT:
            eventInfo.downloadFailed();
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
            break;

        case DownloadRes.DOWNLOAD_COMPLETE:
            if (eventInfo.getVersionCode() <= 0) {
                removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
                removeFromAvailableArray(eventInfo);
                eventInfo.downloadComplete(mContext);
                syncToThirdDownloadMap(eventInfo);
                mAllDownloadEventMap.put(DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode()), eventInfo);
                
            } else {
                eventInfo.downloadComplete(mContext);
            }
            downloadEventInfoChanged(eventInfo);
            if (isApkFileUsable(eventInfo)) {
                addToCompleteArray(eventInfo);
                ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_COMPLETE);
                // downloadEventInfoChanged(eventInfo);
                installApk(eventInfo, true);
                reportDownloadResult(eventInfo);
            } else {
                eventInfo.downloadFailed();
                downloadEventInfoChanged(eventInfo);
                Intent notiIntent = new Intent("com.zhuoyi.market.download.fileNotUsable");
                notiIntent.putExtra("appName", eventInfo.getAppName());
                mContext.sendBroadcast(notiIntent);
                ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_FILE_NOT_USABLE);
            }
            break;

        case DownloadRes.NO_ENOUGH_SPACE:
            eventInfo.downloadFailed();
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_NO_ENOUGH_SPACE);
            break;

        case DownloadRes.SDCARD_LOST:
            eventInfo.downloadFailed();
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_SDCARD_LOST);
            break;

        case DownloadRes.USER_CANCEL:
            File file = new File(eventInfo.getDownloadFilePath());
            file.delete();
            file = eventInfo.getApkFile();
            file.delete();
            removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_CANCEL);
            break;

        case DownloadRes.USER_PAUSE:
            addToPausedArray(eventInfo);
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_PAUSE);
            break;

        case DownloadRes.FILE_NOT_FOUND:
            eventInfo.downloadPause();
            removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
            fileNotFoundInServer(eventInfo);
            break;
        }
        if (downRes != DownloadRes.USER_CANCEL  && downRes != DownloadRes.FILE_NOT_FOUND) {
            downloadEventInfoChanged(eventInfo);
        }

        return downRes;
    }

    private boolean isApkFileUsable(DownloadEventInfo eventInfo) {
        File apkFile = eventInfo.getApkFile();
        PackageInfo pkgInfo = mPkgManager.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
        if (pkgInfo == null) {
            apkFile.delete();
            return false;
        }

        if (pkgInfo.versionCode == eventInfo.getVersionCode()) {
            return true;
        } else {
            apkFile.delete();
            return false;
        }
    }

    private void fileNotFoundInServer(DownloadEventInfo eventInfo) {
    	ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_FILE_NOT_FOUND);

    	if (eventInfo.getDownloadFlag().equals(ReportFlag.FROM_CLOUD_DOWN)) {
    	    //cloud download event don't notify file not found
    	    return;
    	}
        Intent intent = new Intent("com.zhuoyi.market.download.fileNotFoundInServer");
        intent.putExtra("appName", eventInfo.getAppName());
        mContext.sendBroadcast(intent);
    }

    private Object DOWNLOADING_KEY = new Object();

    private boolean isDownloading = false;
    
    private boolean isStartingDownloadThread = false;
    
    private void startDownloadApk(boolean autoDownload, boolean needNotify) {
    	if (isStartingDownloadThread || mThreadArray.size() >= SettingData.mDownloadMaxNum) {
    		return;
    	}
    	isStartingDownloadThread = true;
    	
    	DownloadManager.startServicePauseBackgroundDownload(mContext);
        
        synchronized (DOWNLOADING_KEY) {
        	int waitingNum = mWaitingArray.size();
        	
            while (mThreadArray.size() < SettingData.mDownloadMaxNum && waitingNum > 0) {
            	isDownloading = true;
                startDownloadThread();
                waitingNum--;
            }
        }

        isStartingDownloadThread = false;
        if (mThreadArray.size() > 0) {
        	checkDownloadThread();
        }
        notifyDownloading(needNotify, autoDownload);
    }
    
    
    private void startDownloadThread() {
    	DownThread downThread = new DownThread();
        mThreadArray.add(downThread);
        mThreadCheckTimeMap.put(downThread, System.currentTimeMillis());
        
        downThread.start();
    }
    
    
    public class DownThread extends Thread {
    	public DownloadEventInfo mCurrDownEventInfo = null;
    	
    	private boolean isStarted = false;
    	
    	private boolean isPatchDiffNow = false;
    	
    	public boolean isPatchDiffNow() {
    		return isPatchDiffNow;
    	}
    	
    	public void startPatchDiff() {
    		isPatchDiffNow = true;
    	}
    	
    	public void endPatchDiff() {
    		isPatchDiffNow = false;
    	}
    	
    	public boolean isThreadAlive() {
    		if (!isStarted || this.isAlive()) {
    			return true;
    		}
    		
    		return false;
    	}
    	
    	@Override
        public void run() {
    		isStarted = true;
    		
            downThreadWatchDog(this);

            DownloadEventInfo info = null;
            int httpRes = 0;
            while (mWaitingArray.size() > 0) {
                downThreadWatchDog(this);
                
                info = getNextDownloadEventInfo();
                if(info == null) {
                    break;
                }
	            notifyDownloading(true, false);
	            
                mCurrDownEventInfo = info;
                mThreadCheckTimeMap.put(this, System.currentTimeMillis());
                info.setThreadCheckInfo(DownloadPool.this, this);
                httpRes = downloadApk(info);
                if (httpRes == -1) {
                    synchronized (DOWNLOADING_KEY) {
                    	mThreadArray.remove(this);
                    	mThreadCheckTimeMap.remove(this);
                        if (mThreadArray.size() == 0) {
                        	notifyNetworkDisconnect();
                            //notifyDownloadComplete();
                        }
                    }
                    
                    return;
                } else if (httpRes == DownloadRes.SDCARD_LOST) {
                	NotifyUtil.notifySdcardLost(mContext);
                } else if (httpRes == DownloadRes.NO_ENOUGH_SPACE) {
                	NotifyUtil.notifyNoEnoughSpace(mContext);
                }
            }
            
            mCurrDownEventInfo = null;

            synchronized (DOWNLOADING_KEY) {
                mThreadArray.remove(this);
                mThreadCheckTimeMap.remove(this);
                if (mThreadArray.size() == 0) {
                    notifyDownloadComplete();
                }
            }
        }
    };

    
    private static ArrayList<DownThread> mThreadArray = new ArrayList<DownThread>();
    private HashMap<Thread, Long> mThreadCheckTimeMap;
    
    private static final long THREAD_CHECK_TIME = 40 * 1000;
    
    public void checkDownloadThread() {
    	boolean autoContinueFlag = false;
        for(int i=0; i < mThreadArray.size(); i++) {
            DownThread downThread = mThreadArray.get(i);
            if (downThread.isPatchDiffNow()) {
            	continue;
            }
            long lastCheckTime = mThreadCheckTimeMap.get(downThread);
            if(lastCheckTime > 0 && (System.currentTimeMillis() - lastCheckTime) >= THREAD_CHECK_TIME) {
                downThread.interrupt();
                mThreadArray.remove(i);
                mThreadCheckTimeMap.remove(downThread);
                DownloadEventInfo eventInfo = downThread.mCurrDownEventInfo;
                if(eventInfo != null) {
                    eventInfo.downloadFailed();
                    autoContinueFlag = true;
					
                }
            }
        }
        
        if (autoContinueFlag) {
        	autoContinueDownload();
        }
        
        if(mThreadArray.size() == 0){
        	notifyDownloadComplete();
        }else{
            DownloadService.sendCheckDownThreadMsg();
        }
    }
    
    
    public void downThreadWatchDog(DownThread downThread) {
        mThreadCheckTimeMap.put(downThread, System.currentTimeMillis());
    }
    
    
    public void deleteInstalledApkFile(DownloadEventInfo eventInfo, boolean delFileOnly) {
        if (delFileOnly) {
            File apkFile = eventInfo.getApkFile();
            apkFile.delete();
        } else {
            File apkFile = eventInfo.getApkFile();
            apkFile.delete();
            removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
        }
    }


    private void reportDownloadResult(final DownloadEventInfo eventInfo) {
        // report to statistics server
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportDownloadResult(eventInfo.getDownloadFlag(),
                                eventInfo.getTopicId(),
                                eventInfo.getAppName(),
                                eventInfo.getPkgName(),
                                Integer.toString(eventInfo.getAppId()),
                                eventInfo.getVersionCode());
    }

    public void reportInstallResult(final DownloadEventInfo eventInfo) {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportInstallResult(eventInfo.getDownloadFlag(),
                                eventInfo.getTopicId(),
                                eventInfo.getAppName(),
                                eventInfo.getPkgName(),
                                Integer.toString(eventInfo.getAppId()),
                                eventInfo.getVersionCode());
        
    }
    

    private void installApk(final DownloadEventInfo eventInfo, final boolean installFg) {
        
        final File apkFile = eventInfo.getApkFile();
        if (apkFile == null || !apkFile.exists()) return;
        
        //自更新安装
        if (mContext.getPackageName().equals(eventInfo.getPkgName())) {
            UpdateManager.installSelfUpdateApk(mContext);
            return;
        }
        
        //应用更新安装
        boolean isUpdateEvent = Util.isAppExistInHandsetNow(mContext, eventInfo.getPkgName());
        if (isUpdateEvent) {
            AppUpdateManager.installApk(apkFile, mContext, eventInfo, installFg, "download");
            return;
        }
        
        //下载应用安装
        boolean bgInstall = DownloadSettings.getBgInstallFlag(mContext);
        if (bgInstall) {
            InstallControl instalControl = InstallControl.getControl();
            int installResult = instalControl.silentInstall(mContext,
                                                            apkFile,
                                                            new SilentInstallTask.InstallCallback() {
                @Override
                public void installSuccess() {
                    eventInfo.installed();
                    downloadEventInfoChanged(eventInfo);
                    ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALLED);
                    Util.log(TAG, "installApk", "notify ticker install success");
                    String  tickerStr = mContext.getString(R.string.down_noti_ticker_install_success, eventInfo.getAppName());
                    NotifyUtil.notifyTickerText(mContext, tickerStr);
                }

                @Override
                public void installFailed() {
                    eventInfo.installFailed();
                    downloadEventInfoChanged(eventInfo);
                    ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALL_FAILED);
                    if (installFg) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                        mContext.startActivity(intent);
                    }
                }

                @Override
                public void hasInstalledYet() {
                            
                }
            });
                    
            if (installResult == InstallControl.RESULT_NO_PERMISSION) {
                if (installFg) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    mContext.startActivity(intent);
                }
            } else if (installResult == InstallControl.RESULT_READY_TO_INSTALL) {
                eventInfo.installingApk();
                downloadEventInfoChanged(eventInfo);
                ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALLING);
                if (!installFg) {
                    Intent it = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
                    it.putExtra("package", eventInfo.getPkgName());
                    mContext.sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");
                }
            }
            
        } else if (installFg) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            mContext.startActivity(intent);
        }
    }

    public DownloadEventInfo getEventInfo(String pkgName, int verCode) {
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        return getEventInfo(eventSignal);
    }

    public DownloadEventInfo getEventInfo(String eventSignal) {
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

    public static DownloadEventInfo getEventInfo(Context context, String pkgName, int verCode) {
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        DownloadEventInfo eventInfo = mAllDownloadEventMap.get(eventSignal);
        if (eventInfo != null) {
            return eventInfo;
        }

        return DownStorage.getEventInfo(context, pkgName, verCode);
    }
	

    public void downloadEventInfoChanged(DownloadEventInfo eventInfo) {
        Util.log(TAG, "downloadEventInfoChanged", "event string:" + eventInfo.getEventString());
        // getCaller();
        if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_UPDATE) {
            return;
        }

        synchronized (mDownStorage) {
            if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                Util.log(TAG, "downloadEventInfoChanged", "package name:" + eventInfo.getPkgName()  + ", this event has been canceled, do not save");
                return;
            }
            
            mDownStorage.savaEventInfo(eventInfo);
        }
    }


    private void removeSavedEventInfo(String pkgName, int verCode) {
        Util.log(TAG, "removeSavedEventInfo", "package name:" + pkgName + "version code:" + verCode);
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        synchronized (mDownStorage) {
        	mDownStorage.removeEventInfo(eventSignal);
        }
        DownloadEventInfo eventInfo = mAllDownloadEventMap.remove(eventSignal);
        if (eventInfo != null) {
        	removeFromThirdDownloadMap(eventInfo);
        }
        if (verCode <= 0) {
            mAllDownloadEventMap.remove(pkgName); // for old version sync
        }
        
        notifyDownloading(true, false);
    }

    private HashMap<String, DownloadEventInfo> mThirdDownEventMap = new HashMap<String, DownloadEventInfo>();
    
    
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
    
    
    private static ConcurrentHashMap<String, DownloadEventInfo> mAllDownloadEventMap = new ConcurrentHashMap<String, DownloadEventInfo>();

    private static int mNewDownloadAppId = -1;
    private static String mNewDownloadPacName = null;
    private static Handler mDownloadViewHandler = null;

    public static void setDownloadViewHandler(Handler handler) {
        mDownloadViewHandler = handler;
    }

    private void addToAllDownloadMap(DownloadEventInfo eventInfo, boolean notify) {
        if (isInitializing) {
            return;
        }
        boolean refreshDownloadView = false;
        String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
        if (!mAllDownloadEventMap.containsKey(eventSignal)) {
            refreshDownloadView = true;
        }
        
        notifyDownloading(notify, false);

        if(eventInfo.getEventArray() != DownloadEventInfo.ARRAY_COMPLETE){
            mNewDownloadAppId = eventInfo.getAppId();
            mNewDownloadPacName = eventInfo.getPkgName();
        }
        if (refreshDownloadView && mDownloadViewHandler != null) {
            Message msg = new Message();
            msg.what = DownloadView.HANDLER_ADD_TO_DOWNLOAD;
            msg.obj = eventInfo;
            mDownloadViewHandler.sendMessage(msg);
        }
    }

    
    public static int getNewDownloadAppId() {
        return mNewDownloadAppId;
    }
    
    
    public static String getNewDownloadPacName() {
        return mNewDownloadPacName;
    }
    
    
    public static ConcurrentHashMap<String, DownloadEventInfo> getAllDownloadEvent(Context context) {
        return mAllDownloadEventMap;
    }
    
    
    public void addDataAfterAutoUpdate(DownloadEventInfo eventInfo) {
        mAllDownloadEventMap.put(DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode()), eventInfo);
        addToCompleteArray(eventInfo);
        downloadEventInfoChanged(eventInfo);
        ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_COMPLETE);
    }
    

    private boolean isInitializing = false;
    
    private void initDownloadPool() {
        isInitializing = true;
        ArrayList<DownloadEventInfo> allEventArray = mDownStorage.getAllDownloadEvent(mContext);
        DownloadEventInfo downInfo = null;
        int eventArray = 0;
        String eventSignal = null;
        PackageInfo pkgInfo = null;
        for (int i = 0; i < allEventArray.size(); i++) {
            downInfo = allEventArray.get(i);
            eventArray = downInfo.getEventArray();
            eventSignal = DownStorage.getEventSignal(downInfo.getPkgName(), downInfo.getVersionCode());
            pkgInfo = null;
            try {
                pkgInfo = mContext.getPackageManager().getPackageInfo(downInfo.getPkgName(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (pkgInfo != null) {
                if(pkgInfo.versionCode == downInfo.getVersionCode() && eventArray != DownloadEventInfo.ARRAY_COMPLETE) {
                    removeSavedEventInfo(downInfo.getPkgName(), downInfo.getVersionCode());
                    continue;
                }
            }
            
            if (eventArray == DownloadEventInfo.ARRAY_BACKGROUND) {
            	removeSavedEventInfo(downInfo.getPkgName(), downInfo.getVersionCode());
//            	
//                mBgDownloadArray.add(eventSignal);
//                mBgDownloadMap.put(eventSignal, downInfo);

            } else if (eventArray == DownloadEventInfo.ARRAY_PAUSED && downInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
                downInfo.downloadPause();
                downloadEventInfoChanged(downInfo);
                addToPausedArray(downInfo);
                syncToThirdDownloadMap(downInfo);
                mAllDownloadEventMap.put(eventSignal, downInfo);
            } else if (eventArray == DownloadEventInfo.ARRAY_COMPLETE) {
                addToCompleteArray(downInfo);
                syncToThirdDownloadMap(downInfo);
                mAllDownloadEventMap.put(eventSignal, downInfo);
            } else {
            	downInfo.downloadFailed();
            	addToAvailableArray(downInfo);
            	downloadEventInfoChanged(downInfo);
            	syncToThirdDownloadMap(downInfo);
                mAllDownloadEventMap.put(eventSignal, downInfo);
            }
        }
        isInitializing = false;
    }

    
    private boolean mNotifiedDownloading = false;
    
    private void notifyDownloading(boolean notify, boolean autoDownload) {
    	if (notify) {
    		boolean notifySuccess = NotifyUtil.notifyDownloading(mContext, getNotifyNum(), mNotifiedDownloading, autoDownload);
    		if (notifySuccess) {
    			mNotifiedDownloading = true;
    		}
        }
    }
    
    
    private int getNotifyNum(){
        if (mAvailableArray == null || mAvailableArray.size() <= 0 || mAllDownloadEventMap == null) return 0;
        int num = 0;
        DownloadEventInfo eventInfo = null;
        int totleSize = mAvailableArray.size();
        for (int i=0; i<totleSize; i++) {
            eventInfo = mAllDownloadEventMap.get(mAvailableArray.get(i));
            if (eventInfo != null && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_PAUSED) {
                num++;
            }
        }
        return num;
    }
    

    private void notifyNetworkDisconnect() {
    	if (!mNotifiedDownloading) {
    		return;
    	}
    	mNotifiedDownloading = false;
    	DownloadService.cancelCheckDownThreadMsg();
    	NotifyUtil.notifyNetworkDisconnect(mContext);
    }

    private void notifyDownloadComplete() {
    	if (!mNotifiedDownloading) {
    		return;
    	}
    	mNotifiedDownloading = false;
    	DownloadService.cancelCheckDownThreadMsg();
        DownloadEventInfo downInfo = null;
        synchronized (mWaitingArray) {
	        for (int i=0; i < mWaitingArray.size(); i++) {
	            downInfo = mAllDownloadEventMap.get(mWaitingArray.get(i));
	            if (downInfo != null && downInfo.getCurrState() == DownBaseInfo.STATE_READY) {
	                downInfo.downloadFailed();
	                downloadEventInfoChanged(downInfo);
	            }
	        }
	        
        	mWaitingArray.clear();
        }
        
        for (int i=0; i < mPausedArray.size(); i++) {
            downInfo = mAllDownloadEventMap.get(mPausedArray.get(i));
            if (downInfo == null) {
                mPausedArray.remove(i);
                i--;
            } else if (downInfo.getCurrState() != DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
            	downInfo.downloadPause();
            	downloadEventInfoChanged(downInfo);
            }
        }
        
        for (int i=0; i < mAvailableArray.size(); i++) {
            downInfo = mAllDownloadEventMap.get(mAvailableArray.get(i));
            if (downInfo == null) {
                mAvailableArray.remove(i);
                i--;
            }
        }
        
        int failedNum = mPausedArray.size() + mAvailableArray.size();
        
        if(isDownloading) {
            isDownloading = false;
        }else {
            return;
        }
        
        NotifyUtil.notifyDownloadComplete(mContext, failedNum);
    }

    
}
