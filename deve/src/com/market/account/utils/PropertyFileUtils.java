package com.market.account.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * 读写文件
 * 
 * @author sunlei
 * 
 */
public class PropertyFileUtils {

	/** 文件名，保存在data/data */
	public static final String FILENAME_STRING = "login";


	/**
	 * 写文件
	 * 
	 * @param context
	 *            .
	 * @param jsonString
	 *            .
	 */
	public static void writeFile(Context context, String jsonString) {
		FileOutputStream fileos = null;
		try {
			fileos = context.openFileOutput(FILENAME_STRING,
					Context.MODE_WORLD_READABLE);
			fileos.write(jsonString.getBytes());
			if (fileos != null) {
				fileos.close();
			}
		} catch (Exception e) {
			Log.e("FileNotFoundException", "can't create FileOutputStream");
			return;
		}

	}


	/**
	 * 读文件
	 * 
	 * @param context
	 *            .
	 * @return String
	 */
	public static String readFile(Context context) {
		String resultString = null;
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(FILENAME_STRING);
			InputStreamReader read = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(read);
			StringBuffer sb = new StringBuffer();
			String lineTxt = "";
			while ((lineTxt = bufferedReader.readLine()) != null) {
				sb.append(lineTxt);
			}
			resultString = sb.toString();
			if (fis != null) {
				fis.close();
			}
		} catch (Exception e) {
			Log.e("FileNotFoundException", "Couldn't find or open policy file");
		}
		return resultString;
	}


	/**
	 * 清除文件内容
	 * 
	 * @param context
	 *            。
	 */
	public static void cleanFile(Context context) {
		FileOutputStream fileos = null;
		String cleanStr = "";
		try {
			fileos = context.openFileOutput(FILENAME_STRING,
					Context.MODE_WORLD_READABLE);
			fileos.write(cleanStr.getBytes());
			if (fileos != null) {
				fileos.close();
			}
		} catch (Exception e) {
			Log.e("FileNotFoundException", "can't create FileOutputStream");
			return;
		}

	}


	/**
	 * 得到sdcard的路径
	 * 
	 * @return
	 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		} else {
			return null;
		}
		return sdDir.toString();
	}
}
