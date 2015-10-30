package com.freeme.themeclub.wallpaper.util;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.File;
import java.io.FileNotFoundException;


public class WallpaperCached extends Service {
	private static final String TAG = "VideoWallpaperCached";
	
	private static final int PROCESS_GET_VIDEOWALLPAPER_FD = 1;
	
	
	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			switch (code) {
				case PROCESS_GET_VIDEOWALLPAPER_FD: {
					final ParcelFileDescriptor _result
							= getVideoWallpaperCachedFileDescriptor();
					reply.writeNoException();
					if (_result != null) {
						reply.writeInt(1);
						_result.writeToParcel(reply, 
								android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
					} else {
						reply.writeInt(0);
					}
					return true;
				}
			}
			return super.onTransact(code, data, reply, flags);
		}
    };
	
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	private ParcelFileDescriptor getVideoWallpaperCachedFileDescriptor() {
		File f = new File(getApplicationContext().getFilesDir(),
				WallpaperUtils.CACHED_THUMB_VIDEOWALLPAPER);
		try {
			return ParcelFileDescriptor.open(f, 
					ParcelFileDescriptor.MODE_CREATE | 
					ParcelFileDescriptor.MODE_TRUNCATE | 
					ParcelFileDescriptor.MODE_WRITE_ONLY);
		} catch (FileNotFoundException e) {
			LogUtils.w(TAG, "Error getting video wallpaper cached fd", e);
		}
		
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * clear history thumbnail
	 */
	public static final String VideoWallpaperResetAction = "com.mediatek.vlw.reset_clear";
	public static class WallpaperCachedOpr extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(VideoWallpaperResetAction)) {
				File f = new File(context.getFilesDir(),
						WallpaperUtils.CACHED_THUMB_VIDEOWALLPAPER);
				for (int retry = 0; retry < 3 && f.exists(); ++retry) {
					if (f.delete()) {
						break;
					}
				}
			}
		}
	}
}
