package com.market.net.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppSnapshotBto
{

    @Expose
    @SerializedName("pName")
    private String packageName;

    @Expose
    @SerializedName("ver")
    private int versionCode;

    @Expose
    @SerializedName("md5")
    private String md5;
    
    
    
    public String getMd5()
    {
        return md5;
    }

    public void setMd5(String md5)
    {
        this.md5 = md5;
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
}
