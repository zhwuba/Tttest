package com.market.behaviorLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.market.download.service.DownloadService;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.NetworkType;
import com.market.featureOption.FeatureOption;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class LogManager {
	private static final String TAG = "logManager";
	
	//official server address
	private static final String UPLOAD_ADDRESS = "http://clog-market.yy845.com:2590/upload";
	//test server address
	//private static final String UPLOAD_ADDRESS = "http://192.168.0.81:2004/upload";
	//xiao dan local test server address
	//private static final String UPLOAD_ADDRESS = "http://192.168.3.15:8080/marketLog/upload";
	
	private static final long MIN_SHOW_TIME = 1000;    //1 second
	
	private HashMap<String, JSONObject> mLogMap;
	private HashMap<String, JSONObject> mLogCache;
	
	private HashMap<String, Long> mActivityEntryMap;
	
	private boolean isUploadLogNow = false;
	
	private LogStorage mLogStorage;
	
	private Context mContext;
	
	private String mLastActivityDes = null;
	
	private boolean isEntryAdNow = false;
	
	public LogManager (Context context) {
	    mContext = context;
	    
		mLogCache = new HashMap<String, JSONObject>();
		
		mActivityEntryMap = new HashMap<String, Long>();
		
		mLogStorage = LogStorage.getInstance(mContext);
		
		mLogMap = mLogStorage.initBehaviorLog();
	}
	
	
	public void syncCacheMap() {
	    mLogMap = mLogCache;
        isUploadLogNow = false;
        mLogCache = new HashMap<String, JSONObject>();
        mLogStorage.saveBehaviorLogs(mLogMap);
        
        sendMsgToStopService();
	}
	
	
	
	/*
	 * start coding for activity visit log
	 */
	
	public void activityEntry(String key, long millis) {
		mActivityEntryMap.put(key, millis);
		mLastActivityDes = key;
	}
	
	
	public void activityExit(String key, long millis) {
		Long entryMillis = mActivityEntryMap.remove(key);
		if (entryMillis == null) {
			return;
		}
		long showTime = millis - entryMillis;
		if (showTime > MIN_SHOW_TIME && !isEntryAdNow) {
    		if (isUploadLogNow) {
    		    recordActivityShowLog(key, showTime, mLogCache);
    			//uploading now do not saveCache
    			
    		} else {
    		    JSONObject jo = recordActivityShowLog(key, showTime, mLogMap);
    		    mLogStorage.saveBehaviorLog(key, jo);
    		    
    		}
		}
	}
	
	
	public void entryAdExit(long millis) {
	    if (!isEntryAdNow) {
	        return;
	    }
	    isEntryAdNow = false;
	    if (mLastActivityDes != null) {
	        mActivityEntryMap.put(mLastActivityDes, millis);
	    }
	}
	
	
	private JSONObject recordActivityShowLog(String key, long showTime, HashMap<String, JSONObject> map) {
	    JSONObject jo = map.get(key);
	    if (jo == null) {
	        jo = new JSONObject();
	        
	        try {
                jo.put(LogDefined.KEY_ACTION, LogDefined.AC_TYPE_ACTIVITY);
                jo.put(LogDefined.KEY_ACTIVITY_DES, key);
                jo.put(LogDefined.KEY_SHOW_COUNT, 1);
                jo.put(LogDefined.KEY_SHOW_TIME, showTime);
                map.put(key, jo);
            } catch (Exception e) {
                e.printStackTrace();
            }
	        
	    } else {
            int count = 0;
            long time = 0;
            try {
                count = jo.getInt(LogDefined.KEY_SHOW_COUNT);
                time = jo.getLong(LogDefined.KEY_SHOW_TIME);
            } catch (Exception e) {
                //do not print exception
            }
            count ++;
            time += showTime;
            try {
                jo.put(LogDefined.KEY_SHOW_COUNT, count);
                jo.put(LogDefined.KEY_SHOW_TIME, time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
	    }
        
        return jo;
	}
	
	
	
	/*
     * start coding for view show and click log
     */
	
	private static final int VIEW_SHOW_ACTION = 1;
	private static final int VIEW_CLICK_ACTION = 2;
	
	public void viewShow(String key) {
	    if (key.contains(LogDefined.VIEW_ENTRY_AD)) {
	        isEntryAdNow = true;
	    }
	    
	    if (isUploadLogNow) {
	        recordViewShowClickLog(key, VIEW_SHOW_ACTION, mLogCache);
	    } else {
	        JSONObject jo = recordViewShowClickLog(key, VIEW_SHOW_ACTION, mLogMap);
	        mLogStorage.saveBehaviorLog(key, jo);
	    }
	}
	
	
	public void viewClick(String key) {
	    if (isUploadLogNow) {
            recordViewShowClickLog(key, VIEW_CLICK_ACTION, mLogCache);
        } else {
            JSONObject jo = recordViewShowClickLog(key, VIEW_CLICK_ACTION, mLogMap);
            mLogStorage.saveBehaviorLog(key, jo);
        }
	}
	
	
	private JSONObject recordViewShowClickLog(String key, int action, HashMap<String, JSONObject> map) {
	    JSONObject jo = map.get(key);
        if (jo == null) {
            jo = new JSONObject();
            
            try {
                jo.put(LogDefined.KEY_ACTION, LogDefined.AC_TYPE_VIEW);
                jo.put(LogDefined.KEY_EVENT, key);
                
                if (action == VIEW_SHOW_ACTION) {
                    jo.put(LogDefined.KEY_SHOW_COUNT, 1);
                    jo.put(LogDefined.KEY_CLICK_COUNT, 0);
                
                } else if (action == VIEW_CLICK_ACTION) {
                    jo.put(LogDefined.KEY_SHOW_COUNT, 0);
                    jo.put(LogDefined.KEY_CLICK_COUNT, 1);
                    
                }
                map.put(key, jo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (action == VIEW_SHOW_ACTION) {
                int show = 0;
                
                try {
                    show = jo.getInt(LogDefined.KEY_SHOW_COUNT);
                } catch (Exception e) {
                    //do not print exception
                }
                show++;
                try {
                    jo.put(LogDefined.KEY_SHOW_COUNT, show);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            
            } else if (action == VIEW_CLICK_ACTION) {
                int click = 0;
                
                try {
                    click = jo.getInt(LogDefined.KEY_CLICK_COUNT);
                } catch (Exception e) {
                    //do not print exception
                }
                click++;
                try {
                    jo.put(LogDefined.KEY_CLICK_COUNT, click);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                
            }
            
        }
        
        return jo;
	}
	
	
	
	/*
     * start coding for count log
     */
	
	public void countEventVisit(String key) {
	    if (isUploadLogNow) {
            recordCountEventLog(key, mLogCache);
        } else {
            JSONObject jo = recordCountEventLog(key, mLogMap);
            mLogStorage.saveBehaviorLog(key, jo);
        }
	}
	
	
	private JSONObject recordCountEventLog(String key, HashMap<String, JSONObject> map) {
	    JSONObject jo = map.get(key);
        if (jo == null) {
            jo = new JSONObject();
            
            try {
                jo.put(LogDefined.KEY_ACTION, LogDefined.AC_TYPE_COUNT);
                jo.put(LogDefined.KEY_EVENT, key);
                jo.put(LogDefined.KEY_COUNT, 1);
                map.put(key, jo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
            int count = 0;
            try {
                count = jo.getInt(LogDefined.KEY_COUNT);
            } catch (Exception e) {
                //do not print exception
            }
            count ++;
            try {
                jo.put(LogDefined.KEY_COUNT, count);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        return jo;
	}
	
	
	public void applicationExit() {
	    if (!LogSettings.isUploadSchemeGot(mContext)) {
    	    new Thread() {
    	        public void run() {
    	            if (!LogSettings.isUploadSchemeGot(mContext)) {
    	                reqUploadScheme();
                    }
    	            
    	            handleLogWhenExitApp();
    	        }
    	    }.start();
	    } else {
	        handleLogWhenExitApp();
	    }
	}
	
	
	private void sendMsgToStopService() {
	    if (isUploadLogNow) {
	        return;
	    }
	    Handler handle = LogService.getLogHandler();
	    if (handle != null) {
	        handle.sendEmptyMessage(LogService.MSG_STOP_SERVICE);
	    }
	}
	
	
	private void handleLogWhenExitApp() {
	    if (mLastActivityDes != null && !isEntryAdNow) {
            activityExit(mLastActivityDes, System.currentTimeMillis());
            mLastActivityDes = null;
        }
        
        if (FeatureOption.BEHAVIOR_LOG_DEBUG) {
            uploadLogToServer(1, false);
        } else {
            if (LogSettings.isUploadWhenExit(mContext)) {
                uploadLogToServer(1, false);
            }
        }
        
        sendMsgToStopService();
	}
	
	
	public void alarmToUploadLog() {
	    new Thread() {
	        public void run() {
	            try {
                    sleep(10 * 1000);               //delay 10 seconds to run
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        	    if (NetworkType.isNetworkAvailable(mContext)) {
        	        if (LogSettings.isUploadInTime(mContext)) {
        	            uploadLogToServer(2, true);
        	        }
        	    }
        	    
        	    LogSettings.setAlarmToUpload(mContext, false);
        	    
        	    if (isMarketRunningNow()) {
                    return;
                }
                sendMsgToStopService();
	        }
	    }.start();
	}
	
	
	public void wifiAvailableToUploadLog() {
	    if (LogSettings.isUploadOnWifiConnect(mContext)) {
	        uploadLogToServer(2, false);
	    }
	    
	    if (isMarketRunningNow()) {
	        return;
	    }
	    sendMsgToStopService();
	}
	
	
	private boolean isMarketRunningNow() {
	    ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        RunningTaskInfo info = manager.getRunningTasks(1).get(0);
        String topPkgName = info.topActivity.getPackageName();
        if (mContext.getPackageName().equals(topPkgName)) {
            //market is running now
            return true;
        }
        
        return false;
	}
	
	
	
	/*
	 * start coding for upload log
	 */
	
	private synchronized void uploadLogToServer(final int tryCount, final boolean failDelay) {
	    
	    if (isUploadLogNow) {
	        //upload log now, do nothing and return
	        return;
	    }
	    isUploadLogNow = true;
	    
	    new Thread() {
	        public void run() {
	            LogStorage mLogStorage = LogStorage.getInstance(mContext);
	            if (mLogMap.size() > 0) {
	                mLogStorage.saveLogToFile(mLogMap);
	            }
	            
	            boolean uploadResult = false;
	            if (mLogStorage.zipLogFile()) {
	                if (FeatureOption.BEHAVIOR_LOG_DEBUG) {
	                    Intent intent = new Intent(mContext, DownloadService.class);
	                    intent.putExtra(DownloadManager.EXTRA_EVENT_KEY, DownloadManager.EVENT_DEBUG_PULL_DATA);
	                    
	                    mContext.startService(intent);
	                    
	                    try {
                            sleep(10 * 1000);
                        } catch (InterruptedException e) {}
	                }
	                //do upload
	                int upCount = 0;
	                while (!uploadResult && upCount < tryCount) {
	                    uploadResult = uploadLog();
	                    upCount++;
	                }
	                
	            }
	            
	            mLogMap.clear();
                if (uploadResult) {
                    mLogStorage.clearSavedLogFiles();
                } else {
                    LogSettings.uploadFailedCountAdd(mContext);
                    mLogStorage.clearLogCache();
                    LogSettings.setAlarmToUpload(mContext, true);
                }
                
                //send synchronize cache message
                Handler handle = LogService.getLogHandler();
                if (handle != null) {
                    handle.sendEmptyMessage(LogService.MSG_SYNC_CACHE);
                }
	        }
	    }.start();
	}
	
	
	private boolean reqUploadScheme() {
	    Map<String, String> params = new HashMap<String, String>();
        params.put("action", "1");
        return connectHttp(params, null);
	}
	
	
	private boolean uploadLog() {
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("action", "2");
	    File zipFile = new File(mLogStorage.getLogZipFilePath());
	    if (!zipFile.exists()) {
	        return true;
	    }
	    params.put("fileSize", Long.toString(zipFile.length()));
	    return connectHttp(params, zipFile);
	    //return false;      //test
	}
	
	
	private boolean connectHttp(Map<String, String> params, File file) {
        boolean success = false;
        String prefix = "--";
        String boundary = UUID.randomUUID().toString(); 
        String end = "\r\n";
        String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
        HttpURLConnection conn = null;
        try {
            URL url = new URL(UPLOAD_ADDRESS);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(10 * 1000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", HTTP.UTF_8);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("Accept", accept);

            StringBuilder textEntity = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                textEntity.append(prefix);
                textEntity.append(boundary);
                textEntity.append(end);
                textEntity.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                textEntity.append(entry.getValue());
                textEntity.append(end);
            }

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(textEntity.toString().getBytes());

            if (file != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(prefix);
                sb.append(boundary);
                sb.append(end);
    
                sb.append("Content-Disposition: form-data; name=\"reportfile\"; filename=\""  + file.getName() + "\"" + end);
                sb.append("Content-Type: application/octet-stream; charset="  + HTTP.UTF_8 + end);
                sb.append(end);
                dos.write(sb.toString().getBytes());
    
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
            }
            dos.writeBytes(end);

            byte[] end_data = (prefix + boundary + prefix + end).getBytes();
            dos.write(end_data); 
            int res = conn.getResponseCode();  
            if (res == HttpStatus.SC_OK) {
                InputStream input = conn.getInputStream();
                InputStreamReader inputReader = new InputStreamReader(input, HTTP.UTF_8);
                BufferedReader br = new BufferedReader(inputReader);
                
                char[] chars = new char[1024];
                int n = 0;
                StringBuilder sb = new StringBuilder();
                
                while ((n = br.read(chars)) != -1) {
                    sb.append(chars, 0, n);
                }
                String result = sb.toString();
                input.close();
                dos.close();
                JSONObject jsonResult;
                try {
                    jsonResult = new JSONObject(result);
                    if (jsonResult.getInt("result") == 0) {
                        success = true;
                    }
                    //save upload plan
                    int scheme = jsonResult.getInt("scheme");
                    long freeTime = jsonResult.getLong("freeTime");
                    int freeNet = jsonResult.getInt("freeNet");
                    int freeDelay = jsonResult.getInt("freeDelay");
                    int maxSize = jsonResult.getInt("maxSize");
                    int failNum = jsonResult.getInt("failNum");
                    LogSettings.saveUploadScheme(mContext, scheme, freeTime, freeNet, freeDelay, maxSize, failNum);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
        
        return success;
    }
}
