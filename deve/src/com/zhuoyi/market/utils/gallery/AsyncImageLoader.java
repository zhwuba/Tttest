package com.zhuoyi.market.utils.gallery;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;
public class AsyncImageLoader {

    private Random mRandom;
    private ImageUtils mImageUtils;
    
    public static boolean mIsDownloadStartupImage = false;
        public AsyncImageLoader() {
            mRandom = new Random();
            mImageUtils = new ImageUtils();
        }
     
        public Drawable loadDrawable(final boolean isRefreshIcon,final String imageUrl, final String fileName,final ImageCallback imageCallback) {
        	Drawable drawable = null;
        	
        	try
        	{
        		drawable = mImageUtils.getDrawable(imageUrl);
        		
        	}catch(Exception e)
        	{
        		return drawable;
        	}
        	if(drawable!=null)
        	{
        		
        		return drawable;
        	}
            if(MarketUtils.isNoPicModelReally())
            {
            	return null;
            }
            if(isRefreshIcon == false)
            	return null;
            
            final Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    imageCallback.imageLoaded((Drawable) message.obj,imageUrl);
                }
            };
            new Thread() {
                @Override
                public void run() {
                	Drawable drawable = loadImageFromUrl(imageUrl,fileName);
                	mImageUtils.putDrawable(imageUrl, drawable);
                    Message message = handler.obtainMessage(0, drawable);
                    handler.sendMessageDelayed(message, mRandom.nextInt(500));
                }
            }.start();
            return null;
        }

        
        public static boolean downloadImgToFileSys(String url,String fileName)
        {
        	mIsDownloadStartupImage = true;
			URL m;
			InputStream i = null;
			File f, dirs;
			StringBuilder filePath;
			boolean result = false;
			DataInputStream in = null;
			FileOutputStream out = null;
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        	{
        		filePath = new StringBuilder(Environment.getExternalStorageDirectory().toString());
        		filePath.append(Constant.download_path);
        		filePath.append("download/cache/image/");
        		dirs = new File(filePath.toString());
        		if(!dirs.exists())
        		{
        			dirs.mkdirs();
        		}
        		filePath.append(fileName);
        		
        		f = new File(filePath.toString());
        		File frameTmpFile = new File(filePath.toString() + ".tmp");
	            if(f.exists()) 
	            {
					//f.delete();
	            	return true;
	            }
	            if(frameTmpFile.exists()) {
	            	frameTmpFile.delete();
	            }
	            
	            try 
	            {
	                m = new URL(url);
	                i = (InputStream) m.getContent();
	    			in = new DataInputStream(i);
	    			out = new FileOutputStream(frameTmpFile);
	    			byte[] buffer = new byte[2048];
	    			int   byteread=0;
	    			while ((byteread = in.read(buffer)) != -1)
	    			{
	    				out.write(buffer, 0, byteread);
	    			}
	    			f.delete();
	    			frameTmpFile.renameTo(f);
	    			result = true;
	    			
	            } catch (MalformedURLException e1) {
	                e1.printStackTrace();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	            finally
	            {
	            	
	    			try
	    			{
	    				if(in!=null)
	    					in.close();
	    				if(out!=null)
	    					out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			mIsDownloadStartupImage = false;
	            }
        	}
			return result;
        }
        public static Drawable getStartupImageDrawable(Context context)
        {
        	Drawable drawable = null;
        	StringBuilder filePath;
        	String fileName = "";
          	Bitmap myBitmap = null;
        	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        	{
        		fileName = MarketUtils.getUpdateStartupImageFileName(context);
        		if(TextUtils.isEmpty(fileName))
        			return null;
        		filePath = new StringBuilder(Environment.getExternalStorageDirectory().toString());
        		filePath.append(Constant.download_cache_dir);
        		filePath.append(fileName.hashCode());
        		myBitmap = MarketUtils.read565Bitmap(context, filePath.toString());
        		if (myBitmap != null) 
        		{
        			drawable = new BitmapDrawable(myBitmap);
        		}     		
        	}
 
        	return drawable;
        }
        public static Drawable loadImageFromUrl(String url,String fileName) {
            URL m;
            InputStream i = null;            
            File f,dirs;
            Drawable d =null;
            StringBuilder filePath;
			FileInputStream fis = null;
        	if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        	{
        		filePath = new StringBuilder(Environment.getExternalStorageDirectory().toString());
        		filePath.append(Constant.download_path);
        		filePath.append("download/cache/image/");
        		dirs = new File(filePath.toString());
        		if(!dirs.exists())
        		{
        			dirs.mkdirs();
        		}
        		filePath.append(fileName);
        		
        		f = new File(filePath.toString());
	            if(f.exists()) {
					try {
						fis = new FileInputStream(f);
						d = Drawable.createFromStream(fis, "src");
						fis.close();
						return d;
					} 
					catch (OutOfMemoryError e)
					{
					    e.printStackTrace();
					    System.gc();
					    return null;
					}
					catch (Exception e)
					{
						e.printStackTrace();
						return null;
					}
	            }
	            
	            try {
	                m = new URL(url);
	                i = (InputStream) m.getContent();
	    			DataInputStream in = new DataInputStream(i);
	    			FileOutputStream out = new FileOutputStream(f);
	    			byte[] buffer = new byte[1024];
	    			int   byteread=0;
	    			while ((byteread = in.read(buffer)) != -1) {
	    				out.write(buffer, 0, byteread);
	    			}
	    			in.close();
	    			out.close();
	                
	    			fis = new FileInputStream(f);
	                d = Drawable.createFromStream(fis, "src");
	                fis.close();
	                i.close();
	            }
	            catch (OutOfMemoryError e)
                {
                    e.printStackTrace();
                    System.gc();
                    return null;
                }
	            catch (Exception e1) {
	                e1.printStackTrace();
	            }
        	}else{
				try {
					m = new URL(url);
	    			i = (InputStream) m.getContent();
	    			d = Drawable.createFromStream(i, "src");	    			
				}
				catch (OutOfMemoryError e)
                {
                    e.printStackTrace();
                    System.gc();
                    return null;
                }
				catch (Exception e) {
					e.printStackTrace();					
				}
    		}
        	return d;
        }
      
         public interface ImageCallback {
             public void imageLoaded(Drawable imageDrawable,String imageUrl);
         }

}

