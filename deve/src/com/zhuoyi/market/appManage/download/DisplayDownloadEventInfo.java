package com.zhuoyi.market.appManage.download;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.market.download.userDownload.DownloadEventInfo;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 下载管理展示数据
 * @author dream.zhou
 */
public class DisplayDownloadEventInfo {
    
    private DownloadEventInfo mDownloadEventInfo = null;
    private String mDisplayAppName = "";
    private String mDisplayDownloadedSize = "";
    private String mDisplayTotleSize = "";
    private String mDisplaySpeed = "";
    private String mDisplayVersion = "";
    private boolean mDisplayInstalled = false;
    private boolean mDisplayInstalling = false;
    private boolean mDisplayFileExist = false;
    private int mDisplayPecent = 1;
    
    public DisplayDownloadEventInfo(DownloadEventInfo eventInfo) {
        mDownloadEventInfo = eventInfo;
    }
    
    
    public void initDisplayInfo(boolean download) {
        setDisplayAppName();
        if (download) {
            setDisplaySpeed();
            setDisplayPecent();
            setDisplayDownloadedSize();
            setDisplayTotleSizeForDownloading();
        } else {
            setDisplayVersion();
            setDisplayTotleSizeForDownloaded(); 
            setDisplayInstalling();
        }
    }
    
    
    public void refreshDisplayInfo(boolean download) {
        if (download) {
            setDisplaySpeed();
            setDisplayPecent();
            setDisplayDownloadedSize();
            setDisplayTotleSizeForDownloading();
        } else {
        	setDisplayInstalling();
            setDisplayInstalled();
            setDisplayFileExist();
        }
    } 
    
    
    public void setDownloadEventInfo(DownloadEventInfo eventInfo) {
        mDownloadEventInfo = eventInfo;
    }
    
    
    public DownloadEventInfo getDownloadEventInfo() {
        return mDownloadEventInfo;
    }
    
    
    public String getDisplayAppName() {
        return mDisplayAppName;
    }
    
    
    public String getDisplayDownloadedSize() {
        return mDisplayDownloadedSize;
    }
    
    
    public String getDisplayTotleSize() {
        return mDisplayTotleSize;
    }
    
    
    public String getDisplaySpeed() {
        return mDisplaySpeed;
    }
    
    
    public String getDisplayVersion() {
        return mDisplayVersion;
    }
    
    
    public boolean getDisplayInstalled() {
        return mDisplayInstalled;
    }
    
    
    public boolean getDisplayInstalling() {
        return mDisplayInstalling;
    }    
    
    
    public boolean getDisplayFileExist() {
        return mDisplayFileExist;
    }
    
    
    public int getDisplayPecent() {
        return mDisplayPecent;
    }
    
    
    public void setDisplayInstalling() {
    	if (mDownloadEventInfo != null) {
    		if (DownloadEventInfo.STATE_INSTALLING == mDownloadEventInfo.getCurrState()) {
    			mDisplayInstalling = true;
    		} else {
    			mDisplayInstalling = false;
    		}
        } else {
        	mDisplayInstalling = false;
        }
    }   
    
    
    private void setDisplayAppName() {
        String appName = "";
        if (mDownloadEventInfo != null) {
            String pkgName = mDownloadEventInfo.getPkgName();
            if ("com.zhuoyi.market".equals(pkgName)) {
                appName = MarketApplication.getRootContext().getString(R.string.app_name);;
            } else {
                appName = mDownloadEventInfo.getAppName();
                if (!TextUtils.isEmpty(appName) 
                        && !TextUtils.isEmpty(pkgName) 
                        && appName.contains(pkgName)) {
                    appName = appName.replace(pkgName, ""); 
                }
            }
        }
        mDisplayAppName = appName;
    }

    
    private void setDisplayDownloadedSize() {
        String downloadedSize = "0.00B";
        if (mDownloadEventInfo != null) {
            long size = mDownloadEventInfo.getCurrDownloadSize();
            if(size>0) {
                downloadedSize = getFileSizeSum(size);
            }
        }
        mDisplayDownloadedSize = downloadedSize;
    }
    
    
    private void setDisplayTotleSizeForDownloading() {
        String totleSize = MarketApplication.getRootContext().getString(R.string.unknow_data);
        if (mDownloadEventInfo != null) {
            long size = 0L;
            if (mDownloadEventInfo.isDiffPathUpdateNow()) {
                size = mDownloadEventInfo.getDiffFileSize();
            } else {
                size = mDownloadEventInfo.getTotalSize();
            }
            if(size>0) {
                totleSize = getFileSizeSum(size);
            }
        }
        mDisplayTotleSize = totleSize;
    }
    
    
    private void setDisplayTotleSizeForDownloaded() {
        String totleSize = MarketApplication.getRootContext().getString(R.string.unknow_data);
        if (mDownloadEventInfo != null) {
            long size = mDownloadEventInfo.getTotalSize();
            if (size <= 0) {
                try {
                    FileInputStream fis = new FileInputStream(mDownloadEventInfo.getApkFile());
                    size = fis.available();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    size = 0;
                }
            }
            if(size>0) {
                totleSize = getFileSizeSum(size);
            }
        }
        mDisplayTotleSize = totleSize;
    }    
    
    
    public String getFileSizeSum(long size) {
        String s = "";
        if (size < 1024) {
            s = new DecimalFormat("#.00").format(size);
            s += "B";
        } else if (size < 1024 * 1024) {
            float size1 = (float) (size / (1024.00));
            s = new DecimalFormat("#.00").format(size1);
            s += "KB";
        } else {
            float size1 = (float) (size / (1024.00 * 1024));
            s = new DecimalFormat("#.00").format(size1);
            s += "MB";
        }
        return s;
    } 
    
    
    private void setDisplayPecent() {
        mDisplayPecent = 1;
        if (mDownloadEventInfo != null) {
            long size = mDownloadEventInfo.getTotalSize();
            if (mDownloadEventInfo.isDiffPathUpdateNow()) {
                size = mDownloadEventInfo.getDiffFileSize();
            }
            
            float _pecent = (float)mDownloadEventInfo.getCurrDownloadSize()/size*1.0f;
            int _rate = (int)(_pecent*100);
            if(_rate>100)
                _rate = 100;
            if(_rate == 0)
                _rate = 1;
            if(size==0)
                _rate = 1;
            mDisplayPecent = _rate;
        } 
    }
    
    
    private void setDisplaySpeed() {
        String speed = "0.00 B/s";
        if (mDownloadEventInfo != null) {
            speed = getCountSpeedInfo(mDownloadEventInfo.getDownloadSpeed());
        }
        mDisplaySpeed = speed;
    }
    
    
    private String getCountSpeedInfo(float mCurV) {
        String s = "";
        String scurV = "";
        mCurV = mCurV * 1000;
        if (mCurV > 0) {
            if (mCurV < 1024) {
                scurV = new DecimalFormat("#.00").format(mCurV);
                scurV += " B/s";
            } else if (mCurV < 1024 * 1024) {
                float curV1 = (float) (mCurV / (1024.00));
                scurV = new DecimalFormat("#.00").format(curV1);
                scurV += " KB/s";
            } else {
                float curV1 = (float) (mCurV / (1024.00 * 1024));
                scurV = new DecimalFormat("#.00").format(curV1);
                scurV += " MB/s";
            }
        } else {
            scurV += "0.00 B/s";
        }

        s = "[" + scurV + "]";
        return s;
    }
    
    
    private void setDisplayVersion () {
        String ver = "";
        mDisplayFileExist = false;
        if (mDownloadEventInfo != null) {
            ver = getApkFileVersionName();
            if (TextUtils.isEmpty(ver)) {
                ver = MarketUtils.getInstalledApkVersionName(MarketApplication.getRootContext(), mDownloadEventInfo.getPkgName()); 
            } else {
                mDisplayFileExist = true;
            }
        }
        
        String version = MarketApplication.getRootContext().getString(R.string.detail_version);
        if (TextUtils.isEmpty(ver)) {
            mDisplayVersion = version + MarketApplication.getRootContext().getString(R.string.unknow_data);
        } else {
            mDisplayVersion = version + ver;
        }
    }

    
    private String getApkFileVersionName() {
        String ver = "";
        try {
            String pkgPath = mDownloadEventInfo.getApkFile().getAbsolutePath();
            if (!TextUtils.isEmpty(pkgPath)) {
                PackageInfo pkgInfo = MarketApplication.getRootContext().getPackageManager().getPackageArchiveInfo(pkgPath, 0);  
                ver = pkgInfo.versionName;
            }
        } catch (Exception e) {
            ver = "";
        } 
        
        return ver;
    }
    
    
    public void setDisplayInstalled() {
        if (mDownloadEventInfo != null) {
            mDisplayInstalled = !MarketUtils.checkApkShouldShowInList(MarketApplication.getRootContext(), mDownloadEventInfo.getPkgName(), mDownloadEventInfo.getVersionCode());
        } else {
            mDisplayInstalled = false;
        }
    }
    
    
    public void setDisplayFileExist() {
        try {
            File file = mDownloadEventInfo.getApkFile();
            if (file != null && file.exists()) {
                mDisplayFileExist = true;
            } else {
                mDisplayFileExist = false;
            }
        } catch (Exception e) {
            mDisplayFileExist = false;
        } 
    }
}
