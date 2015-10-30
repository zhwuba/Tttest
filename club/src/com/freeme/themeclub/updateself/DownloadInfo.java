package com.freeme.themeclub.updateself;

import java.io.File;

public class DownloadInfo {
    public static final int STATE_READY = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_COMPLETE = 4;

    private static final String SEPERATOR = ",";

    public static final int TYPE_NORMAL_DOWN = 1;
    public static final int TYPE_DOWNLOAD_BG = 2;

    public String md5;
    public String url;
    public int type;
    public int versionCode;
    public long totalSize;

    private String fileName;
    private String apkFileName;

    public int state;

    public static final String FILE_NAME_PRE = "Update_";

    DownloadInfo(String md5Str, String urlStr, int downType, int verCode) {
        md5 = md5Str;
        url = urlStr;
        type = downType;
        versionCode = verCode;
        state = STATE_READY;
        fileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk.tmp";
        apkFileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk";
    }

    DownloadInfo(String infoStr) {
        String[] infoList = infoStr.split(SEPERATOR);
        state = Integer.parseInt(infoList[0]);
        md5 = infoList[1];
        url = infoList[2];
        type = Integer.parseInt(infoList[3]);
        versionCode = Integer.parseInt(infoList[4]);
        totalSize = Long.parseLong(infoList[5]);
        if (state == STATE_COMPLETE) {
            fileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk";
        } else {
            fileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk.tmp";
        }
        apkFileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk";
    }

    public String getDownloadInfoStr() {
        String eventStr = Integer.toString(state) + SEPERATOR + md5 + SEPERATOR + url + SEPERATOR
                + Integer.toString(type) + SEPERATOR + Integer.toString(versionCode) + SEPERATOR
                + Long.toString(totalSize);
        return eventStr;
    }

    public void setTotalSize(long fileSize) {
        totalSize = fileSize;
    }

    public long getCurrDownloadSize() {
        return Util.getDownloadFileSize(fileName);
    }

    public void downloadPaused() {
        state = STATE_PAUSED;
    }

    public void startDownload() {
        state = STATE_DOWNLOADING;
    }

    public void downloadComplete() {
        state = STATE_COMPLETE;
        File tmpFile = getDownloadFile();
        fileName = FILE_NAME_PRE + md5 + "_" + versionCode + ".apk";
        tmpFile.renameTo(getDownloadFile());
    }

    public File getDownloadFile() {
        return Util.getDownloadFile(fileName);
    }

    public File getApkFile() {
        return Util.getDownloadFile(apkFileName);
    }

    public String getDownloadFilePath() {
        return Util.getDownloadPath() + File.separator + fileName;
    }
}
