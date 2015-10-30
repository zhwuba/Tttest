package com.market.behaviorLog;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.market.download.util.NetworkType;
import com.market.download.util.Util;
import com.market.net.SenderDataProvider;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.LogHelper;

public class LogUtil {
	
	private static final String MODULE_TAG = "marketBehaviorLog";
	
	public static void log(String tag, String func, String msg) {
		LogHelper.trace(MODULE_TAG, tag, func, msg);
    }
	
	
	public static String getPublicParamStr(Context context) {
	    int versionCode = 0;
        String pName = "";
        String apkVersionName = "";
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        
        JSONObject jo = new JSONObject();
        
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();

        pName = context.getPackageName();
        PackageInfo pInfo = SenderDataProvider.getPackageInfo(context, pName);
        if(pInfo!=null)
        {
            versionCode = pInfo.versionCode;
            apkVersionName = pInfo.versionName;
        }
        
        try {
            jo.put("hsman", Build.MANUFACTURER);
            jo.put("hstype", Build.MODEL);
            jo.put("osVer", Build.VERSION.RELEASE);
            jo.put("cpu", Build.HARDWARE);
            jo.put("scrHeight", outMetrics.heightPixels);
            jo.put("scrWidth", outMetrics.widthPixels);
            jo.put("appId", Constant.CP_ID);
            jo.put("channelId", Constant.td);
            jo.put("verCode", versionCode);
            jo.put("pkgName", pName);
            jo.put("verName", apkVersionName);
            jo.put("imei", tManager.getDeviceId() == null? "null" : tManager.getDeviceId());
            jo.put("imsi", tManager.getSubscriberId() == null? "null" : tManager.getSubscriberId());
            jo.put("netType", NetworkType.getNetTypeString(context));
            jo.put("ramSize", SenderDataProvider.getAndroidRamSize());         //KB
            jo.put("romSize", SenderDataProvider.getHandsetRomSize());
            jo.put("lbs", SenderDataProvider.getCurrLbs(context));
            jo.put("uuid", SenderDataProvider.getDeviceUUID());
            jo.put("mac", SenderDataProvider.getMacAddress(context));
            jo.put("marketSign", Util.getMarketSignal(context));
            jo.put("freeMeVer", SenderDataProvider.getFreeMeOsVersion());
            jo.put("sdkApiVer", SenderDataProvider.getSdkApiVersion());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jo.toString();
	}
	
	
	
}
