package com.market.download.common;

import java.io.File;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.util.SdcardUtil;
import com.market.download.util.Util;
import com.zhuoyi.market.constant.Constant;

public class DownBaseInfo {
	private static final String TAG = "DownBaseInfo";
	
	public static final int STATE_DELAY = -1;
	public static final int STATE_READY = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_NETWORK_DISCONNECT = 2;
    public static final int STATE_DOWNLOAD_PAUSE = 3;
    public static final int STATE_DOWNLOAD_FAILED = 4;
    public static final int STATE_DOWNLOAD_COMPLETE = 5;
    public static final int STATE_INSTALLING = 6;
    public static final int STATE_INSTALLED = 7;
    public static final int STATE_INSTALL_FAILED = 8;
    public static final int STATE_CANCEL = 9;
	
    public static final int DIFF_UP_STATE_READY = 0;
    public static final int DIFF_UP_STATE_FAILED = 1;
    
	protected static final String DOWNLOAD_DIR_PATH = Constant.download_dir_name;
	private static final char UNVAVAIBLE_REPLACE_CHAR = '_';
	
	private int mState = STATE_READY;
	private String mPkgName;
	private String mFileName;
	private String mDownloadUrl;
	private String mDiffDownUrl;
	private int mDiffUpState;
	
	private long mDiffSize = 0;
	private long mTotalSize = 0;
	private long mCurrSize = -1;
	
	private float mDownloadSpeed = 0;
	
	//排序
	private long mSortTime = 0;
	
	protected DownBaseInfo(String pkgName, String fileName, String downloadUrl, String diffDownUrl, long totalSize, long diffSize) {
		mPkgName = pkgName;
		mFileName = fileName;
		mDownloadUrl = downloadUrl;
		mDiffDownUrl = diffDownUrl;
		if (mDiffDownUrl != null) {
			mDiffUpState = DIFF_UP_STATE_READY;
		} else {
			mDiffUpState = DIFF_UP_STATE_FAILED;
		}
		formatFileName();
		
		mTotalSize = totalSize;
		mDiffSize = diffSize;
		
		mSortTime = System.currentTimeMillis();
	}
	
	
	protected DownBaseInfo(String pkgName, String fileName, String downloadUrl, long totalSize) {
		mPkgName = pkgName;
		mFileName = fileName;
		mDownloadUrl = downloadUrl;
		mDiffDownUrl = null;
		mDiffUpState = DIFF_UP_STATE_FAILED;
		
		formatFileName();
		
		mTotalSize = totalSize;
		
		mSortTime = System.currentTimeMillis();
	}
	
	
	protected DownBaseInfo(String downEventStr) {
		String[] infoList = downEventStr.split(DownloadEventInfo.SEPERATOR);
		mState = Integer.parseInt(infoList[0]);
		mPkgName = infoList[1];
		mDownloadUrl = infoList[2];
		mTotalSize = 0;
        try {
        	mTotalSize = Long.parseLong(infoList[5]);
        } catch (Exception e) {
            Util.log(TAG, "DownBaseInfo", "eventStr:" + downEventStr);
        }
        mFileName = infoList[7];
        
        mDiffUpState = DIFF_UP_STATE_FAILED;
        
        if (infoList.length >= 16) {
        	mDiffUpState = Integer.parseInt(infoList[13]);
        	mDiffDownUrl = infoList[14];
        	if (mDiffDownUrl.equals("null")) {
        		mDiffDownUrl = null;
        	}
        	mDiffSize = Long.parseLong(infoList[15]);
        }
        
        if (infoList.length >= 19) {
            mSortTime = Long.parseLong(infoList[18]);
        }
        
        if (infoList.length >= 20) {
        	mDownloadSpeed = Float.parseFloat(infoList[19]);
        }
        
        if (infoList.length >= 21) {
            mCurrSize = Long.parseLong(infoList[20]);
        }
	}
	
	
	/**
     * For get the download event current state
     * 
     * @return {@link #STATE_READY} means ready to download<br/>
     *         {@link #STATE_DOWNLOADING} means event is downloading now<br/>
     *         {@link #STATE_DOWNLOAD_PAUSE} means event has been paused by user<br/>
     *         {@link #STATE_DOWNLOAD_COMPLETE} means apk has download complete<br/>
     *         {@link #STATE_DOWNLOAD_FAILED} means apk download failed, it just
     *         like the pause state<br/>
     *         {@link #STATE_INSTALLED} means apk has been installed<br/>
     *         {@link #STATE_INSTALL_FAILED} means apk install failed<br/>
     *         {@link #STATE_CANCEL} means this download event has been canceled
     *         by user<br/>
     *         {@link #STATE_NETWORK_DISCONNECT} means this download event is
     *         paused when network disconnect<br/>
     *         {@link #STATE_INSTALLING} means the apk file is installing
     */
	public int getCurrState() {
		return mState;
	}
	
	
	public long getDiffFileSize() {
		return mDiffSize;
	}
	
	
	public void setDiffFileSize(long diffFileSize) {
		mDiffSize = diffFileSize;
	}
	
	
	public int getDiffUpdateState() {
		return mDiffUpState;
	}
	
	
	public void diffUpdateFailed() {
		mDiffUpState = DIFF_UP_STATE_FAILED;
	}
	
	
	public boolean isDiffPathUpdateNow() {
		if (mDiffUpState == DIFF_UP_STATE_READY) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public String getDiffDownloadUrl() {
		return mDiffDownUrl;
	}
	
	
	public void delayToDownload() {
	    mState = STATE_DELAY;
	}
	
	
	public void readyToDownload() {
        mState = STATE_READY;
    }
	
	
	public void downloadComplete() {
        mState = STATE_DOWNLOAD_COMPLETE;
        mSortTime = System.currentTimeMillis();
    }

	
    public void installed() {
        mState = STATE_INSTALLED;
        mSortTime = System.currentTimeMillis();
    }

    
    public void installFailed() {
        mState = STATE_INSTALL_FAILED;
    }

    
    public void downloading() {
        mState = STATE_DOWNLOADING;
        mCurrSize = getDownloadFileSize();
    }

    
    public void downloadPause() {
        mState = STATE_DOWNLOAD_PAUSE;
        mCurrSize = getDownloadFileSize();
    }
    

    public void downloadFailed() {
        mState = STATE_DOWNLOAD_FAILED;
        mCurrSize = getDownloadFileSize();
    }

    
    public void cancelEvent() {
        mState = STATE_CANCEL;
        mCurrSize = 0;
    }

    
    public void networkDisconnect() {
        mState = STATE_NETWORK_DISCONNECT;
    }

    
    public void installingApk() {
        mState = STATE_INSTALLING;
    }
	
	
	public void setDownloadSpeed(float downSpeed) {
		mDownloadSpeed = downSpeed;
	}
	
	
	public float getDownloadSpeed() {
		return mDownloadSpeed;
	}
	
	
	public void setTotalSize(long totalSize) {
		mTotalSize = totalSize;
	}
	
	
	public long getTotalSize() {
		return mTotalSize;
	}
	
	
	public String getDownloadUrl() {
		if (mDiffUpState == DIFF_UP_STATE_READY && mDiffDownUrl != null) {
			return mDiffDownUrl;
		} else {
			return mDownloadUrl;
		}
	}
	
	
	public String getFileName() {
		return mFileName;
	}
	
	
	public String getPkgName() {
		return mPkgName;
	}
	
	
	public void setCurrDownloadSize(long currSize) {
		mCurrSize = currSize;
	}
	
	
	public long getCurrDownloadSize() {
		if (mCurrSize == -1) {
            mCurrSize = getDownloadFileSize();
        }
        return mCurrSize;
	}
	
	
	public long syncCurrDownloadSize() {
	    mCurrSize = getDownloadFileSize();
	    return mCurrSize;
	}
	
	
	protected boolean isDiffDownloadNow() {
		if (mDiffUpState == DIFF_UP_STATE_READY && mDiffDownUrl != null) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public File getApkFile() {
        File apkFile = new File(getDownloadDirPath() + File.separator + mFileName + ".apk");
        return apkFile;
    }
	
	
	public File getDownloadFile() {
		File downloadFile = new File(getDownloadFilePath());
        return downloadFile;
    }
	
	
	public String getDownloadFilePath() {
		String FilePath = null;
		if (isDiffDownloadNow()) {
			FilePath = getDownloadDirPath() + File.separator + mFileName + ".diff.tmp";
		} else {
			FilePath = getDownloadDirPath() + File.separator + mFileName + ".apk.tmp";
		}
		
        return FilePath;
    }
	
	
	public String getDownloadDirPath() {
        return SdcardUtil.getSdcardPath() + DOWNLOAD_DIR_PATH;
    }
	
	
	private long getDownloadFileSize() {
        File downloadFile = getDownloadFile();
        return downloadFile.length();
    }
	
	
	private void formatFileName() {
		mFileName = mFileName.replace('\\', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('/', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace(':', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('*', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('?', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('\"', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('<', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('>', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace('|', UNVAVAIBLE_REPLACE_CHAR);
		mFileName = mFileName.replace(' ', UNVAVAIBLE_REPLACE_CHAR);
    }
	
	
	public long getSortTime() {
	    return mSortTime;
	}
}
