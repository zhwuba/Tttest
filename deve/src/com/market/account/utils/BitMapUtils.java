package com.market.account.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;

public class BitMapUtils {

	public static byte[] createBitByteArray(Bitmap bitmap) {
		if (null == bitmap)
			return null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		return os.toByteArray();
	}


	public static byte[] createBitByteArrayByDrawable(Drawable drawable) {
		BitmapDrawable bd = (BitmapDrawable) drawable;
		return createBitByteArray(bd.getBitmap());
	}


	public static Bitmap convertFileToBitmap() {
		Bitmap myBitmap = null;
		String sdPath = getSDPath();

		if (TextUtils.isEmpty(sdPath))
			return null;

		String fileString = sdPath + "/zhuoyoulogin/screen.png";

		File file = new File(fileString);

		if (file.exists()) {
			try {
				myBitmap = BitmapFactory.decodeFile(fileString);

			} catch (OutOfMemoryError e) {
				System.gc();
				myBitmap = null;
				e.printStackTrace();
			}

		}

		return myBitmap;
	}


	public static String getScreenShot() {
		String filePath = getSDPath() + "/zhuoyoulogin/screen.png";
		File f = new File(filePath);
		if (f.exists())
			return filePath;
		else
			return null;
	}


	public static void saveBitmapToFile(Bitmap bitmap) {
		String filePath = getSDPath() + "/zhuoyoulogin/";
		File dir = new File(filePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(filePath + "screen.png");
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if (fOut != null) {
				fOut.flush();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			if (fOut != null) {
				fOut.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}


	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			return null;
		}
		return sdDir.toString();
	}

}
