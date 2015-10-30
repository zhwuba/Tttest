package com.zhuoyi.market.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.market.download.util.Util;
import com.market.view.CommonLoadingManager;
import com.zhuoyi.market.appdetail.AppDetailInfoActivity;
import com.zhuoyi.market.badger.ShortcutBadger;
import com.zhuoyi.market.receiver.HomePressedRecevier;
import com.zhuoyi.market.receiver.HomePressedRecevier.OnHomePressListener;

public class AppListenerService extends Service implements OnHomePressListener{

	public static final String TAG = "AppListenerService";

	private HomePressedRecevier mHomePressedRecevier;

	private Thread mThread;
	
	private boolean mFlag;
	
	public Object synchronizedObject = new Object();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(mFlag){

					// TODO Auto-generated method stub
					if(isRunningForeground(AppListenerService.this)){	
//						Log.w(TAG, "--->ǰ̨前台运行");
						synchronized (synchronizedObject) {
							if(mHomePressedRecevier == null){
	//							Log.w(TAG, "----> 注册广播");
								registerReceiver();
								mHomePressedRecevier.setOnHomePressListener(AppListenerService.this);
								
								/**	清除桌面角标提示 	**/
								ShortcutBadger mShortcutBadger = ShortcutBadger.getBadgerImpl(getApplicationContext());
								if(mShortcutBadger != null) {
									Util.setCompaignsNotifyFlag(getApplicationContext(), false);
									mShortcutBadger.clearBadge();
								}
							}
						}
						CommonLoadingManager.get().setMarketRunningForeground(true);
					} else {
						CommonLoadingManager.get().setMarketRunningForeground(false);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mThread.start();

		return super.onStartCommand(intent, flags, startId);
	}



	@Override
	public void onCreate() {
		mFlag = true;
		super.onCreate();
	}

	/**
	 * 注册监听Home键广播
	 */
	public void registerReceiver() {
		mHomePressedRecevier = new HomePressedRecevier();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(mHomePressedRecevier, filter);
	}


	/**
	 * 获取顶部Activity名称
	 * 
	 * @param 
	 * @return
	 */
	public String getTopActivityName(Context context) {
		String topActivityClassName = null;
		try {
    		ActivityManager activityManager = (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
    		List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
    		if (runningTaskInfos != null) {
    			ComponentName f = runningTaskInfos.get(0).topActivity;
    			topActivityClassName = f.getClassName();
    		}
		} catch (Exception e) {
		    topActivityClassName = null;
		}
		return topActivityClassName;
	}

	/**
	 * 
	 * @return apk是否前台运行
	 */

	public boolean isRunningForeground(Context context) {
		String packageName = context.getPackageName();
		String topActivityClassName = getTopActivityName(context);
		if (packageName != null && topActivityClassName != null
				&& (topActivityClassName.startsWith(packageName)
				        || topActivityClassName.equals("com.market.account.login.BaseHtmlActivity"))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 按下Home键 会执行此方法
	 */
	@Override
	public void onHomePressed(Context context) {
		synchronized (synchronizedObject) {
			if (mHomePressedRecevier != null) {
				unregisterReceiver(mHomePressedRecevier);
				mHomePressedRecevier = null;
			}
		}
		
		/**
		 * 设置详情返回不弹窗
		 */
		AppDetailInfoActivity.setIsFromInner(true);
	}

	@Override
	public void onDestroy() {
		synchronized (synchronizedObject) {
			mFlag = false;
			if (mHomePressedRecevier != null) {
				unregisterReceiver(mHomePressedRecevier);
				mHomePressedRecevier = null;
			}
		}
		super.onDestroy();
	}



}
