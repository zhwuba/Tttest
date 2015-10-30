package com.market.download.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;

import com.market.daemon.Daemon;
import com.market.debug.MarketDebug;
import com.market.download.common.DownloadSettings;
import com.market.download.common.DownloadSettings.WifiDownConfig;
import com.market.download.silent.SilentEventManager;
import com.market.download.updates.AppUpdateManager;
import com.market.download.updates.UpdateAppDisplayInfo;
import com.market.download.userDownload.DeleteCacheThread;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.userDownload.ListenerManager;
import com.market.download.util.NetworkType;
import com.market.download.util.NotifyUtil;
import com.market.download.util.SdcardUtil;
import com.market.download.util.Util;
import com.market.featureOption.FeatureOption;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.TerminalInfo;
import com.market.net.request.GetAppsUpdateReq;
import com.market.net.response.GetAppsUpdateResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.statistics.ReportManager;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.appResident.SettingData;
import com.zhuoyi.market.asyncTask.CampaignsTimerTask;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.DiskLruCache;
import com.zhuoyi.market.utils.external.DownLoadSocketUtil;
import com.zhuoyi.system.promotion.listener.ZyPromSDK;

public class DownloadService extends Service {
    public static final String TAG = "DownloadService";

    private DownloadManager mDownManager;
    
    private ListenerManager mListenManager;
//    private Messenger mClientMsger;
//    private Messenger mManagerMsger;

    private SilentEventManager mSilentManager;
    
    private ReportManager mReportManager;

    private static Handler mSelfHandler = null;

    private boolean mHasStartService = false;
    
    /**	活动奖品查询 Task	**/
    private CampaignsTimerTask mCampaignsTimerTask;
    private Timer mCampaignsTimer;
    static {
//    	System.loadLibrary("ApkPatchLibrary");
    	System.loadLibrary("uninstalled_observer");
    }
    
    public static void notifyFoundAppUpdate(Context context) {
        if (mSelfHandler == null) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(DownloadManager.EXTRA_EVENT_KEY, DownloadManager.EVENT_FOUND_APP_UPDATE);
            context.startService(intent);

        } else {
            mSelfHandler.sendEmptyMessage(DownloadManager.EVENT_FOUND_APP_UPDATE);

        }
    }
    
    
    public static void notifyFoundSilentUpdateApps(Context context) {
        if (mSelfHandler == null) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(DownloadManager.EXTRA_EVENT_KEY, DownloadManager.EVENT_FOUND_SILENT_UPDATE_APPS);
            context.startService(intent);

        } else {
            mSelfHandler.sendEmptyMessage(DownloadManager.EVENT_FOUND_SILENT_UPDATE_APPS);

        }
    }

    
    public static void getAppUpdateList(Context context) {
        if (mSelfHandler == null) {
            Intent intent = new Intent(context, DownloadService.class);
            intent.putExtra(DownloadManager.EXTRA_EVENT_KEY, DownloadManager.EVENT_GET_APP_UPDATE_LIST);
            context.startService(intent);

        } else {
            mSelfHandler.sendEmptyMessage(DownloadManager.EVENT_GET_APP_UPDATE_LIST);
        }
    }

    
    public static void sendCheckDownThreadMsg() {
        if(mSelfHandler != null) {
            if(!mSelfHandler.hasMessages(DownloadManager.EVENT_CHECK_DOWNLOAD_THREAD)) {
                mSelfHandler.sendEmptyMessageDelayed(DownloadManager.EVENT_CHECK_DOWNLOAD_THREAD, 15 * 1000);
            }
        }
    }
    
    
    public static void cancelCheckDownThreadMsg() {
        if(mSelfHandler != null) {
            mSelfHandler.removeMessages(DownloadManager.EVENT_CHECK_DOWNLOAD_THREAD);
        }
    }
    
    
    public static Handler getServiceHandler() {
        return mSelfHandler;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        if(!mHasStartService) {
            startService(new Intent(this, DownloadService.class));
        }
        return mDownService;
    }

    
    private void initDirectory() {
        //for oppo bug: ZhuoYiMarket is a file but not the directory
        File dir = new File(Constant.download_dir_name);
        boolean exist = dir.exists();
        if (!exist) {
            if (!dir.isDirectory()) {
                dir.delete();
            }
            
            dir.mkdirs();
        }
    }
    
    
    private HandlerThread mHandlerThread;
    private ServiceHandler mHandler;
    
    @Override
    public void onCreate() {
    	ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
    	if((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
    		Daemon.run(this, DownloadService.class, (int) (Daemon.INTERVAL_ONE_MINUTE * 0.5));
    	}
        mDownManager = DownloadManager.getInstance(this);
        mSilentManager = SilentEventManager.getInstance(getApplicationContext());
        mReportManager = ReportManager.getInstance(getApplicationContext());
        super.onCreate();
        
        Constant.checkMarketSign(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter);

        String sdDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File debugFile = new File(sdDirPath + "/TydAppStatisticsDebugSwitchOn");
        if (debugFile.exists()) {
        	FeatureOption.DOWNLOAD_LOG = true;
        }

        mHandlerThread = new HandlerThread("marketHandleThread");
        mHandlerThread.start();
        mHandler = new ServiceHandler(mHandlerThread.getLooper());
        
        mSelfHandler = mHandler;

        mListenManager = ListenerManager.getInstance(this);
        
        startCampaignsQueryTask();
        if(FeatureOption.DOWNLOAD_FROM_THIRD_PARTY) {
        	DownLoadSocketUtil downLoadSocketUtil = new DownLoadSocketUtil(getApplicationContext(), mSelfHandler);
            downLoadSocketUtil.startServerSocket();
        }
        
        ZyPromSDK.getInstance().init(getApplicationContext(), true);
        
        initDirectory();
        
        startMonitorUninstall();
//        DownloadEventInfo eventInfo = new DownloadEventInfo("com.yph.textSpeed",
//                "yphTestSpeed",
//                null,
//                "http://msoftdl.360.cn/mobilesafe/shouji360/360safesis/360clear_doraemon.apk",
//                "test", "test", false, true, false, 0,
//                0, null, 0, 0);
//        Message msg = new Message();
//        msg.what = DownloadManager.EVENT_ADD_DOWNLOAD;
//        msg.obj = eventInfo;
//        mHandler.sendMessage(msg);
    }

    @Override
    public void onDestroy() {
        mHandlerThread.quit();
        if(mCampaignsTimer != null) {
        	mCampaignsTimer.cancel();
        	mCampaignsTimer = null;
        }
        mCampaignsTimerTask = null;
        unregisterReceiver(mReceiver);
        mSelfHandler = null;
        super.onDestroy();
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Util.log(TAG, "mReceiver.onReceive", "action:" + action);
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
//                mHandler.sendEmptyMessageDelayed(DownloadManager.EVENT_SCREEN_OFF, 30 * 1000);
                mHandler.sendEmptyMessage(DownloadManager.EVENT_SCREEN_OFF);
                
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mHandler.sendEmptyMessage(DownloadManager.EVENT_SCREEN_ON);
                
            }
        }
    };
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mHasStartService = true;
        boolean isStartDeleteCache = false;
        String cachePath = "";
        if (intent != null) {

            isStartDeleteCache = intent.getBooleanExtra("deleteCache", false);

            if (isStartDeleteCache) {
                cachePath = intent.getStringExtra("deletePath");
                if (!TextUtils.isEmpty(cachePath)) {
                    new DeleteCacheThread(DownloadService.this, cachePath).start();
                }
                return super.onStartCommand(intent, flags, startId);
            }

            int eventFlag = intent.getIntExtra(DownloadManager.EXTRA_EVENT_KEY, DownloadManager.EVENT_UNDEFINIT);
            if (eventFlag != DownloadManager.EVENT_UNDEFINIT) {
                Message msg = new Message();
                msg.what = eventFlag;
                if (eventFlag == DownloadManager.EVENT_ADD_DOWNLOAD) {
                    String pkgName = intent.getStringExtra(DownloadManager.EVENT_KEY_PKGNAME);
                    String appName = intent.getStringExtra(DownloadManager.EVENT_KEY_APPNAME);
                    String url = intent.getStringExtra(DownloadManager.EVENT_KEY_URL);
                    String md5 = intent.getStringExtra(DownloadManager.EVENT_KEY_MD5);
                    int verCode = intent.getIntExtra(DownloadManager.EVENT_KEY_VERCODE, 0);
                    int appId = intent.getIntExtra(DownloadManager.EVENT_KEY_APPID, 0);
                    String from = intent.getStringExtra(DownloadManager.EVENT_KEY_FROM);
                    String topicId = intent.getStringExtra(DownloadManager.EVENT_KEY_TOPICID);
                    long totalSize = intent.getLongExtra(DownloadManager.EVENT_KEY_TOTALSIZE, 0);
                    String receiverPkg = intent.getStringExtra(DownloadManager.EVENT_KEY_RECEIVER_PKG);
                    String receiverClass = intent.getStringExtra(DownloadManager.EVENT_KEY_RECEIVER_CLASS);
                    if (topicId == null) {
                    	topicId = ReportFlag.TOPIC_NULL;
                    }
                    if (from == null) {
                        from = ReportFlag.FROM_EXTRA_DOWN;
                    }
                    DownloadEventInfo eventInfo = null;
                    boolean wifiFlag = NetworkType.isWifiAvailable(this);
                    if (md5 != null) {
                        eventInfo = new DownloadEventInfo(pkgName, appName,
                                md5, url, topicId, from, false, wifiFlag, false,
                                verCode, appId, totalSize);
                    } else {
                        eventInfo = new DownloadEventInfo(pkgName, appName,
                                url, topicId, from, false, wifiFlag, totalSize);
                    }
                    if (receiverPkg != null && receiverClass != null) {
                        eventInfo.setReceiverClassName(receiverPkg, receiverClass);
                    }
                    
                    msg.obj = eventInfo;

                } else if (eventFlag == DownloadManager.EVENT_CANCELDOWNLOAD
                        || eventFlag == DownloadManager.EVENT_PAUSEDOWNlOAD
                        || eventFlag == DownloadManager.EVENT_STARTDOWNLOAD
                        || eventFlag == DownloadManager.EVENT_REPORT_INSTALL_RESULT
                        || eventFlag == DownloadManager.EVENT_DELETE_INSTALLED_APK_FILE) {
                    String pkgName = intent.getStringExtra(DownloadManager.EVENT_KEY_PKGNAME);
                    int verCode = intent.getIntExtra(DownloadManager.EVENT_KEY_VERCODE, 0);
                    msg.obj = new EventSignal(pkgName, verCode);

                } else if (eventFlag == DownloadManager.EVENT_EXTERNEL_DOWN_IMAGE_CACHE) {
                    ImageByteInfo imageInfo = new ImageByteInfo();
                    imageInfo.packageName = intent.getStringExtra(DownloadManager.EVENT_KEY_PKGNAME);
                    imageInfo.apkBitmapByte = intent.getByteArrayExtra(DownloadManager.EVENT_KEY_IMAGEBYTES);
                    msg.obj = imageInfo;
                } else if (eventFlag == DownloadManager.EVENT_APP_INSTALLED) {
                    String pkgName = intent.getStringExtra(DownloadManager.EVENT_KEY_PKGNAME);
                    msg.obj = pkgName;
                } else if (eventFlag == DownloadManager.EVENT_REPORT_OFFLINE_LOG) {
                	String logAction = intent.getStringExtra(DownloadManager.EVENT_KEY_REPORT_ACTION);
                	String aFrom = intent.getStringExtra(DownloadManager.EVENT_KEY_FROM);
                	String pkgName = intent.getStringExtra(DownloadManager.EVENT_KEY_PKGNAME);
                	Bundle data = new Bundle();
                	data.putString(DownloadManager.EVENT_KEY_REPORT_ACTION, logAction);
                	data.putString(DownloadManager.EVENT_KEY_FROM, aFrom);
                	data.putString(DownloadManager.EVENT_KEY_PKGNAME, pkgName);
                	msg.setData(data);
                }

                mHandler.sendMessage(msg);
            }
        }
        return START_STICKY;
    }

    private class WifiStartEndTime {
        public int startMinute;
        public int endMinute;
    }

    private ArrayList<WifiStartEndTime> parserWifiStartEndTime(String timeStr) {
        ArrayList<WifiStartEndTime> timeArray = new ArrayList<WifiStartEndTime>();

        if (TextUtils.isEmpty(timeStr))
            return timeArray;

        if (timeStr.contains(",")) {
            String[] startEndArray = timeStr.split(",");
            String[] startEnd = null;
            WifiStartEndTime wifiTime = null;
            for (int i = 0; i < startEndArray.length; i++) {
                startEnd = startEndArray[i].split("-");
                wifiTime = new WifiStartEndTime();
                wifiTime.startMinute = Integer.parseInt(startEnd[0]);
                wifiTime.endMinute = Integer.parseInt(startEnd[1]);
                timeArray.add(wifiTime);
            }

        } else {
            String[] startEnd = timeStr.split("-");
            WifiStartEndTime wifiTime = new WifiStartEndTime();
            wifiTime.startMinute = Integer.parseInt(startEnd[0]);
            wifiTime.endMinute = Integer.parseInt(startEnd[1]);
            timeArray.add(wifiTime);
        }
        return timeArray;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    class EventSignal {
        String pkgName;
        int verCode;

        EventSignal(String packageName, int versionCode) {
            pkgName = packageName;
            verCode = versionCode;
        }
    }

    private IBinder mDownService = new IDownloadService.Stub() {

        @Override
        public void setClientMsgHandler(IBinder msgHandler)
                throws RemoteException {
            if (msgHandler == null) {
                mListenManager.setClientMsgHandler(null);
            } else {
                mListenManager.setClientMsgHandler(new Messenger(msgHandler));
            }
        }

        @Override
        public void startDownload(String pkgName, int verCode)
                throws RemoteException {
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_STARTDOWNLOAD;
            msg.obj = new EventSignal(pkgName, verCode);
            mHandler.sendMessage(msg);
        }

        @Override
        public void pauseDownload(String pkgName, int verCode)
                throws RemoteException {
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_PAUSEDOWNlOAD;
            msg.obj = new EventSignal(pkgName, verCode);
            mHandler.sendMessage(msg);
        }

        @Override
        public void cancelDownload(String pkgName, int verCode, boolean delFile)
                throws RemoteException {
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_CANCELDOWNLOAD;
            msg.obj = new EventSignal(pkgName, verCode);
            msg.arg1 = delFile ? 1:0;
            mHandler.sendMessage(msg);
        }

        @Override
        public void addDownload(String pkgName, String appName, String md5,
                String url, String topicId, String flag, int verCode, int appId, long totalSize)
                throws RemoteException {
            boolean wifiFlag = NetworkType.isWifiAvailable(DownloadService.this);
            DownloadEventInfo eventInfo = new DownloadEventInfo(pkgName,
                    appName, md5, url, topicId, flag, false, wifiFlag, false, verCode,
                    appId, totalSize);
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_ADD_DOWNLOAD;
            msg.obj = eventInfo;
            mHandler.sendMessage(msg);
        }

        @Override
        public void setManagerMsgHandler(IBinder msgHandler)
                throws RemoteException {
            if (msgHandler == null) {
                mListenManager.setManagerMsgHandler(null);
            } else {
                mListenManager.setManagerMsgHandler(new Messenger(msgHandler));
            }
        }

        @Override
        public int getDownloadingNumber() throws RemoteException {
            return DownloadManager.getDownloadingNumber();
        }

        @Override
        public int getDownloadState(String pkgName, int verCode)
                throws RemoteException {
            return mDownManager.getDownloadEventState(pkgName, verCode);
        }

        @Override
        public void addDownloadWithoutNotifyReady(String pkgName,
                String appName, String md5, String url, String topicId, String flag,
                int verCode, int appId, long totalSize) throws RemoteException {
            boolean wifiFlag = NetworkType.isWifiAvailable(DownloadService.this);
            DownloadEventInfo eventInfo = new DownloadEventInfo(pkgName,
                    appName, md5, url, topicId, flag, false, wifiFlag, false, verCode,
                    appId, totalSize);
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_ADD_DOWNLOAD_WITHOUT_NOTIFY_READY;
            msg.obj = eventInfo;
            mHandler.sendMessage(msg);
        }

		@Override
		public void addDiffDownload(String pkgName, String appName, String md5,
				String url, String topicId, String flag, int verCode,
				int appId, String diffDownUrl, long totalSize, long diffPatchSize) throws RemoteException {
			boolean wifiFlag = NetworkType.isWifiAvailable(DownloadService.this);
			DownloadEventInfo eventInfo = new DownloadEventInfo(pkgName,
                    appName, md5, url, topicId, flag, false, wifiFlag, false, verCode,
                    appId, diffDownUrl, totalSize, diffPatchSize);
            Message msg = new Message();
            msg.what = DownloadManager.EVENT_ADD_DOWNLOAD;
            msg.obj = eventInfo;
            mHandler.sendMessage(msg);
		}

        @Override
        public void ignoreUpdate(String pkgName, int verCode) throws RemoteException {
//            mDownManager.pauseAppAutoUpdate();
            mDownManager.getAutoUpdateControl().removeFromAppUpdateList(pkgName, verCode);
        }

    };

    
    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Util.log(TAG, "handleMessage", "receive msg what=" + what);
            switch (what) {
            case DownloadManager.EVENT_ADD_DOWNLOAD:
                DownloadEventInfo eventInfo = (DownloadEventInfo) msg.obj;
                mDownManager.addDownloadEvent(eventInfo, true);
                break;

            case DownloadManager.EVENT_ADD_DOWNLOAD_WITHOUT_NOTIFY_READY:
                DownloadEventInfo noToastInfo = (DownloadEventInfo) msg.obj;
                mDownManager.addDownloadEvent(noToastInfo, false);
                break;

            case DownloadManager.EVENT_STARTDOWNLOAD:
                EventSignal startSignal = (EventSignal) msg.obj;
                mDownManager.startDownloadEvent(startSignal.pkgName, startSignal.verCode);
                break;

            case DownloadManager.EVENT_PAUSEDOWNlOAD:
                EventSignal pauseSignal = (EventSignal) msg.obj;
                mDownManager.pauseDownloadEvent(pauseSignal.pkgName, pauseSignal.verCode);
                break;

            case DownloadManager.EVENT_CANCELDOWNLOAD:
                EventSignal cancelSignal = (EventSignal) msg.obj;
                boolean delFile = msg.arg1 == 1 ? true:false;
                mDownManager.cancelDownloadEvent(cancelSignal.pkgName, cancelSignal.verCode, delFile);
                break;

            case DownloadManager.EVENT_DELETE_INSTALLED_APK_FILE:
                EventSignal deleteSignal = (EventSignal) msg.obj;
                mDownManager.reportInstallResult(deleteSignal.pkgName, deleteSignal.verCode);

                // AppInfoBto DelInfoBto = AppStore.getAppInfoBtoFromUpdateList(deleteSignal.pkgName);
                UpdateAppDisplayInfo updateInfo = AppUpdateManager.getUpdateAppInfo(DownloadService.this, deleteSignal.pkgName);
                DownloadEventInfo delEventInfo = DownloadManager.getEventInfo(DownloadService.this, deleteSignal.pkgName, deleteSignal.verCode);
                if (updateInfo != null && delEventInfo != null
                        && delEventInfo.getMd5() != null
                        && updateInfo.getMd5().equals(delEventInfo.getMd5())) {
                    // AppStore.removeFromUpdateList(deletePkgName);
                    mDownManager.updateAppInstalled(deleteSignal.pkgName);
                }
                
                boolean delFileOnly = false;
                if (msg.arg1 == 1) {
                    delFileOnly = true;
                }
                mDownManager.deleteInstalledApkFile(deleteSignal.pkgName, deleteSignal.verCode, delFileOnly);
                break;

            case DownloadManager.EVENT_REPORT_INSTALL_RESULT:
                EventSignal installSignal = (EventSignal) msg.obj;
                mDownManager.reportInstallResult(installSignal.pkgName, installSignal.verCode);

                AppInfoBto insInfoBto = MarketApplication.getAppInfoBtoFromUpdateList(installSignal.pkgName);
                DownloadEventInfo insEventInfo = DownloadManager.getEventInfo(DownloadService.this, installSignal.pkgName, installSignal.verCode);
                if (insInfoBto != null && insEventInfo.getMd5() != null
                        && insInfoBto.getMd5().equals(insEventInfo.getMd5())) {
                    // AppStore.removeFromUpdateList(installPkgName);
                    mDownManager.updateAppInstalled(installSignal.pkgName);
                }
                mListenManager.downInfoChanged(insEventInfo, DownloadMsg.MSG_INSTALLED);
                break;

            case DownloadManager.EVENT_SCREEN_OFF:
                Util.log(TAG, "handleMessage", "receiver screen off msg");
                if (!FeatureOption.BACKGROUND_DOWNLOAD) {
                    return;
                }
                
                if (!mSilentManager.startDownload705706Event()) {
                    Util.log(TAG, "handleMessage", "screen of msg: no 705 706 event");
                    boolean silentStartFlag = false;
                    WifiDownConfig config = DownloadSettings.getWifiDownConfig(DownloadService.this);
                    if (config.enable && NetworkType.isNetworkAvailable(DownloadService.this)) {
                        ArrayList<WifiStartEndTime> timeArray = parserWifiStartEndTime(config.exectime);
                        Calendar now = Calendar.getInstance();
                        int hour = now.get(Calendar.HOUR_OF_DAY);
                        int minute = now.get(Calendar.MINUTE);
                        int dayMinutes = hour * 60 + minute;
                        WifiStartEndTime wifiTime = null;
                        for (int i = 0; timeArray != null && i < timeArray.size(); i++) {
                            wifiTime = timeArray.get(i);
                            if (dayMinutes <= wifiTime.endMinute && dayMinutes >= wifiTime.startMinute) {
                                silentStartFlag = mSilentManager.startSilentDownload();
                            }
                        }
                    }
                        
                    if (!silentStartFlag) {
                        Util.log(TAG, "handleMessage", "screen of msg: no 700 event");
                        mDownManager.startAppUpdateDownload();
                    }
                }
                break;

            case DownloadManager.EVENT_AUTO_CONTINUE:
                mDownManager.autoContinueDownload();
                break;

            case DownloadManager.EVENT_NETWORK_DISCONNECT:
                mDownManager.networkDisconnect();
                break;

            case DownloadManager.EVENT_GET_WIFI_DOWN_ARRAY:
            	mSilentManager.requestSilentEventList();
                break;

            case DownloadManager.EVENT_PAUSE_WIFI_AUTO_DOWNLOAD:
                this.removeMessages(DownloadManager.EVENT_SCREEN_OFF);
            	mSilentManager.pauseSilentDownload();
                break;

            case DownloadManager.EVENT_FOUND_APP_UPDATE:
                mDownManager.initAppUpdateList();
                break;

            case DownloadManager.EVENT_EXTERNEL_DOWN_IMAGE_CACHE:
                final ImageByteInfo imageInfo = (ImageByteInfo) msg.obj;
                final byte[] apkBitmapByte = imageInfo.apkBitmapByte;
                new Thread() {
                    public void run() {
                        if (apkBitmapByte != null) {
                            Bitmap bmpout = generateBitmap(apkBitmapByte);
                            DiskLruCache.putImageToDisk(imageInfo.packageName,
                                    SdcardUtil.getSdcardPath() + DownloadEventInfo.IMAGE_DIR_PATH,
                                    bmpout);
                            if(bmpout != null && !bmpout.isRecycled()) {
                                bmpout.recycle();
                                bmpout = null;
                            }
                        }
                    }
                }.start();
                break;

            case DownloadManager.EVENT_GET_APP_UPDATE_LIST:
                GetAppsUpdateReq appUpdateReq = new GetAppsUpdateReq();
                String contents = SenderDataProvider.buildToJSONData(
                        getApplicationContext(), MessageCode.GET_APPS_UPDATE,
                        appUpdateReq);
                StartNetReqUtils.execListByPageRequest(mHandler,
                        DownloadManager.EVENT_RES_GET_UPDATE_LIST,
                        MessageCode.GET_APPS_UPDATE, contents);
                break;

            case DownloadManager.EVENT_RES_GET_UPDATE_LIST:
                Map<String, Object> map = (Map<String, Object>) msg.obj;

                GetAppsUpdateResp resp = null;

                List<AppInfoBto> appInfoList = null;

                if (map != null && map.size() > 0) {
                    resp = (GetAppsUpdateResp) map.get("appsUpdate");
                    if (resp != null) {
                        appInfoList = resp.getAppList();
                        long firstGetTime = DownloadSettings.setFirstGetUpdateListTime(getApplicationContext());
                        int SwitchFor704 = resp.getIsForcedUp();
                        long currSystemTime = System.currentTimeMillis();
                        if (SwitchFor704 == 1) {
                            long passTimeOf704 = resp.getSpreadTime() * 24 * 60 * 60 * 1000;
                            if ((currSystemTime - firstGetTime) >= passTimeOf704) {
                                DownloadSettings.setUpdateAutoFlag(getApplicationContext(), SwitchFor704);
                            }
                        }else {
                            DownloadSettings.setUpdateAutoFlag(getApplicationContext(), SwitchFor704);
                        }
                        
                        int zeroServiceSwitch = resp.getIsZeroFlow();
                        if (zeroServiceSwitch == 1) {
                            long passTimeOfZero = resp.getZeroFlowTime() * 24 * 60 * 60 * 1000;
                            boolean hasOpenedYet = DownloadSettings.hasServiceOpenUserUpdateFlag(getApplicationContext());
                            if (!hasOpenedYet && (currSystemTime - firstGetTime) >= passTimeOfZero) {
                                DownloadSettings.setUserUpdateAutoFlag(getApplicationContext(), true);
                                DownloadSettings.setServerOpenUserUpdateFlag(getApplicationContext());
                            }
                        }
                        DownloadSettings.setGetUpdateMillis(getApplicationContext());
                    }
                    if (appInfoList != null && appInfoList.size() > 0) {
                        MarketApplication.setAppUpdateList(appInfoList);
                        
                        mDownManager.initAppUpdateList();
                    }
                    
                    List<AppInfoBto> silentAppList = resp.getSilentAppList();
                    if (silentAppList != null) {
                        mSilentManager.init705706AppList(silentAppList);
                    }
                }
                break;
                
            case DownloadManager.EVENT_FOUND_SILENT_UPDATE_APPS:
                List<AppInfoBto> silentAppList = MarketApplication.getSilentUpdateList();
                if (silentAppList != null) {
                    mSilentManager.init705706AppList(silentAppList);
                }
                break;

            case DownloadManager.EVENT_APP_INSTALLED:
                String addedPkgName = (String) msg.obj;

                PackageInfo pkgInfo = null;
                try {
                    pkgInfo = getPackageManager().getPackageInfo(addedPkgName, 0);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                int verCode = 0;
                if (pkgInfo != null) {
                    verCode = pkgInfo.versionCode;
                }

                UpdateAppDisplayInfo updateDisInfo = AppUpdateManager.getUpdateAppInfo(DownloadService.this, addedPkgName);
                if (updateDisInfo != null && updateDisInfo.getVerCode() == verCode) {
                    if (!updateDisInfo.isInstalled()) {
                        updateDisInfo.installed();
                        AppUpdateManager.saveUpdatedAppInfo(DownloadService.this, updateDisInfo);
                        if (updateDisInfo.isAutoDownloaded()) {
                            AppUpdateManager.addEconomizedBytes(DownloadService.this, updateDisInfo.getFileSize());
                        }
                    }
                }

                DownloadEventInfo installedInfo = null;
                if (SettingData.mDeleteInstallPackage) {
                    installedInfo = DownloadManager.getEventInfo(DownloadService.this, addedPkgName, verCode);

                    if (installedInfo != null && (pkgInfo == null || (pkgInfo != null && verCode == installedInfo.getVersionCode()))) {
                        // DownloadManager.startServiceDeleteApkFile(DownloadService.this,
                        // addedPkgName, verCode);
                        installedInfo.installed();
                        Message handlerMsg = new Message();
                        handlerMsg.what = DownloadManager.EVENT_DELETE_INSTALLED_APK_FILE;
                        handlerMsg.obj = new EventSignal(addedPkgName, verCode);
                        handlerMsg.arg1 = 1;
                        mHandler.sendMessage(handlerMsg);
                    } else {
                        installedInfo = null;
                    }
                } else {
                    installedInfo = DownloadManager.getEventInfo(DownloadService.this, addedPkgName, verCode);
                    if (installedInfo != null && (pkgInfo == null || (pkgInfo != null && verCode == installedInfo.getVersionCode()))) {
                        // DownloadManager.startServiceReportInstallResult(DownloadService.this,
                        // addedPkgName, verCode);
                        installedInfo.installed();
                        Message handlerMsg = new Message();
                        handlerMsg.what = DownloadManager.EVENT_REPORT_INSTALL_RESULT;
                        handlerMsg.obj = new EventSignal(addedPkgName, verCode);
                        mHandler.sendMessage(handlerMsg);
                    } else {
                        installedInfo = null;
                    }
                }
                
                if (installedInfo != null) {
                	NotifyUtil.notifyInstallSuccess(installedInfo.getAppName(), installedInfo.getPkgName());
                }

                break;
                
            case DownloadManager.EVENT_CHECK_DOWNLOAD_THREAD:
                mDownManager.checkDownloadThread();
                break;
                
            case DownloadManager.EVENT_DEBUG_PULL_DATA:
                MarketDebug.pullDebugMarketData(getApplicationContext());

            	break;
            case CampaignsTimerTask.CAMPAIGNS_RECEIVER:	//查询奖品发放提醒
            	NotifyUtil.campaignsSendNotification(getApplicationContext(), 1);
            	break;
            	
            case DownloadManager.EVENT_REPORT_OFFLINE_LOG:
            	Bundle data = msg.getData();
            	String reportAction = data.getString(DownloadManager.EVENT_KEY_REPORT_ACTION);
            	if (reportAction == null) {
            		reportAction = "";
            	}
            	String reportFrom = data.getString(DownloadManager.EVENT_KEY_FROM);
            	if (reportFrom == null) {
            		reportFrom = "";
            	}
            	if (reportAction.equals(ReportFlag.ACTION_VIEW_COLUMN)
            			&& reportFrom.equals(ReportFlag.FROM_HOME_AD)) {
            		String reportPkgName = data.getString(DownloadManager.EVENT_KEY_PKGNAME);
            		if (reportPkgName == null) {
            			reportPkgName = "";
            		}
            		mReportManager.reportOffLineLog(reportAction, reportFrom, reportPkgName);
            	} else {
            		mReportManager.reportOffLineLog(reportAction, reportFrom);
            	}
            	break;
            case DownloadManager.EVENT_APK_WEB_REQUEST:
            	Map<String, String> downloadMap = (Map<String, String>)msg.obj;
            	DownLoadSocketUtil.downloadApk(downloadMap, getApplicationContext());
            	break;
				
			case DownloadManager.EVENT_PAUSE_BACKGROUND_DOWNLOAD:
			    this.removeMessages(DownloadManager.EVENT_SCREEN_OFF);
            	mDownManager.pauseAppAutoUpdate();
            	mSilentManager.pauseSilentDownload();
            	break;
            	
			case DownloadManager.EVENT_PAUSE_UPDATE_DOWNLOAD:
                this.removeMessages(DownloadManager.EVENT_SCREEN_OFF);
			    mDownManager.pauseAppAutoUpdate();
			    break;
			    
			case DownloadManager.EVENT_SCREEN_ON:
			    WifiDownConfig config = DownloadSettings.getWifiDownConfig(DownloadService.this);
                if (!config.fgflag) {
                    mSilentManager.pauseSilentDownload();
//                    mHandler.sendEmptyMessage(DownloadManager.EVENT_PAUSE_WIFI_AUTO_DOWNLOAD);
                }
                
                mDownManager.pauseAppAutoUpdate();
//                mHandler.sendEmptyMessage(DownloadManager.EVENT_PAUSE_UPDATE_DOWNLOAD);
			    break;
            default:
                super.handleMessage(msg);
            }
        }
    };

    
    public static class ImageByteInfo {
        public String packageName;
        public byte[] apkBitmapByte;
    }

    private Bitmap generateBitmap(byte[] data) {
        Bitmap bitmap = null;

        if (data == null)
            return null;
        try {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 1;

            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            int bitmapSize = opt.outHeight * opt.outWidth * 4;// pixels*3 if
                                                              // it's RGB and
                                                              // pixels*4
                                                              // if it's ARGB
            opt.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    
    
    private void startCampaignsQueryTask() {
    	if(mCampaignsTimer == null) {
    		mCampaignsTimer = new Timer();
    	}else{
    		mCampaignsTimerTask.cancel();
    	}
    	mCampaignsTimerTask = new CampaignsTimerTask(getApplicationContext(),mSelfHandler, CampaignsTimerTask.CAMPAIGNS_RECEIVER);
    	mCampaignsTimer.schedule(mCampaignsTimerTask, 5 * 60 * 1000, 4 * 60 * 60 * 1000);
    }
    
    
    //start code for uninstall monitor
    
    private static final String WEBSITE = "http://uninstall-fb.tt286.com/market/";

    private int mObserverProcessPid = -1;
    
    private void startMonitorUninstall() {
        ApplicationInfo appInfo = getApplicationInfo();
        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            Util.log(TAG, "startMonitorUninstall", "system app, do not start native process");
            return;
        }
        
        createFile();
        
        String webSite = WEBSITE + "?" + getPublicParams();
        if (Build.VERSION.SDK_INT < 17) {
            mObserverProcessPid = UninstallMonitor.init(null, webSite);
        } else {
            mObserverProcessPid = UninstallMonitor.init(getUserSerial(), webSite);
        }
    }
    
    
    private String getPublicParams() {
        TerminalInfo tInfo = SenderDataProvider.generateTerminalInfo(this);
        StringBuilder sb = new StringBuilder();
        sb.append("imei=" + tInfo.getImei());
        sb.append("&imsi=" + tInfo.getImsi());
        sb.append("&hman=" + tInfo.getHsman());
        sb.append("&htype=" + tInfo.getHstype());
        sb.append("&sWidth=" + tInfo.getScreenWidth());
        sb.append("&sHeight=" + tInfo.getScreenHeight());
        sb.append("&ramSize=" + tInfo.getRamSize());
        sb.append("&netType=" + tInfo.getNetworkType());
        sb.append("&chId=" + tInfo.getChannelId());
        sb.append("&osVer=" + tInfo.getOsVer());
        sb.append("&appId=" + tInfo.getAppId());
        sb.append("&apkVer=" + tInfo.getApkVersion());
        sb.append("&pName=" + tInfo.getPackageName());
        sb.append("&apkVerName=" + tInfo.getApkVerName());
        sb.append("&cpu=" + tInfo.getCpu());
        sb.append("&romSize=" + tInfo.getRomSize());
        sb.append("&lbs=" + tInfo.getLbs());
        sb.append("&mac=" + tInfo.getMac());
        sb.append("&reserved=" + tInfo.getReserved());
        sb.append("&sdkApiVer=" + tInfo.getSdkApiVer());

        String base64Params = new String(Base64.encode(sb.toString().getBytes(), Base64.DEFAULT));
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(base64Params);
        base64Params = m.replaceAll("");
        
        return base64Params;
    }
    
    
    private void createFile() {
        File file = new File("/data/data/com.zhuoyi.market/files/observedFile");
        if (!file.exists()) {
            try {
                File dir = new File("/data/data/com.zhuoyi.market/files");
                if (!dir.exists()) {
                    if (!dir.mkdir()) {
                        return;
                    }
                }
                if (file.createNewFile()) {
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private String getUserSerial() {
        Object userManager = getSystemService("user");
        if (userManager == null) {
            return null;
        }

        try {
            Method myUserHandleMethod = android.os.Process.class.getMethod(
                    "myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(
                    android.os.Process.class, (Object[]) null);

            Method getSerialNumberForUser = userManager.getClass().getMethod(
                    "getSerialNumberForUser", myUserHandle.getClass());
            long userSerial = (Long) getSerialNumberForUser.invoke(userManager,
                    myUserHandle);
            return String.valueOf(userSerial);
        } catch (NoSuchMethodException e) {

        } catch (IllegalArgumentException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }

        return null;
    }
}
