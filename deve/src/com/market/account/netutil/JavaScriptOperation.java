package com.market.account.netutil;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.GetUserInfo;
import com.market.account.dao.UserInfo;
import com.market.account.login.BaseHtmlActivity;
import com.market.account.login.RegisterActivity_new;
import com.market.account.utils.EncoderAndDecoder;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.MD5Util;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.wxapi.ShareToWeibo;
import com.zhuoyi.market.wxapi.ShareToWeixin;

public class JavaScriptOperation implements JavaScriptInterface {

	public Activity mActivity;
	public static final String AUTOHORITY = "com.zhuoyi.market.downloadModule.DownloadProvider";
	private GetUserInfo getUserInfo;
	private static String mPrefixUrl = "";
	public SharedPreferences userPreferences;
	private String mUploadFlag;
	private int mTopicId;
	private Dialog mDialog;
	
	public static final String SHARE_URL = "/share/index.html";

	private  WebView mWebView;

	public WebView getmWebView() {
		return mWebView;
	}

	public JavaScriptOperation(Activity activity,WebView webView, String uploadFlag, int topicId) {
		mActivity = activity;
		mUploadFlag = uploadFlag;
		mTopicId = topicId;
		if(webView != null){
			mWebView = webView;
		}
		userPreferences = mActivity.getSharedPreferences("userCenterPreferences", Context.MODE_PRIVATE);
	}

	public void setPrefixUrl(String url){
		mPrefixUrl = url;
	}

	public static String getFilterProfixUrl(){
		int last = mPrefixUrl.lastIndexOf("/");
		String lastUrl = "";
		if(last != -1){
			lastUrl = mPrefixUrl.substring(0, last);
		}
		return lastUrl;
	}

	/***********************  与HTML5交互  **********************************/
	@Override
	public void zhuoyou_login_shareToWXTimeLine(String desc, String share_url) {
		if(TextUtils.isEmpty(desc)) return;
		ShareToWeixin shareToWeixin = new ShareToWeixin(mActivity);
		if(ShareToWeixin.api.isWXAppInstalled()){
			if(TextUtils.isEmpty(share_url)){
				shareToWeixin.shareWebPageToWX(mActivity, true, JavaScriptOperation.getFilterProfixUrl() + SHARE_URL, desc, null, null);
			}else{
				shareToWeixin.shareWebPageToWX(mActivity, true, share_url, desc, null, null);
			}


		}else{
			Toast.makeText(mActivity, R.string.weixin_uninstall, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void zhuoyou_login_shareToWeibo(final String desc, final String share_url) {
		mActivity.runOnUiThread(new Runnable()    

		{ 
			public void run()     {    

				if(TextUtils.isEmpty(desc)) return;
				ShareToWeibo shareToWeibo = new ShareToWeibo(mActivity);
				shareToWeibo.setmDesc(desc);

				if(TextUtils.isEmpty(share_url)){
					shareToWeibo.setmWebUrl(JavaScriptOperation.getFilterProfixUrl() + SHARE_URL);
				}else{
					shareToWeibo.setmWebUrl(share_url);

				}
				shareToWeibo.shareToSinaWeibo(ShareToWeibo.SHARE_REDPACKET);
			}    
		});

	}

	@Override
	public void zhuoyou_login_goto(String url,String titleName) {
		if(!TextUtils.isEmpty(url) && !TextUtils.isEmpty(titleName)){
			Intent intent = new Intent(mActivity,BaseHtmlActivity.class);
			intent.putExtra("wbUrl", url);
			intent.putExtra("titleName", titleName);
			intent.putExtra("fromMarket", true);
			mActivity.startActivity(intent);
		}
	}

	@Override
	public void zhuoyou_login_goto(String url,String titleName, String callback) {
		if(!TextUtils.isEmpty(url) && !TextUtils.isEmpty(titleName)){
			Intent intent = new Intent(mActivity,BaseHtmlActivity.class);

			String lastUrl = getFilterProfixUrl();

			int first = url.indexOf("/");
			String firstUrl = url.substring(first);

			intent.putExtra("wbUrl",lastUrl + firstUrl);
			intent.putExtra("titleName", titleName);
			intent.putExtra("callback", callback);
			intent.putExtra("fromMarket", true);
			mActivity.startActivity(intent);
		}
	}		

	/** apk下载状态	*/
	@Override
	public int zhuoyou_login_download_state(String pkgName,String verCode) {
		String result = null;
		try {
			result = mActivity.getContentResolver().getType(Uri.parse("content://" + AUTOHORITY + "/" + pkgName + verCode));	//查询下载状态
			return TextUtils.isEmpty(result) ? -1 : Integer.valueOf(result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}


	@Override
	public String get_openid() {
		String openid = "";
		try {
			JSONObject userInfoJsonObject = UserInfo.getUserInfo(mActivity);
			if (userInfoJsonObject != null)
			{
				openid = userInfoJsonObject.getString("openid");
			}
			return openid;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}


	@Override
	public String get_token() {
		String token = "";
		try {
			JSONObject userInfoJsonObject = UserInfo.getUserInfo(mActivity);

			if (userInfoJsonObject != null) {
				token = userInfoJsonObject.getString("TOKEN");
			}
			return token;
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}


	@Override
	public String generate_sign(String params) {
		try {
			String sign = MD5Util.md5(params + Constant.SIGNKEY);
			return sign;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * 跳转登录界面
	 */
	@Override
	public void zhuoyou_login_authenticator() {
		mActivity.startActivity(new Intent(mActivity, AuthenticatorActivity.class));
	}

	/**
	 * 启动apk
	 */
	@Override
	public void zhuoyou_login_start_app(String pkgName) {
		PackageManager packageManager = mActivity.getPackageManager();
		Intent intent = new Intent();
		try {
			intent = packageManager.getLaunchIntentForPackage(pkgName);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		mActivity.startActivity(intent);
	}


	public void zhuoyou_login_close() {
		mActivity.finish();
	}

	@Override
	public int zhuoyou_login_getApkVersionCode(String packageName) {
		if (packageName == null || "".equals(packageName))
			return -1;

		try {
			PackageInfo pi = mActivity.getPackageManager().getPackageInfo(packageName, 0);
			return pi.versionCode;
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public void zhuoyou_login_update_userinfo() {
		if (getUserInfo != null && getUserInfo.getStatus() != AsyncTask.Status.RUNNING)
		{

		}
	}

	public void zhuoyou_login_register(){
		Intent intent = new Intent(mActivity, RegisterActivity_new.class);
		RegisterActivity_new.setCallBackWebView(mWebView);
		mActivity.startActivity(intent);

	}

	/**
	 * apk下载
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
				String appUrl = jsonObject.has("app_url") ? jsonObject.getString("app_url") : null;
				String imageUrl = jsonObject.getString("icon");
				Intent intent = new Intent("com.zhuoyi.market.extern.download");
				intent.putExtra("packageName", packageName);
				intent.putExtra("appId", appId);
				intent.putExtra("apkName", apkName);
				intent.putExtra("md5", md5);
				intent.putExtra("url", url);
				intent.putExtra("from", mUploadFlag);
				intent.putExtra("verCode", vercode);
				intent.putExtra("imageUrl", imageUrl);
				intent.putExtra("app_url", appUrl);
				intent.putExtra("topicId", mTopicId);
				mActivity.sendBroadcast(intent);

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
				String refId = jsonObject.has("apk_id") ? jsonObject.getString("apk_id") : null;
				String imageUrl = jsonObject.has("icon") ? jsonObject.getString("icon") : null;
				int integral = jsonObject.has("integral") ? jsonObject.getInt("integral") : 0;
				String from = mUploadFlag;
				Intent intent = new Intent("com.zhuoyi.appDetailInfo");
				intent.putExtra("refId", TextUtils.isEmpty(refId) ? -1 : Integer.valueOf(refId));
				intent.putExtra("imgUrl", imageUrl);
				intent.putExtra("from_path", from);
				intent.putExtra("app_integral", integral);
				mActivity.startActivity(intent);
			}
			catch (Exception e)  {
				e.printStackTrace();
			}
		}
		return 0;
	}


	public String getEncryUrl(String url, Context context) {
		String encryUrl = null;
		String openId = get_openid();
		String token = get_token();
		encryUrl = url + "?token=" + token + "&openid=" + openId + "&devinfo="
				+ UserInfo.getDeviceInfo(context);

		return encryUrl;

	}

	public String getEncryUrl(String url) {
		String encryUrl = null;
		HashMap<String, String> publicParams =  GetPublicParams.getPublicParaForPush(mActivity.getApplicationContext(), mActivity.getPackageName(), R.raw.td);
		String IMSI = publicParams.get("imsi");
		String IMEI = publicParams.get("imei");
		String MAC = getMacAddress();
		String versionCode = publicParams.get("versionCode");
		MAC = MAC == null ? "" : MAC;
		IMSI = IMSI == null ? "123456789012345" : IMSI;
		IMEI = IMEI == null ? "" : IMEI;

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("imsi", IMSI);
			jsonObject.put("imei", IMEI);
			jsonObject.put("mac", MAC);
			jsonObject.put("versionCode", versionCode);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		encryUrl = url + "?devinfo="
				+ EncoderAndDecoder.encrypt(jsonObject.toString());

		return encryUrl;

	}

	public String getMacAddress() {
		String mac = userPreferences.getString("mac_address", null);
		if(!TextUtils.isEmpty(mac)){
			return mac;
		}
		WifiManager wifiManager = (WifiManager) mActivity.getSystemService(mActivity.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getMacAddress()))
		{
			return null;
		}
		mac = wifiInfo.getMacAddress();

		Editor editor = userPreferences.edit();
		editor.putString("mac_address", mac);
		editor.commit();
		return mac;
	}

	private static int IDENTIFY_LEN=14;

	private static String formatIdentify(String identify) {
		if(TextUtils.isEmpty(identify)) {
			return identify;
		}
		identify = identify.trim();
		int len = identify.length();
		if(len== IDENTIFY_LEN) {
			return identify;
		}
		if(len>IDENTIFY_LEN) {
			return identify.substring(0, IDENTIFY_LEN);
		}
		for(;len<IDENTIFY_LEN;len++) {
			identify += "0";
		}
		return identify;  
	}


	@Override
	public void zhuoyou_login_logout() {

	}

	@Override
	public void zhuoyou_login_update_userinfo(String userInfo) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_login_shareToWXTimeLine() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_login_shareToWeibo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_login_auth(String utype) {
	}

	@Override
	public void zhuoyou_login_update_title(String title) {
	}

	@Override
	public int zhuoyou_login_download_progress(String info) {
		return 0;
	}

	@Override
	public void zhuoyou_login_share_app(String appName, String shareUrl) {

	}

	@Override
	public boolean zhuoyou_login_app_collect(boolean isCollect,String appInfo) {
		return false;
	}

	@Override
	public boolean zhuoyou_login_get_collect_status(String packageName) {
		return false;
	}


	@Override
	public String zhuoyou_login_get_uuid() {
		return GetPublicParams.getPublicParaForPush(mActivity, mActivity.getPackageName(), R.raw.td).get("uuid");
	}


	@Override
	public void zhuoyou_present(String url) {

	}

	@Override
	public void zhuoyou_present_show(boolean show) {

	}

	@Override
	public void zhuoyou_login_hasGift_receive() {
	}


	@Override
	public boolean zhuoyou_copy_text(String needCopy) {
		if (TextUtils.isEmpty(needCopy))
			return false;
		ClipboardManager clipboardManager = (ClipboardManager) mActivity.getSystemService(Activity.CLIPBOARD_SERVICE);
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

	}

	@Override
	public void zhuoyou_javascript_available() {

	}

	@Override
	public void zhuoyou_web_refresh() {

	}

	@Override
	public int zhuoyou_show_image() {
		return 0;
	}


	/**
	 *	弹出下载窗口
	 */
	public void zhuoyou_download_dialog(){
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				createDialog(0);
			}
		});
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
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					createDialog(1);
				}
			});
		}
	}

	@Override
	public String zhuoyou_get_marketInfo() {
		String marketId = com.zhuoyi.market.utils.MarketUtils.getSharedPreferencesString(mActivity, com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
		if (TextUtils.isEmpty(marketId)) {
			marketId = "null";
		}
		String src = marketId;
		src += "/-1/-1/-1";

		TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(mActivity);
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


	/**
	 * @param type  0:下载安装 1:复制礼包
	 * @return
	 */
	private Dialog createDialog(int type){
		if(mDialog == null) {
			mDialog = new Dialog(mActivity, R.style.MyMarketDialog);
		}
		mDialog.setContentView(R.layout.detail_download_apk_dialog);
		ImageView titleIcon = (ImageView) mDialog.findViewById(R.id.title_image);
		TextView titleText = (TextView) mDialog.findViewById(R.id.title_text);
		TextView showMessage = (TextView) mDialog.findViewById(R.id.showMessage);
		Button sureButton = (Button) mDialog.findViewById(R.id.ok);
		Button cancelButton = (Button) mDialog.findViewById(R.id.cancel);

		switch (type) {	
		case 0:		
			
			AsyncImageCache.from(mActivity.getApplicationContext()).displayImage(true, false, titleIcon, R.drawable.dialog_warn, 0, 0,
					new AsyncImageCache.GeneralImageGenerator("game_install", null), false,true,true,null);
			titleText.setText(mActivity.getResources().getText(R.string.app_detail_need_install));
			titleText.setTextColor(mActivity.getResources().getColor(R.color.detail_app_dialog_install_text));
			sureButton.setText(mActivity.getResources().getText(R.string.app_detail_game_install));
			showMessage.setVisibility(View.GONE);
			cancelButton.setText(mActivity.getResources().getText(R.string.cancel));
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
			AsyncImageCache.from(mActivity.getApplicationContext()).displayImage(true, false, titleIcon, R.drawable.dialog_success, 0, 0,
					new AsyncImageCache.GeneralImageGenerator("game_start", null), false,true,true,null);
			titleText.setText(mActivity.getResources().getText(R.string.app_detail_gift_copy_success));
			titleText.setTextColor(mActivity.getResources().getColor(R.color.detail_app_dialog_copy_text));
			sureButton.setText(mActivity.getResources().getText(R.string.app_detail_game_start));
			showMessage.setText(mShowMessage);
			cancelButton.setText(mActivity.getResources().getText(R.string.return_introduction));
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

	private CallBack mCallBack;
	
	public void setCallBackListener(CallBack callBack){
		mCallBack = callBack;
	}
	
	public interface CallBack{
		public void onJsCall();
		
	}
}
