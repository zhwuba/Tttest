package com.zhuoyi.market.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.asyncTask.MarketFrameInfoTask;
import com.zhuoyi.market.asyncTask.TaskManager;
import com.zhuoyi.market.constant.Constant;
import com.market.download.util.SdcardUtil;
import com.market.net.response.BaseInfo;

public class FrameInfoCache {
	private static String mVersionCode;
	
	static {
		try {
			mVersionCode = getMarketVersionCode();
		} catch (NameNotFoundException e) {
			mVersionCode = "";
			e.printStackTrace();
		}
	}

	/*****	add by  huangyn 	******/
	public static void saveFrameInfoToStorage(final BaseInfo frameResp, final String frameName) {
		
		if(TextUtils.isEmpty(frameName)) {
			return;
		}
		
		if(frameResp != null && SdcardUtil.isSdcardAvailable()) {
			String filePath = MarketUtils.FileManage.getSDPath() + Constant.config_path + "marketframe/";
			MarketFrameInfoTask marketFrameInfoTask = new MarketFrameInfoTask(frameName, filePath,
					frameResp, mVersionCode);
			TaskManager.getInstance().startTask(marketFrameInfoTask, true);
		}
	}
	
	
	public static BaseInfo getFrameInfoFromStorage(String frameName) {
		if(TextUtils.isEmpty(frameName)) {
			return null;
		}
		
		BaseInfo baseInfo = null;
		if(SdcardUtil.isSdcardAvailable()) {
			String filePath = MarketUtils.FileManage.getSDPath() + Constant.config_path + "marketframe/" + frameName + mVersionCode;
			File file = new File(filePath);
			if(file.exists()) {
				FileInputStream fis = null;
				ObjectInputStream oos = null;
				try {
					fis = new FileInputStream(file);
					oos = new ObjectInputStream(fis);
					baseInfo = (BaseInfo) oos.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				finally {
					try {
					if(fis != null){
							fis.close();
					}
					if(oos != null){
						oos.close();
					}
						} catch (IOException e) {
							e.printStackTrace();
						}
				} 
			}
		}
		return baseInfo;
	}
	
	
	private static String getMarketVersionCode() throws NameNotFoundException {
		Context context = MarketApplication.getRootContext();
        PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
		return pi.versionCode+"";
	}
}
