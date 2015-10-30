package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetWallpaperDetailReq{
	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo = new TerminalInfo();
	
	@Expose
	@SerializedName("appId")
	private int appId;

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}
}
