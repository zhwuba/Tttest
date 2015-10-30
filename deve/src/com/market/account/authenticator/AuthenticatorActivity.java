/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.market.account.authenticator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.constant.Constant;
import com.market.account.login.FindCodeActivity_new;
import com.market.account.login.RegisterActivity_new;
import com.market.account.netutil.HttpOperation;
import com.market.account.tencent.AppConstants;
import com.market.account.tencent.Util;
import com.market.account.user.User;
import com.market.account.utils.BitMapUtils;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.MD5Util;
import com.market.account.utils.PhoneNumUtils;
import com.market.account.utils.PropertyFileUtils;
import com.market.account.weibosdk.AccessTokenKeeper;
import com.market.account.weibosdk.RequestListener;
import com.market.account.weibosdk.SsoHandler;
import com.market.view.LoadingProgressDialog;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * Activity which displays login screen to the user.
 * 
 * @author sunlei
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    /** The Intent flag to confirm credentials. */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /** The Intent extra to store password. */
    public static final String PARAM_PASSWORD = "password";

    /** The Intent extra to store username. */
    public static final String PARAM_USERNAME = "username";

    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    public static AuthenticatorActivity mInstance;
    private AccountManager mAccountManager;

    /** Keep track of the login task so can cancel it if requested */
    private UserLoginTask mAuthTask = null;

    /** Keep track of the progress dialog so we can dismiss it */
    private ProgressDialog mProgressDialog = null;

    /**
     * If set we are just checking that the user knows their credentials; this doesn't cause the user's password or authToken to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

    private String mPassword;

    private EditText mPasswordEdit;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;
    private EditText mUsernameEdit;

    /** merger from old login app */
    private User user = new User();
    private TextView register_btn;
    private TextView forget_code_btn;
    private Button login_btn_qq;
    private Button login_btn_sina;
    private ImageView mImageView_logo;
    
    private View viewAnimationSpace;
    private View actionBar;
    private View animationContainer;
    private boolean isAnimationStart = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()); 
    /*
     * QQ login
     */
    /** tencent 登陆 */
    public Tencent mTencent;
    /** mAppid 腾讯申请的app的id */
    private UserInfo mInfo;

    /** 登陆认证对应的listener */
    private AuthListener mLoginListener = new AuthListener();

    // 创建授权认证信息
    private AuthInfo mAuthInfo = null;
    private SsoHandler mSsoHandler = null;

    /** 微博授权认证回调 */
    private WeiboAuthListener mAuthListener;
    
    private OtherUserLoginTask mOtherUserLoginTask;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle)
    {
    	
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(icicle);
        mInstance = this;

        mAccountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = mAccountManager.getAccountsByType(Constant.ACCOUNT_TYPE);
        int result = getIntent().getIntExtra("result", -100);
		if (result != 2) {
			if (accounts != null && accounts.length >= 1) {
            	Toast.makeText(getApplicationContext(), getString(R.string.has_been_added_account), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        
		try {
        	if(mTencent == null) {
        		mTencent = Tencent.createInstance(AppConstants.APP_ID,  getApplicationContext());
        	}
		} catch (Exception e) {
            e.printStackTrace();
        }

        // 创建授权认证信息
        mAuthInfo = new AuthInfo(getApplicationContext(), com.market.account.weibosdk.Constants.APP_KEY, com.market.account.weibosdk.Constants.REDIRECT_URL, com.market.account.weibosdk.Constants.SCOPE);
        mAuthListener = mLoginListener;

        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mRequestNewAccount = mUsername == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS, false);
        setContentView(R.layout.layout_login);
        
        setViews();
        if (!TextUtils.isEmpty(mUsername))
            mUsernameEdit.setText(mUsername);
        initFullStyle();
    }
    

    /**
     * init views .
     */
    private void setViews() {
        register_btn = (TextView) findViewById(R.id.register_btn);
        forget_code_btn = (TextView) findViewById(R.id.forget_code_btn);
        login_btn_qq = (Button) findViewById(R.id.login_btn_qq);
        login_btn_sina = (Button) findViewById(R.id.login_btn_sina);
        mUsernameEdit = (EditText) findViewById(R.id.login_name);
        mPasswordEdit = (EditText) findViewById(R.id.login_code);
        mImageView_logo = (ImageView) findViewById(R.id.logo_login);
        viewAnimationSpace = findViewById(R.id.viewAnimationSpace);
        animationContainer = findViewById(R.id.rlLoginContainer);
        actionBar = findViewById(R.id.title);
        actionBar.setVisibility(View.INVISIBLE);
        
        register_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
            	Intent intent = new Intent(getApplicationContext(), RegisterActivity_new.class);
            	intent.putExtra(PARAM_USERNAME, mUsernameEdit.getText().toString());
            	
                startActivity(intent);
            }
        });

        forget_code_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), FindCodeActivity_new.class);
            	intent.putExtra(PARAM_USERNAME, mUsernameEdit.getText().toString());
                startActivity(intent);
            }
        });

        login_btn_qq.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onClickLogin();
            }
        });
        login_btn_sina.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	/**	web login	**/
            	if(mAuthInfo != null){
            	    mSsoHandler = new SsoHandler(AuthenticatorActivity.this, mAuthInfo);
                    mSsoHandler.authorizeWeb(mAuthListener);
            	}
            }
        });
        
        mUsernameEdit.setOnClickListener(onFocusClickListener);
        mPasswordEdit.setOnClickListener(onFocusClickListener);
        mPasswordEdit.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if(hasFocus)
					mImageView_logo.setImageResource(R.drawable.logo_pass);
				else
					mImageView_logo.setImageResource(R.drawable.logo_login);
			}
		});
    }
	
	private OnClickListener onFocusClickListener = new OnClickListener()
	{
		
		@Override
		public void onClick(final View v)
		{
			if (isAnimationStart)
			{
				return;
			}
			isAnimationStart = true;
			Animation translateIn = new TranslateAnimation(0, 0, viewAnimationSpace.getHeight(), 0);
			translateIn.setDuration(500);
			translateIn.setFillAfter(true);
			viewAnimationSpace.setVisibility(View.GONE);
			animationContainer.startAnimation(translateIn);
			
			mHandler.postDelayed(new Runnable()
			{
				
				@Override
				public void run()
				{
					actionBar.setVisibility(View.VISIBLE);
					
				}
			}, 500);
			mHandler.postDelayed(new Runnable()
			{
				
				@Override
				public void run()
				{
					mUsernameEdit.setFocusable(true);
					mUsernameEdit.setFocusableInTouchMode(true);
					
					mPasswordEdit.setFocusable(true);
			        mPasswordEdit.setFocusableInTouchMode(true);
					switch(v.getId())
					{
						case R.id.login_name:
							mUsernameEdit.requestFocus();
							showSoftInput(mUsernameEdit);
							break;
						case R.id.login_code:
							mPasswordEdit.requestFocus();
							showSoftInput(mPasswordEdit);
							break;
					}
				}
			}, 550);
			
		}
	};
	
	public static void hideSoftKeyBoard(View view)
    {
        if (view == null)
        {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm.isActive())
        {
        	imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

	
    public void showSoftInput(final EditText editText)
    {
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        final LoadingProgressDialog dialog = new LoadingProgressDialog(this);
        dialog.setMessage(getString(R.string.authenticate_login));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				if (mAuthTask != null) {
                    mAuthTask.cancel(true);
                }
				if (mOtherUserLoginTask != null) {
                    mOtherUserLoginTask.cancel(true);
                    mOtherUserLoginTask = null;
                }
            }
        });
        // We save off the progress dialog in a field so that we can dismiss
        // it later. We can't just call dismissDialog(0) because the system
        // can lose track of our dialog if there's an orientation change.
        mProgressDialog = dialog;
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to the server for authentication. The button is configured to call handleLogin() in the layout XML.
     * 
     * @param view
     *            The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {

        if (mRequestNewAccount)  {
            mUsername = mUsernameEdit.getText().toString();
        }
        mPassword = mPasswordEdit.getText().toString();
        if (TextUtils.isEmpty(mUsername)) {
            mUsernameEdit.requestFocus();
            Toast.makeText(getApplicationContext(), R.string.tip_username_none, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordEdit.requestFocus();
            Toast.makeText(getApplicationContext(), R.string.tip_password_none, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!PhoneNumUtils.isPhoneNumberValid(mUsername))  {
            mUsernameEdit.requestFocus();
            Toast.makeText(getApplicationContext(), R.string.tip_username_must_phonenum, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPassword.length() < 6 || mPassword.length() > 16)  {
            mPasswordEdit.requestFocus();
            Toast.makeText(getApplicationContext(), R.string.tip_password_not_right_length, Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Show a progress dialog, and kick off a background task to perform
            // the user login attempt.
			if (GetPublicParams.getAvailableNetWorkType(getApplicationContext()) == -1) {
        		Toast.makeText(getApplicationContext(), R.string.tip_login_fail_no_network, Toast.LENGTH_SHORT).show();
            }else{
            	mAuthTask = new UserLoginTask(mUsername, mPassword);
            	mAuthTask.execute();
            }
        }

    }
    
	private void initFullStyle()
	{
		Window window = getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = LayoutParams.MATCH_PARENT;
		layoutParams.height = layoutParams.MATCH_PARENT;
		layoutParams.gravity = Gravity.FILL;
		window.setAttributes(layoutParams);
	}

    /**
     * Called when response is received from the server for confirm credentials request. See onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
     * caller.
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
    }

    /**
     * Called when response is received from the server for authentication request. See onAuthenticationResult(). Sets the AccountAuthenticatorResult which is sent back to the
     * caller. We store the authToken that's returned from the server as the 'password' for this account - so we're never storing the user's actual password locally.
     * 
     * @param result
     *            the confirmCredentials result.
     */
    private void finishLogin(String authToken) {

        try  {
            final Account account = new Account(user.getNickname(), Constant.ACCOUNT_TYPE);
            if (mRequestNewAccount) {
                try  {
                    mAccountManager.addAccountExplicitly(account, authToken, null);
                    // Set contacts sync for this account.
                    // ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
			} else {
                mAccountManager.setPassword(account, authToken);
            }
         // 保存返回信息 write to file
            String jsonStringer = null;
            
            jsonStringer = new JSONStringer().object()
                            .key("nickname").value(TextUtils.isEmpty(user.getNickname()) ? mUsernameEdit.getText().toString().trim() : user.getNickname())
                            .key("username").value(TextUtils.isEmpty(user.getUsername()) ? null : user.getUsername())
                            .key("password").value(TextUtils.isEmpty(user.getPassword()) ? mPasswordEdit.getText().toString().trim() : user.getPassword())
                            .key("UID").value(user.getUID())
                            .key("openid").value(user.getOpenid())
                            .key("OpenKey").value(user.getOpenKey())
                            .key("TOKEN").value(user.getTOKEN())
                            .key("regtype").value(user.getRegtype())
                            .key("expires_in").value(user.getExpires_in())
                            .key("recode").value(TextUtils.isEmpty(user.getRecode() + "") ? 100 : user.getRecode())
                            .key("logoUrl").value(TextUtils.isEmpty(user.getLogoUrl()) ? null : user.getLogoUrl())
                            .endObject()
                            .toString();
            mAccountManager.setUserData(account, "userInfo", jsonStringer);
            // send msg to third app
            Intent intent = new Intent("zhuoyou.android.account.SEND_USER_INFO");
            Bundle bundle = new Bundle();
            bundle.putString("logoCache", PropertyFileUtils.getSDPath() + "/usercenter/logo.png");
            intent.putExtra("userInfo", jsonStringer);
            intent.putExtra("userLogin", bundle);
            sendBroadcast(intent);
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, user.getNickname());
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constant.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        hideProgress();
        com.market.account.authenticator.AccountManager.getInstance().onAccountLogin();
        finish();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		mInstance = null;
	}

	/**
     * Called when the authentication process completes (see attemptLogin()).
     * 
     * @param authToken
     *            the authentication token returned by the server, or NULL if authentication failed.
     */
    public void onAuthenticationResult(User user) {
        String authToken = null;
        if (null != user) {
            authToken = user.getTOKEN();
        }
        boolean success = ((authToken != null) && (authToken.length() > 0));

        // Our task is complete, so clear it out
        mAuthTask = null;

        // Hide the progress dialog
        // hideProgress();

        if (success) {
        	if(TextUtils.isEmpty(user.getNickname())){
            	Toast.makeText(getApplicationContext(), R.string.tip_login_fail, Toast.LENGTH_SHORT).show();
            	hideProgress();
            	return;
            }
            if (!mConfirmCredentials) {
                finishLogin(authToken);
			} else {
                finishConfirmCredentials(success);
            }

		} else {
            // Hide the progress dialog
            hideProgress();
            String desc = user.getDesc();
            if(TextUtils.isEmpty(desc)) {
            	user.setDesc(getResources().getString(R.string.tip_login_fail));
            }
            
            if (mRequestNewAccount) {
                // "Please enter a valid username/password. -2001
        		Toast.makeText(getApplicationContext(), user.getDesc(), Toast.LENGTH_SHORT).show();
			} else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
                Toast.makeText(getApplicationContext(), user.getDesc(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onAuthenticationCancel() {
        // Our task is complete, so clear it out
        mAuthTask = null;
        // Hide the progress dialog
        hideProgress();
    }

    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        if (TextUtils.isEmpty(mUsername)) {
            // If no username, then we ask the user to log in using an
            // appropriate service.
            final CharSequence msg = "If no username, then we ask the user to log in using an appropriate service.";
            return msg;
        }
        if (TextUtils.isEmpty(mPassword)) {
            // We have an account but no password
            return "We have an account but no password";
        }
        return null;
    }

    /**
     * Shows the progress UI for a lengthy operation.
     */
    private void showProgress() {
        if (mInstance != null && !mInstance.isFinishing()) {
            showDialog(0);
        }
    }

    /**
     * Hides the progress UI for a lengthy operation.
     */
    private void hideProgress() {
		if (mProgressDialog != null) {
            mProgressDialog.dismiss();
//            mProgressDialog = null;
        }
    }

    /**
     * Represents an asynchronous task used to authenticate a user against the SampleSync Service
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {
        private String username;
        private String password;
        private String loginResult;

        public UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgress();
        }

        @Override
        protected String doInBackground(Void... params) {
            final Map<String, String> loginParams = new HashMap<String, String>();
            String passwdMD5 = MD5Util.md5(password);
            String devinfo = com.market.account.dao.UserInfo.getDeviceInfo(getApplicationContext());
            loginParams.put("uid", username);
            loginParams.put("passwd", passwdMD5);
            loginParams.put("utype", "zhuoyou");
            loginParams.put("devinfo", devinfo);

            loginParams.put("sign", MD5Util.md5(username + passwdMD5 + "zhuoyou" + devinfo + Constant.SIGNKEY));
            loginResult = null;
            loginResult = HttpOperation.postRequest(Constant.LOGIN, loginParams);
            try {
                JSONObject jsonObject = new JSONObject(loginResult);
             // result == 0 表示正常?小于0 接口错误?
                int result = jsonObject.getInt("result");
                if (result == 0) {
                	user.setNickname(jsonObject.has("nickname")?jsonObject.getString("nickname"): username);
               		user.setUsername(jsonObject.has("username")?jsonObject.getString("username"): null);
                    user.setPassword(passwdMD5);
                    user.setTOKEN(jsonObject.has("token")?jsonObject.getString("token"):null);
                    user.setUID(jsonObject.has("username")?jsonObject.getString("username"): username);
                    user.setOpenid(jsonObject.has("openid")?jsonObject.getString("openid"):null);
                    user.setExpires_in(jsonObject.has("expire")?jsonObject.getString("expire"):null);
                    user.setRecode(jsonObject.has("score")?jsonObject.getInt("score"):0);
                    user.setLogoUrl(jsonObject.has("avatarurl") ? jsonObject.getString("avatarurl") : jsonObject.has("avatar") ? jsonObject.getString("avatar") : null);
                    // user.setLevel(jsonObject.getInt("level"));
                    // user.setLogoUrl(jsonObject.getString("avatar"));
                    // user.setGender(jsonObject.getString("gender"));
                    // user.setAge(jsonObject.getInt("age"));
                    if(TextUtils.isEmpty(user.getLogoUrl())) {
                    	AuthenticatorActivity.deleteUserLogo();
                    }else {
                    	AuthenticatorActivity.downloadUserLogo(user.getLogoUrl());
                    }
                }
                user.setRegtype("zhuoyou");
                user.setResult(result);
                user.setDesc(jsonObject.has("desc")?jsonObject.getString("desc"):null);
			} catch (Exception e) {
                e.printStackTrace();
            }
            return loginResult;
        }

        @Override
        protected void onPostExecute(final String authToken) {
            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            onAuthenticationResult(user);
        }

        @Override
        protected void onCancelled() {
            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the
            // activity to let it know.
            onAuthenticationCancel();
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
    			public void onError(UiError e)
    			{
    				hideProgress();
    				Util.toastMessage(AuthenticatorActivity.this, getText(R.string.tip_login_fail).toString());
    			}
    			
    		};
    		try {
    			showProgress();
    			mTencent.setAccessToken(user.getTOKEN(), user.getExpires_in());
    			mTencent.setOpenId(user.getOpenid());
    			mTencent.login(this, "all", listener);
    		} catch (Exception e) {
    			hideProgress();
    			e.printStackTrace();
    		}
        }
    }

	private class BaseUiListener implements IUiListener {

        @Override
		public void onComplete(Object response) {
            JSONObject values = (JSONObject) response;
			try {
                user.setExpires_in(values.has("expires_in") ? values.getString("expires_in") : null);
                user.setTOKEN(values.has("access_token") ? values.getString("access_token") : null);
                user.setUID(values.has("openid") ? values.getString("openid") : null);
                user.setOpenKey(values.has("pfkey") ? values.getString("pfkey") : null);
			} catch (JSONException e) {
                e.printStackTrace();
            }
            this.doComplete(values);
        }

		protected void doComplete(JSONObject values) {
            
        }

        @Override
		public void onError(UiError e) {
            Util.toastMessage(AuthenticatorActivity.this, "onError: " + e.errorDetail);
        }

        @Override
		public void onCancel() {
            hideProgress();
            Util.toastMessage(AuthenticatorActivity.this, getText(R.string.weibosdk_demo_toast_auth_canceled).toString());
        }

    }

    /**
     * Represents an asynchronous task used to authenticate a user against the SampleSync Service
     */
    public class OtherUserLoginTask extends AsyncTask<Void, Void, String>
    {
        private String loginResult;
        private String data;
        private String utype;

		public OtherUserLoginTask(String data, String utype) {
            this.data = data;
            this.utype = utype;
        }

        @Override
		protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params)
        {
            final Map<String, String> loginParams = new HashMap<String, String>();
            String devinfo = com.market.account.dao.UserInfo.getDeviceInfo(getApplicationContext());
            String signString = MD5Util.md5(user.getUID() + user.getTOKEN() + utype + data + devinfo + Constant.SIGNKEY);

            loginParams.put("uid", user.getUID());
            loginParams.put("passwd", user.getTOKEN());
            loginParams.put("utype", utype);
            loginParams.put("data", data);
            loginParams.put("sign", signString);
            // loginParams.put("openid", user.getUID());
            loginParams.put("devinfo", devinfo);
            if(isCancelled())
                return "";
            loginResult = HttpOperation.postRequest(Constant.AUTH, loginParams);
            try
            {
                // {"desc":"登录成功","openqq":"8E654C320C76CDA765F0617658FCE4CF","gender":"?,"nickname":"幽幽","result":0,"openid":"5372d294aebf1e774c000001","score":0}

                JSONObject jsonObject = new JSONObject(loginResult);
                // result == 0 表示正常?小于0 接口错误?
                int result = jsonObject.getInt("result");
                if (result == 0)
                {
                    user.setNickname(jsonObject.has("nickname")?jsonObject.getString("nickname"):TextUtils.isEmpty(user.getNickname())?utype:user.getNickname());
                    user.setUsername(jsonObject.has("username") ? jsonObject.getString("username") : null);
                    user.setTOKEN(jsonObject.has("token")?jsonObject.getString("token"):null);
                    user.setOpenid(jsonObject.has("openid")?jsonObject.getString("openid"):null);		//返回值卓悠的openid (唯一)
                    user.setRecode(jsonObject.has("score")?jsonObject.getInt("score"):0);
                    user.setLogoUrl(jsonObject.has("avatarurl") ? jsonObject.getString("avatarurl") : jsonObject.has("avatar") ? jsonObject.getString("avatar") : null);
                    user.setGender(jsonObject.has("gender")?jsonObject.getString("gender"):null);
                    if(TextUtils.isEmpty(user.getLogoUrl())){
                    	deleteUserLogo();
                    }else{
                    	AuthenticatorActivity.downloadUserLogo(user.getLogoUrl());
                    }
                }
                user.setRegtype(utype);
                user.setResult(result);
                user.setDesc(jsonObject.has("desc")?jsonObject.getString("desc"):null);
			} catch (Exception e) {
                e.printStackTrace();
            }
            return loginResult;
        }

        
        @Override
		protected void onPostExecute(final String authToken) {
            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            if(isCancelled())
                return;
			if (user.getResult() == 0) {
                onAuthenticationResult(user);
            } else if (user.getResult() < 0 || TextUtils.isEmpty(authToken)) {
            	hideProgress();
                if (TextUtils.isEmpty(user.getDesc())) {
                    Toast.makeText(AuthenticatorActivity.this, R.string.tip_login_fail, Toast.LENGTH_LONG).show();
                } else {
                	Toast.makeText(getApplicationContext(), user.getDesc(), Toast.LENGTH_SHORT).show();
                }

            }

        }

        @Override
        protected void onCancelled()
        {
            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the
            // activity to let it know.
            onAuthenticationCancel();
        }
        
        
        public void downloadUserLogo(String jsonString,String type){
        	if(type.equals("openweibo")){
        		if (null != jsonString)
                {
                    try
                    {
                        JSONObject response1 = new JSONObject(jsonString);
                        if (response1.has("profile_image_url"))
                        {
                            Bitmap bitmap = null;
                            bitmap = Util.getbitmap(response1.getString("profile_image_url"));
                            if (null != bitmap)
                            {
                                String logoUrlDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO;
                                String logoUrlBaseDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO_BASE_PATH;
                                File f = new File(logoUrlDir);
								if (!f.exists()) {
                                	new File(logoUrlBaseDir).mkdirs();
                                } else if(!new File(logoUrlBaseDir).exists())
                                	new File(logoUrlBaseDir).mkdir();
                                File file = new File(logoUrlBaseDir + "logo.png");

                                FileOutputStream out = new FileOutputStream(file);
                                out.write(BitMapUtils.createBitByteArray(bitmap));
                                out.flush();
                                out.close();
                            }

                        }
					} catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        	}else if(type.equals("openqq")){
        		try
        		{
        			JSONObject json = new JSONObject(jsonString);
        			if (json.has("figureurl"))
        			{
        				Bitmap bitmap = null;
        				bitmap = Util.getbitmap(json.getString("figureurl_qq_2"));
        				if (null != bitmap)
        				{
        						String logoUrlDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO;
        						String logoUrlBaseDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO_BASE_PATH;
        						File f = new File(logoUrlDir);
        						if (!f.exists()) {
        							new File(logoUrlBaseDir).mkdirs();
        						} else if (!new File(logoUrlBaseDir).exists())
        							new File(logoUrlBaseDir).mkdir();
        						File file = new File(logoUrlBaseDir + "logo.png");
        						FileOutputStream out = new FileOutputStream(file);
        						out.write(BitMapUtils.createBitByteArray(bitmap));
        						out.flush();
        						out.close();
        					}
        				}
                }catch (Exception e) {
                	e.printStackTrace();
                }
			}
        }
    }

    public static void deleteUserLogo(){
   	 String logoFile = PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH;
        if (new File(logoFile).exists())
        {
            new File(logoFile).delete();
        }
   }
    
    public static void downloadUserLogo(String logoUrl){
    	if(TextUtils.isEmpty(logoUrl)) {
    		return;
    	}
    	Bitmap bitmap = null;
    	bitmap = Util.getbitmap(logoUrl);
    	if (null != bitmap)
        {
    		String logoUrlDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO;
            String logoUrlBaseDir = PropertyFileUtils.getSDPath() + Constant.USERLOGO_BASE_PATH;
            File f = new File(logoUrlDir);
			if (!f.exists()) {
            	new File(logoUrlBaseDir).mkdirs();
            } else if(!new File(logoUrlBaseDir).exists())
            	new File(logoUrlBaseDir).mkdir();
            File file = new File(logoUrlBaseDir + "logo.png");

            try {
				FileOutputStream out = new FileOutputStream(file);
				out.write(BitMapUtils.createBitByteArray(bitmap));
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
    }
    private void updateUserInfo()
    {
        if (mTencent != null && mTencent.isSessionValid() && mTencent.getOpenId() != null) //判断用户是否登录成功，且是否获取了openid
        {
			IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e)
                {
                	hideProgress();
                	Util.toastMessage(AuthenticatorActivity.this, e.errorMessage);
                }

                @Override
                public void onComplete(final Object response)
                {
					final JSONObject json = (JSONObject) response;
					if (null != response) {
						try {
                            user.setNickname(json.has("nickname")?json.getString("nickname") : null);
                            user.setLogoUrl(json.has("figureurl_qq_2")?json.getString("figureurl_qq_2") : null);
						} catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
					mOtherUserLoginTask = new OtherUserLoginTask(response.toString(), "openqq");
					mOtherUserLoginTask.execute();
//                    new OtherUserLoginTask(response.toString(), "openqq").execute();
                }

                @Override
				public void onCancel() {
                	hideProgress();
                }
            };
            mInfo = new UserInfo(getApplicationContext(), mTencent.getQQToken());
            mInfo.getUserInfo(listener);
		} else {
            hideProgress();
            Toast.makeText(getApplicationContext(), getString(R.string.weibosdk_demo_toast_auth_canceled_again), Toast.LENGTH_SHORT).show();
        }
    }

    /************************************************ qq login end ***************************************************/
    /************************************************ sina login start ***********************************************/

    /**
     * 登入按钮的监听器，接收授权结果?
     */
    private class AuthListener implements WeiboAuthListener
    {
        @Override
        public void onComplete(Bundle values)
        {
            showProgress();
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid())
            {

                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
                final String uid = accessToken.getUid();
                final String token = accessToken.getToken();
                user.setUID(uid);
                user.setExpires_in(String.valueOf(accessToken.getExpiresTime()));
                user.setTOKEN(token);

				new Thread() {

                    @Override
					public void run() {
                    	// 通过uid获取用户基础信息，只需要用户的昵称和头?
                    	StringBuilder sBuilder = new StringBuilder();
                        sBuilder.append("https://api.weibo.com/2/users/show.json?").append("source=").append(com.market.account.weibosdk.Constants.APP_KEY).append("&uid=").append(uid).append("&access_token=").append(token);
                        String jsonString = null;
						try {
                            jsonString = HttpOperation.getRequest(sBuilder.toString());
							if (null != jsonString) {
								try {
                                    JSONObject response1 = new JSONObject(jsonString);
                                    user.setNickname(response1.getString("screen_name"));
                                    user.setLogoUrl(response1.getString("profile_image_url"));
                                    mOtherUserLoginTask = new OtherUserLoginTask(jsonString, "openweibo");
                                    mOtherUserLoginTask.execute();
//                                    new OtherUserLoginTask(jsonString, "openweibo").execute();
								} catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                            	hideProgress();
                            	Looper.prepare();
                            	Toast.makeText(getApplicationContext(), R.string.get_user_info_failed, Toast.LENGTH_SHORT).show();
                            	Looper.loop();
                            }
						} catch (Exception e) {
							hideProgress();
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        }
        @Override
        public void onWeiboException(WeiboException e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel()
        {
            Toast.makeText(getApplicationContext(), R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 登出按钮的监听器，接收登出处理结果。（API 请求结果的监听器?
     */
    private class LogOutRequestListener implements RequestListener
    {
        @Override
		public void onComplete(String response) {
			if (!TextUtils.isEmpty(response)) {
				try {
                    JSONObject obj = new JSONObject(response);
                    String value = obj.getString("result");

					if ("true".equalsIgnoreCase(value)) {
                        AccessTokenKeeper.clear(getApplicationContext());
                    }
				} catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS)
        {
            // Do nothing
        }

        @Override
        public void onIOException(IOException e)
        {
            // mTokenView.setText(R.string.weibosdk_demo_logout_failed);
        }

        @Override
        public void onError(WeiboException e)
        {
            // mTokenView.setText(R.string.weibosdk_demo_logout_failed);
        }

        @Override
        public void onWeiboException(WeiboException paramWeiboException) {
            // TODO Auto-generated method stub
            
        }
    }

    /**
     * ?SSO 授权 Activity 退出时，该函数被调用?
     * 
     * @param requestCode
     *            .
     * @param resultCode
     *            .
     * @param data
     *            .
     * @see {@link Activity#onActivityResult}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (mSsoHandler != null)
        {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }
    
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		finish();
		return true;
	}

    @Override  
    public void finish() 
    {  
        super.finish();  
        hideSoftKeyBoard(mPasswordEdit);
        if (isAnimationStart)
    	{
        	overridePendingTransition(R.anim.push_bottom_in,R.anim.push_right_out);  
    	}
        else
        {
        	overridePendingTransition(R.anim.push_bottom_in,R.anim.push_bottom_out);  
        }
    }  
}
