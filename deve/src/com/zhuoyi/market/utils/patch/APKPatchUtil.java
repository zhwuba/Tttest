package com.zhuoyi.market.utils.patch;

import java.io.File;

import android.content.Context;
import android.text.TextUtils;

/**
 * 差分包合成工具类
 * @author JLu
 *
 */
public class APKPatchUtil {
	public static final int FAILD_NOT_INSTALLED =-1;
	public static final int FAILD_NOT_FOUND_PATCH = -2;
	public static final int FAILD_NOT_GET_OLD_APK = -3;
	public static final int FAILD_PATCH_ERROR = -4;
	public static final int FAILD_SIGNATURE_NOT_MATCH = -5;
	public static final int SUCCESSFUL = 0;

	/**
	 * 使用差分包和已安装apk合成新apk
	 * @param context
	 * @param pkgName 应用包名
	 * @param patchPath 差分包路径
	 * @param newApkPath 合成后的新apk存放路径
	 * @return 0代表成功  其他小于0的值都代表失败
	 */
	public static int patchApk(Context context,String pkgName , String patchPath ,String newApkPath) {
		File patchFile = new File(patchPath);

		if(!ApkUtils.isInstalled(context, pkgName)) {
			//未安装该应用
			return FAILD_NOT_INSTALLED;
		} else if(!patchFile.exists()) {
			//未找到差分包
			return FAILD_NOT_FOUND_PATCH;
		} else {
			//进行包合成
			String oldApkPath = ApkUtils.getSourceApkPath(context, pkgName);

			if (!TextUtils.isEmpty(oldApkPath)) {

				//				int patchResult = PatchUtils.patch(Constants.OLD_APK_PATH, Constants.NEW_APK_PATH, Constants.PATCH_PATH);
				int patchResult = PatchUtils.patch(oldApkPath, newApkPath, patchPath);

				//合成新包成功
				if (patchResult == 0) {
					//检测签名
					String signatureNew = SignUtils.getUnInstalledApkSignature(newApkPath);
					String signatureSource = SignUtils.InstalledApkSignature(context,pkgName);

					if ( !TextUtils.isEmpty(signatureNew) && !TextUtils.isEmpty(signatureSource) && signatureNew.equals(signatureSource)) {
						//新apk已合成成功
						return SUCCESSFUL;
					} else {
						//签名不一致
						return FAILD_SIGNATURE_NOT_MATCH;
					}
					
				} else {
					//合成失败
					return FAILD_PATCH_ERROR;
				}
			} else {
				//无法获取当前安装apk路径
				return FAILD_NOT_GET_OLD_APK;
			}
		}

	}

	static {
		System.loadLibrary("ApkPatchLibrary");
	}
}
