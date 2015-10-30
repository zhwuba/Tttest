package com.zhuoyi.market.wallpaper;

import java.util.ArrayList;
import java.util.List;

import com.market.net.data.WallpaperInfoBto;

public class DisplayWallpaperStorage {
    
    private ArrayList<WallpaperInfoBto> mDisplayList = new ArrayList<WallpaperInfoBto>();
    
    public int getDisplayCount() {
        if (mDisplayList == null) return 0;
        
        return mDisplayList.size();
    }
    
    
    public WallpaperInfoBto getDisplayWallpaperInfo(int position) {
        if (mDisplayList == null || position >= mDisplayList.size()) return null;
        
        return mDisplayList.get(position);
    }
    
    
    public void putWallpaperData(List<WallpaperInfoBto> dataList) {
        if (dataList == null || dataList.size() <= 0) return;
        mDisplayList.addAll(dataList);
    }
}
