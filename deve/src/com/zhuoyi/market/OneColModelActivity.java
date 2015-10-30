package com.zhuoyi.market;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.LinearLayout;

import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.statistics.ReportFlag;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.market.view.PressInstallButtonAnimView;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;
import com.zhuoyi.market.view.CustomViewFactory;

/**
 * 通用的单列表Activity
 * @author JLu
 *
 */
public class OneColModelActivity extends DownloadTabBaseActivity implements DownloadCallBackInterface {
	
	private CommonSubtitleView mTitleBar;
	private AbsCustomView mCustomView;
	private LinearLayout mParentLayout = null;
	private PressInstallButtonAnimView mPressInstallButtonAnimView = null;
	private int[] mDownloadLocation = {0,0};
	private int mStatusBarHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		setContentView(R.layout.layout_one_col_model);
		findViews();
	}

	private void findViews() {
		Intent intent = getIntent();
		String titleName = intent.getStringExtra("titleName");
		mTitleBar = (CommonSubtitleView)findViewById(R.id.title_bar);
		mTitleBar.setSubtitleName(titleName);
		mTitleBar.showSearchBtn(true);
		mTitleBar.registeredReceiver();
		mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);
		int viewType = intent.getIntExtra("viewType", -1);
		mCustomView = CustomViewFactory.create(viewType, getApplicationContext(), this, intent.getIntExtra("assemblyId", -1));
		if (viewType == CustomViewFactory.VIEW_RANKLIST_ALL) {
		    mCustomView.setTitleName(titleName);
		    mCustomView.openStatisticsWhenEntry();
		}
		mCustomView.entryView();

		mParentLayout = (LinearLayout)findViewById(R.id.parent_layout);
		mParentLayout.addView(mCustomView.getRootView());
	}
	
	public void onBackPressed() {
	    this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		CommonLoadingManager.get().showLoadingAnimation(this);
		
		mCustomView.entryView();
	}
	
	@Override
	protected void onPause() {
	    mCustomView.exitView();
	    
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		if(mCustomView!=null)
			mCustomView.freeViewResource();
		if (mTitleBar != null)
            mTitleBar.unRegisteredReceiver();
		super.onDestroy();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			mTitleBar.setDownloadStatus();
			mCustomView.notifyDataSetChanged(null);
		}

		if(mDownloadLocation[0] == 0 || mDownloadLocation[1] == 0) {

			Rect frame = new Rect();  
			getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
			mStatusBarHeight = frame.top; 
			int downloadWidth = mTitleBar.getDownloadWidth();
			int downloadHeight = mTitleBar.getDownloadHeight();

			mDownloadLocation = mTitleBar.getDownloadLocation();
			mDownloadLocation[0] = mDownloadLocation[0] - downloadWidth/4;
			mDownloadLocation[1] = mDownloadLocation[1] - downloadHeight/2;
		}
		super.onWindowFocusChanged(hasFocus);
	}	

	@Override
	protected void onDownloadServiceBind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}
	
	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		mCustomView.notifyDataSetChanged(null);
	}

    @Override
    public void startDownloadApp(String pacName, String appName,
            String filePath, String md5, String url, String topicId, String type, int verCode,
            int appId, long totalSize) {
        try {
            addDownloadApk(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
    }

    @Override
    public void startIconAnimation(String pacName, int versionCode,
    		Drawable drawable, int fromX, int fromY) {
    	if(mPressInstallButtonAnimView != null)
    		mPressInstallButtonAnimView.startDownloadAnim(
    				pacName, 
    				versionCode, 
    				drawable, 
    				fromX, 
    				mDownloadLocation[0], 
    				(fromY-mStatusBarHeight), 
    				mDownloadLocation[1]);
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
}
