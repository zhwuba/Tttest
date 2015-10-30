package com.market.download.updates;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.zhuoyi.market.R;
import com.market.download.common.DownBaseInfo;
import com.market.download.common.DownloadSettings;
import com.market.download.common.InstallControl;
import com.market.download.common.SilentInstallTask;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.download.userDownload.ListenerManager;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.util.NotifyUtil;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.constant.SharedPrefDefine;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.patch.SignUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class AppUpdateManager {
    
    private static final String KEY_ECONOMIZED_BYTES = "economizeBytes";
    private static final String KEY_ECONOMIZED_MOUNTH = "economizeMonth";
    private static final String KEY_IGNORE_SP = "keyIgnore";

    public static final int MSG_REFRESH_UPDATE_SCREEN = 1;
    public static final int MSG_RESET_LIST = 2;
    public static final int MSG_OPEN_AUTO_UPDATE_SETTING = 3;
    public static final int MSG_UPDATE_INSTALLED = 4;

    private static HashMap<String, UpdateAppDisplayInfo> mUpAppDisInfoMap = new HashMap<String, UpdateAppDisplayInfo>();

    private static HashMap<String, DownloadEventInfo> mDownEventInfoMap = new HashMap<String, DownloadEventInfo>();

    private static HashMap<String, Boolean> mDownCompleteMap = new HashMap<String, Boolean>();
    
    private static HashMap<String, Boolean> mDownloadingWaitingMap = new HashMap<String, Boolean>();

    public static AppUpdateManager mSelf = null;
    
    private static String mUpdateAppsInstalling = "";

    private Context mContext;
    private static WeakReference<Handler> mHandler;

    private static Handler mAppUpdateHandler;

    public static AppUpdateManager getInstance(Context context) {
        if (mSelf == null) {
            mSelf = new AppUpdateManager(context);
        }

        return mSelf;
    }

    public static AppUpdateManager getStaticInstance() {
        return mSelf;
    }

    public AppUpdateManager(Context context) {
        mContext = context;
        mUpdateAppsInstalling = "";
        
        syncInstalledUpdateRelativeData();
    }

    
    private void syncInstalledUpdateRelativeData() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.UPDATE_APP, Context.MODE_PRIVATE);
        if (mEconomizedMonthStr == null) {
            mEconomizedMonthStr = sp.getString(KEY_ECONOMIZED_MOUNTH, "");
        }
        String currMonthStr = getCurrMonthString();
        if (!mEconomizedMonthStr.equals(currMonthStr)) {
            SharedPreferences.Editor editor = sp.edit();
            if(!mEconomizedMonthStr.equals("")) {
                clearLastMonthUpdatedInfo(mContext);
                
            }else {
                mEconomizedMonthStr = currMonthStr;
                editor.putString(KEY_ECONOMIZED_MOUNTH, mEconomizedMonthStr);
                editor.commit();
            }
        }
        
    }
    
    
    public static void setActivityHandler(Handler handler) {
        mHandler = new WeakReference<Handler>(handler);
        
    }

    public static void setAppUpdateHandler(Handler handler){
    	mAppUpdateHandler = handler;
    }
    public static void sendMsgToRefreshUpdateScreen(String pkgName) {
        if (mHandler != null && mHandler.get() != null) {
            Message msg = new Message();
            msg.what = MSG_REFRESH_UPDATE_SCREEN;
            msg.obj = pkgName;
            try {
                mHandler.get().sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // mHandler.sendEmptyMessage(MSG_REFRESH_UPDATE_SCREEN);
        }
    }

    public void sendMsgToResetList() {
        if (mHandler != null && mHandler.get() != null) {
            try {
                mHandler.get().sendEmptyMessage(MSG_RESET_LIST);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMsgToDisplayUpdateApp(){
    	if (mAppUpdateHandler != null) {
    		mAppUpdateHandler.sendEmptyMessage(MSG_RESET_LIST);
        }
    }
    

    public void downloadEventInfoChange(DownloadEventInfo eventInfo) {
        String pkgName = eventInfo.getPkgName();
        UpdateAppDisplayInfo updateInfo = getUpdateAppInfo(pkgName);
        if (updateInfo != null
                && updateInfo.getVerCode() == eventInfo.getVersionCode()) {
            if (eventInfo.getCurrState() == DownBaseInfo.STATE_CANCEL) {
                mDownEventInfoMap.remove(pkgName);
                mDownloadingWaitingMap.remove(pkgName);
                mDownCompleteMap.remove(pkgName);
                sendMsgToRefreshUpdateScreen(pkgName);
                return;
            } else {
                mDownEventInfoMap.put(pkgName, eventInfo);
                syncDownloadingWaitingMap(eventInfo);
            }
            if (eventInfo.getCurrState() != DownBaseInfo.STATE_INSTALLED) {
                sendMsgToRefreshUpdateScreen(pkgName);
            } else {
                if (!updateInfo.isInstalled()) {
                    updateInfo.installed();
                    AppUpdateManager.saveUpdatedAppInfo(mContext, updateInfo);
                    if (updateInfo.isAutoDownloaded()) {
                        AppUpdateManager.addEconomizedBytes(mContext,
                                updateInfo.getFileSize());
                    }
                    updateAppInstalled(pkgName);
                }
            }
        }
    }

    public static HashMap<String, DownloadEventInfo> getRelativeDownEventInfoMap() {
        return mDownEventInfoMap;
    }
    
    public static int getDownCompleteNum() {
        return mDownCompleteMap.size();
    }

    public static int getDownloadingWaitingNum() {
        return mDownloadingWaitingMap.size();
    }


    private static void syncDownloadingWaitingMap(DownloadEventInfo eventInfo) {
        String pkgName = eventInfo.getPkgName();
        int eventArray = eventInfo.getEventArray();
        if (eventArray == DownloadEventInfo.ARRAY_DOWNLOADING) {
            mDownloadingWaitingMap.put(pkgName, true);
        } else if (eventArray == DownloadEventInfo.ARRAY_WAITING) {
            mDownloadingWaitingMap.put(pkgName, false);
        } else {
            if (eventArray == DownloadEventInfo.ARRAY_COMPLETE) {
                if (eventInfo.getApkFile().exists()) {
                    mDownCompleteMap.put(pkgName, true);
                }else {
                    mDownCompleteMap.remove(pkgName);
                }
            }
            
            mDownloadingWaitingMap.remove(pkgName);
        }
    }

    public static void addDownloadEventInfo(DownloadEventInfo eventInfo) {
        String pkgName = eventInfo.getPkgName();
        mDownEventInfoMap.put(pkgName, eventInfo);
        syncDownloadingWaitingMap(eventInfo);
    }

    public static void saveUpdatedAppInfo(Context context,
            UpdateAppDisplayInfo updateInfo) {
        synchronized (AUTO_UP_SYNC_KEY) {
            SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(updateInfo.getPkgName(), updateInfo.getInfoStr());
            editor.commit();

            mUpAppDisInfoMap.put(updateInfo.getPkgName(), updateInfo);
        }
    }

    private static Object AUTO_UP_SYNC_KEY = new Object();

    public void saveUpdatedAppInfo(UpdateAppDisplayInfo updateInfo) {
        synchronized (AUTO_UP_SYNC_KEY) {
            SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(updateInfo.getPkgName(), updateInfo.getInfoStr());
            editor.commit();

            mUpAppDisInfoMap.put(updateInfo.getPkgName(), updateInfo);
        }
    }

    
    private static void removeUpdatedAppInfo(Context context, String pkgName) {
        synchronized (AUTO_UP_SYNC_KEY) {
            SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(pkgName);
            editor.commit();
            mUpAppDisInfoMap.remove(pkgName);
        }
    }

    
    public static void removeIgnoreApp(Context context, String pkgName) {
    	if(TextUtils.isEmpty(pkgName)) return;
    	synchronized(AUTO_UP_SYNC_KEY) {
    		  SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_IGNORE, Context.MODE_PRIVATE);
              SharedPreferences.Editor editor = sp.edit();
              String ignoreString = sp.getString(KEY_IGNORE_SP, "").trim();
              if(ignoreString.contains(pkgName)) {
            	  ignoreString = ignoreString.replace(pkgName + ",", "");
              }
              editor.putString(KEY_IGNORE_SP, ignoreString.trim());
              editor.commit();
    	}
    }
    
    
    private static void saveIgnoreApp(Context context, String pkgName) {
    	synchronized(AUTO_UP_SYNC_KEY) {
    		 SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_IGNORE, Context.MODE_PRIVATE);
             SharedPreferences.Editor editor = sp.edit();
             String ignoreString = sp.getString(KEY_IGNORE_SP, "").trim();
             ignoreString += (pkgName + ",");
             editor.putString(KEY_IGNORE_SP, ignoreString);
             editor.commit();
    	}
    }

    
    public static boolean containsIgnoreApp(Context context, String pkgName) {
    	if(TextUtils.isEmpty(pkgName)) return false;
    	synchronized (AUTO_UP_SYNC_KEY) {
    		SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_IGNORE, Context.MODE_PRIVATE);
    		String ignoreString = sp.getString(KEY_IGNORE_SP, "").trim();
    		if(ignoreString.contains(pkgName)) {
    			return true;
    		} else {
    			return false;
    		}
		}
    }
    
    /**
     * 获取忽略更新中已下载完成的app
     * @return
     */
    public static ArrayList<String> getIgnoreDownloadCompletedApp(Context context) {
    	ArrayList<String> igdownedList = new ArrayList<String>();
    	ArrayList<String> ignoreList = getIgnoreApp(context);
    	for(String packageName : mDownCompleteMap.keySet()) {
    		boolean isDownloaded = mDownCompleteMap.get(packageName);
    		if(isDownloaded && ignoreList.contains(packageName)) {
    			igdownedList.add(packageName);
    		}
    	}
    	return igdownedList;
    	
    }
    
    
    public static ArrayList<String> getIgnoreApp(Context context) {
    	synchronized(AUTO_UP_SYNC_KEY) {
   		 SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_IGNORE, Context.MODE_PRIVATE);
            String ignoreString = sp.getString(KEY_IGNORE_SP, "");
            String[] ignores = ignoreString.split(",");
            ArrayList<String> ignoreList = new ArrayList<String>();
            for(int i = 0; i < ignores.length; i++) {
            	if(!TextUtils.isEmpty(ignores[i])) {
            		ignoreList.add(ignores[i]);
            	}
            }
            return ignoreList;
    	}
    }
    
    
    public static class GroupData {
        public String groupName;
        public int groupType;
        public ArrayList<String> updateAppInfoList;
    }

    private static ArrayList<String> mUninstallInfoList = new ArrayList<String>();
    private static ArrayList<String> mUpdatedInfoList = new ArrayList<String>();
    
    private static long mAutoDownloadedSize = 0;
    
    public static long getCurrAutoDownloadSize() {
        return mAutoDownloadedSize;
    }
    
    
    public void syncInfoAfterAutoDownload(UpdateAppDisplayInfo updateInfo) {
        updateInfo.autoUpdateDownloaded();
        saveUpdatedAppInfo(updateInfo);
        mAutoDownloadedSize += updateInfo.getFileSize();
    }
    
    
    // group type must start with 0, unless will out of array;
    public static final int UNUPDATE_GROUP = 0;
    public static final int UPDATED_GROUP = 1;
    public static ArrayList<GroupData> groups = new ArrayList<GroupData>();

    public static ArrayList<String> getUninstallInfoList() {
        return mUninstallInfoList;
    }

    public static ArrayList<String> getUpdatedInfoList() {
        return mUpdatedInfoList;
    }

    
    public static ArrayList<String> getUpdateIgnoreList() {
        return getIgnoreApp(MarketApplication.getRootContext());
    }
    
    
    public static ArrayList<String> getUpdateIgnoreList(Context context) {
    	return getIgnoreApp(context);
    }
    
    
    public static void ignoreUpdateByPkgName(Context context, String pkgName) {
        
        if (mUninstallInfoList == null) return;
        String uninstallPkgname = null;
        boolean exist = false;
        for (int i = 0; i < mUninstallInfoList.size(); i++) {
            uninstallPkgname = mUninstallInfoList.get(i);
            if (pkgName.equals(uninstallPkgname)) {
                mUninstallInfoList.remove(i);
                exist = true;
            }
        }
        
		if(exist) {
			saveIgnoreApp(context, pkgName);
		} 
    }
    
    
    public static void cancelIgnoreUpdateByPkgName(Context context, String pkgName) {
    	if(containsIgnoreApp(context, pkgName)) {
    		removeIgnoreApp(context, pkgName);
    		
    		if (mUninstallInfoList == null)return;
    		String uninstallPkgname = null;
    		boolean exist = false;
            for (int i = 0; i < mUninstallInfoList.size(); i++) {
                uninstallPkgname = mUninstallInfoList.get(i);
                if (pkgName.equals(uninstallPkgname)) {
                    exist = true;
                    break;
                }
            }
    		if (!exist)
    		    mUninstallInfoList.add(pkgName);
    	}
    }

    public static void initUpdateAppInfoList(Context context) {
        if (context == null) {
            context = MarketApplication.getRootContext();
        }
//        mAutoDownloadedSize = 0;
        // mUpAppDisInfoMap.clear();
        mDownEventInfoMap.clear();
        mDownloadingWaitingMap.clear();
        mDownCompleteMap.clear();
        mUninstallInfoList.clear();
        mUpdatedInfoList.clear();
        
        groups.clear();

        GroupData unUpGroup = new GroupData();
        GroupData upedGroup = new GroupData();

        // for updated app info
        upedGroup.groupType = UPDATED_GROUP;
        upedGroup.groupName = context.getString(R.string.update_done_group_name);
        ArrayList<String> doneUpDisInfoList = new ArrayList<String>();

        // for unupdated app info
        unUpGroup.groupType = UNUPDATE_GROUP;
        unUpGroup.groupName = context.getString(R.string.update_undo_group_name);
        ArrayList<String> undoUpDisInfoList = new ArrayList<String>();

        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
        Map<String, ?> allMap = sp.getAll();

        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            UpdateAppDisplayInfo updateInfo = null;
            int verCode = 0;
            String pkgName = null;
            DownloadEventInfo eventInfo = null;
            PackageInfo pInfo = null;
            boolean isInstalled = false;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                updateInfo = new UpdateAppDisplayInfo(infoStr);
                pkgName = updateInfo.getPkgName();
                verCode = updateInfo.getVerCode();
                isInstalled = updateInfo.isInstalled();
                
                //获取已安装包信息
                try {
                    pInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                } catch (NameNotFoundException e) {
                    pInfo = null;
                }
                
                //已安装：是否又卸载掉了（恢复到出厂），是：重新放回到更新列表，否：放到更新完成列表
                if (isInstalled) {
                    if (pInfo != null && pInfo.versionCode < updateInfo.getVerCode()) {
                        updateInfo.unInstalled();
                        saveUpdatedAppInfo(context, updateInfo);
                        isInstalled = false;
                    } else {
                        doneUpDisInfoList.add(pkgName);
//                        mAutoDownloadedSize += updateInfo.getFileSize();
                    }
                }

                //未安装：该应用被卸载掉（完全卸载掉）或者安装的version code比市场拿到的高，认为此应用不需要在更新
                if (!isInstalled) {
                    if (pInfo == null || pInfo.versionCode > updateInfo.getVerCode()) {
                        eventInfo = DownloadManager.getEventInfo(context, pkgName, verCode);
                        if (eventInfo == null || eventInfo.getCurrState() != DownBaseInfo.STATE_INSTALLING) {
                            removeUpdatedAppInfo(context, pkgName);
                            removeIgnoreApp(context, pkgName);
                            continue;
                        }
                        
                    } else if (pInfo.versionCode == updateInfo.getVerCode()) {
                        continue;
                    }

                    //不在忽略更新列表
                    if(!containsIgnoreApp(context, pkgName)) {
                        undoUpDisInfoList.add(pkgName);
                        if (updateInfo.isAutoDownloaded()) {
//                            mAutoDownloadedSize += updateInfo.getFileSize();
                        }
                    
                        eventInfo = DownloadManager.getEventInfo(context, pkgName, verCode);
                        if (eventInfo != null 
                                && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_BACKGROUND
                                && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_UPDATE) {
                            addDownloadEventInfo(eventInfo);
                        } 
                    } 
                }
            }
        }

        upedGroup.updateAppInfoList = doneUpDisInfoList;
        mUpdatedInfoList = doneUpDisInfoList;

        unUpGroup.updateAppInfoList = undoUpDisInfoList;
        mUninstallInfoList = undoUpDisInfoList;

        groups.add(unUpGroup);
        groups.add(upedGroup);
    }
    
    
    private static int mUpdateAppNum = 0;
    
    public static int getUpdateAppNum() {
        return mUpdateAppNum;
    }
    
    
    public int syncUpdateAppNum() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
        Map<String, ?> allMap = sp.getAll();

        mUpdateAppNum = 0;
        mAutoDownloadedSize = 0;
        if (allMap != null) {
            Iterator iter = allMap.entrySet().iterator();
            Map.Entry entry = null;
            String infoStr = null;
            UpdateAppDisplayInfo updateInfo = null;
            int verCode = 0;
            String pkgName = null;
            DownloadEventInfo eventInfo = null;
            PackageInfo pInfo = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                infoStr = (String) entry.getValue();
                updateInfo = new UpdateAppDisplayInfo(infoStr);
                pkgName = updateInfo.getPkgName();
                verCode = updateInfo.getVerCode();

                if (!updateInfo.isInstalled()) {
                    try {
                        pInfo = mContext.getPackageManager().getPackageInfo(pkgName, 0);
                        if (pInfo.versionCode >= updateInfo.getVerCode()) {
                        	removeIgnoreApp(mContext, pkgName);
                        	continue;
                        }
                    } catch (NameNotFoundException e) {
                    	removeIgnoreApp(mContext, pkgName);
                        continue;
                    }
                    
                    if(!containsIgnoreApp(mContext, pkgName)) {
                    	mUpdateAppNum++;
                    }
                    
                }
                
                if (updateInfo.isAutoDownloaded()) {
                    mAutoDownloadedSize += updateInfo.getFileSize();
                }
            }
        }
        
        return mUpdateAppNum;
    }


    public static ArrayList<GroupData> getUpdatedAppInfoList(Context context) {
        return groups;
    }

    public void updateAppInstalled(String pkgName) {

        if (mUninstallInfoList == null) return;
        UpdateAppDisplayInfo updateInfo = getUpdateAppInfo(pkgName);
        
        String uninstallPkgname = null;
        for (int i = 0; i < mUninstallInfoList.size(); i++) {
            uninstallPkgname = mUninstallInfoList.get(i);
            if (pkgName.equals(uninstallPkgname)) {
                mUninstallInfoList.remove(i);
            }
        }

        String installedPkgname = null;
        for (int i = 0; i < mUpdatedInfoList.size(); i++) {
            installedPkgname = mUpdatedInfoList.get(i);
            if (pkgName.equals(installedPkgname)) {
                mUpdatedInfoList.remove(i);
            }
        }
        
        Util.displayNumOnLauncher(mContext, mUninstallInfoList.size());

        if (updateInfo != null) {
            mUpdatedInfoList.add(0, pkgName);
        }

        MarketApplication.removeFromUpdateList(pkgName);
        if (mHandler != null && mHandler.get() != null) {
            try {
                mHandler.get().sendEmptyMessage(MSG_UPDATE_INSTALLED);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if(mAppUpdateHandler != null){
        	mAppUpdateHandler.sendEmptyMessage(MSG_UPDATE_INSTALLED);
        }
    }
    

    public static UpdateAppDisplayInfo getUpdateAppInfo(Context context,
            String pkgName) {
        UpdateAppDisplayInfo updateInfo = mUpAppDisInfoMap.get(pkgName);
        if (updateInfo != null) {
            return updateInfo;
        }

        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
        String infoStr = sp.getString(pkgName, null);
        if (infoStr == null) {
            return null;
        } else {
            try {
                updateInfo = new UpdateAppDisplayInfo(infoStr);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return updateInfo;
    }

    public UpdateAppDisplayInfo getUpdateAppInfo(String pkgName) {
        UpdateAppDisplayInfo updateInfo = mUpAppDisInfoMap.get(pkgName);
        if (updateInfo != null) {
            return updateInfo;
        }

        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
        String infoStr = sp.getString(pkgName, null);
        if (infoStr == null) {
            return null;
        } else {
            try {
                updateInfo = new UpdateAppDisplayInfo(infoStr);
                mUpAppDisInfoMap.put(pkgName, updateInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        return updateInfo;
    }


    private static void clearLastMonthUpdatedInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP_DIS_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();

        sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.putLong(KEY_ECONOMIZED_BYTES, 0);
        editor.putString(KEY_ECONOMIZED_MOUNTH, getCurrMonthString());
        editor.commit();
    }


    private static String getCurrMonthString() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        String monthStr = Integer.toString(year) + "/"
                + Integer.toString(month);
        return monthStr;
    }

    private static String mEconomizedMonthStr = null;

    public static void addEconomizedBytes(Context context, long bytes) {
        synchronized (SharedPrefDefine.UPDATE_APP) {
            SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            
            long economizedBytes = sp.getLong(KEY_ECONOMIZED_BYTES, 0);
            economizedBytes += bytes;
            editor.putLong(KEY_ECONOMIZED_BYTES, economizedBytes);
            editor.commit();
        }
    }
    

    public static long getEconomizedBytes(Context context) {
        SharedPreferences sp = context.getSharedPreferences(SharedPrefDefine.UPDATE_APP, Context.MODE_PRIVATE);
        return sp.getLong(KEY_ECONOMIZED_BYTES, 0);
    }


    public String getInstalledApkVersionName(String pName) {
        String versionName = null;

        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(
                    pName, 0);
            versionName = pinfo.versionName;

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    
    
//    public static void installApk(File apkFile, 
//            Context context,
//            UpdateAppDisplayInfo updateInfo) {
//        DownloadEventInfo eventInfo = mDownEventInfoMap.get(updateInfo.getPkgName());
//        if (eventInfo == null) {
//            eventInfo = new DownloadEventInfo(
//                updateInfo.getPkgName(),
//                updateInfo.getAppName(), 
//                updateInfo.getMd5(),
//                updateInfo.getDownUrl(),
//                ReportFlag.TOPIC_NULL,
//                ReportFlag.FROM_UPDATE_MANA, false, true,
//                true, 
//                updateInfo.getVerCode(),
//                updateInfo.getApkId(),
//                updateInfo.getFileSize());
//            
//            mDownEventInfoMap.put(eventInfo.getPkgName(), eventInfo);
//            eventInfo.downloadComplete(context);
//        }
//        
//        installApk(apkFile, context, eventInfo, true, "update");
//    }
    

    public static void installApk(final File apkFile, 
            final Context context, 
            final DownloadEventInfo eventInfo, 
            final boolean installFg,
            final String from) {
    	
    	if (!TextUtils.isEmpty(mUpdateAppsInstalling) && mUpdateAppsInstalling.contains(eventInfo.getPkgName() + ";")) return;
    	mUpdateAppsInstalling = mUpdateAppsInstalling + eventInfo.getPkgName() + ";";

        new Thread() {
            @Override
            public void run() {
                boolean hasPermission = Util.hasInstallPermission(context);
                boolean signSame = true;
                //签名判断
                String fileSign = SignUtils.getUnInstalledApkSignature(apkFile.getAbsolutePath());
                String appSign = SignUtils.InstalledApkSignature(context, eventInfo.getPkgName());
                if (!TextUtils.isEmpty(appSign) 
                        && !TextUtils.isEmpty(fileSign) 
                        && !appSign.equals(fileSign)) {
                    signSame = false;
                }
                final boolean isSignSame = signSame;
                
                boolean bgInstall = DownloadSettings.getBgInstallFlag(context);
                if (bgInstall) {

                    if ("update".equals(from)) {
                        sendMsgToRefreshUpdateScreen(eventInfo.getPkgName());
                    } else if ("download".equals(from)) {
                        //nothing
                    } else if ("downloaded".equals(from)) {
                        //nothing
                    }
                    
                    if (!installFg) {
                        Intent it = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
                        it.putExtra("package", eventInfo.getPkgName());
                        context.sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");
                    }
                    
                    //签名不同，静默卸载
                    if (!signSame && hasPermission) {
                        Util.silentUninstallApp(context, eventInfo.getPkgName());
                    }
                    
                    //静默安装
                    if (hasPermission) {
                        
                        //静默安装成功提示、刷新；静默安装失败如果允许提示更新，弹出安装界面或者签名不同的弹出提示
                        InstallControl instalControl = InstallControl.getControl();
                        int installResult = instalControl.silentInstall(context,
                                                                        apkFile,
                                                                        new SilentInstallTask.InstallCallback() {
                            @Override
                            public void installSuccess() {
                                eventInfo.installed();
                                ListenerManager.getInstance(context).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALLED);
                                
//                                String tickerStr = context.getString( R.string.down_noti_ticker_update_success, eventInfo.getAppName());
//                                NotifyUtil.notifyTickerText(context, tickerStr);
                            }
                            
                            @Override
                            public void installFailed() {
                                eventInfo.installFailed();
                                ListenerManager.getInstance(context).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALL_FAILED);
                                
                                if (installFg) {
                                    startInstallActivity(apkFile, context, eventInfo.getPkgName(), eventInfo.getAppName(), isSignSame);
                                }
                            }
                            
                            @Override
                            public void hasInstalledYet() {
                                
                            }
                        });
                        
                        if (installResult == InstallControl.RESULT_NO_PERMISSION) {
                            eventInfo.installFailed();
                            ListenerManager.getInstance(context).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALL_FAILED);
                            
                            if (installFg) {
                                startInstallActivity(apkFile, context, eventInfo.getPkgName(), eventInfo.getAppName(), signSame);
                            }
                        } else if(installResult == InstallControl.RESULT_READY_TO_INSTALL) {
                            eventInfo.installingApk();
                            ListenerManager.getInstance(context).downInfoChanged(eventInfo, DownloadMsg.MSG_INSTALLING);
                        }
                        
                    } else {
                        if ("update".equals(from)) {
                            sendMsgToRefreshUpdateScreen(eventInfo.getPkgName());
                        } else if ("download".equals(from)) {
                            //nothing
                        } else if ("downloaded".equals(from)) {
                           //nothing
                        }
                      
                        if (installFg) {
                            startInstallActivity(apkFile, context, eventInfo.getPkgName(), eventInfo.getAppName(), signSame);
                        }
                    }
                    
                } else {
                    if (installFg) {
                        startInstallActivity(apkFile, context, eventInfo.getPkgName(), eventInfo.getAppName(), signSame);
                    }
                }

                if (!TextUtils.isEmpty(mUpdateAppsInstalling) && mUpdateAppsInstalling.contains(eventInfo.getPkgName() + ";")) {
                	mUpdateAppsInstalling = mUpdateAppsInstalling.replace(eventInfo.getPkgName() + ";", "");
                }
                
            }
        }.start();
    }
    
    
    public static void installApk(final File apkFile, 
            final Context context, 
            final String pkgName, 
            final String appName) {
    	
    	if (!TextUtils.isEmpty(mUpdateAppsInstalling) && mUpdateAppsInstalling.contains(pkgName + ";")) return;
    	mUpdateAppsInstalling = mUpdateAppsInstalling + pkgName + ";";

        new Thread() {
            @Override
            public void run() {
                boolean hasPermission = Util.hasInstallPermission(context);
                boolean signSame = true;
                    //签名判断
                String fileSign = SignUtils.getUnInstalledApkSignature(apkFile.getAbsolutePath());
                String appSign = SignUtils.InstalledApkSignature(context, pkgName);
                if (!TextUtils.isEmpty(appSign) 
                        && !TextUtils.isEmpty(fileSign) 
                        && !appSign.equals(fileSign)) {
                    signSame = false;
                }
                
                final boolean isSignSame = signSame;
                boolean bgInstall = DownloadSettings.getBgInstallFlag(context);
                if (bgInstall) {
                    
                    Intent it = new Intent("android.intent.action.ZHUOYOU_INSTALL_APK_QUIETLY");
                    it.putExtra("package", pkgName);
                    context.sendBroadcast(it, "com.zhuoyi.app.permission.INTERNEL_FLAG");
                    
                    //签名不同，静默卸载
                    if (!signSame && hasPermission) {
                        Util.silentUninstallApp(context, pkgName);
                    }
                    
                    //静默安装
                    InstallControl instalControl = InstallControl.getControl();
                    int installResult = instalControl.silentInstall(context,
                                                                    apkFile,
                                                                    new SilentInstallTask.InstallCallback() {
                        @Override
                        public void installSuccess() {
//                            String tickerStr = context.getString( R.string.down_noti_ticker_update_success, appName);
//                            NotifyUtil.notifyTickerText(context, tickerStr);
                        }
                        
                        @Override
                        public void installFailed() {
                            startInstallActivity(apkFile, context, pkgName, appName, isSignSame);
                        }
                        
                        @Override
                        public void hasInstalledYet() {
                            
                        }
                    });
                    
                    if (installResult == InstallControl.RESULT_NO_PERMISSION) {
                        startInstallActivity(apkFile, context, pkgName, appName, signSame);
                    }
                    
//                    boolean installSuccess = Util.backgroundInstallAPK(context, apkFile);
//                    
//                    //静默安装成功提示、刷新；静默安装失败如果允许提示更新，弹出安装界面或者签名不同的弹出提示
//                    if (installSuccess) {
//                        String tickerStr = context.getString( R.string.down_noti_ticker_update_success, appName);
//                        NotifyUtil.notifyTickerText(context, tickerStr);
//                    } else {
//                        startInstallActivity(apkFile, context, pkgName, appName, signSame);
//                    }
                } else {
                    startInstallActivity(apkFile, context, pkgName, appName, signSame);
                }
                
                if (!TextUtils.isEmpty(mUpdateAppsInstalling) && mUpdateAppsInstalling.contains(pkgName + ";")) {
                	mUpdateAppsInstalling = mUpdateAppsInstalling.replace(pkgName + ";", "");
                }
                
            }
        }.start();
    }
    
    
    private static void startInstallActivity(File file, 
            Context context, 
            String pkgName,
            String appName,
            boolean signSame) {
        
        boolean systemApp = MarketUtils.isSystemApp(context, pkgName);
        
        if (signSame || systemApp) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(context, AppUpdateSignCheckActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("pkg_name", pkgName);
            intent.putExtra("app_name", appName);
            intent.putExtra("file_name", file.getAbsolutePath());
            context.startActivity(intent);
        }
    }

    
    
    /**
     * 释放资源
     */
    public void releaseRes() {
//        if (mHandler != null) {
//            mHandler = null;
//        }
    }
    
}
