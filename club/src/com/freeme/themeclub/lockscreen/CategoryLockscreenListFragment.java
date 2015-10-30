//package com.freeme.themeclub.lockscreen;
//import android.os.Bundle;
//import android.view.View;
//
//import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
//import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
//
//public class CategoryLockscreenListFragment extends OnlineThemesFragment{
//    
//    public CategoryLockscreenListFragment(View contentView){
//        view=contentView;
//    }
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        isOnlineLockscreens=true;
//        msgCode=getMsgCode();
//        sort="02";
//        serialNum=4;
//        super.onCreate(savedInstanceState);
//    }
//    
//    public int getMsgCode(){
//        return MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ;
//    }
//}
