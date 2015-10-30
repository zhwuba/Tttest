package com.market.net.build;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.GetModelApkListByPageReq;
import com.market.net.response.GetMarketFrameResp;
import com.zhuoyi.market.appResident.MarketApplication;

public class BuildModelApkListByPage implements DataBuilder
{
    @Override
    public String buildToJson(Context context,int msgCode,Object obj)
    {
        String result = "";

        String body = "";


        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;

          
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        GetMarketFrameResp marketResp = MarketApplication.getMarketFrameResp();
        
        GetModelApkListByPageReq listByPageReq = (GetModelApkListByPageReq)obj;
        listByPageReq.setMarketId(marketResp.getMarketId());
        listByPageReq.setScreenWidth((short)outMetrics.widthPixels);
        listByPageReq.setScreenHeight((short)outMetrics.heightPixels);
        
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        listByPageReq.setTerminalInfo(terminalInfo);
        
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        body = gson.toJson(listByPageReq);
        try
        {
            jsObject.put("head", SenderDataProvider.buildHeadData(msgCode));
            
            jsObject.put("body", body);
            
            result = jsObject.toString();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
}
