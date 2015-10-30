package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;

/**
 * 详细分类信息接收对象
 * @author JLu
 *
 */
public class GetSoftGameDetailResp extends BaseInfo {

	@Expose
	@SerializedName("assLst")
	private List<AssemblyInfoBto> assemblyList;

	@Expose
	@SerializedName("appLst")
	private List<AppInfoBto> appInfoList;//全部应用信息列表

	public List<AppInfoBto> getAppInfoList() {
		return appInfoList;
	}

	public void setAppInfoList(List<AppInfoBto> appInfoList) {
		this.appInfoList = appInfoList;
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

}
