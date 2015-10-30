package com.zhuoyi.market.appManage.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 建立一个数据库帮助类
 */
public class DBHelper extends SQLiteOpenHelper {

	// download.db-->数据库名
	public DBHelper(Context context) {
		super(context, "download.db", null, 6);
	}


	/**
	 * 在download.db数据库下创建一个download_info表存储下载信息
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table if not exists download_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer, "
				+ " start_pos integer, end_pos integer, compelete_size integer,url char, "
				+ " appName char, "
				+ " pid String, "
				+ " Xingji integer, "
				+ " bitmap BLOB, " + " fileSizeSum char) ");
		db.execSQL("create table if not exists downloaded_info(_id integer PRIMARY KEY AUTOINCREMENT, url char, "
				+ " appName char, "
				+ " pid String, "
				+ " Xingji integer, "
				+ " bitmap BLOB, "
				+ " fileSizeSum char,"
				+ " localfilepath char," + " apppackagename char) ");
		db.execSQL("create table if not exists download_queue(_id integer PRIMARY KEY AUTOINCREMENT, "
				+ " start_pos integer, end_pos integer, compelete_size integer,"
				+ " appName char, "
				+ " pid String, "
				+ " Xingji integer, "
				+ " bitmap BLOB, "
				+ " fileSizeSum char,"
				+ " packageName char,"
				+ " externDownload integer,"
				+ " url char) ");
		db.execSQL("create table if not exists favorite_app(_id integer PRIMARY KEY AUTOINCREMENT, url char, "
				+ " appName char, "
				+ " md5 String, "
				+ " bitmap BLOB, "
				+ " fileSizeSum char,"
				+ " localfilepath char,"
				+ " versioncode char,"
				+ " versionname char,"
				+ " apppackagename char,"
				+ " appId integer,"
				+ " iconUrl char)");

		db.execSQL("create table if not exists web_app (_id integer PRIMARY KEY AUTOINCREMENT, appId integer, appUrl char, type integer)");
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 4) {
			db.execSQL("create table if not exists download_queue(_id integer PRIMARY KEY AUTOINCREMENT, "
					+ " start_pos integer, end_pos integer, compelete_size integer,"
					+ " appName char, "
					+ " pid String, "
					+ " Xingji integer, "
					+ " bitmap BLOB, "
					+ " fileSizeSum char,"
					+ " packageName char,"
					+ " externDownload integer,"
					+ " url char) ");
		}
		if (oldVersion <= 5) {
			db.execSQL("drop table if exists favorite_app");
			db.execSQL("create table if not exists favorite_app(_id integer PRIMARY KEY AUTOINCREMENT, url char, "
					+ " appName char, "
					+ " md5 String, "
					+ " bitmap BLOB, "
					+ " fileSizeSum char,"
					+ " localfilepath char,"
					+ " versioncode char,"
					+ " versionname char,"
					+ " apppackagename char,"
					+ " appId integer,"
					+ " iconUrl char)");
		} else if (oldVersion == 4) {
			db.execSQL("alter table favorite_app add appId integer");
			db.execSQL("alter table favorite_app add versionname char");
			db.execSQL("alter table favorite_app add iconUrl char");
		}
	}

}
