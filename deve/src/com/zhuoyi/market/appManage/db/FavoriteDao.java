package com.zhuoyi.market.appManage.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;

public class FavoriteDao {
	private DBHelper dbHelper;
	private final static byte[] _writeLock = new byte[0];
	private Context mContext;

	public FavoriteDao(Context context)
	{
		dbHelper = new DBHelper(context);
		mContext = context;
	}

	
	/**
	 * 查看数据库中是否有数据
	 */
	public boolean isHasInfors(String packageName)
	{
		SQLiteDatabase database = null;
		Cursor cursor = null;
		int count = -1;
		try {
		    database = dbHelper.getReadableDatabase();
			String sql = "select count(*)  from favorite_app where apppackagename=?";
			cursor = database.rawQuery(sql, new String[] { packageName });

			if (cursor.moveToFirst())
				count = cursor.getInt(0);
		}
		catch (Exception e) {

		}
		finally {
			if (cursor != null)
				cursor.close();
			if (database != null)
			    database.close();
		}
		return count != 0;

	}

	
	/**
	 * 保存 收藏信息
	 */
	public void saveInfos(FavoriteInfo info) {
		synchronized (_writeLock) {
			SQLiteDatabase database = null;
			try {
			    database = dbHelper.getWritableDatabase();
				String sql = "insert into favorite_app(url,appName,md5,bitmap,fileSizeSum,localfilepath,versioncode,versionname,apppackagename,appId, iconUrl) values (?,?,?,?,?,?,?,?,?,?,?)";
				Object[] bindArgs = { info.getUrl(), info.getAppName(), info.getMd5(), info.getBitmap(), info.getFileSizeSum(), info.getLocalFilePath(), info.getVersionCode(),info.getVersionName(), info.getAppPackageName(), info.getAppId(), info.getIconUrl()};
				database.execSQL(sql, bindArgs);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
				    if (database != null)
				        database.close();
				}
				catch (Exception e2) {
				}

			}
		}

	}

	
	/**
	 * 得到所有的收藏项目
	 */
	public List<FavoriteInfo> getAllInfos() {
		List<FavoriteInfo> list = new ArrayList<FavoriteInfo>();
		synchronized (_writeLock) {
			SQLiteDatabase database = null;
			Cursor cursor = null;
			try {
			    database = dbHelper.getReadableDatabase();
			    ConcurrentHashMap<String, DownloadEventInfo> allDownEventMap = DownloadPool.getAllDownloadEvent(mContext);
				cursor = database.query("favorite_app", new String[] { "url", "appName", "md5", "bitmap", "fileSizeSum", "localfilepath", "versioncode", "apppackagename", "appId", "iconUrl"}, null, null, null, null, null);
				while (cursor.moveToNext()) {
					FavoriteInfo info = new FavoriteInfo(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getBlob(3),
							cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getInt(8), cursor.getString(9));
					DownloadEventInfo eventInfo = allDownEventMap.get(info.getAppPackageName());
					if(eventInfo != null && eventInfo.getVersionCode() == Integer.valueOf(info.getVersionCode())){
						delete(info.getAppPackageName());
					}else{
						list.add(info);
					}
				}
			}
			finally {
				// database.endTransaction();
				if (null != cursor) {
					cursor.close();
				}
				try {
				    if (database != null)
				        database.close();
				}
				catch (Exception e2) {
				}
			}
		}
		return list;
	}

	
	/**
	 * 关闭数据库
	 */
	public void closeDb() {
		dbHelper.close();
	}

	
	/**
	 * 删除数据库中的数据
	 */
	public void delete(String packageName) {
		SQLiteDatabase database = null;
		try {
		    database = dbHelper.getWritableDatabase();
			database.delete("favorite_app", "apppackagename=?", new String[] { packageName });
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		    if (database != null)
		        database.close();
		}

	}

	
	public void deleteAll() {
		SQLiteDatabase database = null;
		try {
		    database = dbHelper.getWritableDatabase();
			database.execSQL("delete from favorite_app");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		    if (database != null)
		        database.close();
		}
	}
}
