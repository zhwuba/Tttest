package com.freeme.themeclub.wallpaper.util;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ImageCacheDecoder {
	
    protected class BitmapCache {

        private static final int MAGIC_INDEX = -999;
        
        private Map<String, Bitmap> mCache = null;
        ArrayList<String> mIdList = null;
        ArrayList<Integer> mIndexList = null;
        private int mCacheCapacity = 0;
        private int mCurrentIndex = 0;
        

        public BitmapCache(int cacheCapacity) {
            setCacheSize(Math.max(3, cacheCapacity));
            mCache = Collections.synchronizedMap(new HashMap<String, Bitmap>(mCacheCapacity));
            mIdList = new ArrayList<String>(mCacheCapacity);
            mIndexList = new ArrayList<Integer>(mCacheCapacity);
        }
        
        public void setCacheSize(int newSize) {
            if (newSize > 1 && newSize != mCacheCapacity) {
                if ((newSize & 1) == 0) {
                	newSize++;
                }
                mCacheCapacity = newSize;
            }
        }
        
        public void add(String id, Bitmap b, int useIndex) {
            if (!mCache.containsKey(id)) {
                removeIdleCache(false);
                mCache.put(id, b);
                mIdList.add(id);
                mIndexList.add(useIndex);
            }
        }

        public void clean() {
            for (Iterator<String> i = mIdList.iterator(); i.hasNext(); /**/) {
            	String id = i.next();
            	Bitmap b = mCache.get(id);
            	if (b != null) {
//            		b.recycle();
//            		b = null;
            	}
            }
            mCache.clear();
            mIdList.clear();
            mIndexList.clear();
        }

        public Bitmap getBitmap(String id) {
            return mCache.get(id);
        }

        public int getCurrentUseIndex() {
            return mCurrentIndex;
        }

        public void setCurrentUseIndex(int index) {
            mCurrentIndex = index;
        }

        public boolean inCacheScope(int index) {
            return (Math.abs(index - mCurrentIndex) <= mCacheCapacity / 2);
        }

        public boolean isFull() {
            return (mCache.size() >= mCacheCapacity);
        }

        public Bitmap removeIdleCache(boolean returnBitmap) {
            return removeIdleCache(returnBitmap, MAGIC_INDEX);
        }

        public Bitmap removeIdleCache(boolean returnBitmap, int forIndex) {
            int selectIndex = getNextRemoveCachePosition(forIndex);
            Bitmap ret = null;
            if (0 <= selectIndex && selectIndex < mIndexList.size()) {
                Bitmap b = mCache.remove(mIdList.get(selectIndex));
                if (b != null) {
                    if (returnBitmap) {
                    	ret = b;
                    } else {
//                        b.recycle();
                    }
                }
                mIdList.remove(selectIndex);
                mIndexList.remove(selectIndex);
            }
            return ret;
        }
        
        private int getNextRemoveCachePosition(int forIndex) {
            int selectIndex = mIndexList.indexOf(forIndex);
            if (selectIndex < 0 && (forIndex == mCurrentIndex || isFull())) {
            	selectIndex = mIndexList.isEmpty() ? -1 : 0;
                for (int i = 1; i < mIndexList.size(); i++) {
                    if (Math.abs(mCurrentIndex - mIndexList.get(i)) 
                    		> Math.abs(mCurrentIndex - mIndexList.get(selectIndex))) {
                    	selectIndex = i;
                    }
                }
            }
            return selectIndex;
        }
    }

    public static interface ImageDecodingListener {
        void handleDecodingResult(boolean result, String localPath, String onlinePath);
    }

    private class JobRun implements Runnable {

        private String imageLocalPath = null;
        private String imageOnlinePath = null;
        private int useIndex = 0;
        
        public JobRun(String imageLocalPath, String imageOnlinePath, int useIndex) {
            this.imageLocalPath = imageLocalPath;
            this.imageOnlinePath = imageOnlinePath;
            this.useIndex = useIndex;
        }
        
        public void dispatchJob() {
        	mDecodeExecutorService.submit(this);
        }

        public void run() {
            boolean result;
            if (mBitmapCache.inCacheScope(useIndex)) {
                if (decodeLocalImage(imageLocalPath, useIndex, true) != null) {
                	result = true;
                } else {
                	result = false;
                }
            } else {
            	result = false;
            }
            mDoingJob.remove(imageLocalPath);
            
            if (mBitmapCache.inCacheScope(useIndex)) {
            	Message msg = mHandler.obtainMessage();
            	msg.what = MSG_ASYNC_DECODING_FINISH;
            	msg.arg1 = result ? SUCCESS : FAILED;
            	msg.obj = new Pair<String, String>(imageLocalPath, imageOnlinePath);
                mHandler.sendMessage(msg);
            }
        }
    }


    private static final int MSG_ASYNC_DECODING_FINISH = 0;
    @SuppressWarnings("unused")
	private static final int MSG_ASYNC_DOWNLOAD_FINISH = 1;
    
    private static final int SUCCESS = 0;
    private static final int FAILED = 1;
    
    protected BitmapCache mBitmapCache = null;
    private ExecutorService mDecodeExecutorService = null;
    protected int mDecodedHeight = 0;
    protected int mDecodedWidth = 0;
    private HashSet<String> mDoingJob = null;
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unchecked")
        @Override
		public void handleMessage(Message msg) {
			Pair<String, String> files = (Pair<String, String>) msg.obj;
            final boolean result = (msg.arg1 == SUCCESS);
            switch (msg.what) {
	            case MSG_ASYNC_DECODING_FINISH: {
	            	if (mListener != null) {
	                    mListener.handleDecodingResult(result, files.first, files.second);
	            	}
	            	break;
	            }
	            default: break;
            }
        }
    };
    private ImageDecodingListener mListener = null;

    
    public ImageCacheDecoder() {
        this(3);
    }

    public ImageCacheDecoder(int cacheSize) {
        mDoingJob = new HashSet<String>();
        mBitmapCache = new BitmapCache(cacheSize);
        mDecodeExecutorService = Executors.newFixedThreadPool(2);
        mListener = null;
    }

    public void clean(boolean stopBgThread) {
        mBitmapCache.clean();
        if (stopBgThread) {
            mDecodeExecutorService.shutdown();
            
            mHandler.removeMessages(MSG_ASYNC_DECODING_FINISH);
        }
    }

    public void decodeImageAsync(String imageLocalPath, String imageOnlinePath, int useIndex) {
        if (!TextUtils.isEmpty(imageLocalPath) && mBitmapCache.getBitmap(imageLocalPath) == null 
        		&& !mDoingJob.contains(imageLocalPath) && !mDecodeExecutorService.isShutdown()) {
        	
            mDoingJob.add(imageLocalPath);
            (new JobRun(imageLocalPath, imageOnlinePath, useIndex)).dispatchJob();
        }
    }

    public Bitmap decodeLocalImage(String imagePath, int useIndex, boolean addIntoCache) {
        Bitmap b = getBitmap(imagePath);
        if (b == null) {
            b = ImageUtils.getBitmap(new InputStreamLoader(imagePath), 
            		mDecodedWidth, mDecodedHeight, mBitmapCache.removeIdleCache(true));
            if (b != null && addIntoCache) {
                mBitmapCache.add(imagePath, b, useIndex);
            }
        }
        return b;
    }

    public Bitmap getBitmap(String imagePath) {
        return mBitmapCache.getBitmap(imagePath);
    }

    public int getCurrentUseBitmapIndex() {
        return mBitmapCache.getCurrentUseIndex();
    }

    public void regeisterListener(ImageDecodingListener l) {
        mListener = l;
    }

    public void setCacheCapacity(int cacheCapacity) {
        mBitmapCache.setCacheSize(cacheCapacity);
    }

    public void setCurrentUseBitmapIndex(int index) {
        mBitmapCache.setCurrentUseIndex(index);
    }

    public void setScaledSize(int scaledWidth, int scaledHeight) {
        mDecodedWidth = scaledWidth;
        mDecodedHeight = scaledHeight;
    }
}