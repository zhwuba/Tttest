package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

/**
 * 详细分类信息请求对象
 * @author JLu
 *
 */
public class GetSoftGameDetailReq {

	@Expose
	@SerializedName("tInfo")
	private TerminalInfo terminalInfo = new TerminalInfo();

	/**
	 * 三级分类ID
	 */
	@Expose
	@SerializedName("topicId")
	private int topicId;

	/**
	 * 父类ID(三级分类的父类)
	 */
	@Expose
	@SerializedName("parentId")
	private int parentId;

	/**
	 *  path = marketId+"@"+channelID+"@"+topicName(三级或二级分类名)
	 */
	@Expose
	@SerializedName("path")
	private String path;

	@Expose
	@SerializedName("start")
	private int start;

	@Expose
	@SerializedName("fixed")
	private int fixedLength;
	
	@Expose
	@SerializedName("reqModel")
	private int reqModel;

	public int getReqModel() {
		return reqModel;
	}

	/**
	 * 
	 * @param reqModel 0表示请求三级分类列表和分类的app列表,1表示只请求app列表
	 */
	public void setReqModel(int reqModel) {
		this.reqModel = reqModel;
	}

	public TerminalInfo getTerminalInfo() {
		return terminalInfo;
	}

	public void setTerminalInfo(TerminalInfo terminalInfo) {
		this.terminalInfo = terminalInfo;
	}

	public int getTopicId() {
		return topicId;
	}

	/**
	 * 
	 * @param topicId 三级分类ID
	 */
	public void setTopicId(int topicId) {
		this.topicId = topicId;
	}

	public String getPath() {
		return path;
	}

	/**
	 * 
	 * @param path   path = marketId+"@"+channelID+"@"+topicName(三级或二级分类名)
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getFixedLength() {
		return fixedLength;
	}

	public void setFixedLength(int fixedLength) {
		this.fixedLength = fixedLength;
	}

	public int getParentId() {
		return parentId;
	}

	/**
	 * 
	 * @param parentId 二级分类ID
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
	
	private int topicIndex;
    private int channelIndex;
    private String topicName;

	public String getTopicName() {
		return topicName;
	}

	/**
	 * 
	 * @param topicName 三级或二级分类名
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public int getTopicIndex() {
		return topicIndex;
	}

	public void setTopicIndex(int topicIndex) {
		this.topicIndex = topicIndex;
	}

	public int getChannelIndex() {
		return channelIndex;
	}

	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}
}
