package com.freeme.themeclub.theme.onlinetheme;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ThemeDownloadProvider extends ContentProvider{
    private static final String AUTHORITY = "com.freeme.themeclub.provider.themedownload";
    private static final String TABLE_NAME = "themedownload";
    private SQLiteOpenHelper openHelper;
    private static final int  FIRST_CODE = 1;
    private static final int SECOND_CODE = 2;
    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    static{
        matcher.addURI(AUTHORITY, "themedownload", FIRST_CODE);
        matcher.addURI(AUTHORITY, "themedownload/#", SECOND_CODE);
    }
    
    private static class MyOpenHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "themedownload.db";
        private static final int DATABASE_VERSION = 1;
        
        public MyOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table if not exists " + TABLE_NAME
                            + " (_id integer primary key autoincrement, "
                            + " name String, "
                            + " theme_id integer, "
                            + " is_theme integer, "
                            + " download_id integer, "
                            + " path string, "
                            + " url string, "
                            + " package_name string);"
                    );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME);
        }
        
    }
    
    @Override
    public boolean onCreate() {
        openHelper = new MyOpenHelper(getContext());
        return true;
    }
    
    

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int id;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        id = db.delete(TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return id;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (matcher.match(uri) != FIRST_CODE) {
            throw new IllegalArgumentException("cannot insert uri: " + uri);
        }
        
        ContentValues contentValues = new ContentValues(values);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        
        long id = db.insert(TABLE_NAME,null, contentValues);
        if (id < 0) {
            throw new IllegalArgumentException("Failed to insert " + uri);
        }
        Uri newUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sq = new SQLiteQueryBuilder();
        sq.setTables(TABLE_NAME);
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = sq.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor == null) {
            Log.v("yzy", uri +" query failed");
        }else {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
