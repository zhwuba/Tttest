package com.freeme.themeclub.wallpaper.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import com.freeme.themeclub.wallpaper.os.ExtraFileUtils;

public class FolderCache {
	
	public static final FilenameFilter sFilenameFilter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return !filename.startsWith(".");
		}
	};
	
    public static class FileInfo {
        public long length = 0;
        public long modifiedTime = 0;
        public String name = null;
        public String path = null;
    }

    public static class FolderInfo {
    	/**
    	 *  < filePath, fileInfo >
    	 */
        public Map<String, FileInfo> files = null;
        public int filesCount = 0;
        public long modifiedTime = 0;
        public String name = null;
        public String path = null;
    }

    
    private DataCache<String, SoftReference<FolderInfo>> folderCache = null;

    public FolderCache() {
        folderCache = new DataCache<String, SoftReference<FolderInfo>>(4);
    }

    protected FileInfo buildFileInfo(String filePath, FolderInfo folderInfo) {
        File file = new File(filePath);
        if (!file.isDirectory()) {
            FileInfo fileInfo = newFileInfo();
            fileInfo.name = file.getName();
            fileInfo.path = filePath;
            fileInfo.modifiedTime = file.lastModified();
            fileInfo.length = file.length();
            return fileInfo;
        }
        return null;
    }

    /**
     * @param folderPath needs ends with '/'
     * @return
     */
    protected FolderInfo buildFolderInfo(String folderPath) {
        File folder = new File(folderPath);
        if (folder.isDirectory()) {
        	FolderInfo folderInfo = newFolderInfo();
        	folderInfo.name = folder.getName();
        	folderInfo.path = folderPath;
        	folderInfo.modifiedTime = folder.lastModified();
            
            String[] files = folder.list(sFilenameFilter);
            
            final int filesCount = (files == null) ? 0 : files.length;
            folderInfo.filesCount = filesCount;
            folderInfo.files = new HashMap<String, FileInfo>(filesCount);
            if (files != null) {
                for (int i = 0; i < filesCount; i++) {
                    String filePath = new StringBuilder(folderPath).append(files[i]).toString();
                    FileInfo fileInfo = buildFileInfo(filePath, folderInfo);
                    if (fileInfo != null) {
                    	folderInfo.files.put(filePath, fileInfo);
                    }
                }
            }
            return folderInfo;
        }
        return null;
    }

    public FolderInfo get(String folderPath) {
        folderPath = ExtraFileUtils.standardizeFolderPath(folderPath);
        SoftReference<FolderInfo> ref = folderCache.get(folderPath);
        FolderInfo folderInfo = (ref == null) ? null : ref.get();
        
    	if (needRefresh(folderInfo)) {
    		folderInfo = buildFolderInfo(folderPath);
            if (folderInfo != null) {
            	synchronized (folderCache) {
            		folderCache.put(folderPath, 
            				new SoftReference<FolderInfo>(folderInfo));
            	}
            }
        }
        return folderInfo;
    }

    protected boolean needRefresh(FolderInfo folderInfo) {
    	if (folderInfo == null) {
    		return true;
    	}
    	
		File folder = new File(folderInfo.path);
		if (folderInfo.modifiedTime != folder.lastModified()) {
			return true;
		}
		String[] files = folder.list();
		if (folderInfo.filesCount != ((files == null) ? 0 : files.length)) {
			return true;
		}
		return false;
    }

    protected FileInfo newFileInfo() {
        return new FileInfo();
    }

    protected FolderInfo newFolderInfo() {
        return new FolderInfo();
    }
}