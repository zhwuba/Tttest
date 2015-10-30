package com.zhuoyi.market.appManage.db;

import android.graphics.Bitmap;

public class FavoriteInfo {

	private String url;// 下载器网络标识
	private int appId;
	private String appName;
	private String md5;
	private String iconUrl;
	private boolean select;

	public boolean isSelect() {
		return select;
	}

	public void setSelect(boolean select) {
		this.select = select;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	private byte[] bitmap;
	private Bitmap mBitmap;
	private String fileSizeSum;
	private String localFilePath;
	private String versionCode;
	private String appPackageName;
	private String versionName;

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public FavoriteInfo(String url, String appName, String md5, byte[] bitmap,
			String fileSizeSum, String localFilePath, String versionCode,
			String appPackageName, int appId, String iconUrl) {
		super();
		this.appId = appId;
		this.url = url;
		this.appName = appName;
		this.md5 = md5;
		this.bitmap = bitmap;
		this.fileSizeSum = fileSizeSum;
		this.localFilePath = localFilePath;
		this.versionCode = versionCode;
		this.appPackageName = appPackageName;
		this.iconUrl = iconUrl;
	}

	public FavoriteInfo() {

	}

	public void setAppId(int id) {
		appId = id;
	}

	public int getAppId() {
		return appId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public Bitmap getmBitmap() {
		return mBitmap;
	}

	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public byte[] getBitmap() {
		return bitmap;
	}

	public void setBitmap(byte[] bitmap) {
		this.bitmap = bitmap;
	}

	public String getFileSizeSum() {
		return fileSizeSum;
	}

	public void setFileSizeSum(String fileSizeSum) {
		this.fileSizeSum = fileSizeSum;
	}

	public String getLocalFilePath() {
		if (localFilePath.contains("/kedou/")) {
			localFilePath = localFilePath.replace("/kedou/", "/ZhuoYiMarket/");
		}
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}

	public String getAppPackageName() {
		return appPackageName;
	}

	public void setAppPackageName(String appPackageName) {
		this.appPackageName = appPackageName;
	}

}
