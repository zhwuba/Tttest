package com.market.download.userDownload;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.market.download.common.DownBaseInfo;
import com.market.download.userDownload.DownloadPool.DownThread;
import com.market.download.util.SdcardUtil;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;
import com.market.updateSelf.Custom;
import com.market.updateSelf.SelfUpdateInfo;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.patch.APKPatchUtil;

public class DownloadEventInfo extends DownBaseInfo{
    public static final String TAG = "eventInfo";
    public static final String IMAGE_DIR_PATH = Constant.download_cache_dir;

    public static final int ARRAY_WAITING = 0;
    public static final int ARRAY_DOWNLOADING = 1;
    public static final int ARRAY_PAUSED = 2;
    public static final int ARRAY_BACKGROUND = 3;
    public static final int ARRAY_COMPLETE = 4;
    public static final int ARRAY_UPDATE = 5;
    
    private String mAppName;
    private String mMd5;
    private int mDownloadArray = ARRAY_WAITING;
    private boolean mOnlyDownInWifi = false;
    private String mFlag;
    private int mVersionCode = 0;
    private int mAppId = 0;
    private String mTopicId;

    private boolean isSelfApk = false;

    private boolean isRenameing = false;
    
    public static final String SEPERATOR = ",,";
    
    private String mListenPkgName;
    private String mListenClassName;

    public DownloadEventInfo(String pkgName, String appName, String md5,
            String url, String topicId, String flag, boolean downBg, boolean downOnlyWifi,
            boolean isUpdate, int verCode, int appId, String diffDownUrl, long totalSize, long diffSize) {
    	super(pkgName, getApkFileNameWithoutSuffix(md5, appName, pkgName, verCode), url, diffDownUrl, totalSize, diffSize);
        mVersionCode = verCode;
        mAppName = appName;

        mAppId = appId;
        // mFileName = mAppName + ".apk.tmp";
        mMd5 = md5;
        if (downBg) {
            mDownloadArray = ARRAY_BACKGROUND;
        } else if (isUpdate) {
            mDownloadArray = ARRAY_UPDATE;
        } else {
            mDownloadArray = ARRAY_WAITING;
        }
        mOnlyDownInWifi = downOnlyWifi;
        mFlag = flag;
        mTopicId = topicId;
        initSelfDownloadFlag(mMd5, mAppName);
    }
    
    
    public DownloadEventInfo(String pkgName, String appName, String md5,
            String url, String topicId, String flag, boolean downBg, boolean downOnlyWifi,
            boolean isUpdate, int verCode, int appId, long totalSize) {
    	super(pkgName, getApkFileNameWithoutSuffix(md5, appName, pkgName, verCode), url, totalSize);
        mVersionCode = verCode;
        mAppName = appName;

        mAppId = appId;
        // mFileName = mAppName + ".apk.tmp";
        mMd5 = md5;
        if (downBg) {
            mDownloadArray = ARRAY_BACKGROUND;
        } else if (isUpdate) {
            mDownloadArray = ARRAY_UPDATE;
        } else {
            mDownloadArray = ARRAY_WAITING;
        }
        mOnlyDownInWifi = downOnlyWifi;
        mFlag = flag;
        mTopicId = topicId;
        initSelfDownloadFlag(mMd5, mAppName);
    }
    

    public DownloadEventInfo(String pkgName, String appName, String url, String topicId, String flag,
            boolean downBg, boolean downOnlyWifi, long totalSize) {
    	super(pkgName, getApkFileNameWithoutSuffix(null, appName, pkgName, 0), url, totalSize);
        mAppName = appName;
        mMd5 = null;
        if (downBg) {
            mDownloadArray = ARRAY_BACKGROUND;
        } else {
            mDownloadArray = ARRAY_WAITING;
        }
        mOnlyDownInWifi = downOnlyWifi;
        mFlag = flag;
        mTopicId = topicId;
        initSelfDownloadFlag(mMd5, mAppName);
    }
    
    
    public DownloadEventInfo(String eventStr) {
    	super(eventStr);
    	
    	String[] infoList = eventStr.split(SEPERATOR);
        
        mAppName = infoList[3];
        mMd5 = infoList[4];
        if (mMd5.equals("null")) {
        	mMd5 = null;
        }
        
        mDownloadArray = Integer.parseInt(infoList[6]);
        
        mOnlyDownInWifi = Boolean.parseBoolean(infoList[8]);
        mFlag = infoList[9];
        
        mVersionCode = 0;
        mAppId = 0;
        mTopicId = ReportFlag.FROM_NULL;
        
        if (infoList.length >= 11) {
        	mVersionCode = Integer.parseInt(infoList[10]);
        }

        if (infoList.length >= 12) {
        	mAppId = Integer.parseInt(infoList[11]);
        }

        if (infoList.length >= 13) {
        	mTopicId = infoList[12];
        } else {
        	mTopicId = Integer.toString(-2);
        }
        
        if (infoList.length >= 18) {
            mListenPkgName = infoList[16];
            mListenClassName = infoList[17];
        }
        
        initSelfDownloadFlag(mMd5, mAppName);
    }
    
    
    public String getReceiverPkgName() {
        return mListenPkgName;
    }
    
    
    public String getReceiverClass() {
        return mListenClassName;
    }
    
    
    public ComponentName getReceiverComponentName() {
        if (mListenPkgName == null || mListenClassName == null) {
            return null;
        }
        ComponentName cn = new ComponentName(mListenPkgName, mListenClassName);
        return cn;
    }
    
    
    public void setReceiverClassName(String receiverPkg, String receiverClass) {
        mListenPkgName = receiverPkg;
        mListenClassName = receiverClass;
    }
    

    private static boolean isSelfDownloadEvent(String md5, String appName) {
    	if (md5 != null && appName.startsWith(SelfUpdateInfo.FILE_NAME_PRE + md5)) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
    private void initSelfDownloadFlag(String md5, String appName) {
    	isSelfApk = isSelfDownloadEvent(md5, appName);
    }

    public String getEventString() {
        String md5Str = "null";
        if (mMd5 != null) {
            md5Str = mMd5;
        }

        String eventStr = Integer.toString(getCurrState())
                            + SEPERATOR + getPkgName()
                            + SEPERATOR + getDownloadUrl()
                            + SEPERATOR + mAppName
                            + SEPERATOR + md5Str
                            + SEPERATOR + Long.toString(getTotalSize())
                            + SEPERATOR + Integer.toString(mDownloadArray)
                            + SEPERATOR + getFileName()
                            + SEPERATOR + Boolean.toString(mOnlyDownInWifi)
                            + SEPERATOR + mFlag
                            + SEPERATOR + mVersionCode
                            + SEPERATOR + mAppId
                            + SEPERATOR + mTopicId
                            + SEPERATOR + Integer.toString(getDiffUpdateState())
                            + SEPERATOR + (getDiffDownloadUrl() == null ? "null" : getDiffDownloadUrl())
                            + SEPERATOR + Long.toString(getDiffFileSize())
                            + SEPERATOR + mListenPkgName
                            + SEPERATOR + mListenClassName
                            + SEPERATOR + getSortTime();
        return eventStr;
    }

    public String getSpeedEventStr() {
        String md5Str = "null";
        if (mMd5 != null) {
            md5Str = mMd5;
        }

        String eventStr = Integer.toString(getCurrState())
                            + SEPERATOR + getPkgName()
                            + SEPERATOR + getDownloadUrl()
                            + SEPERATOR + mAppName
                            + SEPERATOR + md5Str
                            + SEPERATOR + Long.toString(getTotalSize())
                            + SEPERATOR + Integer.toString(mDownloadArray)
                            + SEPERATOR + getFileName()
                            + SEPERATOR + Boolean.toString(mOnlyDownInWifi)
                            + SEPERATOR + mFlag
                            + SEPERATOR + mVersionCode
                            + SEPERATOR + mAppId
                            + SEPERATOR + mTopicId
                            + SEPERATOR + Integer.toString(getDiffUpdateState())
                            + SEPERATOR + (getDiffDownloadUrl() == null ? "null" : getDiffDownloadUrl())
                            + SEPERATOR + Long.toString(getDiffFileSize())
                            + SEPERATOR + mListenPkgName
                            + SEPERATOR + mListenClassName
                            + SEPERATOR + getSortTime()
                            + SEPERATOR + Float.toString(getDownloadSpeed())
                            + SEPERATOR + Long.toString(super.getCurrDownloadSize());
        return eventStr;
    }

    
    private DownloadPool mDownPool = null;
    private DownThread mDownThread = null;
    
    public void setThreadCheckInfo(DownloadPool downPool, DownThread downThread){
        mDownPool = downPool;
        mDownThread = downThread;
    }
    
    public void watchDog(){
        if(mDownPool != null && mDownThread != null){
            mDownPool.downThreadWatchDog(mDownThread);
        }
    }
    
    
    public boolean isRenameingTmpFile() {
        return isRenameing;
    }
    
    
    public static File getSelfApkFile(String apkFileName) {
        String filePath = SdcardUtil.getSdcardPath() + DOWNLOAD_DIR_PATH
                + File.separator + Custom.UPDATE_DIR_NAME
                + File.separator + apkFileName + ".apk";
        return new File(filePath);
    }

    public static File getSelfApkTmpFile(String apkFileName) {
        String filePath = SdcardUtil.getSdcardPath() + DOWNLOAD_DIR_PATH
                + File.separator + Custom.UPDATE_DIR_NAME
                + File.separator + apkFileName + ".apk.tmp";
        return new File(filePath);
    }


    private static String getApkFileNameWithoutSuffix(String md5, String appName, String pkgName, int verCode) {
        if (isSelfDownloadEvent(md5, appName)) {
            return appName;
        } else {
            String fileName = appName + "_" + pkgName + "_" + Integer.toString(verCode);
            return fileName;
        }
    }
    
    
    public String getTopicId() {
    	return mTopicId;
    }
    

    public int getAppId() {
        return mAppId;
    }

    public int getVersionCode() {
        return mVersionCode;
    }


    public String getMd5() {
        return mMd5;
    }

    public String getAppName() {
        return mAppName;
    }


    public int getEventArray() {
        return mDownloadArray;
    }


    @Override
    public String getDownloadDirPath() {
        if (isSelfApk) {
            return SdcardUtil.getSdcardPath() + DOWNLOAD_DIR_PATH + "/"
                    + Custom.UPDATE_DIR_NAME;
        } else {
            return SdcardUtil.getSdcardPath() + DOWNLOAD_DIR_PATH;
        }
    }

    public boolean isSelfApk() {
        return isSelfApk;
    }

    public boolean isOnlyDownInWifi() {
        return mOnlyDownInWifi;
    }

    public void setOnlyDownInWifi(boolean flag) {
        if(mOnlyDownInWifi == false) {
            return;
        }
        mOnlyDownInWifi = flag;
    }

    public String getDownloadFlag() {
        return mFlag;
    }
    
    
    public void setDownloadFlag(String flag) {
    	mFlag = flag;
    }


    public void setEventArray(int eventArray) {
        mDownloadArray = eventArray;
    }


    @Override
    public void readyToDownload() {
    	super.readyToDownload();
        mDownloadArray = ARRAY_WAITING;
    }

    
    private static final Object PATCH_KEY = new Object();

    public void downloadComplete(Context context) {
        isRenameing = true;

        if (!getApkFile().exists()) {
	        File downFile = getDownloadFile();
	        if (downFile.exists()) {
	        	if (isDiffDownloadNow()) {
	        		if(mDownPool != null && mDownThread != null) {
	                    mDownPool.downThreadWatchDog(mDownThread);
	        			mDownThread.startPatchDiff();
	        		}
	        		int diffResult = -1;
	        		if (Util.isMemoryAvailableToDiffPath(context, getPkgName(), getTotalSize())) {
		        		synchronized (PATCH_KEY) {
			            	diffResult = APKPatchUtil.patchApk(context, getPkgName(), getDownloadFilePath(), getApkFile().getAbsolutePath());
		        		}
	        		}
		            if (mDownPool != null && mDownThread != null) {
		            	mDownPool.downThreadWatchDog(mDownThread);
		            	mDownThread.endPatchDiff();
		            }
		            if (diffResult == 0) {
		            	//success
		            	downFile.delete();
		            	setTotalSize(getApkFile().length());
		            	
		            } else {
		            	//failed
		            	downFile.delete();
		            	getApkFile().delete();
		            	diffUpdateFailed();
		            	isRenameing = false;
		            	return;
		            }
	        	} else {
	        		if (mVersionCode <= 0) {
		                PackageInfo pkgInfo = context.getPackageManager()
		                        .getPackageArchiveInfo(downFile.getAbsolutePath(), 0);
		                if (pkgInfo != null) {
		                    mVersionCode = pkgInfo.versionCode;
		                }
		            }
	        		
	        		File renameFile = getApkFile();
	            	if (renameFile.exists()) {
	            		renameFile.delete();
	            	}
	            	downFile.renameTo(renameFile);
	        	}
	        }
        }
        if (mDownloadArray != ARRAY_BACKGROUND) {
        	mDownloadArray = ARRAY_COMPLETE;
        }
        super.downloadComplete();
        isRenameing = false;
    }

    
    @Override
    public void installed() {
    	super.installed();
        if (mDownloadArray != ARRAY_BACKGROUND) {
            mDownloadArray = ARRAY_COMPLETE;
        }
    }

    
    @Override
    public void installFailed() {
    	super.installFailed();
        if (mDownloadArray != ARRAY_BACKGROUND) {
            mDownloadArray = ARRAY_COMPLETE;
        }
    }

    
    @Override
    public void downloading() {
    	super.downloading();
        if (mDownloadArray != ARRAY_BACKGROUND
                && mDownloadArray != ARRAY_UPDATE) {
            mDownloadArray = ARRAY_DOWNLOADING;
        }
        //mCurrSize = getDownloadFileSize();
    }

    
    @Override
    public void downloadPause() {
    	super.downloadPause();
        if (mDownloadArray != ARRAY_BACKGROUND
                && mDownloadArray != ARRAY_UPDATE) {
            mDownloadArray = ARRAY_PAUSED;
        }
        //mCurrSize = getDownloadFileSize();
    }

    
    @Override
    public void downloadFailed() {
    	super.downloadFailed();
        if (mDownloadArray != ARRAY_BACKGROUND
                && mDownloadArray != ARRAY_UPDATE) {
            mDownloadArray = ARRAY_PAUSED;
        }
        //mCurrSize = getDownloadFileSize();
    }
    
    
    @Override
	public void networkDisconnect() {
		super.networkDisconnect();
		if (mDownloadArray != ARRAY_BACKGROUND
                && mDownloadArray != ARRAY_UPDATE) {
            mDownloadArray = ARRAY_PAUSED;
        }
	}

	public void setVersionCode(int verCode) {
        mVersionCode = verCode;
    }
}
