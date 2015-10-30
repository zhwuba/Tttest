package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.ChannelInfoBto;
import com.market.net.data.HotSearchInfoBto;


public class GetMarketFrameResp extends BaseInfo {

	@Expose
	@SerializedName("marketId")
	private String marketId;

	// 渠道开关
	@Expose
	@SerializedName("isForcedUp")
	private int isForcedUp;

	@Expose
	@SerializedName("chLst")
	private List<ChannelInfoBto> channelList = new ArrayList<ChannelInfoBto>();


	@Expose
	@SerializedName("hotSearchList")
	private List<HotSearchInfoBto> hotSearchList;

	//退出市场时提示语
	@Expose
	@SerializedName("exitTip")
	private String exitTip;
	
	
	@Expose
	@SerializedName("logSwitch")
	private int logSwitch;
	
	
	public void setLogSwitch(int logSwitch) {
	    this.logSwitch = logSwitch;
	}
	
	
	public int getLogSwitch() {
	    return logSwitch;
	}
	

	public List<HotSearchInfoBto> getHotSearchList() {
		return hotSearchList;
	}

	public void setHotSearchList(List<HotSearchInfoBto> hotSearchList) {
		this.hotSearchList = hotSearchList;
	}

	public String getMarketId() {
		return marketId;
	}

	public void setIsForcedUp(int isForcedUp) {
		this.isForcedUp = isForcedUp;
	}

	public int getIsForcedUp() {
		return isForcedUp;
	}

	public void setMarketId(String marketIdx) {
		this.marketId = marketIdx;
	}

	public List<ChannelInfoBto> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<ChannelInfoBto> channelList) {
		this.channelList = channelList;
	}

	public void addChannelInfo(ChannelInfoBto channelInfo) {
		channelList.add(channelInfo);
	}

	public String getExitTip() {
		return exitTip;
	}

	public void setExitTip(String exitTip) {
		this.exitTip = exitTip;
	}


}
