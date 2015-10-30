package com.zhuoyi.market.appManage.download;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zhuoyi.market.commonInterface.ImageLoadedCallBack;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.appManage.download.DownloadView;

public class MarketDownloadActivity extends DownloadBaseActivity implements ImageLoadedCallBack, DownloadInterface {

	private DownloadView mDownloadView;
	private boolean mIsFirstEntryDownload = true;
	private RelativeLayout mMainView = null;
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String apkName = intent.getStringExtra("packageName");
            int verCode = intent.getIntExtra("versionCode", 0);

            mDownloadView.downloadComplete(apkName + verCode);
        }
	    
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_manage_view);

		mMainView = (RelativeLayout) this.findViewById(R.id.download_main);
		
		mIsFirstEntryDownload = true;
	    mDownloadView = new DownloadView(this, this);
		mDownloadView.entryDownloadView();
        
        mMainView.addView(mDownloadView.getMyView());
        
        String iconUrl = this.getIntent().getStringExtra("loadImageiconUrl");
        String pkgName = this.getIntent().getStringExtra("loadImagePkgName");
        if (iconUrl != null) {
            AsyncImageCache.from(this.getApplicationContext())
                    .displayImage(
                            new AsyncImageCache.NetworkImageGenerator(pkgName,
                                    iconUrl), ReportFlag.FROM_THIRD_DOWNLOAD);
        }
        AsyncImageCache.from(this.getApplication()).setImageLoadedCallBack("download", this);
        
        registerReceiver(mReceiver, new IntentFilter("com.zhuoyi.market.DOWNLOAD_COMPLETE"));
        
        DownloadManager.startServiceReportOffLineLog(getApplicationContext(), ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_DOWN_MANA);
	}
	
	
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        mDownloadView.windowFocusChanged(hasFocus);
    }
	
	
	@Override
	protected void onResume()
	{
		if(!mIsFirstEntryDownload){
			mDownloadView.refreshDownArraysData();
		}else{
			mIsFirstEntryDownload = false;
			mDownloadView.getRecommendData();
		}
		super.onResume();
		
		//for record user behavior log
        UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_DOWNLOAD_VIEW));
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
		mDownloadView.freeViewResource();
		AsyncImageCache.from(this.getApplication()).removeImageLoadedCallBack("download");
		unregisterReceiver(mReceiver);
	}

	
	@Override
	protected void onDownloadServiceBind() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE, eventInfo);
        }	
	}

	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		Toast.makeText(MarketDownloadActivity.this,getResources().getString(R.string.cardException), Toast.LENGTH_SHORT).show();
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE, eventInfo);
        }
	}

	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		Toast.makeText(MarketDownloadActivity.this,getResources().getString(R.string.no_sd_card) , Toast.LENGTH_SHORT).show();
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE, eventInfo);
        }
	}

	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		Toast.makeText(MarketDownloadActivity.this, getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE, eventInfo);
        }
	}

	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE_DELAY, eventInfo);
        }
	}

	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
	    if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_COMPLETE, eventInfo);
        }
	}

	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_INSTALLED, eventInfo);
        }
	}

	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_INSTALLED, eventInfo);
        }
	}

	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_REMOVE, eventInfo);
        }
	}
	

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_UPDATE, eventInfo);
        }
	}


    @Override
    public void imageLoaded(String tag) {
        // TODO Auto-generated method stub
        if (mDownloadView != null) {
            mDownloadView.notifyDataSetChanged(DownloadView.ACTION_DEFAULT, null);
        }
    }


	@Override
	public boolean downloadStart(String pacName, int verCode) {
		// TODO Auto-generated method stub
		try {
			return this.startDownloadApk(pacName, verCode);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		// TODO Auto-generated method stub
		
		try {
			return this.pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}


	@Override
	public boolean downloadDeleteItem(String pkgName, int verCode,
			boolean delItemFile) {
		// TODO Auto-generated method stub
		
		try {
			return this.cancelDownloadApk(pkgName, verCode, delItemFile);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}


	@Override
	public void downloadDeleteAll(final List<String> pkgName, final List<Integer> verCode,
			final boolean delItemFile) {
		// TODO Auto-generated method stub
	    new Thread(){
	        @Override
	        public void run(){
	            int count = pkgName.size();
	            for(int i=0; i<count; i++){
	                try {
                        MarketDownloadActivity.this.cancelDownloadApk(pkgName.get(i), verCode.get(i), delItemFile);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
	            verCode.clear();
	            verCode.clear();
	        }
	    }.start();
	}

}
