package com.zhuoyi.market.cleanTrash;

import com.zhuoyi.market.appResident.MarketApplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrashService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    private TrashControl mTrashControl;
    
    public static final String EXTRA_FROM_KEY = "callFrom";
    
    public static final int FROM_MARKET_START = 1;
    public static final int FROM_ALARM_TIME = 2;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mTrashControl = TrashControl.get(MarketApplication.getRootContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int callFrom = intent.getIntExtra(EXTRA_FROM_KEY, 0);
            if (callFrom != 0) {
                checkTrashInfo(callFrom);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    
    private void checkTrashInfo(final int callFrom) {
        if (!mTrashControl.isNeedNotifiedToday()) {
            TrashService.this.stopSelf();
            return;
        } else if (callFrom == FROM_MARKET_START) {
            mTrashControl.setNextCheckTrashAlarm(System.currentTimeMillis() + TrashControl.START_MARKET_CHECK_DELAY_MILLIS);
            return;
        }
        
        mTrashControl.checkTrashStatus(0, true, new TrashControl.CheckTrashCallback() {
            @Override
            public void checkTrashFinish() {
                TrashService.this.stopSelf();
            }
        });
        
    }
}
