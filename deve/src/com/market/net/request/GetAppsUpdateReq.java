package com.market.net.request;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppSnapshotBto;
import com.market.net.data.TerminalInfo;

public class GetAppsUpdateReq {

  @Expose
  @SerializedName("tInfo")
  private TerminalInfo      terminalInfo;

  @Expose
  @SerializedName("appLst")
  private List<AppSnapshotBto> appList;

  @Expose
  @SerializedName("fr")
  private String fr;

  @Expose
  @SerializedName("marketId")
  private String marketId;

  public String getMarketId()
  {
      return marketId;
  }

  public void setMarketId(String marketId)
  {
      this.marketId = marketId;
  }
  
  
  public String getFrId()
  {
      return fr;
  }

  public void setFrId(String id)
  {
      this.fr = id;
  }
  
  public TerminalInfo getTerminalInfo() {
    return terminalInfo;
  }

  public void setTerminalInfo(TerminalInfo terminalInfo) {
    this.terminalInfo = terminalInfo;
  }

  public List<AppSnapshotBto> getAppList() {
    return appList;
  }

  public void setAppList(List<AppSnapshotBto> appList) {
    this.appList = appList;
  }
}
