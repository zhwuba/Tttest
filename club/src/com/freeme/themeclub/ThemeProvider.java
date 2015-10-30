package com.freeme.themeclub;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.ThemeInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.freeme.themeclub.R;
import com.freeme.themeclub.individualcenter.ThemeConstants;
import com.freeme.themeclub.theme.onlinetheme.util.BitmapUtiles;

/**
 * Provider to manager information of themes in current system in database.
 */
public class ThemeProvider extends ContentProvider {
	private SQLiteDatabase sqlDB;
	private DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = "themes.db";
	private static final int DATABASE_VERSION = 4;
	private static final String TABLE_NAME = "theme";
	private static final String TAG = "ThemeProvider";

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context mContext;
		private final Collator sCollator = Collator.getInstance();

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Enter DatabaseHelper.onCreate()");
			db.execSQL("Create table " + TABLE_NAME
					+ "( _id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "package_name TEXT UNIQUE," + "theme_path TEXT," + "theme_type TEXT," + "font TEXT," + "title TEXT,"
					+ "description TEXT," + "author TEXT," + "version TEXT,"+ "thumbnail BLOB);");

			initDatabase(db);

			Log.d(TAG, "Leave DatabaseHelper.onCreate()");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}

		public void initDatabase(SQLiteDatabase db) {
			List<PackageInfo> allApps = new ArrayList<PackageInfo>();
			List<PackageInfo> selectedApps = new ArrayList<PackageInfo>();
			PackageInfo defaultApp = new PackageInfo();
			PackageManager pmg = mContext.getPackageManager();
			allApps = pmg.getInstalledPackages(0);
			for (PackageInfo app : allApps) {
				Log.d(TAG, "initDatabase: packageName = " + app.packageName);
				if (app.packageName.equals("android")) {
					defaultApp = app;
				}
				if (app.packageName.startsWith("com.freeme.theme.")) {
					selectedApps.add(app);
				}
			}
			
			selectedApps.add(defaultApp);

			for (PackageInfo item : selectedApps) {
				Resources.ThemeInfo themeInfo = mContext.getResources()
						.getThemeInfo(item.applicationInfo.sourceDir,
								item.packageName);

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
				        mContext.getResources()
                        .getThemePreview(themeInfo.themePath,
                                Resources.THEME_PREVIEW_THUMB)
                                .getBitmap()));
				db.insert(TABLE_NAME, null, values);
			}

			allApps.clear();
			selectedApps.clear();
		}
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "Enter delete()");
		sqlDB = dbHelper.getWritableDatabase();
		return sqlDB.delete(TABLE_NAME, selection, selectionArgs);
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues arg) {
		Log.d(TAG, "Enter insert()");
		sqlDB = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues(arg);
		long rowId = sqlDB.insert(TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(ThemeConstants.CONTENT_URI.buildUpon(),
					rowId).build();
			Log.d(TAG, "Leave insert()");
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "Enter query()");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		qb.setTables(TABLE_NAME);
		Log.d(TAG, "query(): uri: " + uri.toString());

		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(), uri);
		}
		Log.d(TAG, "Leave query()");
		return c;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}
}
