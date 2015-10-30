package com.freeme.themeclub.statisticsdata.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.freeme.themeclub.statisticsdata.LocalUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class StatisticDBHelper extends SQLiteOpenHelper{

	private static final String DB_NAEM="themeclub_statistics_db";
	private static final String TABLE="themeclub_statistics_info";
	private static final int VIEWSION=1;
	private static StatisticDBHelper mDBHelper;
	private SQLiteDatabase mDatabase;
	private AtomicInteger mOpenCounter = new AtomicInteger();
	
	public StatisticDBHelper(Context context) {
		super(context, DB_NAEM, null, VIEWSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="CREATE TABLE IF NOT EXISTS " + TABLE + " (id integer primary key autoincrement,info TEXT)";
		db.execSQL(sql);
	}
	private synchronized SQLiteDatabase openDatabase(){
		if(mOpenCounter.incrementAndGet()==1){
			mDatabase = mDBHelper.getWritableDatabase();
		}
		return mDatabase;
	}
	private synchronized void closeDatabase(){
		if(mOpenCounter.decrementAndGet()==0){
			mDatabase.close();
		}
	}

	public static synchronized StatisticDBHelper getInstance(Context context){
		if(mDBHelper==null){
			mDBHelper=new StatisticDBHelper(context);
		}
		return mDBHelper;
	}
	
	public  void intserStatisticdataToDB(String info){
		synchronized (mDBHelper) {
            SQLiteDatabase db = openDatabase();
            ContentValues values = new ContentValues();          
            values.put("info", info);
            db.insert(TABLE, null, values);
            closeDatabase();
        }
	}
	
	public synchronized void saveInfosToFileFromDB(Context context){
		SQLiteDatabase db = openDatabase();
		String sql= "select * from " + TABLE;
		Cursor cursor=db.rawQuery(sql, null);
		if(cursor.getCount()==0){
			cursor.close();
			closeDatabase();
			return;
		}
		String fileName = LocalUtil.getStatisticFilePathName();
        File file = LocalUtil.createFile(fileName);
        if (file == null) {
            return;
        }
        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fw = new FileWriter(file, true);
            writer = new BufferedWriter(fw);
            if (file.length() == 0) {
                String infoStr = LocalUtil.getCommonInfoJsonStr(context);
                writer.write(infoStr);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            String info = cursor.getString(cursor.getColumnIndex("info"));
                try {
                    writer.write(info);
                    writer.newLine();
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        cursor.close();
        closeDatabase();
        try {
            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
		
	}
	
	public int getDataNumber(){
		SQLiteDatabase db = openDatabase();
		String sql= "select * from " + TABLE;
		Cursor cursor=db.rawQuery(sql, null);
		if(cursor.getCount()==0){
			cursor.close();
			closeDatabase();
			return 0;
		}else {
			cursor.close();
			closeDatabase();
			return cursor.getCount();
		}
	}
	
	public void cleanup() {
        SQLiteDatabase db = openDatabase();
        String sql = "DELETE FROM " + TABLE + ";";
        db.execSQL(sql);
        closeDatabase();
    }
	
}
