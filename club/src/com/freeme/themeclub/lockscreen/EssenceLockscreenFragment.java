package com.freeme.themeclub.lockscreen;

import android.os.Bundle;

import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

public class EssenceLockscreenFragment extends OnlineThemesFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        isOnlineLockscreens=true;
        msgCode=getMsgCode();
        bout="1";
        serialNum=2;
        sort="02";
        super.onCreate(savedInstanceState);
    }
    
    public int getMsgCode(){
        return MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ;
    }
}
