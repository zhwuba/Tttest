package com.market.account.receiver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.netutil.HttpOperation;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PropertyFileUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class MyReceiver extends BroadcastReceiver {

	private SharedPreferences getRecodePreferences;
	private Context mContext;
	private JSONObject mJsonObject;


	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if (intent.getAction().equals("com.yyapk.login.logout")) {
			PropertyFileUtils.cleanFile(mContext);
		} else if (intent.getAction().equals("zhuoyou.android.account.USER_CHECK_IN")) {
			Bundle bundle = intent.getBundleExtra("flag");
			final boolean fromUserCenter = bundle.getBoolean("fromUserCenter");
			// 每日签到
			new Thread() {

				@Override
				public void run() {

					// 更新用户信息
					int result = -1;
					String userInfo = null;
					JSONObject jsonObject = null;
					AccountManager mAccountManager = AccountManager.get(mContext);
					Account[] accounts = mAccountManager.getAccountsByType(Constant.ACCOUNT_TYPE);
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
						String loginResult = HttpOperation.postRequest(Constant.ZHUOYOU_USER_CHECK_IN, loginParams);
						mJsonObject = new JSONObject(loginResult);
						// result == 0 表示正常， 小于0 接口错误。
						result = mJsonObject.getInt("result");

						if (result == 0) {
							jsonObject.put("recode", mJsonObject.getInt("score"));
							mAccountManager.setUserData(accounts[0], "userInfo", jsonObject.toString());
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					// */
					// send msg to apps
					Intent intent = null;
					if(fromUserCenter)
						intent = new Intent("zhuoyou.android.account.USERCENTER_CHECKIN");
					else
						intent = new Intent("zhuoyou.android.account.SEND_USER_INFO");
					Bundle bundle = new Bundle();
					bundle.putBoolean("fromUserCenter", fromUserCenter);
					bundle.putString("logoCache", PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH);
					if (jsonObject != null) {
						intent.putExtra("userInfo", jsonObject.toString());
					}
					intent.putExtra("userLogin", bundle);
					intent.putExtra("result", result);
					try {
						intent.putExtra("checkIn", mJsonObject.toString());
						intent.putExtra("desc", mJsonObject.has("desc") ? mJsonObject.getString("desc") : null);
					} catch (Exception e) {
						e.printStackTrace();
					}
					mContext.sendBroadcast(intent);

				}

			}.start();

		} else if (intent.getAction().equals(
				"zhuoyou.android.account.CHANGE_USER_INTEGRAL")) {
			// 查询积分
			getRecodePreferences = mContext.getSharedPreferences("getRecode", 0);
			long oldTime = getRecodePreferences.getLong("time", 0);
			if (System.currentTimeMillis() - oldTime > 100) {
				Bundle bundle = intent.getExtras();
				Editor editor = getRecodePreferences.edit();
				if (bundle != null)
					editor.putString("pkgName", bundle.getString("pkgName"));
				editor.putLong("time", System.currentTimeMillis());
				editor.commit();
				// 去服务去查询
				new Thread() {

					@Override
					public void run() {

						// 更新用户信息
						String userInfo = null;
						JSONObject jsonObject = null;
						AccountManager mAccountManager = AccountManager
								.get(mContext);
						Account[] accounts = mAccountManager
								.getAccountsByType(Constant.ACCOUNT_TYPE);
						if (accounts != null && accounts.length >= 1) {
							userInfo = mAccountManager.getUserData(accounts[0],
									"userInfo");
						}
						try {
							if (userInfo == null)
								return;
							jsonObject = new JSONObject(userInfo);
							// 获取积分
							final Map<String, String> loginParams = new HashMap<String, String>();
							String openid = jsonObject.getString("openid");
							String token = jsonObject.getString("TOKEN");
							String signString = MD5Util.md5(openid + token
									+ Constant.SIGNKEY);

							loginParams.put("openid", openid);
							loginParams.put("token", token);
							loginParams.put("sign", signString);
							String loginResult = HttpOperation.postRequest(
									Constant.ZHUOYOU_GET_USER_SCORE,
									loginParams);
							JSONObject mJsonObject = new JSONObject(loginResult);
							// result == 0 表示正常， 小于0 接口错误。
							int result = mJsonObject.getInt("result");

							if (result == 0) {
								jsonObject.put("recode",
										mJsonObject.getInt("score"));
								mAccountManager.setUserData(accounts[0],
										"userInfo", jsonObject.toString());
							}
							/** if logofile not exists,then download userlogo **/
							String logoFile = PropertyFileUtils.getSDPath()
									+ Constant.USERLOGO_PATH;
							if (!new File(logoFile).exists()) {
								AuthenticatorActivity
										.downloadUserLogo(jsonObject
												.getString("logoUrl"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (jsonObject == null)
							return;
						// send msg to market
						Intent intent = new Intent(
								"zhuoyou.android.account.SEND_USER_INFO");
						Bundle bundle = new Bundle();
						bundle.putString("logoCache",
								PropertyFileUtils.getSDPath()
										+ Constant.USERLOGO_PATH);
						intent.putExtra("userInfo", jsonObject.toString());
						intent.putExtra("userLogin", bundle);
						mContext.sendBroadcast(intent);

					}

				}.start();
			}
		}
	}

}
