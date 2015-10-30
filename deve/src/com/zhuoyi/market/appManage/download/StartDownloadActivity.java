package com.zhuoyi.market.appManage.download;

import com.market.updateSelf.SelfUpdateInfo;
import com.market.updateSelf.UpSelfStorage;
import com.market.updateSelf.UpdateManager;
import com.zhuoyi.market.appManage.AppManageUtil;
import com.zhuoyi.market.manager.MarketNotificationManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;

/**
 * 自更新从通知栏进入此界面
 * 通过此activity去启动下载管理界面，如果直接去启动下载管理，界面显示会有问题
 * @author dream.zhou
 *
 */
public class StartDownloadActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //自更新下载
        boolean updateSelf = this.getIntent().getBooleanExtra("updateSelf", false);
        if (updateSelf) {
            SelfUpdateInfo updateInfo = UpSelfStorage.getSelfUpdateInfo(getApplicationContext());
            if (updateInfo != null) {
                UpdateManager.startDownloadUpdate(
                        this.getApplicationContext(), 
                        updateInfo.getDownloadUrl(), 
                        updateInfo.getMd5(), 
                        updateInfo.getVersionCode(),
                        updateInfo.getTotalSize());
            }
        }
        
        //启动下载管理
        AppManageUtil.startDownloadActivity(this.getApplicationContext());
        
        //清除通知
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(MarketNotificationManager.NOTIFY_ID_WIFI_HAS_UPDATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.finish();
    }
}
