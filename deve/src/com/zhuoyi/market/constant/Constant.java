package com.zhuoyi.market.constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.market.featureOption.FeatureOption;
import com.market.net.utils.OpenUrlPostUtils;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.Jni;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.XmlParseUrlUtil;

public class Constant {
	public static  String DB_NAME = "update.db";
	public static int VERSION = 1;

    
    /**
     * if value is true, now use the local urls whitch load from sdcard, default false;
     */
    public static boolean useLocalDomainName = false;
    
	
    /*
     * end define switch
     */
    
	// 渠道号
    public static final String td = MarketApplication.mChannelID;// "1001";
    
    public static final String CP_ID = MarketApplication.mCpID;//"1001";
	//download path
    public static final String download_dir_name = "/ZhuoYiMarket";
	public static final String download_path = download_dir_name + "/";
	public static final String download_cache_dir = download_path+"download/cache/image/";
	public static final String config_path = download_path + "download/cache/";
	
	//update self path
	public static final String update_dir_name = "UpdateSelf";
	public static final String update_dir_path = download_path + update_dir_name;
	
	public static final String URL_CONFIG_PATH = download_path + "urlconfig/";
    public static final String URL_CONFIG =  URL_CONFIG_PATH + "urlconfig.xml";


    
/*/
    //正式服务器
//    public static String MARKET_URL_FIRST = "http://service-market.oo523.com:2560";
//    public static String EXTERN_DOWNLOAD_URL_FIRST = "http://service-market.oo523.com:2580/101016?imei=";
//    public static String TOTAL_URL_FIRST = "http://data-market.oo523.com:2540";
//    public static String SELF_UPDATE_URL_FIRST = "http://update-market.oo523.com:2520";
//    
//    public static String MARKET_URL_SECOND = "http://service-market.tt286.com:2560";
//    public static String EXTERN_DOWNLOAD_URL_SECOND = "http://service-market.tt286.com:2580/101016?imei=";
//    public static String TOTAL_URL_SECOND = "http://data-market.tt286.com:2540";
//    public static String SELF_UPDATE_URL_SECOND = "http://update-market.tt286.com:2520";
    
    public static String ENCODE_DECODE_KEY = "x_s0_s22";
    
    //测试服务器
//    public static String MARKET_URL_FIRST = "http://101.95.97.178:2560";
//    public static String EXTERN_DOWNLOAD_URL_FIRST = "http://service-market.oo523.com:2580/101016?imei=";
//    public static String TOTAL_URL_FIRST = "http://101.95.97.178:2540";
//    public static String SELF_UPDATE_URL_FIRST = "http://101.95.97.178:2520";
//    
//    public static String MARKET_URL_SECOND = "http://101.95.97.178:2560";
//    public static String EXTERN_DOWNLOAD_URL_SECOND = "http://service-market.oo523.com:2580/101016?imei=";
//    public static String TOTAL_URL_SECOND = "http://101.95.97.178:2540";
//    public static String SELF_UPDATE_URL_SECOND = "http://101.95.97.178:2520";
    
    
    //正式服务器，测试端口
    public static String MARKET_URL_FIRST = "http://211.151.182.182:2571";
    public static String EXTERN_DOWNLOAD_URL_FIRST = "http://service-market.oo523.com:2580/101016?imei=";
    public static String TOTAL_URL_FIRST = "http://data-market.oo523.com:2540";
    public static String SELF_UPDATE_URL_FIRST = "http://update-market.oo523.com:2520";
      
    public static String MARKET_URL_SECOND = "http://211.151.182.182:2571";
    public static String EXTERN_DOWNLOAD_URL_SECOND = "http://service-market.tt286.com:2580/101016?imei=";
    public static String TOTAL_URL_SECOND = "http://data-market.tt286.com:2540";
    public static String SELF_UPDATE_URL_SECOND = "http://update-market.tt286.com:2520";  
      
   
    //xiaodan 主机
//    public static String MARKET_URL_FIRST = "http://192.168.3.15:2560";
//    public static String EXTERN_DOWNLOAD_URL_FIRST = "http://service-market.oo523.com:2580/101016?imei=";
//    public static String TOTAL_URL_FIRST = "http://192.168.3.15:2540";
//    public static String SELF_UPDATE_URL_FIRST = "http://192.168.3.15:2520";
    
/*/
    
    public static String MARKET_URL_FIRST = "";
    public static String EXTERN_DOWNLOAD_URL_FIRST = "";
    public static String TOTAL_URL_FIRST = "";
    public static String SELF_UPDATE_URL_FIRST = "";
    
    public static String MARKET_URL_SECOND = "";
    public static String EXTERN_DOWNLOAD_URL_SECOND = "";
    public static String TOTAL_URL_SECOND = "";
    public static String SELF_UPDATE_URL_SECOND = "";
    
    public static String ENCODE_DECODE_KEY = "";
//*/
    
    public static String MARKET_URL = MARKET_URL_FIRST;
    public static String EXTERN_DOWNLOAD_URL = EXTERN_DOWNLOAD_URL_FIRST;
    public static String TOTAL_URL = TOTAL_URL_FIRST;
    public static String SELF_UPDATE_URL = SELF_UPDATE_URL_FIRST;
    
    public static String ZHUOYOU_URL_SCORE_DETAIL = com.market.account.constant.Constant.BASE_URL + "/lapi/ui_scoredetail";
    public static String ZHUOYOU_URL_INTEGRATION_RULE = com.market.account.constant.Constant.BASE_URL + "/lapi/ui_scorerule";
    public static String ZHUOYOU_URL_MINE_ACTIVITY= com.market.account.constant.Constant.BASE_URL + "/lapi/ui_home";
    public static String ZHUOYOU_URL_TASK= com.market.account.constant.Constant.BASE_URL + "/lapi/ui_task";
    public static String ZHUOYOU_URL_BINEMOBILE= com.market.account.constant.Constant.BASE_URL + "/lapi/ui_bindmobile";
    
    public static final String MARKET_APP_SHARE_URL = "http://qrscan.zhuoyi.com/appinfo/index.php";
    
    
    public static void setDomainName(boolean trustedDomainName) { 
        if(useLocalDomainName) {
        	return;
        }
    	if(trustedDomainName) {
    		MARKET_URL = MARKET_URL_FIRST;
    		EXTERN_DOWNLOAD_URL = EXTERN_DOWNLOAD_URL_FIRST;
    		TOTAL_URL = TOTAL_URL_FIRST;
    	    SELF_UPDATE_URL = SELF_UPDATE_URL_FIRST;
    	} else {
        	MARKET_URL = MARKET_URL_SECOND;
        	EXTERN_DOWNLOAD_URL = EXTERN_DOWNLOAD_URL_SECOND;
        	TOTAL_URL = TOTAL_URL_SECOND;
        	SELF_UPDATE_URL = SELF_UPDATE_URL_SECOND;
    	}
    }
    
    
    private static boolean getMarketUrlFromJni(Context context) {
    	Jni checkSignUtil = new Jni();
		String[] url = checkSignUtil.getMarketUrl(context);
		if(url != null && url.length == 9){
			Constant.MARKET_URL_FIRST = url[0];
			Constant.EXTERN_DOWNLOAD_URL_FIRST = url[1];
			Constant.TOTAL_URL_FIRST = url[2];
			Constant.SELF_UPDATE_URL_FIRST = url[3];
			
			Constant.MARKET_URL_SECOND = url[4];
			Constant.EXTERN_DOWNLOAD_URL_SECOND = url[5];
			Constant.TOTAL_URL_SECOND = url[6];
			Constant.SELF_UPDATE_URL_SECOND = url[7];
			
			ENCODE_DECODE_KEY = url[8];
			
			//must pass the signal check, unless don't use url loaded from sdcard
			initMarketUrlFromSdcard();
			return true;
		}
		
		return false;
    }
    
    
    /**
	 * 从外部直接进入详情，初始化市场地址
	 */
	public static void initMarketUrl(Context context) {
		if(!FeatureOption.CHECK_SIGN) return;
		if(TextUtils.isEmpty(Constant.MARKET_URL_FIRST)){
			getMarketUrlFromJni(context);
			setDomainName(true);
		}
	}
	
	
//	public void initSdcardUrlXml(){
//		try {
//			File cfgFile = new File(Util.FileManage.getSDPath() + Constant.URL_CONFIG);
//			if(!cfgFile.exists()){
//				File file = new File(Util.FileManage.getSDPath() + Constant.URL_CONFIG_PATH);
//				if(!file.exists()){
//					file.mkdirs();
//				}
//				cfgFile = new File(Util.FileManage.getSDPath() + Constant.URL_CONFIG);
//				OutputStream out = new FileOutputStream(cfgFile);
//				Map<String,String> map = new HashMap<String, String>();
//				map.put(XmlParseUrlUtil.MARKET_TAG,"");
//				map.put(XmlParseUrlUtil.TOTAL_TAG, "");
//				map.put(XmlParseUrlUtil.SELF_UPDATE_TAG, "");
//
//				map.put(XmlParseUrlUtil.ACCOUNT_TAG, "");
//				XmlParseUrlUtil.saveUrlToXML(map, out);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	public static void initMarketUrlFromSdcard(){
		if (!FeatureOption.READ_SDCARD_URL) {
			return;
		}
		Map<String, String> map;
		String key = "";
		String value = "";
		File file = new File(MarketUtils.FileManage.getSDPath() + Constant.URL_CONFIG);
		if(file.exists()){
			try {
				InputStream input = new FileInputStream(file);
				map = XmlParseUrlUtil.getMarketURLFromXml(input);
				for(Map.Entry<String, String> entry : map.entrySet()){
					key = entry.getKey();
					value = entry.getValue();
					if(key.equals(XmlParseUrlUtil.MARKET_TAG) && !TextUtils.isEmpty(value)){
						Constant.MARKET_URL = value;
						useLocalDomainName = true;
					}else if(key.equals(XmlParseUrlUtil.TOTAL_TAG) && !TextUtils.isEmpty(value)){
						Constant.TOTAL_URL = value;
						useLocalDomainName = true;
					}else if(key.equals(XmlParseUrlUtil.SELF_UPDATE_TAG) && !TextUtils.isEmpty(value)){
						Constant.SELF_UPDATE_URL = value;
						useLocalDomainName = true;
					}
					else if(key.equals(XmlParseUrlUtil.ACCOUNT_TAG) && !TextUtils.isEmpty(value)){
						com.market.account.constant.Constant.BASE_URL = value;
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	//签名相关start
    public static boolean checkMarketSign(Context context) {
        if(!FeatureOption.CHECK_SIGN) {
        	return true;
        }
        
        return getMarketUrlFromJni(context);
    }

    
    static{
        System.loadLibrary("zyjni");
    }
    //签名相关end

}
