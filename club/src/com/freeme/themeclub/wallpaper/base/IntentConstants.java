package com.freeme.themeclub.wallpaper.base;

import android.content.Intent;


public class IntentConstants {
	// live picker app
	public static final String LIVE_PICKER_PKG = "com.android.wallpaper.livepicker";
	public static final String LIVE_PICKER_PKG_ENTRY = "com.android.wallpaper.livepicker.LiveWallpaperActivity";
	// video live picker app
	public static final String VIDEO_LIVE_PICKER_PKG = "com.mediatek.vlw";
	public static final String VIDEO_LIVE_PICKER_PKG_ENTRY = "com.mediatek.vlw.VideoEditor";
	// video live wallpaper service
	public static final String VIDEO_LIVE_WALLPAPER_PKG = "com.mediatek.vlw";
	public static final String VIDEO_LIVE_WALLPAPER_PKG_ENTRY = "com.mediatek.vlw.VideoLiveWallpaper";
	
	/// -----------------------------------------------------------------------
	public static final String ACTION_SET_WALLPAPER = Intent.ACTION_SET_WALLPAPER;
	public static final String ACTION_SET_LOCKSCREEN_WALLPAPER = "android.intent.action.SET_LOCKSCREEN_WALLPAPER";
	
	/// -----------------------------------------------------------------------
	private static final String _PREFIX = "com.tydtech.wallpaperchooser.";
	public static final String EXTRA_RESOURCE_FLAG = _PREFIX + "extra_resource_flag";
	public static final String EXTRA_RESOLVE_INFO_LIST = _PREFIX + "extra_resolve_info_list";
	
	public static final String EXTRA_META_DATA= "META_DATA";
	public static final String EXTRA_META_DATA_FOR_LOCAL = "META_DATA_FOR_LOCAL";
	public static final String EXTRA_META_DATA_FOR_ONLINE = "META_DATA_FOR_ONLINE";
	
	public static final String EXTRA_RESOURCE_INDEX = _PREFIX + "extra_resource_index";
	public static final String EXTRA_RESOURCE_GROUP = _PREFIX + "extra_resource_group";
	public static final String EXTRA_RESOURCE_SET_PACKAGE = _PREFIX + "extra_resource_set_package";
	public static final String EXTRA_RESOURCE_SET_SUBPACKAGE = _PREFIX + "extra_resource_set_subpackage";
	public static final String EXTRA_RESOURCE_SET_NAME = _PREFIX + "extra_resource_set_name";
	
	public static final String EXTRA_DISPLAY_TYPE = _PREFIX + "extra_resource_display_type";
	public static final String EXTRA_SOURCE_FOLDERS = _PREFIX + "extra_source_folders";
	public static final String EXTRA_CURRENT_USING_PATH = _PREFIX + "extra_current_using_path";
	public static final String EXTRA_CACHE_LIST_FOLDER = _PREFIX + "extra_cache_list_folder";
	
	public static final String EXTRA_DETAIL_ACTIVITY_PACKAGE = _PREFIX + "extra_detail_activity_package";
	public static final String EXTRA_DETAIL_ACTIVITY_CLASS = _PREFIX + "extra_detail_activity_class";
}
