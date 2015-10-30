package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;


public class GetRecommendAppResp extends BaseInfo
{

    @Expose
    @SerializedName("appLst")
    private List<AppInfoBto> appList = new ArrayList<AppInfoBto>();


	@Expose
	@SerializedName("appLikeList")
	private List<AppInfoBto> appLikeList = new ArrayList<AppInfoBto>();	//大家也喜欢列表

	public List<AppInfoBto> getAppLikeList() {
		return appLikeList;
	}

	public void setAppLikeList(List<AppInfoBto> appLikeList) {
		this.appLikeList = appLikeList;
	}
    public List<AppInfoBto> getAppList()
    {
        return appList;
    }

    public void setAppList(List<AppInfoBto> appList)
    {
        this.appList = appList;
    }

    public void addAppInfo(AppInfoBto appInfo)
    {
        this.appList.add(appInfo);
    }

}
