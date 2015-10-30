package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class CommitUserCommentReq {


	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo;

	@Expose
	@SerializedName("openId")
	private String openId;
	
	@Expose
	@SerializedName("resId")
	private int resId;
	
	@Expose
	@SerializedName("commentContent")
	private String commentContent;
	
	@Expose
	@SerializedName("stars")
	private int stars;
	
	@Expose
	@SerializedName("version")
	private int version;
	
	@Expose
	@SerializedName("verName")
	private String verName;
	
	@Expose
	@SerializedName("pName")
	private String pName;
	
	@Expose
	@SerializedName("uuid")
	private String uuid;
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getVerName() {
		return verName;
	}

	public void setVerName(String verName) {
		this.verName = verName;
	}

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}


	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public int getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

}
