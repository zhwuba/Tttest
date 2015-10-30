package com.market.net.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommentBto implements Serializable{

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getHstype() {
		return hstype;
	}

	public void setHstype(String hstype) {
		this.hstype = hstype;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Expose
	@SerializedName("nickName")
	private String nickName;
	
	@Expose
	@SerializedName("commentContent")
	private String commentContent;
	
	@Expose
	@SerializedName("stars")
	private int stars;
	
	@Expose
	@SerializedName("version")
	private String version;
	
	@Expose
	@SerializedName("hstype")
	private String hstype;
	
	@Expose
	@SerializedName("time")
	private String time;
	
}