package com.zhuoyi.market.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

public class DensityUtil {
	 /** 
     * from dp to px by density 
     */  
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
    
  
    /** 
     * from px to dp by density
     */  
    public static int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
    
    
    /**
     * 获取通知栏高度
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity){
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;
	}
    
    
    /**
     * 获取屏幕宽
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
    	
        if(context == null)
            return 0;
        
    	return context.getResources().getDisplayMetrics().widthPixels;
    	
    }
    
    
    /**
     * 获取屏幕高
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        
        if(context == null)
            return 0;
        
        return context.getResources().getDisplayMetrics().heightPixels;
    }
    
    
    public static float getDensity(Context context) {
    	return context.getResources().getDisplayMetrics().density;
    }
}
