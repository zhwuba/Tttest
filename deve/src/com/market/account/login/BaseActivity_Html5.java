package com.market.account.login;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.GetUserInfo;
import com.market.account.netutil.HttpOperation;
import com.market.account.netutil.JavaScriptInterface;
import com.market.account.netutil.JavaScriptOperation;
import com.market.account.netutil.JavaScriptOperation.CallBack;
import com.market.account.tencent.AppConstants;
import com.market.account.tencent.Util;
import com.market.account.user.User;
import com.market.account.utils.EncoderAndDecoder;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PropertyFileUtils;
import com.market.account.weibosdk.AccessTokenKeeper;
import com.market.account.weibosdk.RequestListener;
import com.market.download.userDownload.DownStorage;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.market.account.weibosdk.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.wxapi.ShareActivity;
import com.zhuoyi.market.wxapi.ShareToWeibo;
import com.zhuoyi.market.wxapi.ShareToWeixin;

public class BaseActivity_Html5 extends BaseAcvivity_web implements
		JavaScriptInterface {

	public boolean mForResult = false;// 用于绑定手机时返回结果

	public ImageView mImageView_Present;
	public String mUrl_Present = null;
	public boolean mShowPresent = false;
	protected WebView mWebView;
	public ProgressDialog mProgressDialog;
	// public ProgressBar progressBar_id;
	public ProgressBar top_progressBar_id;
	boolean blockLoadingNetworkImage = false;
	public static final String AUTOHORITY = "com.zhuoyi.market.downloadModule.DownloadProvider";
	private FrameLayout usercenter_view_id;

	public static Tencent mTencent;
	public static String mAppid;
	private UserInfo mInfo;
	private User user = new User();

	private LinearLayout mErrorLayout;
	private Button mRefreshBt;
	public String mLoadUrl = "";
	private boolean mLoadSuccessed;
	protected boolean mIsLoadBackUrl; // 是否加载的是 javascript的返回方法
	private boolean mIsLoading = false;
	/** 是否有等待发放的奖品 **/
	public static String HAS_GIFT_RECEIVER = "hasGiftReceiver";

	private OtherUserLoginTask mOtherUserLoginTask;
	/** Title */
	public String mTitle;
	private AuthListener mLoginListener = new AuthListener();
	AuthInfo authInfo = null;
	/** SSO authorization */
	private SsoHandler mSsoHandler = null;
	private WeiboAuthListener mAuthListener;
	public Handler mHandler = new Handler();
	private GetUserInfo getUserInfo;
	public boolean exit_flag = false;

	private BroadcastReceiver mApkStateReceiver;
	private BroadcastReceiver mShareCallBackReceiver;
	public final static String SHARE_CALLBACK = "com.yyapk.login.ACTION_SHARE_CALLBACK";
	public static final String SHARE_URL = "share/index.html";

	protected static boolean mRefreshPage = false;

	private String mUploadFlag;
	private int mTopicId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_usercenter_shop);
		Intent intent = getIntent();
		mUploadFlag = intent.getStringExtra("from_path");
		mTopicId = intent.getIntExtra("topicId", -1);
		if (TextUtils.isEmpty(mUploadFlag) || mUploadFlag.equals("Null")) {
			mUploadFlag = "DownGift";
		}

		initView();
		initListener();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getResources().getString(
				R.string.user_center_loading));
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mOtherUserLoginTask != null) {
					mOtherUserLoginTask.cancel(true);
					mOtherUserLoginTask = null;
				}
			}
		});
		if (mShareCallBackReceiver == null) {
			mShareCallBackReceiver = new ShareCallBackReceiver();
			registerReceiver(mShareCallBackReceiver, new IntentFilter(
					SHARE_CALLBACK));
		}
		if (mApkStateReceiver == null) {
			mApkStateReceiver = new ApkStateChangeReciver();
			registerReceiver(mApkStateReceiver, new IntentFilter(
					"com.zhuoyi.market.install.INSTALL_COMPLETED"));
		}
		// QQ
		final Context ctxContext = getApplicationContext();
		mAppid = AppConstants.APP_ID;
		try {
			if (mTencent == null) {
				mTencent = Tencent.createInstance(mAppid, ctxContext);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		authInfo = new AuthInfo(this,
				com.market.account.weibosdk.Constants.APP_KEY,
				com.market.account.weibosdk.Constants.REDIRECT_URL,
				com.market.account.weibosdk.Constants.SCOPE);
		mAuthListener = mLoginListener;

		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheMaxSize(1024 * 1024);
		// webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setRenderPriority(RenderPriority.HIGH);
		webSettings.setBlockNetworkImage(true);

		Method downPolymorphic = null;
		try {
			downPolymorphic = mWebView.getClass().getMethod(
					"setOverScrollMode", new Class[] { int.class });
			if (downPolymorphic != null)
				downPolymorphic.invoke(mWebView,
						new Object[] { WebView.OVER_SCROLL_NEVER });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		blockLoadingNetworkImage = true;
		mWebView.setWebViewClient(new HelloWebViewClient());
		mWebView.setWebChromeClient(new MyWebChromeClient());
		mWebView.addJavascriptInterface(this, "zhuoyou_login");

		mWebView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
	}


	private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] result = null;
		if (bmp == null)
			return null;
		try {
			bmp.compress(CompressFormat.PNG, 100, output);
			if (needRecycle) {
				bmp.recycle();
			}
			result = output.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (output != null)
				output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}


	public void initView() {
		mImageView_Present = (ImageView) findViewById(R.id.subtitle_present);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setVisibility(View.VISIBLE);
		mRefreshBt = (Button) findViewById(R.id.refresh_btn);
		usercenter_view_id = (FrameLayout) findViewById(R.id.usercenter_view_id);
		mErrorLayout = (LinearLayout) findViewById(R.id.error_layout);
		top_progressBar_id = (ProgressBar) findViewById(R.id.top_progressBar_id);
	}


	public void initListener() {
		mRefreshBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (GetPublicParams.getAvailableNetWorkType(getApplicationContext()) == -1) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.tip_network_wrong),
							Toast.LENGTH_LONG).show();
				} else {
					top_progressBar_id.setVisibility(View.VISIBLE);
					mWebView.setVisibility(View.VISIBLE);
					mWebView.clearView();
					mErrorLayout.setVisibility(View.GONE);
					loadData();
				}
			}
		});
	}


	public void loadData() {
		mWebView.loadUrl(com.market.account.dao.UserInfo.getEncryUrl(getApplicationContext(), mLoadUrl));
	}


	@Override
	protected void onStop() {
		super.onStop();
	}


	public void updateUserInfo(JSONObject newUserInfoJsonObject) {
		final JSONObject userInfoJsonObject = com.market.account.dao.UserInfo
				.getUserInfo(getApplicationContext());
		if (userInfoJsonObject == null)
			return;
		final AccountManager accountManager = AccountManager
				.get(getApplicationContext());
		Account[] accounts = accountManager
				.getAccountsByType(Constant.ACCOUNT_TYPE);
		try {
			final String nickName = newUserInfoJsonObject.has("nickname") ? newUserInfoJsonObject
					.getString("nickname") : null;
			String avatar = newUserInfoJsonObject.has("avatar") ? newUserInfoJsonObject
					.getString("avatar") : null;
			String score = newUserInfoJsonObject.has("score") ? newUserInfoJsonObject
					.getString("score") : null;
			String openid = newUserInfoJsonObject.has("openid") ? newUserInfoJsonObject
					.getString("openid") : null;
			String gender = newUserInfoJsonObject.has("gender") ? newUserInfoJsonObject
					.getString("gender") : null;
			final String token = userInfoJsonObject.has("TOKEN") ? userInfoJsonObject
					.getString("TOKEN") : null;

			userInfoJsonObject.put("username", nickName).put("logoUrl", avatar)
					.put("recode", score).put("openid", openid)
					.put("gender", gender);

			if (token != null) {
				accountManager.removeAccount(accounts[0],
						new AccountManagerCallback<Boolean>() {

							@Override
							public void run(AccountManagerFuture<Boolean> future) {
								Account account = new Account(
										nickName,
										Constant.ACCOUNT_TYPE);
								accountManager.addAccountExplicitly(account,
										token, null);
								accountManager.setUserData(account, "userInfo",
										userInfoJsonObject.toString());
							}
						}, mHandler);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static void deleteUserLogo() {
		String logoFile = PropertyFileUtils.getSDPath()
				+ Constant.USERLOGO_PATH;
		File file = new File(logoFile);
		if (file != null && file.exists()) {
			file.delete();
		}
	}


	/*********************** HTML5 interface **********************************/
	public void zhuoyou_login_logout() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {

				mProgressDialog.setMessage(getResources().getString(
						R.string.user_center_logout));
				mProgressDialog.show();
				AccountManager mAccountManager = AccountManager
						.get(getApplicationContext());
				Account[] accounts = mAccountManager
						.getAccountsByType(Constant.ACCOUNT_TYPE);
				if (accounts != null && accounts.length >= 1) {
					mAccountManager.removeAccount(accounts[0],
							new AccountManagerCallback<Boolean>() {

								@Override
								public void run(AccountManagerFuture<Boolean> future) {
									deleteUserLogo();
									mProgressDialog.dismiss();
									mWebView.loadUrl(mLoadUrl);
								}
							}, mHandler);
				} else {
					mProgressDialog.dismiss();
				}
			}
		});
	}


	public void zhuoyou_login_update_userinfo(String userInfo) {
		try {
			JSONObject jsonObject = new JSONObject(userInfo);
			updateUserInfo(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void zhuoyou_login_shareToWXTimeLine() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				ShareToWeixin shareToWeixin = new ShareToWeixin(
						getApplicationContext());
				if (ShareToWeixin.api.isWXAppInstalled()) {
					shareToWeixin.SharetoWX(true,
							getScreenShot(usercenter_view_id));
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.weixin_uninstall, Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
	}


	public void zhuoyou_login_shareToWeibo() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				ShareToWeibo shareToWeibo = new ShareToWeibo(
						BaseActivity_Html5.this);
				shareToWeibo.setmBitmap(getScreenShot(usercenter_view_id));
				shareToWeibo.setmDesc(getResources().getString(
						R.string.tip_share_pic));
				shareToWeibo.shareToSinaWeibo(ShareToWeibo.SHARE_ACHIV);
			}
		});
	}


	public void zhuoyou_login_shareToWXTimeLine(String desc, String share_url) {
		ShareToWeixin shareToWeixin = new ShareToWeixin(this);

		if (ShareToWeixin.api.isWXAppInstalled()) {
			if (TextUtils.isEmpty(share_url)) {
				shareToWeixin.shareWebPageToWX(this, true,
						JavaScriptOperation.getFilterProfixUrl() + SHARE_URL,
						desc, null, null);
			} else {
				shareToWeixin.shareWebPageToWX(this, true, share_url, desc,
						null, null);
			}
		} else {
			Toast.makeText(this, R.string.weixin_uninstall, Toast.LENGTH_SHORT)
					.show();
		}
	}


	public void zhuoyou_login_shareToWeibo(final String desc,
			final String share_url) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				ShareToWeibo shareToWeibo = new ShareToWeibo(
						BaseActivity_Html5.this);
				shareToWeibo.setmDesc(desc);
				if (TextUtils.isEmpty(share_url)) {
					shareToWeibo.setmWebUrl(JavaScriptOperation
							.getFilterProfixUrl() + SHARE_URL);
				} else {
					shareToWeibo.setmWebUrl(share_url);
				}
				shareToWeibo.shareToSinaWeibo(ShareToWeibo.SHARE_REDPACKET);
			}
		});
	}


	public void zhuoyou_login_goto(String url, String titleName, String callback) {
		if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(titleName)) {
			Intent intent = new Intent(this, BaseHtmlActivity.class);
			intent.putExtra("wbUrl", url);
			intent.putExtra("titleName", titleName);
			intent.putExtra("fromMarket", false);
			if (!TextUtils.isEmpty(callback)) {
				intent.putExtra("callback", callback);
			}
			startActivity(intent);
		}
	}


	public void zhuoyou_login_goto(String url, String titleName) {
		if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(titleName)) {
			Intent intent = new Intent(this, BaseHtmlActivity.class);
			intent.putExtra("wbUrl", url);
			intent.putExtra("titleName", titleName);
			intent.putExtra("fromMarket", false);
			startActivity(intent);
		}
	}
	

	public int zhuoyou_login_download_state(String pkgName, String verCode) {
		String result = null;
		try {
			result = getApplicationContext().getContentResolver().getType(
					Uri.parse("content://" + AUTOHORITY + "/" + pkgName
							+ verCode));
			return TextUtils.isEmpty(result) ? -1 : Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}


	public String get_openid() {
		String openid = "";
		try {
			JSONObject userInfoJsonObject = com.market.account.dao.UserInfo
					.getUserInfo(getApplicationContext());
			if (userInfoJsonObject != null) {
				openid = userInfoJsonObject.getString("openid");
			}
			return openid;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return openid;
	}


	public String get_token() {
		String token = "";
		try {
			JSONObject userInfoJsonObject = com.market.account.dao.UserInfo.getUserInfo(getApplicationContext());
			if (userInfoJsonObject != null) {
				token = userInfoJsonObject.getString("TOKEN");
			}
			return token;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return token;
	}


	public String generate_sign(String params) {
		try {
			String sign = MD5Util.md5(params + Constant.SIGNKEY);
			return sign;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public void zhuoyou_login_authenticator() {
		startActivity(new Intent(getApplicationContext() ,AuthenticatorActivity.class));
	}


	public void zhuoyou_login_register() {
		Intent intent = new Intent(getApplicationContext(),
				RegisterActivity_new.class);
		RegisterActivity_new.setCallBackWebView(mWebView);
		startActivity(intent);
	}


	/**
	 * launcher apk
	 */
	public void zhuoyou_login_start_app(String pkgName) {
		PackageManager packageManager = getApplicationContext().getPackageManager();
		Intent intent = new Intent();
		try {
			intent = packageManager.getLaunchIntentForPackage(pkgName);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void zhuoyou_login_close() {
		finish();
	}


	public int zhuoyou_login_getApkVersionCode(String packageName) {
		if (TextUtils.isEmpty(packageName))
			return -1;
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);
			return pi.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}


	public void zhuoyou_login_update_userinfo() {
		if (getUserInfo != null
				&& getUserInfo.getStatus() != AsyncTask.Status.RUNNING) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					getUserInfo.execute();
				}
			});
		}
	}


	public void zhuoyou_login_auth(String utype) {

		if (utype.equals("openqq")) {
			mHandler.post(new Runnable() {

				public void run() {
					onClickLogin();
				}
			});
		} else if (utype.equals("openweibo")) {
			mHandler.post(new Runnable() {

				public void run() {
					/** web login **/
					if (authInfo != null) {
					    mSsoHandler = new SsoHandler(BaseActivity_Html5.this, authInfo);
                        mSsoHandler.authorizeWeb(mAuthListener);
					}
				}
			});

		}
	}


	/**
	 * apk download
	 */
	public int zhuoyou_login_download(String info) {
		if (!TextUtils.isEmpty(info)) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(info);
				String packageName = jsonObject.getString("pkg_name");
				String apkName = jsonObject.getString("apk_name");
				String md5 = jsonObject.getString("md5");
				String url = jsonObject.getString("url");
				int appId = jsonObject.getInt("apk_id");
				int vercode = jsonObject.getInt("vercode");
				String appUrl = jsonObject.has("app_url") ? jsonObject
						.getString("app_url") : null;
				String from = mUploadFlag;// jsonObject.getString("from");
				// Byte[] bitmapArray = (Byte[]) jsonObject.get("bitmapArray");
				String imageUrl = jsonObject.getString("icon");
				Intent intent = new Intent("com.zhuoyi.market.extern.download");
				intent.putExtra("packageName", packageName);
				intent.putExtra("appId", appId);
				intent.putExtra("apkName", apkName);
				intent.putExtra("md5", md5);
				intent.putExtra("url", url);
				intent.putExtra("from", from);
				intent.putExtra("verCode", vercode);
				// intent.putExtra("bitmapArray", bitmapArray);
				intent.putExtra("imageUrl", imageUrl);
				intent.putExtra("app_url", appUrl);
				intent.putExtra("topicId", mTopicId);
				sendBroadcast(intent);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}


	public int zhuoyou_login_apk_detail(String info, int score) {
		if (!TextUtils.isEmpty(info)) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(info);
				String activity_url = jsonObject.has("activity_url") ? jsonObject.getString("activity_url") : null;
				String refId = jsonObject.has("apk_id") ? jsonObject.getString("apk_id") : null;
				Intent intent = new Intent("com.zhuoyi.appDetailInfo");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("refId", TextUtils.isEmpty(refId) ? -1
						: Integer.valueOf(refId));
				intent.putExtra("from_path", mUploadFlag);
				intent.putExtra("fromInner", true);
				intent.putExtra("activity_url", activity_url);
					startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}


	public void zhuoyou_login_share() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				getScreenShot(usercenter_view_id);
				Intent intent = new Intent();
				intent.putExtra("from", ShareActivity.SHARE_ACHIV);
				intent.setClass(getApplicationContext(), ShareActivity.class);
				startActivity(intent);

			}
		});

	}


	public void zhuoyou_login_update_title(final String title) {
		if (!TextUtils.isEmpty(title)) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					setTitleText(title);
					setPresentShow(mImageView_Present, mShowPresent);
				}
			});
		}
	}


	public int zhuoyou_login_download_progress(String info) {
		int progress = 0;
		if (!TextUtils.isEmpty(info)) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(info);
				String packageName = jsonObject.getString("pkg_name");
//				String apkName = jsonObject.getString("apk_name");
//				String md5 = jsonObject.getString("md5");
//				String url = jsonObject.getString("url");
				String fileSize = jsonObject.getString("filesize");
				int vercode = jsonObject.getInt("vercode");
				// long totalSize = getApkTotalSizeByUrl(url);
				if (!TextUtils.isEmpty(fileSize)) {
					long totalSize = Long.parseLong(fileSize);
					if (totalSize <= 0) {
						return 0;
					}

					DownloadEventInfo downloadEventInfo =  DownStorage.getFgDownloadEventInfo(getApplicationContext(), packageName, vercode);
					if (downloadEventInfo.getApkFile().exists()) {
						return 100;
					} else {
						File file = new File(downloadEventInfo.getDownloadFilePath());
						if (file.exists()) {
							return (int) (file.length() * 100 / totalSize);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return progress;
	}


	/***************************************************************************************/
	public long getApkTotalSizeByUrl(String urlString) {
		long size = 0;
		HttpURLConnection connection = null;
		URL url;
		try {
			url = new URL(urlString);
			connection = (HttpURLConnection) url.openConnection();
			size = connection.getContentLength();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}


	private Bitmap getScreenShot(View mScreenshot) {
		/*
		 * mScreenshot.measure(MeasureSpec.makeMeasureSpec(0,
		 * MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
		 * MeasureSpec.UNSPECIFIED)); mScreenshot.layout(0, 0,
		 * mScreenshot.getMeasuredWidth(), mScreenshot.getMeasuredHeight());
		 * Bitmap bitmap = Bitmap.createBitmap(mScreenshot.getDrawingCache());
		 * BitMapUtils.saveBitmapToFile(bitmap); bitmap.recycle(); bitmap =
		 * null;
		 */
		try {
			for (int i = 0; i < 2; i++) {
				Bitmap bmp = Bitmap.createBitmap(mScreenshot.getWidth(),
						mScreenshot.getHeight(), Config.ARGB_8888);
				if (bmp != null) {
					mScreenshot.draw(new Canvas(bmp));
					return bmp;
				}
			}
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		return null;
	}


//	public boolean uninstallSoftware(Context context, String packageName) {
//		PackageManager packageManager = context.getPackageManager();
//		try {
//			PackageInfo pInfo = packageManager.getPackageInfo(packageName,
//					PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
//
//			if (pInfo != null) {
//				return true;
//			}
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	public class HelloWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (!mIsLoadBackUrl) {
				exit_flag = false;
			}
			return super.shouldOverrideUrlLoading(view, url);
		}


		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// mProgressDialog.show();
			super.onPageStarted(view, url, favicon);
			top_progressBar_id.setVisibility(View.VISIBLE);
			top_progressBar_id.setProgress(10);
			mIsLoading = true;
			int index = url.indexOf("&openid");
			if (index == -1) {
				index = url.indexOf("?openid");
			}
			try {
				if (index == -1) {
					mLoadUrl = url;
				} else {
					mLoadUrl = url.substring(0, index);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			// mProgressDialog.dismiss();
			if (blockLoadingNetworkImage) {
				if (mWebView != null) {
					mWebView.getSettings().setBlockNetworkImage(false);
					blockLoadingNetworkImage = false;
				}
			}
			mIsLoading = false;
		}

		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler,
				SslError error) {
			handler.proceed();
		}


		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mErrorLayout.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
			top_progressBar_id.setVisibility(View.GONE);
			mLoadUrl = failingUrl;
			mLoadSuccessed = false;
		}

	}

	private class MyWebChromeClient extends WebChromeClient {

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
			return super.onConsoleMessage(consoleMessage);
		}


		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (top_progressBar_id != null && mWebView != null
					&& !mIsLoadBackUrl) {
				top_progressBar_id.setProgress(newProgress);
				if (newProgress >= 100) {
					top_progressBar_id.setProgress(newProgress);
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							if (!mIsLoading) {
								top_progressBar_id.setVisibility(View.GONE);
							}
						}
					}, 300);

					if (!mLoadSuccessed) {
						mLoadSuccessed = true;
					}
					mWebView.requestFocus();
				} else {
					top_progressBar_id.setVisibility(View.VISIBLE);
				}
			}

		}

	}


	/************************************************ qq login start ***************************************************/
	private void onClickLogin() {
		if (mTencent != null) {
			IUiListener listener = new BaseUiListener() {

				@Override
				protected void doComplete(JSONObject values) {
					updateUserInfo();
				}


				@Override
				public void onError(UiError e) {
					super.onError(e);
					mProgressDialog.cancel();
				}

			};
			try {
				mProgressDialog.setMessage(getResources().getString(
						R.string.authing));
				mProgressDialog.show();
				mTencent.setAccessToken(user.getTOKEN(), user.getExpires_in());
				mTencent.setOpenId(user.getOpenid());
				mTencent.login(this, "all", listener);
			} catch (Exception e) {
				mProgressDialog.cancel();
				e.printStackTrace();
			}

		}
	}

	private class BaseUiListener implements IUiListener {

		@Override
		public void onComplete(Object response) {
			JSONObject values = (JSONObject) response;
			try {
				user.setExpires_in(values.has("expires_in") ? values
						.getString("expires_in") : null);
				user.setTOKEN(values.has("access_token") ? values
						.getString("access_token") : null);
				user.setUID(values.has("openid") ? values.getString("openid")
						: null);
				user.setOpenKey(values.has("pfkey") ? values.getString("pfkey")
						: null);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			this.doComplete(values);
		}


		protected void doComplete(JSONObject values) {
		}


		@Override
		public void onError(UiError e) {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {

				mProgressDialog.dismiss();
			}
		}


		@Override
		public void onCancel() {
			Util.toastMessage(BaseActivity_Html5.this,
					getText(R.string.weibosdk_demo_toast_auth_canceled)
							.toString());
			if (mProgressDialog != null && mProgressDialog.isShowing()) {

				mProgressDialog.dismiss();
			}
		}

	}

	/**
	 * Represents an asynchronous task used to authenticate a user against the
	 * SampleSync Service
	 */
	public class OtherUserLoginTask extends AsyncTask<String, Void, String> {

		private String loginResult;
		private String data;
		private String utype;


		public OtherUserLoginTask(String utype) {
			this.utype = utype;
		}


		@Override
		protected String doInBackground(String... params) {
			data = params[0];
			final Map<String, String> loginParams = new HashMap<String, String>();
			String devinfo = com.market.account.dao.UserInfo.getDeviceInfo(getApplicationContext());
			String openid = get_openid();
			if (TextUtils.isEmpty(openid)) {
				return null;
			}
			String token = get_token();
			String signString = MD5Util.md5(user.getUID() + user.getTOKEN()
					+ utype + data + openid + token + devinfo
					+ Constant.SIGNKEY);

			loginParams.put("uid", user.getUID());
			loginParams.put("passwd", user.getTOKEN());
			loginParams.put("utype", utype);
			loginParams.put("data", data);
			loginParams.put("sign", signString);
			loginParams.put("openid", openid); // qq unique id at zhuoyou
			loginParams.put("token", token); //
			loginParams.put("devinfo", devinfo);
			loginResult = HttpOperation.postRequest(Constant.AUTH, loginParams);
			// openqq={"openqq":"8E654C320C76CDA765F0617658FCE4CF","nickname":"15618262164","result":0,"openid":"5372f1f4aebf1ef16b000005","desc":"缂傚倷鐒﹂崹鐢告偩閸撗勫闁挎洩鎷锋繛鍥煙鐎涙ê濮囧┑顕嗘嫹,"expire":1405744887,"token":"537adcf7aebf1e8e45000002","cancheckin":true,"score":5230,"username":"15618262164","checkin5days":0,"checkindaliy":70}
			try {
				// httpManager.jsonUnboxing(loginResult, user);
				JSONObject jsonObject = new JSONObject(loginResult);
				// result == 0
				int result = jsonObject.getInt("result");
				if (result == 0) {
					user.setNickname(jsonObject.getString("nickname"));
					user.setUsername(jsonObject.has("username") ? jsonObject
							.getString("username") : null);
					user.setOpenid(jsonObject.getString("openid"));
					user.setRecode(jsonObject.getInt("score"));
					// user.setLevel(jsonObject.getInt("level"));
					user.setLogoUrl(jsonObject.getString("avatar"));
					// user.setGender(jsonObject.getString("gender"));
					// user.setAge(jsonObject.getInt("age"));
					if (TextUtils.isEmpty(user.getLogoUrl())) {
						AuthenticatorActivity.deleteUserLogo();
					} else {
						AuthenticatorActivity.downloadUserLogo(user
								.getLogoUrl());
					}
					updateUserInfo(jsonObject);
				}

				user.setResult(result);
				user.setDesc(jsonObject.getString("desc"));

			} catch (Exception e) {
				e.printStackTrace();
			}
			return loginResult;
		}


		@Override
		protected void onPostExecute(final String authToken) {
			// On a successful authentication, call back into the Activity to
			// communicate the authToken (or null for an error).
			// onAuthenticationResult(user);
			if (mProgressDialog != null && mProgressDialog.isShowing()) {

				mProgressDialog.dismiss();
			}
			if (user.getResult() == 0) {
				mWebView.loadUrl("javascript:zhuoyou_login_auth_setresult('"
						+ utype + "',true)");
				Toast.makeText(getApplicationContext(), user.getDesc(),
						Toast.LENGTH_SHORT).show();
			} else if (user.getResult() < 0 || TextUtils.isEmpty(authToken)) {
				mWebView.loadUrl("javascript:zhuoyou_login_auth_setresult('"
						+ utype + "',false)");
				if (TextUtils.isEmpty(user.getDesc())) {
					Toast.makeText(getApplicationContext(),
							R.string.tip_bind_failed, Toast.LENGTH_SHORT)
							.show();
				} else {
					Toast.makeText(getApplicationContext(), user.getDesc(),
							Toast.LENGTH_SHORT).show();
				}
			}

		}


		@Override
		protected void onCancelled() {
			// If the action was canceled (by the user clicking the cancel
			// button in the progress dialog), then call back into the
			// activity to let it know.
			// onAuthenticationCancel();
			mWebView.loadUrl("javascript:zhuoyou_login_auth_setresult('"
					+ utype + "',false)");
		}
	}


	private void updateUserInfo() {
		// DebugLog.e(Constant.TAG, "html5  mQQAuth=" + mQQAuth +
		// "  mQQAuth.isSessionValid()=" + mQQAuth.isSessionValid());
		if (mTencent != null && mTencent.isSessionValid()
				&& mTencent.getOpenId() != null) // 判断用户是否登录，且是否获取了openid
		{
			IUiListener listener = new IUiListener() {

				@Override
				public void onError(UiError e) {
					Toast.makeText(getApplicationContext(), e.toString(),
							Toast.LENGTH_SHORT).show();
				}


				@Override
				public void onComplete(final Object response) {
					// DebugLog.e(Constant.TAG,
					// "qq get userinfo onComplete response=" +
					// response.toString());
					final JSONObject json = (JSONObject) response;
					if (null != response) {
						// set user bean

						try {
							user.setNickname(json.has("nickname") ? json
									.getString("nickname") : null);
							user.setUsername(json.has("username") ? json
									.getString("username") : null);
							user.setLogoUrl(json.has("figureurl_qq_2") ? json
									.getString("figureurl_qq_2") : null);
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
					mOtherUserLoginTask = new OtherUserLoginTask("openqq");
					mOtherUserLoginTask.execute(response.toString());
				}


				@Override
				public void onCancel() {

				}
			};
			mInfo = new UserInfo(this, mTencent.getQQToken());
			mInfo.getUserInfo(listener);

		} else {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.weibosdk_demo_toast_auth_canceled_again_bind),
					Toast.LENGTH_SHORT).show();
		}
	}

	/************************************************ qq login end ***************************************************/
	/************************************************ sina login start ***********************************************/

	private class AuthListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle values) {
			mProgressDialog.setMessage(getResources().getString(
					R.string.authing));
			mProgressDialog.show();

			Oauth2AccessToken accessToken = Oauth2AccessToken
					.parseAccessToken(values);
			if (accessToken != null && accessToken.isSessionValid()) {

				AccessTokenKeeper.writeAccessToken(getApplicationContext(),
						accessToken);
				final String uid = accessToken.getUid();
				final String token = accessToken.getToken();
				user.setUID(uid);
				user.setExpires_in(String.valueOf(accessToken.getExpiresTime()));
				user.setTOKEN(token);

				mOtherUserLoginTask = new OtherUserLoginTask("openweibo");
				new Thread(new Runnable() {

					@Override
					public void run() {
						StringBuilder sBuilder = new StringBuilder();
						sBuilder.append(
								"https://api.weibo.com/2/users/show.json?")
								.append("source=")
								.append(com.market.account.weibosdk.Constants.APP_KEY)
								.append("&uid=").append(uid)
								.append("&access_token=").append(token);
						String jsonString = null;
						try {
							jsonString = HttpOperation.getRequest(sBuilder
									.toString());

							if (null != jsonString) {
								try {
									JSONObject response1 = new JSONObject(
											jsonString);
									user.setNickname(response1
											.getString("screen_name"));
									user.setLogoUrl(response1
											.getString("profile_image_url"));
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
						/*
						 * / onAuthenticationResult(user); /
						 */
						mOtherUserLoginTask.execute(jsonString);
						// */

					}
				}).start();

			}
		}


		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(),
					Toast.LENGTH_SHORT).show();
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}

		}


		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(),
					R.string.weibosdk_demo_toast_auth_canceled,
					Toast.LENGTH_SHORT).show();
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		}
	}

	private class LogOutRequestListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				try {
					JSONObject obj = new JSONObject(response);
					String value = obj.getString("result");

					if ("true".equalsIgnoreCase(value)) {
						AccessTokenKeeper.clear(getApplicationContext());
						// mTokenView.setText(R.string.weibosdk_demo_logout_success);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}


		@Override
		public void onComplete4binary(ByteArrayOutputStream responseOS) {
			// Do nothing
		}


		@Override
		public void onIOException(IOException e) {
			// mTokenView.setText(R.string.weibosdk_demo_logout_failed);
		}


		@Override
		public void onError(WeiboException e) {
			// mTokenView.setText(R.string.weibosdk_demo_logout_failed);
		}


        @Override
        public void onWeiboException(WeiboException paramWeiboException) {
            // TODO Auto-generated method stub
            
        }
	}


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }


	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			mWebView.stopLoading();
			usercenter_view_id.removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		if (mApkStateReceiver != null) {
			unregisterReceiver(mApkStateReceiver);
			mApkStateReceiver = null;
		}
		if (mShareCallBackReceiver != null) {
			unregisterReceiver(mShareCallBackReceiver);
			mShareCallBackReceiver = null;
		}
		super.onDestroy();
	}

	/** apk install state changed **/
	public class ApkStateChangeReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getStringExtra("package");
			int state = intent.getIntExtra("state", -1);
			if (!TextUtils.isEmpty(packageName)
					&& !TextUtils.isEmpty(String.valueOf(state))) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("pkg_name", packageName);
					jsonObject.put("install_state", state);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mWebView.loadUrl("javascript:zhuoyou_login_state_change('"
						+ packageName + "'," + state + ")");
			}
		}

	}

	class ShareCallBackReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int isShare = intent.getIntExtra("isShare", -2);
			mWebView.loadUrl("javascript:zhuoyou_login_shareCallBack("
					+ isShare + ")");
		}

	}


	@Override
	public void zhuoyou_login_share_app(String appName, String shareUrl) {
		Intent intent = new Intent("com.zhuoyi.market.appdetail.SHARE_APP");
		intent.putExtra("shareStr", shareUrl);
		intent.putExtra("appName", appName);
		sendBroadcast(intent);
	}


	@Override
	public boolean zhuoyou_login_app_collect(boolean isCollect, String appInfo) {
		return false;
	}


	@Override
	public boolean zhuoyou_login_get_collect_status(String packageName) {
		return false;
	}


	@Override
	public String zhuoyou_login_get_uuid() {
		return GetPublicParams.getPublicParaForPush(getApplicationContext(),
				getApplicationContext().getPackageName(), R.raw.td).get("uuid");
	}


	@Override
	public void zhuoyou_login_hasGift_receive() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				HAS_GIFT_RECEIVER, MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean("giftReceiver", true);
		editor.commit();
	}


	@Override
	public void zhuoyou_present(String url) {
		mUrl_Present = url;

	}


	@Override
	public void zhuoyou_present_show(boolean show) {
		mShowPresent = show;
	}


	@Override
	public boolean zhuoyou_copy_text(String needCopy) {
		if (TextUtils.isEmpty(needCopy))
			return false;
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboardManager.setText(needCopy);
		if (!TextUtils.isEmpty(clipboardManager.getText())
				&& clipboardManager.getText().equals(needCopy)) {
			return true;
		} else {
			return false;
		}
	}


	@Override
	public void zhuoyou_bindMobile_result(boolean result, String phone) {
		if (mForResult) {
			Intent intent = new Intent();
			intent.putExtra("result", result);
			intent.putExtra("phone", phone);
			setResult(5, intent);
			finish();
		}
	}


	@Override
	public void zhuoyou_javascript_available() {
		exit_flag = true;
	}


	@Override
	public void zhuoyou_web_refresh() {
		mRefreshPage = true;
	}


	@Override
	public int zhuoyou_show_image() {
		if (Constant.SHOW_IMAGE) {
			return 1;
		} else {
			return 0;
		}
	}


	@Override
	public String zhuoyou_get_marketInfo() {
		String marketId = com.zhuoyi.market.utils.MarketUtils.getSharedPreferencesString(getApplicationContext(), com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
        if (TextUtils.isEmpty(marketId)) {
        	marketId = "null";
        }
        String src = marketId;
        src += "/-1/-1/-1";
        
		TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(getApplicationContext());
		if(terminalInfo == null) return "";
		
		JSONObject reservedJo = null;
		try {
			reservedJo = new JSONObject(terminalInfo.getReserved());
			reservedJo.put("webFlag", 1);
			
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		if (reservedJo == null) {
			reservedJo = new JSONObject();
		}
		
		JSONObject jsonTerminalInfo = new JSONObject();
		try {
			jsonTerminalInfo.put("hman", terminalInfo.getHsman());
			jsonTerminalInfo.put("htype", terminalInfo.getHstype());
			jsonTerminalInfo.put("sWidth", terminalInfo.getScreenWidth());
			jsonTerminalInfo.put("sHeight", terminalInfo.getScreenHeight());
			jsonTerminalInfo.put("ramSize", terminalInfo.getRamSize());
			jsonTerminalInfo.put("lac", terminalInfo.getLac());
			jsonTerminalInfo.put("netType", terminalInfo.getNetworkType());
			jsonTerminalInfo.put("chId", terminalInfo.getChannelId());
			jsonTerminalInfo.put("osVer", terminalInfo.getOsVer());
			jsonTerminalInfo.put("appId", terminalInfo.getAppId());
			jsonTerminalInfo.put("apkVer", terminalInfo.getApkVersion());
			jsonTerminalInfo.put("pName", terminalInfo.getPackageName());
			jsonTerminalInfo.put("apkVerName", terminalInfo.getApkVerName());
			jsonTerminalInfo.put("imsi", terminalInfo.getImsi());
			jsonTerminalInfo.put("imei", terminalInfo.getImei());
			jsonTerminalInfo.put("cpu", terminalInfo.getCpu());
			jsonTerminalInfo.put("romSize", terminalInfo.getRomSize());
			jsonTerminalInfo.put("lbs", terminalInfo.getLbs());
			jsonTerminalInfo.put("uuid", terminalInfo.getUuid());
			jsonTerminalInfo.put("mac", terminalInfo.getMac());
			//jsonTerminalInfo.put("reserved", terminalInfo.getReserved());
			jsonTerminalInfo.put("reserved", reservedJo.toString());
			jsonTerminalInfo.put("marketId", marketId);
			jsonTerminalInfo.put("src", src);
			jsonTerminalInfo.put("from",mUploadFlag);
			jsonTerminalInfo.put("topicId", mTopicId);
			
			return jsonTerminalInfo.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	private String mNeedCopy;
	private String mShowMessage;
	private String mPackName;
	/**
	 * 复制礼包窗口
	 * @param needCopy		需要复制的信息
	 * @param showMessage		窗口显示的信息
	 * @param packName		启动的游戏包名
	 */
	public void zhuoyou_copy_dialog(String needCopy, String showMessage, String packName){
		mNeedCopy = needCopy;
		mShowMessage = showMessage;
		mPackName = packName;
		if(zhuoyou_copy_text(mNeedCopy)){
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					createDialog(1);
				}
			});
		}
	}
	
	private Dialog mDialog;
	private CallBack mCallBack;
	/**
	 * @param type  0:下载安装 1:复制礼包
	 * @return
	 */
	private Dialog createDialog(int type){
		if(mDialog == null) {
			mDialog = new Dialog(this, R.style.MyMarketDialog);
		}
		mDialog.setContentView(R.layout.detail_download_apk_dialog);
		ImageView titleIcon = (ImageView) mDialog.findViewById(R.id.title_image);
		TextView titleText = (TextView) mDialog.findViewById(R.id.title_text);
		TextView showMessage = (TextView) mDialog.findViewById(R.id.showMessage);
		Button sureButton = (Button) mDialog.findViewById(R.id.ok);
		Button cancelButton = (Button) mDialog.findViewById(R.id.cancel);

		switch (type) {	
		case 0:		
			
			AsyncImageCache.from(getApplicationContext()).displayImage(true, false, titleIcon, R.drawable.dialog_warn, 0, 0,
					new AsyncImageCache.GeneralImageGenerator("game_install", null), false,true,true,null);
			titleText.setText(getApplicationContext().getResources().getText(R.string.app_detail_need_install));
			titleText.setTextColor(getApplicationContext().getResources().getColor(R.color.detail_app_dialog_install_text));
			sureButton.setText(getApplicationContext().getResources().getText(R.string.app_detail_game_install));
			showMessage.setVisibility(View.GONE);
			cancelButton.setText(getApplicationContext().getResources().getText(R.string.cancel));
			sureButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(mCallBack != null) {
						mCallBack.onJsCall();
					}
					mDialog.dismiss();
				}
			});
			
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;

		case 1:
			AsyncImageCache.from(getApplicationContext()).displayImage(true, false, titleIcon, R.drawable.dialog_success, 0, 0,
					new AsyncImageCache.GeneralImageGenerator("game_start", null), false,true,true,null);
			titleText.setText(getApplicationContext().getResources().getText(R.string.app_detail_gift_copy_success));
			titleText.setTextColor(getApplicationContext().getResources().getColor(R.color.detail_app_dialog_copy_text));
			sureButton.setText(getApplicationContext().getResources().getText(R.string.app_detail_game_start));
			showMessage.setText(mShowMessage);
			cancelButton.setText(getApplicationContext().getResources().getText(R.string.return_introduction));
			sureButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(!TextUtils.isEmpty(mPackName)) {
						zhuoyou_login_start_app(mPackName);
					}
				}
			});
			
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;

		}
		mDialog.show();
		return mDialog;
	}
}
