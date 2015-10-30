package com.market.net.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DiscoverAppInfoBto implements Serializable{
	/**
     * <code>serialVersionUID</code> - {description}.
     */
    private static final long serialVersionUID = 8797892294471496627L;
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
	@SerializedName("packageName")
	private String packageName;
	@Expose
	@SerializedName("downloadUrl")
	private String downloadUrl;
	@Expose
	@SerializedName("fileSize")
	private long fileSize;
	@Expose
	@SerializedName("versionCode")
	private int versionCode;
	@Expose
	@SerializedName("activityUrl")
	private String activityUrl;
	@Expose
	@SerializedName("md5")
	private String md5;
	@Expose
	@SerializedName("thumb")
	private String thumbImageUrl;
	@Expose
	@SerializedName("cornerMarkInfo")
	private CornerIconInfoBto cornerMarkInfo;
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public CornerIconInfoBto getCornerMarkInfo() {
		return cornerMarkInfo;
	}
	public void setCornerMarkInfo(CornerIconInfoBto cornerMarkInfo) {
		this.cornerMarkInfo = cornerMarkInfo;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getActivityUrl() {
		return activityUrl;
	}
	public void setActivityUrl(String activityUrl) {
		this.activityUrl = activityUrl;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getThumbImageUrl() {
		return thumbImageUrl;
	}
	public void setThumbImageUrl(String thumbImageUrl) {
		this.thumbImageUrl = thumbImageUrl;
	}
	
}