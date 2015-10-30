package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;


public class GetApkDetailReq {

	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo;

	@Expose
	@SerializedName("resId")
	private int             resId;

	@Expose
	@SerializedName("pName")
	private String packageName;


	public String getPackageName() {
		return packageName;
	}


	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Expose
  @SerializedName("topicId")
  private String          topicId;
  
  @Expose
  @SerializedName("from")
  private String          from;
  
  @Expose
  @SerializedName("src")
  private String          src;
  
  
  public void setSrc(String src) {
	  this.src = src;
  }
  
  
  public String getSrc() {
	  return src;
  }
  
  
  public void setFrom(String from) {
	  this.from = from;
  }
  
  
  public String getFrom() {
	  return from;
  }
  
  
  public void setTopicId(String topicId) {
	  this.topicId = topicId;
  }
  
  
  public String getTopicId() {
	  return topicId;
  }
  
  
  public TerminalInfo getTerminalInfo() {
    return terminalInfo;
  }

  public void setTerminalInfo(TerminalInfo terminalInfo) {
    this.terminalInfo = terminalInfo;
  }

  public int getResId() {
    return resId;
  }

  public void setResId(int resId) {
    this.resId = resId;
  }

}
