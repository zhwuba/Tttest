package com.zhuoyi.market.utils.gallery;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ImageUtils
{

	//需要导入外部jar文件 android-support-v4.jar
	 //开辟8M硬缓存空间
	 private final int hardCachedSize = 2*1024*1024;  
	 //软引用
	 private static final int SOFT_CACHE_CAPACITY = 40;
	 //hard cache
	 private final LruCache<String, Drawable> sHardBitmapCache = new LruCache<String, Drawable>(hardCachedSize)
	 {
		  /*@Override
		  public int sizeOf(String key, Drawable value)
		  {
			  return value.getRowBytes() * value.getHeight();
		  }*/
		  @Override
		  protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue)
		  {
		   Log.v("tag", "hard cache is full , push to soft cache");
		   //硬引用缓存区满，将一个最不经常使用的oldvalue推入到软引用缓存区
		   sSoftBitmapCache.put(key, new SoftReference<Drawable>(oldValue));
		  }
	 };
	 
	 private final static ConcurrentHashMap<String, SoftReference<Drawable>> sSoftBitmapCache = 
		  new  ConcurrentHashMap<String, SoftReference<Drawable>>(SOFT_CACHE_CAPACITY);
		 /* {
			  @Override
			  public SoftReference<Drawable> put(String key, SoftReference<Drawable> value)
			  {
				  
			   return super.put(key, value);
			  }
			  @Override
			  protected boolean removeEldestEntry(LinkedHashMap.Entry<String, SoftReference<Drawable>> eldest)
			  {
				   if(size() > SOFT_CACHE_CAPACITY)
				   {
				    Log.v("tag", "Soft Reference limit , purge one");
				    return true;
				   }
				   return false;
			  }
		  }*/
		 //缓存bitmap
		 public boolean putDrawable(String key, Drawable drawable)
		 {
			  if(drawable != null)
			  {
				   synchronized(sHardBitmapCache)
				   {
				    sHardBitmapCache.put(key, drawable);
				   }
				   return true;
			  }  
			  return false;
		 }
		 //从缓存中获取bitmap
		 public Drawable getDrawable(String key)
		 {
			  synchronized(sHardBitmapCache)
			  {
				   final Drawable drawable = sHardBitmapCache.get(key);
				   if(drawable != null)
				    return drawable;
			  }
			  //硬引用缓存区间中读取失败，从软引用缓存区间读取
			  synchronized(sSoftBitmapCache)
			  {
				   SoftReference<Drawable> drawableReference = sSoftBitmapCache.get(key);
				   if(drawableReference != null)
				   {
					    final Drawable drawable = drawableReference.get();
					    if(drawable != null)
					    	return drawable;
					    else
					    {
					     	sSoftBitmapCache.remove(key);
					    }
				   }
			  }
			  return null;
		 }
	
}
