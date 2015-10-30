package com.zhuoyi.market.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.market.download.util.NetworkType;
import com.market.net.data.UploadInfo;
import com.market.net.utils.UpLoadFilesUtil;
import com.zhuoyi.market.appResident.MarketApplication;

@SuppressLint("ParserError")
public class CrashHandler implements UncaughtExceptionHandler
{

	public static final String TAG = "CrashHandler";

	public static final String LOG_DIR = "/ZhuoYiMarket/crash";
	public static final String SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath();

	private static CrashHandler INSTANCE = new CrashHandler();

	private Context mContext;

	private Map<String, String> infos = new HashMap<String, String>();

	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

	private CrashHandler()
	{
		mContext = MarketApplication.getRootContext();
	}

	public static synchronized CrashHandler getInstance()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new CrashHandler();
		}
		return INSTANCE;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex)
	{

		ex.printStackTrace();
		handleException(ex);
		try
		{
			Thread.sleep(3500);
		}
		catch (InterruptedException e)
		{
			Log.e(TAG, "error : ", e);
		}

		reStartProcess();
	}

	private void handleException(Throwable ex)
	{
		if (ex != null)
		{
			collectDeviceInfo(mContext);
			saveCrashInfo2File(ex, LOG_DIR);

			if (NetworkType.isNetworkAvailable(mContext))
			{
				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						sendCrashReportsToServer(mContext);
					}
				}).start();
			}
		}
	}

	public static boolean isApkDebugable(Context context)
	{
		try
		{
			ApplicationInfo info = context.getApplicationInfo();
			return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void collectDeviceInfo(Context ctx)
	{
		try
		{
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), 0);
			if (pi != null)
			{
				String versionName = pi.versionName == null ? "null" : pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		}
		catch (NameNotFoundException e)
		{
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields)
		{
			try
			{
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			}
			catch (Exception e)
			{
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	public UploadInfo getUploadInfo(Context ctx)
	{
		UploadInfo uploadInfo = new UploadInfo();
		String versionName = null;
		String appName = null;
		try
		{
			String packageName = ctx.getPackageName();
			uploadInfo.setPackageName(packageName);
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			if (pi != null)
			{
				uploadInfo.setVersionCode(pi.versionCode);
				versionName = pi.versionName == null ? "null" : pi.versionName;
				appName = pi.applicationInfo.loadLabel(pm).toString();
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "an error occured when collect package info", e);
		}
		uploadInfo.setManufacturer(Build.MANUFACTURER);
		uploadInfo.setMobileType(Build.MODEL);
		uploadInfo.setResolution(getResolution(ctx));
		uploadInfo.setVersionName(versionName);
		uploadInfo.setAppName(appName);
		return uploadInfo;
	}

	private int getPackageVersionCode(Context context)
	{
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = null;
		try
		{
			pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			return pi.versionCode;
		}
		catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return 0;

	}

	private String saveCrashInfo2File(Throwable ex, String logPath)
	{

		StringBuffer sb = new StringBuffer();

		for (Map.Entry<String, String> entry : infos.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n"); // device info
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();

		// If the Throwable contains a cause, the method will be invoked
		// recursively for the nested
		while (cause != null)
		{
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try
		{
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = mContext.getPackageName() + "_crash-" + time + "-" + timestamp + "-"
					+ infos.get("versionCode") + ".log";
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				String path = SD_CARD_PATH + logPath;
				File dir = new File(path);
				if (!dir.exists())
				{
					dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(path + File.separator + fileName);
				fos.write(EncodingUtils.getBytes(sb.toString(), HTTP.UTF_8));
				fos.close();
			}
			return fileName;
		}
		catch (Exception e)
		{
			Log.e(TAG, "an error occured while writing file", e);
		}
		return null;
	}

	public String[] getCrashReportFileNames(final Context context)
	{
		File reportDir = new File(SD_CARD_PATH + LOG_DIR);
		FilenameFilter filter = new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				File file = new File(dir, name);
				if (deleteFileByVersionCode(file, getPackageVersionCode(context)))
				{
					return false;
				}
				else
				{
					return true;
				}
			}
		};
		return reportDir.list(filter);

	}

	public void sendCrashReportsToServer(Context ctx)
	{
		if (isApkDebugable(ctx))
			return;
		String[] reportFilesNames = getCrashReportFileNames(ctx);
		UploadInfo uploadInfo = getUploadInfo(ctx);
		if (reportFilesNames != null && reportFilesNames.length > 0)
		{
			File[] files = new File[reportFilesNames.length];

			for (int i = 0; i < reportFilesNames.length; i++)
			{
				File file = new File(SD_CARD_PATH + LOG_DIR, reportFilesNames[i]);

				if (file.exists())
				{
					files[i] = file;
				}
			}
			String fileIndexs = UpLoadFilesUtil
					.postReport(ctx, UpLoadFilesUtil.getUploadparams(ctx, uploadInfo), files);
			deleteFiles(files, fileIndexs);
		}
	}

	public void deleteFiles(File[] files, String fileIndexs)
	{
		if (!TextUtils.isEmpty(fileIndexs))
		{
			try
			{
				String[] indexs = fileIndexs.split(",");
				for (String str : indexs)
				{
					int i = Integer.parseInt(str);
					if (i > files.length - 1)
					{
						continue;
					}
					files[i].delete();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void reStartProcess()
	{
		Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
		if (launchIntent != null)
		{
			launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			mContext.startActivity(launchIntent);
		}
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public String getResolution(Context context)
	{
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels + "x" + dm.widthPixels;
	}

	/**
	 * 非当前版本的 log文件直接删除，不上传
	 * 
	 * @param file
	 * @param code
	 * @return
	 */
	private boolean deleteFileByVersionCode(File file, int code)
	{
		if (file != null)
		{
			String name = file.getName();
			if (!name.endsWith("-" + code + ".log"))
			{
				file.delete();
				return true;
			}
		}
		return false;

	}

}
