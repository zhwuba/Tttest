package com.zhuoyi.market.search;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import com.zhuoyi.market.R;

public class ShortCutUtils {

    public static void createShortCut(Context mContext) {
        if (mContext == null) return; 
        try {
            if (checkShortCutIsExist(mContext)) {
                return;
            }
            Object localObject = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.NAME",
            mContext.getString(R.string.shortcut_lanucher_name));
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.ICON", getIcon(mContext));
            ((Intent) localObject).putExtra("duplicate", false);
            Intent localIntent = new Intent("com.zhuoyi.market.action.search");
            localIntent.putExtra("launcher_from", "search_short_cut");
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.INTENT", localIntent);
            mContext.sendBroadcast((Intent) localObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap getIcon(Context mContext) {
        Bitmap localBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.short_cut)).getBitmap()
            .copy(Bitmap.Config.ARGB_8888, true);
        return localBitmap;
    }
    
    
    private static Bitmap getTrashCleanIcon(Context mContext) {
        Bitmap localBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.trash_icon)).getBitmap()
                .copy(Bitmap.Config.ARGB_8888, true);
        return localBitmap;
    }
    
    
    public static void createTrashCleanShortCut(Context mContext) {
        if (mContext == null) return; 
        try {
            Object localObject = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.NAME",
            mContext.getString(R.string.home_title_trashclean));
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.ICON", getTrashCleanIcon(mContext));
            ((Intent) localObject).putExtra("duplicate", false);
            Intent localIntent = new Intent("com.zhuoyi.market.action.trash");
            ((Intent) localObject).putExtra("android.intent.extra.shortcut.INTENT", localIntent);
            mContext.sendBroadcast((Intent) localObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }


    public static boolean checkShortCutIsExist(Context mContext) {
        boolean isInstallShortcut = false;
        String AUTHORITY = "com.android.launcher2.settings";
        String launcherPackageName = getLauncherPackageName(mContext);
        Intent localIntent = new Intent("com.zhuoyi.market.action.search");
        localIntent.putExtra("launcher_from", "search_short_cut");
        if (launcherPackageName == null) {
            return isInstallShortcut;
        }
        if (launcherPackageName.equals("com.oppo.launcher")) { //oppo手机
            AUTHORITY = getAuthorityFromPermission(mContext, launcherPackageName + ".permission.READ_SETTINGS");
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/singledesktopitems?notify=true");
            isInstallShortcut = commonQuery(mContext, isInstallShortcut, CONTENT_URI,localIntent);
        } else if(launcherPackageName.equals("com.zte.mifavor.launcher")){ //中兴手机
            AUTHORITY = getAuthorityFromPermission(mContext, launcherPackageName + ".permission.READ_SETTINGS");
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
            isInstallShortcut = commonQuery(mContext, isInstallShortcut, CONTENT_URI,localIntent);
        } else if (launcherPackageName.equals("com.bbk.launcher2")) { //vivo手机(vivo x5 FunTouchOS)
            //vivo手机中创建快捷方式的时候存入了component数据
            localIntent.setClassName("com.zhuoyi.market", "com.zhuoyi.market.search.SearchActivity");
            AUTHORITY = getAuthorityFromPermission(mContext, launcherPackageName + ".permission.READ_SETTINGS");
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
            isInstallShortcut = commonQuery(mContext, isInstallShortcut, CONTENT_URI,localIntent);
        } else { //com.android.launcher
            int versionLevel = android.os.Build.VERSION.SDK_INT;
            if (versionLevel >= 8) {
                AUTHORITY = "com.android.launcher2.settings";
            } else {
                AUTHORITY = "com.android.launcher.settings";
            }
            Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
            isInstallShortcut = commonQuery(mContext, isInstallShortcut, CONTENT_URI,localIntent);
            return isInstallShortcut;
        }
        return isInstallShortcut;
    }


    private static boolean commonQuery(Context mContext, boolean isInstallShortcut, Uri CONTENT_URI,Intent intent) {
        ContentResolver cr = mContext.getContentResolver();
        try {
            Cursor c = cr.query(CONTENT_URI, new String[] { "title", "intent" }, "title=? and intent=?",
                new String[] { mContext.getString(R.string.shortcut_lanucher_name) , intent.toUri(0)}, null);
            if (c != null && c.getCount() > 0) {
                isInstallShortcut = true;
            }
            if (c != null) {
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInstallShortcut;
    }


    public static String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            // 有多个桌面程序存在，且未指定默认项时；
            return null;
        } else {
            return res.activityInfo.packageName;
        }
    }


    public static String getAuthorityFromPermission(Context context, String permission) {
        if (permission == null)
            return null;
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        if (packs != null) {
            for (PackageInfo pack : packs) {
                ProviderInfo[] providers = pack.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (permission.equals(provider.readPermission))
                            return provider.authority;
                        if (permission.equals(provider.writePermission))
                            return provider.authority;
                    }
                }
            }
        }
        return null;
    }

}
