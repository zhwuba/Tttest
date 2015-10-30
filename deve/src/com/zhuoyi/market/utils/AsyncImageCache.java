/*
 * Copyright (C) 2013 tyd Jack 20131205
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhuoyi.market.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.RejectedExecutionException;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;

import com.market.net.data.CornerIconInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.ImageLoadedCallBack;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;

public class AsyncImageCache {

	private Context mContext;
	private int iconPxHome = 0; //精选图标大小
	private int iconPxAHotCate = 0; //热门分类A图标大小
	private int iconPxBHotCate = 0; //热门分类B图标大小(默认分类尺寸一样)
	private int iconPxCategoty = 0; //二级分类图标大小(style=1或2时使用)
	private int iconPxDCategoty = 0; //二级分类图标大小(默认style时使用)

	private Map<String, Integer> cornerIconMap = new HashMap<String, Integer>();
	private static AsyncImageCache instance;
	
	private Random mRandom;

	public LruCache<String, Bitmap> mMemoryCache;
	

	private static final long DISK_CACHE_SIZE = 1024 * 1024 * 5; // 5MB
	private static final String DISK_CACHE_SUBDIR = Constant.download_cache_dir;//"AsyncImageCache";
	private DiskLruCache mDiskCache;
	public DiskLruCache getDiskLruCache() {
		return mDiskCache;
	}

	private Stack<AsyncImageLoadTask> mTaskStack = new Stack<AsyncImageLoadTask>();

	private static int sDisplayWidthPixels = 480;
	private static int sDisplayHeightPixels = 800;

	private static boolean sDiskCacheEnable = false;
	private static long sDiskCacheSize = DISK_CACHE_SIZE;
	private static int sDiskCacheCount = 64;
	private static String sDiskCacheDir = null;

	private static int sMemoryCacheSize = 0;
	
	private HashMap<String, WeakReference<ImageLoadedCallBack>> mImageLoadedCallBack = new HashMap<String, WeakReference<ImageLoadedCallBack>>();

	/**
	 * Sets the disk cache enable.
	 *
	 * @param enable the new disk cache enable
	 */
	public static void setDiskCacheEnable(boolean enable) {
		sDiskCacheEnable = enable;
	}

	/**
	 * Sets the disk cache size.
	 *
	 * @param size the new disk cache size
	 */
	public static void setDiskCacheSize(long size) {
		sDiskCacheSize = size;
	}

	/**
	 * Sets the disk cache dir.
	 *
	 * @param dir the new disk cache dir
	 */
	public static void setDiskCacheDir(String dir) {
		sDiskCacheDir = dir;
	}

	public static void setDiskCacheCount(int count) {
		sDiskCacheCount = count;
	}

	public static void setMemoryCacheSize(int size) {
		sMemoryCacheSize = size;
	}

	/**
	 * Get instance
	 * 
	 * @param context
	 * @return
	 */
	public static AsyncImageCache from(Context context) {

		if (Looper.myLooper() != Looper.getMainLooper()) {
			throw new RuntimeException("Cannot instantiate outside UI thread.");
		}

		if (instance == null) {
			instance = new AsyncImageCache(context.getApplicationContext());
		}

		return instance;
	}

	private Bitmap bitmapZoomBySize(Bitmap srcBitmap,int newWidth,int newHeight) {
		int srcWidth = srcBitmap.getWidth();   
		int srcHeight = srcBitmap.getHeight();    
		Bitmap resizedBitmap = null;
		float scaleWidth = ((float) newWidth) / srcWidth;   
		float scaleHeight = ((float) newHeight) / srcHeight;   

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);   
		try {
			resizedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth,   
					srcHeight, matrix, true);
		} catch(Exception e) {
			e.printStackTrace();
			resizedBitmap = null;
		} catch(OutOfMemoryError e) {
			e.printStackTrace();
			resizedBitmap = null;
		}

		if(resizedBitmap != null) {
			if (srcBitmap != null && !srcBitmap.isRecycled() && srcBitmap != resizedBitmap) {
				srcBitmap.recycle();
				srcBitmap = null;
			}
			return resizedBitmap;
		} else {
			return srcBitmap;
		}

	}
	/**
	 * Instantiates a new async image loader.
	 * 
	 * @param context
	 *            the context
	 */
	private AsyncImageCache(Context context) {
		mContext = context;
		mRandom = new Random();
		int memClass = ((ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;

		if(sMemoryCacheSize ==0 ) {
			// default Heap * 1/8
			sMemoryCacheSize = memClass / 10;
		} else if(sMemoryCacheSize > memClass / 4) {
			// max Heap * 1/4
			sMemoryCacheSize = memClass / 4;
		}
		
		final int cacheSize = sMemoryCacheSize;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {

			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getRowBytes() * bitmap.getHeight();
			}

		};

//		if (mReleaseImage == null)
//		    mReleaseImage = new ArrayList<String>();

		if(sDiskCacheEnable) {
			File cacheDir = null;
			if(sDiskCacheDir != null) {
				cacheDir = new File(sDiskCacheDir, DISK_CACHE_SUBDIR);
				cacheDir.mkdirs();
			} else {
				cacheDir= DiskLruCache
				.getDiskCacheDir(context, DISK_CACHE_SUBDIR);
			}

			mDiskCache = DiskLruCache.openCache(context, cacheDir, sDiskCacheSize, sDiskCacheCount);
		}
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		sDisplayWidthPixels = dm.widthPixels;
		sDisplayHeightPixels = dm.heightPixels;

		initCornerIconData();
		
		iconPxHome = context.getResources().getDimensionPixelSize(R.dimen.common_app_icon_size);
        iconPxAHotCate = context.getResources().getDimensionPixelSize(R.dimen.category_a_hot_icon_size);
        iconPxBHotCate = context.getResources().getDimensionPixelSize(R.dimen.category_b_hot_icon_size);
        iconPxCategoty = context.getResources().getDimensionPixelSize(R.dimen.category_item_image_size);
        iconPxDCategoty = context.getResources().getDimensionPixelSize(R.dimen.category_item_image_size_d);
        	
	}

	/**
	 * Display Image
	 * 
	 * @param imageView
	 * @param resId
	 * @param imageGenerator
	 */
	public void displayImage(boolean isRefreshIcon,ImageView imageView, int resId,
			ImageGenerator<?> imageGenerator, boolean roundCorner) {
		switch(resId) {
		case R.drawable.picture_bg1_big:
			//精选
			displayImage(isRefreshIcon,false,imageView, resId, iconPxHome, iconPxHome, imageGenerator, roundCorner, false,false, null);
			break;
		/*case R.drawable.picture_bg1:
			//列表
			displayImage(isRefreshIcon,false,imageView, resId, iconPxList, iconPxList, imageGenerator, roundCorner, false,false, null);
			break;*/
		case R.drawable.category_a_hot_icon_bg:
			//热门分类A
			displayImage(isRefreshIcon,false,imageView, resId, iconPxAHotCate, iconPxAHotCate, imageGenerator, roundCorner, false,false, null);
			break;
		case R.drawable.category_b_hot_icon_bg:
		    //热门分类B(默认热门分类)
            displayImage(isRefreshIcon,false,imageView, resId, iconPxBHotCate, iconPxBHotCate, imageGenerator, roundCorner, false,false, null);
            break;
		case R.drawable.category_icon_bg:
			//二级分类(style:1/2)
			displayImage(isRefreshIcon,false,imageView, resId, iconPxCategoty, iconPxCategoty, imageGenerator, roundCorner, false,false, null);
			break;
		case R.drawable.category_icon_d_bg:
		    //二级分类(style默认)
		    displayImage(isRefreshIcon,false,imageView, resId, iconPxDCategoty, iconPxDCategoty, imageGenerator, roundCorner, false,false, null);
		    break;
		}
	}


	//角标图片
	private void initCornerIconData() {
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type0), R.drawable.corner_icon_image_type0);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type1), R.drawable.corner_icon_image_type1);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type2), R.drawable.corner_icon_image_type2);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type3), R.drawable.corner_icon_image_type3);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type4), R.drawable.corner_icon_image_type4);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type5), R.drawable.corner_icon_image_type5);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type6), R.drawable.corner_icon_image_type6);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type7), R.drawable.corner_icon_image_type7);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type8), R.drawable.corner_icon_image_type8);
		
		String iden = "iden";
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type0) + iden, R.drawable.iden_icon_image_type0);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type1) + iden, R.drawable.iden_icon_image_type1);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type2) + iden, R.drawable.iden_icon_image_type2);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type3) + iden, R.drawable.iden_icon_image_type3);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type4) + iden, R.drawable.iden_icon_image_type4);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type5) + iden, R.drawable.iden_icon_image_type5);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type6) + iden, R.drawable.iden_icon_image_type6);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type7) + iden, R.drawable.iden_icon_image_type7);
		cornerIconMap.put(mContext.getString(R.string.corner_icon_type8) + iden, R.drawable.iden_icon_image_type8);
		
	}
	
	/**
	 * 
	 * @param imageView
	 * @param cornerIconInfo
	 * @param showType   角标显示类型  0: GridView（角标） ， 1：ListView (文字标识)
	 */
	public void displayImage(ImageView imageView, CornerIconInfoBto cornerIconInfo, int showType) {
		//如果该类型本地内置，使用本地内置图片，如果不存在就走default，去下载图片
		Integer type = -1;
		if(showType == 0) {
			type = cornerIconMap.get(cornerIconInfo.getName());
		} else if(showType == 1) {
			type = cornerIconMap.get(cornerIconInfo.getName() + "iden");
		}
		
		if (type != null) {
			imageView.setImageResource(type);
		} else {
			displayImage(true, false, imageView, -1, 0, 0,
					new AsyncImageCache.NetworkImageGenerator(cornerIconInfo.getName(),cornerIconInfo.getHdImageUrl()),
					false, false,false,null);
		}	
	}    


	
	public void displayImage(ViewGroup parentView, int iconType) {
		//积分是0，礼包是2，活动是4
		int type = -1;
		switch (iconType) {
		case 0:
			type = cornerIconMap.get(mContext.getString(R.string.corner_icon_type0) + "iden");
			break;
		case 2:
			type = cornerIconMap.get(mContext.getString(R.string.corner_icon_type1) + "iden");
			break;
		case 4:
			type = cornerIconMap.get(mContext.getString(R.string.corner_icon_type4) + "iden");
			break;
		}
		
		if (type != -1) {
			ImageView imageView = new ImageView(mContext);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.leftMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_corner_left_margin);
//			params.topMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_corner_top_margin);
			imageView.setLayoutParams(params);
			imageView.setImageResource(type);
			parentView.addView(imageView);
			
		}
	}    
	
	

	/**
	 * Display Image by width & height
	 * @param isRefreshIcon
	 * @param showBgIcon
	 * @param imageView
	 * @param resId
	 * @param width
	 * @param height
	 * @param imageGenerator
	 * @param roundCorner
	 * @param onlyZoom
	 * @param isAlwaysShow 始终显示图片的开关,不受其他flag或无图模式的影响
	 */
	@TargetApi(11)
	public void displayImage(boolean isRefreshIcon,boolean showBgIcon,ImageView imageView, int resId, int width,
			int height, ImageGenerator<?> imageGenerator, boolean roundCorner, boolean onlyZoom,boolean isAlwaysShow,String releaseTag) {
		if(imageView == null) {
			return;
		}

		if(resId >= 0) {
			if(showBgIcon) {           
				try {
					imageView.setImageResource(resId);
					imageView.setTag(R.id.tag_image_resid, resId);
				} catch (OutOfMemoryError e) {
					System.gc();
				}
			}
		}

		if (imageGenerator == null || imageGenerator.getTag() == null) {
			return;
		}

		String tag = imageGenerator.getTag();
		imageView.setTag(tag);

		String key = (width != 0 && height != 0) ? tag + width + height : tag;
		Bitmap bitmap = null;
		if (roundCorner) {
			key = key + "round"; 
		}
		
        synchronized (mMemoryCache) {
//            if (releaseTag != null) {
//                key = key +  releaseTag;
//            }
            bitmap = mMemoryCache.get(key);
        }

		if(bitmap != null) {
			setImageBitmap(imageView, bitmap, false);
			return;
		} else if(!showBgIcon && resId >= 0) {
			try {
				imageView.setImageResource(resId);
				imageView.setTag(R.id.tag_image_resid, resId);
			} catch (OutOfMemoryError e) {
				System.gc();
			}
		}

		if(!isRefreshIcon) {
			return;
		}

		synchronized (mTaskStack) {
			for (AsyncImageLoadTask asyncImageTask : mTaskStack) {
				if (asyncImageTask != null 
						&& asyncImageTask.mImageRef != null
						&& tag.equals(asyncImageTask.mImageRef.tag)) {
					if(!asyncImageTask.imageViewList.contains(imageView)) {
						//不同imageView显示同一张图片的处理
						asyncImageTask.imageViewList.add(imageView);
					}
					return;
				}
			}
		}
		
		String callBack = "";
		try {
		    callBack = (String) imageGenerator.mParams[1];
		} catch (Exception e) {
		    callBack = null;
		}
		boolean isCallBack = false;
		if ("callBack".equals(callBack)) {
		    isCallBack = true;
		}

		ImageRef imageRef = new ImageRef(imageView, tag, resId, width, height,
				imageGenerator, roundCorner, onlyZoom, releaseTag, isCallBack, isAlwaysShow);

		AsyncImageLoadTask asyncImageTask = new AsyncImageLoadTask();
		mTaskStack.push(asyncImageTask); 

		try {
			if(Build.VERSION.SDK_INT >= 11) {
				asyncImageTask.executeOnExecutor(MarketUtils.getImgReqExecutor(), imageRef);
			} else {
				asyncImageTask.execute(imageRef);
			}
		} catch(RejectedExecutionException e) {
			//到达AsyncTask线程池task队列上限(128)
			mTaskStack.remove(asyncImageTask);
		}
	}
	
	
    public void displayImage(ImageGenerator<?> imageGenerator, boolean isAlwaysShow) {
    
        if (imageGenerator == null || imageGenerator.getTag() == null) {
            return;
        }

		String callBack = "";
		try {
		    callBack = (String) imageGenerator.mParams[1];
		} catch (Exception e) {
		    callBack = null;
		}
		boolean isCallBack = false;
		if ("callBack".equals(callBack)) {
		    isCallBack = true;
		}

        ImageRef imageRef = new ImageRef(null, imageGenerator.getTag(), -1, 0, 0,
                imageGenerator, false, false, null, isCallBack,isAlwaysShow);
        
        AsyncImageLoadTask asyncImageTask = new AsyncImageLoadTask();
        mTaskStack.push(asyncImageTask); 
    
        try {
            if(Build.VERSION.SDK_INT >= 11) {
                asyncImageTask.executeOnExecutor(MarketUtils.getImgReqExecutor(), imageRef);
            } else {
                asyncImageTask.execute(imageRef);
            }
        } catch(RejectedExecutionException e) {
            mTaskStack.remove(asyncImageTask);
        }
    }
    
    
    public void displayImage(ImageGenerator<?> imageGenerator, String from) {
    
        if (imageGenerator == null || imageGenerator.getTag() == null || MarketUtils.isNoPicModelReally()) {
            return;
        }
        
        int iconPx = iconPxHome;
        /*if (from.contains(ReportFlag.FROM_HOMEPAGE)
                || from.contains(ReportFlag.FROM_DOWN_GIFT)
                || from.contains(ReportFlag.FROM_TYD_LAUNCHER)) {
            iconPx = iconPxHome;
        }*/
    
        ImageRef imageRef = new ImageRef(null, imageGenerator.getTag(), -1, iconPx, iconPx,
                imageGenerator, true, false, null, true,false);
        
        AsyncImageLoadTask asyncImageTask = new AsyncImageLoadTask();
        mTaskStack.push(asyncImageTask); 
    
        try {
            if(Build.VERSION.SDK_INT >= 11) {
                asyncImageTask.executeOnExecutor(MarketUtils.getImgReqExecutor(), imageRef);
            } else {
                asyncImageTask.execute(imageRef);
            }
        } catch(RejectedExecutionException e) {
            mTaskStack.remove(asyncImageTask);
        }
    }
    
    
    public Bitmap getBitmapWithTag(String tag) {
        
        synchronized (mMemoryCache) {
            return mMemoryCache.get(tag);
        }
    }
    
    
    public void removeBitmapWithTag(String tag) {
        
        synchronized (mMemoryCache) {
            mMemoryCache.remove(tag);
        }
    }    
    
    
    public void setImageLoadedCallBack (String key, ImageLoadedCallBack callBack) {
        if (callBack != null) {
            WeakReference<ImageLoadedCallBack> wCallBack = new WeakReference<ImageLoadedCallBack> (callBack);
            mImageLoadedCallBack.put(key, wCallBack);
        }
    }
    
    
    public void removeImageLoadedCallBack(String key){
        mImageLoadedCallBack.remove(key);
    }


	/**
	 * Call by Activity#onDestroy
	 */
	public void stop() {

		synchronized (mTaskStack) {
			while (!mTaskStack.empty()) {
				AsyncImageLoadTask asyncImageTask = mTaskStack.pop();
				asyncImageTask.cancel(true);
			}
		}
	}

	public void cleanDiskCache() {
		if (sDiskCacheEnable && mDiskCache != null) {
			synchronized (mDiskCache) {
				mDiskCache.clearCache();
			}
		}
	}

	
/*	public void cleanAllMemeryCache () {
	    new Thread () {
            @Override
            public void run () {
                synchronized (mMemoryCache) { 
                    //释放图片缓存，如果需要，请打开
                    //mMemoryCache.evictAll();
                    mReleaseImage.clear();
                    System.gc();
                }
            }
        }.start();
	}*/
	
	
/*	public void cleanMemoryReleaseCache(final String tag) {
	    
	    new Thread () {
	        @Override
	        public void run () {
	            synchronized (mMemoryCache) { 
    	            int count = mReleaseImage.size() - 1;
    	            String key = null;
    	            Bitmap value = null;
    	            while (count >= 0) {
    	                key =  mReleaseImage.get(count) + tag;
    	                value = mMemoryCache.get(key);
    	                if (value != null) {
    	                    if (!value.isRecycled())
    	                        value.recycle();
    	                    mMemoryCache.remove(key);
    	                    mReleaseImage.remove(count);
    	                }
    	                count--;
    	            }
    	            System.gc();
	            }
	        }
	    }.start();
	}*/
	
	
	/**
	 * Image reference info
	 */
	static class ImageRef {

		ImageView imageView;
		String tag;
		int resId;
		int width = 0;
		int height = 0;

		ImageGenerator<?> imageGenerator;
		boolean lazy = false;

		boolean mImageRoundCorner = false;
		boolean mOnlyZoom = false;
		String mReleaseTag = null;
		boolean mCallBack = false;
		boolean mIsAlwaysShow= false;

		/**
		 * Constructor
		 * 
		 * @param imageView
		 * @param tag
		 * @param resId
		 * @param imageGenerator
		 */
		public ImageRef(ImageView imageView, String tag, int resId, ImageGenerator<?> imageGenerator, boolean roundCorner, boolean onlyZoom, String releaseTag, boolean callBack,boolean isAlwaysShow) {
			this.imageView = imageView;
			this.tag = tag;
			this.resId = resId;

			this.imageGenerator = imageGenerator;
			this.mImageRoundCorner = roundCorner;
			this.mOnlyZoom = onlyZoom;
			this.mReleaseTag = releaseTag;
			this.mCallBack = callBack;
			this.mIsAlwaysShow = isAlwaysShow;
		}

		public ImageRef(ImageView imageView, String tag, int resId,
				int width, int height, ImageGenerator<?> imageGenerator, boolean roundCorner, boolean onlyZoom, String releaseTag, boolean callBack,boolean isAlwaysShow) {

			this(imageView, tag, resId, imageGenerator, roundCorner, onlyZoom, releaseTag, callBack,isAlwaysShow);

			this.mImageRoundCorner = roundCorner;
			this.width = width;
			this.height = height;

		}

	}

	/**
	 * Sets the image bitmap.
	 *
	 * @param imageView the image view
	 * @param bitmap the bitmap
	 * @param isTran the is tran
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap,
			boolean isTran) {

		if(isTran) {
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(android.R.color.transparent),
							new BitmapDrawable(mContext.getResources(), bitmap) });
			td.setCrossFadeEnabled(true);
			imageView.setImageDrawable(td);
			td.startTransition(280);
		} else {
			imageView.setImageBitmap(bitmap);
		}
		imageView.setTag(R.id.tag_image_resid, -1);
	}

	public Bitmap toRoundCorner(Bitmap bitmap, int pixels) { 
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888); 
			Canvas canvas = new Canvas(output); 
			final Paint paint = new Paint(); 
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
			final RectF rectF = new RectF(rect); 
			paint.setAntiAlias(true); 
			canvas.drawARGB(0, 0, 0, 0); 
			paint.setColor(0xff424242); 
			canvas.drawRoundRect(rectF, pixels, pixels, paint); 
			paint.setXfermode(new android.graphics.PorterDuffXfermode(Mode.SRC_IN)); 
			canvas.drawBitmap(bitmap, rect, rect, paint);
			if (bitmap != null && !bitmap.isRecycled() && bitmap != output) {
				bitmap.recycle();
				bitmap = null;
			}
			return output; 
		} catch (OutOfMemoryError e) {
			return bitmap;
		}
	}	

	/** 
	 * 获取裁剪后的圆形图片 
	 *  
	 * @param radius 
	 *            半径 
	 */  
	public static Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {  
		Bitmap scaledSrcBmp;  
		int diameter = radius;
		// 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片  
		int bmpWidth = bmp.getWidth();  
		int bmpHeight = bmp.getHeight();  
		int squareWidth = 0, squareHeight = 0;  
		int x = 0, y = 0;  
		Bitmap squareBitmap;  
		if (bmpHeight > bmpWidth) {// 高大于宽  
			squareWidth = squareHeight = bmpWidth;  
			x = 0;  
			y = (bmpHeight - bmpWidth) / 2;  
			// 截取正方形图片  
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,  
					squareHeight); 
			if (bmp != null && !bmp.isRecycled() && bmp != squareBitmap) {
    			bmp.recycle();
    			bmp = null;
			}
		} else if (bmpHeight < bmpWidth) {// 宽大于高  
			squareWidth = squareHeight = bmpHeight;  
			x = (bmpWidth - bmpHeight) / 2;  
			y = 0;  
			squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,  
					squareHeight);
			if (bmp != null && !bmp.isRecycled() && bmp != squareBitmap) {
    			bmp.recycle();
    			bmp = null;
			}
		} else {  
			squareBitmap = bmp;  
		}  

		if (squareBitmap.getWidth() != diameter  
				|| squareBitmap.getHeight() != diameter) {  
			scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,  
					diameter, true); 
			if (squareBitmap != null && !squareBitmap.isRecycled() && scaledSrcBmp != squareBitmap) {
    			squareBitmap.recycle();
    			squareBitmap = null;
			}
		} else {  
			scaledSrcBmp = squareBitmap;  
		}  
		Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),  
				scaledSrcBmp.getHeight(), Config.ARGB_8888);  
		Canvas canvas = new Canvas(output);  

		Paint paint = new Paint();  
		Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),  
				scaledSrcBmp.getHeight());  

		paint.setAntiAlias(true);  
		paint.setFilterBitmap(true);  
		paint.setDither(true);  
		canvas.drawARGB(0, 0, 0, 0);  
		canvas.drawCircle(scaledSrcBmp.getWidth() / 2,  
				scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2,  
				paint);  
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
		canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);

		if (scaledSrcBmp != null && !scaledSrcBmp.isRecycled() && scaledSrcBmp != output) {
    		scaledSrcBmp.recycle();
    		scaledSrcBmp = null; 
		}
		return output;  
	}
	public static Bitmap addBorder(Bitmap bitmap) {
		Canvas canvas = new Canvas(bitmap); 
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(4);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(Color.WHITE);  
		paint.setStyle(Paint.Style.STROKE);  
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2 - 2, paint);
		return bitmap;
	}
	
	public static Bitmap addBorder(Bitmap bitmap, int strokeWidth) {
		Canvas canvas = new Canvas(bitmap); 
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(strokeWidth);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		paint.setColor(Color.WHITE);  
		paint.setStyle(Paint.Style.STROKE);  
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2 - (float)strokeWidth / 2, paint);
		return bitmap;
	}
	
	public static class MD5 {
		public static String Md5(String str) {
			if(str != null && !str.equals("")) {
				try {
					MessageDigest md5 = MessageDigest.getInstance("MD5");
					char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
							'9', 'a', 'b', 'c', 'd', 'e', 'f' };
					byte[] md5Byte = md5.digest(str.getBytes("UTF8"));
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < md5Byte.length; i++) {
						sb.append(HEX[(int) (md5Byte[i] & 0xff) / 16]);
						sb.append(HEX[(int) (md5Byte[i] & 0xff) % 16]);
					}
					str = sb.toString();
				} catch (NoSuchAlgorithmException e) {
				} catch (Exception e) {
				}
			}
			return str;
		}

	}

	/**
	 * Abstract class ImageGenerator.
	 */
	public static abstract class ImageGenerator<Params> {
		private String mTag;
		protected Params[] mParams;

		/**
		 * Instantiates a new image generator.
		 *
		 * @param tag the tag
		 * @param params the params
		 */
		public ImageGenerator(String tag, Params... params) {
//			if(tag.startsWith("http://")) {
//				tag = Util.getImgUrlKey(tag); 
//			}
			mTag = tag;
			mParams = params;
		}

		/**
		 * Generate.
		 *
		 * @return the bitmap
		 */
		public abstract Bitmap generate();

		/**
		 * Gets the tag.
		 *
		 * @return the tag
		 */
		public String getTag() {
			return mTag;
		}
	}

	
	/*
	 * 565  16位字节Bitmap处理，含透明像素图片勿用此方法
	 */
	
	public static class NetworkImage565Generator extends NetworkImageGenerator {

		private boolean mZoom = true;
		
		public NetworkImage565Generator(String tag, String params) {
			super(tag, params);
			mZoom = true;
		}
		
		//壁纸使用
        public NetworkImage565Generator(String tag, String params, String params2) {
            super(tag, params, params2);
            mZoom = false;
        }

		@Override
		public Bitmap generate() {
			Bitmap bitmap = null;
			byte[] data = loadByteArrayFromNetwork(mParams[0]);
			if (data != null) {
				bitmap = getBitmapByBytes(data, true, mZoom);
			}
			return bitmap;
		}
		
	}
	
	
	/**
	 * Class NetworkImageGenerator, the image from network.
	 */
	public static class NetworkImageGenerator extends ImageGenerator<String> {

		/**
		 * Instantiates a new network image generator.
		 *
		 * @param tag the key 请勿直接传imageurl 
		 * @param params the image url
		 */
		public NetworkImageGenerator(String tag, String params) {
			super(tag, params);
		}
		
		
        public NetworkImageGenerator(String tag, String params, String params2) {
            super(tag, params, params2);
        }

		/* (non-Javadoc)
		 * @see com.android.internal.util.AsyncImageCache.ImageGenerator#generate()
		 */
		@Override
		public Bitmap generate() { 
			Bitmap bitmap = null;
			byte[] data = loadByteArrayFromNetwork(mParams[0]);
			if (data != null) {
				bitmap = getBitmapByBytes(data, false, true);
			}
			return bitmap;
		}

		
		/**
		 *  是否获取 RGB_565 位图
		 * @param data
		 * @param isDecode565
		 * @return
		 */
		protected Bitmap getBitmapByBytes(byte[] data, boolean  isDecode565, boolean zoom) {
			Bitmap bitmap = null;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inSampleSize = 1;
			opt.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0,
					data.length, opt);
			int bitmapSize = opt.outHeight * opt.outWidth* 4;// pixels*3 if it's RGB and pixels*4
			// if it's ARGB
			if (bitmapSize > sDisplayWidthPixels * sDisplayHeightPixels * 4 && zoom)
				opt.inSampleSize = 2;

			opt.inPurgeable = true;
            opt.inInputShareable = true; 
			opt.inJustDecodeBounds = false;
			if(isDecode565) {
				opt.inPreferredConfig = Bitmap.Config.RGB_565; 
			}
			try {
				bitmap = BitmapFactory.decodeByteArray(data,
						0, data.length, opt);
				data = null;

			} catch(OutOfMemoryError e) {
			    if (bitmap != null && !bitmap.isRecycled()) {
			        bitmap.recycle();
			        bitmap = null;
			    }
			    data = null;
				System.gc();

			}
			return bitmap;
		}
		
		
		/**
		 * Load byte array from network.
		 *
		 * @param path the path
		 * @return the byte[]
		 */
		protected byte[] loadByteArrayFromNetwork(String path) {

			ByteArrayOutputStream outputStream = null;
			InputStream inputStream = null;
			try {
				URL url = new URL(path);
				inputStream = (InputStream) url.getContent();
				// int availableCount = 0;
				outputStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				/* while((availableCount=inputStream.available()) > 0){  
                    byte[] requestbuffer = new byte[availableCount];  
                    inputStream.read(requestbuffer); 
                    outputStream.write(requestbuffer, 0, availableCount);
                }*/
				while ((len = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, len);

				}
				outputStream.close();
				inputStream.close();
				inputStream = null;

				if (outputStream != null) {
					buffer = outputStream.toByteArray();
					outputStream = null;
					return buffer;
				}

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				try
				{
					if (outputStream != null)
					{
						outputStream.close();
					}
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			} 
			return null;
		}
	}

	/**
	 * Class NetworkImageGenerator, the image from exists bitmap.
	 */
	public static class GeneralImageGenerator extends ImageGenerator<Bitmap> {

		/**
		 * Instantiates a new General image generator.
		 *
		 * @param tag the tag
		 * @param params the params
		 */
		public GeneralImageGenerator(String tag, Bitmap params) {
			super(tag, params);
		}

		/* (non-Javadoc)
		 * @see com.android.internal.util.AsyncImageCache.ImageGenerator#generate()
		 */
		@Override
		public Bitmap generate() {
			return mParams[0];
		}
	}



	public static class UpdateAppImageGenerator extends ImageGenerator<Bitmap> {
		PackageManager mPkgManager;

		public UpdateAppImageGenerator(String tag, PackageManager pkgManager) {
			super(tag);
			mPkgManager = pkgManager;
		}


		@Override
		public Bitmap generate() {
			String pkgName = getTag();
			Drawable drawable = null;
			try {
				PackageInfo pInfo = mPkgManager.getPackageInfo(pkgName,PackageManager.GET_META_DATA);
				drawable = pInfo.applicationInfo.loadIcon(mPkgManager);
			} catch (NameNotFoundException e) { }

			return BitmapUtiles.drawableToBitmap(drawable);
		}
	}


	public interface ImageCallback {
		public void imageLoaded(Bitmap bmp);
	}

	/**
	 * Class AsyncImageLoadTask, the image load task.
	 */
	class AsyncImageLoadTask extends AsyncTask<ImageRef, Integer, Bitmap>
	{
		ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
		private ImageRef mImageRef;
		
		private final Handler handler = new Handler() {
			public void handleMessage(Message message) {

				if(message.what == 0) {
					ImageRef imageRef = mImageRef;

					if(imageRef == null)
						return;
					
					if (imageRef.mCallBack && mImageLoadedCallBack != null) {
					    Iterator iter = mImageLoadedCallBack.entrySet().iterator();
					    WeakReference<ImageLoadedCallBack> callBack;
					    while (iter.hasNext()) {
					        Map.Entry entry = (Map.Entry) iter.next();  
					        callBack = (WeakReference<ImageLoadedCallBack>)entry.getValue();
					        callBack.get().imageLoaded(imageRef.tag);
					    }  
					}

					if(imageRef.imageView == null
							|| imageRef.imageView.getTag() == null
							|| imageRef.tag == null)
						return;

					if(!(imageRef.tag).equals((String) imageRef.imageView
							.getTag())) {
						return;
					}

					Bitmap bitmap = (Bitmap)message.obj;
					setImageBitmap(imageRef.imageView, bitmap, imageRef.lazy);

					/**
					 * 多个ImageView 显示同一张图片
					 */
					for(int i = 0; i < imageViewList.size(); i++) {
						setImageBitmap(imageViewList.get(i), bitmap, imageRef.lazy);
					}
					imageViewList.clear();
					imageViewList = null;
				}       
			}
		};

		@Override
		protected Bitmap doInBackground(ImageRef... params)
		{
			if (isCancelled())
				return null;

			mImageRef = params[0];

			Bitmap bitmap = null;
			Bitmap tBitmap = null;

			if (mImageRef.tag == null)
				return null;

			// got from Disk Cache
			String diskCachekey = MD5.Md5(mImageRef.tag);
			String memoryCachekey = mImageRef.tag;
			if (mImageRef.width != 0 && mImageRef.height != 0)
			{
				diskCachekey = MD5.Md5(mImageRef.tag + mImageRef.width + mImageRef.height);
				memoryCachekey = mImageRef.tag + mImageRef.width + mImageRef.height;
			}

			if (sDiskCacheEnable && mDiskCache != null)
			{
				synchronized (mDiskCache)
				{
					bitmap = mDiskCache.get(diskCachekey);
				}
			}

			try
			{

				if (bitmap != null)
				{
					// save to memory cache
					if (mImageRef.mImageRoundCorner)
					{
						bitmap = toRoundCorner(bitmap, 12);
						memoryCachekey = memoryCachekey + "round";
					}

					synchronized (mMemoryCache)
					{
						// if (mImageRef.mReleaseTag != null) {
						// mReleaseImage.add(memoryCachekey);
						// memoryCachekey = memoryCachekey +
						// mImageRef.mReleaseTag;
						// }
						if (mMemoryCache.get(memoryCachekey) == null)
						{
							mMemoryCache.put(memoryCachekey, bitmap);
						}
					}
				}
				else if (mImageRef.mIsAlwaysShow
						|| !MarketUtils.isNoPicModelReally())
				{
					long startTime = System.currentTimeMillis();

					if (mImageRef.imageGenerator != null)
						tBitmap = mImageRef.imageGenerator.generate();

					// 图片缩放,宽高传0,0不缩放
					if (tBitmap != null && mImageRef.width != 0 && mImageRef.height != 0)
					{
						// 长宽相等(icon)中心截取缩放
						if (!mImageRef.mOnlyZoom && mImageRef.width == mImageRef.height)
						{
							bitmap = ThumbnailUtils.extractThumbnail(tBitmap, mImageRef.width, mImageRef.height,
									ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
						}
						else
						{
							// 长款不等,按比例缩放
							bitmap = bitmapZoomBySize(tBitmap, mImageRef.width, mImageRef.height);
						}

					}
					else
					{
						bitmap = tBitmap;
						tBitmap = null;
					}

					if (bitmap != null)
					{
						// save to Disk Cache & memory cache
						// save thumb bitmap
						if (sDiskCacheEnable && mDiskCache != null)
						{
							synchronized (mDiskCache)
							{
								mDiskCache.put(diskCachekey, bitmap);
							}
						}

						if (mImageRef.mImageRoundCorner)
						{
							bitmap = toRoundCorner(bitmap, 12);
							memoryCachekey = memoryCachekey + "round";
						}

						synchronized (mMemoryCache)
						{
							// if (mImageRef.mReleaseTag != null) {
							// mReleaseImage.add(memoryCachekey);
							// memoryCachekey = memoryCachekey +
							// mImageRef.mReleaseTag;
							// }
							if (mMemoryCache.get(memoryCachekey) == null)
							{
								mMemoryCache.put(memoryCachekey, bitmap);
							}
						}
					}

					long duration = System.currentTimeMillis() - startTime;
					mImageRef.lazy = duration > 200;

				}
			}
			catch (OutOfMemoryError e)
			{
				bitmap = null;
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			synchronized (mTaskStack) {
				mTaskStack.remove(this);
			}

			do {
				if(isCancelled())
					break;

				if(bitmap == null)
					break;

				Message message = handler.obtainMessage(0, bitmap);
				handler.sendMessageDelayed(message, mRandom.nextInt(120));

			} while (false);
		}

	}

	public void releaseRes() {
		if(instance != null) {
			instance = null;
		}
	}
	
}
