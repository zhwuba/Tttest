package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AssemblyInfoBto;

//@SignalCode(messageCode = 201028)
public class GetModelApkListByPageResp extends BaseInfo {

	@Expose
	@SerializedName("assemblyList")
	private List<AssemblyInfoBto> assemblyList;

	@Expose
	@SerializedName("index")
	private int index;

	@Expose
	@SerializedName("sonIndex")
	private int sonIndex;

	@Expose
	@SerializedName("assemblyId")
	private int assemblyId;

	public List<AssemblyInfoBto> getAssemblyList() {
		return assemblyList;
	}

	public void setAssemblyList(List<AssemblyInfoBto> assemblyList) {
		this.assemblyList = assemblyList;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getSonIndex() {
		return sonIndex;
	}

	public void setSonIndex(int sonIndex) {
		this.sonIndex = sonIndex;
	}


	public int getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(int assemblyId) {
		this.assemblyId = assemblyId;
	}
}