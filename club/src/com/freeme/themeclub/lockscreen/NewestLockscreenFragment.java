package com.freeme.themeclub.lockscreen;
import android.os.Bundle;

import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

public class NewestLockscreenFragment extends OnlineThemesFragment{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        isOnlineLockscreens=true;
        msgCode=MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ;
        sort="02";
        serialNum=1;
        loadAds=true;
        super.onCreate(savedInstanceState);
    }
}
