package com.market.net.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AssemblyInfoBto implements Serializable
{

	@Expose
	@SerializedName("assId")
	private int assemblyId;

	@Expose
	@SerializedName("type")
	private int type;

	@Expose
	@SerializedName("assName")
	private String assName;

	@Expose
	@SerializedName("appLst")
	private List<AppInfoBto> appInfoList = new ArrayList<AppInfoBto>();

	@Expose
	@SerializedName("resId")//添加三级分类资源id
	private int resId;

	@Expose
	@SerializedName("style")//添加分类样式显示标识，0为原市场默认样式，1为新定样式A，2为新定样式B
	private int style;


	@Expose
	@SerializedName("assUrl")
	private String assIconUrl;//组件图标

	public String getAssIconUrl() {
		return assIconUrl;
	}

	public void setAssIconUrl(String assIconUrl) {
		this.assIconUrl = assIconUrl;
	}

	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public int getAssemblyId()
	{
		return assemblyId;
	}

	public void setAssemblyId(int assemblyId)
	{
		this.assemblyId = assemblyId;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public List<AppInfoBto> getAppInfoList()
	{
		return appInfoList;
	}

	public int getAppInfoListSize()
	{
		if (appInfoList == null)
			return 0;
		else
			return appInfoList.size();
	}

	public void setAppInfoList(List<AppInfoBto> appInfoList)
	{
		this.appInfoList = appInfoList;
	}

	public void addAppInfo(AppInfoBto appInfo)
	{
		this.appInfoList.add(appInfo);
	}

	public String getAssName()
	{
		return assName;
	}

	public void setAssName(String assName)
	{
		this.assName = assName;
	}
}
