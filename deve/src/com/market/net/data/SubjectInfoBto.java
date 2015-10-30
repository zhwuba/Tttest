package com.market.net.data;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SubjectInfoBto {
	@Expose
	@SerializedName("topicId")
	private int topicId;
	@Expose
	@SerializedName("appList")
	private List<AppInfoBto> appList;
	@Expose
	@SerializedName("title")
	private String title;
	@Expose
	@SerializedName("desc")
	private String desc;
	@Expose
	@SerializedName("imageUrl")
	private String imageUrl;

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public List<AppInfoBto> getAppList() {
		return appList;
	}

	public void setAppList(List<AppInfoBto> appList) {
		this.appList = appList;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

}
