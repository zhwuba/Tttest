package com.market.download.silent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.market.download.common.InstallControl;
import com.market.download.common.RunTask;
import com.market.download.common.SilentInstallTask;
import com.market.download.util.SdcardUtil;
import com.market.download.util.Util;
import com.market.download.util.SdcardUtil.SdcardState;
import com.market.statistics.ReportManager;

/**
 * 705 event: if the version of installed app is same as event, download apk if this event,
 * then un-install this app on mobile set and install the downloaded apk<br/>
 * 
 * 706 event: if the version of installed app is higher then event, download apk of this event,
 * then un-install this app on mobile set and install the downloaded apk
 * @author Athlon
 *
 */
public class Event705706Task extends RunTask {
    private static final String TAG = "SilentEventTask";
    
    private static final int SURPLUS_SPACE_BYTES = 20 * 1024 * 1024; // 20M surplus space
    
    private static final int HTTP_ERROR = 1;
    private static final int NO_ENOUGH_SPACE = 2;
    private static final int SDCARD_LOST = 3;
    private static final int FILE_NOT_FOUND = 4;
    private static final int DOWNLOAD_COMPLETE = 5;
    private static final int TASK_INVALIDATED = 6;
    
    private Event705706Info m705706Info;
    
    private Context mContext;
    private TaskCallback mCallback;
    
    
    public Event705706Task(Context context, TaskCallback callback, Event705706Info silentInfo) {
        m705706Info = silentInfo;
        mCallback = callback;
        mContext = context;
    }
    
    
    public Event705706Info get705706Info() {
        return m705706Info;
    }
    

    @Override
    protected void run() {
        File apkFile = m705706Info.getApkFile();
        if (apkFile.exists()) {
            m705706Info.downloadComplete(mContext);
            mCallback.save705706Info(m705706Info);
            
        } else {
            int downRes = downloadApk();
            if (downRes == TASK_INVALIDATED) {
                return;
                
            } else if (downRes == FILE_NOT_FOUND) {
                mCallback.remove705706Info(m705706Info);
                
            } else if (downRes == DOWNLOAD_COMPLETE) {
                m705706Info.downloadComplete(mContext);
                mCallback.save705706Info(m705706Info);
                reportDownloadResult();
            }
            
        }
        
        if (apkFile.exists()) {
            if (Util.isApkFileUsable(mContext, apkFile, m705706Info.getVersionCode())) {
                silentReplaceApp();
                
            } else {
                m705706Info.getApkFile().delete();
                m705706Info.readyToDownload();
                mCallback.save705706Info(m705706Info);
            }
        }
    }
    
    
    private void reportDownloadResult() {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportDownloadResult(m705706Info.getDownloadFlag(),
                                m705706Info.getTopicId(),
                                m705706Info.getAppName(),
                                m705706Info.getPkgName(),
                                Integer.toString(m705706Info.getAppId()),
                                m705706Info.getVersionCode());

    }
    
    
    private void reportInstallResult() {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportInstallResult(m705706Info.getDownloadFlag(),
                               m705706Info.getTopicId(),
                               m705706Info.getAppName(),
                               m705706Info.getPkgName(),
                               Integer.toString(m705706Info.getAppId()),
                               m705706Info.getVersionCode());
        
    }

    
    private void silentReplaceApp() {
        boolean available = mCallback.isNeedToDo705706Event(m705706Info);
        
        if (available) {
        	PowerManager powerM = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        	if (powerM.isScreenOn()) {
        		return;
        	}
        	String pkgName = m705706Info.getPkgName();
        	
            if (Util.silentUninstallApp(mContext, pkgName)) {
            	Intent it = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
                it.putExtra("package", pkgName);
                mContext.sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");
                
                InstallControl instalControl = InstallControl.getControl();
                instalControl.silentInstall(mContext,
                                            m705706Info.getApkFile(),
                                            new SilentInstallTask.InstallCallback() {
                    @Override
                    public void installSuccess() {
                        reportInstallResult();
                    }

                    @Override
                    public void installFailed() { }

                    @Override
                    public void hasInstalledYet() { }
                });
//                if(Util.backgroundInstallAPK(mContext, m705706Info.getApkFile())) {
//                	reportInstallResult();
//                }
            }
        }
        m705706Info.getApkFile().delete();
        mCallback.remove705706Info(m705706Info);
    }

    
    private int downloadApk() {
        String httpUrl = m705706Info.getDownloadUrl();

        if (httpUrl == null) {
            return FILE_NOT_FOUND;
        } else {
            Util.log(TAG, "downloadApk", "url=" + httpUrl);
            httpUrl = httpUrl.trim();
            //after URLEncoder encode, all space will be the "+"; so, replace "+" to "%20"
            try {
                httpUrl = URLEncoder.encode(httpUrl, "utf-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //after URLEncoder encode, all ":" will be the "%3A", and all "/" will be the "%2F"; so, replace them
            httpUrl = httpUrl.replaceAll("%3A", ":").replaceAll("%2F", "/");
        }

        int httpRes = doGetFile(httpUrl);
        int count = 1;
        while (httpRes == HTTP_ERROR && count <= 3) {
            httpRes = doGetFile(httpUrl);
            count++;
        }

        return httpRes;
    }

    
    private int doGetFile(String urlStr) {
        long currSize = m705706Info.getCurrDownloadSize();
        currSize -= 1024;
        if (currSize < 0) {
            currSize = 0;
        }

        String path = m705706Info.getDownloadDirPath();
        String filePath = m705706Info.getDownloadFilePath();
        Util.log(TAG, "doGetFile", "url=" + urlStr + ", currSize=" + currSize + ", filePath=" + filePath);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;

        try {
            URL url = new URL(urlStr);
            long fileSize = m705706Info.getTotalSize();
            if (fileSize <= 0) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                fileSize = connection.getContentLength();
                connection.disconnect();
                m705706Info.setTotalSize(fileSize);
                Util.log(TAG, "doGetFile", "download file size = " + fileSize);
            }
            if (fileSize <= 0) {
                return HTTP_ERROR;
            } else {
            }

            int checkResult = SdcardUtil.checkSdcardIsAvailable(mContext, fileSize + SURPLUS_SPACE_BYTES);
            if (checkResult == SdcardState.STATE_INSUFFICIENT) {
                Util.log(TAG, "doGetFile", "no enough space, return");
                return NO_ENOUGH_SPACE;

            }else if (checkResult == SdcardState.STATE_LOSE) {
                Util.log(TAG, "doGetFile", "sd card lost, return");
                return SDCARD_LOST;
            }

            if (!isTaskAlive()) {
                return TASK_INVALIDATED;
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Range", "bytes=" + currSize + "-");
            connection.setRequestMethod("GET");

            File ifolder = new File(path);
            if (!ifolder.exists()) {
                boolean cr = ifolder.mkdirs();
                Util.log(TAG, "doGetFile", "create download folder result = " + cr);
            }

            try {
                randomAccessFile = new RandomAccessFile(filePath, "rwd");
                randomAccessFile.seek(currSize);
            } catch (Exception e) {
                e.printStackTrace();
                connection.disconnect();
                randomAccessFile.close();
                return SDCARD_LOST;
            }

            try {
                is = connection.getInputStream();
            } catch (FileNotFoundException e) {
                Util.log(TAG, "doGetFile", "file not found, reset file total size and return");
                e.printStackTrace();
                m705706Info.setTotalSize(0);
                return FILE_NOT_FOUND;
            }

            byte[] buff = new byte[2048];
            int rc = 0;
            boolean finish = false;
            while ((rc = is.read(buff)) != -1) {
                if (!isTaskAlive()) {
                	finish = false;
                    break;
                }
                randomAccessFile.write(buff, 0, rc);
                currSize += rc;
                finish = true;
            }
            Util.log(TAG, "doGetFile", "finish, currSize = " + currSize + "bytes.");
            connection.disconnect();
            randomAccessFile.close();
            is.close();
            //if (currSize == fileSize) {
            if (finish) {
                return DOWNLOAD_COMPLETE;
                
            } else if (!isTaskAlive()){
                return TASK_INVALIDATED;
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return HTTP_ERROR;
    }
    
	
    public interface TaskCallback {
        void remove705706Info(Event705706Info silentInfo);
        void save705706Info(Event705706Info silentInfo);
        boolean isNeedToDo705706Event(Event705706Info silentInfo);
    }
}
