package com.market.download.userDownload;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.zhuoyi.market.constant.SharedPrefDefine;
import com.zhuoyi.market.utils.MarketUtils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class DownStorage {
//	private static final String TAG = "DownStorage";
	
	private SharedPreferences mSharedPref;
	
	private static DownStorage mSelf = null;
	
	public static DownStorage getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new DownStorage(context);
		}
		
		return mSelf;
	}
	
	
	DownStorage(Context context) {
		mSharedPref = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_USER, Context.MODE_PRIVATE);
	}
	
	
	public static String getEventSignal(String pkgName, int verCode) {
        return pkgName + Integer.toString(verCode);
    }
	
	
	public static DownloadEventInfo getFgDownloadEventInfo(Context context, String pkgName, int verCode) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_USER, Context.MODE_PRIVATE);
        String eventSignal = DownStorage.getEventSignal(pkgName, verCode);
        String infoStr = sp.getString(eventSignal, null);
        if (infoStr == null) {
            return null;
        }
        DownloadEventInfo eventInfo = new DownloadEventInfo(infoStr);
        if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_BACKGROUND
                || eventInfo.getEventArray() == DownloadEventInfo.ARRAY_UPDATE) {
            return null;
        }
        return eventInfo;
    }
	
	
	public static DownloadEventInfo getEventInfo(Context context, String pkgName, int verCode) {
        String eventSignal = getEventSignal(pkgName, verCode);

        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_USER, Context.MODE_PRIVATE);
        String eventStr = sp.getString(eventSignal, null);
        if (eventStr == null) {
            return null;
        }

        return new DownloadEventInfo(eventStr);
    }
	
	
	public DownloadEventInfo getEventInfo(String eventSignal) {
		DownloadEventInfo eventInfo = null;
		String eventStr = mSharedPref.getString(eventSignal, null);
        if (eventStr != null) {
            eventInfo = new DownloadEventInfo(eventStr);
        }
        
        return eventInfo;
	}
	
	
	public void savaEventInfo(DownloadEventInfo eventInfo) {
		SharedPreferences.Editor editor = mSharedPref.edit();
        String eventStr = eventInfo.getEventString();
        String eventSignal = getEventSignal(eventInfo.getPkgName(), eventInfo.getVersionCode());
        editor.putString(eventSignal, eventStr);
        editor.commit();
	}
	
	
	public void removeEventInfo(String eventSignal) {
		SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(eventSignal);
        editor.commit();
	}
	
	
	public ArrayList<DownloadEventInfo> getAllDownloadEvent(Context context) {
        ArrayList<DownloadEventInfo> eventInfoArray = new ArrayList<DownloadEventInfo>();
        Map<String, ?> allMap = mSharedPref.getAll();
        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            String eventSignal = null;
            DownloadEventInfo eventInfo = null;
            File apkFile = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                eventSignal = (String) entry.getKey();
                try {
                    eventInfo = new DownloadEventInfo(infoStr);
                } catch (Exception e) {
                    e.printStackTrace();
                    iter.remove();
					continue;
                }
                if (eventInfo.getVersionCode() == 0) {
                    SharedPreferences.Editor editor = mSharedPref.edit();
                    editor.remove(eventInfo.getPkgName());
                    editor.commit();
                }
                if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
                    apkFile = eventInfo.getApkFile();
                    if (apkFile.exists() || MarketUtils.checkInstalled(context, eventInfo.getPkgName())) {
                        eventInfoArray.add(eventInfo);
                    } else {
                        SharedPreferences.Editor editor = mSharedPref.edit();
                        editor.remove(eventSignal);
                        editor.commit();
                    }
                } else {
                    eventInfoArray.add(eventInfo);
                }
            }
        }
        return eventInfoArray;
    }
	
	
	public static void putSignCheckInstallInfo(Context context, String key, String fileName) {
	    if (TextUtils.isEmpty(fileName) || context == null) return;
	    SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UNINSTALL_UPDATE, Context.MODE_PRIVATE); 
	    SharedPreferences.Editor editor = sp.edit();
        editor.putString(key+"_signCheck", fileName);
        editor.commit();
	}


    public static String getSignCheckInstallInfo(Context context, String key) {
        if (context == null) return "";
        
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UNINSTALL_UPDATE, Context.MODE_PRIVATE);
        String fileName = sp.getString(key+"_signCheck", "");
        
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key+"_signCheck");
        editor.commit();
        
        return fileName;
    }
}
