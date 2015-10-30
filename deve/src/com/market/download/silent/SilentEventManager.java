package com.market.download.silent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zhuoyi.market.constant.Constant;
import com.market.download.common.DownloadSettings;
import com.market.download.common.RunTask;
import com.market.download.common.TaskThread;
import com.market.download.common.DownloadSettings.WifiDownConfig;
import com.market.download.httpConnect.HttpConnect;
import com.market.download.util.NetworkType;
import com.market.download.util.Util;
import com.market.net.data.AppInfoBto;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.statistics.ReportFlag;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

/**
 * for 705, 706 event
 * @author Athlon
 *
 */
public class SilentEventManager {
    private static final String TAG = "SilentEventManager";
	
    private Context mContext;
    
    private ConcurrentHashMap<String, Event705706Info> m705706EventMap;
    
    private ConcurrentHashMap<String, Event705706Task> m705706TaskMap;
    
    private SilentStorage mSilentStorage;
    
    private TaskThread m705706TaskThread;
    
    private Thread705706Callback mThread705706Cb;
    private Task705706Callback mTask705706Cb;
    
    private ConcurrentHashMap<String, SilentDownEventInfo> mSilentEventMap;
    private ConcurrentHashMap<String, SilentDownEventInfo> mInstalledSilentMap;
    private ConcurrentHashMap<String, SilentDownEventTask> mSilentTaskMap;
    
    private TaskThread mSilentTaskThread;
    
    private SilentTaskCallback mSilentTaskCb;
    private SilentThreadCallback mSilentThreadCb;
    
    
    private static SilentEventManager mSelf = null;
    
    public static SilentEventManager getInstance(Context context) {
    	if (mSelf == null) {
    		mSelf = new SilentEventManager(context);
    	}
    	
    	return mSelf;
    }
    
    
    public SilentEventManager (Context context) {
        mContext = context;
        mSilentStorage = new SilentStorage(mContext);
        m705706EventMap = mSilentStorage.getAll705706EventInfo();
        mThread705706Cb = new Thread705706Callback();
        mTask705706Cb = new Task705706Callback();
        m705706TaskMap = new ConcurrentHashMap<String, Event705706Task>();
        
        mSilentEventMap = new ConcurrentHashMap<String, SilentDownEventInfo>();
        mInstalledSilentMap = new ConcurrentHashMap<String, SilentDownEventInfo>();
        mSilentTaskMap = new ConcurrentHashMap<String, SilentDownEventTask>();
        mSilentTaskCb = new SilentTaskCallback();
        mSilentThreadCb = new SilentThreadCallback();
        
        initSilentEventMap();
    }
    
    
    private void new705706Event(AppInfoBto infoBto) {
        String pkgName = infoBto.getPackageName();
        
        boolean available = isNeedToUninstallLocal(pkgName, infoBto.getIsForcedUp(), infoBto.getVersionCode(), infoBto.getMd5());
        
        if (available) {
        	Event705706Info silentInfo = null;
            if (m705706EventMap.containsKey(pkgName)) {
                silentInfo = m705706EventMap.get(pkgName);
                if (silentInfo.getVersionCode() == infoBto.getVersionCode()) {
                    return;
                }
            }
            
            String from = "unKnown";
            if (infoBto.getIsForcedUp() == 3) {
                from = ReportFlag.FROM_705;
            } else if (infoBto.getIsForcedUp() == 4) {
                from = ReportFlag.FROM_706;
            }
            
            silentInfo = new Event705706Info(infoBto, from);
            mSilentStorage.save705706EventInfo(silentInfo);
            m705706EventMap.put(pkgName, silentInfo);
        }
    }
    
    
    /**
     * for 705 706 function
     * @param pkgName
     * @param isForcedUp
     * @param verCode
     * @param md5
     * @return
     */
    private boolean isNeedToUninstallLocal(String pkgName, int isForcedUp, int verCode, String md5) {
        PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            if (pkgInfo != null && appInfo != null) {
                if (isForcedUp == 3) {     //means 705, un-install same version
                    if (verCode == pkgInfo.versionCode) {
                        String installMd5 = Util.getFileMd5(appInfo.publicSourceDir);
                        if (installMd5 != null && !installMd5.equals(md5)) {
                            return true;
                        }
                    }
                    
                } else if (isForcedUp == 4) {      //means 706, un-install high version
                    if (verCode < pkgInfo.versionCode ) {
                    	return true;
                    	
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    public void init705706AppList(final List<AppInfoBto> silentList) {
    	int serverUpdateFlag = DownloadSettings.getUpdateAutoFlag(mContext);
    	if (serverUpdateFlag == 0) {
    		return;
    	}
    	
        new Thread() {
            public void run() {
                AppInfoBto infoBto = null;
                for (int i=0; i < silentList.size(); i++) {
                    infoBto = silentList.get(i);
                    new705706Event(infoBto);
                }
            }
        }.start();
    }
    
    
    public boolean startDownload705706Event() {
    	if (!Util.hasInstallPermission(mContext)) {
    		Util.log(TAG, "startDownload705706Event", "not install and uninstall permission, do nothing");
    		return false;
    	}
    	
    	int serverUpdateFlag = DownloadSettings.getUpdateAutoFlag(mContext);
    	if (serverUpdateFlag == 0) {
    	    Util.log(TAG, "startDownload705706Event", "switch is off");
    		return false;
    	}
    	
    	if (m705706TaskThread != null && m705706TaskThread.isThreadAlive()) {
    	    Util.log(TAG, "startDownload705706Event", "is downloading now, do not start again");
    	    return true;
    	} else if (!Util.isBatteryStatusOKey(mContext)) {
    	    Util.log(TAG, "startDownload705706Event", "battery is lower than 20%");
            return false;
        }
    	
    	boolean startFlag = false;
        if (NetworkType.isWifiAvailable(mContext)) {
            Iterator iter = m705706EventMap.entrySet().iterator();
            Map.Entry entry = null;
            Event705706Info silentInfo = null;
            String pkgName = null;
            Event705706Task silentTask = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                silentInfo = (Event705706Info)entry.getValue();
                pkgName = silentInfo.getPkgName();
                if (!m705706TaskMap.containsKey(pkgName)) {
                    if (m705706TaskMap.size() >= 2) {
                        break;
                    }
                    m705706TaskMap.put(pkgName, new Event705706Task(mContext, mTask705706Cb, silentInfo));
                } else {
                    silentTask = m705706TaskMap.get(pkgName);
                    if (!silentTask.get705706Info().getMd5().equals(silentInfo.getMd5())) {
                        silentTask.invalidateTask();
                        if (m705706TaskMap.size() >= 2) {
                            break;
                        }
                        m705706TaskMap.put(pkgName, new Event705706Task(mContext, mTask705706Cb, silentInfo));
                    }
                }
            }
                    
            if (m705706TaskMap.size() > 0) {
                startFlag = true;
                if (m705706TaskThread == null || !m705706TaskThread.isThreadAlive()) {
                    m705706TaskThread = new TaskThread(mThread705706Cb);
                    m705706TaskThread.start();
                }
            }
        }
        
        return startFlag;
    }
    
    
    private class Task705706Callback implements Event705706Task.TaskCallback {
        @Override
        public void remove705706Info(Event705706Info silentInfo) {
            m705706EventMap.remove(silentInfo.getPkgName());
            mSilentStorage.remove705706EventInfo(silentInfo);
        }

        @Override
        public void save705706Info(Event705706Info silentInfo) {
            mSilentStorage.save705706EventInfo(silentInfo);
        }

        @Override
        public boolean isNeedToDo705706Event(Event705706Info silentInfo) {
            return isNeedToUninstallLocal(silentInfo.getPkgName(),
                                          silentInfo.getSilentFlag(),
                                          silentInfo.getVersionCode(),
                                          silentInfo.getMd5());
        }
        
    };
    
    
    private class Thread705706Callback implements TaskThread.ThreadCallback {
        @Override
        public RunTask getTopRunTask() {
            Iterator iter = m705706TaskMap.entrySet().iterator();
            if (iter.hasNext() && Util.isBatteryStatusOKey(mContext)) {
                Map.Entry entry = (Map.Entry) iter.next();
                Event705706Task silentTask = (Event705706Task)entry.getValue();
                return silentTask;
            } else {
                return null;
            }
        }
    
        @Override
        public void threadFinished(TaskThread downThread) {
            //do nothing now
        }
    
        @Override
        public void watchDog(TaskThread downThread) {
            //do nothing now
        }

        @Override
        public void removeTopRunTask() {
            Iterator iter = m705706TaskMap.entrySet().iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }
    };
    
    
    
    /**
     * start code for silent download apk and install
     */
    
    private void initSilentEventMap() {
    	new Thread() {
    		public void run() {
    			mSilentStorage.initAllSilentDownEventInfo(mSilentEventMap, mInstalledSilentMap);
    		}
    	}.start();
    }
    
    
    public void requestSilentEventList() {
    	new Thread() {
    		public void run() {
    			ArrayList<SilentDownEventInfo> eventArray = getWifiDownEventArray();
                if (eventArray == null || eventArray.size() <= 0) {
                    // TBD later
                    return;
                }
                
                SilentDownEventInfo sInfo = null;
                SilentDownEventInfo memInfo = null;
            	for (int i=0; i < eventArray.size(); i++) {
            		sInfo = eventArray.get(i);
            		if (!Util.isAppExistInHandsetNow(mContext, sInfo.getPkgName())) {
            			memInfo = mInstalledSilentMap.get(sInfo.getPkgName());
            			if (memInfo != null) {
            				continue;
            			}
            			
            			mSilentEventMap.put(sInfo.getPkgName(), sInfo);
            			mSilentStorage.saveSilentEventInfo(sInfo);
            		} else {
            			memInfo = mSilentEventMap.get(sInfo.getPkgName());
            			if (memInfo != null) {
            				mSilentEventMap.remove(memInfo.getPkgName());
            				mSilentStorage.removeSilentEventInfo(memInfo);
            			}
            		}
            	}
    		}
    	}.start();
    }
    
    
    private ArrayList<SilentDownEventInfo> getWifiDownEventArray() {
        // String[] addressList =
        // com.zhuoyi.market.utils.Util.getAccessNetworkAddress(mContext);
        String content = null;
        try {
            content = HttpConnect.doPost(Constant.MARKET_URL, getWifiDownArrayRequestContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (content == null) {
            return null;
        }
        Util.log(TAG, "getWifiDownEventArray", "responce content:" + content);
        return parserWifiQueryData(content);
    }
    
    
    private ArrayList<SilentDownEventInfo> parserWifiQueryData(String result) {
        JSONObject jsonObject, bodyJSONObject;
        String bodyResult = "";
        ArrayList<SilentDownEventInfo> infoArray = new ArrayList<SilentDownEventInfo>();
        if (TextUtils.isEmpty(result))
            return null;

        try {
            jsonObject = new JSONObject(result);
            bodyResult = jsonObject.getString("body");
            bodyJSONObject = new JSONObject(bodyResult);
            if (bodyJSONObject.has("errorCode") && (bodyJSONObject.getInt("errorCode") == 0 || bodyJSONObject.getInt("errorCode") == 2)) {
                JSONObject configJo = bodyJSONObject.getJSONObject("config");
                int listVer = configJo.getInt("versoin");
                boolean enable = configJo.getBoolean("enable");
                int enableDays = configJo.getInt("enableTime");
                long firstGetTime = DownloadSettings.setFirstGetUpdateListTime(mContext);
                long enableMillis = firstGetTime + enableDays * 24 * 60 * 60 * 1000;
                if (enable && enableMillis > System.currentTimeMillis()) {
                	enable = false;
                }
                
                int expire = configJo.getInt("expire");
                boolean fgflag = false;
                String execTime = null;
                if (configJo.has("exectime")) {
                    execTime = configJo.getString("exectime");
                }
                if (configJo.has("fgflag")) {
                    fgflag = configJo.getBoolean("fgflag");
                }

                DownloadSettings.setWifiDownConfig(mContext, listVer, enable, execTime, expire, fgflag);

                JSONArray appsJa = configJo.getJSONArray("apps");
                JSONObject appInfoJo = null;
                String appPkgName = null;
                String appApkName = null;
                String appDownUrl = null;
                String appMd5 = null;
                int appVerCode = 0;
                int appId = 0;
                int downNetType = 3;		//1->2G; 2->3G;	3->wifi; 4->4G; 5->5G
                for (int i = 0; i < appsJa.length(); i++) {
                    appInfoJo = appsJa.getJSONObject(i);
                    appPkgName = appInfoJo.getString("pName");
                    appApkName = appInfoJo.getString("name");
                    appDownUrl = appInfoJo.getString("downUrl");
                    appMd5 = appInfoJo.getString("md5");
                    appVerCode = appInfoJo.getInt("verCode");
                    appId = appInfoJo.getInt("appId");
                    downNetType = appInfoJo.getInt("downNet");
                    // int downType = appInfoJo.getInt("downType");
                    infoArray.add(new SilentDownEventInfo(appPkgName, appApkName, appDownUrl, appVerCode, appMd5, appId, downNetType, 0));
//                    infoArray.add(new DownloadEventInfo(appPkgName, appApkName,
//                            appMd5, appDownUrl, ReportFlag.TOPIC_NULL,
//                            ReportFlag.FROM_BACRGROUND_DOWN, true, true,
//                            false, appVerCode, appId));
                }
            }
            return infoArray;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWifiDownArrayRequestContent() {
        JSONObject contentJO = new JSONObject();

        // header
        String headerStr = null;
        JSONObject headerObject = new JSONObject();
        UUID uuid = UUID.randomUUID();
        try {
            headerObject.put("ver", 1);
            headerObject.put("type", 1);
            headerObject.put("msb", uuid.getMostSignificantBits());
            headerObject.put("lsb", uuid.getLeastSignificantBits());
            headerObject.put("mcd", MessageCode.GET_SILENT_DOWNLOAD_REQ);
            headerStr = headerObject.toString();
            contentJO.put("head", headerStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (headerStr == null) {
            return null;
        }

        // body
        String bodyStr = null;

        String marketId = com.zhuoyi.market.utils.MarketUtils.getSharedPreferencesString(mContext, com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
        if (TextUtils.isEmpty(marketId)) {
            marketId = "null";
        }

        try {
        	JSONObject jsonObjBody = new JSONObject(SenderDataProvider.generateTerminalInfo(mContext).toString());
        	
            jsonObjBody.put("marketId", marketId);
            WifiDownConfig wifiConfig = DownloadSettings.getWifiDownConfig(mContext);
            jsonObjBody.put("version", wifiConfig.version);
            bodyStr = jsonObjBody.toString();
            contentJO.put("body", bodyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (bodyStr == null) {
            return null;
        }

        return contentJO.toString();
    }
    
    
    public boolean startSilentDownload() {
    	if (!Util.hasInstallPermission(mContext)) {
    		Util.log(TAG, "startSilentDownload", "no install permission, do nothing");
    		return false;
    	}
    	
    	if (mSilentEventMap.size() == 0) {
    		Util.log(TAG, "startSilentDownload", "no silent event to do");
    		return false;
    	}
    	
    	if (mSilentTaskThread != null && mSilentTaskThread.isThreadAlive()) {
    	    Util.log(TAG, "startSilentDownload", "silent downloading now");
    	    return true;
    	} else if (!Util.isBatteryStatusOKey(mContext)) {
            Util.log(TAG, "startSilentDownload", "battery is lower than 20%");
            return false;
        }
    	
    	boolean startFlag = false;
        Iterator iter = mSilentEventMap.entrySet().iterator();
        Map.Entry entry = null;
        SilentDownEventInfo silentInfo = null;
        String pkgName = null;
        SilentDownEventTask silentTask = null;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            silentInfo = (SilentDownEventInfo)entry.getValue();
            if (isNetTypeOkForSilentEvent(silentInfo)) {
                pkgName = silentInfo.getPkgName();
                if (!mSilentTaskMap.containsKey(pkgName)) {
                    if (mSilentTaskMap.size() >= 2) {
                        break;
                    }
                    mSilentTaskMap.put(pkgName, new SilentDownEventTask(mContext, mSilentTaskCb, silentInfo));
                    startFlag = true;
                }
            }
        }
                
        if (mSilentTaskMap.size() > 0) {
            if (mSilentTaskThread == null || !mSilentTaskThread.isThreadAlive()) {
                mSilentTaskThread = new TaskThread(mSilentThreadCb);
                mSilentTaskThread.start();
//                PowerChangeReceiver.regPowerChangeReceiver(mContext, PowerChangeReceiver.REG_FLAG_SILENT_DOWN);
            }
        }
        
        return startFlag;
    }
    
    
    private boolean isNetTypeOkForSilentEvent(SilentDownEventInfo silentInfo) {
    	boolean netTypeOk = false;
    	int currNetType = NetworkType.getNetworkType(mContext);
    	if (currNetType == NetworkType.WIFI) {
    		netTypeOk = true;
    		
    	} else {
    		int downNetType = silentInfo.getDownloadNetwork();
	    	if (downNetType != NetworkType.WIFI && currNetType >= downNetType) {
	    			netTypeOk = true;
	    	}
    	}
    	
    	return netTypeOk;
    }
    
    
    public void pauseSilentDownload() {
    	mSilentTaskMap.clear();
    	if (mSilentTaskThread != null) {
	    	mSilentTaskThread.stopThread();
	    	SilentDownEventTask task = (SilentDownEventTask)mSilentTaskThread.getCurrRunTask();
	    	if (task != null) {
	    		task.invalidateTask();
	    	}
    	}
    }
    
    
    private class SilentTaskCallback implements SilentDownEventTask.TaskCallback {

		@Override
		public void removeSilentInfo(SilentDownEventInfo silentInfo) {
			mSilentEventMap.remove(silentInfo.getPkgName());
			mSilentStorage.removeSilentEventInfo(silentInfo);
		}

		@Override
		public void saveSilentInfo(SilentDownEventInfo silentInfo) {
			mSilentStorage.saveSilentEventInfo(silentInfo);
			if (silentInfo.hasInstalled()) {
				mInstalledSilentMap.put(silentInfo.getPkgName(), silentInfo);
				mSilentEventMap.remove(silentInfo.getPkgName());
			}
		}
    	
    }
    
    
    private class SilentThreadCallback implements TaskThread.ThreadCallback {

		@Override
		public RunTask getTopRunTask() {
			Iterator iter = mSilentTaskMap.entrySet().iterator();
            if (iter.hasNext() && Util.isBatteryStatusOKey(mContext)) {
                Map.Entry entry = (Map.Entry) iter.next();
                SilentDownEventTask silentTask = (SilentDownEventTask)entry.getValue();
                return silentTask;
            } else {
                return null;
            }
		}

		@Override
		public void removeTopRunTask() {
			Iterator iter = mSilentTaskMap.entrySet().iterator();
            if (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
		}

		@Override
		public void threadFinished(TaskThread downThread) {
//			PowerChangeReceiver.unRegPowerChangeReceiver(mContext, PowerChangeReceiver.REG_FLAG_SILENT_DOWN);
		}

		@Override
		public void watchDog(TaskThread downThread) {
			
		}
    	
    }
}
