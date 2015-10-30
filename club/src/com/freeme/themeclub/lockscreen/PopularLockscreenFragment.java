package com.freeme.themeclub.lockscreen;

import android.os.Bundle;

import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

public class PopularLockscreenFragment extends OnlineThemesFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        isOnlineLockscreens=true;
        msgCode=getMsgCode();
        serialNum=3;
        super.onCreate(savedInstanceState);
    }
    
    public int getMsgCode(){
        return MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ;
    }
}
