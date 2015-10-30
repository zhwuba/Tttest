package com.market.updateSelf;

import android.content.Context;
import android.content.Intent;

import com.zhuoyi.market.constant.Constant;
import com.market.download.userDownload.DownloadManager;
import com.market.statistics.ReportFlag;

public class UpdateManager {
    public static final String TAG = "DownManager";
    private HttpManager mHttpManager;

    private static UpdateManager mSelf;

    UpdateManager(UpdateSelfService service) {
        mHttpManager = new HttpManager(service);

    }

    public static UpdateManager getInstance(UpdateSelfService service) {
        if (mSelf == null) {
            mSelf = new UpdateManager(service);
        }
        return mSelf;
    }

    
    /**
     * 在市场下载管理下载自更新文件
     * @param context
     * @param url
     * @param md5
     * @param versionCode
     */
    public static void startDownloadUpdate(Context context, String url, String md5, int versionCode, long totalSize) {
        String appName = SelfUpdateInfo.FILE_NAME_PRE + md5 + "_" + versionCode;
        DownloadManager.startServiceAddEvent(context, url,
                context.getPackageName(), appName, md5, ReportFlag.TOPIC_NULL,
                ReportFlag.FROM_SELF_UPDATE, versionCode,
                Constant.CP_ID.hashCode(), totalSize);
    }
    
    /**
     * 后台下载自更新文件，没有root权限状态栏提醒用户安装
     * @param context
     */
    public static void updateServiceStartDown(Context context) {

        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_START_DOWNLOAD);
        context.startService(intent);
    }
    
    /**
     * 继续下载
     * @param context
     */
    public static void updateServiceResumeDown(Context context) {
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_RESUME_DOWNLOAD);
        context.startService(intent);
    }

    /**
     * 安装：退出市场，前台下载完成
     * @param context
     */
    public static void installSelfUpdateApk(Context context) {
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_START_INSTALL);
        context.startService(intent);
    }

    /**
     * 下载
     * @param downInfo
     * @return
     */
    public int downloadUpdate(SelfUpdateInfo downInfo) {
        return mHttpManager.downloadUpdate(downInfo);
    }

    
    public static class DownloadRes {
        public static final int HTTP_ERROR = 0;
        public static final int DOWNLOAD_COMPLETE = 1;
        public static final int SDCARD_LOST = 2;
        public static final int NO_ENOUGH_SPACE = 3;
    }

    
    public static class QueryRes {
        public static final int FOUND_NEW = 0;
        public static final int NO_NEW = 1;
        public static final int HTTP_ERROR = 2;
    }



}
