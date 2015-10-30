package com.zhuoyi.market;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.market.account.login.BaseHtmlActivity;
import com.market.download.userDownload.DownloadManager;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.TerminalInfo;
import com.market.statistics.ReportFlag;
import com.market.view.CommonTitleDownloadView;
import com.zhuoyi.market.appManage.db.FavoriteDao;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.search.SearchActivity;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class WebActivity extends BaseHtmlActivity implements OnClickListener{


	private ImageView mSearchView;
	private CommonTitleDownloadView mTitleDownload;
	private String mUploadFlag;
	private int mRefId;
	private int mTopicId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		mUploadFlag = intent.getStringExtra("from_path");
		mRefId = intent.getIntExtra("refId", -1);
		mTopicId= intent.getIntExtra("topicId", -1);
		
		if(TextUtils.isEmpty(mUploadFlag) || mUploadFlag.equals(ReportFlag.FROM_NULL))
			mUploadFlag = ReportFlag.FROM_DETAIL;
		else
			mUploadFlag = mUploadFlag + ReportFlag.FROM_DETAIL;
		
		mSearchView = (ImageView) findViewById(R.id.subtitle_search);
		mSearchView.setVisibility(View.VISIBLE);
		mSearchView.setOnClickListener(this);
		
		int heigh = getResources().getDimensionPixelSize(R.dimen.title_heigh);
		mTitleDownload = new CommonTitleDownloadView(getApplicationContext());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		        heigh,
		        heigh);
		
		LinearLayout parentLayout = (LinearLayout) findViewById(R.id.subtitle_main);
		parentLayout.addView(mTitleDownload, params);
		
		mTitleDownload.registeredReceiver();
		
		/**	将app 的 url 地址存入数据库**/
		WebAppDao webAppDao = new WebAppDao(getApplicationContext());
		if(!TextUtils.isEmpty(mUrl)) {
			if(!TextUtils.isEmpty(webAppDao.getWebUrl(mRefId))){
				webAppDao.removeWebAppInfo(mRefId);
			}
			webAppDao.saveWebAppInfo(mRefId, mUrl);
		}
	}
	
	
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if (mTitleDownload != null)
                mTitleDownload.setDownloadStatus();
        }
        super.onWindowFocusChanged(hasFocus);
    }
	
	
	protected void onDestroy() {
        if (mTitleDownload != null)
            mTitleDownload.unRegisteredReceiver();
        super.onDestroy();

	}
	

	@Override
	public void zhuoyou_login_share_app(String appName, String shareUrl) {
		if(!TextUtils.isEmpty(appName) && !TextUtils.isEmpty(shareUrl)){
			Intent intent = new Intent();
			intent.setClass(this, ShareAppActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString(ShareAppActivity.INTENT_KEY_SHARE_URL, shareUrl);
			bundle.putString(ShareAppActivity.INTENT_KEY_APP_NAME, appName);
			intent.putExtras(bundle);
			this.startActivity(intent);
		}
	}

	
	@Override
	public boolean zhuoyou_login_app_collect(boolean isCollect, String appInfo) {
		Gson gson = new Gson();
		AppInfoBto appInfoBto = gson.fromJson(appInfo, AppInfoBto.class);
		if(appInfoBto != null){
			if(isCollect){
				FavoriteInfo favoriteInfo = new FavoriteInfo();
				favoriteInfo.setUrl(appInfoBto.getDownUrl());
				favoriteInfo.setAppName(appInfoBto.getName());
				favoriteInfo.setMd5(appInfoBto.getMd5());
				favoriteInfo.setBitmap(null);
				favoriteInfo.setFileSizeSum(MarketUtils.humanReadableByteCount(appInfoBto.getFileSize(),false));
				favoriteInfo.setLocalFilePath(MarketUtils.FileManage.getSDPath()+ Constant.download_path + appInfoBto.getName() + ".apk");
				favoriteInfo.setVersionCode(appInfoBto.getVersionCode() + "");
				favoriteInfo.setVersionName(appInfoBto.getVersionName());
				favoriteInfo.setAppPackageName(appInfoBto.getPackageName());
				favoriteInfo.setAppId(appInfoBto.getRefId());
				new FavoriteDao(getApplicationContext()).saveInfos(favoriteInfo);
			}else{
				FavoriteDao favoriteDao = new FavoriteDao(getApplicationContext());
				favoriteDao.delete(appInfoBto.getPackageName());
			}
			return true;
		}else{
			return false;
		}
	}
	

	@Override
	public boolean zhuoyou_login_get_collect_status(String packageName) {
		FavoriteDao favoriteDao = new FavoriteDao(getApplicationContext());
		if(!TextUtils.isEmpty(packageName)){
			return favoriteDao.isHasInfors(packageName);
		}else {
			return false;
		}
	}
	
	
	public void zhuoyou_login_detail_download(String downloadInfo) {
		JSONObject jsonObject;
		try
		{
			jsonObject = new JSONObject(downloadInfo);
			final String packageName = jsonObject.getString("pkg_name");
			String apkName = jsonObject.getString("apk_name");
			String md5 = jsonObject.getString("md5");
			String url = jsonObject.getString("url");
			int appId = jsonObject.getInt("apk_id");
			int vercode = jsonObject.getInt("vercode");
			long totalSize = jsonObject.has("totalSize") ? jsonObject.getLong("totalSize") : 0L;
			final String imageUrl = jsonObject.has("imageUrl") ? jsonObject.getString("imageUrl") : null;
			DownloadManager.startServiceAddEvent(getApplicationContext(),
					                            url,
					                            packageName,
					                            apkName,
					                            md5,
					                            Integer.toString(mTopicId),
					                            mUploadFlag,
					                            vercode,
					                            appId,
					                            totalSize);
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					AsyncImageCache.from(getApplicationContext())
					.displayImage(
							new AsyncImageCache.NetworkImageGenerator(packageName,
									imageUrl), mUploadFlag);
					
				}
			});
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void zhuoyou_login_open_recommend(String appInfo) {
		JSONObject jsonObject;
		try
		{
			jsonObject = new JSONObject(appInfo);
			int appId = jsonObject.getInt("apk_id");
			MarketUtils.startDetailActivity(getApplicationContext(), appId, null, -1, null);
			finish();
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.subtitle_search:
			 Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
             startActivity(intent);
			break;
		}
	}

	
}
