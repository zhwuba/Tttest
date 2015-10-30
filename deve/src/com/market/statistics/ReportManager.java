package com.market.statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;

import com.market.download.common.RunTask;
import com.market.download.common.TaskThread;
import com.market.featureOption.FeatureOption;
import com.zhuoyi.market.constant.SharedPrefDefine;

public class ReportManager {
	
	private static ReportManager mSelf;
	
	private Context mContext;
	
	private ArrayList<OffLineReportTask> mReportTaskList;
	private ArrayList<DownReportTask> mDownTaskList;
	private ArrayList<InstallReportTask> mInstallTaskList;
	
	private TaskThread mReportThread;
	private TaskThread mDownTaskThread;
	private TaskThread mInstallTaskThread;
	
	private ReportThreadCallback mReportCallback;
	private DownThreadCallback mDownCallback;
	private InstallThreadCallback mInstallCallback;
	
	public static ReportManager getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new ReportManager(context);
		}
		
		return mSelf;
	}
	
	
	ReportManager(Context context) {
		mContext = context;
		mReportTaskList = new ArrayList<OffLineReportTask>();
		mDownTaskList = new ArrayList<DownReportTask>();
		mInstallTaskList = new ArrayList<InstallReportTask>();
		
		mReportCallback = new ReportThreadCallback();
		mDownCallback = new DownThreadCallback();
		mInstallCallback = new InstallThreadCallback();
	}
	
	
	public static String getCurrDateString () {
		Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
	}
	
	
	public static void recordEntryMarketTime(Context context) {
        String dateString = getCurrDateString();
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.REPORT_LOG_MANA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("entryTime", dateString);
        editor.commit();
	}
	
	
	public static String getEntryMarketTime(Context context) {
		SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.REPORT_LOG_MANA, Context.MODE_PRIVATE);
		return sp.getString("entryTime", "");
	}
	
	
	
	/*
	 * start code for off line log report
	 */
	
	public void reportOffLineLog(String action, String from) {
	    if (!FeatureOption.STATISTICS_REPORT) {
            //report switch is closed, do nothing
            return;
        }
	    
		OffLineReportTask task = new OffLineReportTask(mContext, action, from);
		mReportTaskList.add(task);
		
		startReportThread();
	}
	
	
	public void reportOffLineLog(String action, String from, String pkgName) {
	    if (!FeatureOption.STATISTICS_REPORT) {
            //report switch is closed, do nothing
            return;
        }
	    
		OffLineReportTask task = new OffLineReportTask(mContext, action, from, pkgName);
		mReportTaskList.add(task);
		
		startReportThread();
	}
	
	
	private void startReportThread() {
		if (mReportTaskList.size() > 0) {
            if (mReportThread == null || !mReportThread.isThreadAlive()) {
                mReportThread = new TaskThread(mReportCallback);
                mReportThread.start();
            }
        }
	}
	
	
	private class ReportThreadCallback implements TaskThread.ThreadCallback {
        @Override
        public RunTask getTopRunTask() {
            if (mReportTaskList.size() > 0) {
                return mReportTaskList.get(0);
            } else {
                return null;
            }
        }
    
        @Override
        public void threadFinished(TaskThread downThread) {
            //do nothing now
        }
    
        @Override
        public void watchDog(TaskThread downThread) {
            //do nothing now
        }

        @Override
        public void removeTopRunTask() {
            if (mReportTaskList.size() > 0) {
            	mReportTaskList.remove(0);
            }
        }
    };
    
    
    
    /*
     * start code for download and install report
     */
    
    public void reportDownloadResult(String fromFlag, String topicId, String appName,
                                    String pkgName, String appId, int verCode) {
//        if (!FeatureOption.STATISTICS_REPORT) {
//            //report switch is closed, do nothing
//            return;
//        }
        DownReportTask task = new DownReportTask(mContext, fromFlag, topicId, appName,
                                                pkgName, appId, verCode);
        mDownTaskList.add(task);
        
        startDownReportThread();
    }
    
    
    private void startDownReportThread() {
        if (mDownTaskList.size() > 0) {
            if (mDownTaskThread == null || !mDownTaskThread.isThreadAlive()) {
                mDownTaskThread = new TaskThread(mDownCallback);
                mDownTaskThread.start();
            }
        }
    }
    
    
    private class DownThreadCallback implements TaskThread.ThreadCallback {
        @Override
        public RunTask getTopRunTask() {
            if (mDownTaskList.size() > 0) {
                return mDownTaskList.get(0);
            } else {
                return null;
            }
        }
    
        @Override
        public void threadFinished(TaskThread downThread) {
            //do nothing now
        }
    
        @Override
        public void watchDog(TaskThread downThread) {
            //do nothing now
        }

        @Override
        public void removeTopRunTask() {
            if (mDownTaskList.size() > 0) {
                mDownTaskList.remove(0);
            }
        }
    };
    
    
    public void reportInstallResult(String fromFlag, String topicId, String appName,
                                    String pkgName, String appId, int verCode) {
//        if (!FeatureOption.STATISTICS_REPORT) {
//            //report switch is closed, do nothing
//            return;
//        }
        
        InstallReportTask task = new InstallReportTask(mContext, fromFlag, topicId, appName,
                                                        pkgName, appId, verCode);
        mInstallTaskList.add(task);
        
        startInstallReportThread();
    }
    
    
    private void startInstallReportThread() {
        if (mInstallTaskList.size() > 0) {
            if (mInstallTaskThread == null || !mInstallTaskThread.isThreadAlive()) {
                mInstallTaskThread = new TaskThread(mInstallCallback);
                mInstallTaskThread.start();
            }
        }
    }
    
    
    private class InstallThreadCallback implements TaskThread.ThreadCallback {
        @Override
        public RunTask getTopRunTask() {
            if (mInstallTaskList.size() > 0) {
                return mInstallTaskList.get(0);
            } else {
                return null;
            }
        }
    
        @Override
        public void threadFinished(TaskThread downThread) {
            //do nothing now
        }
    
        @Override
        public void watchDog(TaskThread downThread) {
            //do nothing now
        }

        @Override
        public void removeTopRunTask() {
            if (mInstallTaskList.size() > 0) {
                mInstallTaskList.remove(0);
            }
        }
    };
}
