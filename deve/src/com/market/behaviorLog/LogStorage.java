package com.market.behaviorLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;

import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class LogStorage {
	private static final String TAG = "LogStorage";
    
	private static final String DIR_LOG_SAVED = "/BehaviorLog";
	
	private SharedPreferences mLogSharedPref;
	
	private Context mContext;
	
	private static WeakReference<LogStorage> mSelf = null;
	
	public static WeakReference<LogStorage> getWeakReference() {
	    return mSelf;
	}
	
	public static LogStorage getInstance(Context context) {
	    if (mSelf == null || mSelf.get() == null) {
	        mSelf = new WeakReference<LogStorage>(new LogStorage(context));
	    }
	    
	    return mSelf.get();
	}
	
	
	LogStorage(Context context) {
	    mContext = context;
		mLogSharedPref = context.getSharedPreferences(SharedPrefDefine.BEHAVIOR_LOG, Context.MODE_PRIVATE);
	}
	
	
	/**
	 * save behavior log to cache file
	 * @param key
	 * @param logJo
	 */
	public void saveBehaviorLog(String key, JSONObject logJo) {
		SharedPreferences.Editor editor = mLogSharedPref.edit();
		editor.putString(key, logJo.toString());
		editor.commit();
	}
	
	
	/**
	 * save behavior logs to cache file
	 * @param saveMap
	 */
	public void saveBehaviorLogs(HashMap<String, JSONObject> saveMap) {
	    SharedPreferences.Editor editor = mLogSharedPref.edit();
	    Iterator iter = saveMap.entrySet().iterator();
        Map.Entry entry = null;
        JSONObject jo = null;
        String key = null;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            jo = (JSONObject) entry.getValue();
            key = (String)entry.getKey();
            editor.putString(key, jo.toString());
        }
	    
	    editor.commit();
	}
	
	
	private void clearCurrBehaviorLog() {
		SharedPreferences.Editor editor = mLogSharedPref.edit();
		editor.clear();
		editor.commit();
	}
	
	
	/**
	 * init behavior log map from cache.
	 * if application be killed by system, get unsaved log from cache
	 * @return
	 */
	public HashMap<String, JSONObject> initBehaviorLog() {
		HashMap<String, JSONObject> logMap = new HashMap<String, JSONObject>();
		Map<String, ?> spMap = mLogSharedPref.getAll();
		Iterator iter = spMap.entrySet().iterator();
		Map.Entry entry = null;
		String infoStr = null;
		String key = null;
		JSONObject jo = null;
		while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            infoStr = (String) entry.getValue();
            key = (String) entry.getKey();
            try {
            	jo = new JSONObject(infoStr);
            	logMap.put(key, jo);
            } catch(Exception e) {
            	iter.remove();
            	e.printStackTrace();
            }
		}
		
		return logMap;
	}
	
	
	
	/**
	 * 
	 * @return absolute directory file path for save log file
	 */
	private String getRecordDirPath() {
		String path = null;
		try {
			path = mContext.getFilesDir().getAbsolutePath() + DIR_LOG_SAVED;
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return path;
	}
	
	
	/**
	 * create the name of file to save log
	 * @return absolute file path
	 */
	private String getRecordFileName() {
	    long millis = System.currentTimeMillis();
	    TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	    String imei = (tm.getDeviceId() == null? "null" : tm.getDeviceId());
	    String imsi = (tm.getSubscriberId() == null? "null" : tm.getSubscriberId());
	    
	    String fileName = imei + "_" + imsi + "_" + Long.toString(millis) + ".txt";
	    
	    return getRecordDirPath() + "/" + fileName;
	}
	
	
	private boolean zipFile(File zipFile, File[] fileList){
	    FileOutputStream fops = null;
        ZipOutputStream zipOps = null;
        FileInputStream fips = null;
        try {
            fops = new FileOutputStream(zipFile, false);
            zipOps = new ZipOutputStream(fops);
            zipOps.setMethod(ZipOutputStream.DEFLATED);
            zipOps.setLevel(8);
            int len = 0;
            byte[] buffer = new byte[1024 * 4];
            ZipEntry zipEntry = null;
            for(File file : fileList){
                fips = new FileInputStream(file);
                zipEntry = new ZipEntry(file.getName());
                
                zipOps.putNextEntry(zipEntry);
                
                while((len = fips.read(buffer)) != -1){
                    zipOps.write(buffer, 0, len);
                }
                fips.close();
                zipOps.closeEntry();
            }
            
            zipOps.finish();
            
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                zipOps.close();
            } catch (Exception e) {
                //do nothing
            }
            
            try {
                fops.close();
            } catch (Exception e) {
                //do nothing
            }
            
            try {
                fips.close();
            } catch (Exception e) {
                //do nothing
            }
        }
        
    }
	
	
	/**
	 * get the file path about log zip file
	 * @return absolute file path
	 */
	public String getLogZipFilePath() {
		String fileDirPath = null;
	    try {
	    	fileDirPath = "/data/data/" + mContext.getPackageName() + "/files";
	        fileDirPath = mContext.getFilesDir().getAbsolutePath();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return fileDirPath + "/logFile.zip";
	}
	
	
	/**
	 * zip log files, if no file to zip, return false
	 * @return if zip success, return true, unless return false
	 */
	public boolean zipLogFile() {
	    File zipFile = new File(getLogZipFilePath());
	    if (zipFile.exists()) {
	        zipFile.delete();
	    }
	    File[] logFiles = new File(getRecordDirPath()).listFiles();
	    if (logFiles != null && logFiles.length > 0) {
	        return zipFile(zipFile, logFiles);
	    }
	    
	    return false;
	}
	
	
	/**
	 * clear the saved log file after upload success
	 */
	public void clearSavedLogFiles() {
	    //clear shared preferences saved log
	    clearCurrBehaviorLog();
	    
	    //clear saved log files
	    File dirFile = new File(getRecordDirPath());
	    File[] files = dirFile.listFiles();
	    for (int i=0; i < files.length; i++) {
	        files[i].delete();
	    }
	    
	    //clear uploaded zip file
	    File zipFile = new File(getLogZipFilePath());
	    zipFile.delete();
	}
	
	
	/**
	 * upload log file failed, clear cache log and zip file, but not clear the saved log files
	 */
	public void clearLogCache() {
	    //clear shared preferences saved log
        clearCurrBehaviorLog();
        
	}
	
	
	/**
	 * new a log file to save behavior log
	 * @param saveMap behavior log map
	 */
	public void saveLogToFile(HashMap<String, JSONObject> saveMap) {
	    String saveFilePath = getRecordFileName();
	    File saveFile = new File(saveFilePath);
	    //if directory file is not exist, create it
	    File dirFile = saveFile.getParentFile();
	    if (!dirFile.exists()) {
	        dirFile.mkdirs();
	    }
	    
	    FileOutputStream fops = null;
	    try {
            fops = new FileOutputStream(saveFile);
            
            String paramStr = LogUtil.getPublicParamStr(mContext) + "\n";
            byte[] writeBytes = null;
            try {
                writeBytes = paramStr.getBytes("utf-8");
            } catch (UnsupportedEncodingException e1) {
                LogUtil.log(TAG, "saveLogToFile", "error encode params:" + paramStr);
            }
            
            if (writeBytes == null) {
                writeBytes = paramStr.getBytes();
            }
            
            //write params
            fops.write(writeBytes);
            
            writeBytes = null;
            Iterator iter = saveMap.entrySet().iterator();
            Map.Entry entry = null;
            JSONObject jo = null;
            String writeStr = null;
            while (iter.hasNext()) {
                entry = (Map.Entry) iter.next();
                jo = (JSONObject) entry.getValue();
                writeStr = "\n" + jo.toString();
                try {
                    writeBytes = writeStr.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    LogUtil.log(TAG, "saveLogToFile", "error encode JSONObject:" + writeStr);
                    writeBytes = writeStr.getBytes();
                }
                
                fops.write(writeBytes);
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            
        } catch (IOException e) {
            e.printStackTrace();
            
        } finally {
            try {
                fops.close();
            } catch (Exception e) {
                //do nothing
            }
        }
	    
	}
	
	
}
