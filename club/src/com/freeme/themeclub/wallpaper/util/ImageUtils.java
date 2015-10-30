package com.freeme.themeclub.wallpaper.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

//import libcore.io.Streams;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//TESTYZY

public class ImageUtils {

    public static int computeSampleSize(InputStreamLoader streamLoader, int pixelSize) {
        int roundedSize = 1;
        if (pixelSize > 0) {
            BitmapFactory.Options options = getBitmapSize(streamLoader);
            for (double size = Math.sqrt(options.outWidth * options.outHeight / pixelSize); 
            		(roundedSize << 1) <= size; roundedSize <<= 1)
            	;
        }
        return roundedSize;
    }

    public static boolean cropBitmapToAnother(Bitmap srcBmp, Bitmap destBmp, boolean recycleSrcBmp) {
        if (srcBmp != null && destBmp != null) {
            int srcWidth = srcBmp.getWidth();
            int srcHeight = srcBmp.getHeight();
            int destWidth = destBmp.getWidth();
            int destHeight = destBmp.getHeight();
            float ratio = Math.max((float) destWidth / srcWidth, (float) destHeight / srcHeight);
            
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            paint.setAntiAlias(true);
            paint.setDither(true);
            
            Canvas canvas = new Canvas(destBmp);
            canvas.translate(((float) destWidth - ratio * srcWidth) / 2, 
            		((float) destHeight - ratio * srcHeight) / 2);
            canvas.scale(ratio, ratio);
            canvas.drawBitmap(srcBmp, 0, 0, paint);
            
            if (recycleSrcBmp) {
//            	srcBmp.recycle();
            }
            
            return true;
        }
        return false;
    }

    public static final Bitmap getBitmap(InputStreamLoader streamLoader, int pixelSize) {
        BitmapFactory.Options options = getDefaultOptions();
        options.inSampleSize = computeSampleSize(streamLoader, pixelSize);
        try {
	        for (int retry = 0; retry < 3; ++retry) {
	        	try {
	        		Bitmap destBimtmap = BitmapFactory.decodeStream(streamLoader.get(), null, options);
	        		
	        		return destBimtmap;
	        	} catch (OutOfMemoryError error) {
	        		System.gc();
	        		options.inSampleSize *= 2;
	        		continue;
	        	}
	        }
        } catch (Exception ex) {
        } finally {
        	streamLoader.close();
        }
        return null;
    }
    
    private static final int PIXEL_FACTOR_FOR_COMPUTING_SAMPLE_SIZE = 2;
    public static Bitmap getBitmap(InputStreamLoader streamLoader, int destWidth, int destHeight) {
        int pixelSize = (destWidth * destHeight) * PIXEL_FACTOR_FOR_COMPUTING_SAMPLE_SIZE;
        if (destWidth <= 0 || destHeight <= 0) {
        	pixelSize = -1;
        }
        
        Bitmap destBmp = getBitmap(streamLoader, pixelSize);
        if (pixelSize > 0) {
        	destBmp = scaleBitmapToDesire(destBmp, destWidth, destHeight, true);
        }
        return destBmp;
    }

    public static Bitmap getBitmap(InputStreamLoader streamLoader, int destWidth, int destHeight, Bitmap reusedBitmap) {
        Bitmap srcBitmap = null;
        if (reusedBitmap != null && !reusedBitmap.isRecycled()) {
            BitmapFactory.Options sizeOp = getBitmapSize(streamLoader);
            if (sizeOp.outWidth == reusedBitmap.getWidth()
            		&& sizeOp.outHeight == reusedBitmap.getHeight()) {
                BitmapFactory.Options op = getDefaultOptions();
                op.inBitmap = reusedBitmap;
                op.inSampleSize = 1;
                srcBitmap = BitmapFactory.decodeStream(streamLoader.get(), null, op);

                streamLoader.close();
            }
            if (srcBitmap == null) {
//            	reusedBitmap.recycle();
            }
        }
        Bitmap destBitmap = srcBitmap;
        if (destBitmap != null) {
            if (destWidth > 0 && destHeight > 0) {
            	destBitmap = scaleBitmapToDesire(destBitmap, destWidth, destHeight, true);
            }
        } else {
        	destBitmap = getBitmap(streamLoader, destWidth, destHeight);
        }
        return destBitmap;
    }

    public static final BitmapFactory.Options getBitmapSize(String filePath) {
        return getBitmapSize(new InputStreamLoader(filePath));
    }

    public static final BitmapFactory.Options getBitmapSize(InputStreamLoader streamLoader) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(streamLoader.get(), null, options);
        } catch (Exception e) {
        } finally {
        	streamLoader.close();
        }
        return options;
    }

    public static BitmapFactory.Options getDefaultOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inJustDecodeBounds = false;
        options.inSampleSize = 1;
        options.inScaled = false;
        /*/
        options.inPostProc = true;
        options.inPostProcFlag = 1;
        //*/
        return options;
    }

    public static boolean saveBitmapToLocal(InputStreamLoader streamLoader, String path, int destWidth, int destHeight) {
        if (streamLoader == null || path == null || destWidth < 1 || destHeight < 1) {
        	return false;
        }
        
        boolean result = false;
        BitmapFactory.Options options = getBitmapSize(streamLoader);
        final int srcWidth = options.outWidth;
        final int srcHeight = options.outHeight;
        if (srcWidth > 0 && srcHeight > 0) {
            if (srcWidth == destWidth && srcHeight == destHeight) {
            	result = saveToFile(streamLoader, path);
            } else {
                Bitmap destBmp = getBitmap(streamLoader, destWidth, destHeight);
                if (destBmp != null) {
                	result = saveToFile(destBmp, path);
//                	destBmp.recycle();
                }
            }
        }
        return result;
    }

    public static boolean saveToFile(Bitmap bitmap, String path) {
        if(bitmap == null) {
        	return false;
        }

        FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(path);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
		} catch (FileNotFoundException e) {
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
        return true;
    }

    private static boolean saveToFile(InputStreamLoader streamLoader, String path) {
        FileOutputStream outputStream = null;
        boolean result;
        try {
	        outputStream = new FileOutputStream(path);
	        //Streams.copy(streamLoader.get(), outputStream);
	        
	        result = true;
        } catch (Exception e) {
        	result = false;
        } finally {
        	if (outputStream != null) {
        		try {
        			outputStream.close();
        		} catch (IOException e) {
        		}
        	}
        	if (streamLoader != null) {
        		streamLoader.close();
        	}
        }
        return result;
    }

    public static Bitmap scaleBitmapToDesire(Bitmap srcBmp, int destWidth, int destHeight, boolean recycleSrcBmp) {
        Bitmap destBmp = null;
        try {
            int srcWidth = srcBmp.getWidth();
            int srcHeight = srcBmp.getHeight();
            if (srcWidth == destWidth && srcHeight == destHeight) {
            	return srcBmp;
            }
            
            Bitmap.Config config = srcBmp.getConfig();
            if (config == null) {
            	config = Bitmap.Config.ARGB_8888;
            }
            destBmp = Bitmap.createBitmap(destWidth, destHeight, config);
            cropBitmapToAnother(srcBmp, destBmp, recycleSrcBmp);
        } catch (Exception e1) { 
        } catch (OutOfMemoryError e2) {
        }
        return destBmp;
    }
}
