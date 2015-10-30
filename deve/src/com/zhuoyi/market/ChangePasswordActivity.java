package com.zhuoyi.market;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.constant.Constant;
import com.market.account.netutil.HttpOperation;
import com.market.account.utils.MD5Util;
import com.market.view.LoadingProgressDialog;
import com.zhuoyi.market.utils.MarketUtils;

public class ChangePasswordActivity extends Activity implements OnClickListener {

	private TextView mTextView_logged_name;
	private Button mTextView_verification_code;
	private EditText mEditText_verification_code;
	private EditText mEditText_new_password;
	private Button mButton_save;
	private ImageView mImageView_logo;
	
	private String phone;
	private LoadingProgressDialog mProgressDialog;
	private ResetPass mResetPass;
	
	private TimeCount time;
	private SmsReceiver smsReceiver;
	public  String REGEX = "";
    public  String token = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		phone = intent.getStringExtra("phone");
		setContentView(R.layout.personalinfo_change_password);
		
		mProgressDialog = new LoadingProgressDialog(this);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setMessage(getResources().getString(R.string.personalInfo_waiting_for_result));
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.personalInfo_cancel), Toast.LENGTH_SHORT).show();
				if(mResetPass != null && mResetPass.getStatus() == AsyncTask.Status.RUNNING) {
					mResetPass.cancel(true);
//					mResetPass = null;
				}
			}
		});
		
		//params1:从开始调用start()到倒计时完成并onFinish()方法被调用的毫秒数   
		//params2:接收onTick(long)回调的间隔时间
		time = new TimeCount(60 * 1000, 1000);
		smsReceiver = new SmsReceiver();
		registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		findView();
		initView();
	}

	
	private void findView() {
		mTextView_logged_name = (TextView) findViewById(R.id.logged_in_name);
		mTextView_verification_code = (Button) findViewById(R.id.verification_code_button);
		mEditText_verification_code = (EditText) findViewById(R.id.editText_verification_code);
		mEditText_new_password = (EditText) findViewById(R.id.editText_new_password);
		mButton_save = (Button) findViewById(R.id.submit);
		mImageView_logo = (ImageView) findViewById(R.id.logo_changepass);
		
	}

	
	private void initView() {
		mTextView_logged_name.setText(getString(R.string.personalInfo_password_already_logged_in, phone));
		mEditText_verification_code.setText("");
		mEditText_new_password.setText("");
		mTextView_verification_code.setOnClickListener(this);
		mButton_save.setOnClickListener(this);
		
		mEditText_new_password.addTextChangedListener(new TextWatcher() {
			
			int mStart = 0;
			int mCount = 0;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mStart = start;
				mCount = count;
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(mCount == 0)
					return;
				if(checkNameChese(s.toString().substring(mStart, mStart + mCount)))
					s.delete(mStart, mStart + mCount);
			}
		});
		
		mEditText_new_password.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					mImageView_logo.setImageResource(R.drawable.logo_pass);
				else
					mImageView_logo.setImageResource(R.drawable.logo_login);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.verification_code_button://获取验证码
			mEditText_verification_code.setFocusable(true);
			mEditText_verification_code.setFocusableInTouchMode(true);
			mEditText_verification_code.requestFocus();
			new GetRegNum(phone).execute();
			time.start();
			break;
		case R.id.submit://确认并保存
			submit();
			break;
		default:
			break;
		}
	}

	
	private void submit() {
		String verification_code = mEditText_verification_code.getText().toString();
		String newPassword = mEditText_new_password.getText().toString();
		if(TextUtils.isEmpty(verification_code) || verification_code.length() != 6) {
			Toast.makeText(getBaseContext(), R.string.reg_security_code, Toast.LENGTH_SHORT).show();
        	return;
		}
		if(newPassword.contains(" ") || newPassword.length() < 6 || newPassword.length() > 16) {
			Toast.makeText(getBaseContext(), R.string.personalInfo_password_newPass_error, Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog.show();
		mResetPass = new ResetPass();
		if(mResetPass != null && mResetPass.getStatus() != AsyncTask.Status.RUNNING) {
			mResetPass.execute(newPassword, verification_code);
		}
	}

	
	/**
	 * 倒计时
	 */
	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			mTextView_verification_code.setText(getResources().getString(R.string.account_register_security_code_again));
			mTextView_verification_code.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mTextView_verification_code.setClickable(false);
			mTextView_verification_code.setText(millisUntilFinished / 1000 + getResources().getString(R.string.second));
		}

	}

	
	/**
	 * 获取验证码
	 */
	public class GetRegNum extends AsyncTask<Object, Object, String> {
		String uid = null;

		public GetRegNum(String uid) {
			this.uid = uid;
		}

		@Override
		protected String doInBackground(Object... params) {
			String result = null;
			Map<String, String> rawParams = new HashMap<String, String>();
			String signString = MD5Util.md5(uid + "resetpasswd" + Constant.SIGNKEY);;
			rawParams.put("uid", uid);
			rawParams.put("codetype", "resetpasswd");
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
						token = jsonObject.getString("token");
						REGEX = (String) jsonObject.get("smspattern");
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
	
	
	public class ResetPass extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			if(isCancelled())
				return null;
			if(TextUtils.isEmpty(token))
				return null;
			String sign = MD5Util.md5(token + params[0] + params[1] + Constant.SIGNKEY);
			Map<String, String> mParams = new HashMap<String, String>();
			mParams.put("token", token);
			mParams.put("passwd", params[0]);
			mParams.put("randcode", params[1]);
			mParams.put("sign", sign);
			String result = HttpOperation.postRequest(Constant.ZHUOYOU_USER_RESET_PASSWD, mParams);
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			try {
				if(result != null) {
					JSONObject object = new JSONObject(result);
					if(object.getInt("result") == 0) {
						finish();
						Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.personalInfo_success), Toast.LENGTH_SHORT).show();
					} else{
						String desc = object.getString("desc");
						if(!TextUtils.isEmpty(desc))
							Toast.makeText(getBaseContext(), desc.trim(), Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(getApplicationContext(), getResources().getString(R.string.personalInfo_fail), Toast.LENGTH_SHORT).show();
					}
				} else {
					if(TextUtils.isEmpty(token))
	            		Toast.makeText(getApplicationContext(), getResources().getText(R.string.tip_code_wrong), Toast.LENGTH_SHORT).show();
	            	else
	            		Toast.makeText(getApplicationContext(), R.string.server_exception, Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public class SmsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
				StringBuilder sb = new StringBuilder();
                Bundle bundle = intent.getExtras();
				if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] msg = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++)
                        msg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    for (SmsMessage curMsg : msg)
                        sb.append(curMsg.getDisplayMessageBody());
                    if (!TextUtils.isEmpty(REGEX)) {
                        Pattern pattern = Pattern.compile(REGEX);
                        Matcher matcher = pattern.matcher(sb.toString());
                        boolean rs = matcher.find();
						if (rs) {
                            for (int i = 1; i <= matcher.groupCount(); i++) {
                                mEditText_verification_code.setText(matcher.group(i));
                                mEditText_verification_code.setSelection(mEditText_verification_code.getText().length());
                                time.cancel();
                                mTextView_verification_code.setText(getText(R.string.account_register_security_code_again));
                                mTextView_verification_code.setClickable(true);
                            }
                        }
                    }

                }
			}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (smsReceiver != null)
            unregisterReceiver(smsReceiver);
	}
}
