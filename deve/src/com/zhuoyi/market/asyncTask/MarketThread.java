package com.zhuoyi.market.asyncTask;


public class MarketThread extends Thread {
    /**
     * this flag signal {@link MarketThread} is available or not
     */
    private boolean mStopFlag = false;

    private boolean mIsStarted = false;
    
    private ThreadCallback mMyCallback;
    
    private MarketAsyncTask mCurrTask;
    
    private String mGroupName;
    
    /**
     * new a thread to process {@link AsyncTask}
     * @param task if null, thread will get task from task array
     */
    public MarketThread(String groupName, MarketAsyncTask task, ThreadCallback callback) {
        mCurrTask = task;
        mMyCallback = callback;
        mGroupName = groupName;
    }
    
    
    public String getGroupName() {
        return mGroupName;
    }

    
    public MarketAsyncTask getCurrRunTask() {
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
        mCurrTask = mCurrTask.getRunTask();
        while (mCurrTask != null && mCurrTask.isTaskAlive() && !mStopFlag) {
            mCurrTask.runTask();
            mCurrTask = mCurrTask.getRunTask();
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
    
    
    /**
     * to listen thread finished, and get the next {@link AsyncTask}
     * @author Athlon
     *
     */
    public interface ThreadCallback {
        void threadFinished(MarketThread thread);
    }
}
