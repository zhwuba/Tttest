package com.freeme.themeclub.wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Pair;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.http.entity.InputStreamEntity;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.base.ResourceConstants;
import com.freeme.themeclub.wallpaper.cache.FolderCache;
import com.freeme.themeclub.wallpaper.os.ExtraFileUtils;
import com.freeme.themeclub.wallpaper.util.LogUtils;
import com.freeme.themeclub.wallpaper.util.WallpaperUtils;

public class ResourceHelper {

	private static final String TAG = "ResourceHelper";
	
	public static Bundle buildDefaultMetaData(Bundle metaData, String action, Context context) {
        final Intent intent = ((Activity) context).getIntent();
        
        // resource flag
        int resourceFlag = ResourceConstants.R_NONE;
    	if (IntentConstants.ACTION_SET_WALLPAPER.equals(action)) {
    		resourceFlag = ResourceConstants.R_DESKTOP_WALLPAER;
		} else if (IntentConstants.ACTION_SET_LOCKSCREEN_WALLPAPER.equals(action)) {
			resourceFlag = ResourceConstants.R_LOCKSCREEN_WALLPAER;
        } else {
        	resourceFlag = metaData.getInt(IntentConstants.EXTRA_RESOURCE_FLAG, ResourceConstants.R_NONE);
        }
        metaData.putInt(IntentConstants.EXTRA_RESOURCE_FLAG, resourceFlag);
        
        // source folders
        ArrayList<String> sourceFolders = new ArrayList<String>();
        switch (resourceFlag) {
	        case ResourceConstants.R_DESKTOP_WALLPAER: {
	        	sourceFolders.add(ResourceConstants.SYSTEM_WALLPAPER_FOLDER);
	        	break;
	        }
	        case ResourceConstants.R_LOCKSCREEN_WALLPAER: {
	        	sourceFolders.add(ResourceConstants.SYSTEM_LOCKSCREEN_FOLDER);
	        	break;
	        }
        }
        sourceFolders.add(ResourceConstants.DOWNLOAD_WALLPAPER_FOLDER);
        metaData.putStringArray(IntentConstants.EXTRA_SOURCE_FOLDERS, sourceFolders.toArray(new String[0]));
        
        // detail activity
		metaData.putString(IntentConstants.EXTRA_DETAIL_ACTIVITY_PACKAGE,
				context.getPackageName());
		if (resourceFlag == ResourceConstants.R_DESKTOP_WALLPAER
				|| resourceFlag == ResourceConstants.R_LOCKSCREEN_WALLPAER) {
			metaData.putString(IntentConstants.EXTRA_DETAIL_ACTIVITY_CLASS,
					WallpaperDetailActivity.class.getName());
		}
        
        // others
        metaData.putString(IntentConstants.EXTRA_RESOURCE_SET_NAME, 
        		context.getString(getTitleIdByFlag(resourceFlag)));
        
        // +++
        // add display extra flags
        int displayExtra = ResourceConstants.DISPLAY_EXTRA_DEFAULT;
        if (new File(ResourceConstants.SYSTEM_WALLPAPER_FOLDER, ".flat").exists()) {
        	displayExtra = ResourceConstants.DISPLAY_EXTRA_MAIN_FLAT;
        }
        // ---
        metaData.putInt(IntentConstants.EXTRA_DISPLAY_TYPE, getDisplayType(resourceFlag, displayExtra));

        if (metaData.getString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE) == null) {
        	metaData.putString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE, context.getPackageName());
        }
        
        if (metaData.getString(IntentConstants.EXTRA_CACHE_LIST_FOLDER) == null) {
        	String cacheFolder = context.getFilesDir().getAbsolutePath();
            metaData.putString(IntentConstants.EXTRA_CACHE_LIST_FOLDER, cacheFolder);
        }
        
        return metaData;
    }
	
	public static final int getTitleIdByFlag(int flag) {
		int resId = -1;
		switch (flag) {
			case ResourceConstants.R_DESKTOP_WALLPAER: 
				resId = R.string.theme_component_title_wallpaper; 
				break;
			case ResourceConstants.R_LOCKSCREEN_WALLPAER: 
				resId = R.string.theme_component_title_lockwallpaper; 
				break;
			default: break;
		}
		return resId;
	}
	
	public static boolean isImageResource(int flag) {
	    return flag == ResourceConstants.R_DESKTOP_WALLPAER 
	    		|| flag == ResourceConstants.R_LOCKSCREEN_WALLPAER;
	}
	
	public static boolean isMultipleView(int displayType) {
        return false;
    }
	
	public static Pair<Integer, Integer> getThumbnailSize(Activity activity, int displayType, int horizontalPadding) {
        int heightVsWidthRatioId = -1;
        int itemCntPerLine = 0;
        switch (displayType) {
	        case ResourceConstants.DISPLAY_DOUBLE_FLAT:
	        	heightVsWidthRatioId = R.fraction.resource_thumbnail_double_flat_icon_height_vs_width_ratio;
	        	itemCntPerLine = 2;
	        	break;
	        case ResourceConstants.DISPLAY_TRIPLE:
	        	heightVsWidthRatioId = R.fraction.resource_thumbnail_triple_height_vs_width_ratio;
	        	itemCntPerLine = 3;
	        	break;
	        default: break;
        }
        
        int width;
        int height;
        if (heightVsWidthRatioId > 0) {
            Point p = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(p);
            int screenWidth = p.x;
            int itemGap = getThumbnailGap(activity);
            width = (screenWidth - horizontalPadding - itemGap * (itemCntPerLine - 1)) / itemCntPerLine;
            height = (int) activity.getResources().getFraction(heightVsWidthRatioId, width, width);
        } else if (heightVsWidthRatioId == -1) {
        	width = -1;
        	height = -1;
        } else {
        	width = -2;
        	height = -2;
        }
        return new Pair<Integer, Integer>(width, height);
    }
	
	public static int getThumbnailGap(Context context) {
	    return context.getResources().getDimensionPixelSize(R.dimen.resource_thumbnail_gap);
	}
	
	public static int getDataPerLine(int displayType) {
		switch (displayType) {
			case ResourceConstants.DISPLAY_DOUBLE_FLAT: 
				return 2;
			case ResourceConstants.DISPLAY_TRIPLE: 
				return 3;
			
			default: return 1;
		}
	}
	
	/**
	 * TODO: 
	 * @param flag
	 * @param disp_extra
	 * @return
	 */
	public static int getDisplayType(int flag, int disp_extra) {
		int final_flag = flag;
		switch (flag) {
			case ResourceConstants.R_DESKTOP_WALLPAER: 
				switch (disp_extra) {
					case ResourceConstants.DISPLAY_EXTRA_MAIN_FLAT: 
						final_flag = ResourceConstants.R_LOCKSCREEN_WALLPAER;
						break;
				}
				break;
		}
		return getDisplayType(/*final_flag*/ResourceConstants.R_DESKTOP_WALLPAER);
	}
	
	public static int getDisplayType(int flag) {
		int displayType;
		switch (flag) {
			case ResourceConstants.R_DESKTOP_WALLPAER: 
				displayType = ResourceConstants.DISPLAY_DOUBLE_FLAT; 
				break;
			case ResourceConstants.R_LOCKSCREEN_WALLPAER: 
				displayType = ResourceConstants.DISPLAY_TRIPLE; 
				break;
				
			default:
				displayType = ResourceConstants.DISPLAY_SINGLE;
				break;
		}
        return displayType;
    }
	
	public static int getThumbnailViewResource(int displayType) {
        switch (displayType) {
	        case ResourceConstants.DISPLAY_DOUBLE_FLAT: 
	        	return R.layout.resource_item_vertical_flat;
	        case ResourceConstants.DISPLAY_TRIPLE: 
	        	return R.layout.resource_item_vertical;
	        	
	        default: return 0;
        }
    }
	
	public static void showThemeChangedToast(Context context, boolean result) {
        showThemeChangedToast(context, result, "");
    }

    public static void showThemeChangedToast(Context context, boolean result, String name) {
        final int resid = result 
        		? R.string.theme_changed_message
        		: R.string.theme_changed_failed_message;
        
        Toast.makeText(context, 
        		context.getString(resid, new Object[] { name }), 
        		Toast.LENGTH_LONG).show();
    }
    
    public static boolean isSystemResource(String path) {
        return (path != null && path.startsWith("/system"));
    }
    
    public static boolean isDataResource(String path) {
        return (path != null && path.startsWith("/data"));
    }
    
    public static String getFormattedSize(long size) {
        String result;
        if (size < 1024 * 1024) {
        	result = String.format("%.0fK", new Object[] { Double.valueOf((double) size / 1024) });
        } else {
        	result = String.format("%.1fM", new Object[] { Double.valueOf((double) size / (1024 * 1024)) });
        }
        return result;
    }
    
    public static String getDateFormatByFormatSetting(Context context, long millis) {
    	StringBuffer sb = new StringBuffer(DateFormat.getDateFormat(context).format(Long.valueOf(millis)));
    	if (DateFormat.is24HourFormat(context)) {
    		sb.append(' ').append(DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_CAP_AMPM | DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME));
    	} else {
    		sb.append(' ').append(DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_CAP_AMPM | DateUtils.FORMAT_12HOUR | DateUtils.FORMAT_SHOW_TIME));
    	}
    	return sb.toString();
    }
    
    public static final String formatTimeString(long ms) {
        final long secends = ms / 1000L;
        
        final long secs = secends % 60L;
        final long mins = (secends / 60L) % 60L;
        final long hours = secends / 3600L;
        
        Object[] args = new Object[] {
        		hours,
        		mins,
        		secs
        };
        return String.format("%02d:%02d:%02d", args);
    }
    
	public static void updateLockWallpaperInfo(Context context,
			String srcImagePath) {
		FileUtils.setPermissions(WallpaperUtils.LOCK_WALLPAPER_PATH, 0775, -1, -1);
		saveUserPreferenceOnlyPath(context,
				ResourceConstants.R_LOCKSCREEN_WALLPAER, srcImagePath, true);
	}
    
    public static void saveUserPreferenceOnlyPath(Context context, int flag, String path) {
        saveUserPreferenceOnlyPath(context, flag, path, true);
    }

    public static void saveUserPreferenceOnlyPath(Context context, int flag, String path, boolean updateTitle) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        synchronized (pref) {
	        SharedPreferences.Editor edit = pref.edit();
	        String title = null;
	        if (!updateTitle) {
	        	title = pref.getString("title-" + flag, null);
	        }
	        if (title == null) {
	        	title = context.getString(R.string.theme_description_title_customized);
	        }
	        
	        clearUserPreference(edit, flag);
	        
	        edit.putString("path-" + flag, path);
	        edit.putString("title-" + flag, title);
	        edit.putLong("update_time-" + flag, System.currentTimeMillis());
	        edit.commit();
	        
			LogUtils.d(TAG, new StringBuilder()
								.append("saveUserPreferenceOnlyPath flag = ")
								.append(flag).append(" path = ").append(path)
								.toString());
        }
    }
    
    public static void clearUserPreference(SharedPreferences.Editor edit, int flag) {
    	edit.remove("path-" + flag);
    	edit.remove("flags-" + flag);
    	edit.remove("title-" + flag);
    	edit.remove("designer-" + flag);
    	edit.remove("author-" + flag);
    	edit.remove("update_time-" + flag);
    }
    
    public static String computeCurrentUsingPath(Context context, Bundle metaData, int resourceType) {
        String filename = PreferenceManager.getDefaultSharedPreferences(context)
        		.getString("path-" + resourceType, null);
        if (filename == null) {
        	return null;
        }
        
        if (isImageResource(resourceType)) {
			if (resourceType == ResourceConstants.R_DESKTOP_WALLPAER
					&& WallpaperManager.getInstance(context).getWallpaperInfo() != null) {
				filename = null;
			} else if (ResourceHelper.wallpaperPrefOlderThanSystem(context,
					resourceType)) {
				filename = null;
			}
        }
        return filename;
    }
    
    public static boolean wallpaperPrefOlderThanSystem(Context context, int resourceType) {
        File file = null;
        if (resourceType == ResourceConstants.R_DESKTOP_WALLPAER) {
            file = new File(WallpaperUtils.WALLPAPER_PATH);
        } else if (resourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER) {
        	file = new File(WallpaperUtils.LOCK_WALLPAPER_PATH);
        }
        return (file != null && (!file.exists() || 
        			PreferenceManager.getDefaultSharedPreferences(context)
        		.getLong("update_time-" + resourceType, 0) < file.lastModified()));
    }
    
    public static void writeTo(InputStream is, String filename) {
        ExtraFileUtils.mkdirs((new File(filename)).getParentFile(), 0775, -1, -1);
        
        InputStreamEntity inputEntity = new InputStreamEntity(new BufferedInputStream(is), -1L);
        BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(filename));
			FileUtils.setPermissions(filename, 0775, -1, -1);
			inputEntity.writeTo(out);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
			if (is != null) {
		        try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
        (new File(filename)).setLastModified(System.currentTimeMillis());
    }
    
    /**
     * Calculate the size of detail preview image that be decoded with ratio.
     * For at 1080P device, the decoded image is too large to render, 
     * so the system cannot work smooth.
     * 
     * @param wallpaperWidth
     * @param wallpaperHeight
     */
    public static Point calcSizeForDecoder(int wallpaperWidth, int wallpaperHeight) {
    	final float scaleRatio = calcScaleRatio(wallpaperWidth, wallpaperHeight);
		return new Point(
				(int) (wallpaperWidth * scaleRatio), 
				(int) (wallpaperHeight * scaleRatio));
    }
    private static float calcScaleRatio(int wallpaperWidth, int wallpaperHeight) {
    	final int area = wallpaperWidth * wallpaperHeight;
    	if (area < ResourceConstants.IMAGE_DECODER_SCALE_THRESHOLD) {
    		return ResourceConstants.IMAGE_DECODER_SCALE_RATIO[0];
    	} else { // >= 1080P
    		return ResourceConstants.IMAGE_DECODER_SCALE_RATIO[1];
    	}
    	//return ResourceConstants.IMAGE_DECODER_SCALE_RATIO[0];
    }
    
    
    /**
     * Static variant, save folder infos in app life.
     */
    private static FolderCache mFolderInfoCache = new FolderCache();
    public static FolderCache.FolderInfo getFolderInfoCache(String folderPath) {
        return mFolderInfoCache.get(folderPath);
    }
}
