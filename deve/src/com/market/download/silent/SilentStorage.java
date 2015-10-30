package com.market.download.silent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zhuoyi.market.constant.SharedPrefDefine;


import android.content.Context;
import android.content.SharedPreferences;

public class SilentStorage {
    
    private SharedPreferences m705_706_Sp;
    private SharedPreferences mSilentDownSp;
    
    SilentStorage(Context context) {
    	m705_706_Sp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_705_706, Context.MODE_PRIVATE);
    	mSilentDownSp = context.getSharedPreferences(SharedPrefDefine.DOWNLOAD_SILENT, Context.MODE_PRIVATE);
    }
    
    
    public void save705706EventInfo (Event705706Info silentInfo) {
        synchronized (m705_706_Sp) {
            SharedPreferences.Editor editor = m705_706_Sp.edit();
            editor.putString(silentInfo.getPkgName(), silentInfo.getStorageString());
            editor.commit();
        }
    }
    
    
    public void remove705706EventInfo (Event705706Info silentInfo) {
        synchronized (m705_706_Sp) {
            SharedPreferences.Editor editor = m705_706_Sp.edit();
            editor.remove(silentInfo.getPkgName());
            editor.commit();
        }
    }
    
    
    public ConcurrentHashMap<String, Event705706Info> getAll705706EventInfo() {
        ConcurrentHashMap<String, Event705706Info> silentMap = new ConcurrentHashMap<String, Event705706Info>();
        Map<String, ?> allMap = m705_706_Sp.getAll();
        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            Event705706Info silentInfo = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                silentInfo = Event705706Info.decodeStorageString(infoStr);
                if (silentInfo != null) {
                    silentMap.put(silentInfo.getPkgName(), silentInfo);
                }
            }
        }
        return silentMap;
    }
    
    
    /**
     * start code for silent download and install
     */
    
    public void saveSilentEventInfo (SilentDownEventInfo silentInfo) {
        synchronized (mSilentDownSp) {
            SharedPreferences.Editor editor = mSilentDownSp.edit();
            editor.putString(silentInfo.getPkgName(), silentInfo.getStorageString());
            editor.commit();
        }
    }
    
    
    public void removeSilentEventInfo (SilentDownEventInfo silentInfo) {
    	synchronized (mSilentDownSp) {
    		SharedPreferences.Editor editor = mSilentDownSp.edit();
            editor.remove(silentInfo.getPkgName());
            editor.commit();
    	}
    }
    
    
    public void initAllSilentDownEventInfo(ConcurrentHashMap<String, SilentDownEventInfo> silentMap, ConcurrentHashMap<String, SilentDownEventInfo> installedMap) {
        Map<String, ?> allMap = mSilentDownSp.getAll();
        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            SilentDownEventInfo silentInfo = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                silentInfo = SilentDownEventInfo.getInfoFromStorageString(infoStr);
                if (silentInfo != null) {
                	if (silentInfo.hasInstalled()) {
                		installedMap.put(silentInfo.getPkgName(), silentInfo);
                	} else {
                		silentMap.put(silentInfo.getPkgName(), silentInfo);
                	}
                }
            }
        }
    }
}
