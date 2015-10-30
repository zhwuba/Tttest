package com.zhuoyi.market.utils;

import android.app.Activity;
import android.content.SharedPreferences;

import com.zhuoyi.market.appResident.MarketApplication;

public class SharePreferenceUtils
{
	private static final String  SP_NAME = "local_setting";
	private static final String  SP_CHECK_POINT_KEY = "local_setting";
	private static final String  SP_CLEAR_POINT_KEY = "clear_point";
	
	public static boolean hasCheckedMobile()
	{
		return MarketApplication.getRootContext().getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE).getBoolean(SP_CHECK_POINT_KEY, false);
	}
	
	public static void setCheckedMobile()
	{
		SharedPreferences sprefs = MarketApplication.getRootContext().getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SP_CHECK_POINT_KEY, true);
        editor.commit();
	}
	
	public static boolean hasCleardMobile()
    {
        return MarketApplication.getRootContext().getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE).getBoolean(SP_CLEAR_POINT_KEY, false);
    }
    
    public static void setCleardMobile()
    {
        SharedPreferences sprefs = MarketApplication.getRootContext().getSharedPreferences(SP_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(SP_CLEAR_POINT_KEY, true);
        editor.commit();
    }
	
}
