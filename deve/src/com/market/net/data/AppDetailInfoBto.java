package com.market.net.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDetailInfoBto
{

    @Expose
    @SerializedName("apkName")
    private String apkName;

    @Expose
    @SerializedName("pName")
    private String packageName;

    @Expose
    @SerializedName("verCode")
    private int versionCode;

    @Expose
    @SerializedName("verName")
    private String versionName;

    @Expose
    @SerializedName("downUrl")
    private String downUrl;

    @Expose
    @SerializedName("md5")
    private String md5;

    @Expose
    @SerializedName("desc")
    private String desc;

    @Expose
    @SerializedName("fileSize")
    private long fileSize;

    @Expose
    @SerializedName("shotImg")
    private String shotImg;
    
    @Expose
    @SerializedName("label")
    private String label;
    
    @Expose
    @SerializedName("downNum")
    private String downNum;
    
    @Expose
    @SerializedName("company")
    private String company;
    
    @Expose
    @SerializedName("uptime")
    private String uptime;   
    
    @Expose
    @SerializedName("iconUrl")
    private String iconUrl;
    
    @Expose
    @SerializedName("security")
    private int security;

    @Expose
    @SerializedName("charge")
    private int charge;
    
    @Expose
    @SerializedName("isAd")
    private int isAd;
    
    @Expose
    @SerializedName("stars")
    private int stars;
    
    
    @Expose
    @SerializedName("offLogo")
    private int officialLogo;//官方标识标签
    
    @Expose
    @SerializedName("cornerMarkInfo")
    private CornerIconInfoBto cornerIconInfoBto;

    @Expose
    @SerializedName("activity")
    private String activity;
    
	public String getActivity() {
		return activity;
	}

	
	public void setActivity(String activity) {
		this.activity = activity;
	}


	public int getOfficialLogo() {
		return officialLogo;
	}

	
	public void setOfficialLogo(int officialLogo) {
		this.officialLogo = officialLogo;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public int getSecurity() {
		return security;
	}

	public void setSecurity(int security) {
		this.security = security;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public int isAd() {
		return isAd;
	}

	public void setAd(int isAd) {
		this.isAd = isAd;
	}

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public String getDownNum()
    {
        return downNum;
    }

    public void setDownNum(String downNum)
    {
        this.downNum = downNum;
    }

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getUptime()
    {
        return uptime;
    }

    public void setUptime(String uptime)
    {
        this.uptime = uptime;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getApkName()
    {
        return apkName;
    }

    public void setApkName(String apkName)
    {
        this.apkName = apkName;
    }

    public String getPackageName()
    {
        return packageName;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public int getVersionCode()
    {
        return versionCode;
    }

    public void setVersionCode(int versionCode)
    {
        this.versionCode = versionCode;
    }

    public String getVersionName()
    {
        return versionName;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public String getDownUrl()
    {
        return downUrl;
    }

    public void setDownUrl(String downUrl)
    {
        this.downUrl = downUrl;
    }

    public String getMd5()
    {
        return md5;
    }

    public void setMd5(String md5)
    {
        this.md5 = md5;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getShotImg()
    {
        return shotImg;
    }

    public void setShotImg(String shotImg)
    {
        this.shotImg = shotImg;
    }

    public long getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }


    public CornerIconInfoBto getCornerIconInfoBto() {
        return cornerIconInfoBto;
    }


    public void setCornerIconInfoBto(CornerIconInfoBto cornerIconInfoBto) {
        this.cornerIconInfoBto = cornerIconInfoBto;
    }

}
