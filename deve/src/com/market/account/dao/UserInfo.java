/**
 * 
 */
package com.market.account.dao;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.account.constant.Constant;
import com.market.account.utils.EncoderAndDecoder;
import com.market.account.utils.GetPublicParams;
import com.zhuoyi.market.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * 判断用户是否以及获取用户信息
 * 
 * @author sunlei
 * 
 */
public class UserInfo {

	/**
	 * 此方法时为了兼容，过去的存在的业务接口
	 * 
	 * @param context
	 *            应用上下文。
	 * @return boolean true:已经登录。false：没有登录
	 */
	public static SharedPreferences userPreferences;
	private static boolean mHasLogin;
	
	public static boolean getUser(Context context) {
		boolean flag = false;
		Account[] accounts = AccountManager.get(context).getAccountsByType(
				Constant.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1) {
			flag = true;
		}
		return flag;
	}


	public static boolean getUserIsLogin(Context context) {
		boolean flag = false;
		Account[] accounts = AccountManager.get(context).getAccountsByType(
				Constant.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1) {
			flag = true;
		}
		return flag;
	}


	/**
	 * 此方法是为了满足第三方应用的业务需要,去获取用户信息.注意头像已经做了缓存,并非每次都要获取.请查看账户和第三方应用的交互流程
	 * 
	 * @param context
	 *            应用上下文。
	 * @return 
	 *         JSONObject="{"username":"用户名","password":"密码","UID":"用户Id","TOKEN":"
	 *         访问令牌","expires_in":"过期时间","recode":"积分","logoUrl":"头像的URL"}"
	 */
	public static JSONObject getUserInfo(Context context) {
	    if (context == null) return null;
		JSONObject jsonObject = null;
		AccountManager mAccountManager = AccountManager.get(context);
		Account[] accounts = mAccountManager
				.getAccountsByType(Constant.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1) {
			String jsonString = mAccountManager.getUserData(accounts[0],
					"userInfo");
			if (!TextUtils.isEmpty(jsonString)) {
				try {
					jsonObject = new JSONObject(jsonString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return jsonObject;
	}


	// get_openid()
	public static String get_openid(Context context) {
		String openid = "";
		try {
			JSONObject userInfoJsonObject = com.market.account.dao.UserInfo
					.getUserInfo(context);
			if (userInfoJsonObject != null) {
				openid = userInfoJsonObject.getString("openid");
			}
			return openid;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
		    e.printStackTrace();
		}
		return openid;
	}


	public static String get_token(Context context) {
		String token = "";
		try {
			JSONObject userInfoJsonObject = UserInfo.getUserInfo(context);

			if (userInfoJsonObject != null) {
				token = userInfoJsonObject.getString("TOKEN");
			}
			return token;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return token;
	}
	
	

	public static String getEncryUrl(Context context, String url) {
		String encryUrl = null;
		String openId = get_openid(context);
		String token = get_token(context);
		if (!TextUtils.isEmpty(url) && url.contains("?")) {
			encryUrl = url + "&openid=" + openId + "&token=" + token
					+ "&devinfo=" + getDeviceInfo(context);
		} else {
			encryUrl = url + "?openid=" + openId + "&token=" + token
					+ "&devinfo=" + getDeviceInfo(context);
		}
		return encryUrl;
	}
	
	
	public static String getDeviceInfo(Context context) {
		String info = "{}";
		if (context == null)
			return info;
		HashMap<String, String> publicParams = GetPublicParams
				.getPublicParaForPush(context, context.getPackageName(),
						R.raw.td);
		String IMSI = publicParams.get("imsi");
		String IMEI = publicParams.get("imei");
		String MAC = getMacAddress(context);
		String versionCode = publicParams.get("versionCode");
		String channelId = Constant.CHANNEL_ID;
		MAC = MAC == null ? "" : MAC;
		IMSI = IMSI == null ? "123456789012345" : IMSI;
		IMEI = IMEI == null ? "" : IMEI;

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("imsi", IMSI);
			jsonObject.put("imei", IMEI);
			jsonObject.put("mac", MAC);
			jsonObject.put("versionCode", versionCode);
			jsonObject.put("channelId", channelId);
			info = EncoderAndDecoder.encrypt(jsonObject.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return info;
	}
	
	
	public static String getMacAddress(Context context) {
		if (userPreferences == null) {
			userPreferences = context.getSharedPreferences(
					"userCenterPreferences", Context.MODE_PRIVATE);
		}
		String mac = userPreferences.getString("mac_address", null);
		if (!TextUtils.isEmpty(mac)) {
			return mac;
		}
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null || TextUtils.isEmpty(wifiInfo.getMacAddress())) {
			return null;
		}
		mac = wifiInfo.getMacAddress();

		Editor editor = userPreferences.edit();
		editor.putString("mac_address", mac);
		editor.commit();
		return mac;
	}

	private static int IDENTIFY_LEN = 14;


	private static String formatIdentify(String identify) {
		if (TextUtils.isEmpty(identify)) {
			return identify;
		}
		identify = identify.trim();
		int len = identify.length();
		if (len == IDENTIFY_LEN) {
			return identify;
		}
		if (len > IDENTIFY_LEN) {
			return identify.substring(0, IDENTIFY_LEN);
		}
		for (; len < IDENTIFY_LEN; len++) {
			identify += "0";
		}
		return identify;
	}
	
}
