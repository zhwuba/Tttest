package com.market.download.userDownload;

import java.util.ArrayList;
import java.util.HashMap;

import com.market.download.common.DownloadSettings;
import com.market.download.common.RunTask;
import com.market.download.common.TaskThread;
import com.market.download.common.TaskThread.ThreadCallback;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.userDownload.UserDownloadTask.UserDownTaskCallback;
import com.zhuoyi.market.appResident.SettingData;

import android.content.Context;



/**
 * make sure all operation in the same thread
 * @author Athlon
 *
 */
public class UserDownTaskManager {
	
    private ArrayList<UserDownloadTask> mUserTaskWaitList;
	private HashMap<String, UserDownloadTask> mUserTaskMap;
	
	private ArrayList<TaskThread> mUserTaskThreadList;
	
	private static UserDownTaskManager mSelf = null;
	
	private Context mContext;
	
	private DataHolder mDataHolder;
	
	public static UserDownTaskManager getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new UserDownTaskManager(context);
		}
		
		return mSelf;
	}
	
	
	UserDownTaskManager(Context context) {
		mContext = context;
		mUserTaskWaitList = new ArrayList<UserDownloadTask>();
		mUserTaskMap = new HashMap<String, UserDownloadTask>();
		mUserTaskThreadList = new ArrayList<TaskThread>();
		
		mDataHolder = DataHolder.getHolder(mContext);
	}
	
	
	public void pauseUserEvent(String pkgName, int verCode) {
	    DownloadEventInfo info = mDataHolder.getEventInfo(pkgName, verCode);
	    
	    mDataHolder.downloadPaused(info);
	    
	    
	}
	
	
	public void downloadUserEvent(DownloadEventInfo eventInfo) {
		String eventSignal = DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
		UserDownloadTask downTask = mUserTaskMap.get(eventSignal);
		if (downTask == null) {
			downTask = new UserDownloadTask(mContext, mUserDownCb, eventInfo);
			mUserTaskMap.put(eventSignal, downTask);
			mUserTaskWaitList.add(downTask);
			
		} else if (!downTask.isTaskAlive()) {
		    downTask.taskInvalidated();
		    downTask = new UserDownloadTask(mContext, mUserDownCb, eventInfo);
            mUserTaskMap.put(eventSignal, downTask);
            mUserTaskWaitList.add(downTask);
		    
		}
		
		//start download thread to run task
		startDownloadThread();
	}
	
	
	private void startDownloadThread() {
	    while(true) {
	        if (mUserTaskThreadList.size() >= SettingData.mDownloadMaxNum || mUserTaskThreadList.size() == mUserTaskWaitList.size()) {
	            break;
	        }
	        
	        TaskThread thread = new TaskThread(mThreadCb);
	        mUserTaskThreadList.add(thread);
	    }
	    
	    for (TaskThread thread : mUserTaskThreadList) {
	        if (thread.isThreadAlive()) {
	            try {
	                thread.start();
	            } catch (Exception e) {
	                //do nothing
	            }
	        }
	    }
	}
	
	
	ThreadCallback mThreadCb = new ThreadCallback() {

        @Override
        public RunTask getTopRunTask() {
            UserDownloadTask task = null;
            synchronized(mUserTaskThreadList) {
                if (mUserTaskWaitList.size() >= 1) {
                    task = mUserTaskWaitList.remove(0);
                }
            }
            
            return task;
        }

        @Override
        public void removeTopRunTask() {
            //do nothing
            
        }

        @Override
        public void threadFinished(TaskThread downThread) {
            mUserTaskThreadList.remove(downThread);
        }

        @Override
        public void watchDog(TaskThread downThread) {
            // TODO Auto-generated method stub
            
        }
	    
	};
	
	
	UserDownTaskCallback mUserDownCb = new UserDownTaskCallback(){

		@Override
		public void downloadApkStarted(DownloadEventInfo eventInfo) {
		    mDataHolder.downloadStarted(eventInfo);
		}

		@Override
		public void fileTotalSizeGet(DownloadEventInfo eventInfo) {
		    mDataHolder.totalSizeGot(eventInfo);
		}

		@Override
		public void noEnoughSpaceOnSdcard(DownloadEventInfo eventInfo) {
		    mDataHolder.downloadFailed(eventInfo, DownloadMsg.MSG_NO_ENOUGH_SPACE);
		}

		@Override
		public void sdcardHasLost(DownloadEventInfo eventInfo) {
		    mDataHolder.downloadFailed(eventInfo, DownloadMsg.MSG_SDCARD_LOST);
		}

		@Override
		public void fileNotFoundOnHttpServer(DownloadEventInfo eventInfo) {
		    mDataHolder.downloadFailed(eventInfo, DownloadMsg.MSG_FILE_NOT_FOUND);
		}

		@Override
		public void downloadProgressChanged(DownloadEventInfo eventInfo) {
		    mDataHolder.downloadProgressChange(eventInfo);
		}

		@Override
		public void downloadApkSuccess(DownloadEventInfo eventInfo) {
			mDataHolder.downloadComplete(eventInfo);
			
			
		}
    	
    };
}
