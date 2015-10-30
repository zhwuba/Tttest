package com.market.updateSelf;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.market.updateSelf.UpdateManager.DownloadRes;
import com.market.updateSelf.UpdateUtil.SdcardState;

public class HttpManager {
    public static final String TAG = "HttpManager";

    private UpdateSelfService mUpdateService;

    private static final int SURPLUS_SPACE_BYTES = 20 * 1024 * 1024; // 20M
                                                                     // surplus
                                                                     // space

    HttpManager(UpdateSelfService service) {
        mUpdateService = service;
    }

    /**
     * 下载文件
     * @param downInfo
     * @return
     */
    private int doGetFile(SelfUpdateInfo downInfo) {
        long currSize = downInfo.getCurrDownloadSize();
        currSize -= 1024;
        if (currSize < 0) {
            currSize = 0;
        }

        String path = UpdateUtil.getDownloadPath();
        String filePath = downInfo.getDownloadFilePath();
        UpdateUtil.logI(TAG, "doGetFile()", "url=" + downInfo.getDownloadUrl() + ", currSize=" + currSize + ", filePath=" + filePath);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;

        // test
        // urlStr = "http://apk.oo523.com/appstores/DownloadSingleAppImage?td=1001&lcd=480x800&os=android&nt=wifi&ver=65&cid=0&lac=0&mcc=0&mnc=0&mpm=i50&pf=1001&ram=4&rom=4&mod=i50.KB.V2.09&Batch=mt6575&no=i0000217&androidVersion=&lbyver=2.3.6&imsi=null&imei=864471010000089&imageId=C13435.png";

        try {
            URL url = new URL(downInfo.getDownloadUrl());
            long fileSize = downInfo.getTotalSize();
            if (fileSize == 0) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                fileSize = connection.getContentLength();
                connection.disconnect();
                if (fileSize <= 1024) {
                    return DownloadRes.HTTP_ERROR;
                }

                downInfo.setTotalSize(fileSize);
                UpdateUtil.logV(TAG, "doGetFile()", "download file size = " + fileSize);
            }

            int checkResult = UpdateUtil.checkSdcardIsAvailable(mUpdateService, fileSize + SURPLUS_SPACE_BYTES);
            if (checkResult == SdcardState.STATE_INSUFFICIENT) {
            	UpdateUtil.logV(TAG, "doGetFile()", "no enough space, return");
                return DownloadRes.NO_ENOUGH_SPACE;

            }
            if (checkResult == SdcardState.STATE_LOSE) {
            	UpdateUtil.logV(TAG, "doGetFile()", "sd card lost, return");
                return DownloadRes.SDCARD_LOST;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setRequestProperty("Range", "bytes=" + currSize + "-");
            connection.setRequestMethod("GET");

            File ifolder = new File(path);
            if (!ifolder.exists()) {
                boolean cr = ifolder.mkdirs();
                UpdateUtil.logV(TAG, "doGetFile()", "create download folder result = " + cr);
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
            connection.setReadTimeout(26000);
            is = connection.getInputStream();

            byte[] buff = new byte[1024];
            int rc = 0;
            while ((rc = is.read(buff)) != -1) {
                try {
                    randomAccessFile.write(buff, 0, rc);
                    currSize += rc;

                } catch (Exception e) {
                    e.printStackTrace();
                    connection.disconnect();
                    randomAccessFile.close();
                    is.close();
                    return DownloadRes.SDCARD_LOST;
                }
            }
            UpdateUtil.logV(TAG, "doGetFile()", "finish, currSize = " + currSize + "bytes.");
            connection.disconnect();
            randomAccessFile.close();
            is.close();
            if (currSize == fileSize) {
                return DownloadRes.DOWNLOAD_COMPLETE;
            } else {
                return DownloadRes.HTTP_ERROR;
            }
        } catch (Exception e) {
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
        UpdateUtil.logV(TAG, "doGetFile()", "exception exit");
        return DownloadRes.HTTP_ERROR;
    }

    /**
     * 下载自更新文件
     * @param downInfo
     * @return
     */
    public int downloadUpdate(SelfUpdateInfo downInfo) {
        return doGetFile(downInfo);
    }
}
