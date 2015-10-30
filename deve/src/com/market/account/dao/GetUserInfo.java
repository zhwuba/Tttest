/**
 * 
 */
package com.market.account.dao;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.market.account.constant.Constant;
import com.market.account.netutil.HttpOperation;
import com.market.account.utils.MD5Util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;

/**
 * @author sunlei
 * 
 */
public class GetUserInfo extends AsyncTask<String, String, String> {

	private Context mContext;


	public GetUserInfo(Context mContext) {
		this.mContext = mContext;
	}


	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}


	@Override
	protected String doInBackground(String... params) {
		if (isCancelled())
			return null;
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
			// 获取积分
			final Map<String, String> loginParams = new HashMap<String, String>();
			String openid = jsonObject.getString("openid");
			String token = jsonObject.getString("TOKEN");
			String signString = MD5Util.md5(openid + token + Constant.SIGNKEY);

			loginParams.put("openid", openid);
			loginParams.put("token", token);
			loginParams.put("sign", signString);
			String random = getUsername(6);
			loginParams.put("debugcode", random);

			result = HttpOperation.postRequest(Constant.ZHUOYOU_GET_USER_INFO,
					loginParams);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}


	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		getUserInfoListener.OnGetUserInfoCallBack(result);
		// mProgressDialog.dismiss();
	}

	public GetUserInfoListener getUserInfoListener;


	public void setGetUserInfoListener(GetUserInfoListener getUserInfoListener) {
		this.getUserInfoListener = getUserInfoListener;
	}

	public interface GetUserInfoListener {

		public void OnGetUserInfoCallBack(String result);
	}

	// private static String[] randomValues = new String[] { "0", "1", "2", "3",
	// "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h",
	// "i", "j", "k", "l", "m", "n", "u", "t", "s", "o", "x", "v", "p", "q",
	// "r", "w", "y", "z" };
	private static String[] randomValues = new String[] { "0", "1", "2", "3",
			"4", "5", "6", "7", "8", "9" };


	public static String getUsername(int lenght) {
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < lenght; i++) {
			Double number = Math.random() * (randomValues.length - 1);
			str.append(randomValues[number.intValue()]);
		}

		return str.toString();
	}

}
