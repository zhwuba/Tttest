package com.market.net.response;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.IntegralActivityBto;

public class GetIntegralResp extends BaseInfo implements Serializable{
	@Expose
	@SerializedName("inteActList")
	private List<IntegralActivityBto> inteActList;


	@Expose
	@SerializedName("actList")
	private List<IntegralActivityBto> actList; // 兑换活动列表

	
	
	public List<IntegralActivityBto> getActList() {
		return actList;
	}
	
	public List<IntegralActivityBto> getInteActList() {
		return inteActList;
	}
}
