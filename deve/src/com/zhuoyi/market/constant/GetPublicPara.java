package com.zhuoyi.market.constant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;

import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.MarketUtils;

public class GetPublicPara
{

	public static void getPublicParaForPush(Context context,String cp_file,String td_file)
	{
		//td
	    String imsiStr = "";
	    DisplayMetrics outMetrics;
	    String lcdResolution;
		String td = MarketUtils.getFromAssets(context, td_file);
		String cp_id = MarketUtils.getFromAssets(context, cp_file);
	    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

	        // get IMEI
	    imsiStr = tm.getSubscriberId();
		outMetrics = context.getResources().getDisplayMetrics();
        lcdResolution = Integer.toString(outMetrics.widthPixels) + "x" + Integer.toString(outMetrics.heightPixels);
        
        MarketApplication.mChannelID = td != null? td.trim() : null;
        com.market.account.constant.Constant.CHANNEL_ID = MarketApplication.mChannelID;
        MarketApplication.mCpID = cp_id;

        return;
		//return hm;

	}

	public static int getAvailableNetWorkType(Context context)
	{

		int NO_NETWORK_AVAILABLE = -1;
		int netWorkType = NO_NETWORK_AVAILABLE;
		try
		{
			ConnectivityManager connetManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connetManager == null)
			{
				return NO_NETWORK_AVAILABLE;
			}
			NetworkInfo[] infos = connetManager.getAllNetworkInfo();
			if (infos == null)
			{
				return NO_NETWORK_AVAILABLE;
			}
			for (int i = 0; i < infos.length && infos[i] != null; i++)
			{
				if (infos[i].isConnected() && infos[i].isAvailable())
				{
					netWorkType = infos[i].getType();
					break;
				}
			}
		}
		catch (Exception e)
		{
			Log.e("getAva", "getAvailableNetWorkType exception");
			e.printStackTrace();
		}

		return netWorkType;
	}

	public static String getTotalMemory(Context context)
	{
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try
		{
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);

			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString)
			{
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		}
		catch (IOException e)
		{
		}
		return Formatter.formatFileSize(context, initial_memory);// Byte转换为KB或者MB，内存大小规格化
	}
	
	// 等等应用的版本号
	public static int getVersionCode(Context context,String pName)
	{

		int versionCode = 0;

		try
		{

			PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, 0);
			versionCode = pinfo.versionCode;

		}
		catch (NameNotFoundException e)
		{

		}
		return versionCode;
	}
	

	public static String getMyUUID(Context context){

		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);   

		final String tmDevice, tmSerial, androidId;   

		tmDevice = "" + tm.getDeviceId();  

		tmSerial = "" + tm.getSimSerialNumber();   

		androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);   

		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());   

		String uniqueId = deviceUuid.toString();


		return uniqueId;

	}
}