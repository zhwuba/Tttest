package com.market.download.provider;

import java.io.FileNotFoundException;
import java.util.List;

import com.market.download.userDownload.DownStorage;
import com.market.download.userDownload.DownloadEventInfo;
import com.zhuoyi.market.constant.SharedPrefDefine;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class DownloadProvider extends ContentProvider {

    public static final String AUTOHORITY = "com.zhuoyi.market.downloadModule.DownloadProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTOHORITY);

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {

        List<String> segments = uri.getPathSegments();
        if (segments.size() == 0) {
            return null;
        }

        String infoKey = segments.get(0);

        SharedPreferences sp = this.getContext().getSharedPreferences(SharedPrefDefine.DOWNLOAD_USER, Context.MODE_PRIVATE);
        String eventStr = sp.getString(infoKey, null);
        if (eventStr != null) {
            DownloadEventInfo eventInfo = new DownloadEventInfo(eventStr);
            return Integer.toString(eventInfo.getCurrState());
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        // TODO Auto-generated method stub
        return super.openFile(uri, mode);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
