package com.zhuoyi.market.utils.patch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

/**
 * apk 工具类
 * @author JLu
 *
 */
public class ApkUtils {

	/**
	 * 是否安装指定包名的apk
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(packageName, 0);
			installed = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return installed;
	}

	
	/**
	 * 获取已安装apk的路径
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static String getSourceApkPath(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName))
			return null;

		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
			return appInfo.sourceDir;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 安装APK
	 * 
	 * @param context
	 * @param apkPath
	 */
	public static void installApk(Context context, String apkPath) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkPath),
				"application/vnd.android.package-archive");

		context.startActivity(intent);
	}
}