package com.market.net.build;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.market.net.data.ChannelInfoBto;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.data.TopicInfoBto;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetMarketFrameResp;
import com.zhuoyi.market.appResident.MarketApplication;

public class BuildTopicListInfo implements DataBuilder {

    private GetTopicReq builderTopic(GetTopicReq req) {
        String marketId = ""; 
        String channelId = "";
        int topicId = 0;
        int ext = 0;
        String path = "";
        
        if(req==null)
            return null;
        
        GetMarketFrameResp marketResp = MarketApplication.getMarketFrameResp();
        
        marketId = marketResp.getMarketId();
        
        List<ChannelInfoBto> channelList = marketResp.getChannelList();
        
        ChannelInfoBto curChannelInfo = channelList.get(req.getChannelIndex());
        
        channelId =curChannelInfo.getChannelId();
        
        List<TopicInfoBto> topicList = curChannelInfo.getTopicList();
        
        TopicInfoBto curTopicInfo = topicList.get(req.getTopicIndex());
        
        ext = curTopicInfo.getExtension();
        req.setExtension(ext);
        
        if(req.getTopicId()==-1) {
            topicId = curTopicInfo.getTopicId();
            req.setTopicId(topicId);
            path = marketId + "@" + channelId + "@" + curTopicInfo.getTopicName();
        }
        else
            path = marketId + "@" + channelId + "@" + curTopicInfo.getTopicName()+"_"+req.getItem_name();
        
        req.setMarketId(marketId);
        req.setClickPath(path);
        
        return req;
        
    }
    
    
    @Override
    public String buildToJson(Context context, int msgCode, Object obj) {
        // TODO Auto-generated method stub
        String result = "";

        String body = "";

        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;
        
        if(obj == null)
            return result;
        
        GetTopicReq req  = builderTopic((GetTopicReq)obj);
        
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        
        req.setTerminalInfo(terminalInfo);
        
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        body = gson.toJson(req);
        try {
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
