package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppDetailInfoBto;

public class GetApkDetailResp extends BaseInfo
{

    @Expose
    @SerializedName("detailInfo")
    private AppDetailInfoBto appDetailInfo;

    public AppDetailInfoBto getAppDetailInfo()
    {
        return appDetailInfo;
    }

    public void setAppDetailInfo(AppDetailInfoBto appDetailInfo)
    {
        this.appDetailInfo = appDetailInfo;
    }

}
