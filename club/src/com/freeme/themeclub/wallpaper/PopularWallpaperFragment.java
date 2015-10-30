package com.freeme.themeclub.wallpaper;

import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

import android.os.Bundle;

public class PopularWallpaperFragment extends OnlineWallpaper{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        msgCode=getMsgCode();
        serialNum=3;
        super.onCreate(savedInstanceState);
    }
    
    public int getMsgCode(){
        return MessageCode.GET_WALLPAPER_LIST_BY_TAG_REQ;
    }
}
