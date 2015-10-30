package com.zhuoyi.market.appManage.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.market.download.common.DownloadSettings;
import com.market.download.service.DownloadService;
import com.market.download.updates.AppUpdateManager;
import com.market.download.util.NotifyUtil;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.request.GetAppsUpdateReq;
import com.market.net.response.GetAppsUpdateResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.MarketUtils;

public class AppsUpdateManager {
    
    private Context mContext = null;
    private int mFoundUpdateCount = 0;
    private final static int APPS_UPDATE_REQUST = 0;
    private final static int APPS_UPDATE_RESPONSE = 1;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case APPS_UPDATE_REQUST:
                GetAppsUpdateReq appUpdateReq = new GetAppsUpdateReq();
                String contents = SenderDataProvider.buildToJSONData(mContext.getApplicationContext(),MessageCode.GET_APPS_UPDATE,appUpdateReq);
                StartNetReqUtils.execListByPageRequest(
                        mHandler,
                        APPS_UPDATE_RESPONSE,
                        MessageCode.GET_APPS_UPDATE,
                        contents);
                mFoundUpdateCount ++;
                break;
            case APPS_UPDATE_RESPONSE:
                int number = 0;
                HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
                GetAppsUpdateResp resp = null;
                List<AppInfoBto> appInfoList = null;
                List<AppInfoBto> silentList = null;
                
                if (mContext == null) break;
                
                if (map != null && map.size() > 0) {
                    resp = (GetAppsUpdateResp) map.get("appsUpdate");
                    if (resp != null){
                        appInfoList = resp.getAppList();
                        silentList = resp.getSilentAppList();
                        //yphuang add for period get update list
                        long firstGetTime = DownloadSettings.setFirstGetUpdateListTime(mContext.getApplicationContext());
                        int SwitchFor704 = resp.getIsForcedUp();
                        long currSystemTime = System.currentTimeMillis();
                        if (SwitchFor704 == 1) {
                            long passTimeOf704 = resp.getSpreadTime() * 24 * 60 * 60 * 1000;
                            if ((currSystemTime - firstGetTime) >= passTimeOf704) {
                                DownloadSettings.setUpdateAutoFlag(mContext.getApplicationContext(), SwitchFor704);
                            }
                        }else {
                            DownloadSettings.setUpdateAutoFlag(mContext.getApplicationContext(), SwitchFor704);
                        }
                        
                        int zeroServiceSwitch = resp.getIsZeroFlow();
                        if (zeroServiceSwitch == 1) {
                            long passTimeOfZero = resp.getZeroFlowTime() * 24 * 60 * 60 * 1000;
                            boolean hasOpenedYet = DownloadSettings.hasServiceOpenUserUpdateFlag(mContext.getApplicationContext());
                            if (!hasOpenedYet && (currSystemTime - firstGetTime) >= passTimeOfZero) {
                                DownloadSettings.setUserUpdateAutoFlag(mContext.getApplicationContext(), true);
                                DownloadSettings.setServerOpenUserUpdateFlag(mContext.getApplicationContext());
                            }
                        }
                        
                        DownloadSettings.setGetUpdateMillis(mContext.getApplicationContext());
                    }
                    if (appInfoList != null) {
                        number = appInfoList.size() - AppUpdateManager.getUpdateIgnoreList(mContext).size();	//更新总数减去忽略更新的数量
                        MarketApplication.setAppUpdateList(appInfoList);
                    }
                }

                if (number > 0) {
                    // yphuang add for auto download app update
                    DownloadService.notifyFoundAppUpdate(mContext.getApplicationContext());
                } else {
                    if(mFoundUpdateCount < 2){
                        requstUpdateAppsMessage();
                    }
                }
                
                // yphuang add for silent replace applications
                if (silentList != null && silentList.size() > 0) {
                    MarketApplication.setSilentUpdateList(silentList);
                    DownloadService.notifyFoundSilentUpdateApps(mContext.getApplicationContext());
                }
                break;
            }
        }
    };
    
    public AppsUpdateManager(Context context) {
        mContext = context;
    } 
    
    
    /**
     * 释放资源
     */
    public void releaseRes() {
        if (mHandler != null) {
            if (mHandler.hasMessages(APPS_UPDATE_REQUST)) {
                mHandler.removeMessages(APPS_UPDATE_REQUST);
            }
            if (mHandler.hasMessages(APPS_UPDATE_RESPONSE)) {
                mHandler.removeMessages(APPS_UPDATE_RESPONSE);
            }
            mHandler = null;
        }
        mContext = null;
    }
    
    /**
     * 发送消息请求应用更新列表
     */
    public void requstUpdateAppsMessage() {
        mHandler.sendEmptyMessageDelayed(APPS_UPDATE_REQUST, 1000);
    }
    
}
