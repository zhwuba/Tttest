package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetSubjectReq{
	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo = new TerminalInfo();
	
	@Expose
	@SerializedName("topicId")
	private int topicId;
	
	
	@Expose
	@SerializedName("index")
	private int index;
	

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


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}
	
}