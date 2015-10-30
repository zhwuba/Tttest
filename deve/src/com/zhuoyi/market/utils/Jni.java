package com.zhuoyi.market.utils;

import android.content.Context;

public class Jni {
	
	public Jni(){
	}
	
	public native boolean checkSign(Context context);
	
	public native String[] getMarketUrl(Context context);
}
