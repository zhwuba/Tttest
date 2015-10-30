package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;


public class GetStartPageResp extends BaseInfo{

  @Expose
  @SerializedName("txt")
  private String text;

  @Expose
  @SerializedName("imgUrl")
  private String imgUrl;
  
  @Expose
  @SerializedName("appInfo")
  private AppInfoBto appInfo = new AppInfoBto();
  
  public AppInfoBto getAppInfoBto() {
    return appInfo;
  }

  public void setAppInfoBto(AppInfoBto info) {
    this.appInfo = info;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }
}
