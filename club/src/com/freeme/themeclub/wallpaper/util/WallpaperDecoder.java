package com.freeme.themeclub.wallpaper.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class WallpaperDecoder extends ImageCacheDecoder {
	private static final String TAG = "decoder";
	
    public WallpaperDecoder() {
        this(3);
    }

    public WallpaperDecoder(int cacheSize) {
        super(cacheSize);
    }

    private int computeSampleSize(String imagePath, int destWidth, int destHeight) {
        BitmapFactory.Options op = ImageUtils.getBitmapSize(new InputStreamLoader(imagePath));
        int sampleSize = Math.min(op.outWidth / destWidth, op.outHeight / destHeight);
        if (sampleSize < 1) {
        	sampleSize = 1;
        }
        
        for (/**/; (4 * (op.outWidth * op.outHeight)) / (sampleSize * sampleSize) > 0xf00000; ++sampleSize)
        	;
        
        return sampleSize;
    }

    private Bitmap decodeOriginBitmapWithNativeMemory(String imageLocalPath, int needWidth, int needHeight) {
        BitmapFactory.Options op = ImageUtils.getDefaultOptions();
        op.inSampleSize = computeSampleSize(imageLocalPath, needWidth, needHeight);
        op.inPurgeable = true;
        op.inInputShareable = true;
        
        Bitmap srcBmp = null;
        FileInputStream is = null;
        for (int decodeTry = 0; decodeTry < 3 && srcBmp == null; ++decodeTry) {
			try {
				is = new FileInputStream(imageLocalPath);
				srcBmp = BitmapFactory.decodeFileDescriptor(is.getFD(), null, op);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (OutOfMemoryError e) {
				Log.i(TAG, (new StringBuilder())
						.append("decode with native memory: OOM, tryCnt =  : ")
						.append(decodeTry).append(imageLocalPath).toString());
				op.inSampleSize += 1;
			} finally {
				if (is != null) {
                    try {
                    	is.close();
                    } catch (Exception e) {
                    }
                    is = null;
				}
			}
        }
        return srcBmp;
    }

    private synchronized Bitmap getDesiredBitmap(Bitmap srcBmp, String imagePath, int useIndex, boolean addIntoCache) {
        Bitmap destBmp = mBitmapCache.removeIdleCache(true, useIndex);
        if (destBmp == null) {
        	int w = mDecodedWidth;
            int h = mDecodedHeight;
        	for (int cnt = 0; cnt < 2; ++cnt) {
        		try {
        			destBmp = Bitmap.createBitmap(w, h, srcBmp.getConfig());
        			
        			break;
        		} catch (OutOfMemoryError e) {
        			w = w * 2 / 3;
        	        h = h * 2 / 3;
        	        
        	        StringBuilder sb = new StringBuilder().append("should not occur OOM:  currentUsing = ")
        	        		.append(useIndex == getCurrentUseBitmapIndex())
        	        		.append("  resize to: (").append(w).append(", ").append(h).append(")");
        	        Log.i(TAG, sb.toString());
        		}
        	}
        }
        if (destBmp != null) {
            ImageUtils.cropBitmapToAnother(srcBmp, destBmp, true);
            if (addIntoCache) {
                mBitmapCache.add(imagePath, destBmp, useIndex);
            }
        }
        return destBmp;
    }

    @Override
    public Bitmap decodeLocalImage(String imagePath, int useIndex, boolean addIntoCache) {
        Bitmap destBmp = getBitmap(imagePath);
        if (destBmp == null) {
            Bitmap srcBmp = decodeOriginBitmapWithNativeMemory(imagePath, mDecodedWidth, mDecodedHeight);
            if (srcBmp != null) {
                if (mBitmapCache.inCacheScope(useIndex)) {
                	destBmp = getDesiredBitmap(srcBmp, imagePath, useIndex, addIntoCache);
                }
//                srcBmp.recycle();
            }
        }
        return destBmp;
    }
}