package com.zhuoyi.market.asyncTask;


public abstract class MarketAsyncTask {
    public static final int STATE_READY = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISH = 2;
    
    private boolean isAlive = true;
    
    private int mState = STATE_READY;
    
    private TaskCallback mCallback;
    private String mGroup;
    private String mName;
    
    
    public MarketAsyncTask(String name, String group) {
        mName = name;
        mGroup = group;
    }
    
    
    public void setTaskCallback(TaskCallback callback) {
        mCallback = callback;
    }
    
    
    public String getTaskName() {
        return mName;
    }
    
    
    public String getTaskGroup() {
        return mGroup;
    }
    
    
    public int getCurrState() {
        return mState;
    }

    
    public void runTask() {
        mState = STATE_RUNNING;
        run();
        mState = STATE_FINISH;
    }
    
    
    public MarketAsyncTask getRunTask() {
        if(mCallback != null) {
            return mCallback.getRuntask(this);
        }
        
        return null;
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
    
    
    protected abstract void run();
    
    
    public interface TaskCallback {
        MarketAsyncTask getRuntask(MarketAsyncTask task);
    }
}
