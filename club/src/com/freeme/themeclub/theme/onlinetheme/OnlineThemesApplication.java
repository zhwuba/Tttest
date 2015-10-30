package com.freeme.themeclub.theme.onlinetheme;

import android.app.Application;
import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

public class OnlineThemesApplication extends Application {
    static {
        AsyncImageCache.setDiskCacheEnable(true);
        AsyncImageCache.setDiskCacheDir(OnlineThemesUtils.getDownLoadPath()+"download/cache");
        AsyncImageCache.setDiskCacheSize(1024 * 1024 * 50);     //50MB
        AsyncImageCache.setDiskCacheCount(1024);                //1024 item
        
        AsyncImageCache.setMemoryCacheSize(1024 * 1024 * 10);    //10MB
    }
}
