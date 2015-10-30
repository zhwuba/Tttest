package com.market.net.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DiscoverInfoBto implements Serializable{
	/**
     * <code>serialVersionUID</code> - {description}.
     */
    private static final long serialVersionUID = 3345202007087931503L;
    @Expose
	@SerializedName("topicId")
	public int topicId;
	@Expose
	@SerializedName("sonId")
	private int sonTopicId;
	@Expose
	@SerializedName("type") //11小说 12壁纸 13猜你喜欢 14专题 
	public int type;
	@Expose
	@SerializedName("topicName")
	public String topicName;
	@Expose
	@SerializedName("imageUrl")
	public String imageUrl;
	@Expose
	@SerializedName("webUrl")
	private String webUrl;
	@Expose
	@SerializedName("title")
	private String title;
	@Expose
	@SerializedName("desc")
	private String desc;
	@Expose
	@SerializedName("appList")
	public List<DiscoverAppInfoBto> appList;
	public int getTopicId() {
		return topicId;
	}
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public List<DiscoverAppInfoBto> getAppList() {
		return appList;
	}
	public void setAppList(List<DiscoverAppInfoBto> appList) {
		this.appList = appList;
	}
	public String getWebUrl() {
		return webUrl;
	}
	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getSonTopicId() {
		return sonTopicId;
	}
	public void setSonTopicId(int sonTopicId) {
		this.sonTopicId = sonTopicId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}