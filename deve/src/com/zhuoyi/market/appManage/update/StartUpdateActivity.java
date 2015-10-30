package com.zhuoyi.market.appManage.update;

import com.zhuoyi.market.manager.MarketNotificationManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

/**
 * 应用更新（全部更新）从通知栏进入此界面
 * 通过此activity去启动更新界面，然后启动全部更新
 * @author dream.zhou
 *
 */
public class StartUpdateActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = new Intent(this, MarketUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("update_all", true);
        this.startActivity(intent);
        
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(MarketNotificationManager.NOTIFY_ID_APP_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.finish();
    }
}
