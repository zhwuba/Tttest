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
import com.market.net.request.GetSoftGameTopicReq;
import com.market.net.response.GetMarketFrameResp;
import com.zhuoyi.market.appResident.MarketApplication;

public class BuildSoftGameTopicInfo implements DataBuilder {

	@Override
	public String buildToJson(Context context, int msgCode, Object obj) {

		String result = "";
		String body = "";
		JSONObject jsObject = new JSONObject();

		if (context == null)
			return result;

		GetSoftGameTopicReq req = builderTopic((GetSoftGameTopicReq)obj);
		
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

	private GetSoftGameTopicReq builderTopic(GetSoftGameTopicReq req) {
		String marketId = ""; 
		String channelId = "";
		int topicId = 0;
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

		topicId = curTopicInfo.getTopicId();
		req.setTopicId(topicId);
		path = marketId + "@" + channelId + "@" + curTopicInfo.getTopicName();

		req.setMarketId(marketId);
		req.setPath(path);

		return req;

	}

}
