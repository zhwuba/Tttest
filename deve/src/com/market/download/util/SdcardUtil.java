package com.market.download.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class SdcardUtil {
    private static final String TAG = "sdcardUtil";

    /**
     * get sdcard absolute path
     * 
     * @return return sdcard absolute path
     */
    public static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     * check sdcard is available now
     * 
     * @return is sdcard is available, return true, unless return false
     */
    public static boolean isSdcardAvailable() {
        boolean result = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        Util.log(TAG, "isSdcardAvailable", Boolean.toString(result));
        return result;
    }

    /**
     * signal the sdcard state if sdcard lost: {@link STATE_LOSE}; if sdcard
     * available and has enough space: {@link STATE_OK}; if sdcard available but
     * not enough space: {@link STATE_INSUFFICIENT}
     * 
     * @author Athlon
     * 
     */
    public static class SdcardState {
        /**
         * if sdcard lost, use this state
         */
        public static final int STATE_LOSE = 0;

        /**
         * if sdcard available and has enough space, use this state
         */
        public static final int STATE_OK = 1;

        /**
         * if sdcard available but not enough space, use this state
         */
        public static final int STATE_INSUFFICIENT = 2;
    }

    /**
     * check sdcard has enough free space
     * 
     * @param context
     * @param miniSize
     *            the file size needed, long type
     * @return {@link SdcardState}
     */
    public static int checkSdcardIsAvailable(Context context, long miniSize) {
        Util.log(TAG, "checkSdcardIsAvailable", "miniSize = " + miniSize);
        if (!isSdcardAvailable()) {
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
        
        Util.log(TAG, "checkSdcardIsAvailable", "availableSize = " + availableSize);
        if (availableSize < miniSize) {
            return SdcardState.STATE_INSUFFICIENT;
        }
        return SdcardState.STATE_OK;
    }
}
