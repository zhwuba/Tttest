package com.market.download.userDownload;

import com.market.download.common.DownBaseInfo;
import com.market.download.updates.AppUpdateManager;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.util.Util;
import com.zhuoyi.market.appManage.download.DownloadView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class ListenerManager {
	private static final String TAG = "ListenerManager";
	
	private static ListenerManager mSelf = null;
	
	private Context mContext;
	
	public static ListenerManager getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new ListenerManager(context);
		}
		
		return mSelf;
	}
	
	
	ListenerManager(Context context) {
		mContext = context;
	}
	
	
	private Messenger mClientMsger;
	private Messenger mManagerMsger;
	
	
	public void setClientMsgHandler(Messenger msger) {
		mClientMsger = msger;
	}
	
	
	public void setManagerMsgHandler(Messenger msger) {
		mManagerMsger = msger;
	}
	
	
	public void downInfoChanged(DownloadEventInfo eventInfo, int eventFlag) {
        Util.log(TAG, "notifyEventInfoChange", "event flag=" + eventFlag);
//        if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_UPDATE
//                || eventInfo.getEventArray() == DownloadEventInfo.ARRAY_BACKGROUND) {
//            return;
//        }

        if (eventFlag == DownloadMsg.MSG_INSTALLED && eventInfo.getCurrState() != DownBaseInfo.STATE_INSTALLED) {
        	eventInfo.installed();
        	Util.log(TAG, "downloadEventInfoChanged", "event string:" + eventInfo.getEventString());
            // getCaller();
            if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_UPDATE) {
                return;
            }
            
            DownStorage mStorage = DownStorage.getInstance(mContext);
            synchronized (mStorage) {
                if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                    Util.log(TAG, "downloadEventInfoChanged", "package name:" + eventInfo.getPkgName()  + ", this event has been canceled, do not save");
                    return;
                }
                
                mStorage.savaEventInfo(eventInfo);
            }
        }
        
        AppUpdateManager.getInstance(mContext).downloadEventInfoChange(eventInfo);

        if (mClientMsger != null) {
            Message msg = new Message();
            msg.what = eventFlag;
            if (eventFlag == DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE) {
                msg.obj = eventInfo.getSpeedEventStr();
            } else {
                msg.obj = eventInfo.getEventString();
            }
            try {
                mClientMsger.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // TBD

        }

        if (mManagerMsger != null) {
            Message msg = new Message();
            msg.what = eventFlag;
            if (eventFlag == DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE) {
                msg.obj = eventInfo.getSpeedEventStr();
            } else {
                msg.obj = eventInfo.getEventString();
            }
            try {
                mManagerMsger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            // TBD

        }
        
        if (eventFlag == DownloadMsg.MSG_DOWNLOAD_COMPLETE) {
            Intent intent = new Intent();
            intent.setAction("com.zhuoyi.market.DOWNLOAD_COMPLETE");
            intent.putExtra("packageName", eventInfo.getPkgName());
            intent.putExtra("versionCode", eventInfo.getVersionCode());
            mContext.sendBroadcast(intent);
        }
        
        //send broadcast for external download, to notify download state changed
        exterBroadcastSend(eventInfo, eventFlag);
    }
	
	
	private static final int EXTER_STATE_WAITING = 1;
	private static final int EXTER_STATE_NO_APK = 2;
	private static final int EXTER_STATE_START_DOWN = 3;
	private static final int EXTER_STATE_PAUSED = 4;
	private static final int EXTER_STATE_CANCEL = 5;
	private static final int EXTER_STATE_DOWN_SUCCESS = 6;
	private static final int EXTER_STATE_INSTALLED = 7;
	
	
	public void exterBroadcastSend(DownloadEventInfo eventInfo, int eventFlag) {
	    ComponentName cn = eventInfo.getReceiverComponentName();
	    if (cn == null) {
	        return;
	    }
	    
	    int downState = getExterBroadcastState(eventFlag);
	    if (downState == 0) {
	        return;
	    }
	    Intent intent = new Intent();
	    intent.setAction("com.zhuoyi.market.cloud.downStateNotify");
	    intent.setComponent(cn);
	    intent.putExtra("packageName", eventInfo.getPkgName());
	    intent.putExtra("downState", downState);
	    mContext.sendBroadcast(intent);
	}
	
	private int getExterBroadcastState(int eventFlag) {
	    int downState = 0;
        
        switch(eventFlag) {
        case DownloadMsg.MSG_APK_CANCEL:
            downState = EXTER_STATE_CANCEL;
            break;
            
        case DownloadMsg.MSG_APK_DOWNLOADING:
            downState = EXTER_STATE_START_DOWN;
            break;
            
        case DownloadMsg.MSG_APK_WAIT_DOWNLOAD:
            downState = EXTER_STATE_WAITING;
            break;
            
        case DownloadMsg.MSG_DOWNLOAD_COMPLETE:
            downState = EXTER_STATE_DOWN_SUCCESS;
            break;
            
        case DownloadMsg.MSG_FILE_NOT_FOUND:
            downState = EXTER_STATE_NO_APK;
            break;
            
        case DownloadMsg.MSG_APK_PAUSE:
        case DownloadMsg.MSG_DOWNLOAD_HTTP_ERROR:
        case DownloadMsg.MSG_FILE_NOT_USABLE:
        case DownloadMsg.MSG_NO_ENOUGH_SPACE:
        case DownloadMsg.MSG_SDCARD_LOST:
            downState = EXTER_STATE_PAUSED;
            break;
            
        case DownloadMsg.MSG_INSTALLED:
            downState = EXTER_STATE_INSTALLED;
            break;
            
        case DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE:
        case DownloadMsg.MSG_INSTALL_FAILED:
        case DownloadMsg.MSG_INSTALLING:
            //do nothing
            break;
        
        }
        
        return downState;
	}
	
	
	
	private static int mNewDownloadAppId = -1;
    private static String mNewDownloadPacName = null;
    private static Handler mDownloadViewHandler = null;

    public static void setDownloadViewHandler(Handler handler) {
        mDownloadViewHandler = handler;
    }
    
    
    public void sendRefreshViewBroadcast() {
        mContext.sendBroadcast(new Intent("download.refresh"));
    }
    
    
    public void newDownloadEventAdded(DownloadEventInfo eventInfo, boolean refreshView) {
        sendRefreshViewBroadcast();

        if(eventInfo.getEventArray() != DownloadEventInfo.ARRAY_COMPLETE){
            mNewDownloadAppId = eventInfo.getAppId();
            mNewDownloadPacName = eventInfo.getPkgName();
        }
        if (refreshView && mDownloadViewHandler != null) {
            Message msg = new Message();
            msg.what = DownloadView.HANDLER_ADD_TO_DOWNLOAD;
            msg.obj = eventInfo;
            mDownloadViewHandler.sendMessage(msg);
        }
    }
    
    
    public static int getNewDownloadAppId() {
        return mNewDownloadAppId;
    }
    
    
    public static String getNewDownloadPacName() {
        return mNewDownloadPacName;
    }
    
    
}
