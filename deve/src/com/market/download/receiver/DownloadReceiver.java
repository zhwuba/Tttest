package com.market.download.receiver;


import java.io.File;
import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.zhuoyi.market.R;
import com.market.download.userDownload.DownStorage;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.AsyncImageCache;

public class DownloadReceiver extends BroadcastReceiver {
    public static final String TAG = "DownReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Util.log(TAG, "onReceive", "DownloadReceiver action:" + action);
        if (action.equals("com.zhuoyi.market.extern.download")) {
            String pkgName = intent.getStringExtra("packageName");
            String apkName = intent.getStringExtra("apkName");
            String md5 = intent.getStringExtra("md5");
            String url = intent.getStringExtra("url");
            String from = intent.getStringExtra("from");
            String topicId = intent.getStringExtra("topicId");
            String imageUrl = intent.getStringExtra("imageUrl");
            String receiverPkg = intent.getStringExtra("callApp");
            String receiverClass = intent.getStringExtra("receivName");
            
            if (topicId == null) {
            	topicId = ReportFlag.TOPIC_NULL;
            }
            if (from == null) {
            	from = ReportFlag.FROM_EXTRA_DOWN;
            }
            
            String appUrl = intent.getStringExtra("app_url");
            int appId = intent.getIntExtra("appId", 0);
            int verCode = intent.getIntExtra("verCode", 0);
           
            /**	下载icon 图标	**/
            AsyncImageCache imageCache = AsyncImageCache.from(context);
            if (imageCache != null && !TextUtils.isEmpty(imageUrl)) {
            	imageCache.displayImage(new AsyncImageCache.NetworkImageGenerator(pkgName, imageUrl), from);
            }
            /**	end	**/
            DownloadManager.startServiceAddExterEvent(context,
                                                      url,
                                                      pkgName,
                                                      apkName,
                                                      md5,
                                                      topicId,
                                                      from,
                                                      verCode,
                                                      appId,
                                                      receiverPkg,
                                                      receiverClass);
            
            if(!TextUtils.isEmpty(appUrl)) {
            	WebAppDao webAppDao = new WebAppDao(context);
            	if(!TextUtils.isEmpty(webAppDao.getWebUrl(appId))) {
            		webAppDao.removeWebAppInfo(appId);
            	}
            	webAppDao.saveWebAppInfo(appId, appUrl);
            }
            
        } else if (action.equals("com.zhuoyi.market.download.fileNotFoundInServer")) {
            String appName = intent.getStringExtra("appName");

            Toast.makeText(context, R.string.no_connect_hint, Toast.LENGTH_SHORT).show();

        } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            DownloadManager.startServiceForAppInstalled(context, packageName);
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            final String fileName = DownStorage.getSignCheckInstallInfo(context, packageName);
            if (!TextUtils.isEmpty(fileName)) {
                final Context installContext = context;
                new Thread() {
                    @Override
                    public void run() {
                    File f = new File(fileName);
                    if (f == null || !f.exists()) return;
                    Intent intentInstall = new Intent(Intent.ACTION_VIEW);
                    intentInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentInstall.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
                    installContext.startActivity(intentInstall);
                    }
                }.start();
            }
            
        } else if (action.equals("com.zhuoyi.market.download.fileNotUsable")) {
            String appName = intent.getStringExtra("appName");
            String notiStr = context.getString(R.string.down_noti_ticker_file_not_usable, appName);
            Toast.makeText(context, notiStr, Toast.LENGTH_LONG).show();
        } else if (action.equals("com.zhuoyi.market.download.viewAppDetail")) {
            int apkId = intent.getIntExtra("apkId", -1);
            if (apkId == -1) {
                return;
            }
            boolean autoDownload = intent.getBooleanExtra("autoDownload", false);
            String from = intent.getStringExtra("from");

            Intent detailIntent = new Intent("com.zhuoyi.appDetailInfo");
            detailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailIntent.putExtra("refId", apkId);
            detailIntent.putExtra("autoDownload", autoDownload);
            detailIntent.putExtra("from", from);

            context.startActivity(detailIntent);
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
        	NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        	if (info == null) {
        		return;
        	}
        	
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
            	Util.log(TAG, "onReceive", "wifi disconnected");
            	DownloadManager.startServiceNetworkDisconnect(context);
            } else if(info.getState().equals(NetworkInfo.State.CONNECTED)){
//            	WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
//            	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//            	Util.log(TAG, "onReceive", "wifi connected: " + wifiInfo.getSSID());
                //nothing now
            }
        } else if ("com.zhuoyi.market.installed".equals(action)) {
        	String pkgName = intent.getStringExtra("pkg_name");
            int notifyId = intent.getIntExtra("notify_id", 0);
            doInstalled(context, pkgName, notifyId);
        }

    }
    
    
    private void doInstalled(Context context, String pkgName, int notifyId) {
        if (!TextUtils.isEmpty(pkgName)) {
        	try {
	            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
	            if (intent == null) {
	                intent = new Intent(pkgName);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            }
	            context.startActivity(intent);
        	} catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (0 != notifyId) {
            try {
            	MarketNotificationManager.get().cancel(notifyId);
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse = null;

            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

}
