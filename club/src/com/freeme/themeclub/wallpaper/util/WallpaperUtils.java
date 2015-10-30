package com.freeme.themeclub.wallpaper.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.base.ResourceConstants;

public class WallpaperUtils {
	
	private static final boolean __TOO_SMALL_TO_CROP = false;
	
	public static final String WALLPAPER_PATH = "/data/system/users/0/wallpaper";
	public static final String LOCK_WALLPAPER_PATH = "/data/system/users/all/lockwallpaper";
	
	public static final int REQUEST_CODE_IMAGE_DETAIL = 1;
	public static final int REQUEST_CODE_PICK_IMAGE = 4;
	public static final int REQUEST_CODE_CROP_APPL_LOCKSCREEN = 28673;
	public static final int REQUEST_CODE_CROP_APPL_DESKTOP = 28674;
	
	public static final int WALLPAPER_SCREEN_SPAN = 2;
	public static final String ACTION_CROP = "com.android.camera.action.CROP";
    public static final String KEY_ASPECT_X = "aspectX";
    public static final String KEY_ASPECT_Y = "aspectY";
    public static final String KEY_SPOTLIGHT_X = "spotlightX";
    public static final String KEY_SPOTLIGHT_Y = "spotlightY";
    public static final String KEY_OUTPUT_X = "outputX";
    public static final String KEY_OUTPUT_Y = "outputY";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    public static final String KEY_OUTPUT_FORMAT = "outputFormat";
    public static final String KEY_IS_INIT_FULL_SELECTION= "isInitFullSelection";
    public static final String KEY_IS_LARGE_IMAGE = "is-large-image";
    public static final String KEY_NO_FACE_DETECTION = "noFaceDetection";
    public static final String KEY_SET_AS_WALLPAPER = "set-as-wallpaper";
    public static final String KEY_SET_AS_LOCKSCREEN = "set-as-lockscreen";
    public static final String IMAGE_TYPE = "image/*";
	
	private static Uri sLastCropWallpaperUri = null;
	
	public static void cropAndApplyWallpaper(Activity activity,
			int resourceType, String filepath, 
			boolean saveDirectly, boolean forceCroping) {
		cropAndApplyWallpaper(activity, null, resourceType,
				Uri.parse("file://" + filepath), saveDirectly, forceCroping);
	}
    
    public static boolean cropAndApplyWallpaper(Activity activity, Fragment frag, 
    								int resourceType, Uri wallpaperUri, 
    								boolean saveDirectly, boolean forceCroping) {
        if (!saveDirectly) {
            final boolean isLockscreen = 
            		(resourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER);
            
            Pair<Integer, Integer> size = getWallpaperExpectedSize(activity, isLockscreen);
            final int wallpaperWidth = size.first;
            final int wallpaperHeight = size.second;
            
            boolean needCrop = forceCroping;
            if (!needCrop && __TOO_SMALL_TO_CROP) {
            	int maxNeedWidth = (int) (1.1f * wallpaperWidth);
            	int maxNeedHeight = (int) (1.1f * wallpaperHeight);
                InputStreamLoader is = new InputStreamLoader(activity, wallpaperUri);
                BitmapFactory.Options options = ImageUtils.getBitmapSize(is);
                is.close();
                needCrop = (options.outWidth > maxNeedWidth || options.outHeight > maxNeedHeight);
            }
            if (needCrop) {
            	Intent intent = new Intent(ACTION_CROP)
                		//.setClassName("com.miui.gallery", "com.miui.gallery.app.CropImage")
                		.setDataAndType(wallpaperUri, IMAGE_TYPE)
                		.putExtra(KEY_OUTPUT_X, wallpaperWidth)
                		.putExtra(KEY_OUTPUT_Y, wallpaperHeight)
                		.putExtra(KEY_ASPECT_X, wallpaperWidth)
                		.putExtra(KEY_ASPECT_Y, wallpaperHeight)
                		.putExtra(KEY_SCALE, true)
                		.putExtra(KEY_IS_INIT_FULL_SELECTION, true)
                		.putExtra(KEY_NO_FACE_DETECTION, true)
                		.putExtra(KEY_IS_LARGE_IMAGE, true)
						.putExtra(isLockscreen ? KEY_SET_AS_LOCKSCREEN
										: KEY_SET_AS_WALLPAPER, true);
				final int requestCode = isLockscreen ? REQUEST_CODE_CROP_APPL_LOCKSCREEN
						: REQUEST_CODE_CROP_APPL_DESKTOP;
                if (frag == null) {
                    activity.startActivityForResult(intent, requestCode);
                } else {
                	frag.startActivityForResult(intent, requestCode);
                }
                sLastCropWallpaperUri = wallpaperUri;
                return false;
            }
        }
        
        boolean changeLock = true;
        boolean changedDesk = true;
        if ((resourceType & ResourceConstants.R_LOCKSCREEN_WALLPAER) != 0) {
            changeLock = saveLockWallpaperByDisplay(activity, null, wallpaperUri);
        }
        if ((resourceType & ResourceConstants.R_DESKTOP_WALLPAER) != 0) {
            changedDesk = saveDeskWallpaperByDisplay(activity, null, wallpaperUri);
        }
        if (!changeLock) {
            ResourceHelper.showThemeChangedToast(activity, false, 
            		activity.getString(ResourceHelper.getTitleIdByFlag(ResourceConstants.R_LOCKSCREEN_WALLPAER)));
        }
        if (!changedDesk) {
        	ResourceHelper.showThemeChangedToast(activity, false, 
        			activity.getString(ResourceHelper.getTitleIdByFlag(ResourceConstants.R_DESKTOP_WALLPAER)));
        }
        if (changeLock && changedDesk) {
        	ResourceHelper.showThemeChangedToast(activity, true);
        }
        return (changedDesk && changeLock);
    }

    public static void dealCropWallpaperResult(Context context, int requestCode, int resultCode) {
        if (resultCode == Activity.RESULT_OK 
        		&& (requestCode == REQUEST_CODE_CROP_APPL_DESKTOP || requestCode == REQUEST_CODE_CROP_APPL_LOCKSCREEN)) {
            final int resourceType = (requestCode == REQUEST_CODE_CROP_APPL_DESKTOP)
			        ? ResourceConstants.R_DESKTOP_WALLPAER
			        : ResourceConstants.R_LOCKSCREEN_WALLPAER;
            String path = (sLastCropWallpaperUri != null) 
            		? sLastCropWallpaperUri.getPath()
                	: "";
            ResourceHelper.saveUserPreferenceOnlyPath(context, resourceType, path);
            ResourceHelper.showThemeChangedToast(context, true);
        }
    }

    public  static Pair<Integer, Integer> getWallpaperExpectedSize(
    			Context context, boolean isLockscreen) {
        DisplayMetrics dm = getScreenDisplayMetrics(context);
        int scrWidth = dm.widthPixels;
        if (!isLockscreen) {
        	scrWidth *= WALLPAPER_SCREEN_SPAN;
        }
        int scrHeight = dm.heightPixels;
        return new Pair<Integer, Integer>(scrWidth, scrHeight);
    }

    public static void setSuggestDesiredDimensions(WallpaperManager ws, int width, int height) {
        ws.suggestDesiredDimensions(width, height);
    } 
    
    /**
     * TODO: Size as Screen.
     * @param context
     * @param candidateBmp
     * @param candidateUri
     * @param flat
     * @return
     */
    public static boolean saveDeskWallpaperByDefaultDisplay(Context context, 
    		Bitmap candidateBmp, Uri candidateUri, boolean flat) {
        return saveWallpaperByDisplay(context, candidateBmp, candidateUri, false, flat);
    }
    
    public static boolean saveDeskWallpaperByDisplay(Context context, 
    		Bitmap candidateBmp, Uri candidateUri) {
        return saveWallpaperByDisplay(context, candidateBmp, candidateUri, false, false);
    }

    public static boolean saveLockWallpaperByDisplay(Context context, 
    		Bitmap candidateBmp, Uri candidateUri) {
        return saveWallpaperByDisplay(context, candidateBmp, candidateUri, true, /*ignore*/false);
    }

    private static boolean saveWallpaperByDisplay(Context context, 
    		Bitmap candidateBmp, Uri candidateUri, boolean isLockscreen, boolean flat) {
        boolean result = false;
        
        final Pair<Integer, Integer> size = getWallpaperExpectedSize(context, isLockscreen || flat);
        final int needWidth = size.first;
        final int needHeight = size.second;
        
        //final WallpaperManager ws = isLockscreen ? null : WallpaperManager.getInstance(context);
        final WallpaperManager ws = WallpaperManager.getInstance(context);
        /*/ start
        sendWallpaperBroadcast(context, isLockscreen, false);
        //*/
        
        if (candidateBmp != null) {
        	Bitmap tmpBmp = null;
        	
            final int cbWidth = candidateBmp.getWidth();
            final int cbHeight = candidateBmp.getHeight();
            if (cbWidth >= needWidth && cbHeight >= needHeight) {
            	tmpBmp = ImageUtils.scaleBitmapToDesire(candidateBmp, needWidth, needHeight, false);
            }
            if (tmpBmp == null) {
            	tmpBmp = candidateBmp;
            }
            //if (isLockscreen) {
            //	result = ImageUtils.saveToFile(tmpBmp, LOCK_WALLPAPER_PATH);
            //} else {
            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
            	tmpBmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
                try {
                    if(isLockscreen){
                        ws.setLockscreenStream(new ByteArrayInputStream(bos.toByteArray()));
                    }else {
                        setSuggestDesiredDimensions(ws, needWidth, needHeight);
                        ws.setStream(new ByteArrayInputStream(bos.toByteArray()));
                    }
					result = true;
				} catch (IOException e) {
					result = false;
				} catch (OutOfMemoryError e) {
					System.gc();
					result = false;
				} finally {
					if (bos != null) {
						try {
							bos.close();
						} catch (IOException e) {
						}
					}
				}
                // result
            //}
            
            if (tmpBmp != null && tmpBmp != candidateBmp) {
//            	tmpBmp.recycle();
            }
        	
        } else if (candidateUri != null) {
            if (isLockscreen) {
            	result = ImageUtils.saveBitmapToLocal(new InputStreamLoader(context, candidateUri), 
            	        LOCK_WALLPAPER_PATH, needWidth, needHeight);
            } else {
            	File tmpDeskWallpaperFile = new File(context.getFilesDir(), "tmp_desk_wallpaper");
                ImageUtils.saveBitmapToLocal(new InputStreamLoader(context, candidateUri), 
                		tmpDeskWallpaperFile.getAbsolutePath(), needWidth, needHeight);
                if (tmpDeskWallpaperFile.exists()) {
                	FileInputStream tmpIs = null;
					try {
						tmpIs = new FileInputStream(tmpDeskWallpaperFile);
                        setSuggestDesiredDimensions(ws, needWidth, needHeight);
						ws.setStream(tmpIs);
					} catch (FileNotFoundException e) {
					} catch (IOException e) {
					} finally {
						if (tmpIs != null) {
							try {
								tmpIs.close();
							} catch (IOException e) {
							}
						}
					}
                    result = true;
                    
                    tmpDeskWallpaperFile.delete();
                }
            }
        }
        
        /*/ end
        sendWallpaperBroadcast(context, isLockscreen, true);
        //*/
        /*
        if (result) {
            if (isLockscreen) {
            	ResourceHelper.updateLockWallpaperInfo(context, candidateUri.getPath());
            } else {
				ResourceHelper.saveUserPreferenceOnlyPath(context,
						ResourceConstants.R_DESKTOP_WALLPAER,
						candidateUri.getPath());
            }
        }*/
        return result;
    }
    
    public static DisplayMetrics getScreenDisplayMetrics(Context context) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        if (dm.widthPixels == 0 || dm.heightPixels == 0) {
        	dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        }
        return dm;
    }
    
    /*/
	private static final String ACTION_NORMAL_WALLPAPER_SET = "com.tydtech.action.WALLPAPER_SET";
	private static final String ACTION_LOCKSCREEN_WALLPAPER_SET = "com.tydtech.action.LOCKSCREEN_WALLPAPER_SET";
	private static final String ACTION_EXTRA_COMPLETE = "com.tydtech.extra.COMPLETE";
    public static void sendWallpaperBroadcast(Context context, boolean isLockscreen, boolean isComplete) {
		Intent intent = new Intent()
			.setAction(isLockscreen ? ACTION_LOCKSCREEN_WALLPAPER_SET
						: ACTION_NORMAL_WALLPAPER_SET)
			.putExtra(ACTION_EXTRA_COMPLETE, isComplete);
		context.sendBroadcast(intent);
    }
    //*/
    
    
    public static String CACHED_THUMB_VIDEOWALLPAPER = "cached_thumb_videowall";
}
