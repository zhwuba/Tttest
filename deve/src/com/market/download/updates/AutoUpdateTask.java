package com.market.download.updates;

import android.content.Context;

import com.market.download.common.DownBaseTask;
import com.market.download.common.DownloadSettings;
import com.market.download.util.Util;
import com.market.statistics.ReportManager;

public class AutoUpdateTask extends DownBaseTask {

    private AutoUpdateEventInfo mEventInfo;
    private TaskCallback mCallback;
    
    protected AutoUpdateTask(Context context, AutoUpdateEventInfo eventInfo, TaskCallback callback) {
        super(context, eventInfo);
        
        mEventInfo = eventInfo;
        mCallback = callback;
    }
    
    
    @Override
    protected void run() {
        if (!Util.isAppExistInHandsetNow(mContext, mEventInfo.getPkgName())) {
            mCallback.removeAutoUpdateInfo(mEventInfo);
            return;
        }
        
        if (mEventInfo.getApkFile().exists()) {
            if (Util.isApkFileUsable(mContext, mEventInfo.getApkFile(), mEventInfo.getVersionCode())) {
                mCallback.installFile(mEventInfo);
                return;
            } else {
                mEventInfo.getApkFile().delete();
            }
        }
        
        super.run();
    }
    
    
    private void reportDownloadResult() {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportDownloadResult(mEventInfo.getDownloadFlag(),
                                mEventInfo.getTopicId(),
                                mEventInfo.getAppName(),
                                mEventInfo.getPkgName(),
                                Integer.toString(mEventInfo.getAppId()),
                                mEventInfo.getVersionCode());
    }
    

    @Override
    public void taskInvalidated() {
        // TODO Auto-generated method stub

    }

    @Override
    public void fileTotalSizeGet() {
        // TODO Auto-generated method stub

    }

    @Override
    public void noEnoughSpaceOnSdcard() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sdcardHasLost() {
        // TODO Auto-generated method stub

    }

    @Override
    public void fileNotFoundOnHttpServer() {
        // TODO Auto-generated method stub

    }

    @Override
    public void downloadThreadInterrupted() {
        // TODO Auto-generated method stub

    }

    @Override
    public void downloadProgressChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void downloadApkSuccess() {
        mEventInfo.downloadComplete(mContext);
        if (Util.isApkFileUsable(mContext, mEventInfo.getApkFile(), mEventInfo.getVersionCode())) {
            final boolean userUpdateFlag = DownloadSettings.getUserUpdateAutoFlag(mContext);
            if (!userUpdateFlag) {
                mEventInfo.setUpdateFlagTo704();
            }
            reportDownloadResult();
            
            mCallback.installFile(mEventInfo);
        } else {
            mEventInfo.getApkFile().delete();
        }
    }

    @Override
    public void pausedByUser() {
        // TODO Auto-generated method stub

    }

    @Override
    public void canceledByUser() {
        // TODO Auto-generated method stub

    }

    
    public interface TaskCallback {
        void removeAutoUpdateInfo(AutoUpdateEventInfo eventInfo);
        void installFile(AutoUpdateEventInfo eventInfo);
    }
}
