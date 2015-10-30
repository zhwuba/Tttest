package com.market.download.common;



public abstract class RunTask {
    public static final int STATE_READY = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISH = 2;
    
    private boolean isAlive = true;
    
    private int mState = STATE_READY;
    
    private TaskThread mTaskThread;
    
    
    public void setRunTaskThread(TaskThread taskThread) {
        mTaskThread = taskThread;
    }
    
    
    protected void watchDog() {
        if(mTaskThread != null) {
            mTaskThread.watchDog();
        }
    }
    
    
    public int getCurrState() {
        return mState;
    }

    
    public void runTask() {
        mState = STATE_RUNNING;
        run();
        mState = STATE_FINISH;
    }


    public void invalidateTask() {
        isAlive = false;
    }
    
    
    public boolean isTaskAlive() {
        if(isAlive && mState != STATE_FINISH) {
            return true;
        }else {
            return false;
        }
        
    }
    
    
    public void unableCheckThread() {
    	if (mTaskThread != null) {
            mTaskThread.unableCheckThread();
        }
    }
    
    
    public void enableCheckThread() {
    	if (mTaskThread != null) {
            mTaskThread.enableCheckThread();
        }
    }
    
    
    protected abstract void run();
    
}
