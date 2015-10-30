package com.zhuoyi.market.appdetail;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.JavaScriptInterface;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;

public class AppDetailJsOperation implements JavaScriptInterface {
	
	private Context mContext;
	private String mUploadFlag;
	private int mTopicId;
	
	public AppDetailJsOperation(Context context, String uploadFlag, int topicId) {
		mContext = context;
		mUploadFlag = uploadFlag;
		mTopicId = topicId;
	}
	
	@Override
	public void zhuoyou_login_logout() {

	}

	@Override
	public void zhuoyou_login_update_userinfo(String userInfo) {

	}

	@Override
	public void zhuoyou_login_shareToWXTimeLine() {

	}

	@Override
	public void zhuoyou_login_shareToWeibo() {

	}

	@Override
	public void zhuoyou_login_shareToWXTimeLine(String desc, String share_url) {

	}

	@Override
	public void zhuoyou_login_shareToWeibo(String desc, String share_url) {

	}

	@Override
	public void zhuoyou_login_goto(String url, String titleName, String callback) {

	}

	@Override
	public void zhuoyou_login_goto(String url, String titleName) {

	}

	@Override
	public int zhuoyou_login_download_state(String pkgName, String verCode) {
		return 0;
	}

	@Override
	public String get_openid() {

		String openid = "";
		try {
			JSONObject userInfoJsonObject = com.market.account.dao.UserInfo
					.getUserInfo(mContext);
			if (userInfoJsonObject != null) {
				openid = userInfoJsonObject.getString("openid");
			}
			return openid;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return openid;
	
	}

	@Override
	public String get_token() {

		String token = "";
		try {
			JSONObject userInfoJsonObject = com.market.account.dao.UserInfo.getUserInfo(mContext);
			if (userInfoJsonObject != null) {
				token = userInfoJsonObject.getString("TOKEN");
			}
			return token;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return token;
	
	}

	@Override
	public String generate_sign(String params) {
		return null;
	}

	@Override
	public void zhuoyou_login_authenticator() {
		Intent intent = new Intent(mContext,AuthenticatorActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}

	@Override
	public void zhuoyou_login_start_app(String pkgName) {

	}

	@Override
	public void zhuoyou_login_close() {

	}

	@Override
	public int zhuoyou_login_getApkVersionCode(String packageName) {
		return 0;
	}

	@Override
	public void zhuoyou_login_update_userinfo() {

	}

	@Override
	public void zhuoyou_login_auth(String utype) {

	}

	@Override
	public int zhuoyou_login_download(String info) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int zhuoyou_login_apk_detail(String info, int score) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void zhuoyou_login_update_title(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public int zhuoyou_login_download_progress(String info) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void zhuoyou_login_share_app(String appName, String shareUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean zhuoyou_login_app_collect(boolean isCollect, String appInfo) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean zhuoyou_login_get_collect_status(String packageName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String zhuoyou_login_get_uuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void zhuoyou_login_hasGift_receive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_present(String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_present_show(boolean show) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean zhuoyou_copy_text(String needCopy) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void zhuoyou_bindMobile_result(boolean result, String phone) {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_javascript_available() {
		// TODO Auto-generated method stub

	}

	@Override
	public void zhuoyou_web_refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public int zhuoyou_show_image() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String zhuoyou_get_marketInfo() {
		String marketId = com.zhuoyi.market.utils.MarketUtils.getSharedPreferencesString(mContext, com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
        if (TextUtils.isEmpty(marketId)) {
        	marketId = "null";
        }
        String src = marketId;
        src += "/-1/-1/-1";
        
		TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(mContext);
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

}
