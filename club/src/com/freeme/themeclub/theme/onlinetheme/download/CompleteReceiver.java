package com.freeme.themeclub.theme.onlinetheme.download;

import java.io.File;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.freeme.themeclub.theme.onlinetheme.ThemeDownload;

public class CompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent2) {

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
                Uri.parse("file://" + Environment.getExternalStorageDirectory()+ "/themes/download" )));
        DownloadManagerHelper mDownloadManagerHelper = new DownloadManagerHelper(
                (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE));
        long completeDownloadId = intent2.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID, -1);
       
        if(mDownloadManagerHelper.getStatusById(completeDownloadId) == DownloadManager.STATUS_SUCCESSFUL){
            Cursor cursor = context.getContentResolver().query(ThemeDownload.URI, 
                    ThemeDownload.QUERY, ThemeDownload.DOWNLOAD_ID+"="+completeDownloadId, null, null);

            if(cursor!=null && cursor.getCount()!=0){
                cursor.moveToFirst();
                Intent intent = new Intent();
                intent.setClass(context, BackInstallService.class);
                int mDownloadId=cursor.getInt(cursor.getColumnIndex(ThemeDownload.DOWNLOAD_ID));
                intent.putExtra("downloadId",mDownloadId );
                String apkPath=cursor.getString(cursor.getColumnIndex(ThemeDownload.PATH));
                intent.putExtra("apkPath", apkPath);
                if(!fileIsExists(apkPath)){
                    context.getContentResolver().delete(ThemeDownload.URI,
                            ThemeDownload.DOWNLOAD_ID+"="+mDownloadId,null);
                    cursor.close();
                    return ;
                }
                intent.putExtra("id", cursor.getInt(cursor.getColumnIndex(ThemeDownload.THEME_ID)));
                intent.putExtra("isOnlineLockscreen", cursor.getInt(
                        cursor.getColumnIndex(ThemeDownload.IS_THEME))==0?false:true);
                intent.putExtra("fromMain", true);
                intent.setPackage(context.getPackageName());
                context.startService(intent);           
            } 
            cursor.close();
        }
        

    }

    public boolean fileIsExists(String path){
        try{
            File f=new File(path);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
