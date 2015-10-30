package com.market.net.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WallpaperInfoBto{
	@Expose
	@SerializedName("appId")
	private int appId;
	
	@Expose
	@SerializedName("appName")
	private String appName;
	
	@Expose
	@SerializedName("imageUrl")
	private String imageUrl;
	
	@Expose
	@SerializedName("thumb")
	private String thumbImageUrl;
	
	@Expose
	@SerializedName("code")
	private String code;

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getThumbImageUrl() {
		return thumbImageUrl;
	}

	public void setThumbImageUrl(String thumbImageUrl) {
		this.thumbImageUrl = thumbImageUrl;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}