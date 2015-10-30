package com.zhuoyi.market.mine;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.authenticator.AuthenticatorActivity;
import com.market.account.constant.Constant;
import com.market.account.dao.UserInfo;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.user.UserInit;
import com.market.account.utils.GetPublicParams;
import com.market.account.utils.PropertyFileUtils;
import com.market.download.updates.AppUpdateManager;
import com.market.net.data.AppInfoBto;
import com.zhuoyi.market.CheckActivity;
import com.zhuoyi.market.PersonalInfoActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.ShareAppActivity;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appManage.AppManageUtil;
import com.zhuoyi.market.appManage.update.MarketUpdateActivity;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.cleanTrash.TrashActivity;
import com.zhuoyi.market.setting.MarketFeedbackActivity;
import com.zhuoyi.market.setting.SettingActivity;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.SharePreferenceUtils;
import com.zhuoyi.market.view.AbsCustomView;

public class ManageView extends AbsCustomView implements OnClickListener{

	public final static int UPDATE_APP_DES = 0;
	public final static int UPDATE_USER_LOGO = 3;
	public static final int GET_CAMPAIGN_INFO = 1;
	private static final int ZHUOYOU_USER_INFO = 1000;
	private final String LOGO_FILE_PATH = PropertyFileUtils.getSDPath() + Constant.USERLOGO_PATH;

	private View mRootView;
	private RelativeLayout mUpdateLayout;
	private ImageView mUserLogo;
	private ImageView mQQLogo;
	private ImageView mSinaLogo;
	private ImageView mNameEdit;
	private TextView mUserName;
	private TextView mEntryUserCenterText;
	private TextView mAppUpdateCountText;
	private LinearLayout mCollectLayout;
	private LinearLayout mMarketShareLayout;
	private RelativeLayout mAppCheckLayout;
	private RelativeLayout mAppClearLayout;
	private LinearLayout mMarketSettingLayout;
	private LinearLayout mMarketFeedBackLayout;
	private LinearLayout mAppUpdateCountLayout;
	
	private ArrayList<AppInfoBto> mAppUpdateList;
	private boolean mIsShowUpdateCount = true;
	private String openqq;
	private String openweibo;
	private Handler mHandler;
	private Drawable mUserLogoUnLoginDrawable;
	private Drawable mUserLogogroudDrawable;
	private Bitmap mUserBitmap = null;
	private ArrayList<ImageView> mUpdateIcon;
	private int mShowAppUpdateCount = 0;
	private ImageView mAppClearPoint;
	
	private BroadcastReceiver clearDot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (mAppClearPoint != null) {
                mAppClearPoint.setVisibility(View.GONE);
            }
        }

    };
	
	protected ManageView(Context context) {
		super(context);
		mRootView = (View) View.inflate(context, R.layout.layout_manage, null);
		mContext = context;
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch(msg.what) {
				case AppUpdateManager.MSG_UPDATE_INSTALLED:
				case AppUpdateManager.MSG_RESET_LIST:
					disPlayUpdateApp();
					setUpdateCountVisible();
					break;
				case UPDATE_USER_LOGO:
					setLoginAccountLogo(BitmapFactory.decodeFile(LOGO_FILE_PATH));
					break;
				
				case UserInit.UPDATE_USER_INFO:
					setUserInfo();
					break;

				}
			}
		};
	}


	private void findView() {
		mUpdateLayout = (RelativeLayout) mRootView.findViewById(R.id.manage_app_update);
		mUserLogo = (ImageView) mRootView.findViewById(R.id.manage_user_logo);
		mUserName = (TextView) mRootView.findViewById(R.id.manage_user_name);
		mNameEdit = (ImageView) mRootView.findViewById(R.id.manage_user_name_edit);
		mQQLogo = (ImageView) mRootView.findViewById(R.id.manage_user_login_qq);
		mSinaLogo = (ImageView) mRootView.findViewById(R.id.manage_user_login_sina);
		mEntryUserCenterText = (TextView) mRootView.findViewById(R.id.manage_entry_center_middle);
		mAppUpdateCountText = (TextView) mRootView.findViewById(R.id.manage_app_update_count);
		
		mEntryUserCenterText.setTextColor(mContext.getResources().getColor(R.color.entry_usercenter));
		mEntryUserCenterText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mEntryUserCenterText.getPaint().setAntiAlias(true);
		mCollectLayout = (LinearLayout) mRootView.findViewById(R.id.manage_app_collect);
		mMarketShareLayout = (LinearLayout) mRootView.findViewById(R.id.manage_share);
		mAppCheckLayout = (RelativeLayout) mRootView.findViewById(R.id.manage_app_check);
		mAppClearLayout = (RelativeLayout) mRootView.findViewById(R.id.manage_clear);
		mMarketSettingLayout = (LinearLayout) mRootView.findViewById(R.id.manage_setting);
		mMarketFeedBackLayout = (LinearLayout) mRootView.findViewById(R.id.manage_feedback);
		mAppUpdateCountLayout = (LinearLayout) mRootView.findViewById(R.id.app_update_count_layout);
		mAppClearPoint = (ImageView) mRootView.findViewById(R.id.app_clear_point);
		mNameEdit.setOnClickListener(this);
		mUpdateLayout.setOnClickListener(this);
		mUserLogo.setOnClickListener(this);
		mUserName.setOnClickListener(this);
		mEntryUserCenterText.setOnClickListener(this);
		
		mCollectLayout.setOnClickListener(this);
		mMarketShareLayout.setOnClickListener(this);
		mAppCheckLayout.setOnClickListener(this);
		mAppClearLayout.setOnClickListener(this);
		mMarketSettingLayout.setOnClickListener(this);
		mMarketFeedBackLayout.setOnClickListener(this);

		if (!SharePreferenceUtils.hasCleardMobile()){
		    mAppClearPoint.setVisibility(View.VISIBLE);
        }
		
		try {
			if(mUserLogogroudDrawable == null){
				mUserLogogroudDrawable = mContext.getResources().getDrawable(R.drawable.usercenter_logo);
			}
			if(mUserLogoUnLoginDrawable == null)
				mUserLogoUnLoginDrawable = mContext.getResources().getDrawable(R.drawable.usercenter_unlogin_logo);
			mUserLogo.setBackgroundDrawable(mUserLogoUnLoginDrawable);
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		IntentFilter filter = new IntentFilter("com.zhuoyi.removeclearnotify");
        mContext.registerReceiver(clearDot, filter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.manage_app_update:
			if (mShowAppUpdateCount > 0) {
			    sendMessageToUpdateTab(0);
            }
			mIsShowUpdateCount = false;
			startActivity(MarketUpdateActivity.class);
			break;

		case R.id.manage_app_collect:
			AppManageUtil.startFavoriteActivity(mContext);
			break;

		case R.id.manage_share:
			Intent intent = new Intent(mContext, ShareAppActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Bundle bundle = new Bundle();
			intent.putExtras(bundle);
			intent.putExtra(ShareAppActivity.INTENT_KEY_FROM_MINE, true);
			mContext.startActivity(intent);
			break;

		case R.id.manage_setting:
			startActivity(SettingActivity.class);
			break;

		case R.id.manage_feedback:
			startActivity(MarketFeedbackActivity.class);
			break;
			
		case R.id.manage_clear:
		    clearRedDot();
		    startActivity(TrashActivity.class);
			break;
			
		case R.id.manage_app_check:
            startActivity(CheckActivity.class);
			break;
			
		case R.id.manage_entry_center_middle:
			startActivity(MarketMineActivity.class);
			break;

		case R.id.manage_user_name_edit:
		case R.id.manage_user_name:
		case R.id.manage_user_logo:
			if(!UserInfo.getUserIsLogin(mContext)) {
				startActivity(AuthenticatorActivity.class);
			} else {
				if (GetPublicParams.getAvailableNetWorkType(mContext) == -1)
					Toast.makeText(mContext, R.string.no_network, Toast.LENGTH_SHORT).show();
				else
					startActivity(PersonalInfoActivity.class);
			}

			break;

		default:
			break;
		}
	}


    private void clearRedDot() {
        mAppClearPoint.setVisibility(View.GONE);
        SharePreferenceUtils.setCleardMobile();
        if(mShowAppUpdateCount > 0 && mIsShowUpdateCount){
        }else{
            sendMessageToUpdateTab(0);
        }
    }

	@Override
	public View getRootView() {
		return mRootView;
	}

	@Override
	public void notifyDataSetChanged(String pkgName) {

	}

	@Override
	public void entryView() {
		super.entryView();
		findView();
		UserInit.registerUserInfoUpdate(mHandler);
		AppUpdateManager.setAppUpdateHandler(mHandler);
		mUpdateIcon = new ArrayList<ImageView>();
		disPlayUpdateApp();
		setUpdateCountVisible();
	}


	private void setUpdateCountVisible() {
		if(mIsShowUpdateCount){
			if(mAppUpdateList != null && mAppUpdateList.size() > 0) {
				int count = mAppUpdateList.size();
				if(count >0) {
					sendMessageToUpdateTab(count);
				}
			} else {
				mAppUpdateCountText.setText(mContext.getResources().getString(R.string.app_no_update));
			}
		}else{
			sendMessageToUpdateTab(0);
		}
	}


	public void disPlayUpdateApp() {
		if(MarketApplication.getAppUpdateList() == null) return;
		mAppUpdateList = (ArrayList<AppInfoBto>) ((ArrayList<AppInfoBto>)MarketApplication.getAppUpdateList()).clone();
		mAppUpdateList = filterIgnoreApp(mAppUpdateList);
		mShowAppUpdateCount = mAppUpdateList.size();
		if(mShowAppUpdateCount > 0) {
				mAppUpdateCountText.setText(
						(mShowAppUpdateCount > 3 ? mContext.getResources().getString(R.string.app_update_count_pre) : "") + 
						String.format(mContext.getResources().getString(R.string.app_update_count), mShowAppUpdateCount));
		} else{
				mAppUpdateCountText.setText(mContext.getResources().getString(R.string.app_no_update));
		}

		/**
		 * 显示 <= 3个应用更新图标
		 */
		while(mShowAppUpdateCount < mUpdateIcon.size() && mUpdateIcon.size() > 0) {		//更新数量小于3个,删除多余的ImageView组件
			mAppUpdateCountLayout.removeView(mUpdateIcon.get(mUpdateIcon.size() - 1));
			mUpdateIcon.remove(mUpdateIcon.size() - 1);
		} 
		int showIconLength = mUpdateIcon.size();
		while(showIconLength < Math.min(mShowAppUpdateCount, 3)) {		//ImageView组件最多不超过3个
			ImageView iconView = new ImageView(mContext);
			LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.rightMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.manage_update_icon_margin);
			iconView.setLayoutParams(params);
			mUpdateIcon.add(iconView);
			mAppUpdateCountLayout.addView(iconView, 0);
			showIconLength = mUpdateIcon.size();
		}
		
		int showIndex = 0;
		try {
			PackageManager packageManager = mContext.getPackageManager();
			for (AppInfoBto appInfoBto : mAppUpdateList) {
				if(showIndex >= showIconLength) {
					break;
				}
				AsyncImageCache.from(mContext).displayImage(true, false,
						mUpdateIcon.get(showIndex),
						-1,
						mContext.getResources().getDimensionPixelOffset(R.dimen.manage_update_icon_size),
						mContext.getResources().getDimensionPixelOffset(R.dimen.manage_update_icon_size),
						new AsyncImageCache.UpdateAppImageGenerator(appInfoBto.getPackageName(), packageManager),
						false, false ,true, null);
				showIndex ++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private ArrayList<AppInfoBto> filterIgnoreApp(ArrayList<AppInfoBto> appInfoBtos) {
		Iterator<AppInfoBto> it = appInfoBtos.iterator();
		while (it.hasNext()) {
			AppInfoBto appInfoBto = (AppInfoBto) it.next();
			if(AppUpdateManager.containsIgnoreApp(mContext, appInfoBto.getPackageName())) {
				it.remove();
			}
		}
		return appInfoBtos;
	}
	
	private void startActivity(Class classz) {
		Intent intent = new Intent();
		intent = new Intent(mContext, classz);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}


	/**
	 * 
	 * @param updateCount	更新的应用数量
	 * @param isShowCount	是否显示更新
	 */
	private void sendMessageToUpdateTab(int updateCount) {
		Intent intent = new Intent();
		intent.setAction(Splash.APPS_UPDATE);
		intent.putExtra("update_count", updateCount);
		mContext.sendBroadcast(intent);
	}




	protected void setUserInfo(){
		if (UserInfo.getUser(mContext)) {
			final JSONObject userObject = UserInfo.getUserInfo(mContext);
			try {
				final String userName = userObject.getString("nickname");
				final String score = userObject.getString("recode");
				final boolean canCheckIn = userObject.has("cancheckin") ? userObject.getBoolean("cancheckin") : true;
				Bitmap bitmap = null;
				mUserName.setText(userName);
				mNameEdit.setVisibility(View.VISIBLE);
				updateUserInfoByJson(userObject);
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


	protected void onResume() {
		setUserInfo();
		disPlayUpdateApp();
	}

	protected void setUserLogoutInfo() {
		mUserLogo.setBackgroundDrawable(mUserLogoUnLoginDrawable);
		mUserName.setText(mContext.getResources().getString(R.string.user_center));
		mNameEdit.setVisibility(View.GONE);
		mQQLogo.setVisibility(View.GONE);
		mSinaLogo.setVisibility(View.GONE);
		mNameEdit.setVisibility(View.GONE);
		BaseActivity_Html5.deleteUserLogo();
	}

	private void updateUserInfoByJson(JSONObject jsonObject){
		if(!UserInfo.getUserIsLogin(mContext)) return;
		try {
			openqq = jsonObject.has("openqq") ? jsonObject.getString("openqq") : null;
			openweibo = jsonObject.has("openweibo") ? jsonObject.getString("openweibo") : null;
			String nickName = jsonObject.has("nickname") ? jsonObject.getString("nickname") : null;

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
	

	@Override
	public void freeViewResource() {
		AppUpdateManager.setAppUpdateHandler(null);
		mHandler = null;
		if(mUserBitmap != null && !mUserBitmap.isRecycled()) {
            mUserBitmap.recycle();
            mUserBitmap = null;
        }
		mContext.unregisterReceiver(clearDot);
		super.freeViewResource();
	}


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
}


