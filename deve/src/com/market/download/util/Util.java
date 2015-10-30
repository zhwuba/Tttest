package com.market.download.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;

import com.market.featureOption.FeatureOption;
import com.zhuoyi.market.R;
import com.zhuoyi.market.badger.ShortcutBadger;
import com.zhuoyi.market.constant.SharedPrefDefine;
import com.zhuoyi.market.utils.LogHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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

public class Util {
    private static final String TAG = "tydMarket";

    
    public static boolean isBatteryStatusOKey(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        
        int level = batteryStatus.getIntExtra("level", 0);
        int scale = batteryStatus.getIntExtra("scale", 100);

        Util.log(TAG, "isBatteryStatusOKey", "status is : " + (level * 100 / scale) + "%");
        return (level * 100 / scale) > 20;
    }
    
    
    public static boolean isMemoryAvailableToDiffPath(Context context, String pkgName, long targetFileSize) {
    	boolean checkResult = false;
    	PackageManager pm = context.getPackageManager();
    	PackageInfo pkgInfo = null;
		try {
			pkgInfo = pm.getPackageInfo(pkgName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	if (pkgInfo != null) {
    		File currApkFile = new File(pkgInfo.applicationInfo.publicSourceDir);
    		long needSize = currApkFile.length() + targetFileSize;
    		ActivityManager am = (ActivityManager)context.getSystemService(Activity.ACTIVITY_SERVICE);
    		ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
    		am.getMemoryInfo(memInfo);
    		long safeMemSize = memInfo.availMem - memInfo.threshold;
    		if (safeMemSize > needSize) {
    			checkResult = true;
    		}
    	}
    	
    	return checkResult;
    }
    
    
    /**
     * return {@link PackageInfo} of the application
     * 
     * @param context
     * @param pName
     *            the package name of application
     * @return {@link PackageInfo}
     */
    public static PackageInfo getPackageInfo(Context context, String pName) {
        PackageInfo pinfo = null;

        try {
            pinfo = context.getPackageManager().getPackageInfo(pName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return pinfo;
    }

    
    public static boolean hasInstallPermission(Context context) {
    	boolean hasPermission = true;
    	ApplicationInfo appInfo = context.getApplicationInfo();
    	if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
  	          && (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
    		hasPermission = context.checkCallingOrSelfPermission("android.permission.INSTALL_PACKAGES") == PackageManager.PERMISSION_GRANTED ;
    	}
  	    
    	return hasPermission;
    }
    
    
    /**
     * install apk file background, it will not pop the install view for user,
     * it's invisible for user
     * 
     * @param context
     * @param file
     *            the apk {@link File} witch you want to installed
     * @return return true if install success, unless return false
     */
    public static boolean backgroundInstallAPK(Context context, File file) {
        if (!hasInstallPermission(context)) {
            log(TAG, "backgroundInstallAPK", "install apk background, no permission, return false");
            return false;
        }

        log(TAG, "backgroundInstallAPK", "install apk background, file:" + file.getPath());
        String[] args = { "pm", "install", "-r", file.getPath() };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosRet = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baosRet.write(read);
                baos.write(read);
            }
            // byte[] data = baos.toByteArray();
            // result = new String(data);
            byte[] data = baosRet.toByteArray();
            result = new String(data);
            log(TAG, "backgroundInstallAPK()", "install result:" + new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && result.startsWith("Success")) {
            log(TAG, "backgroundInstallAPK()", "install success");
            
            if (context != null)
                context.sendBroadcast(new Intent("download.refresh"));
            
            return true;
        }
        log(TAG, "backgroundInstallAPK()", "install failed");
        return false;
    }
    
    
    
    public static boolean silentUninstallApp(Context context, String pkgName) {
        if (!hasInstallPermission(context)) {
            log(TAG, "silentUninstallApp", "no permission, return false");
            return false;
        }

        log(TAG, "silentUninstallApp", "uninstall app silent, package name:" + pkgName);
        String[] args = { "pm", "uninstall", pkgName };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosRet = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
//            errIs = process.getErrorStream();
//            while ((read = errIs.read()) != -1) {
//                baos.write(read);
//            }
//            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baosRet.write(read);
//                baos.write(read);
            }
            // byte[] data = baos.toByteArray();
            // result = new String(data);
            byte[] data = baosRet.toByteArray();
            result = new String(data);
//            logI(TAG, "backgroundInstallAPK", "install result:" + new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && result.startsWith("Success")) {
            log(TAG, "silentUninstallApp", "install success");
            return true;
        }
        log(TAG, "silentUninstallApp", "install failed");
        return false;
    }

    
    /**
     * return the md5 string of the file
     * 
     * @param fileName
     *            the file path witch you want to compute it's md5 string, must
     *            be the absolute path of the file.
     * @return md5 string of the file
     */
    public static String getFileMd5(String fileName) {
        FileInputStream fis = null;
        StringBuffer strBuff = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[10 * 1024];
            int length = -1;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return null;
            }
            String md5s = null;
            for (int i = 0; i < bytes.length; i++) {
                md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s.length() == 1) {
                    strBuff.append("0");
                }
                strBuff.append(md5s);
            }
            return strBuff.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
    /**
     * get application label name
     * @param context
     * @param pkgName
     * @return application label name
     */
    public static String getAppName(Context context, String pkgName) {
        PackageManager packageManager = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            PackageInfo pkgInfo = packageManager.getPackageInfo(pkgName, 0);
            return pkgInfo.applicationInfo.loadLabel(packageManager).toString();
            
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    
    public static boolean isAppExistInHandsetNow(Context context, String pkgName) {
    	PackageManager packageManager = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            PackageInfo pkgInfo = packageManager.getPackageInfo(pkgName, 0);
            if (pkgInfo == null) {
            	return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }  
            
    }
    
    
    public static boolean isApkInstalledYet(Context context, File apkFile) {
        PackageManager pm = context.getPackageManager();
        String filePath = apkFile.getAbsolutePath();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, 0);
        if (packageInfo == null) {
            return false;
        }
        int installedVerVode = 0;
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(packageInfo.packageName, 0);
            installedVerVode = pkgInfo.versionCode;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (packageInfo.versionCode > installedVerVode) {
            return false;
        } else {
            return true;
        }
    }
    
    
    /**
     * get the apk file's version code
     * 
     * @param context
     * @param apkfile the apk file which you want to known the version code
     * @return The version code of apk file
     */
    public static int getVersionCode(Context context, File apkfile) {
        PackageManager pm = context.getPackageManager();
        String filePath = apkfile.getAbsolutePath();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath, 0);
        if (packageInfo == null) {
            return -1;
        }

        log(TAG, "getVersionCode", "apk file: " + filePath + ", version code is " + packageInfo.versionCode);
        return packageInfo.versionCode;
    }
    
    
    /**
     * check the apk file is available or not
     * @param mContext
     * @param apkFile
     * @param verCode
     * @return is apk file is not available, return false, unless return true
     */
    public static boolean isApkFileUsable(Context mContext, File apkFile, int verCode) {
        PackageInfo pkgInfo = mContext.getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
        if (pkgInfo == null) {
            return false;
        }

        if (pkgInfo.versionCode == verCode) {
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * get current uuid
     * @return
     */
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
			e.printStackTrace(); 
		}
		
		return result2!=null?result2.toString():""; 
	}
    
    
    /**
     * display number in application icon on launcher
     * @param context
     * @param number
     */
    public static void displayNumOnLauncher(Context context, int number) {
    	android.provider.Settings.System.putInt(context.getContentResolver(), "com_android_marketHD_mtk_unread", number);
    	
    	ShortcutBadger scBadger = ShortcutBadger.getBadgerImpl(context);
    	if(scBadger != null) {
    		scBadger.addBadge(number);
    	}
	}
    
    
    public static boolean getCampaignsNotifyFlag(Context context) {
    	SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.COMPAIGNS, Context.MODE_PRIVATE);
    	return sp.getBoolean("LauncherDisFlag", false);
    }
    
    
    public static void setCompaignsNotifyFlag(Context context, boolean flag) {
    	SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.COMPAIGNS, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = sp.edit();
    	editor.putBoolean("LauncherDisFlag", flag);
    	editor.commit();
    }
    
    
    public static long getHandsetRomSize() {
		//ROM
		File romPath = Environment.getDataDirectory();
		StatFs stat = new StatFs(romPath.getPath());
		long blockSize = stat.getBlockSize();
		long blockCount = stat.getBlockCount();
		long totalBytes = blockSize * blockCount;
		return totalBytes / (1024 * 1024);
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
	
	
	private static int mMarketSignal = 0;		//0 means not initialized
	
	
	public static int getMarketSignal(Context context) {
		if (mMarketSignal != 0) {
			return mMarketSignal;
		}
		int signal = 2;		//means it's a external market version
		boolean internalFlag = false;
		try {
			InputStream is = context.getAssets().open("internalFlag");
			BufferedReader br = new BufferedReader(
                    new InputStreamReader(is));
            String line = br.readLine();
            if (line != null && line.equals("true") ) {
            	internalFlag = true;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean isTydProduc = isTydProduction();
		if (isTydProduc && internalFlag) {
			signal = 1;		//means it's a internal market version
		} else if (isTydProduc && !internalFlag) {
			signal = 3;		//means it's a external market version but installed in tyd device
		}
		
		mMarketSignal = signal;
		return mMarketSignal;
	}
	
	
	private static boolean isTydProduct = false;
	private static boolean hasGetTydProduct = false;
	
	
	private static boolean isTydProduction() {
		if (hasGetTydProduct) {
			return isTydProduct;
		}
		try {
			Class<?> classType = Class.forName("android.os.SystemProperties");
			Method getMethod = classType.getDeclaredMethod("get", String.class);
			String value = (String) getMethod.invoke(classType, "ro.build.tyd.production");
			if (value != null && value.length() > 0) {
				isTydProduct = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		hasGetTydProduct = true;
		return isTydProduct;
	}

    
    /**
     * get download manager activity intent
     * 
     * @return return {@link Intent} without any extra
     */
    public static Intent getDownloadActivityIntent() {
        return new Intent("com.zhuoyi.newMarket.downloadActivity");
    }


    
    /**
     * if FeatureOption.DOWNLOAD_LOG is true, print log, unless do nothing
     * 
     * @param tag
     *            log tag, signal the module witch print log
     * @param func
     *            witch function print this log
     * @param msg
     *            log message
     */
    public static void log(String tag, String func, String msg) {
    	LogHelper.downloadTrace(tag, func, msg);
    }

    public static int getWaterFlowLayoutId()
	{
        if (FeatureOption.WATER_FLOW) {
    		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? R.layout.app_universal_item
    				: R.layout.home_list_item_type05;
		} else {
		    return R.layout.home_list_item_type05;
		}
	}
}
