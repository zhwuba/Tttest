package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Get3rdPartyDownloadResp extends BaseInfo {

    @Expose
    @SerializedName("url")
    private String url;

    @Expose
    @SerializedName("iconUrl")
    private String iconUrl;

    @Expose
    @SerializedName("pkgName")
    private String pkgName;

    @Expose
    @SerializedName("appName")
    private String appName;

    @Expose
    @SerializedName("verCode")
    private int verCode;
    
    @Expose
    @SerializedName("verName")
    private String verName;
    
    @Expose
    @SerializedName("size")
    private long size;


    public String getDownloadUrl() {
        return url;
    }


    public void setDownloadUrl(String url) {
        this.url = url;
    }


    public String getIconUrl() {
        return iconUrl;
    }


    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }


    public String getPkgName() {
        return pkgName;
    }


    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }


    public String getAppName() {
        return appName;
    }


    public void setAppName(String appName) {
        this.appName = appName;
    }
    

    public int getVerCode() {
        return verCode;
    }


    public void setVerCode(int verCode) {
        this.verCode = verCode;
    }
    
    public String getVerName() {
        return verName;
    }


    public void setSize(String verName) {
        this.verName = verName;
    }
    
    public long getSize() {
        return size;
    }


    public void setSize(long size) {
        this.size = size;
    }
}
