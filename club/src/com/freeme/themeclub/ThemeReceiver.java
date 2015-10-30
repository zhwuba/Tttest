package com.freeme.themeclub;

import com.freeme.themeclub.individualcenter.IndividualThemeFragment;
import com.freeme.themeclub.individualcenter.ThemeConstants;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.util.BitmapUtiles;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.ThemeInfo;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;

import android.util.Log;

/**
 * Receive package add and removed broadcast, and add a record to database when
 * the added package is a theme package, or delete the related record in
 * database when a theme package is removed.
 */
public class ThemeReceiver extends BroadcastReceiver {
    private static final String TAG = "ThemeReceiver";
    private PackageManager mPm;

    @Override
    public void onReceive(Context context, Intent intent) {
        mPm = context.getPackageManager();
        String packageName = intent.getData().getSchemeSpecificPart();
        String action = intent.getAction();
        Log.w("yzy", "action = "+action);
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            Log.w("yzy", "ACTION_PACKAGE_ADDED");
            // When a package added to system, judge whether this package is a
            // theme package.
            try {
                PackageInfo info = mPm.getPackageInfo(packageName, 0);
                if (info.packageName.startsWith("com.freeme.theme.")) {
                    Resources.ThemeInfo themeInfo = context.getResources()
                            .getThemeInfo(info.applicationInfo.sourceDir,
                                    info.packageName);

                    Log.w("yzy", "themeInfo.packageName = "+themeInfo.packageName);
                    Log.w("yzy", "themeInfo.themePath = "+themeInfo.themePath);
                    ContentValues values = new ContentValues();
                    values.put(ThemeConstants.PACKAGE_NAME, themeInfo.packageName);
                    values.put(ThemeConstants.THEME_PATH, themeInfo.themePath);
                    values.put(ThemeConstants.THEME_TYPE, themeInfo.themeType);
                    values.put(ThemeConstants.FONT, themeInfo.font);
                    values.put(ThemeConstants.TITLE, themeInfo.title);
                    values.put(ThemeConstants.DESCRIPTION, themeInfo.description);
                    values.put(ThemeConstants.AUTHOR, themeInfo.author);
                    values.put(ThemeConstants.VERSION, themeInfo.version);
                    values.put(ThemeConstants.THUMBNAIL, BitmapUtiles.flattenBitmap(
                            context.getResources()
                            .getThemePreview(themeInfo.themePath,
                                    Resources.THEME_PREVIEW_THUMB)
                                    .getBitmap()));
                    
                    removeTheme(context, themeInfo.packageName);
                    
                    context.getContentResolver().insert(ThemeConstants.CONTENT_URI,
                            values);
                    
                }
            } catch (NameNotFoundException e) {
                Log.d(TAG,
                        "Intent.ACTION_PACKAGE_ADDED can not find name:packageName = "
                                + packageName);
            }
        } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            // When a package removed from system, judge whether this package is
            // a theme package.
            Log.d(TAG, "delete theme: " + packageName);

            removeTheme(context, packageName);
        }
        Intent it = new Intent(IndividualThemeFragment.ACTION_REFRESH_THEME);
        context.sendBroadcast(it);
    }

    private void removeTheme(Context context, String packageName) {
        Cursor c = context.getContentResolver().query(ThemeConstants.CONTENT_URI, null,
                ThemeConstants.PACKAGE_NAME + " = ?", new String[] { packageName },
                null);
        if (c != null) {
            context.getContentResolver().delete(ThemeConstants.CONTENT_URI,
                    ThemeConstants.PACKAGE_NAME + " = ?", new String[] { packageName });
            c.close();
        }
    }
}
