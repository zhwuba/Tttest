package com.zhuoyi.market.utils.external;

import java.io.File;

import com.zhuoyi.market.R;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

public class ExternalDownloadUtil {
	private Context mContext;
	private String[] brwDownloadDirs = null;
	
	public ExternalDownloadUtil(Context context) {
		mContext = context;
		brwDownloadDirs = context.getResources().getStringArray(R.array.common_browser_download_paths);
	}

	/**
	 * 目前适配的浏览器: 冒泡,猎豹,2345,百度,uc,欧朋,chrome,搜狗,qq,遨游,360,4G浏览器,Firefox,悦动,海豚
	 */
	public void downloadAppByKey() {
		String key = getKey(brwDownloadDirs);
		
		DownloadFor3rdParty startDownUtil = new DownloadFor3rdParty(mContext);
		
		startDownUtil.getDataFromServer(key);
	}


	private String getKey(String[] brwDownloadDirs) {  
		long lastModified = 0;
		String key = null;
		PackageManager pm = mContext.getPackageManager();
		String storagePath = null;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			return null;
		}
		
		for(int j=0;j<brwDownloadDirs.length;j++) {
			File dir = new File(storagePath + brwDownloadDirs[j]);

			String[] fileNames = dir.list(); 

			if (fileNames == null) {  
				//该目录下无文件
				continue;  
			}  
			for (int i=0; i<fileNames.length; i++) {  
				//如果不是apk文件，则跳过继续扫描  
				if (!isApkFile(fileNames[i])) {  
					continue;  
				}  

				File apkFile = new File(dir, fileNames[i]);

				//读取包名
				String tempPkgName = "";
				PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
				ApplicationInfo appInfo = null;
				if (pkgInfo != null) {
					appInfo = pkgInfo.applicationInfo;
					tempPkgName = appInfo.packageName;
				}

				//是目标apk
				if(tempPkgName.equals(mContext.getPackageName())) {
					int start = fileNames[i].indexOf("_")+1;
					int end = fileNames[i].lastIndexOf(".");

					if(apkFile.lastModified() > lastModified) {
						lastModified = apkFile.lastModified();
						key = fileNames[i].substring(start, end);
					}
					//删除apk包
					apkFile.delete();
				}
			} 
		}
		return key;
	}  

	private boolean isApkFile(String fileName) {
		if(fileName.endsWith(".apk")) {
			return true;
		}
		return false;
	}
}
