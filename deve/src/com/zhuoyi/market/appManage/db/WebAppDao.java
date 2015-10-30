package com.zhuoyi.market.appManage.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class WebAppDao {

	private DBHelper dbHelper;
	private final static byte[] _writeLock = new byte[0];
	
	public WebAppDao(Context context) {
		dbHelper = new DBHelper(context);
	}
	
	
	/**
	 * 查看数据库中是否有数据
	 */
	public String getWebUrl(int appId) {
		SQLiteDatabase database = null;
		Cursor cursor = null;
		String webUrl = null;
		try {
		    database = dbHelper.getReadableDatabase();
			String sql = "select appUrl from web_app where appId = ?";
			cursor = database.rawQuery(sql, new String[] { "" + appId });
			if (cursor.moveToFirst())
				webUrl = cursor.getString(0);
		}
		catch (Exception e) {

		}
		finally {
			if (cursor != null)
				cursor.close();
			
			if (database != null)
			    database.close();
		}
		return webUrl;

	}
	
	
	public void saveWebAppInfo(int appId, String appUrl){
		if(appId != -1 && !TextUtils.isEmpty(appUrl)){
			synchronized (_writeLock) {
				SQLiteDatabase database = null;
				try {
				    database = dbHelper.getWritableDatabase();
					String sql = "insert into web_app(appId, appUrl, type) values (?,?,?)";
					Object[] bindArgs = {appId, appUrl, 0};
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
	}
	
	
	
	public void removeWebAppInfo(int appId) {
		SQLiteDatabase database = null;
		try {
		    database = dbHelper.getWritableDatabase();
			database.delete("web_app", "appId = ?", new String[] { "" + appId });
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		    if (database != null)
		        database.close();
		}
	}
	
	
	/**
	 * 删除所有数据
	 */
	public void removeAllWebAppInfo() {
		SQLiteDatabase database = null;
		try {
		    database = dbHelper.getWritableDatabase();
			database.execSQL("delete from web_app");
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
