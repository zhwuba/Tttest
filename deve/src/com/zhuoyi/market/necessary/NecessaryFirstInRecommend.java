package com.zhuoyi.market.necessary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetApkListByPageResp;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.view.CommonLoadingManager;
import com.zhuoyi.market.utils.MarketUtils;

public class NecessaryFirstInRecommend {

    private static ArrayList<AppInfoBto> mInstallAppList = null;
    private final int UPDATE_PAGE_MSG = 1;
    private final int REFRESH_LIST = 2;
    private final int START_REQUEST_LIST_APP = 3;
    private final int FIRST_REFRESH = 4;
    private final int START_INSTALL_NECESSARY = 5;
    private Context mContext = null;

    private boolean mInstallNecessaryLoadFinish = false;
    private int mInstallNecessaryLoadTime = 0;
    private boolean mInstallNecessaryShow = false;

    private GetTopicReq mTopicReq;


    public static ArrayList<AppInfoBto> getInstallAppList() {
        return mInstallAppList;
    }


    public static void clearInstallAppList() {
        if (mInstallAppList != null && mInstallAppList.size() > 0) {
            mInstallAppList.clear();
            mInstallAppList = null;
        }
    }

    private Handler mFirstTimeInHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            HashMap<String, Object> map;
            List<AppInfoBto> list = null;
            int i = 0;
            int getDataSuccess = 0;
            switch (message.what) {
            case START_REQUEST_LIST_APP:
                startRequestInstallApp();
                break;
            case START_INSTALL_NECESSARY:
                if (mInstallNecessaryLoadFinish) {
                    startInstalledNecessaryFirstTime();
                } else {
                    startInstalledNecessaryFirstTimeFromMessage();
                }
                break;
            case UPDATE_PAGE_MSG:
                GetTopicResp mTopicResp = null;
                map = (HashMap<String, Object>) message.obj;
                if (map != null && map.size() > 0) {
                    mTopicResp = (GetTopicResp) map.get("topicResp");
                    map.clear();
                    getTopicApplist(mTopicResp);
                }
                break;
            case REFRESH_LIST:
                GetApkListByPageResp listResp = null;
                getDataSuccess = message.arg1;
                AppInfoBto appInfo;
                if (getDataSuccess == 1) {
                    if (FIRST_REFRESH == message.arg2)
                        list = (List<AppInfoBto>) message.obj;
                    else {
                        map = (HashMap<String, Object>) message.obj;
                        if (map != null) {
                            listResp = (GetApkListByPageResp) map.get("listByPage");
                            map.clear();
                            list = listResp.getAppList();
                        }
                    }
                    clearInstallAppList();
                    mInstallAppList = new ArrayList<AppInfoBto>();

                    for (i = 0; list != null && i < list.size(); i++) {
                        appInfo = list.get(i);
                        appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(), false));
                        mInstallAppList.add(appInfo);
                    }
                    if (list != null)
                        list.clear();
                    mInstallNecessaryLoadFinish = true;
                }
                break;
            }
        }
    };


    public NecessaryFirstInRecommend(Context context) {
        mContext = context;
    }


    public void startInstalledNecessaryFirstTimeFromMessage() {
        Message msg = new Message();
        msg.what = START_INSTALL_NECESSARY;
        if (mFirstTimeInHandler.hasMessages(START_INSTALL_NECESSARY)) {
            mFirstTimeInHandler.removeMessages(START_INSTALL_NECESSARY);
        }

        mInstallNecessaryLoadTime++;
        if (mInstallNecessaryLoadTime > 5) {
            mInstallNecessaryLoadTime = 0;
            return;
        } else {
            mFirstTimeInHandler.sendMessageDelayed(msg, 1000);
        }
    }


    public void setSplashDestroy() {
        mInstallNecessaryShow = true;
        clearInstallAppList();
        mContext = null;
    }


    private void startInstalledNecessaryFirstTime() {

        if (mInstallNecessaryShow)
            return;

        if (!CommonLoadingManager.get().getLoadingInterfaceFinish()) {
            mInstallNecessaryShow = true;
            clearInstallAppList();
            return;
        }

        if ((mInstallAppList != null) && (mInstallAppList.size() > 0)) {
            Intent i = new Intent(mContext, NecessaryDialogActivity.class);
            i.putExtra("start_installed_necessary", true);
            mContext.startActivity(i);

            mInstallNecessaryShow = true;
        }
    }


    public void startRequestInstallAppFromMessage() {
        Message msg = new Message();
        msg.what = START_REQUEST_LIST_APP;
        mFirstTimeInHandler.sendMessage(msg);
    }


    private void startRequestInstallApp() {
    	if (mContext == null) return;
        try {
            mTopicReq = new GetTopicReq();
            mTopicReq.setChannelIndex(0);
            mTopicReq.setTopicIndex(3);
            String contens = SenderDataProvider.buildToJSONData(mContext.getApplicationContext(),
                MessageCode.GET_TOPIC_LIST, mTopicReq);
            StartNetReqUtils.execListByPageRequest(mFirstTimeInHandler, UPDATE_PAGE_MSG, MessageCode.GET_TOPIC_LIST,
                contens);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getTopicApplist(GetTopicResp resp) {

        List<AppInfoBto> appInfoList = new ArrayList<AppInfoBto>();
        int result = 0;
        Message msg = null;
        try {
            while (true) {
                List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();

                if (assemblyList == null || assemblyList.size() <= 0)
                    break;

                for (AssemblyInfoBto assemblyInfoBto : assemblyList) {
                    if (assemblyInfoBto.getAppInfoList() != null) {
                        appInfoList.addAll(assemblyInfoBto.getAppInfoList());
                    }
                }
                result = 1;
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        msg = new Message();
        msg.what = REFRESH_LIST;
        msg.obj = appInfoList;
        msg.arg2 = FIRST_REFRESH;
        msg.arg1 = result;
        mFirstTimeInHandler.sendMessage(msg);

    }
}
