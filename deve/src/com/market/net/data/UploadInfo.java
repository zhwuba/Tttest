package com.market.net.data;

import java.io.File;
import java.util.Date;

public class UploadInfo {

	private String packageName;		
	private int versionCode;		
	private String mobileType;		
	private String manufacturer;	
	private String resolution;		
	private Date uploadDate;	
	private File uploadFile;	
	private String versionName;
	private String appName;
	
	
	public String getAppName() {
		return appName;
	}
	public String getManufacturer() {
		return manufacturer;
	}
	public String getMobileType() {
		return mobileType;
	}
	public String getPackageName() {
		return packageName;
	}
	public String getResolution() {
		return resolution;
	}
	public Date getUploadDate() {
		return uploadDate;
	}
	public File getUploadFile() {
		return uploadFile;
	}
	
	public int getVersionCode() {
		return versionCode;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public void setMobileType(String mobileType) {
		this.mobileType = mobileType;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	public void setUploadFile(File uploadFile) {
		this.uploadFile = uploadFile;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}


}
