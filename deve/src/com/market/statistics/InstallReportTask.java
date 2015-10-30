package com.market.statistics;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.market.account.dao.UserInfo;
import com.market.debug.MarketDebug;
import com.market.download.common.RunTask;
import com.market.download.httpConnect.HttpConnect;
import com.market.download.util.Util;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.statistics.ReportFlag.FromDes;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;

public class InstallReportTask extends RunTask {
    private static final String TAG = "InstallReportTask";
    
    private Context mContext;
    
    private String mFromFlag;
    private String mTopicId;
    private String mAppName;
    private String mPkgName;
    private String mAppId;
    private int mVerCode;
    private String mChildViewDes;
    
    public InstallReportTask(Context context, String fromFlag, String topicId, String appName, String pkgName, String appId, int verCode) {
        mContext = context;
        
        mFromFlag = fromFlag;
        mTopicId = topicId;
        mAppName = appName;
        mPkgName = pkgName;
        mAppId = appId;
        mVerCode = verCode;
        
        FromDes fromDes = ReportFlag.splitFromFlag(mFromFlag);
        mFromFlag = fromDes.fromFlag;
        mChildViewDes = fromDes.childView;
        if (mFromFlag.contains(ReportFlag.FROM_SEE_OTHER) && mChildViewDes != null) {
            mTopicId = ReportFlag.TOPIC_NULL;
        }
    }
    
    
    @Override
    protected void run() {
        boolean result = false;
        
        long startTimeMillis = System.currentTimeMillis();
        int count = 0;
        while (!result && count <= 3) {
            result = reportInstallResult();
            count++;
        }
        long endTimeMillis = System.currentTimeMillis();
        MarketDebug.recordReportTimeLog("report installed apk: " + mAppName, result, startTimeMillis, endTimeMillis, count);
    }

    
    public boolean reportInstallResult() {
        String content = null;
        try {
            String reqContent = getReportInstallReqContent();
            content = HttpConnect.doPost(Constant.TOTAL_URL, reqContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (content == null) {
            return false;
        }

        Util.log(TAG, "reportInstallResult", "responce content:" + content + ", app name:" + mAppName);
        try {
            JSONObject jsonObject = new JSONObject(content);
            String bodyResult = jsonObject.getString("body");
            JSONObject bodyJO = new JSONObject(bodyResult);
            if (bodyJO != null && bodyJO.has("errorCode") && bodyJO.getInt("errorCode") == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    
    private String getReportInstallReqContent() {
        JSONObject contentJO = new JSONObject();
        //
        String headerStr = null;
        JSONObject headerObject = new JSONObject();
        UUID uuid = UUID.randomUUID();
        try {
            headerObject.put("ver", 1);
            headerObject.put("type", 1);
            headerObject.put("msb", uuid.getMostSignificantBits());
            headerObject.put("lsb", uuid.getLeastSignificantBits());
            headerObject.put("mcd", MessageCode.GET_DATA_USER_OPERATE_REQ);
            headerStr = headerObject.toString();
            contentJO.put("head", headerStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (headerStr == null) {
            return null;
        }

        // body
        String bodyStr = null;

        String marketId = MarketUtils.getSharedPreferencesString(mContext, MarketUtils.KEY_MARKET_ID, null);
        if (TextUtils.isEmpty(marketId)) {
            marketId = "null";
        }
        String src = marketId;

        src += "/-1/-1/-1";
        
        try {
            TerminalInfo termInfo = SenderDataProvider.generateTerminalInfo(mContext).cloneInfo();
            if (mChildViewDes != null) {
                JSONObject reservedJo = new JSONObject(termInfo.getReserved());
                reservedJo.put("childViewDes", mChildViewDes);
                termInfo.setReserved(reservedJo.toString());
            }
            
            JSONObject jsonObjBody = new JSONObject(termInfo.toString());
            
            JSONArray appJA = new JSONArray();
            JSONObject appInfoJo = new JSONObject();
            appInfoJo.put("apkName", mAppName);
            appInfoJo.put("pName", mPkgName);
            appInfoJo.put("appId", mAppId);
            appInfoJo.put("apkV", mVerCode);
            appInfoJo.put("uuid", Util.getDeviceUUID());
            appInfoJo.put("topicId", mTopicId);
            appInfoJo.put("act", 1);
            String acountId = UserInfo.get_openid(mContext);
            if (acountId == null) {
                acountId = "";
            }
            appInfoJo.put("acountId", acountId);
            appInfoJo.put("src", src);
            appInfoJo.put("from", mFromFlag);
            appJA.put(appInfoJo);
            jsonObjBody.put("uInfo", appJA);

            bodyStr = jsonObjBody.toString();
            contentJO.put("body", bodyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (bodyStr == null) {
            return null;
        }

        return contentJO.toString();
    }

}
