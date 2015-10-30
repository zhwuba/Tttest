package com.zhuoyi.market.appManage.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.RelativeLayout;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appManage.update.UpdateManagerView;

public class MarketUpdateActivity extends DownloadBaseActivity implements DownloadCallBackInterface{

    private RelativeLayout mMainView = null;
    private UpdateManagerView mUpdateView;
    private PackageUnInstallReceiver mPackageUnInstallReceiver;
    
    private boolean mUpdateAll = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_manage_view);
        mMainView = (RelativeLayout) this.findViewById(R.id.update_main);
        mUpdateView = new UpdateManagerView(this);
        mMainView.addView(mUpdateView.getMyView());
        initUninstallReceiver();
        DownloadManager.startServiceReportOffLineLog(getApplicationContext(), ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_UPDATE_MANA);
    
        mUpdateAll = this.getIntent().getBooleanExtra("update_all", false);
    }

    private void initUninstallReceiver() {
    	mPackageUnInstallReceiver = new PackageUnInstallReceiver();
    	IntentFilter filter = new IntentFilter();
    	filter.addAction("android.intent.action.PACKAGE_REMOVED");
    	filter.addDataScheme("package");
    	registerReceiver(mPackageUnInstallReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        mUpdateAll = intent.getBooleanExtra("update_all", false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mUpdateView.myResume();

        //for record user behavior log
        UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_UPDATE_VIEW));
    }
    
    
    @Override
    public void onBackPressed() {
        startSplash();
        this.finish();
    }
    
    
    private void startSplash() {
        Intent intent = new Intent(this, Splash.class);
        SharedPreferences settings = getSharedPreferences(Splash.PREFS_NAME, 0);
        Editor editor = settings.edit();
        editor.putBoolean(Splash.FIRST_RUN, false);
        editor.commit();
        if(Splash.getHandler() == null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("showLoadingUI", false);
            startActivity(intent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateView.onDestory();
        unregisterReceiver(mPackageUnInstallReceiver);
    }


    @Override
    protected void onDownloadServiceBind() {
        if (mUpdateAll) {
            mUpdateView.updateAllApps(); 
        }
        mUpdateAll = false;
    }


    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo) {

    }


    @Override
    protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {

    }


    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo) {
    	
    }


    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
    	
    }


    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {
    	
    }


	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId, String type, int verCode,
			int appId, long totalSize) {
		try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
	}


	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
	}


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
			return pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	class PackageUnInstallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getData().getSchemeSpecificPart();
			mUpdateView.uninstallRefresh(packageName);
		}
		
	}
}
