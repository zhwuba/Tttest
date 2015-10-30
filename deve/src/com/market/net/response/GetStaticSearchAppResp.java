package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;
import com.market.net.data.HotSearchInfoBto;
import com.market.net.data.KeyWordInfoBto;

public class GetStaticSearchAppResp extends BaseInfo {

	@Expose
	@SerializedName("appLst")
	private List<AppInfoBto>     appList;
	@Expose
	@SerializedName("key24")
	private List<KeyWordInfoBto> key24List;
	@Expose
	@SerializedName("key48")
	private List<KeyWordInfoBto> key48List;
	@Expose
	@SerializedName("hotSearchList")
	private List<HotSearchInfoBto> hotSearchList;

	public List<HotSearchInfoBto> getHotSearchList() {
		return hotSearchList;
	}
	public void setHotSearchList(List<HotSearchInfoBto> hotSearchList) {
		this.hotSearchList = hotSearchList;
	}
	public List<AppInfoBto> getAppList() {
		return appList;
	}
	public void setAppList(List<AppInfoBto> appList) {
		this.appList = appList;
	}
	public void addAppInfo(AppInfoBto appInfo){
		if(appList == null){
			appList = new ArrayList<AppInfoBto>();
		}
		appList.add(appInfo);
	}
	public List<KeyWordInfoBto> getKey24List() {
		return key24List;
	}
	public void setKey24List(List<KeyWordInfoBto> key24List) {
		this.key24List = key24List;
	}
	public void add24Keyword(KeyWordInfoBto keywordBto) {
		if (key24List == null) {
			key24List = new ArrayList<KeyWordInfoBto>();
		}
		key24List.add(keywordBto);
	}

	public List<KeyWordInfoBto> getKey48List() {
		return key48List;
	}
	public void setKey48List(List<KeyWordInfoBto> key48List) {
		this.key48List = key48List;
	}

	public void add48Keyword(KeyWordInfoBto keywordBto) {
		if (key48List == null) {
			key48List = new ArrayList<KeyWordInfoBto>();
		}
		key48List.add(keywordBto);
	}

}
