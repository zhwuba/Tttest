package com.zhuoyi.market.asyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import com.zhuoyi.market.asyncTask.MarketAsyncTask.TaskCallback;
import com.zhuoyi.market.asyncTask.MarketThread.ThreadCallback;

public class TaskManager {
    
    private static TaskManager mSelf = null;
    
    private HashMap<String, ArrayList<MarketAsyncTask>> mTaskMap;
    private HashMap<String, MarketThread> mThreadMap;
    
    private AsyncTaskCallback mTaskCallback;
    private AsyncThreadCallback mThreadCallback;
    
    TaskManager() {
        mTaskCallback = new  AsyncTaskCallback();
        mThreadCallback = new AsyncThreadCallback();
        
        mTaskMap = new HashMap<String, ArrayList<MarketAsyncTask>>();
        mThreadMap = new HashMap<String, MarketThread>();
        
    }
    
    
    public static TaskManager getInstance() {
        if(mSelf == null) {
            mSelf = new TaskManager();
        }
        
        return mSelf;
    }
    
    
    public void startTask(MarketAsyncTask task, boolean addToTop) {
        String group = task.getTaskGroup();
        task.setTaskCallback(mTaskCallback);
        ArrayList<MarketAsyncTask> taskArray = mTaskMap.get(group);
        if(taskArray == null) {
            taskArray = new ArrayList<MarketAsyncTask>();
            mTaskMap.put(group, taskArray);
        }
        
        MarketAsyncTask existTask = null;
        boolean isNameExist = false;
        for(int i=0; i < taskArray.size(); i++) {
            existTask = taskArray.get(i);
            if(existTask == null || existTask.getTaskName() == null)
            	continue;
            if(existTask.getTaskName().equals(task.getTaskName())) {
                isNameExist = true;
                break;
            }
        }
        
        if(addToTop) {
            taskArray.add(0, task);
        }else if(!isNameExist) {
            taskArray.add(task);
        }
        
        MarketThread thread = mThreadMap.get(group);
        if(thread == null || !thread.isThreadAlive()) {
            thread = new MarketThread(group, task, mThreadCallback);
            thread.start();
            mThreadMap.put(group, thread);
        }
    }
    
    
    private class AsyncThreadCallback implements ThreadCallback {
        @Override
        public void threadFinished(MarketThread thread) {
            mThreadMap.remove(thread.getGroupName());
        }
    }
    
    
    private class AsyncTaskCallback implements TaskCallback {
        @Override
        public MarketAsyncTask getRuntask(MarketAsyncTask task) {
            String goupName = task.getTaskGroup();
            ArrayList<MarketAsyncTask> taskArray = mTaskMap.get(goupName);
            if(taskArray!= null && taskArray.size() > 0) {
                return taskArray.remove(0);
            }
            
            return null;
        }
        
    };
}
