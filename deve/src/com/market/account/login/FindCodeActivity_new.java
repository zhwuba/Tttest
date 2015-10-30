package com.market.account.login;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.UserInfo;
import com.market.account.netutil.HttpOperation;
import com.market.account.user.User;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PhoneNumUtils;
import com.market.account.utils.PropertyFileUtils;
import com.market.view.LoadingProgressDialog;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

public class FindCodeActivity_new extends AccountAuthenticatorActivity {

	private static final String strRes = "android.provider.Telephony.SMS_RECEIVED";

	private EditText login_name;
	private EditText login_code;
	private EditText login_security_code;
	private Button register_btn;
	private Button register_get_security_code_btn;
	private ImageView mImageView_logo;
	private TimeCount time;
	public static int count = 0;
	private User user = new User();

	// private TelephonyManager telephonyManager;
	public static String REGEX = "";
	public static String TEMP_TOAKEN = "";
	private SmsReceiver smsReceiver;
	/** Keep track of the login task so can cancel it if requested */
	private UserLoginTask mAuthTask = null;
	/** Keep track of the progress dialog so we can dismiss it */
	private ProgressDialog mProgressDialog = null;
	/**
	 * If set we are just checking that the user knows their credentials; this
	 * doesn't cause the user's password or authToken to be changed on the
	 * device.
	 */
	private Boolean mConfirmCredentials = false;
	/** Was the original caller asking for an entirely new account? */
	protected boolean mRequestNewAccount = true;
	private AccountManager mAccountManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_findcode_new);
		mAccountManager = AccountManager.get(this);
		// telephonyManager = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// register the Broadcast Receivers
		smsReceiver = new SmsReceiver();
		registerReceiver(smsReceiver, new IntentFilter(strRes));
		// 3. 60 seconds
		time = new TimeCount(60000, 1000);

		setProgressDialog();

		setViews();

	}


	private void setProgressDialog() {
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
	}


	private void showProgressDialog(String content) {
		mProgressDialog.setMessage(content);
		if (!mProgressDialog.isShowing())
			if (FindCodeActivity_new.this != null
					&& !FindCodeActivity_new.this.isFinishing())
				mProgressDialog.show();
	}


	private void dismissProgressDialog() {
		if (FindCodeActivity_new.this != null
				&& !FindCodeActivity_new.this.isFinishing())
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
	}

	private class SmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context _context, Intent _intent) {
			if (strRes.equals(_intent.getAction())) {
				StringBuilder sb = new StringBuilder();
				Bundle bundle = _intent.getExtras();
				if (bundle != null) {
					Object[] pdus = (Object[]) bundle.get("pdus");
					SmsMessage[] msg = new SmsMessage[pdus.length];
					for (int i = 0; i < pdus.length; i++) {
						msg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					}

					for (SmsMessage curMsg : msg) {
						/*
						 * sb.append(
						 * "You got the message From:?); sb.append(curMsg.getDisplayOriginatingAddress()); sb.append("
						 * 】Content?);
						 */
						sb.append(curMsg.getDisplayMessageBody());
					}
					if (!TextUtils.isEmpty(REGEX)) {
						Pattern pattern = Pattern.compile(REGEX);
						Matcher matcher = pattern.matcher(sb.toString());
						boolean rs = matcher.find();
						if (rs) {
							for (int i = 1; i <= matcher.groupCount(); i++) {
								login_security_code.setText(matcher.group(i));
								login_security_code.setSelection(login_security_code.getText().length());
								time.cancel();
								register_get_security_code_btn.setText(getText(R.string.account_register_security_code_again));
								register_get_security_code_btn.setClickable(true);
							}
						}
					}

				}
			}
		}

	}

	private Handler handlerl = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				dismissProgressDialog();
				Toast.makeText(FindCodeActivity_new.this,
						getResources().getText(R.string.tip_network_wrong),
						Toast.LENGTH_LONG).show();
				break;
			case 1:
				break;
			case 2:
				Toast.makeText(FindCodeActivity_new.this,
						getResources().getText(R.string.server_exception),
						Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}
	};


	private void setViews() {
		login_name = (EditText) findViewById(R.id.login_name);
		login_code = (EditText) findViewById(R.id.login_code);
		login_security_code = (EditText) findViewById(R.id.login_security_code);
		register_btn = (Button) findViewById(R.id.register_btn);
		register_get_security_code_btn = (Button) findViewById(R.id.register_get_security_code_btn);
		mImageView_logo = (ImageView) findViewById(R.id.logo_findcode);

		try {
			Intent intent = getIntent();
			if (intent != null) {
				String userName = intent.getStringExtra(AuthenticatorActivity.PARAM_USERNAME);
				if (!TextUtils.isEmpty(userName)) {
					login_name.setText(userName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		login_name.requestFocus();
		login_code.addTextChangedListener(new TextWatcher() {

			int mStart = 0;
			int mCount = 0;


			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mStart = start;
				mCount = count;
			}


			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}


			@Override
			public void afterTextChanged(Editable s) {
				if (mCount == 0)
					return;
				if (checkNameChese(s.toString().substring(mStart, mStart + mCount)))
					s.delete(mStart, mStart + mCount);
			}
		});

		login_code.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					mImageView_logo.setImageResource(R.drawable.logo_pass);
				else
					mImageView_logo.setImageResource(R.drawable.logo_findcode);
			}
		});
		register_get_security_code_btn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String userName = login_name.getText().toString();

						if (TextUtils.isEmpty(userName)) {
							login_name.requestFocus();
							Toast.makeText(getApplicationContext(), R.string.tip_username_none, Toast.LENGTH_SHORT).show();
							return;
						} else if (!PhoneNumUtils.isPhoneNumberValid(userName)) {
							login_name.requestFocus();
							Toast.makeText(getApplicationContext(), R.string.tip_username_must_phonenum, Toast.LENGTH_SHORT).show();
							return;
						}
						if (GetPublicParams.getAvailableNetWorkType(FindCodeActivity_new.this) == -1) {
							handlerl.sendEmptyMessage(0);
						} else {
							login_security_code.setFocusable(true);
							login_security_code.setFocusableInTouchMode(true);
							login_security_code.requestFocus();
							new GetRegNum(userName).execute();
							time.start();
						}
					}
				});
		login_name.setFocusable(true);
		register_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String nickName = login_name.getText().toString();
				String userPasswd = login_code.getText().toString();
				String securityCode = login_security_code.getText().toString();
				if (TextUtils.isEmpty(nickName)) {
					login_name.requestFocus();
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_username_none), Toast.LENGTH_LONG).show();
					return;
				} else if (!PhoneNumUtils.isPhoneNumberValid(nickName)) {
					login_name.requestFocus();
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_username_must_phonenum), Toast.LENGTH_LONG).show();
					return;
				} else if (TextUtils.isEmpty(userPasswd)) {
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_password_none), Toast.LENGTH_LONG).show();
					login_code.requestFocus();
					return;
				}
				else if (valid(userPasswd)) {
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_password_valid),Toast.LENGTH_LONG).show();
					login_code.requestFocus();
					return;
				}
				if (userPasswd.length() < 6 || userPasswd.length() > 16) {
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_password_not_right_length), Toast.LENGTH_LONG).show();
					login_code.requestFocus();
					return;
				}

				if (TextUtils.isEmpty(securityCode) || securityCode.length() != 6) {
					Toast.makeText(getBaseContext(), R.string.reg_security_code, Toast.LENGTH_SHORT).show();
					return;
				}

				showProgressDialog(getResources().getString(R.string.tip_reset_password_now));

				// network
				if (GetPublicParams.getAvailableNetWorkType(FindCodeActivity_new.this) == -1) {
					handlerl.sendEmptyMessage(0);
				} else {
					new RegisterUser(nickName, userPasswd, securityCode).execute();
				}

			}
		});
	}

	public class GetRegNum extends AsyncTask<Object, Object, String> {

		String uid = null;


		public GetRegNum(String uid) {
			this.uid = uid;
		}


		@Override
		protected String doInBackground(Object... params) {
			String result = null;
			Map<String, String> rawParams = new HashMap<String, String>();
			String signString = MD5Util.md5(uid + "resetpasswd" + Constant.SIGNKEY);
			rawParams.put("uid", uid);
			rawParams.put("codetype", "resetpasswd");
			signString = MD5Util.md5(uid + "resetpasswd" + Constant.SIGNKEY);
			rawParams.put("sign", signString);
			try {
				result = HttpOperation.postRequest(Constant.ZHUOYOUREGISTER, rawParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}


		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			JSONObject jsonObject = null;
			if (!TextUtils.isEmpty(result)) {
				try {
					jsonObject = new JSONObject(result);
					int re = jsonObject.getInt("result");
					if (re == 0) {
						REGEX = (String) jsonObject.get("smspattern");
						TEMP_TOAKEN = jsonObject.getString("token");
					} else if (re < 0) {
						time.onFinish();
						time.cancel();
					}
					String desc = jsonObject.getString("desc");
					if (!TextUtils.isEmpty(desc)) {
						Toast.makeText(getBaseContext(), desc.trim(), Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				time.onFinish();
				time.cancel();
				Toast.makeText(getBaseContext(), R.string.server_exception, Toast.LENGTH_SHORT).show();
			}
		}
	}

	// 2.register user
	public class RegisterUser extends AsyncTask<Object, Object, String> {

		String nickName = "";
		String userPasswd = "";
		String securityCode = "";


		public RegisterUser(String nickName, String userPasswd, String securityCode) {
			this.nickName = nickName;
			this.userPasswd = userPasswd;
			this.securityCode = securityCode;
		}


		@Override
		protected String doInBackground(Object... params) {
			// token passwd randcode
			String result = null;
			if (TextUtils.isEmpty(TEMP_TOAKEN)) {
				return null;
			}
			Map<String, String> rawParams = new HashMap<String, String>();
			String signString = MD5Util.md5(TEMP_TOAKEN + userPasswd + securityCode + Constant.SIGNKEY);

			rawParams.put("token", TEMP_TOAKEN);
			rawParams.put("passwd", userPasswd);
			rawParams.put("randcode", securityCode);
			rawParams.put("sign", signString);
			try {
				result = HttpOperation.postRequest(Constant.ZHUOYOU_USER_RESET_PASSWD, rawParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}


		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dismissProgressDialog();
			if (!TextUtils.isEmpty(result)) {
				try {
					JSONObject jsonObject = new JSONObject(result);
					int mResult = jsonObject.getInt("result");
					if (mResult == 0) {
						showProgress();
						Toast.makeText(FindCodeActivity_new.this, R.string.tip_findcode_success, Toast.LENGTH_LONG).show();
						mAuthTask = new UserLoginTask(nickName, userPasswd);
						mAuthTask.execute();
					} else {
						String desc = jsonObject.has("desc") ? jsonObject.getString("desc") : getResources().getString(R.string.tip_reset_password_failed);
						Toast.makeText(getApplicationContext(), desc, Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				if (TextUtils.isEmpty(TEMP_TOAKEN)) {
					Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_code_wrong), Toast.LENGTH_SHORT).show();
				} else {
					handlerl.sendEmptyMessage(2);
				}
			}
		}
	}


	/**
	 * Shows the progress UI for a lengthy operation.
	 */
	private void showProgress() {
		showDialog(0);
	}


	private void hideProgress() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}


	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final LoadingProgressDialog dialog = new LoadingProgressDialog(this);
		dialog.setMessage(getString(R.string.authenticate_login));
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			public void onCancel(DialogInterface dialog) {
				if (mAuthTask != null) {
					mAuthTask.cancel(true);
				}
			}
		});
		mProgressDialog = dialog;
		return dialog;
	}

	public class UserLoginTask extends AsyncTask<Void, Void, String> {

		private String nickName;
		private String password;
		private String loginResult;


		public UserLoginTask(String nickName, String password) {
			this.nickName = nickName;
			this.password = password;
		}


		@Override
		protected String doInBackground(Void... params) {
			final Map<String, String> loginParams = new HashMap<String, String>();
			String passwdMD5 = MD5Util.md5(password);
			String devinfo = UserInfo.getDeviceInfo(getApplicationContext());
			loginParams.put("uid", nickName);
			loginParams.put("passwd", passwdMD5);
			loginParams.put("utype", "zhuoyou");
			loginParams.put("devinfo", devinfo);

			loginParams.put( "sign", MD5Util.md5(nickName + passwdMD5 + "zhuoyou" + devinfo + Constant.SIGNKEY));
			loginResult = HttpOperation.postRequest(Constant.LOGIN, loginParams);
			try {
				JSONObject jsonObject = new JSONObject(loginResult);
				// result == 0 表示正常?小于0 接口错误?
				int result = jsonObject.getInt("result");
				if (result == 0) {
					user.setNickname(nickName);
					user.setUsername(jsonObject.has("username") ? jsonObject.getString("username") : null);
					user.setPassword(passwdMD5);
					user.setTOKEN(jsonObject.getString("token"));
					user.setUID(jsonObject.has("username") ? jsonObject.getString("username") : nickName);
					user.setOpenid(jsonObject.getString("openid"));
					user.setExpires_in(jsonObject.getString("expire"));
					user.setRecode(jsonObject.getInt("score"));
					// user.setLevel(jsonObject.getInt("level"));
					user.setLogoUrl(jsonObject.has("avatarurl") ? jsonObject.getString("avatarurl") : jsonObject.has("avatar") ? jsonObject.getString("avatar") : null);
					// user.setGender(jsonObject.getString("gender"));
					// user.setAge(jsonObject.getInt("age"));
					if (TextUtils.isEmpty(user.getLogoUrl())) {
						AuthenticatorActivity.deleteUserLogo();
					} else {
						AuthenticatorActivity.downloadUserLogo(user.getLogoUrl());
					}
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
			onAuthenticationResult(user);
		}


		@Override
		protected void onCancelled() {
			onAuthenticationCancel();
		}
	}


	/**
	 * Called when response is received from the server for confirm credentials
	 * request. See onAuthenticationResult(). Sets the
	 * AccountAuthenticatorResult which is sent back to the caller.
	 * 
	 * @param result
	 *            the confirmCredentials result.
	 */
	private void finishConfirmCredentials(boolean result) {
		final Account account = new Account(user.getNickname(), Constant.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, user.getPassword());
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
//		UserInfo.setHasLogin(true); // to refresh webView
		  com.market.account.authenticator.AccountManager.getInstance().onAccountLogin();
		if (AuthenticatorActivity.mInstance != null) {
			AuthenticatorActivity.mInstance.finish();
		}
	}


	/**
	 * Called when response is received from the server for authentication
	 * request. See onAuthenticationResult(). Sets the
	 * AccountAuthenticatorResult which is sent back to the caller. We store the
	 * authToken that's returned from the server as the 'password' for this
	 * account - so we're never storing the user's actual password locally.
	 * 
	 * @param result
	 *            the confirmCredentials result.
	 */
	private void finishLogin(String authToken) {

		try {
			final Account account = new Account(user.getNickname(), Constant.ACCOUNT_TYPE);
			if (mRequestNewAccount) {
				mAccountManager.addAccountExplicitly(account, authToken, null);
				// Set contacts sync for this account.
				// ContentResolver.setSyncAutomatically(account,
				// ContactsContract.AUTHORITY, true);
			} else {
				mAccountManager.setPassword(account, authToken);
			}
			// 保存返回信息 write to file
			String jsonStringer = null;

			jsonStringer = new JSONStringer()
					.object()
					.key("nickname").value(TextUtils.isEmpty(user.getNickname()) ? login_name.getText().toString().trim() : user.getNickname())
					.key("username").value(TextUtils.isEmpty(user.getUsername()) ? null : user.getUsername())
					.key("password").value(TextUtils.isEmpty(user.getPassword()) ? MD5Util.md5(login_security_code.getText().toString().trim()) : user.getPassword())
					.key("UID").value(user.getUID())
					.key("openid").value(user.getOpenid())
					.key("OpenKey").value(user.getOpenKey())
					.key("TOKEN").value(user.getTOKEN())
					.key("expires_in").value(user.getExpires_in())
					.key("recode").value(TextUtils.isEmpty(user.getRecode() + "") ? 0 : user.getRecode())
					.key("logoUrl").value(TextUtils.isEmpty(user.getLogoUrl()) ? "" : user.getLogoUrl()).endObject().toString();
			mAccountManager.setUserData(account, "userInfo", jsonStringer);
			// send msg to third app
			Intent intent = new Intent("zhuoyou.android.account.SEND_USER_INFO");
			Bundle bundle = new Bundle();
			bundle.putString("logoCache", PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH);
			intent.putExtra("userInfo", jsonStringer);
			intent.putExtra("userLogin", bundle);
			sendBroadcast(intent);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getNickname());
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constant.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
		 com.market.account.authenticator.AccountManager.getInstance().onAccountLogin();
		if (AuthenticatorActivity.mInstance != null) {
			AuthenticatorActivity.mInstance.finish();
		}
	}


	/**
	 * Called when the authentication process completes (see attemptLogin()).
	 * 
	 * @param authToken
	 *            the authentication token returned by the server, or NULL if
	 *            authentication failed.
	 */
	public void onAuthenticationResult(User user) {
		String authToken = null;
		if (null != user) {
			authToken = user.getTOKEN();
		}
		boolean success = ((authToken != null) && (authToken.length() > 0));
		// Our task is complete, so clear it out
		mAuthTask = null;
		dismissProgressDialog();

		if (success) {
			if (!mConfirmCredentials) {
				finishLogin(authToken);
			} else {
				finishConfirmCredentials(success);
			}

		} else {
			if (mRequestNewAccount) {
				String desc = user.getDesc();
				if (TextUtils.isEmpty(desc)) {
					desc = getResources().getString(R.string.login_server_exception);
				}
				Toast.makeText(getApplicationContext(), desc.trim(), Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}


	public void onAuthenticationCancel() {

		// Our task is complete, so clear it out
		mAuthTask = null;
		dismissProgressDialog();
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}


	@Override
	protected void onDestroy() {
		if (smsReceiver != null) {
			unregisterReceiver(smsReceiver);
		}
		super.onDestroy();
	}


	private boolean valid(String str) {
		boolean hasSymble = !str.matches("^[\\da-zA-Z]*$");
		return hasSymble;
	}

	class TimeCount extends CountDownTimer {

		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时?和计时的时间间隔
		}


		@Override
		public void onFinish() {
			// 计时完毕时
			register_get_security_code_btn.setText(getText(R.string.account_register_security_code_again));
			register_get_security_code_btn.setClickable(true);
		}


		@Override
		public void onTick(long millisUntilFinished) {
			// 计时过程显示
			register_get_security_code_btn.setClickable(false);
			register_get_security_code_btn.setText(millisUntilFinished / 1000 + getResources().getString(R.string.second));
		}

	}


	public boolean checkNameChese(String name) {
		boolean res = false;
		char[] cTemp = name.toCharArray();
		for (int i = 0; i < name.length(); i++) {
			if (isChinese(cTemp[i])) {
				res = true;
				break;
			}
		}
		return res;
	}


	public boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
}
