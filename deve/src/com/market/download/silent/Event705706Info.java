package com.market.download.silent;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;

public class Event705706Info extends DownloadEventInfo {
    
    private int mSilentFlag;
    
    public static Event705706Info decodeStorageString(String infoStr) {
        try {
            JSONObject jo = new JSONObject(infoStr);
            String eventStr = jo.getString("base");
            int silentFlag = jo.getInt("silentFlag");
            return new Event705706Info(eventStr, silentFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public String getStorageString() {
        String baseInfoStr = super.getEventString();
        JSONObject jo = new JSONObject();
        try {
            jo.put("base", baseInfoStr);
            jo.put("silentFlag", mSilentFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo.toString();
    }
    
    
    public Event705706Info(AppInfoBto infoBto, String from) {
//        String from = "unKnown";
//        if (infoBto.getIsForcedUp() == 3) {
//            from = ReportFlag.FLAG_705;
//        } else {
//            from = ReportFlag.FLAG_706;
//        }
        
        super(infoBto.getPackageName(),
                infoBto.getName(),
                infoBto.getMd5(),
                infoBto.getDownUrl(),
                ReportFlag.TOPIC_NULL,
                from,
                false,
                true,
                true,
                infoBto.getVersionCode(),
                infoBto.getRefId(),
                infoBto.getFileSize());
        
        mSilentFlag = infoBto.getIsForcedUp();
    }
    
    
    private Event705706Info(String eventStr, int silentFlag) {
        super(eventStr);
        mSilentFlag = silentFlag;
    }

    
    public int getSilentFlag() {
        return mSilentFlag;
    }
    
}
