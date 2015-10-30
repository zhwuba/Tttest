package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetAssociativeWordReq {
    
  @Expose
  @SerializedName("tInfo")
  private TerminalInfo terminalInfo;

  @Expose
  @SerializedName("chId")
  private String chId;

  @Expose
  @SerializedName("key")
  private String key;

  public String getChId()
  {
      return chId;
  }

  public void setChId(String chId)
  {
      this.chId = chId;
  }
  
  public String getKeyWords() {
    return key;
  }

  public void setKeyWords(String key) {
    this.key = key;
  }
  
  public TerminalInfo getTerminalInfo() {
    return terminalInfo;
  }

  public void setTerminalInfo(TerminalInfo terminalInfo) {
    this.terminalInfo = terminalInfo;
  }
}
