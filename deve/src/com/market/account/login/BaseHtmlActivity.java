package com.market.account.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.market.account.authenticator.AccountUpdate;
import com.market.account.authenticator.AccountUpdate.IAccountListener;
import com.market.account.dao.UserInfo;
import com.market.view.CommonLoadingManager;

public class BaseHtmlActivity extends BaseActivity_Html5 implements IAccountListener{

	private String mCallBack;
	protected String mTitle;
	protected String mUrl; 
	protected boolean mFromMarket;
	protected Handler mHandler = new Handler();
	private boolean mNeedTitleBar = true;
	private AccountUpdate mAccountUpdate;
	private boolean mShowExitDialog = false;
	private boolean mShouldCallJs = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent != null){
			mForResult = intent.getBooleanExtra("forResult", false);
			mNeedTitleBar = intent.getBooleanExtra("titleBar", true);
			mShowExitDialog = intent.getBooleanExtra("showExitDialog", false);
			mTitle = intent.getStringExtra("titleName");
			mUrl = intent.getStringExtra("wbUrl");
			if(mUrl.startsWith("EAMAY_")) {
				mUrl = mUrl.substring(6);
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mUrl));
				startActivity(i);
				this.finish();
			} else {
				mCallBack = intent.getStringExtra("callback");
				mFromMarket = intent.getBooleanExtra("fromMarket", false);
				if (!mNeedTitleBar) {
				    setTitleBarGone();
				}
				if (!TextUtils.isEmpty(mTitle)) {
					setTitleText(mTitle);
				}
				if (!TextUtils.isEmpty(mUrl)) {
					mWebView.loadUrl(UserInfo.getEncryUrl(getApplicationContext(), mUrl));
				}
				initViewListener();
			}
			
			mAccountUpdate = new AccountUpdate();
			mAccountUpdate.registerUpdateListener(this);
		}
	}

	public void initViewListener() {
		setLeftButton(0, new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mWebView != null) {
					mWebView.loadUrl("javascript:if(typeof(zhuoyou_login_webview_back)=='function') {zhuoyou_login_webview_back();}else{zhuoyou_login.zhuoyou_login_close();}");
					mIsLoadBackUrl = true; 
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							if (!exit_flag && mWebView != null) {
								if (mWebView.canGoBack()) {
									mWebView.goBack();
								} else {
									finish();
								}
							}else {
							}
							mIsLoadBackUrl = false;
						}
					}, 200);
				} else {
					finish();
				}
}
		});
		mImageView_Present.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mUrl_Present != null)
					mWebView.loadUrl(UserInfo.getEncryUrl(getApplicationContext(), mUrl_Present));
			}
		});
	}
	
	
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus && !mNeedTitleBar) {
            this.onResume();
        }
    }


	@Override
	protected void onResume() {
		super.onResume();
	    CommonLoadingManager.get().showLoadingAnimation(this);
	    callJsRefresh();
	    
		if(mRefreshPage){	//web 通知刷新页面
			mRefreshPage = false;
			if(!TextUtils.isEmpty(mLoadUrl)) {
				mWebView.loadUrl(UserInfo.getEncryUrl(getApplicationContext(), mLoadUrl));
			}
		}
	}
	

	@Override
	public void finish() {
		exit_flag = false;
		if(mRefreshPage) {
			mRefreshPage = false;
			Intent intent = new Intent();
			intent.putExtra("refresh", true);
			setResult(6, intent);
		}
		super.finish();
	}


	@Override
	public void onBackPressed() {
	    if (mShowExitDialog) {
            /** 部分手机  精选-赚积分、游戏-抢礼包 */
	        Intent intent = new Intent("com.zhuoyi.exit.dialog");
	        sendBroadcast(intent);
	        return;
        }
		if (mWebView != null) {
			mWebView.loadUrl("javascript:if(typeof(zhuoyou_login_webview_back)=='function') {zhuoyou_login_webview_back();}else{zhuoyou_login.zhuoyou_login_close();}");
			mIsLoadBackUrl = true;
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (!exit_flag && mWebView != null) {
						if (mWebView.canGoBack()) {
							mWebView.goBack();
						} else {
							finish();
						}
					}else {
					}
					mIsLoadBackUrl = false;
				}
			}, 200);
		} else {
			super.onBackPressed();
		}
	}
	private Toast mToast;
	private void showToast(String stringId){
		if(mToast == null){
			mToast = Toast.makeText(getApplicationContext(), stringId, Toast.LENGTH_SHORT);
		}
		mToast.setText(stringId);
		mToast.show();
	}

	@Override
	protected void onDestroy() {
		mAccountUpdate.unregisterUpdateListener();
		super.onDestroy();
	}

	@Override
	public void onLogin() {
		if (mWebView != null && !TextUtils.isEmpty(mLoadUrl)) {
			mWebView.loadUrl(UserInfo.getEncryUrl(getApplicationContext(), mLoadUrl));
		}
	}

	@Override
	public void onLogout() {
		if(mWebView != null) {
			mWebView.loadUrl(UserInfo.getEncryUrl(getApplicationContext(), mLoadUrl));
		}
	}
	
	
	public void setShouldCallJs(boolean shouldCall) {
		mShouldCallJs = shouldCall;
	}
	
	
	public void callJsRefresh() {
		if(mWebView != null && mShouldCallJs) {
			mWebView.loadUrl("javascript:if(typeof(partRefresh)=='function') {partRefresh();}");
		}
	    
	}
}
