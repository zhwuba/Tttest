package com.market.featureOption;


/**
 * define the function switch on market
 * @author Athlon
 *
 */
public class FeatureOption {
	
    /**
     * close it on development version, and open it on release version
     */
    public static final boolean RELEASE_VERSION = true;
    
    /**
     * for open or close the background download function: 700、704、705、706、零流量
     */
    public static final boolean BACKGROUND_DOWNLOAD = true;
    
    /**
     * for report statistics information to server, please open it on release version, and close it on development version
     */
    public static final boolean STATISTICS_REPORT = true;
    
	/**
	 * for check signal about market, debug version should close it, and open it on release version
	 */
    public static final boolean CHECK_SIGN = RELEASE_VERSION;
    
    /**
     * to read url from sdcard on debug version, please close it on release version
     */
    public static final boolean READ_SDCARD_URL = !RELEASE_VERSION;
    
    /**
     * show guide when use this version first time, close it will not show guide at the first time entry this version
     */
    public static final boolean SHOW_GUIDE_FIRST = true;
    
    /**
     * if open it, market will be able to download app from third-party, unless close it
     */
    public static final boolean DOWNLOAD_FROM_THIRD_PARTY = false;
    
    /**
     * update application with delta file, no need to download the whole apk file to update application
     * function immaturity, so close it now
     */
    public static final boolean DELTA_UPDATE_APP = false;
    
	
	//debug relative start defined, please close these switch on release version
	/**
	 * MarketDebug.java function switch, for record log on 
	 */
	public static final boolean DEBUG_REPORT = !RELEASE_VERSION;
	/**
	 * for download module log switch, open to print adb log for download module
	 */
	public static boolean DOWNLOAD_LOG = !RELEASE_VERSION;
	/**
	 * for foreground log about market, open to print foreground log
	 */
	public static final boolean MARKET_LOG = !RELEASE_VERSION;
	/**
	 * for user behavior log debug mode, please close it on release version
	 */
	public static final boolean BEHAVIOR_LOG_DEBUG = !RELEASE_VERSION;
	
	//debug relative end defined
	
	/**
	 * ListView中item水波纹效果：true打开，false关闭
	 */
	public static final boolean WATER_FLOW = false;
    
    
}
