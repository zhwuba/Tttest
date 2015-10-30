package com.zhuoyi.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.widget.Toast;

import com.market.account.constant.Constant;
import com.market.featureOption.FeatureOption;
import com.zhuoyi.market.asyncTask.MarketStartUpAsyncTask;
import com.zhuoyi.market.asyncTask.TaskManager;
import com.zhuoyi.market.utils.MarketUtils;

public class MarketReceiver extends BroadcastReceiver  {

	private final String mInstallAction = "com.zhuoyi.market.install.apk"; 
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if(intent.getAction().equals(mInstallAction))  {
            String path = intent.getStringExtra("file_path");
            String pName = intent.getStringExtra("packageName");
            if(!TextUtils.isEmpty(path) && !TextUtils.isEmpty(pName))
                new InstallThread(context,path,pName,1).start();
            
        } else if(intent.getAction().equals("com.zhuoyi.market.silent.uninstall"))	{
		    String pName = intent.getStringExtra("packageName");
		    MarketUtils.unInstallSilent(pName);
		    
		} else if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
			if(info != null && info.isAvailable() ) {
				if(info.getType() == ConnectivityManager.TYPE_WIFI) {
				    MarketUtils.setWifiState(true);
				    Constant.SHOW_IMAGE = true;	// web页同步无图模式
				    
				    if (SystemClock.elapsedRealtime() > 5 * 60 * 1000) {
				        MarketStartUpAsyncTask marketStartUpAsyncTask = new MarketStartUpAsyncTask(context, MarketUtils.FileManage.getSDPath(), "startUpImage");
				        TaskManager.getInstance().startTask(marketStartUpAsyncTask, true);
                    }
				} else {
				    MarketUtils.setWifiState(false);
				    Constant.SHOW_IMAGE = false;	// web页同步无图模式
				}
//				Log.d("mydebug", "当前网络名称：" + name);
			} else {
//				Log.d("mydebug", "没有可用网络");
			}
		}
	}
}


