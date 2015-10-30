package com.market.download.updates;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.statistics.ReportFlag;


public class AutoUpdateEventInfo extends DownloadEventInfo {

    private int mUpdateFlag = 0;

    public static final int UP_FLAG_CLOSE = 0;
    public static final int UP_FLAG_DOWN = 1;
    public static final int UP_FLAG_INSTALL = 2;

    public AutoUpdateEventInfo(String pkgName, String appName, String md5, String url,
            boolean downBg, boolean downOnlyWifi, int updateFlag, int verCode, int appId, String diffDownUrl) {
        super(pkgName, appName, md5, url, ReportFlag.TOPIC_NULL, ReportFlag.FROM_AUTO_UPDATE, downBg, downOnlyWifi, true,
                verCode, appId, diffDownUrl, 0, 0);

        mUpdateFlag = updateFlag;
    }

    
    private AutoUpdateEventInfo(String supStr, int updateFlag) {
        super(supStr);
        mUpdateFlag = updateFlag;
    }
    
    
    public String getSaveString() {
        String supStr = super.getEventString();
        JSONObject jo = new JSONObject();
        try {
            jo.put("supStr", supStr);
            jo.put("updateFlag", mUpdateFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static AutoUpdateEventInfo parserSaveString(String saveStr) {
        try {
            JSONObject jo = new JSONObject(saveStr);
            String supStr = jo.getString("supStr");
            int updateFlag = jo.getInt("updateFlag");
            AutoUpdateEventInfo info = new AutoUpdateEventInfo(supStr, updateFlag);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public int getUpdateFlag() {
        return mUpdateFlag;
    }
    
    
    public void setUpdateFlagTo704() {
    	super.setDownloadFlag(ReportFlag.FROM_704);
    }
}
