package com.market.download.baseActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.market.view.CommonLoadingManager;
import com.market.download.service.DownloadService;
import com.market.download.service.IDownloadService;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.util.Util;
import com.zhuoyi.market.utils.LogHelper;

public abstract class DownloadBaseActivity extends Activity {
    public static final String TAG = "downBaseActivity";

    private IDownloadService mDownloadService;
    
    private boolean isBindService = false;
    
    private boolean addDd = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        mHandler = null;
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadService = IDownloadService.Stub.asInterface(service);
            Messenger messenger = new Messenger(mHandler);
            try {
                mDownloadService.setClientMsgHandler(messenger.getBinder());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            onDownloadServiceBind();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadService = null;
        }
    };
    
    
    /**
     * 子类需要在调用super.onResume()前调用
     * @param isShow
     */
    protected void isShowAD(boolean isShow) {
        addDd = isShow;
    }
    

    @Override
    protected void onResume() {
        if (addDd) {
        	CommonLoadingManager.get().showLoadingAnimation(this);
        }
        if (!isBindService) {
            bindService(new Intent(this, DownloadService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            isBindService = true;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if (mDownloadService != null) {
                mDownloadService.setClientMsgHandler(null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (isBindService) {
            try {
                unbindService(mConnection);
            } catch (Exception e) {
                LogHelper.trace(TAG+":"+e.toString());
            }
            isBindService = false;
        }
        mDownloadService = null;

        super.onPause();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            String infoStr = (String) msg.obj;
            Util.log(TAG, "handleMessage", "msg.what=" + what + ", info str:"
                    + infoStr);
            DownloadEventInfo eventInfo = new DownloadEventInfo(infoStr);
            switch (what) {
            case DownloadMsg.MSG_APK_DOWNLOADING:
                onApkDownloading(eventInfo);
                break;

            case DownloadMsg.MSG_NO_ENOUGH_SPACE:
                onNoEnoughSpace(eventInfo);
                break;

            case DownloadMsg.MSG_SDCARD_LOST:
                onSdcardLost(eventInfo);
                break;

            case DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR:
                onDownloadHttpError(eventInfo);
                break;

            case DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE:
                onDownloadProgressUpdate(eventInfo);
                break;

            case DownloadMsg.MSG_DOWNLOAD_COMPLETE:
                onDownloadComplete(eventInfo);
                break;

            case DownloadMsg.MSG_INSTALLING:
                onInstalling(eventInfo);
                break;

            case DownloadMsg.MSG_INSTALLED:
                onInstallSuccess(eventInfo);
                break;

            case DownloadMsg.MSG_INSTALL_FAILED:
                onInstallFailed(eventInfo);
                break;

            case DownloadMsg.MSG_FILE_NOT_FOUND:
                onFileNotFound(eventInfo);
                break;

            case DownloadMsg.MSG_FILE_NOT_USABLE:
                onFileNotUsable(eventInfo);
                break;

            default:
                super.handleMessage(msg);
            }
        }
    };

    /**
     * This function will be called when activity binded the download service,
     * you can do what you want in it.
     * 
     */
    protected abstract void onDownloadServiceBind();

    /**
     * For add download apk event. If return true, means the download service is
     * already bind by activity, else not.
     * 
     * @param pkgName
     *            The package name about download apk
     * @param appName
     *            The application name about download apk, use to save the apk
     *            file name.
     * @param md5
     *            The apk file md5 code, for judge the apk file if there is
     *            already exist
     */
    public boolean addDownloadApk(final String pkgName, final String appName,
            final String md5, final String url, final String topicId, final String flag,
            final int verCode, final int appId, final long totalSize) throws RemoteException {
        boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.addDownload(pkgName, appName, md5, url, topicId, flag,
                    verCode, appId, totalSize);
            result = true;
        }
        return result;
    }

    /**
     * For add download apk event without notify ready toast. If return true,
     * means the download service is already bind by activity, else not.
     * 
     * @param pkgName
     *            The package name about download apk
     * @param appName
     *            The application name about download apk, use to save the apk
     *            file name.
     * @param md5
     *            The apk file md5 code, for judge the apk file if there is
     *            already exist
     */
    public boolean addDownloadApkWithoutNotify(final String pkgName,
            final String appName, final String md5, final String url,
            final String topicId, final String flag, final int verCode,
            final int appId, final long totalSize)
            throws RemoteException {
        boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.addDownloadWithoutNotifyReady(pkgName, appName,
                    md5, url, topicId, flag, verCode, appId, totalSize);
            result = true;
        }
        return result;
    }
    
    
    public boolean addDiffDownload(final String pkgName,
            final String appName, final String md5, final String url,
            final String topicId, final String flag, final int verCode,
            final int appId, final String diffDownUrl, final long totalSize,
            final long diffPatchSize)
            throws RemoteException {
    	boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.addDiffDownload(pkgName, appName,
                    md5, url, topicId, flag, verCode, appId, diffDownUrl, totalSize, diffPatchSize);
            result = true;
        }
        return result;
    }
    

    /**
     * For start download apk. If return true, means the download service is
     * already bind by activity, else not.
     * 
     * @param pkgName
     *            The package name about download apk
     */
    public boolean startDownloadApk(final String pkgName, final int verCode)
            throws RemoteException {
        boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.startDownload(pkgName, verCode);
            result = true;
        }
        return result;
    }

    /**
     * For cancel download apk. The file which is downloading will be delete. If
     * return true, means the download service is already bind by activity, else
     * not.
     * 
     * @param pkgName
     *            The package name about canceled apk.
     */
    public boolean cancelDownloadApk(final String pkgName, final int verCode, boolean delFile)
            throws RemoteException {
        boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.cancelDownload(pkgName, verCode, delFile);
            result = true;
        }
        return result;
    }

    /**
     * For pause download apk. If return true, means the download service is
     * already bind by activity, else not.
     * 
     * @param pkgName
     *            The package name about paused apk.
     */
    public boolean pauseDownloadApk(final String pkgName, final int verCode)
            throws RemoteException {
        boolean result = false;
        if (mDownloadService != null) {
            mDownloadService.pauseDownload(pkgName, verCode);
            result = true;
        }
        return result;
    }

    /**
     * For get current downloading number
     */
    public int getDownloadingNum() throws RemoteException {
        int number = -1;
        if (mDownloadService != null) {
            number = mDownloadService.getDownloadingNumber();
        }
        return number;
    }
    
    public void ignoreUpdate(String pkgName, int verCode) throws RemoteException {
    	if (mDownloadService != null) {
    		mDownloadService.ignoreUpdate(pkgName, verCode);
    	}
    }

    /**
     * This function will be called when a apk start download, you can do what
     * you want in it.
     * 
     * @param eventInfo
     *            The downloading event info.
     */
    protected abstract void onApkDownloading(DownloadEventInfo eventInfo);

    /**
     * This function will be called if there is not enough space in sdcard when
     * a apk start download, you can do what you want in it.
     * 
     * @param eventInfo
     *            The download event info.
     */
    protected abstract void onNoEnoughSpace(DownloadEventInfo eventInfo);

    /**
     * This function will be called if sdcard is not be found when a apk start
     * download, you can do what you want in it.
     * 
     * @param eventInfo
     *            The download event info.
     */
    protected abstract void onSdcardLost(DownloadEventInfo eventInfo);

    /**
     * This function will be called if http error happened when apk download,
     * you can do what you want in it.
     * 
     * @param eventInfo
     *            The download event info.
     */
    protected abstract void onDownloadHttpError(DownloadEventInfo eventInfo);

    /**
     * This function will be called when a apk download progress update, you can
     * update the download progress in it.
     * 
     * @param eventInfo
     *            The download event info.
     */
    protected abstract void onDownloadProgressUpdate(DownloadEventInfo eventInfo);

    /**
     * This function will be called when a apk download complete, you can do
     * what you want in it.
     * 
     * @param eventInfo
     *            The download event info.
     */
    protected abstract void onDownloadComplete(DownloadEventInfo eventInfo);

    /**
     * This function will be called when the apk file start to install, you can
     * do what you want in it.
     * 
     * @param eventInfo
     *            the installing event info
     */
    protected abstract void onInstalling(DownloadEventInfo eventInfo);

    /**
     * This function will be called when the apk file install success, you can
     * do what you want in it.
     * 
     * @param eventInfo
     *            the installed event info
     */
    protected abstract void onInstallSuccess(DownloadEventInfo eventInfo);

    /**
     * This function will be called when the apk file install failed, you can do
     * what you want in it.
     * 
     * @param eventInfo
     *            the install event info
     */
    protected abstract void onInstallFailed(DownloadEventInfo eventInfo);

    /**
     * This function will be called when the apk file not found in server when
     * start download connection, you can do what you want in it.
     * 
     * @param eventInfo
     *            the install event info
     */
    protected abstract void onFileNotFound(DownloadEventInfo eventInfo);

    /**
     * This function will be called when the apk file which download complete is
     * not usable, you can do what you want in it.
     */
    protected abstract void onFileNotUsable(DownloadEventInfo eventInfo);
}
