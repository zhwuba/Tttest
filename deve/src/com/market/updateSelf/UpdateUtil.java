package com.market.updateSelf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

public class UpdateUtil {
    public static final String TAG = "updateSelf";
    private static final int NO_NETWORK_AVAILABLE = -1;

    /**
     * 获取 sd card 路径
     * @return
     */
    public static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * 获取自更新下载文件路径
     * @return
     */
    public static String getDownloadPath() {
        return getSdcardPath() + Custom.UPDATE_DIR_PATH;
    }

    /**
     * 获取已下载文件大小
     * @param fileName
     * @return
     */
    public static long getDownloadFileSize(String fileName) {
        File downloadFile = getDownloadFile(fileName);
        return downloadFile.length();
    }

    /**
     * 获取已下载文件
     * @param fileName
     * @return
     */
    public static File getDownloadFile(String fileName) {
        File downloadFile = new File(getDownloadPath() + File.separator + fileName);
        return downloadFile;
    }

    /**
     * 获取当前网络状态
     * @param context
     * @return
     */
    private static int getCurrNetworkType(Context context) {
        int currNetType = NO_NETWORK_AVAILABLE;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.e(TAG, "getAvailableNetworkType(): connectivity manager is null");
        } else {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            if (infos == null) {
                Log.e(TAG, "getAvailableNetworkType(): network infos is null");
            } else {
                for (NetworkInfo info : infos) {
                    if (info.isConnected() && info.isAvailable()) {
                        currNetType = info.getType();
                    }
                }
            }
        }

        return currNetType;
    }

    /**
     * 判断当前网络是否可用
     * @param context
     * @return
     */
    public static boolean isCurrNetworkAvailable(Context context) {
        int currNetType = getCurrNetworkType(context);

        if (currNetType == NO_NETWORK_AVAILABLE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 判断当前网络是不是wifi
     * @param context
     * @return
     */
    public static boolean isCurrWifiAvailable(Context context) {
        int currNetType = getCurrNetworkType(context);

        if (currNetType == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取安装包信息
     * @param context
     * @param pName
     * @return
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

    /**
     * sd 卡是不是可用
     * @param context
     * @return
     */
    public static boolean isSdcardAvailable(Context context) {
        boolean result = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        Log.i(TAG, "isSdcardAvailable(): " + result);
        return result;
    }

    /**
     * 后台安装apk
     * @param file
     * @return
     */
    public static boolean backgroundInstallAPK(File file, Context context) {
        Log.i(TAG, "install apk background, file:" + file.getPath());
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
            byte[] data = baosRet.toByteArray();
            result = new String(data);
            Log.i("backgroundInstallAPK()", "install result:" + new String(baos.toByteArray()));
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
            Log.i("backgroundInstallAPK()", "install success");
            
            if (context != null)
                context.sendBroadcast(new Intent("download.refresh"));
            
            return true;
        }
        Log.i("backgroundInstallAPK()", "install failed");
        return false;
    }

    /**
     * sd 卡状态
     * @author dream.zhou
     *
     */
    public static class SdcardState {
        public static final int STATE_LOSE = 0;
        public static final int STATE_OK = 1;
        public static final int STATE_INSUFFICIENT = 2;
    }

    /**
     * 检查sd 卡状态
     * @param context
     * @param miniSize
     * @return
     */
    public static int checkSdcardIsAvailable(Context context, long miniSize) {
        Log.i(TAG, "checkSdcardIsAvailable(): miniSize = " + miniSize);
        if (!isSdcardAvailable(context)) {
            return SdcardState.STATE_LOSE;
        }

        String path = getSdcardPath();
        if (path == null) {
            return SdcardState.STATE_LOSE;
        }

        StatFs statfs = new StatFs(path);
        long blockSize = (long) statfs.getBlockSize();
        long blockCount = (long) statfs.getAvailableBlocks();
        long availableSize = blockSize * blockCount;
        if (availableSize < miniSize) {
            Log.i(TAG, "checkSdcardIsAvailable(): miniSize = " + miniSize);
            return SdcardState.STATE_INSUFFICIENT;
        }
        return SdcardState.STATE_OK;
    }

    /**
     * 获取文件md5码
     * @param fileName
     * @return
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
            for (int i = 0; i < bytes.length; i++) {
                String md5s = Integer.toHexString(bytes[i] & 0xff);
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
     * 最上层是不是需要自更新的应用
     * @return
     */
    public static boolean isSelfAppForegound(Context context) {
        String myName = context.getPackageName();
        UpdateUtil.logE(TAG, "isSelfAppForegound", "myName=" + myName);
        if (TextUtils.isEmpty(myName)) return false;
        
        if (myName.equals(getTopAppPkgName(context))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取顶层应用
     * @return
     */
    private static String getTopAppPkgName(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = mActivityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {

            return tasksInfo.get(0).topActivity.getPackageName();
        }
        return "";
    }

    /**
     * log
     * @param tag
     * @param func
     * @param msg
     */
    public static void logI(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.i(TAG, logStr);
    }

    /**
     * log
     * @param tag
     * @param func
     * @param msg
     */
    public static void logE(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.e(TAG, logStr);
    }

    /**
     * log
     * @param tag
     * @param func
     * @param msg
     */
    public static void logV(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.v(TAG, logStr);
    }
}
