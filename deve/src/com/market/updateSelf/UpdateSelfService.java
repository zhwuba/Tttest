package com.market.updateSelf;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import com.market.updateSelf.UpdateManager.DownloadRes;
import com.zhuoyi.market.R;
import com.zhuoyi.market.manager.MarketNotificationManager;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class UpdateSelfService extends Service {
    
    public static final String TAG = "updateService";
    public static final String START_FLAG = "startFlag";
    
    public static final int MSG_RESUME_DOWNLOAD = 1;
    public static final int MSG_START_DOWNLOAD = 2;
    public static final int MSG_START_INSTALL = 3;
    
    private UpdateManager mDownManager;
    private Thread mDownThread;
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownManager = UpdateManager.getInstance(this);   
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int startFlag = intent.getIntExtra(START_FLAG, -1);
            if (startFlag != -1)
                mHandler.sendEmptyMessage(startFlag);
        }

        return super.onStartCommand(intent, flags, startId);
    }



    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	UpdateUtil.logI(TAG, "handleMessage", "msg what is:" + msg.what);
            switch (msg.what) {
            case MSG_RESUME_DOWNLOAD:
            case MSG_START_DOWNLOAD:
                startDownNewUpdate();
                break;
            case MSG_START_INSTALL:
                //退出市场，前台下载完成，直接安装
            	new Thread() {
            		@Override
            		public void run () {
            			super.run();
            			SelfUpdateInfo downInfo = UpSelfStorage.getSelfUpdateInfo(UpdateSelfService.this);
                        if (downInfo != null) {
                            installNewVersion(downInfo, false, false, true);
                        }
                        UpdateSelfService.this.stopSelf();
            		}
            	}.start();
                
                break;
            }
        }
    };

    /**
     * 安装应用
     * @param downInfo 安装信息
     * @param tipInStatusBar 状态栏提示，针对后台
     * @param tipWithTime 按时间提醒
     * @param tipInstall 弹出框提示：针对前台
     */
        
    private void installNewVersion(SelfUpdateInfo downInfo, boolean tipInStatusBar, boolean tipWithTime, boolean tipInstall) {
        //判断是否需要更新版本
        boolean needInstall = false;
        try {
            int currVerCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            if (currVerCode < downInfo.getVersionCode()) {
                needInstall = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //需要更新版本，处理后台操作和前台操作
        if (needInstall) {
            if (tipInstall) {
                //弹出安装界面来提示
                installApkFile(downInfo, false, tipWithTime, true);
            } else {
                if (UpdateUtil.isSelfAppForegound(UpdateSelfService.this)) {
                    return;
                } else {
                    //如果应用在后台，状态栏提示安装
                    if (tipInStatusBar) {
                        installApkFile(downInfo, true, tipWithTime, false);
                    }
                } 
            }
        }
    }

    /**
     * 从本地获取更新数据
     */
    private void startDownNewUpdate() {
    	SelfUpdateInfo downInfo = UpSelfStorage.getSelfUpdateInfo(UpdateSelfService.this);
    	if (downInfo == null) {
    	    UpdateSelfService.this.stopSelf();
    	    return;
    	}
        startDownNewUpdate(downInfo, true, false);
    }

    /**
     * 传递更新数据
     * @param downInfo
     * @param isBg
     * @param statusBarTip
     */
    private void startDownNewUpdate(SelfUpdateInfo downInfo, boolean statusBarTip, boolean tipWithTime) {
        
        File apkFile = downInfo.getApkFile();
        if (apkFile.exists()) {
            String fileMd5 = UpdateUtil.getFileMd5(apkFile.getAbsolutePath());
            if (fileMd5.equals(downInfo.getMd5())) {
                installNewVersion(downInfo, statusBarTip, tipWithTime, false);
                UpdateSelfService.this.stopSelf();
                return;
            }
        }

        downloadUpdate(downInfo, statusBarTip, tipWithTime);
    }
    
    /**
     * 后台下载并安装自更新包
     * @param downInfo
     * @param statusBarTip
     */
    private void downloadUpdate(final SelfUpdateInfo downInfo, final boolean statusBarTip, final boolean tipWithTime) {
        
        if (mDownThread != null && mDownThread.isAlive()) {
            UpdateSelfService.this.stopSelf();
            return;
        }

        mDownThread = new Thread() {
            @Override
            public void run() {
                downInfo.setDownloadState(UpdateSelfService.this, SelfUpdateInfo.STATE_DOWNLOADING);
                int result = mDownManager.downloadUpdate(downInfo);
                if (result == DownloadRes.DOWNLOAD_COMPLETE) {
                    downInfo.setDownloadState(UpdateSelfService.this, SelfUpdateInfo.STATE_COMPLETE);
                    installNewVersion(downInfo, statusBarTip, tipWithTime, false);
                } else if (result == DownloadRes.HTTP_ERROR
                        || result == DownloadRes.NO_ENOUGH_SPACE
                        || result == DownloadRes.NO_ENOUGH_SPACE
                        || result == DownloadRes.SDCARD_LOST) {
                    downInfo.setDownloadState(UpdateSelfService.this, SelfUpdateInfo.STATE_PAUSED);
                }
                
                UpdateSelfService.this.stopSelf();
            }
        };
        mDownThread.start();
    }
    
    /**
     * 安装自更新应用
     * @param downInfo 自更新应用信息
     * @param tipInStatusBar 如果没有root权限，是否在状态栏提示
     * @param tipWithTime 如果没有root权限，是否按照时间来提示用户：当天提示过，不再提醒
     * @param tipInstall 如果没有root权限，是否弹出安装框
     */
    private void installApkFile(SelfUpdateInfo downInfo, boolean tipInStatusBar, boolean tipWithTime, boolean tipInstall) {
        File apkFile = downInfo.getApkFile();
        String fileMd5 = UpdateUtil.getFileMd5(apkFile.getAbsolutePath());
        if (fileMd5 == null || !fileMd5.equals(downInfo.getMd5())) {
        	UpdateUtil.logE(TAG, "installApkFile", "apk file incorrect, file md5: " + fileMd5);
        	UpdateUtil.logE(TAG, "installApkFile", "apk file incorrect, right md5:" + downInfo.getMd5());
            return;
        }

        Intent it = new Intent("android.intent.action.INSTALL_APK_QUIETLY");
        it.putExtra("package", "com.zhuoyi.market");
        sendBroadcast(it);
        
        //后台安装：成功返回true，失败返回false
        boolean installRes = UpdateUtil.backgroundInstallAPK(apkFile, UpdateSelfService.this);
        
        //后台安装失败，说明无root权限，改成提示用户安装
        boolean install = false;
        if (!installRes) {
            if (tipWithTime) {
                install = isTimeToPopInstall();
            } else {
                install = true;
            }
        } else {
            return;
        }
        
        //如果提示安装，根据提示方式，来展示给用户：弹出安装界面，状态栏提示
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        if (install) {
            if (tipInstall) {
                startActivity(intent); 
            } else if (tipInStatusBar) {
                MarketNotificationManager manager = MarketNotificationManager.get();
                String btnStr = this.getString(R.string.notify_tip_user_install);
                manager.notifyWifiUpdte(""+downInfo.getVersionCode(), downInfo.getTitle(), downInfo.getContent(), btnStr, intent);
            }
            
            //保存提示用户的当天时间
            if (tipInstall || tipInStatusBar) {
                UpSelfStorage.setPopInstallTime(UpdateSelfService.this, getCurrTimeStr());
            }
        }
    }

    
    /**
     * 是否到更新提示时间，同一天不提示第二次，针对无root权限用户
     * @return
     */
    private boolean isTimeToPopInstall() {
        String lastPopTime = UpSelfStorage.getLastPopInstallTime(UpdateSelfService.this);
        String currTime = getCurrTimeStr();
        if (lastPopTime != null && lastPopTime.equals(currTime)) {
            return false;
        } else {
            return true;
        }
    }
    

    /**
     * 获取当前日期
     * @return
     */
    private String getCurrTimeStr() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int day = now.get(Calendar.DAY_OF_YEAR);
        return Integer.toString(year) + Integer.toString(day);
    }
}
