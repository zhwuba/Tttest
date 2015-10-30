package com.freeme.themeclub.wallpaper.resource;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class Resource {

    public static final String AUTHOR = "AUTHOR";
    public static final String CREATED_TIME = "CREATED_TIME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DESIGNER = "DESIGNER";
    public static final String DOWNLOAD_COUNT = "DOWNLOAD_COUNT";
    public static final String ID = "ID";
    
    public static final String LOCAL_PATH = "LOCAL_PATH";
    public static final String LOCAL_PREVIEWS = "LOCAL_PREVIEW";
    public static final String LOCAL_THUMBNAILS = "LOCAL_THUMBNAIL";
    
    public static final String MODIFIED_TIME = "MODIFIED_TIME";
    public static final String NAME = "NAME";
    public static final String NVP = "NVP";
    
    public static final String ONLINE_PATH = "ONLINE_PATH";
    public static final String ONLINE_PREVIEWS = "ONLINE_PREVIEW";
    public static final String ONLINE_THUMBNAILS = "ONLINE_THUMBNAIL";
    
    public static final String PLATFORM_VERSION = "PLATFORM_VERSION";
    public static final String SIZE = "SIZE";
    public static final String RESOLUTION = "RESOLUTION";
    public static final String STATUS = "STATUS";
    public static final String VERSION = "VERSION";
    
    public static final int RESOURCE_LATEST_VERSION = 0;
    public static final int RESOURCE_NOT_EXIST = 2;
    public static final int RESOURCE_OLD_VERSION = 1;
    
    private String mTitle;
    private String mId;
    private String mDescription;
    private String mDividerTitle;
    private String mFileHash;
    private long mFileModifiedTime;
    private long mFileSize;
    private Bundle mInformation;
    private int mPlatformVersion;
    private int mStatus;
    private String mResolution;
    
    private String mLocalPath;
    private List<String> mLocalPreviews;
    private List<String> mLocalThumbnails;
    
    private String mOnlinePath;
    private List<String> mOnlinePreviews;
    private List<String> mOnlineThumbnails;
    
    public Resource() {
        mLocalThumbnails = new ArrayList<String>();
        mLocalPreviews = new ArrayList<String>();
        mOnlineThumbnails = new ArrayList<String>();
        mOnlinePreviews = new ArrayList<String>();
    }

    private int getIntValue(Bundle information, String key) {
        String strValue = information.getString(key);
        if (strValue == null) {
        	return 0;
        }

    	try {
    		return Integer.parseInt(strValue);
    	} catch (NumberFormatException e) {
    		return 0;
    	}
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Resource) {
            Resource r = (Resource) o;
            return mLocalPath.equals(r.mLocalPath);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return mLocalPath.hashCode();
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDividerTitle() {
        return mDividerTitle;
    }

    public String getFileHash() {
        return mFileHash;
    }

    public long getFileModifiedTime() {
        return mFileModifiedTime;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public String getId() {
        return mId;
    }

    public Bundle getInformation() {
        return mInformation;
    }

    public String getLocalPath() {
        return mLocalPath;
    }

    public String getLocalPreview(int index) {
        if (index >= mLocalPreviews.size()) {
            return null;
        }
        return mLocalPreviews.get(index);
    }

    public List<String> getLocalPreviews() {
        return mLocalPreviews;
    }

    public String getLocalThumbnail(int index) {
        if (index >= mLocalThumbnails.size()) {
            return null;
        }
        return mLocalThumbnails.get(index);
    }

    public List<String> getLocalThumbnails() {
        return mLocalThumbnails;
    }

    public String getOnlinePath() {
        return mOnlinePath;
    }

    public String getOnlinePreview(int index) {
        if (index >= mOnlinePreviews.size()) {
        	return null;
        }
        return mOnlinePreviews.get(index);
    }

    public List<String> getOnlinePreviews() {
        return mOnlinePreviews;
    }

    public String getOnlineThumbnail(int index) {
        if (index >= mOnlineThumbnails.size()) {
            return null;
        }
        return mOnlineThumbnails.get(index);
    }

    public List<String> getOnlineThumbnails() {
        return mOnlineThumbnails;
    }

    public int getPlatformVersion() {
        return mPlatformVersion;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDividerTitle(String dividerTitle) {
        mDividerTitle = dividerTitle;
    }

    public void setFileHash(String fileHash) {
        mFileHash = fileHash;
    }

    public void setFileModifiedTime(long modifiedTime) {
        mFileModifiedTime = modifiedTime;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }
    
    public String getResolution() {
    	return mResolution;
    }
    public void setResolution(String resolution) {
    	mResolution = resolution;
    }

    public void setInformation(Bundle information) {
        mInformation = information;
        
        mId = information.getString(ID);
        mTitle = information.getString(NAME);
        mDescription = information.getString(DESCRIPTION);
        mLocalPath = information.getString(LOCAL_PATH);
        mOnlinePath = information.getString(ONLINE_PATH);
        mFileSize = getIntValue(information, SIZE);
        
        ArrayList<String> localThumbnails = information.getStringArrayList(LOCAL_THUMBNAILS);
        if (localThumbnails != null) mLocalThumbnails = localThumbnails;
        ArrayList<String> localPreviews = information.getStringArrayList(LOCAL_PREVIEWS);
        if (localPreviews != null) mLocalPreviews = localPreviews;
        
        ArrayList<String> onlineThumbnails = information.getStringArrayList(ONLINE_THUMBNAILS);
        if (onlineThumbnails != null) mOnlineThumbnails = onlineThumbnails;
        ArrayList<String> onlinePreviews = information.getStringArrayList(ONLINE_PREVIEWS);
        if (onlinePreviews != null) mOnlinePreviews = onlinePreviews;
        
        mStatus = information.getInt(STATUS);
        mPlatformVersion = information.getInt(PLATFORM_VERSION);
        
        mResolution = information.getString(RESOLUTION);
    }

    public void updateOnlinePath(String onlinePath) {
        mInformation.putString(ONLINE_PATH, onlinePath);
        mOnlinePath = onlinePath;
    }

    public void updateStatus(int status) {
        mInformation.putInt(STATUS, status);
        mStatus = status;
    }
}
