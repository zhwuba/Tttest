package com.market.net.build;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.constant.Constant;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.SearchAppReq;
import com.market.net.response.GetMarketFrameResp;

public class BuildSearchAppListInfo implements DataBuilder
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
        
        SearchAppReq searchReq = (SearchAppReq)obj;
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        searchReq.setTerminalInfo(terminalInfo);
        searchReq.setFrId(Constant.CP_ID);
        
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        body = gson.toJson(searchReq);
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
