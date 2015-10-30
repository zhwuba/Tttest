package com.market.download.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import com.market.download.util.Util;

import android.content.Context;
import android.content.Intent;

public class SilentInstallTask extends RunTask {
    private static final String TAG = "SilentInfallTask";
    
    private Context mContext;
    private File mApkFile;
    private InstallCallback mCallback;
    
    public SilentInstallTask(Context context, File apkFile, InstallCallback callback) {
        mContext = context;
        mApkFile = apkFile;
        mCallback = callback;
    }
    
    
    @Override
    protected void run() {
        if (Util.isApkInstalledYet(mContext, mApkFile)) {
            mCallback.hasInstalledYet();
            return;
        }
        
        if (backgroundInstallAPK()) {
            mCallback.installSuccess();
        } else {
            mCallback.installFailed();
        }
    }
    
    
    

    
    public boolean backgroundInstallAPK() {
        Util.log(TAG, "backgroundInstallAPK", "install apk background, file:" + mApkFile.getAbsolutePath());
        String[] args = { "pm", "install", "-r", mApkFile.getAbsolutePath() };
        String result = null;
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ByteArrayOutputStream baosRet = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baosRet.write(read);
                baos.write(read);
            }
            // byte[] data = baos.toByteArray();
            // result = new String(data);
            byte[] data = baosRet.toByteArray();
            result = new String(data);
            Util.log(TAG, "backgroundInstallAPK()", "install result:" + new String(baos.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (result != null && result.startsWith("Success")) {
            Util.log(TAG, "backgroundInstallAPK()", "install success");
            
            if (mContext != null)
                mContext.sendBroadcast(new Intent("download.refresh"));
            
            return true;
        }
        Util.log(TAG, "backgroundInstallAPK()", "install failed");
        return false;
    }
    
    
    public interface InstallCallback {
        void installSuccess();
        void installFailed();
        void hasInstalledYet();
    }
}
