package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetUserCommentReq {

	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo;

	@Expose
	@SerializedName("resId")
	private int resId;
	
	@Expose
	@SerializedName("start")
	private int start;
	
	@Expose
	@SerializedName("fixed")
	private int fixed;

	@Expose
	@SerializedName("pName")
	private String pName;
	
	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}
	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getFixed() {
		return fixed;
	}

	public void setFixed(int fixed) {
		this.fixed = fixed;
	}
	
}
