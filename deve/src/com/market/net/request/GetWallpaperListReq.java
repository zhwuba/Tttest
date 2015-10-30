package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetWallpaperListReq {
	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo = new TerminalInfo();
	
	@Expose
	@SerializedName("index")
	private int index;
	
	@Expose
	@SerializedName("type")
	private int type;
	
	@Expose
	@SerializedName("code")
	private String code;
	
	@Expose
	@SerializedName("isWide")
	private int isWide; //0窄幅 1宽幅

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public int getIsWide() {
		return isWide;
	}

	public void setIsWide(int isWide) {
		this.type = isWide;
	}	
}