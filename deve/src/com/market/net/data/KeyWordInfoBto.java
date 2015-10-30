package com.market.net.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class KeyWordInfoBto {
	@Expose
	@SerializedName("key")
	private String key;

	@Expose
	@SerializedName("searchDownload")
	private int searchDownload; 

	@Expose
	@SerializedName("integral")
	private int integral;      //积分值

	@Expose
	@SerializedName("activeType")
	private int activeType;    //下载有礼类型 0积分 大于0礼包 小于0 不是下载有礼

	@Expose
	@SerializedName("appInfoBto")
	private AppInfoBto appInfoBto;

	public AppInfoBto getAppInfoBto() {
		return appInfoBto;
	}

	public void setAppInfoBto(AppInfoBto appInfoBto) {
		this.appInfoBto = appInfoBto;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getSearchDownload(){
		return searchDownload;
	}

	public void setSearchDownload(int searchDownload){
		this.searchDownload = searchDownload;
	}

	public int getActiveType(){
		return activeType;
	}

	public void setActiveType(int activeType){
		this.activeType = activeType;
	}

	public int getIntegral(){
		return integral;
	}

	public void setIntegeral(int integral){
		this.integral = integral;
	}

}
