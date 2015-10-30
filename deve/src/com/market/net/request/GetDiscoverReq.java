package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;
import com.market.net.response.BaseInfo;

//@SignalCode(messageCode = 101031)
public class GetDiscoverReq extends BaseInfo{
	
    @Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo = new TerminalInfo();
	
	@Expose
	@SerializedName("topicIndex")
	private Integer topicIndex;
	
	@Expose
	@SerializedName("marketId")
	private String marketId;
	
	@Expose
    @SerializedName("isWide")
    private int isWide; //0窄幅 1宽幅

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public Integer getTopicIndex() {
		return topicIndex;
	}

	public void setTopicIndex(Integer topicIndex) {
		this.topicIndex = topicIndex;
	}

	public String getMarketId() {
		return marketId;
	}

	public void setMarketId(String marketId) {
		this.marketId = marketId;
	}

    public int getIsWide() {
        return isWide;
    }

    public void setIsWide(int isWide) {
        this.isWide = isWide;
    }
}