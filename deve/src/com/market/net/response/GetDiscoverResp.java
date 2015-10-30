package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.DiscoverInfoBto;

//@SignalCode(messageCode = 201031)
public class GetDiscoverResp extends BaseInfo{
	@Expose
	@SerializedName("topicIndex")
	private Integer topicIndex = null;
	@Expose
	@SerializedName("discoverList")
	private List<DiscoverInfoBto> discoverList;
	
	public Integer getTopicIndex() {
		return topicIndex;
	}
	public void setTopicIndex(Integer topicIndex) {
		this.topicIndex = topicIndex;
	}
	public List<DiscoverInfoBto> getDiscoverList() {
		return discoverList;
	}
	public void setDiscoverList(List<DiscoverInfoBto> discoverList) {
		this.discoverList = discoverList;
	}
}