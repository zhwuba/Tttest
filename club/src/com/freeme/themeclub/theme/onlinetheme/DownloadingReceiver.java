package com.freeme.themeclub.theme.onlinetheme;

import java.util.ArrayList;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
public class DownloadingReceiver extends BroadcastReceiver {

    public int id;
    public ArrayList<Map<String, Object>> listData;
    public boolean isLockscreen;

    public DownloadingReceiver(int id,ArrayList<Map<String, Object>> listData,boolean isLockscreen){
        this.id=id;
        this.isLockscreen=isLockscreen;
        this.listData=listData;
    }
    
    public DownloadingReceiver(){
        
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("yzy intent", intent.toString());

        Bundle extras = intent.getExtras();
        
//        for(int i=0;i<extras.size();i++){
//            Log.w("yzy_extra", extras.get)
//        }
//        Intent intent2 = new Intent(context,
//                OnlineThemesDetailActivity.class);
//        intent2.putExtra("list_id", id);
//        intent2.putExtra("mlistData", listData);
//        intent2.putExtra("isOnlineLockscreen",isLockscreen);
//        context.startActivity(intent2);
        Toast.makeText(context, "bbbbbbbbb", 1).show();
    }


}


