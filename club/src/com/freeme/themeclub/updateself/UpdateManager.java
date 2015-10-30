package com.freeme.themeclub.updateself;

import android.content.Context;
import android.content.Intent;

public class UpdateManager {
    public static final String TAG = "DownManager";

    private UpdateSelfService mUpdateService;
    private HttpManager mHttpManager;

    private static UpdateManager mSelf;

    UpdateManager(UpdateSelfService service) {
        mUpdateService = service;
        mHttpManager = new HttpManager(service);

    }

    public static UpdateManager getInstance(UpdateSelfService service) {
        if (mSelf == null) {
            mSelf = new UpdateManager(service);
        }
        return mSelf;
    }

    public static void updateServiceStartDown(Context context) {
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_START_DOWNLOAD);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    public static void updateServiceResumeDown(Context context) {
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_RESUME_DOWNLOAD);
        intent.setPackage(context.getPackageName());

        context.startService(intent);
    }

    public static void updateServiceQueryNew(Context context) {
        if (Util.getDownloadInfo(context) != null
                && Util.getDownloadInfo(context).state == DownloadInfo.STATE_DOWNLOADING)
            return;
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_QUERY_UPDATE);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    public static void updateServiceDataTraffic(Context context) {
        Intent intent = new Intent(context, UpdateSelfService.class);
        intent.putExtra(UpdateSelfService.START_FLAG, UpdateSelfService.MSG_IGNORE_DATA_TRAFFIC);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
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

    public int queryUpdate() {
        return mHttpManager.queryUpdate();
    }

    public int downloadUpdate(DownloadInfo downInfo) {
        return mHttpManager.downloadUpdate(downInfo);
    }
}
