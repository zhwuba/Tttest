package com.market.net.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 搜索的热搜词对象
 * @author JLu
 *
 */
public class HotSearchInfoBto implements Serializable {
	/**
	 * 表示该对象是某个具体的应用
	 */
	public static final int TYPE_APP_INFO = 1;
	/**
	 * 表示该对象仅仅是一个热词
	 */
	public static final int TYPE_TEXT = 0;
	/**
     * 表示该对象是热词，同时携带了url参数，跳转时进入市场webview
     */
    public static final int TYPE_URL = 1;

	@Expose
	@SerializedName("text")
	private String text;//应用名或者热词
	
	@Expose
	@SerializedName("type")
	private int type;//判断下发词类型(0：表示热词，1：表示应用信息)
	
	@Expose
	@SerializedName("appInfo")
	private AppInfoBto appInfo;//当下发词为具体应用时下发应用详情信息
	
	@Expose
    @SerializedName("jumpFlag")
    private int     jumpFlag;
	
    @Expose
    @SerializedName("jumpUrl")
    private String     jumpUrl;
    
    @Expose
    @SerializedName("colorCode")
    private String     colorCode;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public AppInfoBto getAppInfo() {
		return appInfo;
	}

	public void setAppInfo(AppInfoBto appInfo) {
		this.appInfo = appInfo;
	}
	
	@Override
	public String toString(){
		return null;
	}

    public int getJumpFlag() {
        return jumpFlag;
    }

    public void setJumpFlag(int jumpFlag) {
        this.jumpFlag = jumpFlag;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

}
