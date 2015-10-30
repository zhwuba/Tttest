package com.market.net.build;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.GetApkDetailReq;
import com.market.statistics.ReportFlag;
import com.market.statistics.ReportFlag.FromDes;

public class BuildApkDetailInfo implements DataBuilder
{

    @Override
    public String buildToJson(Context context, int msgCode, Object obj)
    {
        // TODO Auto-generated method stub
        String result = "";

        String body = "";

        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;
        
        GetApkDetailReq detailReq = (GetApkDetailReq)obj;
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context).cloneInfo();
        FromDes fromDes = ReportFlag.splitFromFlag(detailReq.getFrom());
        detailReq.setFrom(fromDes.fromFlag);
        try {
            
            if (fromDes.childView != null) {
                JSONObject reservedJo = new JSONObject(terminalInfo.getReserved());
                reservedJo.put("childViewDes", fromDes.childView);
                terminalInfo.setReserved(reservedJo.toString());
                if (fromDes.fromFlag.contains(ReportFlag.FROM_SEE_OTHER)) {
                    detailReq.setTopicId(ReportFlag.TOPIC_NULL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        detailReq.setTerminalInfo(terminalInfo);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        body = gson.toJson(detailReq);
        try
        {
            jsObject.put("head", SenderDataProvider.buildHeadData(msgCode));
            
            jsObject.put("body", body);
            
            result = jsObject.toString();
            
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }

}
