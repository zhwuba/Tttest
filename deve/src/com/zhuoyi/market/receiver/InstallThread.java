package com.zhuoyi.market.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.utils.MarketUtils;

public class InstallThread extends Thread
{
  public static final int APP_INSTALL_EXTERNAL = 2;
  public static final int APP_INSTALL_INTERNAL = 1;
  String filePath;
  String packageName;
  boolean result = false;
  String apkName;
  long pid;
  Context mContext;
  Notification mNotificationTip;

  public InstallThread(Context context,String path, String packageName,long arg3)
  {
    this.filePath = path;
    this.packageName = packageName;
    mContext = context;
    pid = arg3;
  }

  public void run()
  {
  	int id = 256;
  	String installPackageMd5;
  	String apkFileMd5;
  	String platform = Build.HARDWARE;
  	String tempName="";
  	int index;
  	int end;
  	PendingIntent contentIntent1;
  	NotificationManager notificationManager;
		
 	if(filePath.contains("/kedou/"))
 	{
 		filePath = filePath.replace("/kedou/", "/ZhuoYiMarket/");
 	}
  
  	notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
  	if(platform.contains("sp")||platform.contains("SP"))
  	{
  		result = false;
  	}
  	else
  	{
  		Intent it = new Intent("android.intent.action.INSTALL_APK_QUIETLY");
  		it.putExtra("package",packageName);
  		mContext.sendBroadcast(it);
  		
  		result = MarketUtils.backgroundInstallAPK(filePath,mContext);
  	}
  	
  
  	if(pid == 1)
  	    return;
  	
  	index = filePath.lastIndexOf("/");
	apkName = filePath.substring(index+1);
	
	if(apkName.endsWith(".apk"))
	{
		end = apkName.length()-4;
		tempName = apkName.substring(0, end);
	}
  	if(result==false && MarketUtils.checkInstalled(packageName,mContext))
  	{
  		installPackageMd5 = MarketUtils.getInstalledPackageSignatureMD5(packageName, mContext);
  		apkFileMd5 = MarketUtils.getApkSignatureByFilePath(mContext,filePath);
  		if(!installPackageMd5.equals(apkFileMd5))
  		{
  			String temp_string = (filePath+","+apkName+","+packageName);
            Intent intent2 = new Intent("com.zhuoyi.MyDialogActivity");
            intent2.putExtra("dialogParam", temp_string);
            intent2.putExtra("myCustomType", 1);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent2);
  		}
  		else
  			MarketUtils.AppInfoManager.AppInstall(filePath, mContext, "", "");
  	}
  	else if(result == false)
  	{
  		MarketUtils.AppInfoManager.AppInstall(filePath, mContext, "", "");
  	}
  	else
  	{
  		if(mNotificationTip==null)
      		mNotificationTip = new Notification(R.drawable.status_icon, tempName + mContext.getResources().getString(R.string.introduction_install_complete), System.currentTimeMillis());
  		mNotificationTip.tickerText = tempName + mContext.getResources().getString(R.string.introduction_install_complete);
  		mNotificationTip.flags |= Notification.FLAG_AUTO_CANCEL;
  		Intent intent = new Intent();
  		intent.setClass(mContext, Splash.class);
  		contentIntent1 = PendingIntent.getActivity(mContext, id,intent, PendingIntent.FLAG_UPDATE_CURRENT);
  		mNotificationTip.contentIntent = contentIntent1;
  		
  		mNotificationTip.setLatestEventInfo(mContext, tempName+ mContext.getResources().getString(R.string.introduction_install_complete), "", contentIntent1);
  		notificationManager.cancel(id);
  		notificationManager.notify(id, mNotificationTip);
  		notificationManager.cancel(id);
  	}

  }
}
