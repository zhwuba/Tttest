/**
 * 
 */
package com.market.account.dao;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.market.account.constant.Constant;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.HttpOperation;
import com.market.account.utils.MD5Util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

/**
 * Represents an asynchronous task used to authenticate a user against the
 * SampleSync Service
 */
public class UserLogin extends AsyncTask<Void, Void, String> {

	private Context mContext;

	public UserLogin(Context mContext) {
		this.mContext = mContext;
	}


	@Override
	protected String doInBackground(Void... params) {

		String result = null;
		// 用户信息
		String userInfo = null;
		JSONObject jsonObject = null;
		AccountManager mAccountManager = AccountManager.get(mContext);
		Account[] accounts = mAccountManager
				.getAccountsByType(Constant.ACCOUNT_TYPE);
		if (accounts != null && accounts.length >= 1) {
			userInfo = mAccountManager.getUserData(accounts[0], "userInfo");
		}
		try {
			jsonObject = new JSONObject(userInfo);
			final Map<String, String> loginParams = new HashMap<String, String>();
			if (jsonObject.has("regtype")
					&& (jsonObject.getString("regtype").equals("openqq") || jsonObject
							.getString("regtype").equals("openweibo"))) {
				String devinfo = UserInfo.getDeviceInfo(mContext);
				String data = "{}";
				String uid = jsonObject.has("UID") ? jsonObject
						.getString("UID") : null;
				String token = jsonObject.has("TOKEN") ? jsonObject
						.getString("TOKEN") : null;
				String utype = jsonObject.has("regtype") ? jsonObject
						.getString("regtype") : null;
				String signString = MD5Util.md5(uid + token + utype + data
						+ devinfo + Constant.SIGNKEY);

				loginParams.put("uid", uid);
				loginParams.put("passwd", token);
				loginParams.put("utype", utype);
				loginParams.put("data", data);
				loginParams.put("sign", signString);
				// loginParams.put("openid", user.getUID());
				loginParams.put("devinfo", devinfo);
				result = HttpOperation.postRequest(Constant.AUTH, loginParams);
			} else {
				String uid = jsonObject.has("UID") ? jsonObject
						.getString("UID") : null;
				String passwdMD5 = jsonObject.getString("password");
				String devinfo = UserInfo.getDeviceInfo(mContext);
				loginParams.put("uid", uid);
				loginParams.put("passwd", passwdMD5);
				loginParams.put("utype", "zhuoyou");
				loginParams.put("devinfo", devinfo);

				loginParams.put(
						"sign",
						MD5Util.md5(uid + passwdMD5 + "zhuoyou" + devinfo
								+ Constant.SIGNKEY));
				result = HttpOperation.postRequest(Constant.LOGIN, loginParams);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}


	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		userLoginListener.OnUserLoginCallBack(result);
	}

	public UserLoginListener userLoginListener;


	public void setUserLoginListener(UserLoginListener userLoginListener) {
		this.userLoginListener = userLoginListener;
	}

	public interface UserLoginListener {

		public void OnUserLoginCallBack(String result);
	}

}