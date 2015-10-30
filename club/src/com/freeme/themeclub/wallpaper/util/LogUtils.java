package com.freeme.themeclub.wallpaper.util;

import android.util.Log;

public final class LogUtils {
	private static final boolean DEBUG = false;
	
	public static final void d(String tag, String msg) {
		if (DEBUG) Log.d(tag, msg);
	}
	public static final void w(String tag, String msg) {
		if (DEBUG) Log.w(tag, msg);
	}
	public static final void w(String tag, String msg, Throwable tr) {
		if (DEBUG) Log.w(tag, msg, tr);
	}
	public static final void e(String tag, String msg) {
		Log.w(tag, msg);
	}
	public static final void i(String tag, String msg) {
		if (DEBUG) Log.i(tag, msg);
	}
}
