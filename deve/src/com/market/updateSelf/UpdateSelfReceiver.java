package com.market.updateSelf;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;

public class UpdateSelfReceiver extends BroadcastReceiver {
    public static final String TAG = "UpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        UpdateUtil.logE(TAG, "onReceive", "receive action:" + action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            
            if (SystemClock.elapsedRealtime() < 5 * 60 * 1000) {
                return;
            }
            
            if (UpdateUtil.isCurrNetworkAvailable(context)) {
            	SelfUpdateInfo info = UpSelfStorage.getSelfUpdateInfo(context);
                if (info != null) {
                    if (info.getUpdateType() == SelfUpdateInfo.SELF_UPDATE_TYPE_4) {
                        UpdateManager.updateServiceStartDown(context);
                    } else if (info.getUpdateType() == SelfUpdateInfo.SELF_UPDATE_TYPE_1
                            || info.getUpdateType() == SelfUpdateInfo.SELF_UPDATE_TYPE_2) {
                        
                        if (!UpdateUtil.isSelfAppForegound(context)) {
                            SelfUpdateManager update = SelfUpdateManager.get(context);
                            update.setLocalData(info.getTitle(), 
                                    info.getContent(), 
                                    info.getUpdateType(), 
                                    info.getVersionCode(), 
                                    info.getDownloadUrl(), 
                                    info.getMd5());
                            update.setTipType(SelfUpdateManager.SELF_UPDATE_TIP_TYPE_1);
                            update.showUpdateTip();
                        }
                    }
                } else {
                    //wifi情况下检查是否有最新版本
                    if (UpdateUtil.isCurrWifiAvailable(context)) {
                        SelfUpdateManager update = SelfUpdateManager.get(context);
                        update.setTipType(SelfUpdateManager.SELF_UPDATE_TIP_TYPE_1);
                        update.selfUpdateRequest(SelfUpdateManager.SELF_UPDATE_REQ_FROM_WIFI_CHANGE);
                    }
                }
            }
        }
    }
}
