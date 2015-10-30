package com.market.net.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CornerIconInfoBto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Expose
	@SerializedName("name")
	private String name;// 角标名字

	@Expose
	@SerializedName("type")
	private int type;// 角标type（默认0：没有角标）

	@Expose
	@SerializedName("hdImageUrl")
	private String hdImageUrl;// 角标大图下载地址

	@Expose
	@SerializedName("nhdImageUrl")
	private String nhdImageUrl;// 角标小图下载地址


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	public String getHdImageUrl() {
		return hdImageUrl;
	}


	public void setHdImageUrl(String url) {
		this.hdImageUrl = url;
	}


	public String getNhdImageUrl() {
		return nhdImageUrl;
	}


	public void setNhdImageUrl(String url) {
		this.nhdImageUrl = url;
	}


	@Override
	public String toString() {
		return null;
	}

}
