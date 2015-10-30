package com.market.download.userDownload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.zhuoyi.market.constant.Constant;
import com.market.account.dao.UserInfo;
import com.market.download.common.DownBaseInfo;
import com.market.download.common.DownloadSettings;
import com.market.download.httpConnect.HttpConnect;
import com.market.download.userDownload.DownloadManager.DownloadMsg;
import com.market.download.userDownload.DownloadPool.DownloadRes;
import com.market.download.util.NetworkType;
import com.market.download.util.SdcardUtil;
import com.market.download.util.SdcardUtil.SdcardState;
import com.market.download.util.Util;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;

public class HttpManager {
    public static final String TAG = "httpM";

    private Context mContext;
    private DownloadManager mDownManager;
    private DownloadPool mDownPool;

    private static final int SURPLUS_SPACE_BYTES = 20 * 1024 * 1024; // 20M surplus space

    HttpManager(Context context, DownloadManager downManager, DownloadPool downPool) {
        mContext = context;
        mDownManager = downManager;
        mDownPool = downPool;
    }

    public int downloadApk(DownloadEventInfo eventInfo) {
        String httpUrl = eventInfo.getDownloadUrl();

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
            httpUrl += "&pName=" + eventInfo.getPkgName();
            httpUrl += "&clientPName=" + mContext.getPackageName();
            httpUrl += "&fr=" + "tyd000";// com.zhuoyi.market.utils.Util.getRawData(mContext,
                                         // R.raw.cp);
        } else {
            Util.log(TAG, "downloadApk", "url=" + httpUrl);
            httpUrl = httpUrl.trim();
            
            Pattern p = Pattern.compile("\\s");
            Matcher m = p.matcher(httpUrl);
            httpUrl = m.replaceAll("%20");
            
        }

        int httpRes = doGetFile(httpUrl, eventInfo);
        int count = 1;
        while (httpRes == DownloadRes.HTTP_ERROR && count <= 3) {
            httpRes = doGetFile(httpUrl, eventInfo);
            count++;
        }

        return httpRes;
    }

    private int doGetFile(String urlStr, DownloadEventInfo eventInfo) {
        long currSize = eventInfo.syncCurrDownloadSize();
        currSize -= 1024;
        if (currSize < 0) {
            currSize = 0;
        }

        String path = eventInfo.getDownloadDirPath();
        String filePath = eventInfo.getDownloadFilePath();
        Util.log(TAG, "doGetFile", "url=" + urlStr + ", currSize=" + currSize + ", filePath=" + filePath);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;

        try {
            URL url = new URL(urlStr);
            long fileSize = eventInfo.getTotalSize();
            if (fileSize <= 0) {
                eventInfo.watchDog();
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                fileSize = connection.getContentLength();
                connection.disconnect();
                eventInfo.watchDog();
                eventInfo.setTotalSize(fileSize);
                mDownPool.downloadEventInfoChanged(eventInfo);
                Util.log(TAG, "doGetFile", "download file size = " + fileSize);
            }
            if (fileSize <= 0) {
                return DownloadRes.HTTP_ERROR;
            } else {
            	ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_APK_DOWNLOADING);
            }

            int checkResult = SdcardUtil.checkSdcardIsAvailable(mContext, fileSize + SURPLUS_SPACE_BYTES);
            if (checkResult == SdcardState.STATE_INSUFFICIENT) {
                Util.log(TAG, "doGetFile", "no enough space, return");
                return DownloadRes.NO_ENOUGH_SPACE;

            }
            if (checkResult == SdcardState.STATE_LOSE) {
                Util.log(TAG, "doGetFile", "sd card lost, return");
                return DownloadRes.SDCARD_LOST;
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
                return DownloadRes.SDCARD_LOST;
            }

            try {
                is = connection.getInputStream();
            } catch (FileNotFoundException e) {
                Util.log(TAG, "doGetFile", "file not found, reset file total size and return");
                e.printStackTrace();
                eventInfo.setTotalSize(0);
                mDownPool.downloadEventInfoChanged(eventInfo);
                return DownloadRes.FILE_NOT_FOUND;
            }

            byte[] buff = new byte[1024 * 4];
            int rc = 0;
            long lastMillis = System.currentTimeMillis();
            long lastBytes = currSize;

            int eventState = 0;
            long currMillis = 0;
            long interval = 0;
            long downloadSize = 0;
            float speed = 0;
            eventInfo.watchDog();
            boolean finish = false;
            while ((rc = is.read(buff)) != -1) {
                eventInfo.watchDog();
                try {
                    if(Thread.interrupted()) {
                    	finish = false;
                        return DownloadRes.THREAD_INTERRUPT;
                    }
                    randomAccessFile.write(buff, 0, rc);
                    currSize += rc;
                    eventState = eventInfo.getCurrState();
                    if (eventState == DownBaseInfo.STATE_CANCEL
                            || eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE
                            || eventState == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
                    	finish = false;
                        break;
                    } else if (eventState == DownBaseInfo.STATE_READY) {
                        eventInfo.downloading();
                    }
                    
                    eventInfo.setCurrDownloadSize(currSize);
                    currMillis = System.currentTimeMillis();
                    interval = currMillis - lastMillis;
                    if (interval > 200) {
                        downloadSize = currSize - lastBytes;
                        speed = (float) downloadSize / (float) interval;
                        lastBytes = currSize;
                        lastMillis = currMillis;
                        eventInfo.setDownloadSpeed(speed);
                        ListenerManager.getInstance(mContext).downInfoChanged(eventInfo, DownloadMsg.MSG_DOWNLOAD_PROGRESS_UPDATE);
                    }
                    
                    finish = true;
                } catch (Exception e) {
                	finish = false;
                    e.printStackTrace();
                    connection.disconnect();
                    randomAccessFile.close();
                    is.close();
                    return DownloadRes.SDCARD_LOST;
                }

//                if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_BACKGROUND
//                        || eventInfo.getEventArray() == DownloadEventInfo.ARRAY_UPDATE) {
//                    Thread.sleep(0);
//                }
            }
            eventInfo.watchDog();
            Util.log(TAG, "doGetFile", "finish, currSize = " + currSize + "bytes.");
            connection.disconnect();
            randomAccessFile.close();
            is.close();
            //if (currSize == fileSize) {
            if (finish) {
                return DownloadRes.DOWNLOAD_COMPLETE;
            } else {
                eventState = eventInfo.getCurrState();
                if (eventState == DownBaseInfo.STATE_CANCEL) {
                    return DownloadRes.USER_CANCEL;
                } else if (eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
                    return DownloadRes.USER_PAUSE;
                } else {
                    if (NetworkType.isNetworkAvailable(mContext)) {
                        return DownloadRes.HTTP_ERROR;
                    } else {
                        eventInfo.networkDisconnect();
                        return DownloadRes.THREAD_INTERRUPT;
                    }
                }
            }
        }
//        catch (InterruptedException interE) {
//            Util.log(TAG, "doGetFile", "thread interrupted");
//            return DownloadRes.THREAD_INTERRUPT;
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

        int eventState = eventInfo.getCurrState();
        Util.log(TAG, "doGetFile", "exception exit, current event state:" + eventState);
        if (eventState == DownBaseInfo.STATE_CANCEL) {
            return DownloadRes.USER_CANCEL;
        } else if (eventState == DownBaseInfo.STATE_DOWNLOAD_PAUSE) {
            return DownloadRes.USER_PAUSE;
        } else if (eventState == DownBaseInfo.STATE_NETWORK_DISCONNECT) {
            Util.log(TAG, "doGetFile", "network disconnect");
            return DownloadRes.THREAD_INTERRUPT;
        } else {
            if (NetworkType.isNetworkAvailable(mContext)) {
                return DownloadRes.HTTP_ERROR;
            } else {
                eventInfo.networkDisconnect();
                return DownloadRes.THREAD_INTERRUPT;
            }
        }
    }

    
}
