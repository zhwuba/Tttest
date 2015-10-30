package com.freeme.themeclub.wallpaper.os;

import android.os.FileUtils;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;


public class ExtraFileUtils {

    public static void addNoMedia(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
        	try {
        		new File(dir, ".nomedia").createNewFile();
        	} catch (IOException e) {
        	}
        }
    }

    public static boolean deleteDir(File fileOrFolder) {
        boolean isSuccess = true;
        if (fileOrFolder.isDirectory()) {
        	String[] children = fileOrFolder.list();
            if (children != null) {
            	for (int i = 0; i < children.length; i++)
                    if (!deleteDir(new File(fileOrFolder, children[i])))
                    	isSuccess = false;
            } else {
            	return false;
            }
        }
        if (!fileOrFolder.delete()) {
        	isSuccess = false;
        }
        return isSuccess;
    }

    public static String getExtension(File file) {
        if (file == null) {
        	return "";
        }
        return getExtension(file.getName());
    }

    public static String getExtension(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        int index = fileName.lastIndexOf(".");
        return (index > -1) ? fileName.substring(index + 1) : "";
    }

    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
        	return "";
        }
        int index = filePath.lastIndexOf(File.separator);
        if (index > -1) {
        	filePath = filePath.substring(index + 1);
        }
        return filePath;
    }

    public static String getFileTitle(File file) {
        if (file == null) {
            return "";
        }
        return getFileTitle(file.getName());
    }

    public static String getFileTitle(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
        	return "";
        }
        int index = fileName.lastIndexOf(".");
        if (index > -1) {
        	fileName = fileName.substring(0, index);
        }
        return fileName;
    }

    public static String getParentFolderPath(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
        	return "";
        }
        int index = filePath.lastIndexOf(File.separator);
        if (index > -1) {
        	filePath = filePath.substring(0, index);
        }
        return filePath;
    }

    public static boolean mkdirs(File file, int mode, int uid, int gid) {
        if (!file.exists()) {
        	String parentDir = file.getParent();
            if (parentDir != null) {
                mkdirs(new File(parentDir), mode, uid, gid);
            }
            if (file.mkdir()) {
                FileUtils.setPermissions(file.getPath(), mode, uid, gid);
                return true;
            }
        }
        return false;
    }

    public static String standardizeFolderPath(String path) {
        if (path.endsWith(File.separator)) {
        	return path;
        }
        return path + File.separator;
    }
}