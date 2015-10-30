package com.freeme.themeclub.wallpaper;

import android.app.Application;
import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;

public class OnlineWallpaperApplication extends Application {
	static{
		AsyncImageCache.setDiskCacheEnable(true);
		AsyncImageCache.setDiskCacheDir(OnlineUtils.getSDPath()+"/themes/download/cache");
		AsyncImageCache.setDiskCacheSize(1024 * 1024 * 50);     //50MB
		AsyncImageCache.setDiskCacheCount(1024);                //1024 item
		AsyncImageCache.setMemoryCacheSize(1024 * 1024 * 10);    //10MB
		AsyncImageCache.setIgnoreImageSize(true);
	}
}
