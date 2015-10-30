package com.market.download.userDownload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.appResident.SettingData;
import com.zhuoyi.market.constant.SharedPrefDefine;
import com.market.download.common.DownBaseInfo;
import com.market.download.service.DownloadService;
import com.market.download.service.DownloadService.ImageByteInfo;
import com.market.download.updates.AppUpdateManager;
import com.market.download.updates.AutoUpdateControl;
import com.market.download.updates.AutoUpdateEventInfo;
import com.market.download.updates.UpdateAppDisplayInfo;
import com.market.download.util.NotifyUtil;
import com.market.download.util.Util;
import com.market.featureOption.FeatureOption;
import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

public class DownloadManager {
    public static final String TAG = "DownloadManager";

    public static final String EVENT_KEY_MD5 = "evnetKeyMd5";
    public static final String EVENT_KEY_PKGNAME = "eventKeyPkgName";
    public static final String EVENT_KEY_APPNAME = "eventKeyAppName";
    public static final String EVENT_KEY_URL = "eventKeyUrl";
    public static final String EXTRA_EVENT_KEY = "extraEventKey";
    public static final String EVENT_KEY_FROM = "eventKeyFrom";
    public static final String EVENT_KEY_TOPICID = "eventKeyTopicId";
    public static final String EVENT_KEY_IMAGEBYTES = "eventKeyImageBytes";
    public static final String EVENT_KEY_VERCODE = "eventKeyVerCode";
    public static final String EVENT_KEY_APPID = "eventKeyAppId";
    public static final String EVENT_KEY_REPORT_ACTION = "eventKeyReportAction";
    public static final String EVENT_KEY_TOTALSIZE = "eventKeyTotalSize";
    public static final String EVENT_KEY_RECEIVER_PKG = "eventKeyReceiverPkg";
    public static final String EVENT_KEY_RECEIVER_CLASS = "eventKeyReceiverClass";
    
    public static final int EVENT_UNDEFINIT = -1;
    public static final int EVENT_ADD_DOWNLOAD = 1;
    public static final int EVENT_STARTDOWNLOAD = 2;
    public static final int EVENT_PAUSEDOWNlOAD = 3;
    public static final int EVENT_CANCELDOWNLOAD = 4;
    public static final int EVENT_SCREEN_OFF = 5;
    public static final int EVENT_AUTO_CONTINUE = 6;
    public static final int EVENT_NETWORK_DISCONNECT = 7;
    public static final int EVENT_GET_WIFI_DOWN_ARRAY = 8;
    //public static final int EVENT_WIFI_AUTO_DOWNLOAD = 9;
    public static final int EVENT_PAUSE_WIFI_AUTO_DOWNLOAD = 10;
    public static final int EVENT_DELETE_INSTALLED_APK_FILE = 11;
    public static final int EVENT_REPORT_INSTALL_RESULT = 12;
    public static final int EVENT_FOUND_APP_UPDATE = 13;
//    public static final int EVENT_APP_UPDATE_DOWNLOAD = 14;
    public static final int EVENT_EXTERNEL_DOWN_IMAGE_CACHE = 15;
    public static final int EVENT_GET_APP_UPDATE_LIST = 16;
    public static final int EVENT_RES_GET_UPDATE_LIST = 17;
    public static final int EVENT_ADD_DOWNLOAD_WITHOUT_NOTIFY_READY = 18;
    public static final int EVENT_APP_INSTALLED = 19;
    public static final int EVENT_CHECK_DOWNLOAD_THREAD = 20;
    public static final int EVENT_DEBUG_PULL_DATA = 21;
    public static final int EVENT_FOUND_SILENT_UPDATE_APPS = 22;
    public static final int EVENT_REPORT_OFFLINE_LOG = 23;
    public static final int EVENT_APK_WEB_REQUEST = 24;
	public static final int EVENT_PAUSE_BACKGROUND_DOWNLOAD = 25;
	public static final int EVENT_PAUSE_UPDATE_DOWNLOAD = 26;
	public static final int EVENT_SCREEN_ON = 27;

    private Context mContext;
    private HttpManager mHttpManager;
    private DownloadPool mDownloadPool;

    private AppUpdateManager mAppUpManager;
    private AutoUpdateControl mAutoUpdateControl;

    private static DownloadManager mSelf = null;

    public static DownloadManager getInstance(Context context) {
        if (mSelf == null) {
            mSelf = new DownloadManager(context);
        }
        return mSelf;
    }

    DownloadManager(Context context) {
        mContext = context;
        mAppUpManager = new AppUpdateManager(mContext);
        mDownloadPool = new DownloadPool(mContext, this);
        mHttpManager = new HttpManager(mContext, this, mDownloadPool);
        mAutoUpdateControl = new AutoUpdateControl(mContext, mDownloadPool);
    }
    
    public AutoUpdateControl getAutoUpdateControl() {
        return mAutoUpdateControl;
    }

    private static final String KEY_APK_NAME = "apkFileName";

    private String getSavedSelfApkName() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        return sp.getString(KEY_APK_NAME, null);
    }

    private void saveSelfApkName(String apkName) {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_APK_NAME, apkName);
        editor.commit();
    }

    
    public DownloadPool getDownloadPool() {
        return mDownloadPool;
    }
    
    
    public void checkDownloadThread() {
        mDownloadPool.checkDownloadThread();
    }
    
    public static class DownloadMsg {
        public static final int MSG_APK_WAIT_DOWNLOAD = 1;
        public static final int MSG_APK_DOWNLOADING = MSG_APK_WAIT_DOWNLOAD + 1;
        public static final int MSG_APK_PAUSE = MSG_APK_DOWNLOADING + 1;
        public static final int MSG_APK_CANCEL = MSG_APK_PAUSE + 1;
        public static final int MSG_NO_ENOUGH_SPACE = MSG_APK_CANCEL + 1;
        public static final int MSG_SDCARD_LOST = MSG_NO_ENOUGH_SPACE + 1;
        public static final int MSG_DOWNLOAD_PROGRESS_UPDATE = MSG_SDCARD_LOST + 1;
        public static final int MSG_DOWNLOAD_HTTP_ERROR = MSG_DOWNLOAD_PROGRESS_UPDATE + 1;
        public static final int MSG_DOWNLOAD_COMPLETE = MSG_DOWNLOAD_HTTP_ERROR + 1;
        public static final int MSG_INSTALLED = MSG_DOWNLOAD_COMPLETE + 1;
        public static final int MSG_INSTALL_FAILED = MSG_INSTALLED + 1;
        public static final int MSG_INSTALLING = MSG_INSTALL_FAILED + 1;
        public static final int MSG_FILE_NOT_FOUND = MSG_INSTALLING + 1;
        public static final int MSG_FILE_NOT_USABLE = MSG_FILE_NOT_FOUND + 1;
    }


    public int getDownloadEventState(String pkgName, int verCode) {
        DownloadEventInfo eventInfo = mDownloadPool.getEventInfo(pkgName, verCode);
        if (eventInfo != null) {
            return eventInfo.getCurrState();
        }

        return -1;
    }

    public void addDownloadEvent(DownloadEventInfo eventInfo,
            final boolean disReadyToast) {
        String pkgName = eventInfo.getPkgName();
        int verCode = eventInfo.getVersionCode();
        String receiverPkg = eventInfo.getReceiverPkgName();
        String receiverClass = eventInfo.getReceiverClass();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pkgInfo != null) {
            if(pkgInfo.versionCode == verCode) {
                //this apk has been installed, pass this event;
                return;
            }
        }
        
        DownloadEventInfo info = mDownloadPool.getEventInfo(pkgName, verCode);

        if (info != null) {
            info.setReceiverClassName(receiverPkg, receiverClass);
            if (info.getEventArray() == DownloadEventInfo.ARRAY_PAUSED) {
                if (info.getVersionCode() == verCode) {
                    startDownloadEvent(pkgName, verCode);
                    return;
                }
            } else {
                if (info.getVersionCode() == eventInfo.getVersionCode()) {
                    if (info.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
                        File apkFile = info.getApkFile();
                        if (!apkFile.exists()) {
                            if (eventInfo.isRenameingTmpFile()) {
                                return;
                                
                            }else {
                                mDownloadPool.cancelEvent(info, true);
                            }
                        }
                    } else {
                        toastHasAddedMessage(info.getAppName());
                        return;
                    }
                }
            }
        } else {
        	UpdateAppDisplayInfo upInfo = mAppUpManager.getUpdateAppInfo(pkgName);
        	if (upInfo != null && upInfo.getDiffDownUrl() != null) {
        		String topicId = eventInfo.getTopicId();
        		String fromFlag = eventInfo.getDownloadFlag();
        		eventInfo = new DownloadEventInfo(upInfo.getPkgName(),
        										  upInfo.getAppName(),
        										  upInfo.getMd5(),
        										  upInfo.getDownUrl(),
        										  topicId,
        										  fromFlag,
        										  false,
        										  eventInfo.isOnlyDownInWifi(),
        										  false,
        										  upInfo.getVerCode(),
        										  upInfo.getApkId(),
        										  upInfo.getDiffDownUrl(),
        										  upInfo.getFileSize(),
        										  upInfo.getDiffPatchSize());
        	}
        	
        }
        final String appName = eventInfo.getAppName();
        final String md5Str = eventInfo.getMd5();
        if (eventInfo.isSelfApk()) {
            if (info == null || !info.getMd5().equals(md5Str)) {
                String lastName = getSavedSelfApkName();
                if (!appName.equals(lastName)) {
                    File lastApkFile = DownloadEventInfo.getSelfApkFile(lastName);
                    if (lastApkFile.exists()) {
                        lastApkFile.delete();
                    }
                    File lastTmpFile = DownloadEventInfo.getSelfApkTmpFile(lastName);
                    if (lastTmpFile.exists()) {
                        lastTmpFile.delete();
                    }

                    saveSelfApkName(appName);
                }
            }
        }

        DownloadEventInfo existThirdInfo = mDownloadPool.getExistThirdDownloadInfo(eventInfo);
        
        if (disReadyToast && existThirdInfo == null) {
            Toast.makeText(mContext,
                           mContext.getResources().getString(R.string.download_ready),
                           Toast.LENGTH_SHORT)
                        .show();
        }
        eventInfo.setReceiverClassName(receiverPkg, receiverClass);
        mDownloadPool.addDownloadEvent(eventInfo);
    }

    private void toastHasAddedMessage(String appName) {
        Toast.makeText(mContext,
                       mContext.getResources().getString(R.string.down_toast_has_downloading, appName),
                       Toast.LENGTH_SHORT)
                .show();
    }


    public static int getDownloadingNumber() {
        return DownloadPool.getCurrDownloadingNum();
    }

    public void pauseDownloadEvent(String pkgName, int verCode) {
        mDownloadPool.pauseDownloadEvent(pkgName, verCode);
    }

    public void cancelDownloadEvent(String pkgName, int verCode, boolean delFile) {
        DownloadEventInfo eventInfo = mDownloadPool.getEventInfo(pkgName, verCode);
        if (eventInfo != null) {
            eventInfo.cancelEvent();
        } else {
            return;
        }
        // mDownloadPool.downloadEventInfoChanged(eventInfo);
        mDownloadPool.cancelEvent(eventInfo, delFile);
        mAppUpManager.downloadEventInfoChange(eventInfo);
    }

    public void startDownloadEvent(String pkgName, int verCode) {
        DownloadEventInfo eventInfo = mDownloadPool.getEventInfo(pkgName, verCode);
        if(eventInfo != null){
            eventInfo.readyToDownload();
            mDownloadPool.addDownloadEvent(eventInfo);
        }
    }

    public void networkDisconnect() {
        mDownloadPool.networkDisconnect();
    }

    public void autoContinueDownload() {
        mDownloadPool.autoContinueDownload();
    }

//    public void requestWifiDownEvent() {
//        mDownloadPool.getWifiDownEventArray();
//    }

    public void deleteInstalledApkFile(String pkgName, int verCode, boolean delFileOnly) {
        DownloadEventInfo eventInfo = mDownloadPool.getEventInfo(pkgName, verCode);
        if (eventInfo != null) {
            mDownloadPool.deleteInstalledApkFile(eventInfo, delFileOnly);
            ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALLED);
        }
    }


    public void reportInstallResult(final String pkgName, final int verCode) {
        final DownloadEventInfo eventInfo = mDownloadPool.getEventInfo(pkgName, verCode);
        if (eventInfo == null) {
            return;
        }
        final int versionCode = Util.getVersionCode(mContext, eventInfo.getApkFile());
        if (versionCode == -1) {
            return;
        }
 
        mContext.sendBroadcast(new Intent("download.refresh"));
        
        new Thread() {
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo appInfo = null;
                try {
                    appInfo = pm.getApplicationInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                String installApkMd5 = Util.getFileMd5(appInfo.publicSourceDir);
                if (installApkMd5 != null && eventInfo.getMd5() != null
                        && !installApkMd5.equalsIgnoreCase(eventInfo.getMd5())) {
                    Util.log(TAG, "reportInstallResult", " install Apk Md5:" + installApkMd5);
                    Util.log(TAG, "reportInstallResult", "download Apk Md5:" + eventInfo.getMd5());
                    Util.log(TAG, "reportInstallResult", "no match, cancel report install information");
                    return;
                }

                // report to statistics server

                String from = eventInfo.getDownloadFlag();

                // add by huangyn
                if (from != null && from.equals("/13/DownloadRegard")) {
                    Intent intent = new Intent("com.zhuoyi.market.install.INSTALL_COMPLETED");
                    intent.putExtra("package", pkgName);
                    intent.putExtra("state", DownBaseInfo.STATE_INSTALLED);
                    mContext.sendBroadcast(intent);
                }
                // end
                // report to market server
                mDownloadPool.reportInstallResult(eventInfo);
            }
        }.start();
    }


    /**
     * for download pool use
     * 
     * @param eventInfo
     * @return
     */
    public int downloadApk(DownloadEventInfo eventInfo) {
        if (mHttpManager == null) {
            return -1;
        }
        return mHttpManager.downloadApk(eventInfo);
    }

    public static DownloadEventInfo getEventInfo(Context context, String pkgName, int verCode) {
        return DownloadPool.getEventInfo(context, pkgName, verCode);
    }

    public static void startServiceForAppInstalled(Context context, String pkgName) {
        Handler serviceHandler = DownloadService.getServiceHandler();
        if (serviceHandler == null) {
            Intent serviceIntent = new Intent(context, DownloadService.class);
            serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_APP_INSTALLED);
            serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);

            context.startService(serviceIntent);
        } else {

            Message msg = new Message();
            msg.what = EVENT_APP_INSTALLED;
            msg.obj = pkgName;

            serviceHandler.sendMessage(msg);
        }
    }

    public static void startServiceExternelImageCache(Context context, String pkgName, byte[] imageBytes) {
        Handler serviceHandler = DownloadService.getServiceHandler();
        if (serviceHandler == null) {
            Intent serviceIntent = new Intent(context, DownloadService.class);
            serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_EXTERNEL_DOWN_IMAGE_CACHE);
            serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
            serviceIntent.putExtra(EVENT_KEY_IMAGEBYTES, imageBytes);

            context.startService(serviceIntent);

        } else {
            Message msg = new Message();
            msg.what = EVENT_EXTERNEL_DOWN_IMAGE_CACHE;
            ImageByteInfo imageInfo = new ImageByteInfo();
            imageInfo.packageName = pkgName;
            imageInfo.apkBitmapByte = imageBytes;
            msg.obj = imageInfo;

            serviceHandler.sendMessage(msg);
        }

    }

//    public static void startServiceAutoUpdate(Context context) {
//        Handler serviceHandler = DownloadService.getServiceHandler();
//        if (serviceHandler == null) {
//            Intent serviceIntent = new Intent(context, DownloadService.class);
//            serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_APP_UPDATE_DOWNLOAD);
//            context.startService(serviceIntent);
//        } else {
//            serviceHandler.sendEmptyMessage(EVENT_APP_UPDATE_DOWNLOAD);
//        }
//    }
    
    
    public static void startServiceDownloadSougouApk(Context context, String url,
            String pkgName, String appName, int verCode, long totalSize) {
    	Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_ADD_DOWNLOAD);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        serviceIntent.putExtra(EVENT_KEY_APPNAME, appName);
        serviceIntent.putExtra(EVENT_KEY_FROM, ReportFlag.FROM_THIRD_DOWNLOAD);
        serviceIntent.putExtra(EVENT_KEY_TOPICID, ReportFlag.TOPIC_NULL);
        serviceIntent.putExtra(EVENT_KEY_VERCODE, verCode);
        serviceIntent.putExtra(EVENT_KEY_URL, url);
        serviceIntent.putExtra(EVENT_KEY_TOTALSIZE, totalSize);
        context.startService(serviceIntent);
    }
    
    
    public static void startServiceAddExterEvent(Context context, String url,
            String pkgName, String appName, String md5, String topicId, String from,
            int verCode, int appId, String receiverPkg, String receiverClass) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_ADD_DOWNLOAD);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        serviceIntent.putExtra(EVENT_KEY_APPNAME, appName);
        serviceIntent.putExtra(EVENT_KEY_FROM, from);
        if (!TextUtils.isEmpty(topicId)) {
            serviceIntent.putExtra(EVENT_KEY_TOPICID, topicId);
        }
        serviceIntent.putExtra(EVENT_KEY_VERCODE, verCode);
        serviceIntent.putExtra(EVENT_KEY_APPID, appId);
        if (!TextUtils.isEmpty(md5)) {
            serviceIntent.putExtra(EVENT_KEY_MD5, md5);
        }
        if (!TextUtils.isEmpty(url)) {
            serviceIntent.putExtra(EVENT_KEY_URL, url);
        }
        if (!TextUtils.isEmpty(receiverPkg) && !TextUtils.isEmpty(receiverClass)) {
            serviceIntent.putExtra(EVENT_KEY_RECEIVER_PKG, receiverPkg);
            serviceIntent.putExtra(EVENT_KEY_RECEIVER_CLASS, receiverClass);
        }
        context.startService(serviceIntent);
    }
    

    public static void startServiceAddEvent(Context context, String url,
            String pkgName, String appName, String md5, String topicId, String from,
            int verCode, int appId, long totalSize) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_ADD_DOWNLOAD);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        serviceIntent.putExtra(EVENT_KEY_APPNAME, appName);
        serviceIntent.putExtra(EVENT_KEY_FROM, from);
        if (!TextUtils.isEmpty(topicId)) {
        	serviceIntent.putExtra(EVENT_KEY_TOPICID, topicId);
        }
        serviceIntent.putExtra(EVENT_KEY_VERCODE, verCode);
        serviceIntent.putExtra(EVENT_KEY_APPID, appId);
        if (!TextUtils.isEmpty(md5)) {
            serviceIntent.putExtra(EVENT_KEY_MD5, md5);
        }
        if (!TextUtils.isEmpty(url)) {
            serviceIntent.putExtra(EVENT_KEY_URL, url);
        }
        serviceIntent.putExtra(EVENT_KEY_TOTALSIZE, totalSize);
        context.startService(serviceIntent);
    }

    public static void startServiceAutoContinue(Context context) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_AUTO_CONTINUE);
        context.startService(serviceIntent);
    }

    public static void startServiceNetworkDisconnect(Context context) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_NETWORK_DISCONNECT);
        context.startService(serviceIntent);
    }

    public static void startServiceRequestWifiAutoDownArray(Context context) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_GET_WIFI_DOWN_ARRAY);
        context.startService(serviceIntent);
    }

    public static void startServiceDeleteApkFile(Context context,
            String pkgName, int verCode) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_DELETE_INSTALLED_APK_FILE);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        serviceIntent.putExtra(EVENT_KEY_VERCODE, verCode);
        context.startService(serviceIntent);
    }

    public static void startServiceReportInstallResult(Context context,
            String pkgName, int verCode) {
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_REPORT_INSTALL_RESULT);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        serviceIntent.putExtra(EVENT_KEY_VERCODE, verCode);
        context.startService(serviceIntent);
    }
    
    
    public static void startServiceReportOffLineLog(Context context, String action, String from) {
    	Intent serviceIntent = new Intent(context, DownloadService.class);
    	serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_REPORT_OFFLINE_LOG);
        serviceIntent.putExtra(EVENT_KEY_REPORT_ACTION, action);
        serviceIntent.putExtra(EVENT_KEY_FROM, from);
        context.startService(serviceIntent);
    }
    
    
    public static void startServiceReportHomeAdLog(Context context, String pkgName) {
    	Intent serviceIntent = new Intent(context, DownloadService.class);
    	serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_REPORT_OFFLINE_LOG);
        serviceIntent.putExtra(EVENT_KEY_REPORT_ACTION, ReportFlag.ACTION_VIEW_COLUMN);
        serviceIntent.putExtra(EVENT_KEY_FROM, ReportFlag.FROM_HOME_AD);
        serviceIntent.putExtra(EVENT_KEY_PKGNAME, pkgName);
        context.startService(serviceIntent);
    }
	
	
	public static void startServicePauseBackgroundDownload(Context context) {
		Handler serviceHandler = DownloadService.getServiceHandler();
		if (serviceHandler != null) {
			serviceHandler.sendEmptyMessage(EVENT_PAUSE_BACKGROUND_DOWNLOAD);
			
		} else {
	    	Intent serviceIntent = new Intent(context, DownloadService.class);
	        serviceIntent.putExtra(EXTRA_EVENT_KEY, EVENT_PAUSE_BACKGROUND_DOWNLOAD);
	        context.startService(serviceIntent);
		}
    }
    
    
    public void pauseAppAutoUpdate() {
        mAutoUpdateControl.pauseAutoUpdates();
    }
    

    // start for app update function
    public void initAppUpdateList() {
        new Thread() {
            public void run() {
            	PackageManager pm = mContext.getPackageManager();
            	mAutoUpdateControl.pauseAutoUpdates();
                List<AppInfoBto> updateList = MarketApplication.getAppUpdateList();
                //get the backed list, for different from the original list
                List<AppInfoBto> updateListClone = null;
                if(updateList != null) {
                    updateListClone = (List<AppInfoBto>) ((ArrayList<AppInfoBto>) updateList).clone();
                }
                
                if(updateListClone == null) {
                    return;
                }
                
                ArrayList<String> ignoreList =  AppUpdateManager.getIgnoreApp(mContext);
                List<AppInfoBto> unInstallInfoList = new ArrayList<AppInfoBto>();
                
                int infoUpdateFlag = 0;
                AutoUpdateEventInfo eventInfo = null;
                String verUpStr = null;
                String tmpFileName = null;
                String apkFileName = null;
                UpdateAppDisplayInfo updateInfo = null;
                AppInfoBto infoBto = null;
                
                mAutoUpdateControl.clearStorage();
                for (int i=0; i < updateListClone.size(); i++) {
                    infoBto = updateListClone.get(i);
                    
                    //获取没忽略掉的更新应用信息
                    if (ignoreList == null || !ignoreList.contains(infoBto.getPackageName())) {
                        unInstallInfoList.add(infoBto);
                    }
                    
                    infoUpdateFlag = infoBto.getIsForcedUp();
                    
                    Util.log(TAG, "initAppUpdateList", "one info, appName:" + infoBto.getName() + ", update flag: " + infoUpdateFlag);

                    int currVerCode = -1;
                    try {
						currVerCode = pm.getPackageInfo(infoBto.getPackageName(), 0).versionCode;
					} catch (Exception e) {
						e.printStackTrace();
					}
                    
                    if(!FeatureOption.DELTA_UPDATE_APP 
                    		|| infoBto.getOldVersionCode()!=currVerCode ) { 
                    	infoBto.setPatchUrl(null);
                    }
                    
                    eventInfo = new AutoUpdateEventInfo(infoBto.getPackageName(),
                            infoBto.getName(),
                            infoBto.getMd5(),
                            infoBto.getDownUrl(),
                            false,
                            true,
                            infoUpdateFlag,
                            infoBto.getVersionCode(),
                            infoBto.getRefId(),
                            infoBto.getPatchUrl());
                    eventInfo.setTotalSize(infoBto.getFileSize());
        
                    verUpStr = mAppUpManager.getInstalledApkVersionName(infoBto.getPackageName()) + " -> " + infoBto.getVersionName();
                    tmpFileName = eventInfo.getDownloadFilePath();
                    apkFileName = eventInfo.getApkFile().getAbsolutePath();
                    updateInfo = mAppUpManager.getUpdateAppInfo(infoBto.getPackageName());
                    if (updateInfo == null || updateInfo.getVerCode() != eventInfo.getVersionCode()) {
                        updateInfo = new UpdateAppDisplayInfo(eventInfo.getPkgName(),
                                                              eventInfo.getAppName(),
                                                              eventInfo.getVersionCode(),
                                                              verUpStr,
                                                              infoBto.getFileSize(),
                                                              tmpFileName,
                                                              apkFileName,
                                                              infoBto.getMd5(),
                                                              infoBto.getDownUrl(),
                                                              infoBto.getRefId(),
                                                              infoBto.getPatchUrl(),
                                                              infoBto.getPatchSize(),
                                                              infoBto.getVerUptDes(),
                                                              infoBto.getVerUptTime());
                    }
                    if(updateInfo.isApkFileExist()) {
                        eventInfo.downloadComplete(mContext);
                        mDownloadPool.addToCompleteArray(eventInfo);
                        mDownloadPool.downloadEventInfoChanged(eventInfo);
                    } else {
                        mAutoUpdateControl.addAppUpdateEvent(eventInfo);
                    }

                	mAppUpManager.saveUpdatedAppInfo(updateInfo);
                }
                
                mAppUpManager.syncUpdateAppNum();
                mAppUpManager.sendMsgToResetList();
                mAppUpManager.sendMsgToDisplayUpdateApp();
                
                sendMessageToUpdateTab(AppUpdateManager.getUpdateAppNum());
                if (SettingData.mIsNotify == true) {
                    if (unInstallInfoList != null && unInstallInfoList.size() > 0)
                        NotifyUtil.updateNotificationBar(unInstallInfoList, unInstallInfoList.size());
                }
                com.market.download.util.Util.displayNumOnLauncher(mContext.getApplicationContext(), AppUpdateManager.getUpdateAppNum());
            }
        }.start();
    }

    public void updateAppInstalled(String pkgName) {
        mAppUpManager.updateAppInstalled(pkgName);
    }

    public boolean startAppUpdateDownload() {
        return mAutoUpdateControl.startAutoUpdates();
    }
    

    public void notifyUpdateEventInfoChanged(DownloadEventInfo eventInfo) {
        mAppUpManager.downloadEventInfoChange(eventInfo);
    }

    // end for app update function

    
    /**
     * 更新红点
     * @param updateCount
     * @param isShowCount
     */
    private void sendMessageToUpdateTab(int updateCount) {
        Intent intent = new Intent();
        intent.setAction(Splash.APPS_UPDATE);
        intent.putExtra("update_count", updateCount);
        mContext.sendBroadcast(intent);
    }
}
