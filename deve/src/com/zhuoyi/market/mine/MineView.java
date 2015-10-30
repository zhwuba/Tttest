package com.zhuoyi.market.mine;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.GetUserInfo;
import com.market.account.dao.GetUserInfo.GetUserInfoListener;
import com.market.account.dao.UserInfo;
import com.market.account.dao.UserLogin;
import com.market.account.dao.UserLogin.UserLoginListener;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.login.BaseHtmlActivity;
import com.market.account.receiver.AccountLoginReceiver;
import com.market.account.user.UserInit;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.PropertyFileUtils;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.IntegralActivityBto;
import com.market.net.request.GetIntegralReq;
import com.market.net.response.GetIntegralResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.view.CommonSubtitleView;
import com.market.view.SearchLoadingLayout;
import com.zhuoyi.market.PersonalInfoActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;

public class MineView extends AbsCustomView implements OnClickListener{

	public final static int UPDATE_APP_DES = 0;
	public final static int UPDATE_USER_LOGO = 3;
	public static final int GET_CAMPAIGN_INFO = 1;
	private static final int ZHUOYOU_USER_INFO = 1000;

	private final String LOGO_FILE_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH;
	private View mView;
	private Context mContext;
	private CommonSubtitleView mCommonSubtitleView;
	private Drawable mUserBackgroudDrawable;
	private Drawable mUserLogogroudDrawable;
	private Drawable mUserLogoUnLoginDrawable;
	private ImageView mUserLogo;
	private ImageView mQQLogo;
	private ImageView mSinaLogo;
	private ImageView mNameEdit;
	private TextView mUserName;
	private TextView mRecode;
	private ImageView mCampaignWait;
	//	private TextView mRecodeTask;

	private Bitmap mUserBitmap = null;

	private RelativeLayout mUserRelativeLayout;
	private GridView mIntegralView;
	private GridView mActView;
	private TextView mIntegralText;
	private TextView mActText;
	
	private ScrollView mScrollView;
	private SearchLoadingLayout mLoadingLayout;
	private LinearLayout mRefreshLayout;
	private Button mRefreshButton;

	private GetUserInfo mGetUserInfo;
	private String openqq;
	private String openweibo;
	private Handler mHandler;
	private BroadcastReceiver mAccountLoginReceivernew;
	private CampaignAdapter mCampaignAdapter;
	private CampaignAdapter mActAdapter;

	private long mLastCheckImageTime;
	
	private UserInit mUserInit;
	
	protected MineView(Context context) {
		super(context);
		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch(msg.what) {
				case ZHUOYOU_USER_INFO:
					Bundle bundle = msg.getData();
					String checkIn = bundle.getString("checkIn");
					setUserInfo();
					break;
				case UPDATE_USER_LOGO:
					setLoginAccountLogo(BitmapFactory.decodeFile(LOGO_FILE_PATH));
					break;
				case GET_CAMPAIGN_INFO:
					bindIntegralData((HashMap<String, Object>) msg.obj);
					break;
				}

			}

		};
		
		mView = View.inflate(context, R.layout.mine_content, null);
		
		mUserInit = new UserInit(mContext, true);
	}

	@Override
	public View getRootView() {
		return mView;
	}


	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.myself_user_recode:
			startHtmlActivity(mContext.getResources().getString(R.string.user_center_score_detail), com.zhuoyi.market.constant.Constant.ZHUOYOU_URL_SCORE_DETAIL);
			break;

		case R.id.myself_user_name_edit:
		case R.id.myself_user_name:
		case R.id.myself_user_logo:
			if(!UserInfo.getUserIsLogin(mContext)) {
				startActivity(AuthenticatorActivity.class);
			} else {
				if (GetPublicParams.getAvailableNetWorkType(mContext) == -1)
					Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
				else
					startActivity(PersonalInfoActivity.class);
			}
			break;
		case R.id.refresh_button:
			if (MarketUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				return;
			}
			show(LOADING_VIEW);
			requestCampaignInfo(mContext);
			break;
		}

	}


	@Override
	public void entryView() {
		super.entryView();
		findView();
		mAccountLoginReceivernew = new AccountLoginReceiver(mHandler);
		registerAccountLoginReceiver();
		show(LOADING_VIEW);
		requestCampaignInfo(mContext);
		mUserInit.setTextview(mRecode);
	}


	@Override
	public void freeViewResource() {
		super.freeViewResource();
		mContext.unregisterReceiver(mAccountLoginReceivernew);
		mHandler = null;
		if(mUserBitmap != null && !mUserBitmap.isRecycled()) {
			mUserBitmap.recycle();
			mUserBitmap = null;
		}
		mUserInit.releaseRes();
	}


	private void findView() {
		mCommonSubtitleView = (CommonSubtitleView) mView.findViewById(R.id.title);
		mCommonSubtitleView.setSubtitleName(mContext.getResources().getString(R.string.entry_center_middle));

		mScrollView = (ScrollView) mView.findViewById(R.id.mine_scrollview);
		MarketUtils.setBaseLayout(mScrollView, mContext);
		mUserLogo = (ImageView) mView.findViewById(R.id.myself_user_logo);
		mUserName = (TextView) mView.findViewById(R.id.myself_user_name);
		mNameEdit = (ImageView) mView.findViewById(R.id.myself_user_name_edit);
		mQQLogo = (ImageView) mView.findViewById(R.id.myself_user_login_qq);
		mSinaLogo = (ImageView) mView.findViewById(R.id.myself_user_login_sina);
		mRecode = (TextView) mView.findViewById(R.id.myself_user_recode);

		mCampaignWait = (ImageView) mView.findViewById(R.id.mine_campaign_wait);
		mUserRelativeLayout = (RelativeLayout) mView.findViewById(R.id.myself_user_layout);
		mIntegralView = (GridView) mView.findViewById(R.id.mine_campaign_grid);
		mActView = (GridView) mView.findViewById(R.id.mine_act_grid);
		mRecode.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mRecode.getPaint().setAntiAlias(true);
		
		mIntegralText = (TextView) mView.findViewById(R.id.mine_get_score_text);
		mActText = (TextView) mView.findViewById(R.id.mine_switch_act_text);
		
		mLoadingLayout = (SearchLoadingLayout) mView.findViewById(R.id.search_loading);
		mRefreshLayout = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout_id);
		mRefreshButton = (Button) mView.findViewById(R.id.refresh_button);

		mRefreshButton.setOnClickListener(this);
		mRecode.setOnClickListener(this);
		mUserLogo.setOnClickListener(this);
		mUserName.setOnClickListener(this);

		mScrollView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				long currentTime = System.currentTimeMillis();
				if(currentTime - mLastCheckImageTime > 5000) {
					if (mCampaignAdapter != null) {
						mCampaignAdapter.checkImageLoad();
					}
					mLastCheckImageTime = currentTime;
				}
				return false;
			}
		});

		try {
			Bitmap bitmap = null;
			if(mUserBackgroudDrawable == null){
				bitmap =  MarketUtils.read565Bitmap(mContext, R.drawable.personalinfo_logo_bg);
				mUserBackgroudDrawable = new BitmapDrawable(bitmap);
			}
			if(mUserLogogroudDrawable == null){
				mUserLogogroudDrawable = mContext.getResources().getDrawable(R.drawable.usercenter_logo);
			}
			if(mUserLogoUnLoginDrawable == null)
				mUserLogoUnLoginDrawable = mContext.getResources().getDrawable(R.drawable.usercenter_unlogin_logo);
			mUserRelativeLayout.setBackgroundDrawable(mUserBackgroudDrawable);
			mUserLogo.setBackgroundDrawable(mUserLogoUnLoginDrawable);
		} catch (OutOfMemoryError e) {
			System.gc();
		}
	}


	private void startActivity(Class classz) {
		Intent intent = new Intent();
		intent = new Intent(mContext, classz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}


	public void setLoginAccountState(String userName, String score, boolean canCheckIn) {
		mUserName.setText(userName);
		mRecode.setVisibility(View.VISIBLE);
		mRecode.setText(mContext.getResources().getString(R.string.score) + score);
		//		mRecodeTask.setVisibility(View.VISIBLE);
	}


	@SuppressWarnings("deprecation")
	public void setLoginAccountLogo(Bitmap bitmap) {
		if(bitmap != null) {
			if(mUserBitmap != null && !mUserBitmap.isRecycled()) {
				mUserBitmap.recycle();
				mUserBitmap = null;
			}
			mUserBitmap = AsyncImageCache.getCroppedRoundBitmap(bitmap,(int)mContext.getResources().getDimension(R.dimen.myself_user_logo_width));
			mUserLogo.setBackgroundDrawable(new BitmapDrawable(AsyncImageCache.addBorder(mUserBitmap)));

		} else {
			mUserLogo.setBackgroundDrawable(mUserLogogroudDrawable);
		}
	}


	protected void setUserInfo(){
		if (UserInfo.getUser(mContext)) {
			final JSONObject userObject = UserInfo.getUserInfo(mContext);
			try {
				final String userName = userObject.getString("nickname");
				final String score = userObject.getString("recode");
				final boolean canCheckIn = userObject.has("cancheckin") ? userObject.getBoolean("cancheckin") : true;
				Bitmap bitmap = null;
				setLoginAccountState(userName, score, canCheckIn);
				mNameEdit.setVisibility(View.VISIBLE);
				mCampaignWait.setVisibility(View.GONE);
				if (new File(LOGO_FILE_PATH).exists()) {
					bitmap = BitmapFactory.decodeFile(LOGO_FILE_PATH);
					setLoginAccountLogo(bitmap);
				}else{
					final String userLogoUrl = userObject.has("logoUrl") ? userObject.getString("logoUrl") : null;
					if(!TextUtils.isEmpty(userLogoUrl)) {
						new Thread(new Runnable() {

							@Override
							public void run() {
								AuthenticatorActivity.downloadUserLogo(userLogoUrl);
								if(mHandler != null) { 
									mHandler.sendEmptyMessage(UPDATE_USER_LOGO);
								}
							}
						}).start();
					} else {//未设置过头像
						setLoginAccountLogo(null);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			setUserLogoutInfo();
		}
	}


	protected void updateUser() {
		mGetUserInfo = new GetUserInfo(mContext);
		GetUserInfoListener getUserInfoListener = new GetUserInfoListener() {

			@Override
			public void OnGetUserInfoCallBack(String result) {
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject jsonObject = new JSONObject(result);
						int mResult = jsonObject.getInt("result");

						if (mResult == 0) {
							updateUserInfoByJson(jsonObject);
							UserInit.updateUserInfo(jsonObject,mContext);
						} else if (mResult < 0) {
							Toast.makeText(mContext, jsonObject.getString("desc"), Toast.LENGTH_SHORT).show();
						} else if (mResult == 2) {
							UserLogin userLogin = new UserLogin(mContext);
							userLogin.setUserLoginListener(new UserLoginListener() {

								@Override
								public void OnUserLoginCallBack(String result) {
									JSONObject jsonObject;
									try {
										jsonObject = new JSONObject(result);
										int mResult = jsonObject.getInt("result");

										if (mResult == 0) {
											String userInfo = null;
											JSONObject userjsonObject = null;
											AccountManager mAccountManager = AccountManager.get(mContext);
											Account[] accounts = mAccountManager.getAccountsByType(Constant.ACCOUNT_TYPE);
											if (accounts != null && accounts.length >= 1) {
												userInfo = mAccountManager.getUserData(accounts[0], "userInfo");
											}
											userjsonObject = new JSONObject(userInfo);
											userjsonObject.put("TOKEN", jsonObject.has("token") ? jsonObject.getString("token") : null);
											userjsonObject.put("openid", jsonObject.has("openid") ? jsonObject.getString("openid") : null);
											mAccountManager.setUserData(accounts[0], "userInfo", userjsonObject.toString());
											updateUserInfoByJson(userjsonObject);
											UserInit.updateUserInfo(jsonObject,mContext);
										} else {

											AccountManager mAccountManager = AccountManager.get(mContext);
											Account[] accounts = mAccountManager.getAccountsByType(Constant.ACCOUNT_TYPE);
											if (accounts != null && accounts.length >= 1) {
												mAccountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>() {

													@Override
													public void run(AccountManagerFuture<Boolean> future) {
														// don,t forget to delete the cache .eg:logo
														BaseActivity_Html5.deleteUserLogo();
													}
												}, mHandler);
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

								}
							});
							userLogin.execute();
						}
					}
					catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		};

		mGetUserInfo.setGetUserInfoListener(getUserInfoListener);
		if (mGetUserInfo != null && mGetUserInfo.getStatus() != AsyncTask.Status.RUNNING) {
			try {
				mGetUserInfo.execute();
			} catch (RejectedExecutionException e) {
				e.printStackTrace();
			}
		}
	}


	protected void setUserLogoutInfo() {
		mUserLogo.setBackgroundDrawable(mUserLogoUnLoginDrawable);
		mUserName.setText(mContext.getResources().getString(R.string.user_center));
		mRecode.setVisibility(View.GONE);
		//		mRecodeTask.setVisibility(View.GONE);
		mQQLogo.setVisibility(View.GONE);
		mSinaLogo.setVisibility(View.GONE);
		mNameEdit.setVisibility(View.GONE);
		mCampaignWait.setVisibility(View.VISIBLE);
		BaseActivity_Html5.deleteUserLogo();
	}


	private void updateUserInfoByJson(JSONObject jsonObject){
		if(!UserInfo.getUserIsLogin(mContext)) return;
		try {
			openqq = jsonObject.has("openqq") ? jsonObject.getString("openqq") : null;
			openweibo = jsonObject.has("openweibo") ? jsonObject.getString("openweibo") : null;
			String score = jsonObject.has("score") ? jsonObject.getString("score") : null;
			String nickName = jsonObject.has("nickname") ? jsonObject.getString("nickname") : null;
			if(!TextUtils.isEmpty(score)){
				mRecode.setText(mContext.getResources().getString(R.string.user_center_score_1)  + score );
			}

			if (!TextUtils.isEmpty(openqq)) {
				mQQLogo.setVisibility(View.VISIBLE);
			}
			else {
				mQQLogo.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(openweibo)) {
				mSinaLogo.setVisibility(View.VISIBLE);
			}
			else {
				mSinaLogo.setVisibility(View.GONE);
			}

			if(!TextUtils.isEmpty(nickName)) {
				mUserName.setText(nickName);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	protected void onResume() {
		setUserInfo();
		if(UserInfo.getUserIsLogin(mContext)){
			updateUser();
			if(mUserInit != null)
				mUserInit.checkIn();
		}
	}


	private void registerAccountLoginReceiver() {
		try {
			final IntentFilter filter = new IntentFilter();
			filter.addAction("zhuoyou.android.account.SEND_USER_INFO");
			mContext.registerReceiver(mAccountLoginReceivernew, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void requestCampaignInfo(Context context) {
		String contents = "";
		GetIntegralReq mReq = new GetIntegralReq();
		contents = SenderDataProvider.buildToJSONData(context,
				MessageCode.GET_INTEGRAL_INFO_REQ, mReq);
		try {
			StartNetReqUtils.execMarketRequest(mHandler, GET_CAMPAIGN_INFO, MessageCode.GET_INTEGRAL_INFO_REQ, contents);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}


	private void bindIntegralData(HashMap<String, Object> map) {
		GetIntegralResp getIntegralResp = null;
		if (map != null && map.size()>0) {
			getIntegralResp = (GetIntegralResp) map.get("getIntegralResp");
			FrameInfoCache.saveFrameInfoToStorage(getIntegralResp, "integralframe");
		} else {
			getIntegralResp = (GetIntegralResp) FrameInfoCache.getFrameInfoFromStorage("integralframe");
		}

		if(getIntegralResp != null) {
			mUserInit.setMineviewFinish(true);
			show(CONTENT_VIEW);
			final List<IntegralActivityBto> integralActivityBtos = getIntegralResp.getInteActList();
			final List<IntegralActivityBto> actBtos = getIntegralResp.getActList();

			/** 赚取积分	**/
			if(integralActivityBtos != null && integralActivityBtos.size() > 0) {
				mCampaignAdapter = new CampaignAdapter(mContext, R.layout.mine_content_campaign_item);
				mCampaignAdapter.setDatas(integralActivityBtos);
				mIntegralView.setAdapter(mCampaignAdapter);
			} else {
				mIntegralView.setVisibility(View.GONE);
				mIntegralText.setVisibility(View.GONE);
			}
			
			
			/** 	兑奖活动	**/
			if(actBtos != null && actBtos.size() > 0) {
				
				mActAdapter = new CampaignAdapter(mContext, R.layout.mine_content_campaign_item);
				mActAdapter.setDatas(actBtos);
				mActView.setAdapter(mActAdapter);
			} else {
				mActView.setVisibility(View.GONE);
				mActText.setVisibility(View.GONE);
			}
			
			
		} else {
			show(REFRESH_VIEW);
		}
	}

	private static final int CONTENT_VIEW=0,LOADING_VIEW=1, REFRESH_VIEW=2;
	private void show(int whichView) {
		switch(whichView) {
		case CONTENT_VIEW:
			mUserRelativeLayout.setVisibility(View.VISIBLE);
			mIntegralView.setVisibility(View.VISIBLE);
			mIntegralText.setVisibility(View.VISIBLE);
			mActView.setVisibility(View.VISIBLE);
			mActText.setVisibility(View.VISIBLE);
			mLoadingLayout.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case LOADING_VIEW:
			mUserRelativeLayout.setVisibility(View.GONE);
			mIntegralView.setVisibility(View.GONE);
			mIntegralText.setVisibility(View.GONE);
			mActView.setVisibility(View.GONE);
			mActText.setVisibility(View.GONE);
			mLoadingLayout.setVisibility(View.VISIBLE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case REFRESH_VIEW:
			mUserRelativeLayout.setVisibility(View.GONE);
			mIntegralView.setVisibility(View.GONE);
			mIntegralText.setVisibility(View.GONE);
			mActView.setVisibility(View.GONE);
			mActText.setVisibility(View.GONE);
			mLoadingLayout.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void notifyDataSetChanged(String pkgName) {

	}
	
	private void startHtmlActivity(String title, String url) {
		Intent intent = new Intent(mContext,
				BaseHtmlActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("titleName",title);
		intent.putExtra("wbUrl",url);
		mContext.startActivity(intent);
	}
}
