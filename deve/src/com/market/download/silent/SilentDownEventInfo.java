package com.market.download.silent;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.download.common.DownBaseInfo;
import com.market.statistics.ReportFlag;

public class SilentDownEventInfo extends DownBaseInfo {
	
	private static final String KEY_PKGNAME = "PN";
	private static final String KEY_APPNAME = "AN";
	private static final String KEY_TOTALSIZE = "TS";
	private static final String KEY_DOWNLOADURL = "DU";
	private static final String KEY_VERCODE = "VC";
	private static final String KEY_MD5 = "MD5";
	private static final String KEY_APPID = "AI";
	private static final String KEY_DOWNNETWORK = "DN";
	private static final String KEY_HASINSTALLED = "HI";
	
	private static final String TOPIC_ID = ReportFlag.TOPIC_NULL;
	private static final String FROM_FLAG = ReportFlag.FROM_BACRGROUND_DOWN; 
	
	private String mAppName;
	private int mVerCode;
	private int mAppId;
	private String mMd5;
	private int mDownNetwork;
	private boolean mHasInstalled;
	
	public SilentDownEventInfo(String pkgName, String appName, String downloadUrl, int verCode, String md5, int appId, int downNetwork, long totalSize) {
		super(pkgName, getApkFileNameWithoutSuffix(appName, pkgName, verCode), downloadUrl, totalSize);
		mAppName = appName;
		mVerCode = verCode;
		mAppId = appId;
		mMd5 = md5;
		mDownNetwork = downNetwork;
		mHasInstalled = false;
	}
	
	
	public static SilentDownEventInfo getInfoFromStorageString(String storageStr) {
		SilentDownEventInfo info = null;
		try {
			JSONObject jo = new JSONObject(storageStr);
			String pkgName = jo.getString(KEY_PKGNAME);
			long totalSize = jo.getLong(KEY_TOTALSIZE);
			String appName = jo.getString(KEY_APPNAME);
			String downloadUrl = jo.getString(KEY_DOWNLOADURL);
			int verCode = jo.getInt(KEY_VERCODE);
			String md5 = jo.getString(KEY_MD5);
			int appId = jo.getInt(KEY_APPID);
			int downNetwork = jo.getInt(KEY_DOWNNETWORK);
			boolean hasInstalled = jo.getBoolean(KEY_HASINSTALLED);
			info = new SilentDownEventInfo(pkgName, appName, downloadUrl, verCode, md5, appId, downNetwork, totalSize);
			if (hasInstalled) {
				info.eventInstalled();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	
	public String getStorageString() {
		JSONObject jo = new JSONObject();
		try {
			jo.put(KEY_PKGNAME, getPkgName());
			jo.put(KEY_TOTALSIZE, getTotalSize());
			jo.put(KEY_DOWNLOADURL, getDownloadUrl());
			jo.put(KEY_VERCODE, mVerCode);
			jo.put(KEY_APPNAME, mAppName);
			jo.put(KEY_APPID, mAppId);
			jo.put(KEY_MD5, mMd5);
			jo.put(KEY_DOWNNETWORK, mDownNetwork);
			jo.put(KEY_HASINSTALLED, mHasInstalled);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jo.toString();
	}
	
	
	public int getDownloadNetwork() {
		return mDownNetwork;
	}
	
	
	public boolean hasInstalled() {
		return mHasInstalled;
	}
	
	
	public void eventInstalled() {
		mHasInstalled = false;
	}

	
	public String getAppName() {
		return mAppName;
	}
	
	
	public int getVersionCode() {
		return mVerCode;
	}
	
	
	public int getAppId() {
		return mAppId;
	}
	
	
	public String getMd5() {
		return mMd5;
	}
	
	
	public String getTopicId() {
		return TOPIC_ID;
	}
	
	
	public String getFromFlag() {
		return FROM_FLAG;
	}
	
	
	@Override
	public void downloadComplete() {
		File downFile = new File(getDownloadFilePath());
        if (downFile.exists()) {
        	File renameFile = getApkFile();
        	if (renameFile.exists()) {
        		renameFile.delete();
        	}
        	downFile.renameTo(renameFile);
        }
		super.downloadComplete();
	}


	private static String getApkFileNameWithoutSuffix(String appName, String pkgName, int verCode) {
        return appName + "_" + pkgName + "_" + Integer.toString(verCode);
    }
}
