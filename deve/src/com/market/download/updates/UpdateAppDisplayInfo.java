package com.market.download.updates;

import java.io.File;

public class UpdateAppDisplayInfo {

    private String mPkgName;
    private String mAppName;
    private int mVerCode;
    private String mVerUpStr;
    private long mFileSize;
    private String mTmpFileName;
    private String mApkFileName;
    private String mMd5;
    private String mDownUrl;
    private int mApkId;
    private String mDiffDownUrl;
    private long mDiffPatchSize;
    private String mVerUptDes;
	private String mVerUptTime;//版本更新时间

	private boolean isAutoDownloaded = false;

	private boolean isInstalled = false;
    private static final String SEPERATOR = ",,";
    public UpdateAppDisplayInfo(String infoStr) {
        String[] infoList = infoStr.split(SEPERATOR);
        mPkgName = infoList[0];
        mAppName = infoList[1];
        mVerCode = Integer.parseInt(infoList[2]);
        mVerUpStr = infoList[3];
        mFileSize = Long.parseLong(infoList[4]);
        mTmpFileName = infoList[5];
        isAutoDownloaded = (infoList[6].equals("1")) ? true : false;
        isInstalled = (infoList[7].equals("1")) ? true : false;
        mApkFileName = infoList[8];
        if (infoList.length >= 12) {
            mMd5 = infoList[9];
            mDownUrl = infoList[10];
            mApkId = Integer.parseInt(infoList[11]);
        }
        
        if (infoList.length >= 14) {
        	mDiffDownUrl = infoList[12];
        	if (mDiffDownUrl.equals("null")) {
        		mDiffDownUrl = null;
        	}
        	mDiffPatchSize = Long.parseLong(infoList[13]);
        }
        
        if(infoList.length >= 16) {
        	mVerUptDes = infoList[14];
        	mVerUptTime = infoList[15];
        	if (mVerUptDes.equals("null")) {
        		mVerUptDes = null;
        	}

        	if (mVerUptTime.equals("null")) {
        		mVerUptTime = null;
        	}
        }
        
    }
    public UpdateAppDisplayInfo(String pkgName, String appName, int verCode,
            String verUpStr, long fileSize, String tmpFileName,
            String apkFileName, String md5, String downUrl, int apkId, String diffDownUrl, long diffPatchSize, String verUptDes, String verUptTime) {
        mPkgName = pkgName;
        mAppName = appName;
        mVerCode = verCode;
        mVerUpStr = verUpStr;
        mFileSize = fileSize;
        mTmpFileName = tmpFileName;
        mApkFileName = apkFileName;
        mMd5 = md5;
        mDownUrl = downUrl;
        mApkId = apkId;
        mDiffDownUrl = diffDownUrl;
        mDiffPatchSize = diffPatchSize;
        mVerUptDes = verUptDes;
        mVerUptTime = verUptTime;
    }
    
    
    public void autoUpdateDownloaded() {
        isAutoDownloaded = true;
    }

    
    public File getApkFile() {
        return new File(mApkFileName);
    }

	public int getApkId() {
        return mApkId;
    }


	
	public String getAppName() {
        return mAppName;
    }
    
    public long getCurrDownSize() {
        File tmpFile = new File(mTmpFileName);
        return tmpFile.length();
    }
    
    public String getDiffDownUrl() {
    	return mDiffDownUrl;
    }

    public Long getDiffPatchSize() {
		return mDiffPatchSize;
	}
    
    public String getDownUrl() {
        return mDownUrl;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public String getInfoStr() {
        String infoStr = mPkgName
                        + SEPERATOR + mAppName
                        + SEPERATOR + Integer.toString(mVerCode)
                        + SEPERATOR + mVerUpStr
                        + SEPERATOR + Long.toString(mFileSize)
                        + SEPERATOR + mTmpFileName
                        + SEPERATOR + (isAutoDownloaded? "1" : "0")
                        + SEPERATOR + (isInstalled? "1" : "0")
                        + SEPERATOR + mApkFileName
                        + SEPERATOR + mMd5
                        + SEPERATOR + mDownUrl
                        + SEPERATOR + Integer.toString(mApkId)
                        + SEPERATOR + (mDiffDownUrl == null ? "null" : mDiffDownUrl)
                        + SEPERATOR + mDiffPatchSize
        				+ SEPERATOR + mVerUptDes
        				+ SEPERATOR + mVerUptTime;
        return infoStr;
    }

    public String getMd5() {
        return mMd5;
    }

    public String getmVerUptDes() {
		return mVerUptDes;
	}

    public String getmVerUptTime() {
		return mVerUptTime;
	}

    public String getPkgName() {
        return mPkgName;
    }

    public int getVerCode() {
        return mVerCode;
    }

    public String getVerUpStr() {
        return mVerUpStr;
    }

    public void installed() {
        isInstalled = true;
    }
    
    public void unInstalled() {
        isInstalled = false;
    }

    public boolean isApkFileExist() {
        File apkFile = new File(mApkFileName);
        if (apkFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAutoDownloaded() {
        return isAutoDownloaded;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setmVerUptDes(String mVerUptDes) {
		this.mVerUptDes = mVerUptDes;
	}

    public void setmVerUptTime(String mVerUptTime) {
		this.mVerUptTime = mVerUptTime;
	}
}
