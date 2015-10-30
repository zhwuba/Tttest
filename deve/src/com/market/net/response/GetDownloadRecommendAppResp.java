package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;

public class GetDownloadRecommendAppResp extends BaseInfo {

    @Expose
    @SerializedName("expire")
    private int nextReqTime;

    @Expose
    @SerializedName("appLst")
    private List<AppInfoBto> appList = new ArrayList<AppInfoBto>();


    public void setNextReqTime(int time) {
        nextReqTime = time;
    }


    public int getNextReqTime() {
        return nextReqTime;
    }


    public List<AppInfoBto> getAppList() {
        return appList;
    }


    public void setAppList(List<AppInfoBto> appList) {
        this.appList = appList;
    }


    public void addAppInfo(AppInfoBto appInfo) {
        this.appList.add(appInfo);
    }

}
