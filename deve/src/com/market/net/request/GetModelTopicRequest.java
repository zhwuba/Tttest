package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.ModelListInfoBto;

public class GetModelTopicRequest extends GetBaseRequest{

	@Expose
	@SerializedName("topicId")
	private int topicId;
	
	@Expose
	@SerializedName("path")
	private String	path;	// marketId+"@"+channel+"@"+topicName


    private int topicIndex;
    
    private int channelIndex;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	
	
	public int getTopicIndex() {
		return topicIndex;
	}
	
	
	public void setTopicIndex(int topicIndex) {
		this.topicIndex = topicIndex;
	}
	
	
	public int getChannelIndex() {
		return channelIndex;
	}
	
	
	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}

}	
