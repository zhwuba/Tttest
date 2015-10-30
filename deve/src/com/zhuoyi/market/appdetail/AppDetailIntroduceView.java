package com.zhuoyi.market.appdetail;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.market.account.authenticator.AccountUpdate;
import com.market.account.authenticator.AccountUpdate.IAccountListener;
import com.market.account.dao.UserInfo;
import com.market.account.netutil.JavaScriptOperation;
import com.market.account.netutil.JavaScriptOperation.CallBack;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppDetailInfoBto;
import com.market.net.data.AppInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.market.net.request.GetRecommendAppReq;
import com.market.net.response.GetRecommendAppResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.MyHScrollView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.DensityUtil;
import com.zhuoyi.market.utils.MarketUtils;


public class AppDetailIntroduceView implements OnPageChangeListener, OnClickListener, IAccountListener{

	private Context mContext;
	private final int SHORT_LINE_COUNT = 4;
	private static final int OFFICAL_VISIBLE = 1;
	
	private int mIntroduceLineHeight;
	private boolean mIsSpread = true;
	private Handler mHandler;

	private int mRefid;
	private TextView mParticular_info_introduce;
	private ScrollView mIntroductScrollView;
	private ImageView mMoreImage;
	private LinearLayout mDetailImagesLayout;
	private TextView mParticular_info_version;
	private TextView mIntroduce_info_size;
	private TextView mIntroduce_updatetime;
	private LinearLayout mRecommendation_gridView;
	private LinearLayout mAllPeopleLikeView;
	private TextView mOtherPeopleText, mAllPeopleLikeText;
	private MyHScrollView mHorizontalScrollView;
	private LinearLayout mAppIntroduceLayout;
	private View mIntroductionView;
	private TextView mDownloadView;
	
	private String mActivityUrl;
	/**	 应用介绍 	**/
	private TextView mSecurityText;
	private TextView mChargeText;
	private TextView mAdvertisementText;
	private RatingBar mAppRatingStar;
	private TextView mAppTitleName;
	private LinearLayout mCornerLayout;
	private ImageView mOffImage;
	private ImageView mCornerImage;
	private TextView mParticular_info_size;
	private TextView mParticular_info_downtime;
	private ImageView mParticular_info_appicon;
	private WebView mWebView;
	/**	 应用介绍End  	**/
	
	private ImageView[] mImageViews;
	private LinearLayout mIndicatorLayout;
	private OnStartRecommentAppListener mOnStartRecommentAppListener;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBackInterface;

	private String mUploadFlag;
	private String mTopicId;
	
	private int mWebLoadMaxTime = 3;
	private int mWebLoadTime = 0;
	private JavaScriptOperation mAppDetailJsOperation;
	private AccountUpdate mAccountUpdate;
	
	public AppDetailIntroduceView(Context context,DownloadCallBackInterface downloadCallBackInterface,
			int refId, Handler handler, String uploadFlag, String topicId) {
		mContext = context;
		mRefid = refId;
		mHandler = handler;
		mDownloadCallBackInterface = new WeakReference<DownloadCallBackInterface>(downloadCallBackInterface);
		mUploadFlag = uploadFlag;
		mTopicId = topicId;
	}


	public void initView(View introductionView) {
		mIntroductionView = introductionView;
		mIntroductionView.setVisibility(View.GONE);
		mParticular_info_introduce = (TextView) introductionView.findViewById(R.id.particular_info_introduce);
		mAppIntroduceLayout = (LinearLayout) introductionView.findViewById(R.id.particular_info);
		mParticular_info_introduce.setLineSpacing(3.5f, 1.3f);
		mIntroductScrollView = (ScrollView) introductionView.findViewById(R.id.introduction_scrollview);
		mMoreImage = (ImageView) introductionView.findViewById(R.id.moreImage);
		mDetailImagesLayout = (LinearLayout) introductionView.findViewById(R.id.particular_info_detail_pic_layout);
		mParticular_info_version = (TextView) introductionView.findViewById(R.id.introduce_info_version);
		mIntroduce_info_size = (TextView) introductionView.findViewById(R.id.introduce_info_size);
		mIntroduce_updatetime = (TextView) introductionView.findViewById(R.id.introduce_updatetime);
		mRecommendation_gridView = (LinearLayout) introductionView.findViewById(R.id.app_recommend);
		mAllPeopleLikeView = (LinearLayout) introductionView.findViewById(R.id.app_like);
		mOtherPeopleText = (TextView) introductionView.findViewById(R.id.other_people_like_text);
		mAllPeopleLikeText = (TextView) introductionView.findViewById(R.id.all_people_like_text);
		mHorizontalScrollView = (MyHScrollView) introductionView.findViewById(R.id.horizontalScrollView);
		mWebView = (WebView) introductionView.findViewById(R.id.appdetail_campaign_webview);
		
		/** 应用介绍	**/
		mSecurityText = (TextView) introductionView.findViewById(R.id.particular_security);
		mChargeText = (TextView) introductionView.findViewById(R.id.particular_charge);
		mAdvertisementText = (TextView)introductionView. findViewById(R.id.particular_ad);
		mAppRatingStar = (RatingBar) introductionView.findViewById(R.id.particular_star);
		mAppTitleName = (TextView) introductionView.findViewById(R.id.particular_app_name);
		mCornerLayout = (LinearLayout) introductionView.findViewById(R.id.app_detail_info_corner_layout);
		mOffImage = (ImageView) introductionView.findViewById(R.id.app_detail_info_official);
		mCornerImage = (ImageView) introductionView.findViewById(R.id.app_detail_info_corner);
		mParticular_info_downtime = (TextView) introductionView.findViewById(R.id.particular_down_times);
		mParticular_info_size = (TextView) introductionView.findViewById(R.id.particular_info_size);
		mParticular_info_appicon = (ImageView) introductionView.findViewById(R.id.particular_info_appicon);
		/** 应用介绍 end		**/
		
		webViewSetting();
		initListener();
	}


	public void initListener() {
		mMoreImage.setOnClickListener(this);
		mParticular_info_introduce.setOnClickListener(this);
	}


	interface OnStartRecommentAppListener {
		public void onStartRecommentApp();
	}


	public void setOnStartRecommentAppListener(OnStartRecommentAppListener startRecommentAppListener) {
		mOnStartRecommentAppListener = startRecommentAppListener;
	}

	/**
	 * 显示活动图标
	 * @param activity	
	 */
	private void showActivityIcon(String activity) {
		if(TextUtils.isEmpty(activity)) return;
		String[] icons = activity.split(",");
		try {
			for (String icon : icons) {
				AsyncImageCache.from(mContext).displayImage(mCornerLayout, Integer.parseInt(icon));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void bindBaseIntroductionData(AppDetailInfoBto appDetailInfoBto) {
		
		int starsNum = appDetailInfoBto.getStars();
		int security = appDetailInfoBto.getSecurity();
		int charge = appDetailInfoBto.getCharge();
		int isAd = appDetailInfoBto.isAd();
		int official = appDetailInfoBto.getOfficialLogo();
		
		mAppRatingStar.setRating(starsNum);
		mAppTitleName.setText(appDetailInfoBto.getApkName());
		setSecurityDes(security, mSecurityText);
		setAdDes(isAd, mAdvertisementText);
		setCharegeDes(charge, mChargeText);
		
		if(official == OFFICAL_VISIBLE) {
			mOffImage.setVisibility(View.VISIBLE);
		}
		showActivityIcon(appDetailInfoBto.getActivity());
		CornerIconInfoBto cornerIconInfo = appDetailInfoBto.getCornerIconInfoBto();
		if(cornerIconInfo!=null && cornerIconInfo.getType() > 0) {
            mCornerImage.setVisibility(View.VISIBLE);
            AsyncImageCache.from(mContext).displayImage(mCornerImage, cornerIconInfo, 1);
        }
		
		long downNum = 0;
		try {
			downNum = Long.parseLong(appDetailInfoBto.getDownNum());
		} catch (Exception e) {
			e.printStackTrace();
		}
		mParticular_info_downtime.setText(getDownNum(mContext, downNum) 
				+ mContext.getString(R.string.download_time) + mContext.getString(R.string.onlydownload));
		mParticular_info_size.setText(MarketUtils.humanReadableByteCount(appDetailInfoBto.getFileSize(), false));
		
		AsyncImageCache.from(mContext).displayImage(
				true,
				mParticular_info_appicon,
				R.drawable.picture_bg1_big,
				new AsyncImageCache.NetworkImageGenerator(
						appDetailInfoBto.getPackageName(), appDetailInfoBto
						.getIconUrl()), true);
		
	}
	
	public void bindIntroductionData(AppDetailInfoBto appDetailInfoBto, String activityUrl, TextView downloadView) {
		
		bindBaseIntroductionData(appDetailInfoBto);
		
		/**	下载按钮	**/
		mDownloadView = downloadView;
		
		if(!TextUtils.isEmpty(activityUrl) && mWebView != null) {
			 mActivityUrl = activityUrl;
			 mWebView.loadUrl(UserInfo.getEncryUrl(mContext, mActivityUrl));
			 mAppDetailJsOperation.setCallBackListener(new CallBack() {
				
				@Override
				public void onJsCall() {
					if(mDownloadView != null) {
						mDownloadView.performClick();
					}
				}
			});
		}
		
		mParticular_info_version.setText(mContext.getString(R.string.version_code_text)
				+ appDetailInfoBto.getVersionName());
		mIntroduce_info_size.setText(mContext.getString(R.string.size)
				+ MarketUtils.humanReadableByteCount(appDetailInfoBto.getFileSize(),false));
		mIntroduce_updatetime.setText(mContext.getString(R.string.detail_app_update_time)
				+ appDetailInfoBto.getUptime());

		String content = appDetailInfoBto.getDesc().trim();
		mParticular_info_introduce.setText(content);
		mParticular_info_introduce.invalidate();
		ViewTreeObserver vto = mParticular_info_introduce.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				mParticular_info_introduce.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mIntroduceLineHeight = mParticular_info_introduce.getLineHeight();
				spreadTextView();
				if (mParticular_info_introduce.getLineCount() <= SHORT_LINE_COUNT) {
					mMoreImage.setVisibility(View.GONE);
					mParticular_info_introduce.setOnClickListener(null);
				}
			}
		});

		String images = appDetailInfoBto.getShotImg();
		String[] image = images.split(",");
		int length = image.length;
		updateDetailsImageView(length);

		for (int i = 0; i < length; i++) {
			AsyncImageCache
			.from(mContext)
			.displayImage(
					true,
					false,
					mImageViews[i],
					-1,
					mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_width),
					mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_height),
					new AsyncImageCache.NetworkImage565Generator(
							MarketUtils.getImgUrlKey(image[i]),
							image[i]), false, false,
							false, appDetailInfoBto.getPackageName());
		}


	}


	public void bindData(AppDetailInfoBto appDetailInfoBto, String activityUrl,TextView downloadView) {
		if(appDetailInfoBto == null) return;
		mIntroductionView.setVisibility(View.VISIBLE);
		bindIntroductionData(appDetailInfoBto, activityUrl, downloadView);
		getRecommentApp(mRefid, appDetailInfoBto.getLabel());
		
	}


	public void updateDetailsImageView(final int count) {
		int i = 0;
		int gap = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_gap);
		int leftMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_margin_left), 
				rigntMargin = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_margin_left);
		ImageView imageView;
		LinearLayout.LayoutParams lp;
		mImageViews = new ImageView[count];
		int width = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_width);
		int height = mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_image_height);
		if (count < 3) {
			mDetailImagesLayout.getLayoutParams().width = DensityUtil.getScreenWidth(mContext);
		}

		try {
			Drawable mBackgroundDrawble = mContext.getResources().getDrawable(R.drawable.app_detail_intr_bg);
			for (i = 0; i < count; i++) {
				imageView = new ImageView(mContext);
				lp = new LinearLayout.LayoutParams(width, height);
				if (i == 0) {
					lp.setMargins(leftMargin, 0, 0, 0);
				} else if (i == count - 1)
					lp.setMargins(gap, 0, rigntMargin, 0);
				else
					lp.setMargins(gap, 0, 0, 0);

				imageView.setLayoutParams(lp);
				imageView.setBackgroundDrawable(mBackgroundDrawble);
				imageView.setDrawingCacheEnabled(false);
				mImageViews[i] = imageView;
				imageView.setTag(imageView.getId(), i);
				mDetailImagesLayout.addView(mImageViews[i]);
				imageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						scaleDetailImage(v, count);
					}
				});
			}

			mBackgroundDrawble = null;
		} catch (OutOfMemoryError e) {
			System.gc();
			e.printStackTrace();
		}

	}


	private void spreadTextView() {
		mIsSpread = true;
		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mParticular_info_introduce
				.getLayoutParams();
		params.height = mIntroduceLineHeight * SHORT_LINE_COUNT;
		mParticular_info_introduce.setLayoutParams(params);
		mMoreImage.setImageResource(R.drawable.desc_more);
		mParticular_info_introduce.setClickable(true);
	}


	private void collapseTextView() {
		mIsSpread = false;
		LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) mParticular_info_introduce
				.getLayoutParams();
		if (mParticular_info_introduce.getLineCount() > SHORT_LINE_COUNT) {
			params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
		} else {
			params.height = mIntroduceLineHeight * SHORT_LINE_COUNT;
		}
		mParticular_info_introduce.setLayoutParams(params);
		mMoreImage.setImageResource(R.drawable.desc_less);
		mMoreImage.setVisibility(View.VISIBLE);
		mParticular_info_introduce.setClickable(true);
	}


	public void getRecommentApp(int refId, String label) {
		GetRecommendAppReq appReq = new GetRecommendAppReq();
		appReq.setLabel(label);
		appReq.setResId(refId);
		String contents = SenderDataProvider.buildToJSONData(
				mContext,
				MessageCode.GET_RECOMMEND_APPS, appReq);
		StartNetReqUtils.execListByPageRequest(mHandler, AppDetailInfoActivity.GET_RECOMMEND_APP, MessageCode.GET_RECOMMEND_APPS, contents);
	}


	private void scaleDetailImage(View clickView, int count) {
		final ArrayList<View> views = new ArrayList<View>();
		final Dialog dialog = new Dialog((Context) mDownloadCallBackInterface.get(), R.style.dialog_transparent_style);
		View view = View.inflate(mContext,R.layout.layout_app_detail_image, null);
		dialog.setContentView(view);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.app_detail_image_pager);
		mIndicatorLayout = (LinearLayout) view.findViewById(R.id.app_detail_image_indicator);
		mIndicatorLayout.removeAllViews();
		views.clear();
		for (int i = 0; i < count; i++) {
			View viewItem = View.inflate(mContext,
					R.layout.layout_app_detail_image_item, null);
			final ImageView imageView = (ImageView) viewItem
					.findViewById(R.id.app_detail_image);
			views.add(viewItem);
			AsyncImageCache.from(mContext).displayImage(
					true,
					false,
					imageView,
					-1,
					mContext.getResources().getDimensionPixelOffset(
							R.dimen.app_detail_image_width),
							mContext.getResources().getDimensionPixelOffset(
									R.dimen.app_detail_image_height),
									new AsyncImageCache.NetworkImage565Generator(mImageViews[i]
											.getTag().toString(), mImageViews[i].getTag()
											.toString()), false, true, false, null);
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					//					imageView.transformOut();
				}
			});
			ImageView indicator = new ImageView(mContext);
			indicator.setImageResource(R.drawable.page_indicator_unfocused);
			indicator.setPadding(10, 0, 0, 0);
			mIndicatorLayout.addView(indicator, i);
		}
		dialog.show();
		//        dialog.getWindow().setWindowAnimations(0); //设置窗口弹出动画

		int index = Integer.parseInt(clickView.getTag(clickView.getId())
				.toString());
		viewPager.setAdapter(new AppDetailPagerAdapter(views, null));
		viewPager.setCurrentItem(index);
		viewPager.setOnPageChangeListener(this);
		updateIndicator(index);
	}


	private void updateIndicator(int index) {
		if (mIndicatorLayout == null)
			return;
		for (int i = 0; i < mIndicatorLayout.getChildCount(); i++) {
			ImageView indicator = (ImageView) mIndicatorLayout.getChildAt(i);
			if (index == i) {
				indicator.setImageResource(R.drawable.page_indicator_focused);
			} else {
				indicator.setImageResource(R.drawable.page_indicator_unfocused);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int position) {

	}


	@Override
	public void onPageScrolled(int position, float arg1, int arg2) {

	}


	@Override
	public void onPageSelected(int position) {
		updateIndicator(position);
	}


	private ArrayList<AppInfoBto> recommendListFiter(List<AppInfoBto> appInfoBtos) {
		int count = 4;
		if (appInfoBtos.size() < 4)
			count = appInfoBtos.size();
		ArrayList<AppInfoBto> appInfoBtosFilter = new ArrayList<AppInfoBto>();
		for (int i = 0; i < count; i++) {
			AppInfoBto appInfo = appInfoBtos.get(i);
			appInfo.setFileSizeToString(MarketUtils
					.humanReadableByteCount(
							appInfo.getFileSize(), false));
			appInfoBtosFilter.add(appInfo);
		}
		return appInfoBtosFilter;
	}


	public void bindRecommentAppData(HashMap<String, Object> map) {

		GetRecommendAppResp AppResp = null;
		List<AppInfoBto> appList = new ArrayList<AppInfoBto>();
		List<AppInfoBto> appLikeList = new ArrayList<AppInfoBto>();
		if (map != null && map.size() > 0) {
			AppResp = (GetRecommendAppResp) map.get("detailRecommendInfo");
			map.clear();
			appList = AppResp.getAppList();
			appLikeList = AppResp.getAppLikeList();
		}
		if (appList != null && appList.size() > 0) {

			final ArrayList<AppInfoBto> recommendationList = recommendListFiter(appList);
			appList.clear();

			if (recommendationList != null) {
				final DetailRecommendView recommendView = new DetailRecommendView(mContext, ReportFlag.FROM_DETAIL_RECOMMEND);
				recommendView.setOnItemClickListener(new DetailRecommendView.OnItemClick() {

					@Override
					public void OnItemClickListener(
							int position) {
						if (null != recommendationList && recommendationList.size() > 0) {
							try {
								AppInfoBto mAppInfoBto = (AppInfoBto) recommendationList.get(position);
								MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, ReportFlag.FROM_DETAIL_RECOMMEND, -1);
								if(mOnStartRecommentAppListener != null) {
									mOnStartRecommentAppListener.onStartRecommentApp();
								}
							} catch (Exception e) {
								return;
							}
						}
					}

				});
				mOtherPeopleText.setVisibility(View.VISIBLE);
				mRecommendation_gridView.addView(recommendView.getView(recommendationList, mDownloadCallBackInterface.get()));
				mRecommendation_gridView.setVisibility(View.VISIBLE);
				mRecommendation_gridView.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.recommend_in));
			}else {
				mOtherPeopleText.setVisibility(View.GONE);
				mRecommendation_gridView.setVisibility(View.GONE);
			}

		} else if (appList != null && appList.size() == 0) {
		}

		if(appLikeList != null && appLikeList.size() > 0) {
			final ArrayList<AppInfoBto> allpeopleLikeList = recommendListFiter(appLikeList);
			appLikeList.clear();
			if (allpeopleLikeList != null) {
				final DetailRecommendView allpeopleLikeView = new DetailRecommendView(mContext, ReportFlag.FROM_DETAIL_ALLLIKE);
				allpeopleLikeView.setOnItemClickListener(new DetailRecommendView.OnItemClick() {

					@Override
					public void OnItemClickListener(
							int position) {
						if (null != allpeopleLikeList && allpeopleLikeList.size() > 0) {
							try {
								AppInfoBto mAppInfoBto = (AppInfoBto) allpeopleLikeList.get(position);
								MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, ReportFlag.FROM_DETAIL_ALLLIKE, -1);
								if(mOnStartRecommentAppListener != null) {
									mOnStartRecommentAppListener.onStartRecommentApp();
								}
							} catch (Exception e) {
								return;
							}
						}
					}

				});
				mAllPeopleLikeText.setVisibility(View.VISIBLE);
				mAllPeopleLikeView.addView(allpeopleLikeView.getView(allpeopleLikeList, mDownloadCallBackInterface.get()));
				mAllPeopleLikeView.setVisibility(View.VISIBLE);
				mAllPeopleLikeView.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.recommend_in));
			} else {
				mAllPeopleLikeText.setVisibility(View.GONE);
				mAllPeopleLikeView.setVisibility(View.GONE);
			}
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.moreImage:
		case R.id.particular_info_introduce:
			if (mParticular_info_introduce != null) {
				if (mIsSpread) {
					collapseTextView();
				} else {
					spreadTextView();
				}
			}
			break;
		default:
			break;
		}
	}


	public void scrollToBottom() {
		mIntroductScrollView.smoothScrollTo(0, mOtherPeopleText.getTop() 
				+ mHorizontalScrollView.getMeasuredHeight() + mHorizontalScrollView.getTop());		
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("deprecation")
	private void webViewSetting() {
		
		mAccountUpdate = new AccountUpdate();
		mAccountUpdate.registerUpdateListener(this);
		
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheMaxSize(1024 * 1024);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
		} else {
			webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		}
		mAppDetailJsOperation = new JavaScriptOperation((Activity)mDownloadCallBackInterface.get(), mWebView, mUploadFlag,Integer.parseInt(mTopicId));
		mWebView.requestLayout();
		mWebView.addJavascriptInterface(mAppDetailJsOperation, "zhuoyou_login");
		mWebView.setWebChromeClient(new WebChromeClient(){

			@Override
			public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
				return super.onConsoleMessage(consoleMessage);
			}

			
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
			}

			});
		
		
		mWebView.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if(view != null && view.getVisibility() != View.VISIBLE)
					view.setVisibility(View.VISIBLE);
				mWebLoadTime = 0;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				if(mWebLoadTime < mWebLoadMaxTime) {
					view.loadUrl(failingUrl);
					mWebLoadTime ++;
				}
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			@Override
			public void onScaleChanged(WebView view, float oldScale,
					float newScale) {
				super.onScaleChanged(view, oldScale, newScale);
			}});
		
		
		mWebView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
	}
	
	
	protected void onDestory() {
		if (mWebView != null) {
			mWebView.stopLoading();
			mWebView.removeAllViews();
			mWebView.destroy();
			mWebView = null;
		}
		mAccountUpdate.unregisterUpdateListener();
	}
	
	
	public void setSecurityDes(int type, TextView textView) {
		switch (type) {
		case 0:
			textView.setVisibility(View.GONE);
			break;
		case 1:
			textView.setText(" "+ mContext.getResources().getString(R.string.app_detail_security));
			textView.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.mark_check), null, null, null);
		default:
			break;
		}
	}


	public void setCharegeDes(int type, TextView textView) {
		switch (type) {
		case 0:
			textView.setText(" "+ mContext.getResources().getString(R.string.app_detail_free));
			textView.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.mark_charge), null, null, null);
			break;
		case 1:
			textView.setVisibility(View.GONE);
			break;
		default:
		}
	}


	public void setAdDes(int type, TextView textView) {
		switch (type) {
		case 0:
			textView.setText(" "+ mContext.getResources().getString(R.string.app_detail_no_ad));
			textView.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.mark_ad), null, null, null);
			break;
		case 1:
			textView.setVisibility(View.GONE);
			break;
		default:
		}
	}
	
	
	private StringBuffer getDownNum(Context context, long count) {
		StringBuffer count_string = new StringBuffer();
		if (count < 100000) {
			count_string.append(count);
		} else {
			if (count > 600000)
				count_string.append(">100").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 500000)
				count_string.append(">50").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 300000)
				count_string.append(">30").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 200000)
				count_string.append(">20").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count >= 100000)
				count_string.append(">10").append(
						context.getResources().getString(R.string.ten_thousand));
			else
				count_string.append(count);

		}
		return count_string;
	}
	
	
	protected Drawable getIconDrawable() {
		return mParticular_info_appicon.getDrawable();
	}


	@Override
	public void onLogin() {
		if(mWebView != null) {
			mWebView.loadUrl(UserInfo.getEncryUrl(mContext, mWebView.getUrl()));
		}
	}


	@Override
	public void onLogout() {
		if(mWebView != null) {
			mWebView.reload();
		}
	}


	protected void onResume() {
		if(mWebView != null) {
			mWebView.loadUrl("javascript:if(typeof(partRefresh)=='function') {partRefresh();}");
		}

	}

}
