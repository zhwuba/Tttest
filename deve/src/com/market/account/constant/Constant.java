package com.market.account.constant;

/**
 * 
 * @author sunlei
 * 
 */
public class Constant {

	//测试服务器
//	public static String BASE_URL = "http://101.95.97.178:7895";
	//正式服务器测试端口
//	public static String BASE_URL = "http://lapi.tt286.com:7895";
	//正式服务器
	public static String BASE_URL = "http://lapi.tt286.com:7892";
	
    public static String download_path = "/ZhuoYouLogin/";
    public static String download_cache_dir = download_path+"download/cache/image/";
    
    public static String CHANNEL_ID = "";
    
    public static String USERLOGO  = "/ZhuoYiMarket/";
    public static String USERLOGO_BASE_PATH = USERLOGO + "usercenter/";
    public static String USERLOGO_PATH = USERLOGO_BASE_PATH + "logo.png";
    public static boolean SHOW_IMAGE;
    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.zhuoyou.account.android.samplesync";
    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.zhuoyou.account.android.samplesync";
    
    /**
     * GET_REG_NUM,register url
     */
    public static final String GET_REG_NUM = "http://account.oo523.com/register";
    /**
     * LOGIN,login url
     */
    public static final String LOGIN =BASE_URL + "/lapi/login";
    /**
     * FIND_CODE,find code url
     */
    public static final String FIND_CODE = "http://account.oo523.com/forgetpass";
    /**
     * RESET_CODE,reset code url
     */
    public static final String RESET_CODE = "http://account.oo523.com/resetpass";
    /**
     * SEDN_MMS_DELAY,delay
     */
    public static final int SEDN_MMS_DELAY = 15000;
    /**
     * TAG,for log
     */
    /**
     * user center url
     */
    public static final String USERCENTER =BASE_URL + "/lapi/urls";
    /**
     * signkey md5
     */
    public static final String SIGNKEY = "ZYK_ac17c4b0bb1d5130bf8e0646ae2b4eb4"; 
    /**
     * author (register)
     */
    public static final String AUTH =BASE_URL + "/lapi/auth";
    /**
     * new register 
     */
    public static final String ZHUOYOUREGISTER =BASE_URL + "/lapi/getrandcode";
    /**
     * signup
     */
    public static final String ZHUOYOU_REGISTER_SIGNUP =BASE_URL + "/lapi/signup";
    
    /**
     * user score
     */
    public static final String ZHUOYOU_GET_USER_SCORE =BASE_URL + "/lapi/score";
    /**
     * user checkin
     */
    public static final String ZHUOYOU_USER_CHECK_IN =BASE_URL + "/lapi/checkin";
    /**
     * user resetpass
     */
    public static final String ZHUOYOU_USER_RESET_PASSWD =BASE_URL + "/lapi/resetpass";
    /**
     * user reward_push
     */
    public static final String ZHUOYOU_REWARD_PUSH = BASE_URL + "/lapi/reward_push";
    
    public static final String ZHUOYOU_GET_USER_INFO =BASE_URL + "/lapi/userinfo";
    
    public static final String ZHUOYOU_EDIT_USER_INFO =BASE_URL + "/lapi/useredit";
    //debug
    public static final String ZHUOYOU_DEBUG_RMUSER =BASE_URL + "/lapi/debug_rmusr";

    public static final String ZHUOYOU_DEBUG_UNAUTH =BASE_URL + "/lapi/debug_unauth";

    public static final String ZHUOYOU_DEBUG_UNCHECK =BASE_URL + "/lapi/debug_uncheck";
    public static final String ZHUOYOU_DEBUG_ENABLE =BASE_URL + "/lapi/debug_rmsession";
    /**
     * register licence url
     */
    public static final String ZHUOYOU_LICENCE_URL = "http://ua.zhuoyitech.net/html/zhuoyi.html";
    public static final String KOOBEE_LICENCE_URL = "http://ua.zhuoyitech.net/html/koobee.html";
}

