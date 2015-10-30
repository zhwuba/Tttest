package com.zhuoyi.market.appdetail;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.statistics.ReportFlag;
import com.market.statistics.ReportFlag.FromDes;
import com.zhuoyi.market.MarketDialog;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;

public class AppDetailInfoActivity extends DownloadBaseActivity implements
		 IAppDetailDownloadCallBack {

	public static final int DELETE_DOWNLOAD_APK = 7;
	public static final int REFRESH_DETAIL_DATA = 8;
	public static final int GET_RECOMMEND_APP = 9;
	public static final int GET_APP_COMMENTS = 10;
	public static final int USER_COMMENTS_COMMIT = 11;
	public static final int BACK_TO_SPLASH = 12;
	public static final int CANCEL_DOWNLOAD_APK = 13;
	public static final int COMMENT_ENABLE = 14;
	

	private Handler mHandler;
	private int mRefId = -1;
	private String mPackageName;
	private String mUploadFlag = "";
	private String mActivityUrl;
	private String mTopicId = ReportFlag.TOPIC_NULL;
	private Activity mInstance;
	private static Toast mToast;
	private static boolean mBackToSplash = false;
	private static boolean mIsFromInner;
	private boolean mIsAutoDownload;
	
	private AppDetailView mAppDetailView;

	private String mLogDes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		Constant.initMarketUrl(getApplicationContext());
		mInstance = this;
		Intent intent = getIntent();
		mRefId = intent.getIntExtra("refId", -1);
		mIsAutoDownload = intent.getBooleanExtra("auto_download", false);
		mUploadFlag = intent.getStringExtra("from_path");
		mTopicId = Integer.toString(intent.getIntExtra("topicId", -1));
//		FromDes fromDes = ReportFlag.splitFromFlag(mUploadFlag);
//		mChildDes = fromDes.childView;
//		mUploadFlag = fromDes.fromFlag;
		mActivityUrl = intent.getStringExtra("activity_url");
		if(mRefId == -1) {
			mPackageName = getIntent().getStringExtra("packname");
		}
		
		mIsFromInner = getIntent().getBooleanExtra("fromInner", false);

		if (!ReportFlag.FROM_DETAIL_ALLLIKE.equals(mUploadFlag)
		        && !ReportFlag.FROM_DETAIL_RECOMMEND.equals(mUploadFlag)) {
    		if (TextUtils.isEmpty(mUploadFlag) || ReportFlag.FROM_NULL.equals(mUploadFlag)) {
    			mUploadFlag = ReportFlag.FROM_DETAIL;
    		} else {
    			mUploadFlag = mUploadFlag + ReportFlag.FROM_DETAIL;
    		}
		} else {
		    mUploadFlag = mUploadFlag + ReportFlag.FROM_DETAIL;
		    mTopicId = ReportFlag.TOPIC_NULL;
		}

		mHandler = new Handler() {

			public void handleMessage(Message msg) {
				HashMap<String, Object> map = null;
				switch (msg.what) {

				case REFRESH_DETAIL_DATA:
					map = (HashMap<String, Object>) msg.obj;
					mAppDetailView.bindDetailData(map);
					mLogDes = mAppDetailView.getApkDetailLogDes();
					if (mLogDes != null) {
			            UserLogSDK.logActivityEntry(getApplicationContext(), mLogDes);
			        }
					break;

				case GET_RECOMMEND_APP:
					map = (HashMap<String, Object>) msg.obj;
					mAppDetailView.bindRecommendApp(map);
					break;

				case CANCEL_DOWNLOAD_APK:
					MarketDialog dialog = new MarketDialog(mInstance,
							R.style.MyMarketDialog, mHandler, DELETE_DOWNLOAD_APK,
							getString(R.string.detail_delete_info),
							getString(R.string.detail_delete_title));
					dialog.show();
					break;

				case DELETE_DOWNLOAD_APK:
					onClickDeleteButton();
					break;

				case GET_APP_COMMENTS:
					map = (HashMap<String, Object>) msg.obj;
					mAppDetailView.bindCommentData(map);
					break;

				case USER_COMMENTS_COMMIT:
					map = (HashMap<String, Object>) msg.obj;
					mAppDetailView.addNewComment(map);
					break;

				case BACK_TO_SPLASH: 
					mIsFromInner = true;
					Intent intent = new Intent(mInstance, Splash.class);
					if (msg.arg1 != -1) { 
						SharedPreferences settings = getSharedPreferences(
								Splash.PREFS_NAME, 0);
						Editor editor = settings.edit();
						editor.putBoolean(Splash.FIRST_RUN, false);
						editor.commit();
						if (Splash.getHandler() == null) {
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("showLoadingUI", false);
							startActivity(intent);
						}
					} else {
						intent.putExtra("isClose", true);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					finish();
					break;
					
				case COMMENT_ENABLE:
					if(mAppDetailView != null) {
						mAppDetailView.setBottomButton();
					}
					break;
					
				}
			}
		};
		
		mAppDetailView = new AppDetailView(getApplicationContext(), this, mHandler);
		this.setContentView(mAppDetailView.getView(mRefId, mPackageName, mUploadFlag, mTopicId, mActivityUrl, mIsAutoDownload));
		
		LinearLayout baseView = (LinearLayout) findViewById(R.id.base_layout);
		MarketUtils.setTitleLayout(baseView, this.getApplicationContext());
	}


	@Override
	protected void onResume() {
		if (!mIsFromInner) {
			isShowAD(false);
		} else {
			isShowAD(true);
		}
		super.onResume();
		mAppDetailView.notifyView();
		if (mLogDes != null) {
		    UserLogSDK.logActivityEntry(getApplicationContext(), mLogDes);
		}
	}

	
	@Override
    protected void onPause() {
	    if (mLogDes != null) {
            UserLogSDK.logActivityExit(getApplicationContext(), mLogDes);
        }
	    
        super.onPause();
    }


    protected void onDestroy() {
		mInstance = null;
		mAppDetailView.destoryView();
		super.onDestroy();
	}


	@Override
	public void finish() {

		if (!mIsFromInner) {
			MarketDialog dialog = new MarketDialog(mInstance,
					R.style.MyMarketDialog, mHandler, BACK_TO_SPLASH,
					getString(R.string.back_to_home),
					getString(R.string.return_introduction),
					MarketDialog.FORCE_CLOSE);
			dialog.show();
		} else {
			super.finish();
		}
	}
	

	private void onClickDeleteButton() {
		mAppDetailView.onClickDeleteButton();
	}



	@Override
	protected void onDownloadServiceBind() {
		mAppDetailView.onDownloadServiceBind();
	}


	@Override
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		mAppDetailView.onApkDownloading(eventInfo);
	}


	@Override
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		mAppDetailView.onNoEnoughSpace(eventInfo);
	}


	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		mAppDetailView.onSdcardLost(eventInfo);
	}


	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		mAppDetailView.onDownloadHttpError(eventInfo);
	}


	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		mAppDetailView.onDownloadProgressUpdate(eventInfo);
	}


	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		mAppDetailView.onDownloadComplete(eventInfo);
	}


	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		mAppDetailView.onInstalling(eventInfo);
	}


	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		mAppDetailView.onInstallSuccess(eventInfo);
	}


	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		mAppDetailView.onInstallFailed(eventInfo);
	}


	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		mAppDetailView.onFileNotFound(eventInfo);
	}


	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		mAppDetailView.onFileNotUsable(eventInfo);
	}


	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId,
			String type, int verCode, int appId, long totalSize) {
		try {
			addDownloadApk(pacName, appName, md5, url, topicId,
			        type, verCode, appId, totalSize);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
	}


	@Override
	public void onWindowAttributesChanged(
			android.view.WindowManager.LayoutParams params) {
		super.onWindowAttributesChanged(params);
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			if (mAppDetailView.mTitleDownload != null)
				mAppDetailView.mTitleDownload.setDownloadStatus();
		}
		super.onWindowFocusChanged(hasFocus);
	}


	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
			return pauseDownloadApk(pkgName,verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}


	public static boolean ismBackToSplash() {
		return mBackToSplash;
	}


	public static void setIsFromInner(boolean isFromInner) {
		mIsFromInner = isFromInner;
	}


	public static void showToast(Context context, int stringId) {
		if (mToast == null) {
			mToast = Toast.makeText(context, stringId, Toast.LENGTH_SHORT);
		}
		mToast.setText(stringId);
		mToast.show();
	}


	@Override
	public boolean onResumeDownload(String pkgName, int verCode) {
		try {
			return startDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}


	@Override
	public boolean onCancelDownload(String pkgName, int verCode) {
		try {
			return cancelDownloadApk(pkgName, verCode, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	
}
