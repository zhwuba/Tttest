package com.market.net;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.DisplayMetrics;

import com.zhuoyi.market.constant.Constant;
import com.market.download.util.NetworkType;
import com.market.download.util.Util;
import com.market.net.build.BuildApkDetailInfo;
import com.market.net.build.BuildApkListByPage;
import com.market.net.build.BuildAssociativeWordReq;
import com.market.net.build.BuildCommentReq;
import com.market.net.build.BuildCommitCommentReq;
import com.market.net.build.BuildDetailRecommendInfo;
import com.market.net.build.BuildDiscoverInfo;
import com.market.net.build.BuildDownloadRecommendInfo;
import com.market.net.build.BuildIntegralReq;
import com.market.net.build.BuildModelApkListByPage;
import com.market.net.build.BuildModelTopicInfo;
import com.market.net.build.BuildSaleInfo;
import com.market.net.build.BuildSearchAppListInfo;
import com.market.net.build.BuildSoftGameDetailInfo;
import com.market.net.build.BuildSoftGameTopicInfo;
import com.market.net.build.BuildStaticSearchAppInfo;
import com.market.net.build.BuildSubjectDataInfo;
import com.market.net.build.BuildTopicListInfo;
import com.market.net.build.BuildUpdateMarketAppInfo;
import com.market.net.build.BuildUserFeedbackInfo;
import com.market.net.build.BuildWallpaperDetailInfo;
import com.market.net.build.BuildWallpaperInfo;
import com.market.net.build.BuildYCBAppInfo;
import com.market.net.build.BuildYouLikeAppInfo;
import com.market.net.data.TerminalInfo;

public class SenderDataProvider {

	private static TerminalInfo mTerminalInfo;	
	private static final Map<Integer, DataBuilder> mDataBuilder;
	private static final int MAX_COUNT = 16;
	static {
	    mDataBuilder = new HashMap<Integer,DataBuilder>();
	    mDataBuilder.put(MessageCode.GET_MODEL_APK_LIST_BY_PAGE, new BuildModelApkListByPage());
	    mDataBuilder.put(MessageCode.GET_APK_LIST_BY_PAGE, new BuildApkListByPage());
	    mDataBuilder.put(MessageCode.GET_APK_DETAIL, new BuildApkDetailInfo());
	    mDataBuilder.put(MessageCode.GET_RECOMMEND_APPS, new BuildDetailRecommendInfo());
	    mDataBuilder.put(MessageCode.GET_STATIC_SEARCH_APP, new BuildStaticSearchAppInfo());
	    mDataBuilder.put(MessageCode.GET_TOPIC_LIST, new BuildTopicListInfo());
	    mDataBuilder.put(MessageCode.GET_SEARCH_APP,new BuildSearchAppListInfo());
	    mDataBuilder.put(MessageCode.GET_APPS_UPDATE, new BuildUpdateMarketAppInfo());
	    mDataBuilder.put(MessageCode.CHECK_APP_VALID, new BuildYCBAppInfo());
	    mDataBuilder.put(MessageCode.GUESS_YOU_LIKE, new BuildYouLikeAppInfo());
	    mDataBuilder.put(MessageCode.GET_ASSOCIATIVE_WORD, new BuildAssociativeWordReq());
	    mDataBuilder.put(MessageCode.GET_DATA_STAUS_REQ, new BuildSaleInfo());
	    mDataBuilder.put(MessageCode.GET_USER_COMMENT_REQ, new BuildCommentReq());
	    mDataBuilder.put(MessageCode.GET_DOWNLOAD_RECOMMEND_APPS, new BuildDownloadRecommendInfo());
	    mDataBuilder.put(MessageCode.COMMIT_USER_COMMENT_REQ, new BuildCommitCommentReq());
	    mDataBuilder.put(MessageCode.GET_USER_FEEDBACK, new BuildUserFeedbackInfo());
	    mDataBuilder.put(MessageCode.GET_SOFT_GAME_TOPIC, new BuildSoftGameTopicInfo());
	    mDataBuilder.put(MessageCode.GET_SOFT_GAME_DETAIL, new BuildSoftGameDetailInfo());
	    mDataBuilder.put(MessageCode.GET_INTEGRAL_INFO_REQ, new BuildIntegralReq());
	    mDataBuilder.put(MessageCode.GET_APK_DETAIL_BY_PACKNAME_REQ, new BuildApkDetailInfo());
	    mDataBuilder.put(MessageCode.GET_MODEL_TOPIC_REQ, new BuildModelTopicInfo());
	    mDataBuilder.put(MessageCode.GET_DISCOVER_DATA, new BuildDiscoverInfo());
	    mDataBuilder.put(MessageCode.GET_WALLPAPER_LIST_REQ, new BuildWallpaperInfo());
	    mDataBuilder.put(MessageCode.GET_WALLPAPER_DETAIL_REQ, new BuildWallpaperDetailInfo());
	    mDataBuilder.put(MessageCode.GET_SUBJECT_DATA_REQ, new BuildSubjectDataInfo());
	    mDataBuilder.put(MessageCode.NONE, null);
	}
	
	
	public SenderDataProvider() {
		
	}
	
	public static DataBuilder fetchDataBuilder(int messageCode) {
        if(mDataBuilder.containsKey(messageCode))
            return mDataBuilder.get(messageCode);
        else
            return null;
        
    }
	
	public static PackageInfo getPackageInfo(Context context,String pName) {
	    PackageInfo pinfo = null;

		try {
			pinfo = context.getPackageManager().getPackageInfo(pName, 0);
			//versionCode = pinfo.versionCode;

		} catch (NameNotFoundException e) {

		}
		return pinfo;
	}
	
	
	private static byte getNetworkType(Context context) {
		byte netStatus = 3;
		int currNetType = NetworkType.getNetworkType(context);
		if (currNetType != NetworkType.NOT_AVAILABLE) {
			netStatus = (byte)currNetType;
		}
		
		return netStatus;
	}
	
	public static long getAndroidRamSize()
	{
		String[] meminfoLabels = {"MemTotal:"};
		long[] meminfoValues = new long[1];
		meminfoValues[0] = -1;
		Class<?> proc = null;
		try{
			proc = Class.forName("android.os.Process");
			Method method = proc.getMethod("readProcLines", String.class, String[].class, long[].class);
			method.invoke(proc.newInstance(), "/proc/meminfo", meminfoLabels, meminfoValues);
		}catch(Exception e){
			e.printStackTrace();
		}
//		String RAM = "null";
//		if(meminfoValues[0] != -1){
//			RAM = Long.toString(meminfoValues[0] / 1024) + "M";
//		}
		return meminfoValues[0];
	}
	
	
	public static String getSystemProperties(String key) {
		String value = "";
		try {
			Class<?> sysPro = Class.forName("android.os.SystemProperties");
			Method getMethod = sysPro.getDeclaredMethod("get", String.class);
			value = (String)getMethod.invoke(sysPro, key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return value;
	}
	
	public static String getFreeMeOsVersion() {
		return getSystemProperties("ro.build.version.freemeos");
	}
	
	
	public static TerminalInfo generateTerminalInfo(Context context)
	{
		int versionCode = 0;
		String pName = "";
		String apkVersionName = "";
		if(mTerminalInfo!=null)
			return mTerminalInfo;
		
		TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		mTerminalInfo = new TerminalInfo();
		
		DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();

		mTerminalInfo.setHsman(android.os.Build.MANUFACTURER);
		mTerminalInfo.setHstype(android.os.Build.MODEL);
		mTerminalInfo.setOsVer(android.os.Build.VERSION.RELEASE);
		mTerminalInfo.setScreenHeight((short)outMetrics.heightPixels);
		mTerminalInfo.setScreenWidth((short)outMetrics.widthPixels);
		mTerminalInfo.setAppId(Constant.CP_ID);
		mTerminalInfo.setChannelId(Constant.td);
		pName = context.getPackageName();
		PackageInfo pInfo = getPackageInfo(context, pName);
		if(pInfo!=null)
		{
		    versionCode = pInfo.versionCode;
		    apkVersionName = pInfo.versionName;
		}
		
		mTerminalInfo.setApkVersion(versionCode);
		mTerminalInfo.setPackageName(pName);
		mTerminalInfo.setApkVerName(apkVersionName);
		mTerminalInfo.setImei(tManager.getDeviceId());
		mTerminalInfo.setImsi(tManager.getSubscriberId());
		mTerminalInfo.setNetworkType((byte)getNetworkType(context));
		mTerminalInfo.setRamSize(getAndroidRamSize());
		mTerminalInfo.setCpu(Build.HARDWARE);
		
		mTerminalInfo.setRomSize(getHandsetRomSize());
		mTerminalInfo.setLbs(getCurrLbs(context));
		mTerminalInfo.setUuid(getDeviceUUID());
		mTerminalInfo.setMac(getMacAddress(context));
		
		JSONObject reservedJo = new JSONObject();
		try {
			reservedJo.put("marketSignal", Util.getMarketSignal(context));
			reservedJo.put("freeMeVer", getFreeMeOsVersion());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mTerminalInfo.setReserved(reservedJo.toString());
		mTerminalInfo.setSdkApiVer(getSdkApiVersion());
		
		return mTerminalInfo;
	}

	public static String buildToJSONData(Context context,int msgCode,Object obj)
	{
	    String result = "";
	    
	    try
	    {
	        if(obj==null)
	            result = buildCommonDataToJSON(context,msgCode);
	        else
	            result = fetchDataBuilder(msgCode).buildToJson(context,msgCode,obj);
	        
	    }catch(Exception e)
	    {
	        e.printStackTrace();
	    }
	    return result;
	    
	}
	public static String buildHeadData(int msgCode)
	{
	    String result = "";
	    
	    UUID uuid = UUID.randomUUID();
        
        Header header = new Header();
        
        header.setBasicVer((byte)1);
        
        header.setLength(84);
        
        header.setType((byte)1);
        
        header.setReserved((short)0);
        
        header.setFirstTransaction(uuid.getMostSignificantBits());
        
        header.setSecondTransaction(uuid.getLeastSignificantBits());
        
        header.setMessageCode(msgCode);
        
        result = header.toString();
        
	    return result;
	}
	public static String buildCommonDataToJSON(Context context,int msgCode)
	{
		String result = "";
		
		String body = "";
	
		JSONObject jsObject = new JSONObject();
				
		if(context==null)
			return result;
	
		TerminalInfo terminalInfo = generateTerminalInfo(context);

	    body = terminalInfo.toString();
	    try
	    {
			jsObject.put("head", buildHeadData(msgCode));
			jsObject.put("body", body);
			result = jsObject.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String getMacAddress(Context context) {
		String mac = "";
		try {
			WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);     
			WifiInfo info = wifi.getConnectionInfo();
			mac = info.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac;
	}
	
	
	public static long getHandsetRomSize() {
		//ROM
		File romPath = Environment.getDataDirectory();
		StatFs stat = new StatFs(romPath.getPath());
		long blockSize = stat.getBlockSize();
		long blockCount = stat.getBlockCount();
		long totalBytes = blockSize * blockCount;
		return totalBytes / 1024 / 1024;
	}
	
	
	public static String getCurrLbs(Context context) {
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		int phoneType = tm.getPhoneType();
		int lac = 0;
		int cellId = 0;
		
		CellLocation cellLocation = tm.getCellLocation();
		if(cellLocation != null && phoneType == TelephonyManager.PHONE_TYPE_GSM){
			GsmCellLocation gsmCellLocation = (GsmCellLocation)cellLocation;
			lac = gsmCellLocation.getLac();
			cellId = gsmCellLocation.getCid();
		}else if(cellLocation == null){
			return null;
		}
		String simNumeric = tm.getSimOperator();
		String mcc = "000";
		String mnc = "00";
		if(simNumeric != null && !simNumeric.equals("")){
			mcc = simNumeric.substring(0, 3);
			mnc = simNumeric.substring(3);
		}
		
		String lbs = mcc + ":" + mnc + ":" + Integer.toString(cellId) + ":" + Integer.toString(lac);
		return lbs;
	}
	
	
	public static String getDeviceUUID(){ 
		Object result = null,result1 = null,result2 = null; 
		try { 
			Class<?> classType = Class.forName("android.os.ServiceManager"); 
			Object invokeOperation = classType.newInstance(); 
			Method getMethod = classType.getMethod("getService", new Class[] {String.class}); 
			result = getMethod.invoke(invokeOperation, new Object[] {new String("TydNativeMisc")}); 
	
			Class<?> classType1 = Class.forName("com.freeme.internal.server.INativeMiscService$Stub"); 
			//Object invokeOperation1 = classType1.newInstance(); 
			Method getMethod1 = classType1.getMethod("asInterface", new Class[] {IBinder.class}); 
			result1 = getMethod1.invoke(classType1, new Object[] {result}); 
	
			Class<?> classType2 = result1.getClass();;//Class.forName(result1.toString());//"com.freeme.internal.server.INativeMiscService$Stub$Proxy");//result1.getClass();
			//Object invokeOperation2 = classType2.newInstance(); 
			Method getMethod2 = classType2.getMethod("getDeviceUUID", new Class[] {}); 
			result2 = getMethod2.invoke(result1, new Object[] {}); 

		} catch (Exception e) {
		    //do nothing if can't get free me uuid
		}
		
		return result2!=null?result2.toString():""; 
	}
	
	
	public static int getSdkApiVersion() {
		return Build.VERSION.SDK_INT;
	}
}
