package com.market.net.data;

import java.io.Serializable;
import java.util.List;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppInfoBto implements Serializable
{

	@Expose
	@SerializedName("id")
	private int refId;

	@Expose
	@SerializedName("name")
	private String name;

	@Expose
	@SerializedName("imgUrl")
	private String imgUrl;

	@Expose
	@SerializedName("pName")
	private String packageName;

	@Expose
	@SerializedName("downUrl")
	private String downUrl;

	@Expose
	@SerializedName("md5")
	private String md5;

	@Expose
	@SerializedName("type")
	private int resType;

	@Expose
	@SerializedName("fileSize")
	private long fileSize;

	@Expose
	@SerializedName("downTm")
	private int downTimes;

	@Expose
	@SerializedName("hot")
	private int hot;

	@Expose
	@SerializedName("brief")
	private String briefDesc;

	@Expose
	@SerializedName("verCode")
	private int versionCode;

	@Expose
	@SerializedName("class")
	private short classID;

	@Expose
	@SerializedName("verName")
	private String versionName;

	@Expose
	@SerializedName("isForcedUp")
	private int isForcedUp;

	@Expose
	@SerializedName("integral")
	private int integral;

	@Expose
	@SerializedName("webUrl")
	private String webUrl;

    @Expose
    @SerializedName("activityUrl")
    private String activityUrl;

	@Expose
	@SerializedName("showTime")
	private int showTime;

	@Expose
	@SerializedName("cornerMarkInfo")
	private CornerIconInfoBto cornerMarkInfo;

	@Expose
	@SerializedName("stars")
	private int stars;
	
	@Expose
	@SerializedName("active")
	private boolean active;

	@Expose
	@SerializedName("updateTm")
	private String updateTm;
	
	@Expose
	@SerializedName("patchMd5")
	private String patchMd5;
	
	@Expose
	@SerializedName("patchSize")
	private long patchSize;
	
	@Expose
	@SerializedName("patchUrl")
	private String patchUrl;
	
	@Expose
	@SerializedName("oldFileMd5")
	private String oldFileMd5;
	
	@Expose
	@SerializedName("oldVersionCode")
	private int oldVersionCode;
	
	@Expose
	@SerializedName("verUptDes")
	private String verUptDes;//版本更新描述
	
	
	@Expose
	@SerializedName("description")
	private String mDescription;

	
	@Expose
	@SerializedName("verUptTime")
	private String verUptTime;//版本更新时间

	@SerializedName("description")
	private String description;
	
	@Expose
	@SerializedName("offLogo")
	private int officialLogo;//官方标识标签

	
	@Expose
	@SerializedName("assLst")
	private List<AssemblyInfoBto> assemblyList;


	@Expose
	@SerializedName("parentId")
	private int parentId;


	
	@Expose
	@SerializedName("parentName")
	private String parentName;

	@Expose
	@SerializedName("fgColor")
	private String fgColor;
	
	@Expose
	@SerializedName("riseVal")
	private int riseVal;
	
	
	@Expose
    @SerializedName("isShow")
    private int isShow;//该应用是否显示给用户(0:显示 1：不显示)

	
	@Expose
	@SerializedName("percent")
	private String percent;//用户下载应用百分比 
	
	
	@Expose
	@SerializedName("activity")
	private String activity;
	
	
	public String getPercent() {
		return percent;
	}

	public void setPercent(String percent) {
		this.percent = percent;
	}

	private Drawable drawable;

	private String fileSizeString = "";

	
	public boolean getActive() {
		return active;
	}

	
	public String getActivityUrl() {
        return activityUrl;
    }


	public List<AssemblyInfoBto> getAssemblyList() {
		return assemblyList;
	}

	public String getBriefDesc()
	{
		return briefDesc;
	}

	public short getClassID()
	{
		return classID;
	}
	
	public CornerIconInfoBto getCornerMarkInfo() {
		return cornerMarkInfo;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getDownTimes()
	{
		return downTimes;
	}
	
	public String getDownUrl()
	{
		return downUrl;
	}
	
	
    public Drawable getDrawable()
	{
		return drawable;
	}
    
    public String getFgColor() {
		return fgColor;
	}

    public long getFileSize()
	{
		return fileSize;
	}
	
	public String getFileSizeString()
	{
		return fileSizeString;
	}

	public int getHot()
	{
		return hot;
	}

	public String getImgUrl()
	{
		return imgUrl;
	}

	public int getIntegral()
	{

		return integral;
	}

	public int getIsForcedUp() {
		return isForcedUp;
	}

	public boolean getIsShow() {
        if (isShow == 0) {
            return true;
        } else {
           return false; 
        }
    }

	public String getMd5()
	{
		return md5;
	}

	public String getmDescription() {
		return mDescription;
	}

	public String getName()
	{
		return name;
	}

	public int getOfficialLogo() {
		return officialLogo;
	}

	public String getOldFileMd5() {
		return oldFileMd5;
	}

	public int getOldVersionCode() {
		return oldVersionCode;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public int getParentId() {
		return parentId;
	}

	public String getParentName() {
		return parentName;
	}

	public String getPatchMd5() {
		return patchMd5;
	}

	public long getPatchSize() {
		return patchSize;
	}

	public String getPatchUrl() {
		return patchUrl;
	}

	public int getRefId()
	{
		return refId;
	}

	public int getResType()
	{
		return resType;
	}
	
    public int getRiseVal() {
		return riseVal;
	}

    public int getShowTime() {
		return showTime;
	}

	public int getStars() {
		return stars;
	}


	public String getUpdateTm() {
		return updateTm;
	}

	public int getVersionCode()
	{
		return versionCode;
	}


	public String getVersionName()
	{
		return versionName;
	}

	public String getVerUptDes() {
		return verUptDes;
	}	


	public String getVerUptTime() {
		return verUptTime;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setActivityUrl(String activityUrl) {
        this.activityUrl = activityUrl;
    }

	public void setAssemblyList(List<AssemblyInfoBto> assemblyList) {
		this.assemblyList = assemblyList;
	}

	public void setBriefDesc(String briefDesc)
	{
		this.briefDesc = briefDesc;
	}

	public void setClassID(short classID)
	{
		this.classID = classID;
	}

	public void setCornerMarkInfo(CornerIconInfoBto cornerIcon) {
		this.cornerMarkInfo = cornerIcon;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDownTimes(int downTimes)
	{
		this.downTimes = downTimes;
	}

	public void setDownUrl(String downUrl)
	{
		this.downUrl = downUrl;
	}

	public void setDrawable(Drawable drawable)
	{
		this.drawable = drawable;
	}

	public void setFgColor(String fgColor) {
		this.fgColor = fgColor;
	}

	public void setFileSize(long fileSize)
	{
		this.fileSize = fileSize;
	}

	public void setFileSizeString(String fileSizeString)
	{
		this.fileSizeString = fileSizeString;
	}

	public AppInfoBto setFileSizeToString(String sizeString)
	{
		fileSizeString = sizeString;
		return this;
	}

	public void setHot(int hot)
	{
		this.hot = hot;
	}

	public void setImgUrl(String imgUrl)
	{
		this.imgUrl = imgUrl;
	}

	public void setIntegral(int integral)
	{

		this.integral = integral;
	}

	public void setIsForcedUp(int isForcedUp) {
		this.isForcedUp = isForcedUp;
	}

	public void setIsShow(int isShow) {
        this.isShow = isShow;
    }

	public void setMd5(String md5)
	{
		this.md5 = md5;
	}

	public void setmDescription(String mDescription) {
		this.mDescription = mDescription;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setOfficialLogo(int officialLogo) {
		this.officialLogo = officialLogo;
	}

	public void setOldFileMd5(String oldFileMd5) {
		this.oldFileMd5 = oldFileMd5;
	}

	public void setOldVersionCode(int oldVersionCode) {
		this.oldVersionCode = oldVersionCode;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public void setPatchMd5(String patchMd5) {
		this.patchMd5 = patchMd5;
	}

	public void setPatchSize(long patchSize) {
		this.patchSize = patchSize;
	}

	public void setPatchUrl(String patchUrl) {
		this.patchUrl = patchUrl;
	}

	public void setRefId(int refId)
	{
		this.refId = refId;
	}
	
	public void setResType(int resType)
	{
		this.resType = resType;
	}

	public void setRiseVal(int riseVal) {
		this.riseVal = riseVal;
	}

	public void setShowTime(int showTime) {
		this.showTime = showTime;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public void setUpdateTm(String updateTm) {
		this.updateTm = updateTm;
	}

	public void setVersionCode(int versionCode)
	{
		this.versionCode = versionCode;
	}

	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}

	public void setVerUptDes(String verUptDes) {
		this.verUptDes = verUptDes;
	}

	public void setVerUptTime(String verUptTime) {
		this.verUptTime = verUptTime;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}
}
