package com.market.statistics;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import com.zhuoyi.market.constant.Constant;
import com.market.download.common.RunTask;
import com.market.download.httpConnect.HttpConnect;
import com.market.download.util.Util;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.statistics.ReportFlag.FromDes;

import android.content.Context;
import android.text.TextUtils;

/**
 * for report the user action to server
 * @author Athlon
 *
 */
public class OffLineReportTask extends RunTask {
	private static final String TAG = "OffLineReportTask";
	
	private String mAction;
	private String mFrom;
	private String mPkgName;
	private String mChildViewDes = null;
	
	private Context mContext;
	
	public OffLineReportTask(Context context, String action, String from) {
		mContext = context;
		mAction = action;
		mFrom = from;
		
		FromDes fromDes = ReportFlag.splitFromFlag(mFrom);
		mFrom = fromDes.fromFlag;
        mChildViewDes = fromDes.childView;
	}
	
	
	public OffLineReportTask(Context context, String action, String from, String pkgName) {
		mPkgName = pkgName;
		mContext = context;
		mAction = action;
		mFrom = from;
	}
	
	
	@Override
	protected void run() {
        boolean result = false;

        result = reportOffLineLog();
        int count = 1;
        while (!result && count <= 3) {
            result = reportOffLineLog();
            count++;
        }
	}

    
    private boolean reportOffLineLog() {
        String content = null;
        try {
            content = HttpConnect.doPost(Constant.MARKET_URL, getReportOffLineRequestContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (content == null) {
            return false;
        }

        Util.log(TAG, "reportOffLineLog", "responce content:" + content);
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
    
    
    private String getReportOffLineRequestContent() {
        JSONObject contentJO = new JSONObject();

        // header
        String headerStr = null;

        JSONObject headerObject = new JSONObject();
        UUID uuid = UUID.randomUUID();
        try {
            headerObject.put("ver", 1);
            headerObject.put("type", 1);
            headerObject.put("msb", uuid.getMostSignificantBits());
            headerObject.put("lsb", uuid.getLeastSignificantBits());
            headerObject.put("mcd", MessageCode.REPORT_OFFLINE_LOG);
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
        String pkgName = mContext.getPackageName();

        String marketId = com.zhuoyi.market.utils.MarketUtils.getSharedPreferencesString(mContext, com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
        if (TextUtils.isEmpty(marketId)) {
            marketId = "null";
        }
        
        try {
            TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(mContext).cloneInfo();
            if (mAction.equals(ReportFlag.ACTION_VIEW_COLUMN)
            		&& mFrom.equals(ReportFlag.FROM_HOME_AD)) {
            	terminalInfo.setPackageName(mPkgName);
            } else {
            	terminalInfo.setPackageName(pkgName);
            }
            if (mChildViewDes != null) {
                JSONObject reservedJo = new JSONObject(terminalInfo.getReserved());
                reservedJo.put("childViewDes", mChildViewDes);
                terminalInfo.setReserved(reservedJo.toString());
            }
            JSONObject jsonObjBody = new JSONObject(terminalInfo.toString());
            
            jsonObjBody.put("marketId", marketId);
            String entryTime = "";
            String exitTime = "";
            if (mAction.equals(ReportFlag.ACTION_EXIST_MARKET)) {
            	entryTime = ReportManager.getEntryMarketTime(mContext);
            	exitTime = ReportManager.getCurrDateString();
            }
            jsonObjBody.put("entryTime", entryTime);
            jsonObjBody.put("exitTime", exitTime);
            jsonObjBody.put("action", mAction);
            jsonObjBody.put("afrom", mFrom);

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
