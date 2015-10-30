package com.market.download.common;


public class TaskThread extends Thread {

    
    /**
     * this flag signal {@link DownloadThread} is available or not, 
     * un-available {@link DownloadThread} will be remove from {@link mSelfList}
     */
    private boolean mStopFlag = false;

    private boolean mIsStarted = false;
    
    private boolean mPassCheck = false;
    
    private ThreadCallback mMyCallback;
    
    private RunTask mCurrTask;
    
    
    /**
     * new a thread to process {@link RunTask}
     * @param task if null, thread will get task from task array
     */
    public TaskThread(ThreadCallback callback) {
        mMyCallback = callback;
    }

    
    public RunTask getCurrRunTask() {
        return mCurrTask;
    }
    
    
    /**
     * to stop thread running
     */
    public void stopThread() {
        mStopFlag = true;
        
        if(mCurrTask != null){
            mCurrTask.invalidateTask();
        }
        if(mMyCallback != null) {
            mMyCallback.threadFinished(this);
        }
    }

    
    public boolean isThreadAlive() {
        if(!mIsStarted || (!mStopFlag && this.isAlive())) {
            return true;
        }else {
            return false;
        }
    }
    
    
    @Override
    public void run() {
        /*
         * if RunTask is been invalidated, don't run it
         */
        mIsStarted = true;
        watchDog();
        mCurrTask = mMyCallback.getTopRunTask();
        while (mCurrTask != null && !mStopFlag) {
            watchDog();
            if (mCurrTask.isTaskAlive()) {
                mCurrTask.setRunTaskThread(this);
                mCurrTask.runTask();
                mCurrTask.setRunTaskThread(null);
            }
            watchDog();
            mMyCallback.removeTopRunTask();
            mCurrTask = mMyCallback.getTopRunTask();
        }
        mStopFlag = true;
        if(mMyCallback != null) {
            mMyCallback.threadFinished(this);
        }
    }

    
    @Override
    public synchronized void start() {
        if (mStopFlag) {
            /*
             * if thread is running now, don't start it again
             */
            return;
        }
        super.start();
    }
    
    
    public void watchDog() {
        if(mMyCallback != null) {
            mMyCallback.watchDog(this);
        }
    }
    
    
    public void unableCheckThread() {
    	mPassCheck = true;
    }
    
    
    public void enableCheckThread() {
    	watchDog();
    	mPassCheck = false;
    }
    
    
    public boolean isPassCheckNow() {
    	return mPassCheck;
    }
    
    
    /**
     * to listen thread finished, and get the next {@link RunTask}
     * @author Athlon
     *
     */
    public interface ThreadCallback {
        RunTask getTopRunTask();
        void removeTopRunTask();
        void threadFinished(TaskThread downThread);
        void watchDog(TaskThread downThread);
    }
}
