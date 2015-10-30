package com.zhuoyi.market.home;

import com.market.net.data.AppInfoBto;

/**
 * 精选列表的数据实体类
 * @author JLu
 *
 */
public class HomeListBean {

    private int mAssemblyId;
	private String mTopicImgUrl;
	private String mTitleName;
	private int mItemType;
	private AppInfoBto mAppInfo00,mAppInfo01,mAppInfo02,mAppInfo03;
	
	
	public int getAssemblyId() {
	    return mAssemblyId;
	}
	
	
	public void setAssemblyId(int id) {
	    mAssemblyId = id;
	}
	
	
	public String getTopicImgUrl() {
		return mTopicImgUrl;
	}
	
	public void setTopicImgUrl(String mTopicImgUrl) {
		this.mTopicImgUrl = mTopicImgUrl;
	}
	
	public String getTitleName() {
		return mTitleName;
	}
	
	public void setTitleName(String mTitleName) {
		this.mTitleName = mTitleName;
	}
	
	public AppInfoBto getAppInfo00() {
		return mAppInfo00;
	}

	public void setAppInfo00(AppInfoBto mAppInfo00) {
		this.mAppInfo00 = mAppInfo00;
	}
	
	public AppInfoBto getAppInfo01() {
		return mAppInfo01;
	}
	
	public void setAppInfo01(AppInfoBto mAppInfo01) {
		this.mAppInfo01 = mAppInfo01;
	}
	
	public AppInfoBto getAppInfo02() {
		return mAppInfo02;
	}
	
	public void setAppInfo02(AppInfoBto mAppInfo02) {
		this.mAppInfo02 = mAppInfo02;
	}
	
	public AppInfoBto getAppInfo03() {
		return mAppInfo03;
	}
	
	public void setAppInfo03(AppInfoBto mAppInfo03) {
		this.mAppInfo03 = mAppInfo03;
	}

	public int getItemType() {
		return mItemType;
	}

	public void setItemType(int mItemType) {
		this.mItemType = mItemType;
	}
	
	
	
}
