package com.zhuoyi.market.appManage.favorite;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.statistics.ReportFlag;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;

public class MarketFavoriteActivity extends DownloadBaseActivity implements DownloadCallBackInterface {
    private RelativeLayout mMainView = null;
    private FavoriteView mFavoriteView = null;
    private CommonSubtitleView mCommonSubtitleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_manage_view);
        mCommonSubtitleView = (CommonSubtitleView) findViewById(R.id.title);
        mMainView = (RelativeLayout) this.findViewById(R.id.favorite_main);
        mFavoriteView = new FavoriteView(getApplicationContext(),this);
        mFavoriteView.entryFavoriteView();

        mMainView.addView(mFavoriteView.getMyView());
        DownloadManager.startServiceReportOffLineLog(getApplicationContext(), ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_FAVORITE);
    }

    
    private void initEditView() {
    	TextView mEditFavorite = mCommonSubtitleView.getRightTextView();
    	mEditFavorite.setVisibility(View.VISIBLE);
    	mEditFavorite.setBackgroundResource(R.drawable.favorite_edit_bg);
    	mEditFavorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MarketFavoriteEditActivity.class);
				startActivity(intent);
			}
		});
	}


	@Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onResume() {
    	mFavoriteView.resume();
    	if(mFavoriteView.getFavoritesCount() != 0)
    		initEditView();
    	else
    		mCommonSubtitleView.getRightTextView().setVisibility(View.GONE);
        super.onResume();
        
        //for record user behavior log
        if (!MarketFavoriteEditActivity.Log_Flag) {
            UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_FAVORITE_VIEW));
        } else {
            MarketFavoriteEditActivity.Log_Flag = false;
        }
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFavoriteView.freeViewResource();
    }


    @Override
    protected void onDownloadServiceBind() {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo) {
    	
    }


    @Override
    protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	mFavoriteView.notifyDataSetChanged(null);
    }


	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId, String type, int verCode,
			int appId, long totalSize) {
		try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, ReportFlag.TOPIC_NULL, ReportFlag.FROM_FAVORITE, verCode, appId, totalSize);
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
	
	
	public void downloadDelete(String pkgName, int verCode) throws RemoteException{
		cancelDownloadApk(pkgName, verCode, true);
	}
}
