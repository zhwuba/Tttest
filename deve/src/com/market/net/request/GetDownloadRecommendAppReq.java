package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetDownloadRecommendAppReq {

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo;

    @Expose
    @SerializedName("recPId")
    private int recommendApkId;

    @Expose
    @SerializedName("recpName")
    private String recommendName;

    @Expose
    @SerializedName("fltName")
    private String filterPckName;


    public TerminalInfo getTerminalInfo() {
        return terminalInfo;
    }


    public void setTerminalInfo(TerminalInfo terminalInfo) {
        this.terminalInfo = terminalInfo;
    }


    public int getRecommendAppId() {
        return recommendApkId;
    }


    public void setRecommendAppId(int id) {
        this.recommendApkId = id;
    }


    public String getRecommendPckName() {
        return recommendName;
    }


    public void setRecommendPckName(String name) {
        this.recommendName = name;
    }


    public String getFilterPckName() {
        return filterPckName;
    }


    public void setFilterPckName(String name) {
        this.filterPckName = name;
    }

}
