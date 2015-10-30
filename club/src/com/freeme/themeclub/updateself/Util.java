package com.freeme.themeclub.updateself;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import com.freeme.themeclub.updateself.HttpManager.NewUpdateInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;


public class Util {
    public static final String TAG = "updateSelf";

    public static final String SP_UPDATE_SELF = "updateSelfSp";
    public static final String KEY_DOWNINFO_STR = "downInfoStr";
	//wifi or mobile
    public static final String KEY_LAST_NETWORK_STATE = "lastNTState";

    private static SharedPreferences mSharedPref = null;
    private static SharedPreferences mNewInfoSharedPref = null;
    public static final String SP_NEW_INFO = "newInfoSp";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_VERCODE = "versionCode";
    public static final String KEY_POLICY = "policy";
    public static final String KEY_URL = "url";
    public static final String KEY_MD5 = "md5";

    public static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getDownloadPath() {
        return getSdcardPath() + Custom.UPDATE_DIR_PATH;
    }

    public static long getDownloadFileSize(String fileName) {
        File downloadFile = getDownloadFile(fileName);
        return downloadFile.length();
    }

    public static File getDownloadFile(String fileName) {
        File downloadFile = new File(getDownloadPath() + File.separator + fileName);
        return downloadFile;
    }

    private static final int NO_NETWORK_AVAILABLE = -1;

    private static int getCurrNetworkType(Context context) {
        int currNetType = NO_NETWORK_AVAILABLE;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.w(TAG, "getAvailableNetworkType(): connectivity manager is null");
        } else {
            NetworkInfo[] infos = cm.getAllNetworkInfo();
            if (infos == null) {
                Log.w(TAG, "getAvailableNetworkType(): network infos is null");
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

    public static boolean isCurrNetworkAvailable(Context context) {
        int currNetType = getCurrNetworkType(context);

        if (currNetType == NO_NETWORK_AVAILABLE) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isCurrWifiAvailable(Context context) {
        int currNetType = getCurrNetworkType(context);

        if (currNetType == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else {
            return false;
        }
    }

    public static byte getNetworkType(Context context) {
        byte netStatus = 3;
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectMgr.getActiveNetworkInfo();

        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                // wifi network
                netStatus = 3;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int subType = info.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A) {
                    // 3G network
                    netStatus = 2;
                } else {
                    // 2G or 2.5G
                    netStatus = 1;
                }
            }
        }
        return netStatus;
    }

    public static int isNetworkTypeForMobile(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return 1;
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 2;
            }
        }
        return 0;
    }

    public static int isWifiToMobileNt(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        String mLastNTState = getLastNetworkState(context);

        if (info != null) {
            if (mLastNTState == null) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    saveLastNetworkState(context, "mobile");
                    return 1;
                }
                saveLastNetworkState(context, "wifi");
                return 0;
            } else {
                if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    if ("mobile".equals(mLastNTState))
                        saveLastNetworkState(context, "wifi");
                    return 0;
                } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if ("wifi".equals(mLastNTState)) {
                        saveLastNetworkState(context, "wifi");
                        return 1;
                    }
                    return 0;
                }
            }

        }
        return 2;
    }

    public static PackageInfo getPackageInfo(Context context, String pName) {
        PackageInfo pinfo = null;

        try {
            pinfo = context.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
            // versionCode = pinfo.versionCode;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return pinfo;
    }

    public static boolean isSdcardAvailable(Context context) {
        boolean result = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        Log.i(TAG, "isSdcardAvailable(): " + result);
        return result;
    }

    public static boolean backgroundInstallAPK(File file) {
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
            // byte[] data = baos.toByteArray();
            // result = new String(data);
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
            return true;
        }
        Log.i("backgroundInstallAPK()", "install failed");
        return false;
    }

    public static class SdcardState {
        public static final int STATE_LOSE = 0;
        public static final int STATE_OK = 1;
        public static final int STATE_INSUFFICIENT = 2;
    }

    public static int checkSdcardIsAvailable(Context context, long miniSize) {
        Log.i(TAG, "checkSdcardIsAvailable(): miniSize = " + miniSize);
        if (!isSdcardAvailable(context)) {
            return SdcardState.STATE_LOSE;
        }

        String path = getSdcardPath();
        if (path == null) {
            return SdcardState.STATE_LOSE;
        }
        // File sdcard = new File(path);
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

    public static String getFileMd5(String fileName) {
        FileInputStream fis = null;
        StringBuffer strBuff = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024 * 1024];
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

    public static void saveDownloadInfo(Context context, DownloadInfo info) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_UPDATE_SELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_DOWNINFO_STR, info.getDownloadInfoStr());
        editor.commit();
    }

    public static void saveLastNetworkState(Context context, String info) {
        if (info == null)
            return;
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_UPDATE_SELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(KEY_LAST_NETWORK_STATE, info);
        editor.commit();
    }

    public static String getLastNetworkState(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_UPDATE_SELF, Context.MODE_PRIVATE);
        String infoStr = mSharedPref.getString(KEY_LAST_NETWORK_STATE, null);
        return infoStr;
    }

    public static void clearDownloadInfo(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_UPDATE_SELF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(KEY_DOWNINFO_STR);
        editor.commit();
    }

    public static DownloadInfo getDownloadInfo(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_UPDATE_SELF, Context.MODE_PRIVATE);
        String infoStr = mSharedPref.getString(KEY_DOWNINFO_STR, null);
        if (infoStr == null) {
            return null;
        }
        return new DownloadInfo(infoStr);
    }

    public static NewUpdateInfo getNewInfo(Context context) {
        if (mNewInfoSharedPref == null)
            mNewInfoSharedPref = context.getSharedPreferences(SP_NEW_INFO, Context.MODE_PRIVATE);
        NewUpdateInfo info = new NewUpdateInfo();
        info.verCode = mNewInfoSharedPref.getInt(KEY_VERCODE, 0);
        String pkgName = context.getPackageName();
        PackageInfo pkgInfo = Util.getPackageInfo(context, pkgName);
        if (info.verCode <= pkgInfo.versionCode) {
            return null;
        }
        info.dContent = mNewInfoSharedPref.getString(KEY_CONTENT, null);
        info.dTitle = mNewInfoSharedPref.getString(KEY_TITLE, null);
        // default policy,notify user but not forced.
        info.policy = mNewInfoSharedPref.getInt(KEY_POLICY, 2);
        info.fileUrl = mNewInfoSharedPref.getString(KEY_URL, null);
        info.md5 = mNewInfoSharedPref.getString(KEY_MD5, null);
        if (info.fileUrl == null || info.md5 == null) {
            return null;
        }
        return info;
    }

    public static void saveNewInfo(Context context, NewUpdateInfo info) {
        if (mNewInfoSharedPref == null)
            mNewInfoSharedPref = context.getSharedPreferences(SP_NEW_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mNewInfoSharedPref.edit();
        editor.putString(KEY_TITLE, info.dTitle);
        editor.putString(KEY_CONTENT, info.dContent);
        editor.putInt(KEY_VERCODE, info.verCode);
        editor.putInt(KEY_POLICY, info.policy);
        editor.putString(KEY_URL, info.fileUrl);
        editor.putString(KEY_MD5, info.md5);
        editor.commit();
    }

    public static void saveNewInfo(Context context, String title, String content, int verCode, int policy,
            String fileUrl, String md5) {
        if (mNewInfoSharedPref == null)
            mNewInfoSharedPref = context.getSharedPreferences(SP_NEW_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mNewInfoSharedPref.edit();
        editor.putString(KEY_TITLE, title);
        editor.putString(KEY_CONTENT, content);
        editor.putInt(KEY_VERCODE, verCode);
        editor.putInt(KEY_POLICY, policy);
        editor.putString(KEY_URL, fileUrl);
        editor.putString(KEY_MD5, md5);
        editor.commit();
    }

    public static void logI(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.i(TAG, logStr);
    }

    public static void logE(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.w(TAG, logStr);
    }

    public static void logV(String tag, String func, String msg) {
        String logStr = "[" + tag + "]" + func + "():" + msg;
        Log.v(TAG, logStr);
    }
}
