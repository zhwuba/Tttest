package com.freeme.themeclub.wallpaper;

import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import android.os.Bundle;

public class NewestWallpaperFragment extends OnlineWallpaper{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initParams();
        super.onCreate(savedInstanceState);
    }
    
    public int getMsgCode(){
        return MessageCode.GET_WALLPAPER_LIST_BY_TAG_REQ;
    }
    
    private void initParams(){
        msgCode=getMsgCode();
        sort="02";
        serialNum=1;
        loadAds=true;
    }
}
