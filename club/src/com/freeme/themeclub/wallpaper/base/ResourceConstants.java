package com.freeme.themeclub.wallpaper.base;

import com.freeme.themeclub.wallpaper.util.OnlineUtils;

public class ResourceConstants {
	// resource type
	public static final int R_NONE 					= -1;
	public static final int R_ALL 					= 1 << 0;
	public static final int R_DESKTOP_WALLPAER 		= 1 << 1;
	public static final int R_LOCKSCREEN_WALLPAER 	= 1 << 2;
	
	// display type
    public static final int DISPLAY_SINGLE = 1;
    public static final int DISPLAY_SINGLE_SMALL = 2;
    public static final int DISPLAY_SINGLE_DETAIL = 3;
    public static final int DISPLAY_SINGLE_GALLERY = 4;
    public static final int DISPLAY_SINGLE_MUSIC = 5;
    public static final int DISPLAY_DOUBLE_FLAT = 6;
    public static final int DISPLAY_TRIPLE = 7;
    public static final int DISPLAY_TRIPLE_FLAT = 8;
    public static final int DISPLAY_TRIPLE_TEXT = 9;
    public static final int DISPLAY_DOUBLE_FLAT_FONT = 11;
    public static final int DISPLAY_DOUBLE_FLAT_ICON = 10;
    
    // display type extras
    public static final int DISPLAY_EXTRA_DEFAULT   = 0;
    public static final int DISPLAY_EXTRA_MAIN_FLAT = 1;
    
    // resource path
    public static final String SYSTEM_WALLPAPER_FOLDER = "/system/media/wallpaper";
    public static final String SYSTEM_LOCKSCREEN_FOLDER = "/system/media/lockscreen";
    public static final String DOWNLOAD_WALLPAPER_FOLDER = OnlineUtils.getSDPath()+"/themes/download";
    
    /**
     * For WallpaperDecoder, decoded size ratio. 
     */
    public static final int IMAGE_DECODER_SCALE_THRESHOLD = (int) ((1080 * 1920) * 0.95);
    public static final float[] IMAGE_DECODER_SCALE_RATIO = {
    	1.0f, 	// 480x800 540x960 720x1280 or blow
    	0.5f, 	// 1080x1920 or above
    };
}
