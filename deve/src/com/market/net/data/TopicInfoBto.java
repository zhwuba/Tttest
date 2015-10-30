package com.market.net.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopicInfoBto implements Serializable {

	@Expose
	@SerializedName("topicId")
	private int topicId;

	@Expose
	@SerializedName("topicName")
	private String topicName;

	@Expose
	@SerializedName("ext")
	private int extension;

	@Expose
	@SerializedName("imgUrl")
	private String imgUrl;

	@Expose
	@SerializedName("assLst")
	private List<AssemblyInfoBto> assemblyList;

	@Expose
	@SerializedName("topicType")
	private int topicType;    

	@Expose
	@SerializedName("wbUrl")
	private String wbUrl;  

	@Expose
	@SerializedName("fgColor")
	private String fgColor;  

	@Expose
	@SerializedName("bgColor")
	private String bgColor;  
	
	@Expose
	@SerializedName("model")
	private ModelInfoBto model;
	
	@Expose
    @SerializedName("redDot")
    private int redDot;
	
	public ModelInfoBto getModel() {
		return model;
	}

	public void setModel(ModelInfoBto model) {
		this.model = model;
	}

	public String getFgColor() {
		return fgColor;
	}

	public void setFgColor(String fgColor) {
		this.fgColor = fgColor;
	}

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public String getWbUrl() {
		return wbUrl;
	}

	public void setWbUrl(String wbUrl) {
		this.wbUrl = wbUrl;
	}

	public int getTopicId() {
		return topicId;
	}

	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public int getExtension() {
		return extension;
	}

	public void setExtension(int extension) {
		this.extension = extension;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public List<AssemblyInfoBto> getAssemblyList() {
		return assemblyList;
	}

	public void setAssemblyList(List<AssemblyInfoBto> assemblyList) {
		this.assemblyList = assemblyList;
	}

	public void addAssemblyInfo(AssemblyInfoBto assemblyInfo) {
		if (assemblyList == null) {
			assemblyList = new ArrayList<AssemblyInfoBto>();
		}
		assemblyList.add(assemblyInfo);
	}

	public int getTopicType() {
		return topicType;
	}

	public void setTopicType(int topicType) {
		this.topicType = topicType;
	}

    public int getRedDot() {
        return redDot;
    }

    public void setRedDot(int redDot) {
        this.redDot = redDot;
    }    

}
