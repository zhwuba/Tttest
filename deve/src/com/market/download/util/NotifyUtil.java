package com.market.download.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.market.net.data.AppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;

public class NotifyUtil {
	
	
    public static final int UPDATE_TIPS_ID = 11;
    
    private static int mDownloadNum = 0;
    
    public static boolean mNeedTip = true;
    public static long mReqUpdateTime = 0;
    private static final int UPDATE_TIPS_ALL_MIN_TIME = 60*1000;
    private static final int UPDATE_TIPS_ALL_MAX_TIME = 120*1000;
    private static final int UPDATE_TIPS_ONE_MIN_TIME = 150*1000;
    private static final int UPDATE_TIPS_ONE_MAX_TIME = 300*1000;
    private static final int UPDATE_TIPS_ALL = 0;
    private static final int UPDATE_TIPS_ONE = 1;
    private static final int UPDATE_TIPS_INSTALL = 2;
    private static Handler mHandler = new Handler(MarketApplication.getRootContext().getMainLooper()) {
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UPDATE_TIPS_ALL:
                List<Bitmap> bmpList = (List<Bitmap>) msg.obj;
                int count = msg.arg1;
                MarketNotificationManager.get().notifyAppUpdate(bmpList, count);
                break;
            case UPDATE_TIPS_ONE:
                Drawable recommendIcon = (Drawable) msg.obj;
                Bundle bundle = msg.getData();
                String title = MarketApplication.getRootContext().getString(R.string.update_app_tip,bundle.getString("appName"));
                String verUptDes = bundle.getString("verUptDes");
                if (TextUtils.isEmpty(verUptDes)) {
                    verUptDes = MarketApplication.getRootContext().getString(R.string.update_time) + bundle.getString("updateTime");
                }
                MarketNotificationManager.get().notifyUpdateByApp(recommendIcon, title, title, verUptDes);
                break;
            case UPDATE_TIPS_INSTALL:
            	Drawable installIcon = (Drawable) msg.obj;
                Bundle installBundle = msg.getData();
                MarketNotificationManager.get().notifyInstallSuccess(installBundle.getString("titleName"), installBundle.getString("pkgName"), installIcon);
            	break;
            }
        }
    };
    
    
	/**
	 * 包安装成功后提示
	 * @param title application name
	 * @param pkgName package name
	 */
	public static void notifyInstallSuccess(String title, String pkgName) {
		
		Drawable drawable = null;
		try {
			PackageManager mPkgManager = MarketApplication.getRootContext().getPackageManager();
			PackageInfo pInfo = mPkgManager.getPackageInfo(pkgName, 0);
			drawable = getDrawableFromOther(pInfo.packageName, pInfo.applicationInfo.icon);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Bundle bundle = new Bundle();
        bundle.putString("titleName", title);
        bundle.putString("pkgName", pkgName);
        
        Message msg = new Message();
		msg.what = UPDATE_TIPS_INSTALL;
        msg.setData(bundle);
		msg.obj = drawable;
		mHandler.sendMessage(msg);
	}
    
    
	public static void updateNotificationBar(List<AppInfoBto> updateList, int count) {

	    String recommendApp = getRecommendApp(MarketApplication.getRootContext(), updateList);
	    Drawable recommendIcon = null;
	    AppInfoBto recommendInfo = null;
	    
		List<Bitmap> bmpList = new ArrayList<Bitmap>();
		PackageManager mPkgManager = MarketApplication.getRootContext().getPackageManager();
		Drawable drawable = null;
		int updateSize = updateList.size();
		for (int i = 0; i < updateSize; i++) {
			try {
			    
		        PackageInfo pInfo = mPkgManager.getPackageInfo(updateList.get(i).getPackageName(), 0);
		        drawable = pInfo.applicationInfo.loadIcon(mPkgManager);
		        if (drawable != null && bmpList.size() < MarketNotificationManager.MAX_UPDATE_SHOWICON_NUM) {
		            bmpList.add(BitmapUtiles.drawableToBitmap(drawable));
		        }
				
				if (!TextUtils.isEmpty(recommendApp) && recommendApp.equals(pInfo.packageName)) {
				    recommendIcon = getDrawableFromOther(pInfo.packageName, pInfo.applicationInfo.icon);
				    recommendInfo = updateList.get(i);
				}
				
				drawable = null;
			}
			catch (Exception e) {
			    e.printStackTrace();
			}
		}
		
		long time = System.currentTimeMillis() - mReqUpdateTime;
		long appTime = 0;
		long allTime = 0;
		
		Random rd = new Random();
		if (time > 0 && time < UPDATE_TIPS_ALL_MIN_TIME) {
		    allTime = UPDATE_TIPS_ALL_MIN_TIME - time + rd.nextInt(60) * 1000;
		} else if (time > UPDATE_TIPS_ALL_MAX_TIME) {
		    allTime = 0;
		}
		
		if (time > 0 && time < UPDATE_TIPS_ONE_MIN_TIME) {
		    appTime = UPDATE_TIPS_ONE_MIN_TIME - time + rd.nextInt(150) * 1000; 
		} else if (time > UPDATE_TIPS_ONE_MAX_TIME) {
		    appTime = 0;
		}
		
		if (allTime == 0 && appTime == 0) {
		    appTime =  (90 + rd.nextInt(90)) * 1000;
		}
		
		if (mHandler == null) return;
		if (count > 0 && !mHandler.hasMessages(UPDATE_TIPS_ALL)) {
		    
    		Message msg1 = new Message();
    		msg1.what = UPDATE_TIPS_ALL;
    		msg1.arg1 = count;
    		msg1.obj = bmpList;
    		mHandler.sendMessageDelayed(msg1, allTime);
		}
		
		if (recommendIcon != null && recommendInfo != null
		        && !mHandler.hasMessages(UPDATE_TIPS_ONE)) {
		    
    		Message msg2 = new Message();
    		msg2.what = UPDATE_TIPS_ONE;
    		
    		Bundle bundle = new Bundle();
            bundle.putString("appName", recommendInfo.getName());
            bundle.putString("verUptDes", recommendInfo.getVerUptDes());
            bundle.putString("updateTime", recommendInfo.getVerUptTime());
            
            msg2.setData(bundle);
    		msg2.obj = recommendIcon;
    		
    		mHandler.sendMessageDelayed(msg2, appTime);
		}
    }
	
	
    public static void campaignsSendNotification(Context context, int number) {
    	NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, Splash.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        
        String title = context.getResources().getString(R.string.campaings_has_send);
        String content = context.getResources().getString(R.string.campaings_has_send_tip);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setTicker(content);
        builder.setSmallIcon(R.drawable.icon_notify);
        //builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notify_bar_update));
        
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        
        nm.notify(12, notification);

        Util.setCompaignsNotifyFlag(context, true);
        Util.displayNumOnLauncher(context, number);
    }
	
	
	public static boolean notifyDownloading(Context context, int count, boolean download, boolean outDownload) {
		boolean notifySuccess = false;
		
	    if (mDownloadNum == count && !outDownload) return notifySuccess;
	    mDownloadNum = count;

	    if (mDownloadNum <= 0) {
//	        try {
//	            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//	            notificationManager.cancel(MarketNotificationManager.NOTIFY_ID_DOWNLOADING);
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
	    } else {
	        
            Intent mainInt = Util.getDownloadActivityIntent();
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, mainInt, PendingIntent.FLAG_UPDATE_CURRENT);
            String notiTitle = context.getString(R.string.down_noti_downloading_tip, ""+mDownloadNum);
            String notiContent = context.getString(R.string.down_noti_downloading_content);
            int notiFlag = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

            boolean tipNoti = false;
            if (!download && ((!outDownload && mDownloadNum == 1) || outDownload)) {
            	tipNoti = true;
            }
            String tickerText = tipNoti ? notiTitle : null;
            notificationDownloadState(context, pIntent, tickerText, notiTitle, notiContent, notiFlag, MarketNotificationManager.NOTIFY_ID_DOWNLOADING);
            notifySuccess = true;
        }
        
        context.sendBroadcast(new Intent("download.refresh"));
        return notifySuccess;
    }

	
    public static void notifyNetworkDisconnect(Context context) {
        Intent mainInt = Util.getDownloadActivityIntent();
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, mainInt, PendingIntent.FLAG_UPDATE_CURRENT);
        String notiTitle = context.getString(R.string.down_noti_network_discon_title);
        String notiContent = context.getString(R.string.down_noti_network_discon_content);
        int notiFlag = Notification.FLAG_AUTO_CANCEL;

        notificationDownloadState(context, pIntent, notiContent, notiTitle, notiContent, notiFlag, MarketNotificationManager.NOTIFY_ID_DOWNLOADING);
    }

    
    public static void notifyDownloadComplete(Context context, int failedNum) {
        Intent mainInt = Util.getDownloadActivityIntent();
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, mainInt, PendingIntent.FLAG_UPDATE_CURRENT);
        String notiTitle = null;
        String notiContent = null;
        if (failedNum == 0) {
//            notiTitle = context.getString(R.string.down_noti_downloadcomplete_title);
//            notiContent = context.getString(R.string.down_noti_downloadfinish_content);
            MarketNotificationManager.get().cancel(MarketNotificationManager.NOTIFY_ID_DOWNLOADING);
        } else {
            notiTitle = context.getString(R.string.down_noti_downloadPause_title);
            notiContent = context.getString(R.string.down_noti_downloadcomplete_content, failedNum);
            int notiFlag = Notification.FLAG_AUTO_CANCEL;
            notificationDownloadState(context, pIntent, notiContent, notiTitle, notiContent, notiFlag, MarketNotificationManager.NOTIFY_ID_DOWNLOADING);
        }

        context.sendBroadcast(new Intent("download.refresh"));
    }

    
    public static void notifySdcardLost(Context context) {
        String notiContent = context.getString(R.string.down_noti_sdcard_lost_content);
        notifyTickerText(context, notiContent);
    }

    
    public static void notifyNoEnoughSpace(Context context) {
        String notiContent = context.getString(R.string.down_noti_no_enough_space_content);
        notifyTickerText(context, notiContent);
    }

    
    private static void notificationDownloadState(Context context, PendingIntent pIntent, String tickerText, String title, String content, int notifyFlag, int notifyId) {
        MarketNotificationManager.get().notifyCommon(title, tickerText, content, pIntent, notifyId, notifyFlag);
    }
    
    
    private static void notificationDownloadState(Context context, PendingIntent pIntent, String tickerText, String title, String content, int notifyFlag) {
        MarketNotificationManager.get().notifyCommon(title, tickerText, content, pIntent, 0, notifyFlag);
    }

    
    public static void notifyTickerText(Context context, String tickerText) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_ONE_SHOT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setTicker(tickerText);
        builder.setSmallIcon(R.drawable.icon_notify);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentIntent(pIntent);
        Notification notification = builder.build();
        
        nm.notify(1, notification);
        nm.cancel(1);
    }
    
    
    private static String getRecommendApp(Context context, List<AppInfoBto> list) {
        String recommendApp = null;
        try {
            ActivityManager mAm = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RecentTaskInfo> apps = mAm.getRecentTasks(64, ActivityManager.RECENT_WITH_EXCLUDED);

            int updateSize = 0;
            int recentSize = 0;
            String updateApk = null;
            String recentApk = null;
            ComponentName cn = null;
            Intent baseIntent = null;
            
            if (list != null) {
                updateSize = list.size();
            }
            
            if (apps != null) {
                recentSize = apps.size();
            }

            //从最近列表中获取推荐更新应用
            if (recentSize > 0 && updateSize > 0) {
                
                for (int i=0; i<recentSize; i++) {
                    baseIntent = apps.get(i).baseIntent;
                    if (baseIntent == null || !baseIntent.toString().contains("android.intent.category.LAUNCHER")){
                        continue;
                    }

                    cn = baseIntent.getComponent();
                    if (cn == null) {
                        continue;
                    }
                    recentApk = cn.getPackageName();
                    
                    for (int j=0; j<updateSize; j++) {
                        updateApk = list.get(j).getPackageName();
                        if (recentApk.equals(updateApk)) {
                            recommendApp = updateApk;
                            break;
                        }
                    }
                    
                    if (!TextUtils.isEmpty(recommendApp)) {
                        break;
                    }
                }
                
            }
            
            //如果最近列表中没有需要更新的应用，随机一个
            if (TextUtils.isEmpty(recommendApp) && updateSize > 0) {
              Random rd = new Random();
              int rdNum = rd.nextInt(updateSize);
              recommendApp = list.get(rdNum).getPackageName();
            }
            
        } catch (Exception e) {
            recommendApp = null;
            e.printStackTrace();
        }
        
        return recommendApp;
    }
    
    
    //获取其它包的原始图片，以免某些手机对icon做了修改，导致图片显示有问题
    private static Drawable getDrawableFromOther(String pkgName, int iconId) {
    	if (TextUtils.isEmpty(pkgName)) return null;
    	
    	Context otherContext = null;    
        try {    
        	otherContext = MarketApplication.getRootContext().createPackageContext(pkgName, Context.CONTEXT_IGNORE_SECURITY);  
        	return otherContext.getResources().getDrawable(iconId);
        } catch (Exception e) {     
            return null;    
        }        
    }
}
