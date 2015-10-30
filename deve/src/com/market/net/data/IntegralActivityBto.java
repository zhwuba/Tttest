package com.market.net.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IntegralActivityBto implements Serializable{

	@Expose
	@SerializedName("actName")
	private String actName;       //活动名称

	@Expose
	@SerializedName("actUrl")
	private String actUrl;        //活动跳转地址

	@Expose
	@SerializedName("imgUrl")
	private String imageUrl;   //活动图片URL

	@Expose
	@SerializedName("marketId")
	private String marketId;     // 市场Id	

	@Expose
	@SerializedName("sequence")
	private Integer sequence;    //展示序列   



	public String getActName() {
		return actName;
	}



	public void setActName(String actName) {
		this.actName = actName;
	}



	public String getImageUrl() {
		return imageUrl;
	}



	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}



	public String getMarketId() {
		return marketId;
	}



	public void setMarketId(String marketId) {
		this.marketId = marketId;
	}


	public String getActUrl() {
		return actUrl;
	}



	public void setActUrl(String actUrl) {
		this.actUrl = actUrl;
	}



	public Integer getSequence() {
		return sequence;
	}



	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

}

