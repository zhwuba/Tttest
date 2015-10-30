package com.zhuoyi.market.appdetail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.market.behaviorLog.UserLogSDK;
import com.market.download.common.DownBaseInfo;
import com.market.download.common.DownloadSettings;
import com.market.download.userDownload.DownStorage;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppDetailInfoBto;
import com.market.net.request.GetApkDetailReq;
import com.market.net.response.GetApkDetailResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonTitleDownloadView;
import com.market.view.PagerSlidingTabStrip;
import com.market.view.SearchLoadingLayout;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.zhuoyi.market.R;
import com.zhuoyi.market.ShareAppActivity;
import com.zhuoyi.market.appManage.db.FavoriteDao;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.search.SearchActivity;
import com.zhuoyi.market.utils.MarketUtils;


public class AppDetailView implements OnClickListener{

	protected Context mContext;
	private Handler mHandler;
	
	private final long DEFAULT_DURATION = 400L;
	
	private FavoriteDao mFavoriteDao;
	private AppDetailInfoBto mAppDetailInfoBto;
	
	private String mAppName;
	private String mDownloadUrl = "";
	private String mActivityUrl = "";
	private String mLocalFilePath;
	private String mPackageName;
	private String mVersionCode;
	private String mMd5;
	private String mVersionName;
	private String mIconUrl;
	private ImageView mFavoriteButton, mShareButton;
	private TextView mDownloadToolBarButton;
	private FrameLayout mToolBar;
	private LinearLayout mParticular_info_layout_download,mDownloadToolBarLayout;
	private ProgressBar mProgressbar;
	private TextView mSpeed;
	private TextView mCurrentSizeText;
	private TextView mTotalSizeText;
	private ImageView mLeftDownLoadButton;
	private DownloadEventInfo mEventInfo;
	private FavoriteInfo mFavoriteInfo;
	private LinearLayout mErrorLayout;
	private SearchLoadingLayout mLoadingLayout;
	private RelativeLayout mMainView;
	private Resources mResources;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private ViewPager mViewPager;
	private TextView mBackLayout;
	
	private TextView mCommentBtn;
	
	private boolean mIsFinished = false;
	private boolean mIsAutoDownload = false;
	private final String mFileDir = Constant.download_path;
	private String mInstallingString = "";
	private String mInstallString = "";
	private String mOpenString = "";
	private String mDownloadString = "";
	private int mRefId = -1;
	private GetApkDetailReq mReq = null;
	private String mUploadFlag = "";
	private String mChildDes = null;
	private Drawable mDownBtnPlayDraw;
	private Drawable mDownBtnStopDraw;
	private WeakReference<IAppDetailDownloadCallBack> mDownloadCallBackInterface = null;
	private ImageView mTitleBar;
	public CommonTitleDownloadView mTitleDownload;
	private String mTopicId = ReportFlag.TOPIC_NULL;
	private boolean mIsFirstLoadComment = true;
	//滑动相关
	private int mTabHeight;
	
	private AppDetailCommentView mAppDetailCommentView;
	private AppDetailIntroduceView mAppDetailIntroduceView;
	
	public AppDetailView(Context context, IAppDetailDownloadCallBack downloadCallBackInterface, Handler handler) {
		mContext = context;
		mHandler = handler;
		mDownloadCallBackInterface = new WeakReference<IAppDetailDownloadCallBack>(downloadCallBackInterface);
	}
	
	
	public String getApkDetailLogDes() {
	    if (mAppDetailInfoBto == null) {
	        return null;
	    }
	    
	    String logDes = UserLogSDK.getAppDetailActivityDes(Integer.toString(mRefId), mPackageName, mAppName);
	    
	    return logDes;
	}
	
	
	@Override
	public void onClick(View v) {
		String sdPath1 = MarketUtils.FileManage.getSDPath();
		switch (v.getId()) {
		case R.id.particular_info_detail_download_btn:
			if (mRefId == -1 && TextUtils.isEmpty(mPackageName) || mAppDetailInfoBto == null) {
				Toast.makeText(mContext, mResources.getString(R.string.no_data_errors), Toast.LENGTH_SHORT).show();
				return;
			} else if (TextUtils.isEmpty(sdPath1)) {
				Toast.makeText(mContext, mResources.getString(R.string.no_sd_card), Toast.LENGTH_SHORT).show();
				return;
			} else if (MarketUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext,mResources.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				return;
			} else if (mDownloadToolBarButton.getVisibility() == View.GONE)
				return;
			else if (mDownloadToolBarButton.getTag().equals(mOpenString)) {
			    MarketUtils.openCurrentActivity(mContext, mPackageName);
				return;
			} else if (mDownloadToolBarButton.getTag().equals(mInstallingString)) {
				return;
			}
//			else if (mDownloadToolBarButton.getTag().equals(mInstallString)) {
//                if (TextUtils.isEmpty(mLocalFilePath) && mEventInfo != null) {
//                    mLocalFilePath = mEventInfo.getApkFile().getAbsolutePath();
//                }
//                MarketUtils.AppInfoManager.AppInstall(mLocalFilePath, mContext, mPackageName, mAppName);
//                return;
//            }

			if(MarketUtils.getAPNType(mContext) == -1) {
				AppDetailInfoActivity.showToast(mContext, R.string.update_apk_hint_net_error);
			}else {
				if(mDownloadCallBackInterface != null  && mDownloadCallBackInterface.get() != null) {
					mDownloadCallBackInterface.get().startDownloadApp(mPackageName, mAppName,
							null,
							mAppDetailInfoBto.getMd5(), mDownloadUrl,
							mTopicId, mUploadFlag,
							mAppDetailInfoBto.getVersionCode(), mRefId,
							mAppDetailInfoBto.getFileSize());
					
					mDownloadToolBarLayout.setVisibility(View.GONE);
					addDownloadBar();
					mLeftDownLoadButton.setImageResource(R.drawable.icon_stop_selector);
					mShareButton.setClickable(false);

					if(mViewPager.getCurrentItem() == 0) {
						mAppDetailIntroduceView.scrollToBottom();
					}
				}
			}
			break;
			
		case R.id.particular_info_detail_share_btn:
			getShareableAPPs();
			break;
			
		case R.id.particular_info_detail_favorite_btn:
			if ((mRefId == -1 && TextUtils.isEmpty(mPackageName)) || mAppDetailInfoBto == null) {
				AppDetailInfoActivity.showToast(mContext, R.string.no_data_errors);
			} else if (mFavoriteDao.isHasInfors(mPackageName)) {
				mFavoriteDao.delete(mPackageName);
				mFavoriteButton.setImageResource(R.drawable.app_detail_no_favorite);
				AppDetailInfoActivity.showToast(mContext, R.string.has_canceled_favorites);
			} else {
				mFavoriteInfo = new FavoriteInfo();
				mFavoriteInfo.setUrl(mAppDetailInfoBto.getDownUrl());
				mFavoriteInfo.setAppName(mAppName);
				mFavoriteInfo.setMd5(mMd5);
				mFavoriteInfo.setBitmap(null);
				mFavoriteInfo.setFileSizeSum(MarketUtils.humanReadableByteCount(mAppDetailInfoBto.getFileSize(), false));
				mFavoriteInfo.setLocalFilePath(MarketUtils.FileManage.getSDPath() + mFileDir + mAppName + ".apk");
				mFavoriteInfo.setVersionCode(mVersionCode);
				mFavoriteInfo.setVersionName(mVersionName);
				mFavoriteInfo.setAppPackageName(mPackageName);
				mFavoriteInfo.setAppId(mRefId);
				mFavoriteInfo.setIconUrl(mIconUrl);
				mFavoriteDao.saveInfos(mFavoriteInfo);
				AppDetailInfoActivity.showToast(mContext, R.string.add_favorites_success_tip);
				mFavoriteButton.setImageResource(R.drawable.app_detail_favorite);
			}
			break;

		case R.id.particular_info_detail_comment_btn:
			mAppDetailCommentView.commitComment((Context) mDownloadCallBackInterface.get());
			break;

		case R.id.subtitle_search:
			Intent intent = new Intent(mContext, SearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			break;
			
		case R.id.subtitle_back:
			if(mDownloadCallBackInterface != null && mDownloadCallBackInterface.get() != null) {
				((Activity) mDownloadCallBackInterface.get()).finish();
			}
			break;
		}

	}

	
	public View getView(int refId, String packName, String uploadFlag, String topicId, String activity_url, boolean isAutoDownload) {
		View detailView = View.inflate(mContext, com.zhuoyi.market.R.layout.layout_appdetail, null);
		mFavoriteDao = new FavoriteDao(mContext);
		mRefId = refId;
		mUploadFlag = uploadFlag;
		mTopicId = topicId;
		mActivityUrl = activity_url;
		mIsAutoDownload = isAutoDownload;
		mPackageName = packName;
		initResources(mContext);
		initView(detailView);
//		initScroll();
		
		if(mRefId == -1) {
			getAppDetailInfoByPackageName(mContext, mPackageName);
		} else {
			getAppDetailInfo(mContext);
		}
		
		return detailView;
	}
	

	public void initResources(Context context) {
		mResources = context.getResources();
		mDownloadString = mResources.getString(R.string.download);
		mOpenString = mResources.getString(R.string.open);
		mInstallingString = mResources.getString(R.string.installing);
		mInstallString = mResources.getString(R.string.install);
		mDownBtnStopDraw = mResources.getDrawable(R.drawable.icon_stop_selector);
		mDownBtnPlayDraw = mResources.getDrawable(R.drawable.icon_play_selector);
	}
	
	
	public void initView(View detailView) {
		mTitleDownload = (CommonTitleDownloadView) detailView.findViewById(R.id.title_download);
		mTitleBar = (ImageView) detailView.findViewById(R.id.subtitle_search);
		mBackLayout = (TextView) detailView.findViewById(R.id.subtitle_back);
		mCommentBtn = (TextView) detailView.findViewById(R.id.particular_info_detail_comment_btn);
		
//		mParticularAppDetail = (RelativeLayout) detailView.findViewById(R.id.particular_app_detail_header);
//		mAppDetailLayout = (TouchCallbackLayout) detailView.findViewById(R.id.app_detail_layout);
		mDownloadToolBarButton = (TextView) detailView.findViewById(R.id.particular_info_detail_download_btn);
		mDownloadToolBarButton.setEnabled(false);
		mViewPager = (ViewPager) detailView.findViewById(R.id.app_detail_pager);
		mPagerSlidingTabStrip = (PagerSlidingTabStrip) detailView.findViewById(R.id.app_detail_tabs);
		mPagerSlidingTabStrip.setVisibility(View.GONE);
		mMainView = (RelativeLayout) detailView.findViewById(R.id.main_view);
		addSearchLoadingView();
		
		mToolBar = (FrameLayout) detailView.findViewById(R.id.particular_info_bottom_bar);
		mDownloadToolBarLayout = (LinearLayout) detailView.findViewById(R.id.download_tool_bar);
		mShareButton = (ImageView) detailView.findViewById(R.id.particular_info_detail_share_btn);
		mFavoriteButton = (ImageView) detailView.findViewById(R.id.particular_info_detail_favorite_btn);
		View introductionView = View.inflate(mContext, R.layout.layout_appdetail_introduction, null);
		View ratingView = View.inflate(mContext, R.layout.layout_appdetail_rating, null);
		ArrayList<View> lists = new ArrayList<View>();
		
		lists.add(introductionView);
		lists.add(ratingView);
		
		ArrayList<String> titles = new ArrayList<String>();
		titles.add(mContext.getResources().getString(R.string.app_detail_introduction));
		titles.add(mContext.getResources().getString(R.string.app_detail_comments));
		mViewPager.setDrawingCacheEnabled(false);
		mViewPager.setAdapter(new AppDetailPagerAdapter(lists, titles));
		
		
		mPagerSlidingTabStrip.setViewPager(mViewPager);
		mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);	//未选中字体颜色
		mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);	//指示线
		mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);	//下划线
		mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);	//分割线
		mPagerSlidingTabStrip.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
		mPagerSlidingTabStrip.setIndicatorHeight(mContext.getResources().getDimensionPixelSize(R.dimen.indicator_height));
		mPagerSlidingTabStrip.setShouldExpand(true);
		
		mAppDetailIntroduceView = new AppDetailIntroduceView(mContext, mDownloadCallBackInterface.get(), mRefId, mHandler, mUploadFlag, mTopicId);
		
		mAppDetailIntroduceView.initView(introductionView);
		mAppDetailCommentView = new AppDetailCommentView(mContext, mRefId, mHandler);
		mAppDetailCommentView.initView(ratingView);
		
		mTitleDownload.registeredReceiver();
		initListener();
	}
	
	
	private void addSearchLoadingView() {
		try {
		    if (mLoadingLayout == null) {
		        mLoadingLayout = (SearchLoadingLayout) View.inflate(mContext, R.layout.progress_bar_intern, null);
		        mLoadingLayout.showAnimation();
		        
		        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		        mMainView.addView(mLoadingLayout, params);
		    }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	
	private void removeSearchLoadingView() {
	    if (mLoadingLayout != null) {
	        mLoadingLayout.stopAnimation();
	        mMainView.removeView(mLoadingLayout);
	        mLoadingLayout = null;
	    }
	}
	
	
    private void addErrorView() {
        if (mErrorLayout == null) {
            mErrorLayout = (LinearLayout) View.inflate(mContext, R.layout.layout_network_ungeilivable, null);
            TextView errorText = (TextView) mErrorLayout.findViewById(R.id.conn_no_network);
            errorText.setText(R.string.no_appinfo);
            Button refreshButton = (Button) mErrorLayout.findViewById(R.id.refresh_btn);
            refreshButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (MarketUtils.getAPNType(mContext) != -1) {
                        removeErrorView();
                        addSearchLoadingView();
                        getAppDetailInfo(mContext);
                    } else {
                        AppDetailInfoActivity.showToast(mContext, R.string.no_network);
                    }
                }
                
            });
            
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mMainView.addView(mErrorLayout, params);
        }
    }
    
    
    private void removeErrorView() {
        if (mErrorLayout != null) {
            mMainView.removeView(mErrorLayout);
            mErrorLayout = null;
        }
    }
    
    
    private void addDownloadBar() {
        if (mParticular_info_layout_download == null) {
            
            try {
                mParticular_info_layout_download = (LinearLayout) View.inflate(mContext, R.layout.layout_appdetail_download, null);
            } catch (OutOfMemoryError e) {
            	e.printStackTrace();
            	System.gc();
                mParticular_info_layout_download = (LinearLayout) View.inflate(mContext, R.layout.layout_appdetail_download, null);
            }

            mProgressbar = (ProgressBar) mParticular_info_layout_download.findViewById(R.id.progressbar_id);
            mCurrentSizeText = (TextView) mParticular_info_layout_download.findViewById(R.id.detail_current_size);
            mTotalSizeText = (TextView) mParticular_info_layout_download.findViewById(R.id.detail_total_size);
            mLeftDownLoadButton = (ImageView) mParticular_info_layout_download.findViewById(R.id.particular_info_detail_down_btn);
            mSpeed = (TextView) mParticular_info_layout_download.findViewById(R.id.speed); 
            
            ImageView rightDeleteButton = (ImageView) mParticular_info_layout_download.findViewById(R.id.particular_info_detail_cancal_btn);
            rightDeleteButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mRefId == -1 && TextUtils.isEmpty(mPackageName)) return;
                    mHandler.sendEmptyMessage(AppDetailInfoActivity.CANCEL_DOWNLOAD_APK);
                }
                
            });
            
            mLeftDownLoadButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mRefId == -1 && TextUtils.isEmpty(mPackageName)) return;
                    mEventInfo = DownStorage.getFgDownloadEventInfo(mContext, mPackageName, mAppDetailInfoBto.getVersionCode());
                    if (mEventInfo == null) return;
                    
                    int state = mEventInfo.getCurrState();
                    if (state == DownBaseInfo.STATE_DOWNLOADING || state == DownBaseInfo.STATE_READY) {
                        try {
                            if (mDownloadCallBackInterface != null && mDownloadCallBackInterface.get() != null) {
                                if(mDownloadCallBackInterface.get().downloadPause(mPackageName, Integer.parseInt(mVersionCode))) {
                                    pauseDownloadUI();
                                }
                                
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (state == DownBaseInfo.STATE_DOWNLOAD_PAUSE
                            || state == DownBaseInfo.STATE_DOWNLOAD_FAILED) {
                        
                        String sdPath1 = MarketUtils.FileManage.getSDPath();
                        if (mRefId == -1 && TextUtils.isEmpty(mPackageName) || mAppDetailInfoBto == null) {
                            Toast.makeText(mContext, mResources.getString(R.string.no_data_errors), Toast.LENGTH_SHORT).show();
                        } else if (TextUtils.isEmpty(sdPath1)) {
                            Toast.makeText(mContext, mResources.getString(R.string.no_sd_card), Toast.LENGTH_SHORT).show();
                        } else if (MarketUtils.getAPNType(mContext) == -1) {
                            Toast.makeText(mContext, mResources.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                        } else {
                                if(MarketUtils.getAPNType(mContext) == -1) {
                                    AppDetailInfoActivity.showToast(mContext, R.string.update_apk_hint_net_error);
                                }else {
                                    if (mDownloadCallBackInterface != null && mDownloadCallBackInterface.get() != null) {
                                        if(mDownloadCallBackInterface.get().onResumeDownload(mPackageName, Integer.parseInt(mVersionCode))) {
                                            startDownloadUI();
                                    }
                                }
                            }
                        }
                    }
                }
                
            });

            mToolBar.addView(mParticular_info_layout_download);
        }
    }
    
    
    private void removeDownloadBar() {
        if (mParticular_info_layout_download != null) {
            mToolBar.removeView(mParticular_info_layout_download);
            mParticular_info_layout_download = null;
        }
    }
    
    
    private boolean getDownloadBarShow() {
        if (mParticular_info_layout_download == null) {
            return false;
        } else {
            return true;
        }
    }
	
	
	public void initListener() {
		mTitleBar.setOnClickListener(this);
		mBackLayout.setOnClickListener(this);
		mDownloadToolBarButton.setOnClickListener(this);
		mFavoriteButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);
		mCommentBtn.setOnClickListener(this);
		
		mPagerSlidingTabStrip.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
			    
			    setBottomButton();
				if (position == 1 && mIsFirstLoadComment) {
					mAppDetailCommentView.firstLoadCommont();
					mIsFirstLoadComment = false;
				}
			}


			@Override
			public void onPageScrolled(int position, float arg1,
					int arg2) {

			}


			@Override
			public void onPageScrollStateChanged(int position) {

			}
		});

	}
	
	
	public void bindDetailData(HashMap<String, Object> map) {
		GetApkDetailResp detailResp = null;
		boolean isDownloading = false;
		if (map != null && map.size() > 0) {
			detailResp = (GetApkDetailResp) map.get("detailInfo");
			map.clear();
			mAppDetailInfoBto = detailResp.getAppDetailInfo();
			mIsFinished = true;
		}
		
		if (mAppDetailInfoBto != null) {
			
			mIconUrl = mAppDetailInfoBto.getIconUrl();
			mAppName = mAppDetailInfoBto.getApkName();
			mPackageName = mAppDetailInfoBto.getPackageName();
			mVersionCode = "" + mAppDetailInfoBto.getVersionCode();
			mDownloadUrl = mAppDetailInfoBto.getDownUrl();
			mMd5 = mAppDetailInfoBto.getMd5();
			mVersionName = mAppDetailInfoBto.getVersionName();
			
			mEventInfo = DownloadPool.getAllDownloadEvent(mContext).get(DownStorage.getEventSignal(mPackageName,mAppDetailInfoBto.getVersionCode()));
//			        DownStorage.getFgDownloadEventInfo(mContext, mPackageName,mAppDetailInfoBto.getVersionCode());
			if (mEventInfo != null) {
				if (mEventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOADING) {
					isDownloading = true;
					addDownloadBar();
					if (mLeftDownLoadButton != null)
						mLeftDownLoadButton.setImageResource(R.drawable.icon_stop_selector);
					mDownloadToolBarLayout.setVisibility(View.GONE);
					mShareButton.setClickable(false);
				} else if (mEventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_PAUSE
						|| (mEventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_FAILED)) {
					addDownloadBar();
					if (mLeftDownLoadButton != null)
						mLeftDownLoadButton.setImageResource(R.drawable.icon_play_selector);
					mDownloadToolBarLayout.setVisibility(View.GONE);
					mShareButton.setClickable(false);
				} else {
					setButtonInfomation(mPackageName, mVersionCode);
					mDownloadToolBarLayout.setVisibility(View.VISIBLE);
				}
				
				if (getDownloadBarShow()) {
                    long totalSize = mEventInfo.isDiffPathUpdateNow() ? mEventInfo.getDiffFileSize() : mEventInfo.getTotalSize();
                    float _pecent = (float) mEventInfo.getCurrDownloadSize() / totalSize * 1.0f;
                    int _rate = (int) (_pecent * 100);
                    if (_rate >= 100) _rate = 100;
                    mProgressbar.setProgress(_rate);
                }
				
			} else {
				setButtonInfomation(mPackageName, mVersionCode);
				mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			}
			removeSearchLoadingView();
//			mAppDetailLayout.setVisibility(View.VISIBLE);
			mPagerSlidingTabStrip.setVisibility(View.VISIBLE);
			mDownloadToolBarButton.setEnabled(true);
			
			if (isDownloading == false && mIsAutoDownload 
					&& !(MarketUtils.checkInstalled(mContext, mPackageName)
					&& MarketUtils.isEqualsVersionCode(mContext, mVersionCode, mPackageName))) {
				if (null != mEventInfo) {
				    if (mLeftDownLoadButton != null) {
				        mLeftDownLoadButton.performClick();
					}
				} else {
					mDownloadToolBarButton.performClick();
				}
			}
			if (mFavoriteDao.isHasInfors(mPackageName)) {
				mFavoriteButton.setImageResource(R.drawable.app_detail_favorite);
			}
			
			mAppDetailIntroduceView.bindData(mAppDetailInfoBto, mActivityUrl, mDownloadToolBarButton);
			mAppDetailCommentView.bindData(mAppDetailInfoBto);
			
		} else if (map == null) {
		    removeSearchLoadingView();
		    addErrorView();
			mPagerSlidingTabStrip.setVisibility(View.GONE);
		} else {
		    removeSearchLoadingView();
		    addErrorView();
			mPagerSlidingTabStrip.setVisibility(View.GONE);
		}
	
		//数据获取完成再去刷新
		notifyView();
		
	}
	
	
	public void destoryView() {
		if (mTitleDownload != null) {
			mTitleDownload.unRegisteredReceiver();
		}
		mDownBtnStopDraw.setCallback(null);
		mDownBtnPlayDraw.setCallback(null);
		mAppDetailIntroduceView.onDestory();
	}
	

	public void getAppDetailInfo(Context context) {
		String contents = "";
		mReq = new GetApkDetailReq();
		mReq.setResId(mRefId);
		mReq.setFrom(mUploadFlag);
		mReq.setTopicId(mTopicId);
		String marketId = com.zhuoyi.market.utils.MarketUtils
				.getSharedPreferencesString(context,com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
		if (TextUtils.isEmpty(marketId)) {
			marketId = "null";
		}
		String src = marketId;
		src += "/-1/-1/-1";
		mReq.setSrc(src);
		contents = SenderDataProvider.buildToJSONData(context,
				MessageCode.GET_APK_DETAIL, mReq);
		try {
			StartNetReqUtils.execMarketRequest(mHandler, AppDetailInfoActivity.REFRESH_DETAIL_DATA, MessageCode.GET_APK_DETAIL, contents);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	
	private void getAppDetailInfoByPackageName(Context context, String packageName) {

		String contents = "";
		mReq = new GetApkDetailReq();
		mReq.setPackageName(packageName);
		mReq.setFrom(mUploadFlag);
		mReq.setTopicId(mTopicId);
		String marketId = com.zhuoyi.market.utils.MarketUtils
				.getSharedPreferencesString(context,com.zhuoyi.market.utils.MarketUtils.KEY_MARKET_ID, null);
		if (TextUtils.isEmpty(marketId)) {
			marketId = "null";
		}
		String src = marketId;
		src += "/-1/-1/-1";
		mReq.setSrc(src);
		contents = SenderDataProvider.buildToJSONData(context,
				MessageCode.GET_APK_DETAIL_BY_PACKNAME_REQ, mReq);
		try {
			StartNetReqUtils.execMarketRequest(mHandler, AppDetailInfoActivity.REFRESH_DETAIL_DATA, MessageCode.GET_APK_DETAIL_BY_PACKNAME_REQ, contents);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	
	}
	
	
	
	public void setButtonInfomation(String pName, String versionCode) {
		File file;
		mEventInfo = DownStorage.getFgDownloadEventInfo(mContext, mPackageName, mAppDetailInfoBto.getVersionCode());

		if (TextUtils.isEmpty(pName)) return;
		if (MarketUtils.checkInstalled(mContext, pName)
				&& MarketUtils.isEqualsVersionCode(mContext, versionCode, pName)) {
			mDownloadToolBarButton.setTag(mOpenString);
			mDownloadToolBarButton.setText(R.string.open);
			mDownloadToolBarButton.setEnabled(true);
			mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			removeDownloadBar();
			mShareButton.setClickable(true);
			return;
		}
		if (mEventInfo == null) {
			mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			removeDownloadBar();
			mShareButton.setClickable(true);
			return;
		}
		if (isApkInstalling(pName)) {
			mDownloadToolBarButton.setTag(mInstallingString);
			mDownloadToolBarButton.setText(R.string.installing);
			mDownloadToolBarButton.setEnabled(false);
			mShareButton.setClickable(true);
			return;
		}
		if (isApkUninstall(pName, versionCode)) {
			mLocalFilePath = mEventInfo.getApkFile().getAbsolutePath();
			file = new File(mLocalFilePath);
			if (file.exists()) {
				mDownloadToolBarButton.setTag(mInstallString);
				mDownloadToolBarButton.setEnabled(true);
				mShareButton.setClickable(true);
				mDownloadToolBarButton.setText(R.string.one_key_install2);
			}
		}
		if (mDownloadToolBarButton.getTag().equals(mDownloadString)) {
			if (mEventInfo.getCurrState() == DownBaseInfo.STATE_READY) {
				mDownloadToolBarLayout.setVisibility(View.GONE);
				mShareButton.setClickable(false);
				addDownloadBar();
			} else {
				mDownloadToolBarButton.setText(R.string.one_key_install2);
				mShareButton.setClickable(true);
			}
		}
		if (mEventInfo.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
			mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			removeDownloadBar();
			mShareButton.setClickable(true);
		}
	}
	
	
	public boolean isApkInstalling(String pName) {
		boolean result = false;
		if (mEventInfo.getCurrState() == DownBaseInfo.STATE_INSTALLING) {
			if (DownloadSettings.getBgInstallFlag(mContext))
				result = true;
		}
		return result;
	}


	public boolean isApkUninstall(String pName, String versionCode) {
		boolean result = false;
		if (mEventInfo.getCurrState() == DownBaseInfo.STATE_INSTALL_FAILED
		        || mEventInfo.getCurrState() == DownBaseInfo.STATE_DOWNLOAD_COMPLETE) {
			if (MarketUtils.checkApkShouldShowInList(mContext, pName, Integer.parseInt(mVersionCode)))
				result = true;
		}
		return result;
	}


	public void setBottomButton() {
	    
	    boolean showCommentBtn = false;
	    //已经安装并且在评论页，显示“我来评论”
	    if (mViewPager != null 
	            && mViewPager.getCurrentItem() == 1 
	            && MarketUtils.checkInstalled(mContext , mPackageName)
	            && mAppDetailCommentView.isCommentEnable()) {
	        showCommentBtn = true;
	    }
	    
	    if (showCommentBtn) {
	        mDownloadToolBarButton.setVisibility(View.GONE);
            mCommentBtn.setVisibility(View.VISIBLE); 
	    } else {
	        mDownloadToolBarButton.setVisibility(View.VISIBLE);
            mCommentBtn.setVisibility(View.GONE);
	    }
	}
	
	
	
	
	
	private void getShareableAPPs() {
		Intent intent = new Intent();
		intent.setClass(mContext, ShareAppActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString(ShareAppActivity.INTENT_KEY_SHARE_URL, mAppDetailInfoBto.getDownUrl());
		bundle.putString(ShareAppActivity.INTENT_KEY_APP_NAME, mAppName);
		bundle.putString(ShareAppActivity.INTENT_KEY_ICON_URL, mIconUrl);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getAppBitmap(intent);
		mContext.startActivity(intent);
	}


	private void getAppBitmap(Intent intent) {
		Bitmap bmp = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Drawable tempDrawable = mContext.getResources().getDrawable(R.drawable.picture_bg1_big);
		ConstantState tempState = tempDrawable.getConstantState();
		Drawable drawable = mAppDetailIntroduceView.getIconDrawable();
		if (drawable != null && !drawable.getConstantState().equals(tempState)
				&& drawable instanceof BitmapDrawable) {
			bmp = ((BitmapDrawable) drawable).getBitmap();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
			intent.putExtra("bitmap", baos.toByteArray());
		}
	}
	
	
	public void pauseDownloadUI() {
	    if (getDownloadBarShow()) {
	        mSpeed.setText("");
		    mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
		}
	}


	public void startDownloadUI() {
	    if (getDownloadBarShow()) {
	        mLeftDownLoadButton.setImageDrawable(mDownBtnStopDraw);
	        refreshDownloadProgress();
		}
	}
	
	
	private void refreshDownloadProgress() {
		DownloadEventInfo downInfo = DownStorage.getFgDownloadEventInfo(
				mContext, mPackageName,
				mAppDetailInfoBto.getVersionCode());

		if (downInfo == null) return;

		int int_percent = 0;
		long totalSize = downInfo.isDiffPathUpdateNow() ? downInfo.getDiffFileSize() : downInfo.getTotalSize();
		long compeleteSize = downInfo.getCurrDownloadSize();
		float pecent = (float) compeleteSize / totalSize * 1.0f;

		if (pecent >= 100) {
			int_percent = 100;
		} else {
			int_percent = (int) (pecent * 100);
		}
		if (int_percent == 0) {
			int_percent = 1;
		}
		mCurrentSizeText.setText(MarketUtils.humanReadableByteCount(compeleteSize, false).replaceAll(" ", ""));
		mTotalSizeText.setText("/" + MarketUtils.humanReadableByteCount(totalSize,false).replaceAll(" ", ""));
		mProgressbar.setProgress(int_percent);
	}


	public void notifyView() {
		if (!TextUtils.isEmpty(mPackageName) && !TextUtils.isEmpty(mVersionCode)) {
			setButtonInfomation(mPackageName, mVersionCode);
		}
		if (mEventInfo != null) {
			switch (mEventInfo.getEventArray()) {
			case DownloadEventInfo.ARRAY_PAUSED:
				pauseDownloadUI();
				break;
			case DownloadEventInfo.ARRAY_DOWNLOADING:
				startDownloadUI();
				break;
			}
		}
		mAppDetailIntroduceView.onResume();
		setBottomButton();
	}
		
	
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			mDownloadToolBarButton.setTag(mOpenString);
			mDownloadToolBarButton.setText(R.string.open);
			mDownloadToolBarButton.setEnabled(true);
			mShareButton.setClickable(true);
		}
		setBottomButton();
	}
	
	
	protected void onInstalling(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			mDownloadToolBarButton.setTag(mInstallingString);
			mDownloadToolBarButton.setText(R.string.installing);
			mDownloadToolBarButton.setEnabled(false);
		}
	}
	
	
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			mDownloadToolBarButton.setTag(mInstallString);
			mDownloadToolBarButton.setEnabled(true);
			mShareButton.setEnabled(true);
			mDownloadToolBarButton.setText(R.string.one_key_install2);
		}
	}
	
	
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null&& eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			if (mDownloadToolBarLayout != null)
				mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			mShareButton.setClickable(true);
			removeDownloadBar();
		}

	}
	
	
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
	    
	    if (!getDownloadBarShow()) return;
	    
		float pecent;
		int int_percent = 0;
		long totalSize = eventInfo.isDiffPathUpdateNow() ? eventInfo.getDiffFileSize() : eventInfo.getTotalSize();
		long compeleteSize = eventInfo.getCurrDownloadSize();
		String cur_speed = null;
		if (!mIsFinished) return;
		if (eventInfo == null || mAppDetailInfoBto == null) return;

		if (!eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())	
				|| eventInfo.getVersionCode() != mAppDetailInfoBto.getVersionCode())
			return;
		mLeftDownLoadButton.setImageDrawable(mDownBtnStopDraw);
		/**	
		 * cancelDialog();
		 */
		pecent = (float) compeleteSize / totalSize * 1.0f;
		if (pecent >= 100) {
			int_percent = 100;
		} else {
			int_percent = (int) (pecent * 100);
		}
		if (int_percent == 0)
			int_percent = 1;

		cur_speed = MarketUtils.getCountSpeedInfo(eventInfo.getDownloadSpeed());
		try {
			mCurrentSizeText.setText(MarketUtils.humanReadableByteCount(compeleteSize, false));
			mTotalSizeText.setText("/" + MarketUtils.humanReadableByteCount(totalSize,false).replaceAll(" ", ""));
			mSpeed.setText(cur_speed);
			mProgressbar.setProgress(int_percent);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (int_percent >= 100) {
			// added start by shuaiqing 2012-06-20
			mDownloadToolBarButton.setTag(mInstallingString);
			mDownloadToolBarButton.setEnabled(false);
			// mDownloadToolBarLay.setEnabled(false);
			mShareButton.setClickable(true);
			mDownloadToolBarButton.setText(R.string.installing);
			mLocalFilePath = MarketUtils.FileManage.getSDPath() + mFileDir + mAppName + ".apk";
			mDownloadToolBarLayout.setVisibility(View.VISIBLE);
			removeDownloadBar();
		}
	}
	
	
	protected void onDownloadServiceBind() {
		if (!TextUtils.isEmpty(mPackageName) && !TextUtils.isEmpty(mVersionCode)) {
			setButtonInfomation(mPackageName, mVersionCode);
		}
	}
	
	
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null)
			return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			/**
			 * cancelDialog();
			 */
		    if (getDownloadBarShow()) {
		        mSpeed.setText("");
		        Toast.makeText(mContext, mResources.getString(R.string.no_sd_card), Toast.LENGTH_SHORT).show();
		        mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
		    }
		}
	}
	
	
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		try {
			if (!mIsFinished || mAppDetailInfoBto == null)
				return;
			if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
				/**
				 * cancelDialog();
				 */
			    if (getDownloadBarShow()) {
			        mSpeed.setText("");
			        Toast.makeText(mContext,mResources.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
			        mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
				}
			}
		} catch (Exception e) {
		}
	}
	
	
	protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
		if (eventInfo != null && mAppDetailInfoBto != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			/**
			 * cancelDialog();
			 */
		    if (getDownloadBarShow()) {
		        mSpeed.setText("");
		        Toast.makeText(mContext, mResources.getString(R.string.cardException), Toast.LENGTH_SHORT).show();
		        mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
			}
		}
	}
	
	
	protected void onApkDownloading(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
		    if (mLeftDownLoadButton != null) {
		        mLeftDownLoadButton.setImageDrawable(mDownBtnStopDraw);
			}
		}
	}
	
	
	protected void onDeleteDownload() {
	    if (getDownloadBarShow()) {
    		mProgressbar.setProgress(0);
    		mCurrentSizeText.setText("");
    		mTotalSizeText.setText("");
    		mSpeed.setText("");
		}
		removeDownloadBar();
		
		mDownloadToolBarLayout.setVisibility(View.VISIBLE);
		mShareButton.setClickable(true);
	}
	
	
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		if (!mIsFinished || mAppDetailInfoBto == null) return;
		if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
			/**
			 * cancelDialog();
			 */
		    if (getDownloadBarShow()) {
		        mSpeed.setText("");
		        Toast.makeText(mContext,mResources.getString(R.string.get_data_error),Toast.LENGTH_SHORT).show();
		        mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
			}
		}
	}
	
	
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		try {
			if (!mIsFinished || mAppDetailInfoBto == null) return;
			if (eventInfo != null && eventInfo.getPkgName().equals(mAppDetailInfoBto.getPackageName())) {
				/**
				 * cancelDialog();
				 */
			    if (getDownloadBarShow()) {
			        mSpeed.setText("");
			        mLeftDownLoadButton.setImageDrawable(mDownBtnPlayDraw);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected void onClickDeleteButton() {
		if(mDownloadCallBackInterface.get().onCancelDownload(mPackageName, mAppDetailInfoBto.getVersionCode())) {
			onDeleteDownload();
		}
	}

	public void bindRecommendApp(HashMap<String, Object> map) {
		mAppDetailIntroduceView.bindRecommentAppData(map);
	}

	
	public void addNewComment(HashMap<String, Object> map) {
		mAppDetailCommentView.addNewComment(map);
	}

	public void bindCommentData(HashMap<String, Object> map) {
		mAppDetailCommentView.bindCommentData(map);
	}
	
}
