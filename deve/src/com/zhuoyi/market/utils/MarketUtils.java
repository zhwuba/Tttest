package com.zhuoyi.market.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.market.download.common.DownloadSettings;
import com.market.download.common.InstallControl;
import com.market.download.common.SilentInstallTask;
import com.market.download.updates.AppUpdateManager;
import com.market.download.util.NotifyUtil;
import com.market.download.util.Util;
import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.WebActivity;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.appResident.SettingData;
import com.zhuoyi.market.appdetail.AppDetailInfoActivity;
import com.zhuoyi.market.topic.TopicInfoActivity;
import com.zhuoyi.system.util.Logger;

public class MarketUtils {
    public static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
        'E', 'F' };
    public static final int LESS_ITEM_NUM = 9;
    public static final int TWO_ROW_NUM = 3;
    public static final String KEY_ENTER_MARKET_COUNT = "enter_market_count";
    public static final String KEY_NEW_MARKET = "new_market";
    public static final String KEY_MARKET_ID = "market_id";
    public static final String KEY_GET_IMAGE_NEXT = "next_time";
    public static final String KEY_EXIT_MARKET = "exitTip";
    public static final String KEY_DISCOVER_DOT = "DiscoverDot";
    public static final long TIME_OF_ON_DAY = 24 * 60 * 60 * 1000;
    public static final long TIME_NEXT_UPDATE_VERSION_GAP = 1 * TIME_OF_ON_DAY;
    
    public static int mRedDotCount = 0;
    
    private static final ExecutorService mDataReqExecutor = Executors.newFixedThreadPool(5, new MyThreadFactory());
    private static final ExecutorService mImgReqExecutor = Executors.newFixedThreadPool(10, new MyThreadFactory());
    private static final long TIME_NEXT_UPDATE_STARTUP_IMAGE_GAP = 1 * TIME_OF_ON_DAY;
    private static boolean isWifiOpen = false;


    public static long getNextUpdateVersionTime(Context context, String key) {
        return getSharedPreferencesLong(context, key, -1);
    }


    public static void setNextUpdateVersionTime(Context context, String key, long value) {
        setSharedPreferences(context, key, value);
    }


    public static void setSharedPreferencesString(Context context, String key, String value) {
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putString(key, value);
        editor.commit();
    }


    public static String getSharedPreferencesString(Context context, String key, String defaultValue) {
        String ret = "";
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        ret = sprefs.getString(key, defaultValue);
        return ret;
    }

    
    public static void setDiscoverSPInt(Context context, String key, int value) {
        SharedPreferences sprefs = context.getSharedPreferences(KEY_DISCOVER_DOT, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }


    public static int getDiscoverSPInt(Context context, String key, int defaultValue) {
        int ret = -1;
        SharedPreferences sprefs = context.getSharedPreferences(KEY_DISCOVER_DOT, 0);
        ret = sprefs.getInt(key, defaultValue);
        return ret;
    }
    
    
    public static void setDiscoverSPBoolean(Context context, String key, boolean value) {
        SharedPreferences sprefs = context.getSharedPreferences(KEY_DISCOVER_DOT, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    public static boolean getDiscoverSPBoolean(Context context, String key, boolean defaultValue) {
        boolean ret = false;
        SharedPreferences sprefs = context.getSharedPreferences(KEY_DISCOVER_DOT, 0);
        ret = sprefs.getBoolean(key, defaultValue);
        return ret;
    }
    

    private static void setSharedPreferences(Context context, String key, long value) {
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }


    /**
     * {method description}.
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    private static long getSharedPreferencesLong(Context context, String key, long defaultValue) {
        long ret = -1;
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        ret = sprefs.getLong(key, defaultValue);
        return ret;
    }


    public static void setNextUpdateTime(Context context, String key, long value) {
        long next_time = value + TIME_NEXT_UPDATE_STARTUP_IMAGE_GAP;
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putLong(key, next_time);
        editor.commit();
    }


    public static long getNextUpdateTime(Context context, String key) {
        long ret = -1;
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        ret = sprefs.getLong(key, -1);
        return ret;
    }


    public static void setUpdateStartupImageFileName(Context context, String value) {
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putString("image_file_name", value);
        editor.commit();
    }


    public static String getUpdateStartupImageFileName(Context context) {
        String ret = "";
        SharedPreferences sprefs = context.getSharedPreferences(KEY_NEW_MARKET, 0);
        ret = sprefs.getString("image_file_name", "");
        return ret;
    }


    public static final class FileManage {
        Folder root;


        public Folder getRoot() {
            return root;
        }


        public void setRoot(Folder root) {
            this.root = root;
        }


        public void initRoot() {
            root = new Folder();
            root.name = "sdcard";
            // root.name = "/";
            root.path = getSDPath();
            // root.path = "/";
        }


        /**
         * 得到sdcard的路径
         * 
         * @return
         */
        public static String getSDPath() {
            File sdDir = null;
            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存�?
            if (sdCardExist) {
                sdDir = Environment.getExternalStorageDirectory();// 获取根目录
            } else {
                return null;
            }
            return sdDir.toString();
        }


        public static FileHolder readSDCardSpace() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                StatFs sf = new StatFs(sdcardDir.getPath());
                long blockSize = sf.getBlockSize();
                long blockCount = sf.getBlockCount();
                long availCount = sf.getAvailableBlocks();

                return new FileHolder(blockSize * blockCount, blockSize * availCount);
            }

            return null;
        }


        void readSystem() {
            File root = Environment.getRootDirectory();
            StatFs sf = new StatFs(root.getPath());
            long blockSize = sf.getBlockSize();
            long blockCount = sf.getBlockCount();
            long availCount = sf.getAvailableBlocks();
        }


        /**
         * 得到文件的大小
         * 
         * @param path
         * @return
         */
        public static int getFileSize(String path) {
            File f = new File(path);
            if (f.exists()) {
                return (int) f.length() / 1024;
            }
            return 0;
        }


        /**
         * 遍历root 下的所有文件和文件�?装入root 为根节点的双向链表中
         * 
         * @param root
         */
        public void traversal(Folder root) {
            File dir = new File(root.path);
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            } else {
                for (int i = 0; i < files.length; i++) {
                    Folder root1 = new Folder();
                    root1.path = files[i].getAbsolutePath();
                    root1.name = files[i].getName();
                    root1.prefolder = root;
                    if (files[i].isDirectory()) {// 文件夹
                        root1.isfolder = true;
                        traversal(root1);
                        root.list.add(root1);
                    } else if (isMusicFile(root1.path)) {// 如果是音乐文�?加入
                        root1.isfolder = false;
                        root1.size = this.getFileSize(root1.path);

                        root.list.add(root1);
                    }

                }
            }
        }


        /**
         * 寻找 path �?文件�?
         * 
         * @param root
         * @param path
         * @return
         */
        public static Folder searchPath(Folder root, String path) {

            if (root.getPath().equals(path)) {
                return root;
            } else {
                ArrayList<Folder> folderlist = root.getList();
                for (int i = 0; i < folderlist.size(); i++) {
                    Folder f = searchPath(folderlist.get(i), path);
                    if (f != null) {
                        break;
                    }
                }
            }
            return null;
        }

        public static class FileHolder {// B 为单位
            public FileHolder(long totalSpace, long availSpace) {
                this.totalSpace = totalSpace;
                this.availSpace = availSpace;
            }

            public long totalSpace;
            public long availSpace;
        }


        public static void newPathFolder(String folderPath) {
            int start1 = 0;
            int start2 = folderPath.indexOf("/");

            while (start2 > -1) {
                String s = folderPath.substring(0, start2);
                newFolder(s);
                start1 = start2;
                start2 = folderPath.indexOf("/", start1 + 1);
            }
            if (start1 < folderPath.length() - 1) {
                newFolder(folderPath);
            }
        }


        // 新建一个文件夹
        static public void newFolder(String folderPath) {
            if (folderPath == null || folderPath.equals("")) {
                return;
            }
            try {
                String filePath = folderPath;
                File myFilePath = new File(filePath);
                if (!myFilePath.exists()) {
                    boolean flag = myFilePath.mkdir();
                } else {
                    Log.v("mytag", "folderPath is exists " + folderPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        static public boolean delFolder(String path) {
            delAllFileInFolder(path);
            return delFile(path);
        }


        /**
         * 删除文件
         */
        static public boolean delFile(String path) {
            File delPath = new File(path);
            if (delPath.isFile()) {
                return delPath.delete();
            } else {
                return false;
            }
        }


        /**
         * 删除文件夹里面的所有文件
         */
        static public void delAllFileInFolder(String path) {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }
            if (!file.isDirectory()) {
                return;
            }
            String[] tempList = file.list();
            File temp = null;
            for (int i = 0; i < tempList.length; i++) {
                if (path.endsWith(File.separator)) {
                    temp = new File(path + tempList[i]);
                } else {
                    temp = new File(path + File.separator + tempList[i]);
                }
                if (temp.isFile()) {
                    temp.delete();
                }
                if (temp.isDirectory()) {
                    delAllFileInFolder(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                    delFile(path + "/" + tempList[i]);// 再删除空文件夹
                }
            }
        }

        final static public String[] MusicEndWith = { ".mp3", ".midi", ".mid", ".rm", ".wma" };


        public boolean isMusicFile(String path) {
            for (int i = 0; i < MusicEndWith.length; i++)
                if (path.endsWith(MusicEndWith[i]))
                    return true;
            return false;
        }
    }

    public static final class Folder {
        // 本身
        String name;// folder name
        String path;// folder path
        int size;// K
        boolean isfolder = true;// true >folder false >file
        ArrayList<Folder> list = new ArrayList();
        Folder prefolder;


        // 新建文件夹
        public Folder() {
        }


        public Folder(String path) {
            this.path = path;
        }


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        public String getPath() {
            return path;
        }


        public void setPath(String path) {
            this.path = path;
        }


        public boolean isIsfolder() {
            return isfolder;
        }


        public void setIsfolder(boolean isfolder) {
            this.isfolder = isfolder;
        }


        public ArrayList<Folder> getList() {
            return list;
        }


        public void setList(ArrayList<Folder> list) {
            this.list = list;
        }


        public Folder getPrefolder() {
            return prefolder;
        }


        public void setPrefolder(Folder prefolder) {
            this.prefolder = prefolder;
        }


        public int getSize() {
            return size;
        }


        public void setSize(int size) {
            this.size = size;
        }
    }

    
    /**
     * {获得手机上已经安装的应用}
     *  <br>
     * Create on : 2015-6-11 上午11:54:41<br>
     * @author pc<br>
     * @version zhuoyiStore v0.0.1
     * 
     */
    public static final class AppInfo {
        public int versionCode = 0;
        public String appname = "";
        public String packagename = "";
        public String versionName = "";
        public Drawable appicon = null;
        public String localPath;
        public float length;
    }

    public static final class AppInfoManager {

        final public static int type_system = 0;
        final public static int type_download = 1;
        final public static int type_all = 2;

        Context contextwrapper;


        public AppInfoManager(Context contextwrapper) {
            this.contextwrapper = contextwrapper;
        }

        ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
        List<PackageInfo> packages;


        public ArrayList<AppInfo> getAppInfo(int type) {
            if (packages == null)
                packages = contextwrapper.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                AppInfo tmpInfo = new AppInfo();
                tmpInfo.appname = packageInfo.applicationInfo.loadLabel(contextwrapper.getPackageManager()).toString();
                tmpInfo.packagename = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appicon = packageInfo.applicationInfo.loadIcon(contextwrapper.getPackageManager());

                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                switch (type) {
                case type_system:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                        appList.add(tmpInfo);
                    }
                    break;
                case type_download:
                    if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        appList.add(tmpInfo);
                    }
                    break;
                case type_all:
                    appList.add(tmpInfo);
                    break;
                }
            }
            return appList;
        }


        // 根据文件路径安装 app;
        public static void AppInstall(String filePath, Context act, String apkName, String appName) {

            if (TextUtils.isEmpty(filePath)) {
                return;
            }

            if (filePath.contains("/kedou/")) {
                filePath = filePath.replace("/kedou/", "/ZhuoYiMarket/");
            }

            File f = null;
            try {
                f = new File(filePath);
                if (null == f || !f.exists()) {
                    Toast.makeText(act, act.getString(R.string.file_no_exist), Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (TextUtils.isEmpty(apkName)) {
                apkName = getApkFilePackageName(act, filePath);
            }

            if (!TextUtils.isEmpty(apkName)) {
                Intent it = new Intent("android.intent.action.INSTALL_APK_QUIETLY");
                it.putExtra("package", apkName);
                act.sendBroadcast(it);
                
                boolean isUpdateEvent = Util.isAppExistInHandsetNow(act, apkName);
                if (isUpdateEvent && !TextUtils.isEmpty(appName)) {
                    AppUpdateManager.installApk(f, act, apkName, appName);
                    return;
                } 
                
                if (!TextUtils.isEmpty(appName)) {
                    boolean bgInstall = DownloadSettings.getBgInstallFlag(act);
                    if (bgInstall) {
                        final Context context = act;
                        final File file = f;
                        final String fappName = appName;
                        final String fapkName = apkName;
                        
                        Intent quietIntent = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
                        quietIntent.putExtra("package", fapkName);
                        context.sendBroadcast(quietIntent, "com.zhuoyi.app.permission.INTERNEL_FLAG");
                        
                        InstallControl instalControl = InstallControl.getControl();
                        int result = instalControl.silentInstall(act,
                                                                 file,
                                                                 new SilentInstallTask.InstallCallback() {
                            @Override
                            public void installSuccess() {
//                                String tickerStr = context.getString( R.string.down_noti_ticker_update_success, fappName);
//                                NotifyUtil.notifyTickerText(context, tickerStr);
                            }

                            @Override
                            public void installFailed() {
                                Intent i = new Intent();
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                i.setAction(android.content.Intent.ACTION_VIEW);
                                i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                                context.startActivity(i);
                            }

                            @Override
                            public void hasInstalledYet() { }
                        });
                        
                        if (result == InstallControl.RESULT_NO_PERMISSION) {
                            Intent i = new Intent();
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setAction(android.content.Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            context.startActivity(i);
                        }
                        
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                
//                                Intent it = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
//                                it.putExtra("package", fapkName);
//                                context.sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");
//                                
//                                boolean installSuccess = Util.backgroundInstallAPK(context, file);
//                                if (installSuccess) {
//                                    String tickerStr = context.getString( R.string.down_noti_ticker_update_success, fappName);
//                                    NotifyUtil.notifyTickerText(context, tickerStr);
//                                } else {
//                                    Intent i = new Intent();
//                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    i.setAction(android.content.Intent.ACTION_VIEW);
//                                    i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//                                    context.startActivity(i); 
//                                }
//                            }
//                        }.start();
                        return;
                    }
                }
            }

            Intent i = new Intent();
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.putExtra("FromTydKeDouMarket", true);
            i.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
            act.startActivity(i);
        }


        public static void AppUnInstall(String packagePath, Activity act) {
            Uri packageURI = Uri.parse("package:" + packagePath);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            act.startActivity(uninstallIntent);
        }

    }

    public static final class ToastManager {

        public static void show(Context context, String text) {
            try {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean checkInstalled(String pName, Context contex) {
        if (pName == null) {
            return false;
        } else {
            try {
                contex.getPackageManager().getInstallerPackageName(pName);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }


    public static boolean unInstallSilent(String packageName) {
        boolean result = true;
        try {
            Class<?> pmService;
            Class<?> activityTherad;
            Method method;

            activityTherad = Class.forName("android.app.ActivityThread");
            Class<?> paramTypes[] = getParamTypes(activityTherad, "getPackageManager");
            method = activityTherad.getMethod("getPackageManager", paramTypes);
            Object PackageManagerService = method.invoke(activityTherad);

            pmService = PackageManagerService.getClass();

            Class<?> paramTypes1[] = getParamTypes(pmService, "deletePackage");
            method = pmService.getMethod("deletePackage", paramTypes1);
            method.invoke(PackageManagerService, packageName, null, 0);
        } catch (ClassNotFoundException e) {
            result = false;
            e.printStackTrace();
        } catch (SecurityException e) {
            result = false;
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            result = false;
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            result = false;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            result = false;
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            result = false;
            e.printStackTrace();
        }

        return result;
    }
    
    
    public static boolean uninstallAPK(String pkg) {
		String[] args = { "pm", "uninstall", pkg };
		String result = execCommand(args);

		if (result != null&& (result.endsWith("Success") || result.endsWith("Success\n"))) {
			return true;
		} else {
			return false;
		}
	}
    

	private static String execCommand(String[] args) {
		String result = null;
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
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
		return result;
	}



    public static boolean backgroundInstallAPK(String filePath, Context context) {
        String[] args = { "pm", "install", "-r", filePath };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        ;
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
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

        if (result != null && (result.endsWith("Success") || result.endsWith("Success\n"))) {
            
            if (context != null)
                context.sendBroadcast(new Intent("download.refresh"));
            
            return true;
        }
        return false;
    }


    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }


    public static String signatureMD5(Signature[] signatures) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            if (signatures != null) {
                for (Signature s : signatures)
                    digest.update(s.toByteArray());
            }
            return toHexString(digest.digest());
        } catch (Exception e) {
            return "";
        }
    }


    public static PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        // Workaround for https://code.google.com/p/android/issues/detail?id=9151#c8
        try {
            Class packageParserClass = Class.forName("android.content.pm.PackageParser");
            Class[] innerClasses = packageParserClass.getDeclaredClasses();
            Class packageParserPackageClass = null;
            for (Class innerClass : innerClasses) {
                if (0 == innerClass.getName().compareTo("android.content.pm.PackageParser$Package")) {
                    packageParserPackageClass = innerClass;
                    break;
                }
            }
            Constructor packageParserConstructor = packageParserClass.getConstructor(String.class);
            Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, String.class,
                DisplayMetrics.class, int.class);
            Method collectCertificatesMethod = packageParserClass.getDeclaredMethod("collectCertificates",
                packageParserPackageClass, int.class);
            Method generatePackageInfoMethod = packageParserClass.getDeclaredMethod("generatePackageInfo",
                packageParserPackageClass, int[].class, int.class, long.class, long.class);
            packageParserConstructor.setAccessible(true);
            parsePackageMethod.setAccessible(true);
            collectCertificatesMethod.setAccessible(true);
            generatePackageInfoMethod.setAccessible(true);

            Object packageParser = packageParserConstructor.newInstance(archiveFilePath);

            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();

            final File sourceFile = new File(archiveFilePath);

            Object pkg = parsePackageMethod.invoke(packageParser, sourceFile, archiveFilePath, metrics, 0);
            if (pkg == null) {
                return null;
            }

            if ((flags & android.content.pm.PackageManager.GET_SIGNATURES) != 0) {
                collectCertificatesMethod.invoke(packageParser, pkg, 0);
            }

            return (PackageInfo) generatePackageInfoMethod.invoke(null, pkg, null, flags, 0, 0);
        } catch (Exception e) {
            Log.e("Signature Monitor", "android.content.pm.PackageParser reflection failed: " + e.toString());
        }

        return null;
    }


    /**
     * {method description}.
     * @param context
     * @param apkFile
     * @return
     */
    public static String getApkSignatureByFilePath(Context context, String apkFile) {
        String result = "";
        PackageInfo newInfo = getPackageArchiveInfo(apkFile, PackageManager.GET_ACTIVITIES
            | PackageManager.GET_SIGNATURES);
        if (newInfo != null) {
            if (newInfo.signatures != null && newInfo.signatures.length > 0) {
                // return newInfo.signatures[0];
                result = signatureMD5(newInfo.signatures);
                return result;
            }
        }
        return result;
    }


    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(16);
    }


    public static int getApkFileVersionCode(Context context, String apk_path) {
        PackageManager pm = context.getPackageManager();

        PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
        if (packageInfo == null)
            return 0;
        return packageInfo.versionCode;
    }


    public static String getInstalledPackageSignatureMD5(String packageName, Context context) {
        String result = null;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            if (info != null)
                result = signatureMD5(info.signatures);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String getFromAssets(Context context, String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null)
                result += line;

            inputReader.close();
            bufReader.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getCountSpeedInfo(float mCurV) {
        String s = "";
        String scurV = "";
        mCurV = mCurV * 1000;
        if (mCurV > 0) {
            if (mCurV < 1024) {
                scurV = new DecimalFormat("#.00").format(mCurV);
                scurV += " B/s";
            } else if (mCurV < 1024 * 1024) {
                float curV1 = (float) (mCurV / (1024.00));
                scurV = new DecimalFormat("#.00").format(curV1);
                scurV += " KB/s";
            } else {
                float curV1 = (float) (mCurV / (1024.00 * 1024));
                scurV = new DecimalFormat("#.00").format(curV1);
                scurV += " MB/s";
            }
        } else {
            scurV += "0.00 KB/s";
        }

        s = "[ " + scurV + " ]";
        return s;
    }


    public static Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;

        Method[] mtd = cls.getMethods();

        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }
            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }


    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "");
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }


    public static int getAPNType(Context context) {
        int netType = -1;
        if (context == null) {
            return netType;
        }
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String netString = networkInfo.getExtraInfo();
            Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is " + networkInfo.getExtraInfo());
            if (netString == null) {
                return netType;
            } else if (netString.equalsIgnoreCase("cmnet") || netString.equalsIgnoreCase("uninet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }


    public static String getAppName(Context context) {
        String appName = "";

        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
                PackageManager.GET_CONFIGURATIONS);
            appName = pi.applicationInfo.loadLabel(context.getPackageManager()).toString();
            if (appName == null || appName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {

        }
        return appName;
    }


    public static String getMD5(String str) {
        MessageDigest md5 = null;
        String info = str + "LW5wFncdp!-i";
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        char[] charArray = info.toCharArray();
        byte[] byteArray = new byte[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }


    public static void startActivityFromStartUpAd(Context context, AppInfoBto appInfo, boolean isPassInstall) {
        if (context == null || appInfo == null)
            return;

        Intent intent = null;
        String webUrl = appInfo.getWebUrl();
        if (!TextUtils.isEmpty(webUrl)) {
            intent = new Intent(context, WebActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("wbUrl", webUrl);
            intent.putExtra("titleName", appInfo.getName());
            context.startActivity(intent);
        } else {
            if (appInfo.getResType() == 2) {
                intent = new Intent(context, TopicInfoActivity.class);
                intent.putExtra("mCID", appInfo.getRefId());
                intent.putExtra("mTopicName", appInfo.getName());
                intent.putExtra("mTopicInfo", appInfo.getBriefDesc());
                intent.putExtra("mTopicImage", "HTTP_AD_" + appInfo.getImgUrl().hashCode());
                intent.putExtra("imageUrl", appInfo.getImgUrl());
                intent.putExtra("version_code", appInfo.getVersionCode());
                intent.putExtra("app_integral", appInfo.getIntegral());
                intent.putExtra("isPassInstall", isPassInstall);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else if (appInfo.getResType() == 1) {
                startAppDetailActivity(context, appInfo, ReportFlag.FROM_ENTRY_AD, -1);
            } else {
            }
        }
    }


    public static void startAppDetailActivity(Context context, AppInfoBto appInfo, String fromPath, int topicId) {

        if (appInfo == null || context == null)
            return;

        String activityUrl = appInfo.getActivityUrl();
        if (activityUrl != null && !activityUrl.equals("")) {
            activityUrl = activityUrl + "?apk_id=" + appInfo.getRefId() + "&activity_id="
                + appInfo.getCornerMarkInfo().getType();
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startDetailActivity(context, appInfo.getRefId(), fromPath, topicId, activityUrl);
    }


    public static void startDetailActivity(Context context, int refId, String fromPath, int topicId, String activityUrl) {
        if (context != null) {
            Intent intent = new Intent(context, AppDetailInfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("refId", refId);
            intent.putExtra("from_path", fromPath);
            intent.putExtra("topicId", topicId);
            intent.putExtra("fromInner", true);
            intent.putExtra("activity_url", activityUrl);
            context.startActivity(intent);
        }
    }


    public static void startAppDetailActivity(Context context, AppInfoBto appInfo, String fromPath) {
        startAppDetailActivity(context, appInfo, fromPath, -1);
    }


    public static int getTopicId(int channelIndex, int topicIndex) {
        int topicId = -1;
        try {
            topicId = MarketApplication.getMarketFrameResp().getChannelList().get(channelIndex).getTopicList()
                .get(topicIndex).getTopicId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topicId;
    }


    /**
     * {截取imageUrl中作为key的那部分}.
     * @param imgUrl 图片的下载地址
     * @return 除掉域名和端口号的部分
     */
    public static String getImgUrlKey(String imgUrl) {
        if (imgUrl == null) {
            return null;
        }
        int index = imgUrl.indexOf("img");
        if (index != -1) {
            return imgUrl.substring(index);
        } else {
            return imgUrl;
        }
    }


    /**
     * {RGB_565:5+6+5=16，图形的参数应该由两个字节来表示,应该是一种16位的位图}.
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap read565Bitmap(Context context, int resId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Config.RGB_565;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opts);
    }


    public static Bitmap read565Bitmap(Context context, String path) {
        File file;
        FileInputStream fis = null;
        Bitmap myBitmap = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (TextUtils.isEmpty(path))
                return null;
            file = new File(path);
            if (file != null && file.exists()) {
                try {
                    fis = new FileInputStream(file);

                    BitmapFactory.Options opt = new BitmapFactory.Options();

                    opt.inPreferredConfig = Bitmap.Config.RGB_565;

                    opt.inPurgeable = true;

                    opt.inInputShareable = true;

                    myBitmap = BitmapFactory.decodeStream(fis, null, opt);

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    System.gc();
                    myBitmap = null;
                    e.printStackTrace();
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return myBitmap;
    }
    
    
    //以下代码是原AppStoreUtils中的代码
    public static void setWifiState(boolean isOpen) {
        isWifiOpen = isOpen;
    }


    public static boolean isNoPicModelReally() {
        return !isWifiOpen & SettingData.mNoShowImage;
    }


    public static ExecutorService getDataReqExecutor() {
        return mDataReqExecutor;
    }


    public static ExecutorService getImgReqExecutor() {
        return mImgReqExecutor;
    }

    
    public static class MyThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setPriority(Thread.MAX_PRIORITY);
            return t;
        }
    }


    public static int getInstalledApkVersionCode(Context context, String pName) {
        int versionCode = 0;

        if (context == null)
            return versionCode;

        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
            versionCode = pinfo.versionCode;

        } catch (NameNotFoundException e) {
            // e.printStackTrace();
        }
        return versionCode;
    }


    public static void openCurrentActivity(Context context, String pName) {
        PackageManager pm = context.getPackageManager();
        Intent launch_activity;
        launch_activity = new Intent();
        launch_activity = pm.getLaunchIntentForPackage(pName);
        if (launch_activity != null)
            context.startActivity(launch_activity);
    }


    public static int dipToPixels(Context context, float dip) {
        final float SCALE = context.getResources().getDisplayMetrics().density;
        float valueDips = dip;
        int valuePixels = (int) (SCALE * valueDips);
        return valuePixels;
    }


    public static boolean isEqualsVersionCode(Context context, String versionCode, String pName) {
        boolean result = false;
        int code = 0;
        int installed_apk_code = 0;
        if (!TextUtils.isEmpty(versionCode)) {
            code = Integer.parseInt(versionCode);
        } else return result;

        installed_apk_code = getInstalledApkVersionCode(context, pName);

        if (code <= installed_apk_code)
            result = true;

        return result;
    }


    /**
     * {method description}.
     * @param context
     * @return
     */
    public static int getSaveModeTipCount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("saveMode", Activity.MODE_PRIVATE);
        return sharedPreferences.getInt("tipCount", 0);
    }


    public static void setSaveModeTipCount(Context context, int count) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("saveMode", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("tipCount", count);
        editor.commit();
    }


    public static String getApkFilePackageName(Context context, String apk_path) {
        PackageManager pm = context.getPackageManager();

        PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, PackageManager.GET_ACTIVITIES);
        if (packageInfo == null)
            return null;
        return packageInfo.packageName;

    }


    public static boolean checkInstalled(Context context, String pName) {
        if (pName == null) {
            return false;
        } else {
            try {
                context.getPackageManager().getPackageInfo(pName, 0);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }


    public static boolean checkApkShouldShowInList(Context context, String pName, int versionCode) {

        if (context == null || pName == null) {
            return false;
        }
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, 0);
            if (pinfo == null)
                return false;
            if (versionCode <= pinfo.versionCode) {
                return false;
            } else {
                return true;
            }

        } catch (NameNotFoundException e) {
            return true;
        }
    }


    public static String getInstalledApkVersionName(Context context, String pName) {

        if (context == null || pName == null) {
            return null;
        }
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, 0);
            if (pinfo == null)
                return null;
            return pinfo.versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }
    
    
    public static boolean isSystemApp(Context context, String packageName) {
  	  PackageInfo packageInfo;
  	try {
  		packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
  		return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
  	} catch (NameNotFoundException e) {
  		e.printStackTrace();
  	}
  	return false;
    }  
    
    
    public static String formatTime(long time) {
  	    String result = "" + time;
  	    try {
  	      if (time > 0) {
  	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  	        Date date = new Date(time);
  	        result = sdf.format(date);
  	      }
  	    } catch (Throwable e) {
  	      Logger.p(e);
  	    }
  	    return result;
  	  }
    
    
    public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
    }
    
    
    public static void setSatusBarTranslucent(Activity activity) {
    	if(VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
    		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    		//activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
    
    
    public static void setTitleLayout(LinearLayout baseView, Context context) {
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
        	if (baseView.getVisibility() != View.VISIBLE) {
        		baseView.setVisibility(View.VISIBLE);
        	}
	        int statusBarHeight = MarketUtils.getStatusBarHeight(context);
	        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
	        View statusBarView = new View(context);
	        statusBarView.setTag("statusBarView");
	        baseView.addView(statusBarView, params);
        }
    }
    
    
    public static void setBaseLayout(View baseView, Context context) {
    	if(VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
    		int statusBarHeight = getStatusBarHeight(context);
    		baseView.setPadding(baseView.getPaddingLeft(), baseView.getPaddingTop() + statusBarHeight, baseView.getPaddingRight(), baseView.getPaddingBottom());
		}
    }

}
