package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;


public class GetApkListByPageResp extends BaseInfo{

  @Expose
  @SerializedName("appLst")
  private List<AppInfoBto> appList;

  public List<AppInfoBto> getAppList() {
    return appList;
  }

  public void setAppList(List<AppInfoBto> appList) {
    this.appList = appList;
  }

}
