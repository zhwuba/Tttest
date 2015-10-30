package com.freeme.themeclub.statisticsdata;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.freeme.themeclub.R;
import com.freeme.themeclub.network.NetworkUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;


public class LocalUtil {
	public static final String STATISTIC_FILE_PATHNAME = "/.security/User_improvement/001005/";
	
	public static final String deviceUUID = getDeviceUUID();
	private static final String XM="001005";
	
	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public static final String START_ACTION_ID="1";
	public static final String CLICK_ACTION_ID="2";
	
	public static final String HOME_CLICK_AD="0001";
	public static final String HOME_CLICK_HOT="0002";
	
	public static final String THEME_CLICK_AD="0101";
	public static final String THEME_CLICK_NEWS="0102";
	public static final String THEME_CLICK_ESSENCE="0103";
	public static final String THEME_CLICK_POPULAR="0104";
	public static final String THEME_CLICK_CATEGORY="0105";
	public static final String THEME_CLICK_SHARE="0106";
	public static final String THEME_CLICK_YOULICK="0107";
	
	public static final String LOCKS_CLICK_AD="0201";
	public static final String LOCKS_CLICK_NEWS="0202";
	public static final String LOCKS_CLICK_ESSENCE="0203";
	public static final String LOCKS_CLICK_POPULAR="0204";
	public static final String LOCKS_CLICK_CATEGORY="0205";
	public static final String LOCKS_CLICK_SHARE="0206";
	public static final String LOCKS_CLICK_YOULICK="0207";
	
	public static final String WALL_CLICK_AD="0301";
	public static final String WALL_CLICK_NEWS="0302";
	public static final String WALL_CLICK_ESSENCE="0303";
	public static final String WALL_CLICK_POPULAR="0304";
	public static final String WALL_CLICK_CATEGORY="0305";
	public static final String WALL_CLICK_SHARE="0306";
	
	public static final String MINE_CLICK_SERVERTHEME="0401";
	public static final String MINE_CLICK_CAMERA="0402";
	public static final String MINE_CLICK_WALL="0403";
	
	public static String getStatisticFilePathName(){		
		return getSdCardDirectory()+STATISTIC_FILE_PATHNAME;
	}
	
	public static String getSdCardDirectory(){
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File sdcardDir = Environment.getExternalStorageDirectory();
			return sdcardDir.getAbsolutePath();
		}else {
			return null;
		}
	}
	
	public static String getCommonInfoJsonStr(Context context){
		String ret = "";
        try {          
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uuid", deviceUUID);
            jsonObject.put("v", Build.VERSION.RELEASE);
            jsonObject.put("xm", XM);
            jsonObject.put("ch", getRawData(context, R.raw.cp));   
            jsonObject.put("user_num", "");
            ret = jsonObject.toString();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return ret;
	}
	
	public static class StatisticInfo {
        public String ac_id ;
        public String op_id ; 
        public String name;       
        public long s_dt;
    }
	
	public static String infoToJsonStr(StatisticInfo info) {
		String ret = "";
		if (info == null) {
			return ret;
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ac_id", info.ac_id);
			jsonObject.put("op_id", info.op_id);
			jsonObject.put("name", info.name);
			jsonObject.put("s_dt", info.s_dt);
			ret = jsonObject.toString();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return ret;
	}
	
	public static String saveStatisticInfo(String acid,String opid,String name,long time){
		StatisticInfo mInfo = new StatisticInfo();
		mInfo.ac_id = acid;
		mInfo.op_id = opid;
		mInfo.name=name;
		mInfo.s_dt=time;
		String infoStr = infoToJsonStr(mInfo);
		return infoStr;
	}
	
	public static String startInfoToJsonStr(StatisticInfo info){
		String ret = "";
		if (info == null) {
			return ret;
		}
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("ac_id", info.ac_id);
			jsonObject.put("s_dt", info.s_dt);
			ret = jsonObject.toString();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return ret;
	}
	
	public static String getRawData(Context context, int id) {
        String td;
        InputStream is = context.getResources().openRawResource(id);
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = null;
        try {
            buffer = new byte[is.available()];
            dis.readFully(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                dis.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        td = new String(buffer).trim();
        return td;
    }
	
	public static File createFile(String path){
		File root = new File(path);
        if (root.exists() && root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().contains(deviceUUID)) {
                        return file;
                    }
                }
            }
        } else {
            root.mkdirs();
        }
        String now = DATEFORMAT.format(new Date());
        String fileName = path + deviceUUID + "_" + now;
        File tmpFile = new File(fileName);
        try {
            if (tmpFile.createNewFile()) {
                return tmpFile;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	
	public static String getDeviceUUID() {
        Object result = null, result1 = null, result2 = null;
        try {
            Class<?> classType = Class.forName("android.os.ServiceManager");
            Object invokeOperation = classType.newInstance();
            Method getMethod = classType.getMethod("getService", new Class[] {
                String.class
            });
            result = getMethod.invoke(invokeOperation, new Object[] {
                new String("TydNativeMisc")
            });

            Class<?> classType1 = Class
                    .forName("com.freeme.internal.server.INativeMiscService$Stub");
            // Object invokeOperation1 = classType1.newInstance();
            Method getMethod1 = classType1.getMethod("asInterface", new Class[] {
                IBinder.class
            });
            result1 = getMethod1.invoke(classType1, new Object[] {
                    result
                });

                Class<?> classType2 = result1.getClass();
                ;// Class.forName(result1.toString());//"com.freeme.internal.server.INativeMiscService$Stub$Proxy");//result1.getClass();
                 // Object invokeOperation2 = classType2.newInstance();
                Method getMethod2 = classType2.getMethod("getDeviceUUID", new Class[] {});
                result2 = getMethod2.invoke(result1, new Object[] {});

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result2 != null ? result2.toString() : "";
        }
}
