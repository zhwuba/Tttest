package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

/**
 * 应用或游戏的分类信息请求对象
 * @author JLu
 *
 */
public class GetSoftGameTopicReq {

	@Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo = new TerminalInfo();

    @Expose
    @SerializedName("topicId")
    private int topicId = -1;

    @Expose
    @SerializedName("path")
    private String path;
    
    @Expose
    @SerializedName("start")
    private int start;
    
    @Expose
    @SerializedName("fixed")
    private int fixedLength;
    
    @Expose
    @SerializedName("marketId")
    private String marketId;
    
    
	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getFixedLength() {
		return fixedLength;
	}

	public void setFixedLength(int fixedLength) {
		this.fixedLength = fixedLength;
	}

	public String getMarketId() {
		return marketId;
	}

	public void setMarketId(String marketId) {
		this.marketId = marketId;
	}
    
	
	private int topicIndex;
    
    private int channelIndex;

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
