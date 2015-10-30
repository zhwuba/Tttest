package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.WallpaperInfoBto;

public class GetWallpaperListResp extends BaseInfo{
	@Expose
	@SerializedName("index")
	private int index;
	@Expose
	@SerializedName("appList")
	private List<WallpaperInfoBto> appList;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public List<WallpaperInfoBto> getAppList() {
		return appList;
	}
	public void setAppList(List<WallpaperInfoBto> appList) {
		this.appList = appList;
	}
}