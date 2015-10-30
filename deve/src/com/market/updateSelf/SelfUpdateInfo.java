package com.market.updateSelf;

import java.io.File;

import android.content.Context;

public class SelfUpdateInfo {
    
    //下载状态
    public static final int STATE_READY = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_COMPLETE = 4;
    
    //更新类型
    public static final int SELF_UPDATE_TYPE_1 = 1;//强制更新
    public static final int SELF_UPDATE_TYPE_2 = 2;//提示更新
    public static final int SELF_UPDATE_TYPE_3 = 3;//不更新
    public static final int SELF_UPDATE_TYPE_4 = 4;//后台更新

    //下载方式
    public static final int TYPE_NORMAL_DOWN = 1;
    public static final int TYPE_DOWNLOAD_BG = 2;

    //自更新信息
    private String mTitle = null;
    private String mMd5 = null;
    private String mDownloadUrl = null;
    private int mUpdateType = -1;
    private int mVersionCode = 0;
    private String mContent = null;
    private long mTotalSize = 0L;

    //自更新文件名
    private String mFileName = null;
    private String mApkFileName = null;
    public static final String FILE_NAME_PRE = "Update_";

    //下载状态，默认准备下载
    private int mState = STATE_READY;


    /**
     * 构造函数，通过set方法来初始化各个变量
     */
    SelfUpdateInfo() {
        //nothing
    }
    
    /**
     * 构造函数，初始化数据
     * @param title
     * @param content
     * @param md5Str
     * @param urlStr
     * @param downType
     * @param verCode
     */
    SelfUpdateInfo(String title, String content, String md5Str, String urlStr, int downType, int verCode) {
        mTitle = title;
        mContent= content;
        mMd5 = md5Str;
        mDownloadUrl = urlStr;
        mUpdateType = downType;
        mVersionCode = verCode;
        mState = STATE_READY;
        mFileName = FILE_NAME_PRE + mMd5 + "_" + mVersionCode + ".apk.tmp";
        mApkFileName = FILE_NAME_PRE + mMd5 + "_" + mVersionCode + ".apk";
    }
    
    /**
     * 设置跟新包名字
     */
    public void setFileName() {
        UpdateUtil.logE("SelfUpdateInfo", "setFileName", "mMd5=" + mMd5);
        UpdateUtil.logE("SelfUpdateInfo", "setFileName", "mVersionCode=" + mVersionCode);
        mFileName = FILE_NAME_PRE + mMd5 + "_" + mVersionCode + ".apk.tmp";
        mApkFileName = FILE_NAME_PRE + mMd5 + "_" + mVersionCode + ".apk";
    }
    
    /**
     * 设置title
     * @param title
     */
    public void setTitle(String title) {
        mTitle = title;
    }
    
    /**
     * 获取title
     * @return
     */
    public String getTitle() {
        return mTitle;
    }
    
    /**
     * 设置更新内容
     * @param content
     */
    public void setContent(String content) {
        mContent = content;
    }

    /**
     * 获取更新内容
     * @return
     */
    public String getContent() {
        return mContent;
    }
    
    /**
     * 设置md5码
     * @param md5
     */
    public void setMd5(String md5) {
        mMd5 = md5;
    }
    
    /**
     * 获取md5码
     * @return
     */
    public String getMd5() {
        return mMd5;
    }
    
    /**
     * 设置下载地址
     * @param url
     */
    public void setDownloadUrl(String url) {
        mDownloadUrl = url;
    }
    
    /**
     * 获取下载地址
     * @return
     */
    public String getDownloadUrl() {
        return mDownloadUrl;
    }
    
    /**
     * 设置自更新更新类型
     * @param type
     */
    public void setUpdateType(int type) {
        mUpdateType = type;
    }
    
    /**
     * 获取自更新类型
     * @return
     */
    public int getUpdateType() {
        return mUpdateType;
    }
    
    /**
     * 设置版本号
     * @param verCode
     */
    public void setVersionCode(int verCode) {
            mVersionCode = verCode;
    }
    
    /**
     * 获取版本号
     * @return
     */
    public int getVersionCode() {
        return mVersionCode;
    }
    
    
    /**
     * 设置文件大小
     * @param fileSize
     */
    public void setTotalSize(long fileSize) {
        mTotalSize = fileSize;
    }
    
    /**
     * 获取文件大小
     * @return
     */
    public long getTotalSize() {
        return mTotalSize;
    }
    
    /**
     * 获取已下载文件大小
     * @return
     */
    public long getCurrDownloadSize() {
        return UpdateUtil.getDownloadFileSize(mFileName);
    }
    
    /**
     * 设置下载状态
     * @param state
     */
    public void setDownloadState(int state) {
        mState = state;
        //下载完成，更改文件名
        if (mState == STATE_COMPLETE) {
            File tmpFile = getDownloadFile();
            mFileName = FILE_NAME_PRE + mMd5 + "_" + mVersionCode + ".apk";
            tmpFile.renameTo(getDownloadFile()); 
        }
    }
    
    /**
     * 保存、设置下载状态
     * @param context
     * @param state
     */
    public void setDownloadState(Context context, int state) {
        UpSelfStorage.saveDownloadState(context, state);
        setDownloadState(state);
    }
    
    
    /**
     * 获取下载状态
     * @return
     */
    public int getDownloadState() {
        return mState;
    }

    /**
     * 获取下载文件（.apk.tmp）
     * @return
     */
    public File getDownloadFile() {
        return UpdateUtil.getDownloadFile(mFileName);
    }

    /**
     * 获取包文件（.apk）
     * @return
     */
    public File getApkFile() {
        UpdateUtil.logE("SelfUpdateInfo", "getApkFile", "mApkFileName=" + mApkFileName);
        return UpdateUtil.getDownloadFile(mApkFileName);
    }

    /**
     * 获取下载文件路径
     * @return
     */
    public String getDownloadFilePath() {
        return UpdateUtil.getDownloadPath() + File.separator + mFileName;
    }
}
