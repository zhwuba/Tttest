package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.ModelListInfoBto;

public class GetModelTopicResp extends BaseInfo {
	@Expose
	@SerializedName("modelTopicBto")
	private ModelListInfoBto modelListInfoBto;

	public ModelListInfoBto getModelListInfoBto() {
		return modelListInfoBto;
	}

	public void setModelListInfoBto(ModelListInfoBto modelListInfoBto) {
		this.modelListInfoBto = modelListInfoBto;
	}

}
