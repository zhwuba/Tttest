package com.freeme.themeclub;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @auther tyd Jack 20130726 for, support modular design lockscreen
 */
public class LockscreenInfo {   

    // private static final String LOCKSCREEN_PREVIEW = "lockscreen_preview";
    private static final String LOCKSCREEN_TITLE = "lockscreen_title";
    private static final String LOCKSCREEN_WALLPAPER = "default_wallpaper_lockscreen";

    private static final String FRAMEWORK_PACKAGE_NAME = "android";

    private Context mContext;
    private String mPackagePath;
    private String mPackageName;
    private String mTitle;

    private Context mPackageContext;
    private int mLockscreenWallpaperId = 0;

    private boolean mLockscreenPackageFlag = true;
    public LockscreenInfo(Context context, String packageName,
            String packagePath) {
        mContext = context;
        mPackageName = packageName;
        mPackagePath = packagePath;

        getPackageInfo();
    }

    public BitmapDrawable getPreview() {
        return mContext.getResources().getThemePreview(
                mPackagePath, Resources.THEME_PREVIEW_LOCKSCREEN);
    }

    public BitmapDrawable getPreviewThumb() {
        return mContext.getResources().getThemePreview(
                mPackagePath, Resources.THEME_PREVIEW_LOCKSCREEN_THUMB);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public boolean isLockscreenPackage() {
        return this.mLockscreenPackageFlag;
    }

    public void setTitle(String paramString) {
        this.mTitle = paramString;
    }

    public void setPackageName(String paramString) {
        this.mPackageName = paramString;
    }

    public String getPackagePath() {
        return mPackagePath;
    }

    public void setPackagePath(String packagePath) {
        this.mPackagePath = packagePath;
    }

    private void getPackageInfo() {
        try {
            mTitle = android.os.Build.MODEL;
            if (FRAMEWORK_PACKAGE_NAME.equals(mPackageName))
                return;

            mPackageContext = mContext.createPackageContext(mPackageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            int titleId = mPackageContext.getResources().getIdentifier(
                    LOCKSCREEN_TITLE, "string", mPackageName);

            mLockscreenWallpaperId = mPackageContext.getResources()
                    .getIdentifier(LOCKSCREEN_WALLPAPER, "drawable",
                            mPackageName);

            mTitle = mPackageContext.getResources().getString(titleId);

        } catch (Exception e) {
            mLockscreenPackageFlag = false;
        }
    }

    public Bitmap getLockscreenWallpaper() {
        Bitmap lockscreenWallpaper = null;
        
        if (FRAMEWORK_PACKAGE_NAME.equals(mPackageName)) {
            lockscreenWallpaper = BitmapFactory
                    .decodeResource(
                            mContext.getResources(),
                            com.android.internal.R.drawable.default_wallpaper_lockscreen);
        } else {

            if (mLockscreenWallpaperId != 0 && mPackageContext != null)
                lockscreenWallpaper = BitmapFactory.decodeResource(
                        mPackageContext.getResources(),
                        mLockscreenWallpaperId);
        }
        return lockscreenWallpaper;
    }
}
