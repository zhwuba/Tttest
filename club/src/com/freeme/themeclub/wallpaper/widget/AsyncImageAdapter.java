package com.freeme.themeclub.wallpaper.widget;

import com.freeme.themeclub.wallpaper.util.ImageUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public abstract class AsyncImageAdapter<T> extends AsyncAdapter<T> {
	
    public class AsyncLoadImageTask extends AsyncAdapter<T>.AsyncLoadPartialDataTask {
        private boolean mScaled = false;
        private int mTargetHeight = 0;
        private int mTargetWidth = 0;

        @Override
        protected Object doJob(Object key) {
            String imagePath = (String) key;
            
            BitmapFactory.Options boundsOptions = ImageUtils.getBitmapSize(imagePath);
            int width = boundsOptions.outWidth;
            int height = boundsOptions.outHeight;
            if (mTargetWidth <= 0 || mTargetHeight <= 0) {
                Log.i("ResourceBrowser", "AsyncImageAdapter does not set valid parameters for target size.");
                mTargetWidth = width;
                mTargetHeight = height;
            }
            
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            if (useLowQualityDecoding()) {
            	decodeOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            }
            
            Bitmap originalBitmap = null;
            Bitmap scaledBitmap = null;
            try {
	            if (!mScaled) {
	                int minWidth = Math.min(width, mTargetWidth);
	                int minHeight = Math.min(height, mTargetHeight);
	                int x = (width - minWidth) / 2;
	                int y = (height - minHeight) / 2;
	                originalBitmap = BitmapFactory.decodeFile(imagePath, decodeOptions);
	                scaledBitmap = Bitmap.createBitmap(originalBitmap, x, y, minWidth, minHeight);
	            } else {
	            	int scaledWidth = mTargetWidth;
	                int scaledHeight = mTargetHeight;
	                if (mTargetWidth >= width || mTargetHeight >= height) {
	                	scaledWidth = width;
	                	scaledHeight = height;
	                }
	                decodeOptions.inSampleSize = Math.min(width / scaledWidth, height / scaledHeight);
	                originalBitmap = BitmapFactory.decodeFile(imagePath, decodeOptions);
	                scaledBitmap = ImageUtils.scaleBitmapToDesire(originalBitmap, scaledWidth, scaledHeight, false);
	            }
            } catch (OutOfMemoryError e) {
            	e.printStackTrace();
            	scaledBitmap = null;
            } catch (Exception e) {
            	scaledBitmap = null;
            }

            if (scaledBitmap != originalBitmap && originalBitmap != null) {
//            	originalBitmap.recycle();
            }
            return scaledBitmap;
        }

        public void setScaled(boolean scaled) {
            mScaled = scaled;
        }

        public void setTargetSize(int targetWidth, int targetHeight) {
            mTargetWidth = targetWidth;
            mTargetHeight = targetHeight;
        }
    }


    protected boolean useLowQualityDecoding() {
        return false;
    }
}