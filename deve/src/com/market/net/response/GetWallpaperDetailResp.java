package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.WallpaperInfoBto;

public class GetWallpaperDetailResp  extends BaseInfo{
	@Expose
	@SerializedName("wallpaper")
	private WallpaperInfoBto wallpaper;

	public WallpaperInfoBto getWallpaper() {
		return wallpaper;
	}

	public void setWallpaper(WallpaperInfoBto wallpaper) {
		this.wallpaper = wallpaper;
	}
}