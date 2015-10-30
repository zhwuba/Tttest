package com.freeme.themeclub.theme.onlinetheme.download;

import java.text.DecimalFormat;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

/**
 * DownloadManagerHelper
 */
public class DownloadManagerHelper {

    public static final Uri CONTENT_URI = Uri
            .parse("content://downloads/my_downloads");
    /** represents downloaded file above api 11 **/
    public static final String COLUMN_LOCAL_FILENAME = "local_filename";
    /** represents downloaded file below api 11 **/
    public static final String COLUMN_LOCAL_URI = "local_uri";

    private DownloadManager downloadManager;

    public DownloadManagerHelper(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    /**
     * get download status
     * 
     * @param downloadId
     * @return
     */
    public int getStatusById(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_STATUS);
    }

    /**
     * get downloaded byte, total byte
     * 
     * @param downloadId
     * @return a int array with two elements
     *         <ul>
     *         <li>result[0] represents downloaded bytes, This will initially be
     *         -1.</li>
     *         <li>result[1] represents total bytes, This will initially be -1.</li>
     *         </ul>
     */
    public int[] getDownloadBytes(long downloadId) {
        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        return new int[] { bytesAndStatus[0], bytesAndStatus[1] };
    }

    /**
     * get downloaded byte, total byte and download status
     * 
     * @param downloadId
     * @return a int array with three elements
     *         <ul>
     *         <li>result[0] represents downloaded bytes, This will initially be
     *         -1.</li>
     *         <li>result[1] represents total bytes, This will initially be -1.</li>
     *         <li>result[2] represents download status, This will initially be
     *         0.</li>
     *         </ul>
     */
    public int[] getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[] { -1, -1, 0 };
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                bytesAndStatus[0] = c
                        .getInt(c
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                bytesAndStatus[1] = c
                        .getInt(c
                                .getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                bytesAndStatus[2] = c.getInt(c
                        .getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return bytesAndStatus;
    }

    /**
     * get download file name
     * 
     * @param downloadId
     * @return
     */
    public String getFileName(long downloadId) {
        return getString(downloadId,
                (Build.VERSION.SDK_INT < 11 ? COLUMN_LOCAL_URI
                        : COLUMN_LOCAL_FILENAME));
    }

    /**
     * get download uri
     * 
     * @param downloadId
     * @return
     */
    public String getUri(long downloadId) {
        return getString(downloadId, DownloadManager.COLUMN_URI);
    }

    /**
     * get failed code or paused reason
     * 
     * @param downloadId
     * @return <ul>
     *         <li>if status of downloadId is
     *         {@link DownloadManager#STATUS_PAUSED}, return
     *         {@link #getPausedReason(long)}</li>
     *         <li>if status of downloadId is
     *         {@link DownloadManager#STATUS_FAILED}, return
     *         {@link #getErrorCode(long)}</li>
     *         <li>if status of downloadId is neither
     *         {@link DownloadManager#STATUS_PAUSED} nor
     *         {@link DownloadManager#STATUS_FAILED}, return 0</li>
     *         </ul>
     */
    public int getReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get paused reason
     * 
     * @param downloadId
     * @return <ul>
     *         <li>if status of downloadId is
     *         {@link DownloadManager#STATUS_PAUSED}, return one of
     *         {@link DownloadManager#PAUSED_WAITING_TO_RETRY}<br/>
     *         {@link DownloadManager#PAUSED_WAITING_FOR_NETWORK}<br/>
     *         {@link DownloadManager#PAUSED_QUEUED_FOR_WIFI}<br/>
     *         {@link DownloadManager#PAUSED_UNKNOWN}</li>
     *         <li>else return {@link DownloadManager#PAUSED_UNKNOWN}</li>
     *         </ul>
     */
    public int getPausedReason(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get failed error code
     * 
     * @param downloadId
     * @return one of {@link DownloadManager#ERROR_*}
     */
    public int getErrorCode(long downloadId) {
        return getInt(downloadId, DownloadManager.COLUMN_REASON);
    }

    /**
     * get string column
     * 
     * @param downloadId
     * @param columnName
     * @return
     */
    private String getString(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(downloadId);
        String result = null;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getString(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * get int column
     * 
     * @param downloadId
     * @param columnName
     * @return
     */
    private int getInt(long downloadId, String columnName) {
        DownloadManager.Query query = new DownloadManager.Query()
                .setFilterById(downloadId);
        int result = -1;
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(columnName));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }
    

    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int MB_2_BYTE = 1024 * 1024;
    public static final int KB_2_BYTE = 1024;

    /**
     * @param size
     * @return
     */
    public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(
                    DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
                    .append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(
                    DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
                    .append("K");
        } else {
            return size + "B";
        }
    }

    public static String getNotiPercent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int) ((double) progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }

    public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }
}
