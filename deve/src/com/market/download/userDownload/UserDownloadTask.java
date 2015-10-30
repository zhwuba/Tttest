package com.market.download.userDownload;

import android.content.Context;

import com.market.download.common.DownBaseTask;
import com.market.download.userDownload.DownloadManager.DownloadMsg;

public class UserDownloadTask extends DownBaseTask {
	private static final String TAG = "UserDownloadTask";
	
	
	private DownloadEventInfo mDownInfo;
	private UserDownTaskCallback mUserDownCb;
	
	public UserDownloadTask(Context context, UserDownTaskCallback userDownCb, DownloadEventInfo downInfo) {
		super(context, downInfo);
		
		mDownInfo = downInfo;
		mUserDownCb = userDownCb;
	}
	
	
	public DownloadEventInfo getDownEventInfo() {
		return mDownInfo;
	}
	
	
	@Override
	protected void run() {
		if (mDownInfo.getApkFile().exists()) {
			downloadApkSuccess();
			
		} else {
			if (isTaskAlive()) {
				return;
			}
			downloadApkStarted();
			super.run();
		}
	}
	
	
	private void downloadApkStarted() {
		mDownInfo.downloading();
		mUserDownCb.downloadApkStarted(mDownInfo);
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
	    ListenerManager.getInstance(mContext).downInfoChanged(mDownInfo, DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE);
	}

	
	@Override
	public void downloadApkSuccess() {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void pausedByUser() {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void canceledByUser() {
		// TODO Auto-generated method stub

	}
	
	
	public interface UserDownTaskCallback {
		void downloadApkStarted(DownloadEventInfo eventInfo);
		void fileTotalSizeGet(DownloadEventInfo eventInfo);
		void noEnoughSpaceOnSdcard(DownloadEventInfo eventInfo);
		void sdcardHasLost(DownloadEventInfo eventInfo);
		void fileNotFoundOnHttpServer(DownloadEventInfo eventInfo);
		void downloadProgressChanged(DownloadEventInfo eventInfo);
		void downloadApkSuccess(DownloadEventInfo eventInfo);
	}

	
	
//	private int downloadApk(DownloadEventInfo eventInfo) {
//        int downRes = -1;
//        if (eventInfo.getApkFile().exists()) {
//            downRes = DownloadRes.DOWNLOAD_COMPLETE;
//            
//        } else {
//        	if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
//        		return DownloadRes.USER_CANCEL;
//        		
//        	} else if (eventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
//        		return DownloadRes.USER_PAUSE;
//        		
//        	}
//        	
//            addToDownloadingArray(eventInfo);
//            eventInfo.downloading();
//            downloadEventInfoChanged(eventInfo);
//            
//            downRes = mDownManager.downloadApk(eventInfo);
//
//            if (eventInfo.getCurrState() == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
//                Util.log(TAG, "downloadApk", "network disconnect, will stop download thread");
//                eventInfo.downloadFailed();
//                downloadEventInfoChanged(eventInfo);
//                mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
//                return -1;
//            } else if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
//                downRes = DownloadRes.USER_CANCEL;
//            }
//        }
//
//        switch (downRes) {
//        case DownloadRes.HTTP_ERROR:
//        case DownloadRes.THREAD_INTERRUPT:
//            eventInfo.downloadFailed();
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR);
//            break;
//
//        case DownloadRes.DOWNLOAD_COMPLETE:
//            if (eventInfo.getVersionCode() <= 0) {
//                removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
//                removeFromAvailableArray(eventInfo);
//                eventInfo.downloadComplete(mContext);
//                syncToThirdDownloadMap(eventInfo);
//                mAllDownloadEventMap.put(DownStorage.getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode()), eventInfo);
//                
//            } else {
//                eventInfo.downloadComplete(mContext);
//            }
//            downloadEventInfoChanged(eventInfo);
//            if (isApkFileUsable(eventInfo)) {
//                addToCompleteArray(eventInfo);
//                mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_DOWNLOAD_COMPLETE);
//                // downloadEventInfoChanged(eventInfo);
//                installApk(eventInfo, true);
//                reportDownloadResult(eventInfo);
//            } else {
//                eventInfo.downloadFailed();
//                downloadEventInfoChanged(eventInfo);
//                Intent notiIntent = new Intent("com.zhuoyi.market.download.fileNotUsable");
//                notiIntent.putExtra("appName", eventInfo.getAppName());
//                mContext.sendBroadcast(notiIntent);
//                mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_FILE_NOT_USABLE);
//            }
//            break;
//
//        case DownloadRes.NO_ENOUGH_SPACE:
//            eventInfo.downloadFailed();
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_NO_ENOUGH_SPACE);
//            break;
//
//        case DownloadRes.SDCARD_LOST:
//            eventInfo.downloadFailed();
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_SDCARD_LOST);
//            break;
//
//        case DownloadRes.USER_CANCEL:
//            File file = new File(eventInfo.getDownloadFilePath());
//            file.delete();
//            file = eventInfo.getApkFile();
//            file.delete();
//            removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_APK_CANCEL);
//            break;
//
//        case DownloadRes.USER_PAUSE:
//            addToPausedArray(eventInfo);
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_APK_PAUSE);
//            break;
//
//        case DownloadRes.FILE_NOT_FOUND:
//            eventInfo.downloadPause();
//            removeSavedEventInfo(eventInfo.getPkgName(), eventInfo.getVersionCode());
//            mDownManager.notifyEventInfoChange(eventInfo, DownloadMsg.MSG_FILE_NOT_FOUND);
//            fileNotFoundInServer(eventInfo);
//            break;
//        }
//        if (downRes != DownloadRes.USER_CANCEL  && downRes != DownloadRes.FILE_NOT_FOUND) {
//            downloadEventInfoChanged(eventInfo);
//        }
//
//        return downRes;
//    }
}
