package com.market.net.response;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserFeedbackResp extends BaseInfo {
	@Expose
    @SerializedName("result")
    private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
}
