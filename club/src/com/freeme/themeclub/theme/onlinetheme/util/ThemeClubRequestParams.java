package com.freeme.themeclub.theme.onlinetheme.util;

import android.content.Context;
import com.freeme.themeclub.R;

public class ThemeClubRequestParams {
    
    public static final String NAMES_HEAD = "head";
    public static final String NAMES_BODY = "body";
    
    public static final String NAMES_MF = "mf";
    public static final String NAMES_CHANNEL = "channel";
    public static final String NAMES_VERSION = "ver";
    public static final String NAMES_TYPE = "type";
    public static final String NAMES_LCD = "lcd";
    public static final String NAMES_STANDARD_SDK = "standardSdk";
    public static final String NAMES_CONTENT = "content";
    public static final String NAMES_FROM = "from";
    public static final String NAMES_TO = "to";
    
    public static final String VALUES_MF = "koobee";
    public static final String VALUES_CHANNEL = "koobee";
    public static final String VALUES_THEME_VERSION = "v500";
    public static final String VALUES_LOCKSCREEN_VERSION = "v600";
    public static final String VALUES_STANDARD_SDK = "1";
    
    public static final int VALUES_THEME_TYPE = 1;
    public static final int VALUES_LOCKSCREEN_TYPE = 2;
    public static final int VALUES_WALLPAPER_TYPE = 3;
    
    public static String getLCD(Context context){
        return OnlineThemesUtils
                .getAvailableResolutionForThisDevice(
                        context,
                        context.getResources().getStringArray(
                                R.array.resolution_array))[0];
    }

    public static String getWideLCD(Context context){
        return OnlineThemesUtils
                .getAvailableResolutionForThisDevice(
                        context,
                        context.getResources().getStringArray(
                                R.array.resolution_array))[1];
    }
}
