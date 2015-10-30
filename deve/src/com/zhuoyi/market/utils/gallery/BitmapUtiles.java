package com.zhuoyi.market.utils.gallery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;

public class BitmapUtiles {

	/**
	 * ��ӵ�Ӱ��ԭ�?�ȷ�תͼƬ�����ϵ��·Ŵ�͸����
	 * 
	 * @param originalImage
	 * @return
	 */
	public static Bitmap createReflectedImage(Bitmap originalImage) {
		// The gap we want between the reflection and the original image
		final int reflectionGap = 4;

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// This will not scale but will flip on the Y axis
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		// Create a Bitmap with the flip matrix applied to it.
		// We only want the bottom half of the image
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0,
				height / 2, width, height / 2, matrix, false);

		// Create a new bitmap with same width but taller to fit reflection
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
				(height + height / 2), Config.ARGB_8888);

		// Create a new Canvas with the bitmap that's big enough for
		// the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		// Draw in the original image
		canvas.drawBitmap(originalImage, 0, 0, null);
		// Draw in the gap
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
		// Draw in the reflection
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);

		// Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		LinearGradient shader = new LinearGradient(0,
				originalImage.getHeight(), 0, bitmapWithReflection.getHeight()
						+ reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);
		// Set the paint to use this shader (linear gradient)
		paint.setShader(shader);
		// Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, paint);

		if (null != originalImage)
		{
			originalImage.recycle();
		}
		
		return bitmapWithReflection;
	}
	//drawable ����ת��Ϊbitmap
	public static Bitmap drawableToBitmap(Drawable drawable) {

		if(drawable==null)
			return null;
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
				.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	
	public static Bitmap drawable2Bitmap(Drawable drawable) {
		if (drawable == null) return null;
		
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		} else {
			return drawableToBitmap(drawable);
		}
	}
	
	
	//creates a rounded corner bitmap
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		// final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		// paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		if (null != bitmap) {
			bitmap.recycle();
		}
		return output;
	}
	public static Bitmap convertFileToBitmap2(String saveName)
	{
		Bitmap myBitmap=null;
		String sdPath = MarketUtils.FileManage.getSDPath();
		
		if(TextUtils.isEmpty(sdPath))
			return null;
		
		String fileString = sdPath+ Constant.download_path+"download/cache/image/" + saveName;
		
		File file = new File(fileString);

		if(file.exists())
		{
			BitmapFactory.Options options = new BitmapFactory.Options(); 
	        options.inSampleSize=1; 
	        myBitmap = BitmapFactory.decodeFile(fileString,options);
	        myBitmap=Bitmap.createScaledBitmap(myBitmap, 180, 251, true);//预先缩放，避免实时缩放，可以提高更新率  	
		}

		return myBitmap;
	}
	public static Bitmap convertFileToBitmap(String saveName)
	{
		Bitmap myBitmap=null;
		String sdPath = MarketUtils.FileManage.getSDPath();
		
		if(TextUtils.isEmpty(sdPath))
			return null;
		
		String fileString = sdPath+ Constant.download_path+"download/cache/image/" + saveName;
		
		File file = new File(fileString);

		if(file.exists())
		{
		    try
		    {
		        BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565; 
             
                opt.inPurgeable = true;          
                 
                opt.inInputShareable = true; 
                
                //bitmap = BitmapFactory.decodeStream(inputStream,null,opt);
		        myBitmap = BitmapFactory.decodeFile(fileString,opt);
		        
		    }catch(OutOfMemoryError e)
		    {
		        System.gc();
		        myBitmap = null;
		        e.printStackTrace();
		    }
			
		}

		return myBitmap;
	}
	public static boolean saveBitmapToFile(String saveName,Bitmap myBitmap)
	{
		String sdPath = MarketUtils.FileManage.getSDPath();
		boolean success = false;
		File dir = new File(sdPath+Constant.download_path+"download/cache/image/");
		if(!dir.exists())
		{
			dir.mkdirs();
		}
		File f = new File(sdPath+ Constant.download_path+"download/cache/image/" + saveName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			
			success = myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			if(fOut!=null) {
				fOut.flush();
				fOut.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

}
