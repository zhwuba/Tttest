package com.freeme.themeclub.theme.onlinetheme.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class BitmapUtiles {

	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // жsdǷ
		if (sdCardExist){
			sdDir = Environment.getExternalStorageDirectory();
		}else{
			return null;
		}
		return sdDir.toString();
	}
	
	public static Bitmap convertFileToBitmap2(String saveName){
		Bitmap myBitmap=null;
		String sdPath = getSDPath();
		
		if(TextUtils.isEmpty(sdPath))
			return null;
		
		String fileString = OnlineThemesUtils.getDownLoadPath()+"download/cache/image/" + saveName;
		
		File file = new File(fileString);

		try{
			if(file.exists()){
				BitmapFactory.Options options = new BitmapFactory.Options(); 
		        options.inSampleSize=2; 
		        options.inPurgeable = true;
		        options.inDither = true;
		        myBitmap = BitmapFactory.decodeFile(fileString);
		        if(myBitmap!=null)
		        	myBitmap=Bitmap.createScaledBitmap(myBitmap, 338, 600, true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		

		return myBitmap;
	}
	public static Bitmap convertFileToBitmap(String saveName){
		Bitmap myBitmap=null;
		String sdPath = getSDPath();
		
		if(TextUtils.isEmpty(sdPath))
			return null;
		
		String fileString = OnlineThemesUtils.getDownLoadPath()+"download/cache/image/" + saveName;
		
		File file = new File(fileString);
		
		try{
			if(file.exists()){
				myBitmap = BitmapFactory.decodeFile(fileString);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	
		return myBitmap;
	}
	public static void saveBitmapToFile(String saveName,Bitmap myBitmap){
		String sdPath = getSDPath();
		
		File dir = new File(OnlineThemesUtils.getDownLoadPath()+"download/cache/image/");
		if(!dir.exists()){
			dir.mkdirs();
		}
		File f = new File(OnlineThemesUtils.getDownLoadPath()+"download/cache/image/" + saveName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			
			myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			if(fOut!=null){
				fOut.flush();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			if(fOut!=null){
				fOut.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	
	}
	
	public static byte[] flattenBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
