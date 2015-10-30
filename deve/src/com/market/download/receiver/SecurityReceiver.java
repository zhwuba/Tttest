package com.market.download.receiver;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;

import com.market.behaviorLog.UserLogSDK;
import com.market.download.common.DownloadSettings;
import com.market.download.common.DownloadSettings.WifiDownConfig;
import com.market.download.service.DownloadService;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.NetworkType;
import com.market.download.util.NotifyUtil;
import com.market.download.util.Util;
import com.zhuoyi.market.utils.CrashHandler;

public class SecurityReceiver extends BroadcastReceiver {
    public static final String TAG = "SecurityReceiver";

    private static final long PASS_NETWORK_MILLIS = 5 * 60 * 1000;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Util.log(TAG, "onReceive", "DownloadReceiver action:" + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, DownloadService.class));

        } else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetworkType.isNetworkAvailable(context)) {
                DownloadManager.startServiceNetworkDisconnect(context);
                return;
            }
            
            if (SystemClock.elapsedRealtime() <= PASS_NETWORK_MILLIS) {
                return;
            }
            
            DownloadManager.startServiceAutoContinue(context);
            sendCrashFiles(context); // 发送异常文件
            
            WifiDownConfig config = DownloadSettings.getWifiDownConfig(context);
            long currMillis = System.currentTimeMillis();
            if (config.expireMillis == 0 || config.expireMillis <= currMillis) {
                DownloadManager.startServiceRequestWifiAutoDownArray(context);
            }

            if (NetworkType.isWifiAvailable(context)) {
                if (DownloadSettings.isTimeToGetUpdate(context)) {
                    NotifyUtil.mReqUpdateTime = System.currentTimeMillis();
                    DownloadService.getAppUpdateList(context);
                }
                
                UserLogSDK.wifiAvailabled(context);
            }

        }
    }

    public void sendCrashFiles(final Context context) {
        File file = new File(CrashHandler.SD_CARD_PATH + CrashHandler.LOG_DIR);
        if (!file.exists() || !file.isDirectory()) {
            return;
        } else {
            new Thread(new Runnable() {

                @Override
                public void run() {
                	try {
                		CrashHandler.getInstance().sendCrashReportsToServer(context);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                }
            }).start();

        }
    }
}
