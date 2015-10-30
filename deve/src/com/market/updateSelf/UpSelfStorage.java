package com.market.updateSelf;

import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

public class UpSelfStorage {
	
//	private static final String SP_UPDATE_SELF = "updateSelfSp";
    public static final String KEY_LAST_POP_INSTALL_TIME = "lastPopInstallTime";

    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_VERCODE = "versionCode";
    public static final String KEY_POLICY = "policy";
    public static final String KEY_URL = "url";
    public static final String KEY_MD5 = "md5";
    public static final String KEY_SIZE = "size";
    public static final String KEY_STATE = "state";
    
    /**
     * 
     * @param context
     * @return
     */
    public static String getLastPopInstallTime(Context context) {
    	SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        return sp.getString(KEY_LAST_POP_INSTALL_TIME, null);
    }

    /**
     * 
     * @param context
     * @param currTimeStr
     */
    public static void setPopInstallTime(Context context, String currTimeStr) {
    	SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_LAST_POP_INSTALL_TIME, currTimeStr);
        editor.commit();
    }

    /**
     * 清除自更新信息
     */
    public static void clearSelfUpdateInfo(Context context) {
    	SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(KEY_TITLE);
        editor.remove(KEY_CONTENT);
        editor.remove(KEY_VERCODE);
        editor.remove(KEY_POLICY);
        editor.remove(KEY_URL);
        editor.remove(KEY_MD5);
        editor.commit();
    }

    /**
     * 保存自更新信息
     * @param context
     * @param info
     */
    public static void saveSelfUpdateInfo(Context context, SelfUpdateInfo info) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_TITLE, info.getTitle());
        editor.putString(KEY_CONTENT, info.getContent());
        editor.putInt(KEY_VERCODE, info.getVersionCode());
        editor.putInt(KEY_POLICY, info.getUpdateType());
        editor.putString(KEY_URL, info.getDownloadUrl());
        editor.putString(KEY_MD5, info.getMd5());
        editor.putInt(KEY_STATE, info.getDownloadState());
        editor.putLong(KEY_SIZE, info.getTotalSize());
        editor.commit();
    }

    /**
     * 保存自更新信息
     * @param context
     * @param title
     * @param content
     * @param verCode
     * @param policy
     * @param fileUrl
     * @param md5
     */
    public static void saveSelfUpdateInfo(Context context, String title, String content, int verCode, int policy, String fileUrl, String md5, long totalSize) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_CONTENT, content);
        editor.putInt(KEY_VERCODE, verCode);
        editor.putInt(KEY_POLICY, policy);
        editor.putString(KEY_URL, fileUrl);
        editor.putString(KEY_MD5, md5);
        editor.putLong(KEY_SIZE, totalSize);
        editor.commit();
    }
    
    /**
     * 保存文件大小
     * @param context
     * @param size
     */
    public static void saveSelfUpdateTotleSize(Context context, long size) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(KEY_SIZE, size);
        editor.commit();
    }
    
    /**
     * 保存下载状态
     * @param context
     * @param state
     */
    public static void saveDownloadState(Context context, int state) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_STATE, state);
        editor.commit();
    }
    
    /**
     * 获取自更新信息
     * @param context
     * @return
     */
    public static SelfUpdateInfo getSelfUpdateInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.SELF_UPDATE, Context.MODE_PRIVATE);
        SelfUpdateInfo info = new SelfUpdateInfo();
        int verCode = sp.getInt(KEY_VERCODE, 0);
        PackageInfo pkgInfo = UpdateUtil.getPackageInfo(context, context.getPackageName());
        if (verCode <= pkgInfo.versionCode) {
            return null;
        }
        info.setVersionCode(verCode);
        
        String url = sp.getString(KEY_URL, null);
        String md5 = sp.getString(KEY_MD5, null);
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(md5)) {
            return null;
        }
        info.setDownloadUrl(url);
        info.setMd5(md5);
        
        info.setContent(sp.getString(KEY_CONTENT, null));
        info.setTitle(sp.getString(KEY_TITLE, null));
        info.setUpdateType(sp.getInt(KEY_POLICY, SelfUpdateInfo.SELF_UPDATE_TYPE_2));
        info.setTotalSize(sp.getLong(KEY_SIZE, 0L));
        info.setDownloadState(sp.getInt(KEY_STATE, SelfUpdateInfo.STATE_READY));

        info.setFileName();

        return info;
    }

}
