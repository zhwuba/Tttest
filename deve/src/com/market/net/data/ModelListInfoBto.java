package com.market.net.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelListInfoBto  implements Serializable{
	@Expose
	@SerializedName("assList")
	private List<AssemblyInfoBto> assemblyList;

	@Expose
	@SerializedName("model")
	private ModelInfoBto model;

	public ModelInfoBto getModel() {
		return model;
	}

	public void setModel(ModelInfoBto model) {
		this.model = model;
	}

	public List<AssemblyInfoBto> getAssemblyList() {
		return assemblyList;
	}

	public void setAssemblyList(List<AssemblyInfoBto> assemblyList) {
		this.assemblyList = assemblyList;
	}

}
