package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class UserFeedbackReq {
	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo;
	
	@Expose
	@SerializedName("content")
	private String content;
	
	@Expose
	@SerializedName("openId")
	private String openId;
	
	@Expose
	@SerializedName("contact")
	private String contact;

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
	
}
