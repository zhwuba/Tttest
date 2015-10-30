package com.zhuoyi.market.badger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public abstract class ShortcutBadger {

	public static final String HOME_PACKAGE_SONY = "com.sonyericsson.home";
	public static final String HOME_PACKAGE_SAMSUNG = "com.sec.android.app.launcher";
	public static final String HOME_PACKAGE_LG = "com.lge.launcher2";
	public static final String HOME_PACKAGE_HTC = "com.htc.launcher";
	public static final String HOME_PACKAGE_XIAOMI = "com.miui.home";
	
	protected Context mContext;
	
	
	public ShortcutBadger(Context context) {
		this.mContext = context;
	}
	
	/**
	 * add badge count on shortcut
	 * @param badgeCount  
	 */
	public abstract void addBadge(int badgeCount);
	
	/**
	 * clear badge
	 */
	public abstract void clearBadge();
	
	protected String getContextPackageName() {
		return mContext.getPackageName();
	}
	
	protected String getEntryActivityName() {
		Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
		if(intent != null) {
			ComponentName componentName = intent.getComponent();
			return componentName.getClassName();
		}else {
			return mContext.getPackageName() + ".Splash";
		}
		
	}
	
	private static String getHomeLauncherPkg(Context context) {
    	//find the home launcher Package
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.addCategory(Intent.CATEGORY_HOME);
    	ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    	String currentHomePackage = resolveInfo.activityInfo.packageName;
    	return currentHomePackage;
    }

	public static ShortcutBadger getBadgerImpl(Context context) {
		ShortcutBadger badger = null;
		if(context == null) return null;
		String currentHomePackage = getHomeLauncherPkg(context);
		if (ShortcutBadger.HOME_PACKAGE_SONY.equals(currentHomePackage)) {
			badger = new SonyHomeBadger(context);
		} else if (ShortcutBadger.HOME_PACKAGE_SAMSUNG.equals(currentHomePackage)) {
			badger = new SamsungHomeBadger(context);
		} else if (ShortcutBadger.HOME_PACKAGE_XIAOMI.equals(currentHomePackage)) {
			badger = new XiaoMiHomeBadger(context);
		} else if (ShortcutBadger.HOME_PACKAGE_LG.equals(currentHomePackage)) {
			badger = new LGHomeBadger(context);
		} else if (ShortcutBadger.HOME_PACKAGE_HTC.equals(currentHomePackage)) {
			badger = new HTCHomeBadger(context);
		} else {
			badger = new TYDHomeBadger(context);
		}
		return badger;
	}
}
