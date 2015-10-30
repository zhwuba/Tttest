package com.zhuoyi.market.commonInterface;

import android.graphics.drawable.Drawable;

public interface DownloadCallBackInterface {
    /**
     * start download app.
     **/
    public void startDownloadApp(
            String pacName, 
            String appName,
            String filePath,
            String md5, 
            String url, 
            String topicId,
            String type, 
            int verCode, 
            int appId,
            long totalSize);
    
    /**
     * start icon animation.
     **/
    public void startIconAnimation(
            String pacName, 
            int versionCode, 
            Drawable drawable,
            int fromX,
            int fromY);
    
    
    /**
     *download pause 
     */
    public boolean downloadPause(String pkgName, int verCode);
}

