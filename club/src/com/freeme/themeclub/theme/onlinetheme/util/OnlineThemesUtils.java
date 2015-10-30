package com.freeme.themeclub.theme.onlinetheme.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.freeme.themeclub.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

public class OnlineThemesUtils
{
	public static String mDownload_path = "/themes/";
    public static String mWallpaper_download_path = "/themes/";

	private static String mDownload_dir;	
	public static String mDetailsUrl 			= "http://apk.oo523.com/Appstores/appdetail?format=json";
	public static String mListUrl 				= "http://apk.oo523.com/AppStores/AppListByTagIds?format=json"; //tagids=263
	//public static String mListUrl 				= "http://apk.oo523.com/AppStores/AppListByTagIds?tagids=263&format=json";
	public static String mSingleDownLoadImage 	= "http://apk.oo523.com/appstores/DownloadSingleAppImage?format=json";
	public static String mDownLoadSuccessUrl	= "http://apk.oo523.com/appstores/DownloadSuccess?&typeId=4&format=json";
	public static String mDownloadAppUrl		= "http://apk.oo523.com/appstores/download?format=json";
	public static boolean mIsFirst				= true;
	private static int 			mDownloadInfoFromDb = 0;
	private static int 			mDownload_num = 1;
	private static final int 	mDownload_max_num = 2;
	public static String getListUrl(Context context)
	{
		init_public_param(context);
		return mListUrl;
	}
	public static void savaDownloadCount(int count)
	{
		mDownloadInfoFromDb = count;
	}
	public static boolean isExceedDownLoadNum()
	{
		return mDownloadInfoFromDb > mDownload_max_num ? true : false;
	}
	public static int getMaxDownLoadNum()
	{
		return mDownload_max_num;
	}
	public static String getDetailsUrl(Context context)
	{
		init_public_param(context);
		return mDetailsUrl;
	}
	public static String getSingleDownLoadImageUrl(Context context)
	{
		init_public_param(context);
		return mSingleDownLoadImage;
	}
	public static String getDownLoadAppUrl(Context context)
	{
		init_public_param(context);
		return mDownloadAppUrl;
	}
	public static String getDownLoadAppSuccessedUrl(Context context)
	{
		init_public_param(context);
		return mDownLoadSuccessUrl;
	}
	public static void setDownLoadPath(String newPath)
	{
		mDownload_path = newPath;
	}
	public static void setFileDir(String newPath)
	{
		mDownload_dir = newPath;
	}
	public static String getDownLoadPath()
	{
		return getSDPath()+mDownload_path;
	}
	public static int getInstalledApkVersionCode(Context context,String pName)
	{ 
		int versionCode = 0;

		try 
		{
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(
					pName, PackageManager.GET_CONFIGURATIONS);
			versionCode = pinfo.versionCode;

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}
	public static boolean isEqualsVersionCode(Context context,String versionCode,String pName)
	{
		boolean result = false;
		int code = 0;
		int installed_apk_code = 0;
		if(!TextUtils.isEmpty(versionCode))
		{
			code = Integer.parseInt(versionCode);
		}
		installed_apk_code = getInstalledApkVersionCode(context,pName); 

		if(code == installed_apk_code)
			result = true;

		return result;
	}

	public static boolean checkInstalled(Context context,String pName)
	{
		PackageInfo packageInfo;
		if (pName == null)
		{
			return false;
		}
		else
		{
			try
			{
				packageInfo = context.getPackageManager().getPackageInfo(pName,0);
			}
			catch (Exception e)
			{
				packageInfo = null;
			}
			if (packageInfo == null){
				return false;
			}else{
				return true;
			}
		}
	}
	public static boolean checkSystemApp(Context context,String pName){
		PackageInfo packageInfo;
		if (pName == null){
			return false;
		}else{
			try{
				packageInfo = context.getPackageManager().getPackageInfo(pName,0);
			}catch (Exception e){
				packageInfo = null;
			}if (packageInfo == null){
				return false;
			}else{
				return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
			}
		}
	}
	public static void init_public_param(Context context)
	{
		int cid = 0;
		int lac = 0;
		int mcc = 0;
		int mnc = 0;
		String NTstr = "";
		String mod = "";
		String googleVersion = "";
		String batch = "";
		String softVersion = "";
		String model = "";
		String manufature = "";
		String androidVersion = "";
		String pName = "";
		String verionCode = "";
		TelephonyManager tm;
		String imei = "";
		String imsi = "";
		String lcdSize = "";
		String shareParam = "";
		GsmCellLocation gcl;

		if(context == null)
			return;
		if(!mIsFirst)
			return;

		mIsFirst = false;

		tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// get IMEI
		imei = tm.getDeviceId();
		// get imsi
		imsi = tm.getSubscriberId();

		gcl = (GsmCellLocation) tm.getCellLocation();
		if(gcl!=null){
			cid = gcl.getCid();
			lac = gcl.getLac();			
			if(!TextUtils.isEmpty(tm.getNetworkOperator())){
				mcc = Integer.valueOf(tm.getNetworkOperator().substring(0,3));
				mnc = Integer.valueOf(tm.getNetworkOperator().substring(3,5));
			}
		}

		//get Lcd Size
		DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();	
		lcdSize = Integer.toString(outMetrics.widthPixels) + "x" + Integer.toString(outMetrics.heightPixels);

		int networkType = getAvailableNetWorkType(context);
		if (networkType == ConnectivityManager.TYPE_MOBILE){
			int gprsType = tm.getNetworkType();
			if (gprsType == TelephonyManager.NETWORK_TYPE_UMTS){
				NTstr = "3g";
			}else{
				NTstr = "2g";
			}
		}else{
			NTstr = "wifi";
		}

		// get android version
		googleVersion = Build.VERSION.RELEASE;
		// batch
		batch = android.os.Build.HARDWARE;
		// SoftVersion
		softVersion = android.os.Build.DISPLAY;
		mod = (softVersion!=null)?softVersion.replaceAll(" ", "+") : "";
		model = android.os.Build.MODEL;
		manufature = android.os.Build.MANUFACTURER;
		// android version
		androidVersion = android.os.Build.VERSION.RELEASE;
		pName = context.getPackageName();
		verionCode = ""+getVersionCode(context,pName);
		shareParam = "&Batch=" + batch + "&imsi=" + imsi + "&imei=" + imei + "&nt=" + NTstr+ "&mpm=" + model.replaceAll(" ", "") + "&lcd="
				+ lcdSize + "&cid=" + cid + "&lac=" + lac + "&mcc=" + mcc + "&mnc="+ mnc +"&ver=" + verionCode
				+ "&os=android" + "&lbyver=" + androidVersion + "&mod=" + mod + "&pName=" + pName;
		mDetailsUrl = mDetailsUrl + shareParam + "&appno="; // details ui

		//Modified by zhao xiao xiang at 2013-04-18
		//mListUrl = mListUrl + shareParam + "&startnum=";  // list ui
		//mListUrl = mListUrl +"&thememodel="+ manufature+"_"+outMetrics.heightPixels+"x"+outMetrics.widthPixels+shareParam + "&startnum="; 
		//*/ Modified by Jack 20130813 for, theme list filter (e.g. thememodel=koobee_800x480_v200)
		String themeModel = manufature+"_" + Integer.toString(outMetrics.heightPixels) + "x" + Integer.toString(outMetrics.widthPixels) + "_v" + android.os.Build.getFreemeOSLongVersion().replaceAll("\\.", "") ;
		mListUrl = mListUrl +"&thememodel="+ themeModel +shareParam + "&startnum="; 
		android.util.Log.i("OnlineThemesUtils","themeModel = " + themeModel + ",List Url = " + mListUrl);
		//*/

		mSingleDownLoadImage = mSingleDownLoadImage + shareParam + "&imageId="; // image icon
		mDownLoadSuccessUrl = mDownLoadSuccessUrl + shareParam + "&appNo=";
		mDownloadAppUrl = mDownloadAppUrl + shareParam + "&appno=";

	}
	private static int getVersionCode(Context context,String pName){
		int versionCode = 0;
		try{
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
			versionCode = pinfo.versionCode;
		}catch (NameNotFoundException e){
			e.printStackTrace();
		}
		return versionCode;
	}
	public static int getAvailableNetWorkType(Context context){
		int NO_NETWORK_AVAILABLE = -1;
		int netWorkType = NO_NETWORK_AVAILABLE;
		try{
			ConnectivityManager connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connetManager == null){
				return NO_NETWORK_AVAILABLE;
			}
			NetworkInfo[] infos = connetManager.getAllNetworkInfo();
			if (infos == null){
				return NO_NETWORK_AVAILABLE;
			}
			for (int i = 0; i < infos.length && infos[i] != null; i++){
				if (infos[i].isConnected() && infos[i].isAvailable()){
					netWorkType = infos[i].getType();
					break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return netWorkType;
	}
	public static String openUrlGetResult(String url,String charSet){
		String result = "";
		InputStream is = null;
		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		URLConnection ucon = null;
		try{
			result = null;

			URL myURL = new URL(url);
			ucon = myURL.openConnection();
			ucon.setConnectTimeout(10000);
			ucon.setReadTimeout(25000);
			is = ucon.getInputStream();
			bis = new BufferedInputStream(is);
			baf = new ByteArrayBuffer(1024);
			int current = 0;

			while ((current = bis.read()) != -1){
				baf.append((byte) current);
			}
			result = EncodingUtils.getString(baf.toByteArray(), charSet);
			if (null !=result && result.equals("zero")){
				result = null;
			}
			baf.clear();
			bis.close();
			is.close();
		}
		catch (MalformedURLException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			if (baf != null)
				baf.clear();
			try {
				if (bis != null)
					bis.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static String getListViewData(String listFileName){
		String result="";
		String data = null;
		BufferedReader br=null;
		String myPath;
		String sdPath = getSDPath();
		File myFile;
		if(TextUtils.isEmpty(sdPath))
			return result;

		myPath = sdPath + mDownload_path+"download/cache/listData/";
		myFile = new File(myPath);
		if(myFile!=null && !myFile.exists()){
			myFile.mkdirs();
		}
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(myPath+listFileName)));
			while((data = br.readLine())!=null){
				result = result + data;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(br!=null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		return result;
	}
	public static void saveListViewData(String data,String listFileName){
	    if(TextUtils.isEmpty(data))
            return;
		String myPath;
		String sdPath = getSDPath();
		File myFile;
		if(TextUtils.isEmpty(sdPath))
			return;
		
		File downloadFile = new File(sdPath + mDownload_path);
		if(!downloadFile.isDirectory()){
		    downloadFile.delete();
		    downloadFile.mkdirs();
		}
		myPath = sdPath + mDownload_path +"download/cache/listData/";
		myFile = new File(myPath);
		if(myFile!=null && !myFile.exists()){
			myFile.mkdirs();
		}

		OutputStreamWriter osw=null;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(myPath+listFileName));
			if(osw == null)
				return;

			osw.write(data,0,data.length());
			osw.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally{				
			try {
				if(osw!=null)
					osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist){
			sdDir = Environment.getExternalStorageDirectory();
		}else{
			return null;
		}
		return sdDir.toString();
	}
	public static Bitmap convertFileToBitmap(String saveName){
		Bitmap myBitmap=null;
		String sdPath = getSDPath();
		if(TextUtils.isEmpty(sdPath))
			return null;
		String fileString = sdPath+ mDownload_path+"download/cache/image/" + saveName;
		File file = new File(fileString);
		if(file.exists()){
			myBitmap = BitmapFactory.decodeFile(fileString);
		}

		return myBitmap;
	}
	public static void saveBitmapToFile(String saveName,Bitmap myBitmap){
		String sdPath = getSDPath();
		File dir = new File(sdPath+ mDownload_path+"download/cache/image/");
		if(!dir.exists()){
			dir.mkdirs();
		}
		File f = new File(sdPath+ mDownload_path+"download/cache/image/" + saveName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
			myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if(fOut!=null){
				fOut.flush();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			if(fOut!=null){
				fOut.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	public static List<Map<String, Object>> splitServerDetailData(String result){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		JSONObject object;
		JSONArray images = null;
		String imageString = "";
		String file_size;
		String jsonString = null;

		if (TextUtils.isEmpty(result)){
			return list;
		}

		try{
			JSONObject jsonObject = new JSONObject(result.trim());
			jsonString = (String) jsonObject.getString("data");
		}catch (JSONException e1){
			e1.printStackTrace();
			return list;
		}

		try{
			object = new JSONObject(jsonString);
			map = new HashMap<String, Object>();
			images = object.optJSONArray("ImageList");
			for (int i = 0; i < images.length(); i++)
				imageString = imageString + images.getString(i) + ",";
			map.put("ImageList", imageString);
			map.put("Img",getImageName(object.get("icon").toString(), "1"));
			map.put("SystemSupport", object.get("MinSDKVersion"));
			map.put("Writer", object.get("Company"));
			map.put("VersionName", object.get("VersionName"));
			map.put("Description", object.get("Summary"));
			map.put("AppName", object.get("Name"));
			map.put("DownCount", object.get("download"));
			map.put("rate", object.get("rate"));
			long bytes = Long.parseLong(object.get("size").toString());
			file_size = humanReadableByteCount(bytes,true);
			map.put("bytes",bytes);
			map.put("FileSize", file_size);
			map.put("Pid", object.get("AppNo"));
			map.put("bitmap", null);
			map.put("PackageName", object.get("ApkName"));
			map.put("VersionCode", object.get("Version"));
			list.add(map);
		}
		catch (JSONException e){
			e.printStackTrace();
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		return list;
	}
	public static List<Map<String, Object>> splitServerListData(String result){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String file_size;
		if (TextUtils.isEmpty(result)){
			return list;
		}
		String jsonString = null;

		try{
			JSONObject jsonObject = new JSONObject(result.trim());
			jsonString = (String) jsonObject.getString("data");
		}
		catch (JSONException e1){
			e1.printStackTrace();
			return list;
		}
		Map<String, Object> map;
		JSONObject object;
		JSONArray array = null;
		try{
			array = new JSONArray(jsonString);
			for (int i = 0; i < array.length(); i++){
				map = new HashMap<String, Object>();

				object = array.getJSONObject(i);
				map.put("Img", getImageName((String)object.getString("logo"),"onlineThemes"));
				map.put("AppName", object.get("name"));
				map.put("Pid", object.get("no"));
				long bytes = Long.parseLong(object.get("size").toString());
				file_size = humanReadableByteCount(bytes,true);
				map.put("bytes",bytes);
				map.put("FileSize", file_size);
				map.put("VersionCode", object.get("ver"));
				map.put("bitmap", null);
				map.put("rate", object.get("rate"));
				map.put("publishtime", object.get("publishtime"));
				map.put("visiable", View.VISIBLE);
				map.put("DownCount", object.get("download"));
				map.put("PackageName", object.get("ApkName"));
				list.add(map);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}
	public static String humanReadableByteCount(long bytes, boolean si){
		int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
		return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
	}
	public static String getImageName(String logo_jason,String type){
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		JSONObject object;
		JSONArray array = null;
		try{
			array = new JSONArray(logo_jason);
			for (int i = 0; i < array.length(); i++){
				object = array.getJSONObject(i);
				if(object.get("type").equals(type)){
					return object.getString("id");
				}
			}
			return array.getJSONObject(0).getString("id");
		}catch (JSONException e){
			e.printStackTrace();
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		return null;
	}
	public static View getContentViewByLayout(Context context,int viewId){
		return LayoutInflater.from(context).inflate(context.getResources().getLayout(viewId), null) ;
	}
	public static byte[] createBitByteArray(Bitmap bitmap){
		if (null == bitmap)
			return null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		return os.toByteArray();
	}
	public static String getApkFileInfo(String apkPath, Context ctx){
		PackageInfo pinfo;
		try {
			pinfo = ctx.getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			pinfo = null;
		}
		if (pinfo == null){
			return null;
		}else{
			return pinfo.packageName;
		}
	}
	public static void AppInstall(String filePath, Context act){
		if (TextUtils.isEmpty(filePath)){
			return;
		}

		File f = null ;
		try{
			f = new File(filePath);
			if (null == f || !f.exists()){
				Toast.makeText(act, act.getResources().getString(R.string.file_no_exsit), 1000).show();
				return;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.putExtra("FromTydKeDouMarket", true);
		i.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
		act.startActivity(i);
	}

	public static boolean backgroundInstallAPK(String filePath,Context context){
		String[] args = {"pm", "install", "-r", filePath};
		String result = null;
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while((read = errIs.read()) != -1){
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while((read = inIs.read()) != -1){
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(errIs != null){
					errIs.close();
				}
				if(inIs != null){
					inIs.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			if(process != null){
				process.destroy();
			}
		}
		if(result != null && (result.endsWith("Success")||result.endsWith("Success\n")))
		{
			return true;
		}
		return false;
	}

	public static String[] getAvailableResolutionForThisDevice(Context context, String[] resolutions){
		DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
		String height = Integer.toString(outMetrics.heightPixels);
		String[] availableResolutions = new String[2];

		for(int i=0, j=0; i<resolutions.length; i++){
			if(resolutions[i].startsWith(height)){
				availableResolutions[j] = resolutions[i];
				j++;
				if(j==2){
					break;
				}
			}
		}
		return availableResolutions;
	}
	
	public static void postDownloadTimes(int themeId,int isTheme){
	       new PostDownloadTimesTask().execute(themeId,isTheme);
	    }
	    
	    public static class PostDownloadTimesTask extends AsyncTask<Integer, Void, Void>{

	        @Override
	        protected Void doInBackground(Integer... params) {
	            int themeId = params[0];
	            int isTheme = params[1];
	            JSONObject paraInfo = new JSONObject();
	            try {
	                paraInfo.put("id", themeId);

	                JSONObject jsObject = new JSONObject();
	                if(isTheme==1){
	                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_THEME_DOWNLOAD_TIMES_BY_TAG_REQ));
	                }
	                if(isTheme==0){
	                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_LOCKSCREEN_DOWNLOAD_TIMES_BY_TAG_REQ));
	                }
	                if(isTheme==2){
	                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_WALLPAPER_DOWNLOAD_TIMES_BY_TAG_REQ));
	                }

	                jsObject.put("body", paraInfo.toString());
	                String contents = jsObject.toString();
	                String url = MessageCode.SERVER_URL;
	                String result = com.freeme.themeclub.wallpaper.util.NetworkUtil.accessNetworkByPost(url, contents);

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            return null;
	        }
    }
}
