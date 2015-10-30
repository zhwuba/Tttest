package com.market.download.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.market.download.util.Util;

import android.content.Context;


public class InstallControl {
    
    private static InstallControl mSelf = null;
    
    public static InstallControl getControl() {
        if (mSelf == null) {
            mSelf = new InstallControl();
        }
        
        return mSelf;
    }
    
    
    InstallControl () {
        mSilentThreadCb = new SilentThreadCallback();
        mSilentTaskMap = new ConcurrentHashMap<String, SilentInstallTask>();
        mInstallFileList = new ArrayList<String>();
    }
    
    
    public static final int RESULT_NO_PERMISSION = 0;
    public static final int RESULT_READY_TO_INSTALL = 1;
    
    private TaskThread mInstallThread;
    private ArrayList<String> mInstallFileList;
    private ConcurrentHashMap<String, SilentInstallTask> mSilentTaskMap;
    
    private SilentThreadCallback mSilentThreadCb;
    
    public int silentInstall(Context context, File apkFile, SilentInstallTask.InstallCallback callback) {
        if (!Util.hasInstallPermission(context)) {
            return RESULT_NO_PERMISSION;
        }
        
        String filePath = apkFile.getAbsolutePath().trim();
        mInstallFileList.add(filePath);
        mSilentTaskMap.put(filePath, new SilentInstallTask(context, apkFile, callback));
        
        if (mInstallThread == null || !mInstallThread.isThreadAlive()) {
            if (mInstallThread != null) {
                mInstallThread.stopThread();
            }
            mInstallThread = new TaskThread(mSilentThreadCb);
            mInstallThread.start();
        }
        
        return RESULT_READY_TO_INSTALL;
    }
    
    
    private class SilentThreadCallback implements TaskThread.ThreadCallback {

        @Override
        public RunTask getTopRunTask() {
            SilentInstallTask silentTask = null;
            while (mInstallFileList.size() > 0) {
                String filePath = mInstallFileList.get(0);
                silentTask = mSilentTaskMap.get(filePath);
                if (silentTask != null) {
                    break;
                } else {
                    mInstallFileList.remove(0);
                }
            }
            
            return silentTask;
            
//            Iterator iter = mSilentTaskMap.entrySet().iterator();
//            if (iter.hasNext()) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                SilentInstallTask silentTask = (SilentInstallTask)entry.getValue();
//                return silentTask;
//            } else {
//                return null;
//            }
        }

        @Override
        public void removeTopRunTask() {
            String filePath = mInstallFileList.remove(0);
            mSilentTaskMap.remove(filePath);
            
//            Iterator iter = mSilentTaskMap.entrySet().iterator();
//            if (iter.hasNext()) {
//                iter.next();
//                iter.remove();
//            }
        }

        @Override
        public void threadFinished(TaskThread downThread) {
            
        }

        @Override
        public void watchDog(TaskThread downThread) {
            
        }
        
    }
}
