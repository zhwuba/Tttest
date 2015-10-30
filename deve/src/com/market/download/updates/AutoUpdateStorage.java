package com.market.download.updates;

import com.market.download.userDownload.DownStorage;
import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;

public class AutoUpdateStorage {
    
    SharedPreferences mSharedPref;
    
    AutoUpdateStorage(Context context) {
        mSharedPref = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_AUTO, Context.MODE_PRIVATE);
    }
    
    
    public void saveAutoUpdateEventInfo(AutoUpdateEventInfo info) {
        String eventSignal = DownStorage.getEventSignal(info.getPkgName(), info.getVersionCode());
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(eventSignal, info.getSaveString());
        editor.commit();
    }
    
    
    public void removeAutoUpdateEventInfo(String eventSignal) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(eventSignal);
        editor.commit();
    }
    
    
    public void clearStorage() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        editor.commit();
    }
    
    
    public SharedPreferences getStorageSp() {
        return mSharedPref;
    }
}
