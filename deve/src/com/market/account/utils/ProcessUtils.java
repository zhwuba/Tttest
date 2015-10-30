package com.market.account.utils;

import android.app.ActivityManager;
import android.content.Context;

public class ProcessUtils {

	public static boolean isMainProcess(Context context) {
		try {
			int pid = android.os.Process.myPid();
			String packageName = context.getPackageName();
			ActivityManager mActivityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
					.getRunningAppProcesses()) {
				if (appProcess.pid == pid) {
					if (appProcess.processName.equals(packageName)) {
						return true;
					} else {
						break;
					}
				}
			}
			return false;
		} catch (Exception e) {
			return true;
		}
	}
}
