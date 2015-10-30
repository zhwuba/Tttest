package com.market.behaviorLog;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class LogService extends Service {
	private static final String TAG = "LogService";
	
	public static final String EXTRA_MSG = "msg";
    public static final String EXTRA_DES = "des";
    public static final String EXTRA_MILLIS = "millis";
	
	private HandlerThread mHandlerThread;
    private LogHandler mHandler;
    
    private LogManager mLogManager;
	
    private static LogHandler mStaticHandler = null;
    
    
    public static LogHandler getLogHandler() {
    	return mStaticHandler;
    }
    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mHandlerThread = new HandlerThread("logHandleThread");
        mHandlerThread.start();
        mHandler = new LogHandler(mHandlerThread.getLooper());
        
        mStaticHandler = mHandler;
        
        mLogManager = new LogManager(this);
        
        LogSettings.setAlarmToUpload(this, false);
	}
	

	@Override
	public void onDestroy() {
	    mStaticHandler = null;
	    mHandlerThread.quit();
	    
		super.onDestroy();
	}

	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    if (intent != null) {
	        int msg = intent.getIntExtra(EXTRA_MSG, 0);
	        long millis = intent.getLongExtra(EXTRA_MILLIS, 0);
	        String des = intent.getStringExtra(EXTRA_DES);
	        
	        Message message = new Message();
	        message.what = msg;
	        Bundle data = new Bundle();
	        data.putLong(EXTRA_MILLIS, millis);
	        if (des != null) {
	            data.putString(EXTRA_DES, des);
	        }
	        message.setData(data);
	        
	        mHandler.sendMessage(message);
	    }
	    
		return super.onStartCommand(intent, flags, startId);
	}

	
	/**
	 * handle activity entry behavior, record the current entry activity
	 */
	public static final int MSG_ACTIVITY_ENTRY = 1;
	/**
	 * handle activity exit behavior, record the show count and time
	 */
	public static final int MSG_ACTIVITY_EXIT = 2;
	/**
	 * handle the show event about a view
	 */
	public static final int MSG_VIEW_SHOW_EVENT = 3;
	/**
	 * handle the click event about a view
	 */
	public static final int MSG_VIEW_CLICK_EVENT = 4;
	/**
	 * handle the count event
	 */
	public static final int MSG_COUNT_EVENT = 5;
	/**
	 * called when alarm in time to upload log
	 */
	public static final int MSG_ALARM_TO_UPLOAD = 6;
	/**
	 * called when application exit
	 */
	public static final int MSG_APP_EXIT = 7;
	/**
	 * called when wifi connect available
	 */
	public static final int MSG_WIFI_UPLOAD = 8;
	/**
	 * synchronize the cache log and save them
	 */
	public static final int MSG_SYNC_CACHE = 9;
	/**
	 * called when entry ad exit
	 */
	public static final int MSG_ENTRY_AD_EXIT = 10;
	/**
	 * called after application exit message programming finished
	 */
	public static final int MSG_STOP_SERVICE = 11;
	
	private class LogHandler extends Handler {
        public LogHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        	int what = msg.what;
        	Bundle data = msg.getData();
        	long millis = data.getLong(EXTRA_MILLIS);
        	String description = data.getString(EXTRA_DES);
        	LogUtil.log(TAG, "handleMessage", "receive message = " + what);
        	switch(what) {
        	case MSG_ACTIVITY_ENTRY:
        		mLogManager.activityEntry(description, millis);
        		break;
        		
        	case MSG_ACTIVITY_EXIT:
        	    mLogManager.activityExit(description, millis);
        		break;
        		
        	case MSG_VIEW_SHOW_EVENT:
        	    mLogManager.viewShow(description);
        		break;
        		
        	case MSG_VIEW_CLICK_EVENT:
        	    mLogManager.viewClick(description);
        	    break;
        		
        	case MSG_COUNT_EVENT:
        	    mLogManager.countEventVisit(description);
        		break;
        		
        	case MSG_ALARM_TO_UPLOAD:
        	    mLogManager.alarmToUploadLog();
        		break;
        		
        	case MSG_APP_EXIT:
        	    mLogManager.applicationExit();
        	    break;
        	    
        	case MSG_WIFI_UPLOAD:
        	    mLogManager.wifiAvailableToUploadLog();
        	    break;
        		
        	case MSG_SYNC_CACHE:
        	    mLogManager.syncCacheMap();
        	    break;
        	    
        	case MSG_ENTRY_AD_EXIT:
        	    mLogManager.entryAdExit(millis);
        	    break;
        	    
        	case MSG_STOP_SERVICE:
        	    LogService.this.stopSelf();
        	    break;
        	}
        }
	};
}
