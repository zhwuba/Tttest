package com.freeme.themeclub.theme.onlinetheme.download;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.R;
import com.freeme.themeclub.theme.onlinetheme.ThemeDownload;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class BackInstallService extends Service{

    private static DownloadManagerHelper mDownloadManagerHelper;

    private HashMap<Long, Integer> mIds = new HashMap<Long, Integer>();
    private HashMap<Long, Boolean> mTypes = new HashMap<Long, Boolean>();
    private static HashMap<Long, String> mApkPaths = new HashMap<Long, String>();
    private HashMap<Long, Integer> mStartIds = new HashMap<Long, Integer>();
    private Notification mNotificationTip;
    private CompleteReceiver mCompleteReceiver;

    public static String ACTION_START_INSTALL = "com.freeme.onlinetheme.download.BackInstallService.INSTALL_START";
    public static String ACTION_INSTALL_FAIL = "com.freeme.onlinetheme.download.BackInstallService.INSTALL_FALL";
    public static String ACTION_INSTALL_SUCCESS = "com.freeme.onlinetheme.download.BackInstallService.INSTALL_SUCCESS";

    Intent mIntent;
    @Override
    public void onCreate() {
        mDownloadManagerHelper = new DownloadManagerHelper((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
        super.onCreate();
        Log.w("yzy", "BackinstallAervice onCreate()");
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            stopSelf(startId);
            return START_NOT_STICKY;
        }
        mIntent=intent;
        long downloadId = intent.getLongExtra("downloadId", -1);
        if(downloadId==-1){
            downloadId=intent.getIntExtra("downloadId", -1);
        }
        int id = intent.getIntExtra("id", -1);
        boolean isOnlineLockscreen = intent.getBooleanExtra("isOnlineLockscreen", false);
        String apkPath = intent.getStringExtra("apkPath");
        if(downloadId != -1 && apkPath != null && id != -1){
            Log.w("yzy", "BackinstallAervice onStartCommand");
            mIds.put(downloadId, id);
            mTypes.put(downloadId, isOnlineLockscreen);
            mApkPaths.put(downloadId, apkPath);
            mStartIds.put(downloadId, startId);
            if(intent.getBooleanExtra("fromMain", false)){
                new InstallTask(downloadId).execute(new String[]{apkPath});
            }
        }else{
            stopSelf(startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //    class CompleteReceiver extends BroadcastReceiver {
    //        @Override
    //        public void onReceive(Context context, Intent intent) {
    //            long completeDownloadId = intent.getLongExtra(
    //                    DownloadManager.EXTRA_DOWNLOAD_ID, -1);
    //            Log.w("yzy", "DownloadManager.EXTRA_DOWNLOAD_ID="+completeDownloadId);
    //            if(mApkPaths.containsKey(completeDownloadId)){
    //                if(mDownloadManagerHelper.getStatusById(completeDownloadId) == DownloadManager.STATUS_SUCCESSFUL){
    //                    new InstallTask(completeDownloadId).execute(new String[]{mApkPaths.get(completeDownloadId)});
    //                }	        			        		
    //            }
    //        }
    //    };

    public class InstallTask extends AsyncTask<String, Void, Boolean>{

        long mDownloadId;
        String mApkPath;

        public InstallTask(long downloadId){
            super();
            Log.w("yzy", "BackinstallAervice InstallTask");
            mDownloadId = downloadId;
        }

        @Override
        protected Boolean doInBackground(String... arg0) {							
            updateDncunt(mIds.get(mDownloadId), mTypes.get(mDownloadId));
            Log.w("yzy", "BackinstallAervice doInBackground");
            mApkPath = arg0[0];
            String packageName = OnlineThemesUtils.getApkFileInfo(mApkPath, BackInstallService.this);				
            if(OnlineThemesUtils.checkInstalled(BackInstallService.this, packageName)){
                getContentResolver().delete(ThemeDownload.URI, ThemeDownload.DOWNLOAD_ID+"="+mDownloadId,null);
                deleteAPK(mApkPath);
                String where = MediaStore.Images.Media.DATA + "='" + mApkPath + "'"; 
                getContentResolver().delete(MediaStore.Files.getContentUri("external"), where, null);
                return false;
            }else{
                Intent startInstallIntent = new Intent(ACTION_START_INSTALL);
                startInstallIntent.putExtra("downloadId", mDownloadId);
                sendBroadcast(startInstallIntent);
                return OnlineThemesUtils.backgroundInstallAPK(mApkPath, BackInstallService.this);
            }				
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.v("yuan", "hasInstalled"+result);
            if(result){
                Intent successInstallIntent = new Intent(ACTION_INSTALL_SUCCESS);
                successInstallIntent.putExtra("downloadId", mDownloadId);
                sendBroadcast(successInstallIntent);

                int id = 256;
                PendingIntent contentIntent1;
                NotificationManager notificationManager;

                int index = mApkPath.lastIndexOf("/");
                String tempName=mApkPath.substring(index+1);

                notificationManager = (NotificationManager) BackInstallService.this.getSystemService(
                        BackInstallService.this.NOTIFICATION_SERVICE);
                if(mNotificationTip==null)
                    mNotificationTip = new Notification(R.drawable.status_icon, tempName + 
                            BackInstallService.this.getResources().getString(
                                    R.string.introduction_install_complete), 
                                    System.currentTimeMillis());
                mNotificationTip.tickerText = getRealName(tempName) + 
                        BackInstallService.this.getResources().getString(
                                R.string.introduction_install_complete);
                mNotificationTip.flags |= Notification.FLAG_AUTO_CANCEL;
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(BackInstallService.this, MainActivity.class);
                intent.putExtra("fromNotification", true);
                contentIntent1 = PendingIntent.getActivity(
                        BackInstallService.this, id,intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotificationTip.contentIntent = contentIntent1;
                mNotificationTip.setLatestEventInfo(
                        BackInstallService.this, 
                        getRealName(tempName)+ BackInstallService.this.getResources().getString(
                                R.string.introduction_install_complete),
                                BackInstallService.this.getResources().getString(
                                        R.string.introduction_click_to_see), contentIntent1);
                notificationManager.cancel(id);
                notificationManager.notify(id, mNotificationTip);
                getContentResolver().delete(ThemeDownload.URI, ThemeDownload.DOWNLOAD_ID+"="+mDownloadId,null);
                String where = MediaStore.Images.Media.DATA + "='" + mApkPath + "'"; 
                getContentResolver().delete(MediaStore.Files.getContentUri("external"), where, null);
                deleteAPK(mApkPath);
                Log.w("yzy", "successIntent");
            }else{
                Intent fallInstallIntent = new Intent(ACTION_INSTALL_FAIL);
                fallInstallIntent.putExtra("downloadId", mDownloadId);
                Log.w("yzy", "fallInstallIntent");
                sendBroadcast(fallInstallIntent);
            }

            if(mStartIds!=null&&mStartIds.size()!=0){
                stopSelf(mStartIds.get(mDownloadId));
                mStartIds.remove(mDownloadId);
            }
            if(mApkPaths!=null){
                mApkPaths.remove(mDownloadId);
            }

            super.onPostExecute(result);
        }

    }

    public static String getMimeType(final File file) {
        String extension = getExtension(file);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private static String getExtension(final File file) {
        String suffix = "";
        String name = file.getName();
        final int idx = name.lastIndexOf(".");
        if (idx > 0) {
            suffix = name.substring(idx + 1);
        }
        return suffix;
    }

    private static String getRealName(String wholeName) {
        String realName = wholeName;
        final int idx = wholeName.lastIndexOf(".apk");
        if (idx > 0) {
            realName = wholeName.substring(0, idx );
        }
        return realName;
    }

    private void deleteAPK(String path){
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
    }

    private void updateDncunt(int themeId, boolean isOnlineLockscreen){
        try{
            int msgCode;
            if(isOnlineLockscreen){
                msgCode = MessageCode.UPDATE_LOCKSCREEN_DNCNT_BY_TAG_REQ;
            }else{
                msgCode = MessageCode.UPDATE_THEME_DNCNT_BY_TAG_REQ;
            }
            JSONObject paraInfo = new JSONObject();
            paraInfo.put("id", themeId);			  

            JSONObject jsObject = new JSONObject();
            jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
            jsObject.put("body", paraInfo.toString());
            String contents = jsObject.toString();
            String url = MessageCode.SERVER_URL;
            String result = NetworkUtil.accessNetworkByPost(url, contents);
            Log.v("yuan", "result:"+result);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        //        unregisterReceiver(mCompleteReceiver);
        super.onDestroy();
    }
}
