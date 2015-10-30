package com.freeme.themeclub.updateself;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class UpdateSelfReceiver extends BroadcastReceiver {
    public static final String TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (Util.isCurrWifiAvailable(context)) {
                DownloadInfo info = Util.getDownloadInfo(context);
                if (info != null && info.state != DownloadInfo.STATE_COMPLETE) {
                    Util.logE(TAG, "onReceive", "receive action CONNECTIVITY_ACTION,info.state = " + info.state);
                    if (info.state == DownloadInfo.STATE_READY) {
                        UpdateManager.updateServiceStartDown(context);
                    } else {
                        UpdateManager.updateServiceResumeDown(context);
                    }
                }
            }
        }
    }

}
