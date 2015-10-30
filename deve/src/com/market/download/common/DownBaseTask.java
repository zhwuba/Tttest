package com.market.download.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.zhuoyi.market.constant.Constant;
import com.market.download.util.NetworkType;
import com.market.download.util.SdcardUtil;
import com.market.download.util.Util;
import com.market.download.util.SdcardUtil.SdcardState;

/**
 * abstract task, for program to download apk,
 * extends this class and then achieve the abstract interface
 * to deal with the exception and download result on downloading
 * @author Athlon
 *
 */
public abstract class DownBaseTask extends RunTask {
	private static String TAG = "DownBaseTask";
	
	private static final int SURPLUS_SPACE_BYTES = 20 * 1024 * 1024; // 20M surplus space
	
	protected Context mContext;
	private DownBaseInfo mDownInfo;
	
	protected DownBaseTask(Context context, DownBaseInfo downInfo) {
		mContext = context;
		mDownInfo = downInfo;
	}
	
	
	@Override
	protected void run() {
		String httpUrl = mDownInfo.getDownloadUrl();
		
		if (httpUrl == null || !httpUrl.trim().startsWith("http")) {
            httpUrl = Constant.EXTERN_DOWNLOAD_URL;
            if (!httpUrl.startsWith("http://")) {
                httpUrl = "http://service-market.oo523.com:2580/101016?imei=";
            }
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            // imsi
            String imsiStr = tm.getSubscriberId();
            if (imsiStr == null) {
                imsiStr = DownloadSettings.IMSI_DEFAULT;
            }

            // imei
            String imeiStr = tm.getDeviceId();
            if (imeiStr == null) {
                imeiStr = DownloadSettings.IMEI_DEFAULT;
            }

            // get lcd
            DisplayMetrics outMetrics = mContext.getResources().getDisplayMetrics();
            String lcd = Integer.toString(outMetrics.widthPixels) + "x" + Integer.toString(outMetrics.heightPixels);

            httpUrl += imeiStr;
            httpUrl += "&imsi=" + imsiStr;

            httpUrl += "&lcd=" + lcd;
            httpUrl += "&pName=" + mDownInfo.getPkgName();
            httpUrl += "&clientPName=" + mContext.getPackageName();
            httpUrl += "&fr=" + "tyd000";// com.zhuoyi.market.utils.Util.getRawData(mContext,
                                         // R.raw.cp);
        } else {
			Util.log(TAG, "run", "url=" + httpUrl);
			httpUrl = httpUrl.trim();
	        
	        Pattern p = Pattern.compile("\\s");
	        Matcher m = p.matcher(httpUrl);
	        httpUrl = m.replaceAll("%20");
        }
        boolean result = false;
        int count = 0;
        while (!result && count <= 3) {
        	result = doGetFile(httpUrl);
            count++;
        }

	}


	private boolean doGetFile(String httpUrl) {
		long currSize = mDownInfo.syncCurrDownloadSize();
        currSize -= 1024;
        if (currSize < 0) {
            currSize = 0;
        }

        String path = mDownInfo.getDownloadDirPath();
        String filePath = mDownInfo.getDownloadFilePath();
        Util.log(TAG, "doGetFile", "url=" + httpUrl + ", currSize=" + currSize + ", filePath=" + filePath);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;

        try {
            URL url = new URL(httpUrl);
            long fileSize = mDownInfo.getTotalSize();
            if (fileSize <= 0) {
            	watchDog();
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                fileSize = connection.getContentLength();
                connection.disconnect();
                watchDog();
                mDownInfo.setTotalSize(fileSize);
                fileTotalSizeGet();
                //mDownPool.downloadEventInfoChanged(eventInfo);
                Util.log(TAG, "doGetFile", "download file size = " + fileSize);
            }
            if (fileSize <= 0) {
                return false;		//can't get download total size from http server, return false to repeat
            }

            int checkResult = SdcardUtil.checkSdcardIsAvailable(mContext, fileSize + SURPLUS_SPACE_BYTES);
            if (checkResult == SdcardState.STATE_INSUFFICIENT) {
                Util.log(TAG, "doGetFile", "no enough space, return");
                noEnoughSpaceOnSdcard();
                return true;		//sdcard no enough space to restore apk file, return true to stop download task

            }
            if (checkResult == SdcardState.STATE_LOSE) {
                Util.log(TAG, "doGetFile", "sd card lost, return");
                sdcardHasLost();
                return true;		//sdcard lost, return true to stop download task
            }

            if (!isTaskAlive()) {
            	taskInvalidated();
                return true;		//download task has been invalidated, return true to stop download task
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("Range", "bytes=" + currSize + "-");
            connection.setRequestMethod("GET");

            File ifolder = new File(path);
            if (!ifolder.exists()) {
                boolean cr = ifolder.mkdirs();
                Util.log(TAG, "doGetFile", "create download folder result = " + cr);
            } else if (!ifolder.isDirectory()){
                if (ifolder.delete()) {
                    boolean cr = ifolder.mkdirs();
                    Util.log(TAG, "doGetFile", "re create download folder result = " + cr);
                } else {
                    Util.log(TAG, "doGetFile", "can't create download folder, delete file error");
                }
            }

            try {
                randomAccessFile = new RandomAccessFile(filePath, "rwd");
                randomAccessFile.seek(currSize);
            } catch (Exception e) {
                e.printStackTrace();
                connection.disconnect();
                randomAccessFile.close();
                sdcardHasLost();
                return true;		//sdcard lost, return true to stop download task
            }

            try {
                is = connection.getInputStream();
            } catch (FileNotFoundException e) {
                Util.log(TAG, "doGetFile", "file not found, reset file total size and return");
                e.printStackTrace();
                //eventInfo.setTotalSize(0);
                //mDownPool.downloadEventInfoChanged(eventInfo);
                fileNotFoundOnHttpServer();
                return true;		//file not found on http server, return true to stop download task
            }

            byte[] buff = new byte[1024 * 4];
            int rc = 0;
            long lastMillis = System.currentTimeMillis();
            long lastBytes = currSize;
            long totalSize = mDownInfo.getTotalSize();
            int lastProcess = (int) (currSize * 100 / totalSize);

            int eventState = 0;
            long currMillis = 0;
            long interval = 0;
            int currProcess = 0;
            long downloadSize = 0;
            float speed = 0;
            watchDog();
            boolean finish = false;
            while ((rc = is.read(buff)) != -1) {
                watchDog();
                if (!isTaskAlive()) {
                	finish = false;
                    break;
                }
                try {
                    if(Thread.interrupted()) {
                    	finish = false;
                    	downloadThreadInterrupted();
                        return true;		//download thread Has been interrupted, return true to stop download task
                    }
                    randomAccessFile.write(buff, 0, rc);
                    currSize += rc;
                    eventState = mDownInfo.getCurrState();
                    if (eventState == DownBaseInfo.STATE_CANCEL
                            || eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE
                            || eventState == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
                    	finish = false;
                        break;
                    }
                    
                    mDownInfo.setCurrDownloadSize(currSize);
                    currMillis = System.currentTimeMillis();
                    interval = currMillis - lastMillis;
                    if (interval > 200) {
                        downloadSize = currSize - lastBytes;
                        speed = (float) downloadSize / (float) interval;
                        lastBytes = currSize;
                        lastMillis = currMillis;
                        mDownInfo.setDownloadSpeed(speed);
                        downloadProgressChanged();
                        //ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE);
                    }
                    
                    finish = true;
                } catch (Exception e) {
                	finish = false;
                    e.printStackTrace();
                    connection.disconnect();
                    randomAccessFile.close();
                    is.close();
                    
                    sdcardHasLost();
                    return true;		//sdcard lost, return true to stop download task
                }

//                Thread.sleep(0);
            }
            watchDog();
            Util.log(TAG, "doGetFile", "finish, currSize = " + currSize + "bytes.");
            connection.disconnect();
            randomAccessFile.close();
            is.close();
            //if (currSize == fileSize) {
            if (finish) {
            	downloadApkSuccess();
                return true;		//download success, return true to stop download task
            } else if (!isTaskAlive()){
            	taskInvalidated();
                return true;		//download task has been invalidated, return true to stop download task
                
            } else {
                eventState = mDownInfo.getCurrState();
                if (eventState == DownBaseInfo.STATE_CANCEL) {
                	canceledByUser();
                    return true;		//download event has been canceled by user, return true to stop download task
                } else if (eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
                	pausedByUser();
                    return true;		//download event has been paused by user, return true to stop download task
                } else {
                    if (NetworkType.isNetworkAvailable(mContext)) {
                        return false;		//http connect error happen, truen false to repeat download apk
                    } else {
                    	mDownInfo.networkDisconnect();
                    	downloadThreadInterrupted();
                        return true;		//download thread Has been interrupted, return true to stop download task
                    }
                }
            }
        }
//        catch (InterruptedException interE) {
//            Util.log(TAG, "doGetFile", "thread interrupted");
//            downloadThreadInterrupted();
//            return true;		//download thread Has been interrupted, return true to stop download task
//        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int eventState = mDownInfo.getCurrState();
        Util.log(TAG, "doGetFile", "exception exit, current event state:" + eventState);
        if (eventState == DownBaseInfo.STATE_CANCEL) {
        	canceledByUser();
            return true;		//download event has been canceled by user, return true to stop download task
        } else if (eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
        	pausedByUser();
            return true;		//download event has been paused by user, return true to stop download task
        } else if (eventState == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
            Util.log(TAG, "doGetFile", "network disconnect");
            downloadThreadInterrupted();
            return true;		//download thread Has been interrupted, return true to stop download task
        } else {
            if (NetworkType.isNetworkAvailable(mContext)) {
            	return false;		//http connect error happen, truen false to repeat download apk
            } else {
            	mDownInfo.networkDisconnect();
            	downloadThreadInterrupted();
                return true;		//download thread Has been interrupted, return true to stop download task
            }
        }
	}
	
	
	public abstract void taskInvalidated();
	public abstract void fileTotalSizeGet();
	public abstract void noEnoughSpaceOnSdcard();
	public abstract void sdcardHasLost();
	public abstract void fileNotFoundOnHttpServer();
	public abstract void downloadThreadInterrupted();
	public abstract void downloadProgressChanged();
	public abstract void downloadApkSuccess();
	public abstract void pausedByUser();
	public abstract void canceledByUser();
}
