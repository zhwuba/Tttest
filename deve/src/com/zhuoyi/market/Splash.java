package com.zhuoyi.market;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.market.account.receiver.AccountLoginReceiver;
import com.market.account.receiver.MyReceiver;
import com.market.account.user.UserInit;
import com.market.behaviorLog.LogSettings;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.common.DownloadSettings;
import com.market.download.updates.AppUpdateManager;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.NotifyUtil;
import com.market.featureOption.FeatureOption;
import com.market.net.DataCodecFactory;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.ChannelInfoBto;
import com.market.net.data.TopicInfoBto;
import com.market.net.request.GetDataStatusReq;
import com.market.net.response.GetMarketFrameResp;
import com.market.net.utils.OpenUrlPostUtils;
import com.market.statistics.ReportFlag;
import com.market.statistics.ReportManager;
import com.market.updateSelf.SelfUpdateManager;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonMainTitleView;
import com.market.view.GuideLayout;
import com.market.view.GuideLayout.OnGuideClickListener;
import com.market.view.PressInstallButtonAnimView;
import com.market.view.StartUpLayout;
import com.zhuoyi.market.appManage.update.AppsUpdateManager;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.appResident.SettingData;
import com.zhuoyi.market.asyncTask.CampaignsTimerTask;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.discovery.DiscoverActivity;
import com.zhuoyi.market.home.HomeActivity;
import com.zhuoyi.market.mine.MarketManageActivity;
import com.zhuoyi.market.necessary.NecessaryFirstInRecommend;
import com.zhuoyi.market.search.ShortCutUtils;
import com.zhuoyi.market.service.AppListenerService;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.InflateTabIndicatorUtils;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.SharePreferenceUtils;
import com.zhuoyi.market.utils.external.ExternalDownloadUtil;
import com.zhuoyi.market.view.CustomViewFactory;

public class Splash extends TabActivity implements Runnable {
    
    public static final String PREFS_NAME = "MyPrefsFile";
    public static final String FIRST_RUN = "first_126";
    public static final String IS_CREATE_SHORT_CUT = "is_create_short_cut";
    public static final String IS_CREATE_TRASH_SHORT_CUT = "is_create_trash_short_cut";
    
    private static Handler mHandler = null;
    private static final int CLOSE_STARUP_UI = 1;
    private static final int SAVE_MODE_TIPS = 2;
    private static final int CONNECT_ERRORS = 3;
    private static final int CONNECT_AGAIN_CHANGE_DOMAIN_NAME = 4;
    private static final int QUERY_GIFT_RECEPTION = 5;
    private static final int DISCOVER_RED_DOT = 6;
    
    //加载页
    private StartUpLayout mStartUpLayout = null;
    private boolean mIsShowLoadingUI = true;
    private boolean isStartUIEnd = false;
    
    //向导页
    private boolean mIsFirst;
    private RelativeLayout mGuideParent = null;
    private GuideLayout mGuideLayout = null;
    
    private boolean mShortCutCreate;
    //第一次进入市场装机推荐
    private NecessaryFirstInRecommend mFirstInInstallRecommend = null;

    //主界面
    public TabHost mHost = null;
    private RelativeLayout mMainLayout = null;
    private boolean isTabLayoutLoadedSuccess = false;

    //获取框架信息
    private Thread mGetServThread = null;
    
    //用户
    private UserInit mUserInit = null;
    
    //自更新
    private SelfUpdateManager mSelfUpdateManager = null;
    
    //省流量模式
    private boolean mSaveModeShowing = false;
    private AlertDialog.Builder dialogBuilder = null;
    
    //双域名切换
    private boolean isTrustedDomainName = true;
    
    //home键监听
    private Intent appListenerIntent;

    //标题及下载动画
    private CommonMainTitleView mCommonMainTitleView = null;
    private static PressInstallButtonAnimView mPressInstallButtonAnimView = null;
    private static int[] mDownloadLocation = {0,0};
    private static int mStatusBarHeight = 0;

    //应用更新start
    private AppsUpdateManager mAppsUpdateManager = null;
    private int mUpdateCount = 0;
    private TextView mAppsUpdateCount = null;
    public final static String APPS_UPDATE = "com.zhuoyi.market.apps.update";
    public final static String CLEAR_NOTIFY = "com.zhuoyi.market.clear.notify";
    private boolean isRegistered = false;
    
    private MyReceiver mCheckInReceiver = null;
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            if (arg1.getAction().equals(APPS_UPDATE)) {
                mUpdateCount = arg1.getIntExtra("update_count", 0);
                setUpdateCount(mUpdateCount);
            }
        }

    };
    
    /**
     * <code>notifyReceiver</code> - {发现红点}.
     */
    private BroadcastReceiver notifyReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            clearDiscoverRedDot();
        }
    };
    
    /**
     * <code>clearDot</code> - {垃圾清理红点}.
     */
    private BroadcastReceiver clearDotReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            getTabWidget().getChildAt(4).findViewById(R.id.new_notify).setVisibility(View.GONE);
        }

    };
    
    /**
     * 应用更新红点
     * @param visibility
     * @param count
     */
    private void setUpdateCount(int count) {

        if (mAppsUpdateCount == null)
            return;
        if (isStartUIEnd && count > 0) {
			mAppsUpdateCount.setText("" + count);
            mAppsUpdateCount.setVisibility(View.VISIBLE);
        	getTabWidget().getChildAt(4).findViewById(R.id.new_notify).setVisibility(View.GONE);
        } else if (!SharePreferenceUtils.hasCleardMobile()) { 
            mAppsUpdateCount.setVisibility(View.GONE);
        	getTabWidget().getChildAt(4).findViewById(R.id.new_notify).setVisibility(View.VISIBLE);
        } else {
            mAppsUpdateCount.setVisibility(View.GONE);
        	getTabWidget().getChildAt(4).findViewById(R.id.new_notify).setVisibility(View.GONE);
        }
    }

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MarketApplication.splashCount++;
        MarketUtils.mRedDotCount = 0;
        if(!Constant.checkMarketSign(getApplicationContext())) {
        	finish();
        }
        IntentFilter notifyfilter = new IntentFilter(CLEAR_NOTIFY);
        registerReceiver(notifyReceiver, notifyfilter);
        IntentFilter clearDotFilter = new IntentFilter("com.zhuoyi.removeclearnotify");
        registerReceiver(clearDotReceiver, clearDotFilter);
        //是否显示加载广告页
        if(savedInstanceState != null) {
        	mIsShowLoadingUI = savedInstanceState.getBoolean("showLoadingUI");
        } else {
        	mIsShowLoadingUI = getIntent().getBooleanExtra("showLoadingUI", true);
        }
        
        MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.main);

        //关闭市场
        Intent intent = getIntent();
        if(intent != null){
            boolean isClose = intent.getBooleanExtra("isClose", false);
            if(isClose){
                MarketApplication.getInstance().applicationExit();
                this.finish();
                return;
            }
        }
        
        NotifyUtil.mReqUpdateTime = System.currentTimeMillis();
        
        //初始化布局
        findViews();
        
        //启动home键监听服务
        if (android.os.Build.VERSION.SDK_INT < 14) {
            startHomeKeyListenerService();
        }
        
        //注册广播
        mCommonMainTitleView.registeredReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(APPS_UPDATE);
        this.registerReceiver(mBroadcastReceiver, filter);
        isRegistered = true;
        
        registerAccountCheckInReceiver();
        
        //初始化用户、自更新、应用更新
        mUserInit = new UserInit(this, false);
        mSelfUpdateManager= new SelfUpdateManager(this);
        mAppsUpdateManager = new AppsUpdateManager(this);

        //初始化设置数据、初始化handler、dialog
        initHandle();
        dialogBuilder = new AlertDialog.Builder(this);
        
        //双域名切换
        isTrustedDomainName = true;
        Constant.setDomainName(true);
        
        //获取框架信息
        mGetServThread = new Thread(this);
        mGetServThread.start();   
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mShortCutCreate = settings.getBoolean(IS_CREATE_SHORT_CUT, false);
        if (!mShortCutCreate) {
            ShortCutUtils.createShortCut(this);
            settings.edit().putBoolean(IS_CREATE_SHORT_CUT, true).commit();
        }
        if (!settings.getBoolean(IS_CREATE_TRASH_SHORT_CUT, false)) {
            ShortCutUtils.createTrashCleanShortCut(this);
            settings.edit().putBoolean(IS_CREATE_TRASH_SHORT_CUT, true).commit();
        }
        
    }
    
    
    	private void registerAccountCheckInReceiver() {
    		try {
    		    mCheckInReceiver = new MyReceiver();
    			final IntentFilter filter = new IntentFilter();
    			filter.addAction("zhuoyou.android.account.USER_CHECK_IN");
    			registerReceiver(mCheckInReceiver, filter);
    		} catch (Exception e) {
    		    mCheckInReceiver = null;
    			e.printStackTrace();
    		}
    	}
    
    
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        
        if(hasFocus) {
            CommonMainTitleView.setNeedHotWordChange(true);
            mCommonMainTitleView.setDownloadStatus();
        } else {
            CommonMainTitleView.setNeedHotWordChange(false);
        }

        if(mDownloadLocation[0] == 0 || mDownloadLocation[1] == 0){
            Rect frame = new Rect();  
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
            mStatusBarHeight = frame.top; 
            int downloadWidth = mCommonMainTitleView.getDownloadWidth();
            int downloadHeight = mCommonMainTitleView.getDownloadHeight();

            mDownloadLocation = mCommonMainTitleView.getDownloadLocation();
            mDownloadLocation[0] = mDownloadLocation[0] - downloadWidth/4;
            mDownloadLocation[1] = mDownloadLocation[1] - downloadHeight/2;
        }
        
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        boolean isClose = intent.getBooleanExtra("isClose", false);
        if(isClose){
            this.finish();
        }
    }
    
    
    @Override
    protected void onResume() {
        super.onResume();
        if(isStartUIEnd)
        	CommonLoadingManager.get().showLoadingAnimation(this);
        if(mPressInstallButtonAnimView == null) {
            mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);
        }
        if(mUserInit != null) {
        	mUserInit.checkIn();
        }
        mSelfUpdateManager.selfUpdateRequest(SelfUpdateManager.SELF_UPDATE_REQ_FROM_SPLASH_RESUME);
        mCommonMainTitleView.setTitleLogo();
    }

    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showLoadingUI", false);
    }
    
    
    @Override
    public void onDestroy() {
        MarketApplication.splashCount--;
        if (mFirstInInstallRecommend != null)
            mFirstInInstallRecommend.setSplashDestroy();

        if (mCommonMainTitleView != null) {
            mCommonMainTitleView.unRegisteredReceiver();
            mCommonMainTitleView.releaseRes();
            mCommonMainTitleView = null;
        }
        
        if (isRegistered && mBroadcastReceiver != null) {
            this.unregisterReceiver(mBroadcastReceiver);
            isRegistered = false;
        }
        
        if (mUserInit != null) {
            mUserInit.releaseRes();
            mUserInit = null;
        }
        
        if (mAppsUpdateManager != null) {
            mAppsUpdateManager.releaseRes();
            mAppsUpdateManager = null;
        }
        
        if (mSelfUpdateManager != null) {
            mSelfUpdateManager.releaseRes();
            mSelfUpdateManager = null;
        }
        
        if (mStartUpLayout != null) {
            mStartUpLayout.releaseRes();
            mStartUpLayout = null;
        }
        
        AccountLoginReceiver.clearBufferLogoUrl();
        stopHomeKeyListenerService();

        if (mGetServThread != null && mGetServThread.isAlive()) {
            mGetServThread.interrupt();
            mGetServThread = null;
        }

        mHandler = null;
        dialogBuilder = null;
       
        if(mPressInstallButtonAnimView!=null)
            mPressInstallButtonAnimView = null;

        if (MarketApplication.splashCount == 0) {
            MarketApplication.clearAppUpdateList();
        	DownloadManager.startServiceReportOffLineLog(getApplicationContext(), ReportFlag.ACTION_EXIST_MARKET, "");
		}
        
        if (mCheckInReceiver != null) {
            unregisterReceiver(mCheckInReceiver);
        }
        if (notifyReceiver != null) {
            unregisterReceiver(notifyReceiver);
        }
        if (clearDotReceiver != null) {
            unregisterReceiver(clearDotReceiver);
        }
        AsyncImageCache.from(getApplicationContext()).releaseRes();
        
        super.onDestroy();
    }    
    

    /**
     * 获取handler
     * @return
     */
    public static Handler getHandler() {
        return mHandler;
    }
    
    /**
     * 初始化handler
     */
    private void initHandle() {
        if(mHandler != null) {
        	finish();
        	return; 
        }
        
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case CLOSE_STARUP_UI:
                    if (mStartUpLayout != null && mStartUpLayout.getVisibility()==View.VISIBLE) {
                        mStartUpLayout.setVisibility(View.GONE);
                        mStartUpLayout.releaseRes();
                        UserLogSDK.entryAdExit(MarketApplication.getRootContext().getApplicationContext());
                    }
                    isStartUIEnd = true;
                    if (mUserInit != null) {
                        mUserInit.setSplashInitFinish(true);
                    }
                    if (MarketApplication.getAppUpdateList() != null) {
                        setUpdateCount(MarketApplication.getAppUpdateList().size() - AppUpdateManager.getUpdateIgnoreList().size());
                    } else {
                        setUpdateCount(mUpdateCount);
                    }
                    
                    if (mMainLayout != null) {
                        mMainLayout.setVisibility(View.VISIBLE);
                        
                        startCampaignsQueryTask();
                        sendEmptyMessage(SAVE_MODE_TIPS);
                        
                        if (mSelfUpdateManager != null)
                            mSelfUpdateManager.selfUpdateRequest(SelfUpdateManager.SELF_UPDATE_REQ_FROM_SPLASH_CREATE);
                        
                        if (mAppsUpdateManager != null)
                            mAppsUpdateManager.requstUpdateAppsMessage();
                    }
                    
                    if(mIsFirst && mFirstInInstallRecommend != null) {
                        mFirstInInstallRecommend.startInstalledNecessaryFirstTimeFromMessage();
                    }
                    if(mIsFirst && FeatureOption.DOWNLOAD_FROM_THIRD_PARTY) {
                    	new ExternalDownloadUtil(getApplicationContext()).downloadAppByKey();
                    }
                    break;
                case SAVE_MODE_TIPS:
                	ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                	NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                	if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI){
                	    MarketUtils.setWifiState(true);
                	} else {
                	    MarketUtils.setWifiState(false);
                		if (!SettingData.mNoShowImage){
                			showEntrySaveMode();
                		}
					}
                    break;
                case CONNECT_ERRORS:
                    showConnectErrors();
                    break;
                case CONNECT_AGAIN_CHANGE_DOMAIN_NAME:
                    isTrustedDomainName = false;
                    Constant.setDomainName(false);
                    mGetServThread = null;
                    mGetServThread = new Thread(Splash.this);
                    mGetServThread.start();
                    break;
                case QUERY_GIFT_RECEPTION:
                	NotifyUtil.campaignsSendNotification(getBaseContext(), 1);
                	break;
                case DISCOVER_RED_DOT:
                    setRedDot(MarketApplication.getMarketFrameResp());
                    break;
                default:
                    break;
                }
            }
        };
    }
    
    
    /**
     * 初始化布局
     */
    private void findViews() {
        mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        mAppsUpdateCount = (TextView) findViewById(R.id.update_count);
        mCommonMainTitleView = (CommonMainTitleView) findViewById(R.id.common_title);
        mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);
        initGuideAndStartUpUI();
        initTabView();
    }
    
    
    /**
     * 初始化向导页和启动广告页
     */
    private void initGuideAndStartUpUI() {
        mGuideParent = (RelativeLayout) findViewById(R.id.welcome);
        mStartUpLayout = (StartUpLayout) findViewById(R.id.startup);
        mStartUpLayout.updateStartUpImage();
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mIsFirst = settings.getBoolean(FIRST_RUN, true);
        
        if (mIsFirst) {
            mFirstInInstallRecommend = new NecessaryFirstInRecommend(this);
            
            if (!FeatureOption.SHOW_GUIDE_FIRST) {
                Editor editor = settings.edit();
                editor.putBoolean(FIRST_RUN, false);
                editor.commit();
            }
        }
        
        if (mIsFirst && FeatureOption.SHOW_GUIDE_FIRST) {
            // guide init
            mGuideLayout = new GuideLayout (this);
            mGuideLayout.setOnGuideClickListener(new OnGuideClickListener(){

                @Override
                public void onClick() {
                    // TODO Auto-generated method stub
                    mGuideParent.setVisibility(View.GONE);
                    if (mGuideParent != null) {
                        mGuideParent.removeView(mGuideLayout);
                        mGuideLayout.releaseRes();
                        mGuideLayout.setOnGuideClickListener(null);
                        mGuideLayout = null;
                    }
                    mStartUpLayout.initStartupLayout();
                    mStartUpLayout.setVisibility(View.VISIBLE);
                    mMainLayout.setVisibility(View.INVISIBLE);
                    
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    Editor editor = settings.edit();
                    editor.putBoolean(FIRST_RUN, false);
                    editor.commit();
                    
                    if(isTabLayoutLoadedSuccess){
                    	if (mHandler != null)
                    		mHandler.sendEmptyMessageDelayed(CLOSE_STARUP_UI, 2000);
                    }
                }
                
            });
            mGuideParent.addView(mGuideLayout);
            mGuideParent.setVisibility(View.VISIBLE);
            mStartUpLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
        }else if(mIsShowLoadingUI){
            mStartUpLayout.initStartupLayout();
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    
    /**
     * 初始化tab
     */
    private void initTabView(){
        mHost = getTabHost();
        int[] viewTypes = null;

        TabSpec ts1 = mHost.newTabSpec("home");
        ts1.setIndicator(InflateTabIndicatorUtils.getTabIndicatorView(this, R.drawable.tab_recommend_selector, R.string.home_apps_first_page));
        ts1.setContent(new Intent(Splash.this, HomeActivity.class));
        mHost.addTab(ts1);   

        TabSpec ts2 = mHost.newTabSpec("game");
        ts2.setIndicator(InflateTabIndicatorUtils.getTabIndicatorView(this, R.drawable.tab_game_selector, R.string.home_apps_game));
        Intent intent = new Intent(Splash.this, MarketSoftGameActivity.class);
        viewTypes = new int[]{CustomViewFactory.VIEW_GAME_RECOMMEND,CustomViewFactory.VIEW_GAME_CATEGORY,CustomViewFactory.VIEW_GAME_GIFT,CustomViewFactory.VIEW_GAME_RANK}; 
        intent.putExtra("viewTypes", viewTypes);
        ts2.setContent(intent);
        mHost.addTab(ts2);  
        
        TabSpec ts3 = mHost.newTabSpec("software");
        ts3.setIndicator(InflateTabIndicatorUtils.getTabIndicatorView(this, R.drawable.tab_software_selector, R.string.home_apps_sw));
        intent = new Intent(Splash.this, MarketSoftGameActivity.class);
        viewTypes = new int[]{CustomViewFactory.VIEW_SOFT_RECOMMEND,CustomViewFactory.VIEW_SOFT_CATEGORY,CustomViewFactory.VIEW_SOFT_NEW,CustomViewFactory.VIEW_SOFT_RANK}; 
        intent.putExtra("viewTypes", viewTypes);
        ts3.setContent(intent);
        mHost.addTab(ts3);  

        TabSpec ts4 = mHost.newTabSpec("discover");
        ts4.setIndicator(InflateTabIndicatorUtils.getTabIndicatorView(this, R.drawable.tab_discover_selector, R.string.home_apps_discover));
        ts4.setContent(new Intent(Splash.this, DiscoverActivity.class));
        mHost.addTab(ts4);
 
        TabSpec ts5 = mHost.newTabSpec("myself");
        ts5.setIndicator(InflateTabIndicatorUtils.getTabIndicatorView(this, R.drawable.tab_me_selector, R.string.home_apps_manage));
        ts5.setContent(new Intent(Splash.this, MarketManageActivity.class));
        mHost.addTab(ts5);
        
        mHost.setOnTabChangedListener(new OnTabChangeListener(){

            @Override
            public void onTabChanged(String tabId) {

            	Activity activity = getLocalActivityManager().getActivity(tabId);  
            	if (activity != null) {  
            		activity.onWindowFocusChanged(true);  
            	} 
            	
            	if (!"home".equals(tabId)) {
            	    try {
            	        Activity home = getLocalActivityManager().getActivity("home");  
                        if (home != null) {  
                            home.onWindowFocusChanged(false);  
                        }
            	    } catch (Exception e) {
            	       e.printStackTrace();
            	    }
            	}

            }
            
        });
    }
    
    
    /**
     * {发现栏目有更新时显示红点}.
     */
    protected void showDiscoverRedDot() {
        if (getTabWidget().getTabCount()>4) {
            View view = getTabWidget().getChildAt(3);
            ImageView iv = (ImageView) view.findViewById(R.id.new_notify);
            if (iv.getVisibility() == View.GONE) {
                iv.setVisibility(View.VISIBLE);
            }
        }
    }
    
    
    /**
     * {清楚发现栏目红点提示}.
     */
    protected void clearDiscoverRedDot() {
        if (getTabWidget().getTabCount()>4) {
            View view = getTabWidget().getChildAt(3);
            ImageView iv = (ImageView) view.findViewById(R.id.new_notify);
            if (iv.getVisibility() == View.VISIBLE) {
                iv.setVisibility(View.GONE);
            }
        }
    }
    
    
    /**
     * 启动home键监听服务
     */
    private void startHomeKeyListenerService(){
        appListenerIntent = new Intent(this, AppListenerService.class);
        startService(appListenerIntent);
    }
    

    /**
     * 注销home监听服务
     */
    private void stopHomeKeyListenerService(){
        if(appListenerIntent != null){
            stopService(appListenerIntent);
        }
    }

    
    /**
     * 提醒用户开启省流量模式
     */
    private void showEntrySaveMode() {
        int saveModeTipCount = MarketUtils.getSaveModeTipCount(this);
        if (saveModeTipCount>2) {
			return;
		}else {
			saveModeTipCount++;
			MarketUtils.setSaveModeTipCount(this,saveModeTipCount);
		}
        
        if(mSaveModeShowing || dialogBuilder==null || Splash.this.isFinishing())
            return;
        mSaveModeShowing = true;
        dialogBuilder.setMessage(R.string.entry_save_mode)
                .setCancelable(false)
                .setTitle(R.string.notification_soft_update_title)
                .setPositiveButton(R.string.dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SettingData.setNoShowImage(getBaseContext(), true);
                                MarketUtils.setSaveModeTipCount(Splash.this,0);
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }

    
    /**
     * 联网失败，提示用户
     */
    private void showConnectErrors() {
        if(dialogBuilder == null || Splash.this.isFinishing()) return;
        dialogBuilder.setMessage(R.string.launch_connect_errors)
                .setCancelable(false)
                .setTitle(R.string.notification_soft_update_title)
                .setPositiveButton(R.string.retry,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                isTrustedDomainName = true;
                                Constant.setDomainName(true);
                                mGetServThread = null;
                                mGetServThread = new Thread(Splash.this);
                                mGetServThread.start();
                            }
                        })
                .setNegativeButton(R.string.dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                            }
                        });
        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
    
  
    @Override
    public void run() {
        Context context = getBaseContext();
        try {
            if (getMarketFrameInfo(Constant.MARKET_URL)) {
                sendBrocastToHome(HomeActivity.START_HOME_ACTION);
                if (mIsFirst && mFirstInInstallRecommend != null) {
                    mFirstInInstallRecommend.startRequestInstallAppFromMessage();
                }
                //设置发现小红点
                if(mHandler!=null)
                    mHandler.sendEmptyMessageDelayed(DISCOVER_RED_DOT, 200);
            }
            //销量统计
//            String sale_info = MarketUtils.getSharedPreferencesString(context, "sales_info", "-1");
//            if (!sale_info.equals("1") && postSaleTotals(Constant.TOTAL_URL)) {
//                MarketUtils.setSharedPreferencesString(context, "sales_info", "1");
//            }
        } catch (Exception e) {
        }
        
        if (!isTabLayoutLoadedSuccess) {
            if(isTrustedDomainName){
                if(mHandler!=null)
                    mHandler.sendEmptyMessageDelayed(CONNECT_AGAIN_CHANGE_DOMAIN_NAME, 200);
            }else{
                if(mHandler!=null)
                    mHandler.sendEmptyMessageDelayed(CONNECT_ERRORS, 200);
            }
        } else {
        	ReportManager.recordEntryMarketTime(getApplicationContext());
            try {
                String installedApkName = "";
                String name = "";
                List<PackageInfo> packages = Splash.this.getPackageManager().getInstalledPackages(0);
                for(int i=0;i<packages.size();i++) { 
                    name = packages.get(i).packageName;
                    if(!installedApkName.contains(name)){
                        installedApkName = name + ";" + installedApkName; 
                    }
                } 
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                Editor editor = settings.edit();
                editor.putString("installed_apk_name", installedApkName);
                editor.commit();
            } catch (Exception e) {
            }
        }
    }
    

    /**
     * 获取框架信息
     * @param url
     * @param isMaxGap
     * @return
     */
    private boolean getMarketFrameInfo(String url) {
        boolean isSuccessed = false;
        String contents = "";
        String result = "";
        HashMap<String, Object> map = null;
        GetMarketFrameResp marketFrameResp = null;
        try {
            contents = SenderDataProvider.buildToJSONData(getApplicationContext(), MessageCode.GET_MARKET_FRAME, null);
            result = OpenUrlPostUtils.accessNetworkByPost(url, contents, false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return isSuccessed;
        }
        map = (HashMap<String, Object>) DataCodecFactory.fetchDataCodec(MessageCode.GET_MARKET_FRAME).splitMySelfData(result);
        if (map != null) {
            marketFrameResp = (GetMarketFrameResp) map.get("marketFrame");
        }else {
            if(isTrustedDomainName) {
                return isSuccessed;
            }else{
                GetMarketFrameResp frameResp = (GetMarketFrameResp) FrameInfoCache.getFrameInfoFromStorage("marketframe");
                if(frameResp != null) {
                    Constant.setDomainName(true);
                    marketFrameResp = frameResp;
                }
            }
        }
        
        if (marketFrameResp != null) {
            MarketApplication.setMarketFrameResp(marketFrameResp);
            isSuccessed = true;
            isTabLayoutLoadedSuccess = true;
            
            FrameInfoCache.saveFrameInfoToStorage(marketFrameResp, "marketframe");
            mCommonMainTitleView.startHotWordsRolling(marketFrameResp.getHotSearchList());

            MarketUtils.setSharedPreferencesString(getBaseContext(), MarketUtils.KEY_MARKET_ID, marketFrameResp.getMarketId());
            
            //保存退出市场时显示的字串
            MarketUtils.setSharedPreferencesString(getBaseContext(), MarketUtils.KEY_EXIT_MARKET, marketFrameResp.getExitTip());
            // yphuang add for auto download and install update
            DownloadSettings.setUpdateAutoFlag(getBaseContext(), marketFrameResp.getIsForcedUp());

            //for record user behavior log
            LogSettings.setLogSwitch(getBaseContext(), marketFrameResp.getLogSwitch());
            
            if(mGuideLayout == null || mGuideLayout.getVisibility() != View.VISIBLE){
                mHandler.sendEmptyMessageDelayed(CLOSE_STARUP_UI, 1000);
            }
        }
        return isSuccessed;
    }

    public void setRedDot(GetMarketFrameResp marketFrameResp){
        if (marketFrameResp == null || marketFrameResp.getChannelList() == null) {
            return;
        }
        for (ChannelInfoBto channelInfoBto : marketFrameResp.getChannelList()) {
            if (channelInfoBto == null) {
                continue;
            }
            if (channelInfoBto.getChannelId().equals("12")) {
                List<TopicInfoBto> topicList = channelInfoBto.getTopicList();
                for (int i = 0; i < topicList.size(); i++) {
                    checkIsDotShow(topicList.get(i));
                }
                if (MarketUtils.mRedDotCount>0) {
                    showDiscoverRedDot();
                }
                return;
            }
        }
    }
    

    public void checkIsDotShow(TopicInfoBto topicInfoBto){
        if (topicInfoBto==null) {
            return;
        }
        checkExistDot(topicInfoBto,String.valueOf(topicInfoBto.getTopicId()),topicInfoBto.getTopicId()+"show");
    }


    private void checkExistDot(TopicInfoBto topicInfoBto,String key1,String key2) {
        int redDot = MarketUtils.getDiscoverSPInt(this, key1, -1);
        if (redDot == -1) { //还没有存过数据
            if (topicInfoBto.getRedDot()==0) {//0表示不需要显示红点
                MarketUtils.setDiscoverSPInt(this, key1, topicInfoBto.getRedDot());
                MarketUtils.setDiscoverSPBoolean(this, key2, false);
                return;
            }
            MarketUtils.setDiscoverSPInt(this, key1, topicInfoBto.getRedDot());
            MarketUtils.setDiscoverSPBoolean(this, key2, true);
            MarketUtils.mRedDotCount++;
            return;
        }
        if (topicInfoBto.getRedDot() != redDot) { //有更新数据
            MarketUtils.setDiscoverSPInt(this, key1, topicInfoBto.getRedDot());
            MarketUtils.setDiscoverSPBoolean(this, key2, true);
            MarketUtils.mRedDotCount++;
            return;
        }
        if (MarketUtils.getDiscoverSPBoolean(this, key2, false)) { //无更新数据，但是之前未查看过数据
            MarketUtils.mRedDotCount++;
            return;
        }
    }
    
    
    /**
     * 销量统计
     * @param url
     * @return
     */
    private boolean postSaleTotals(String url) {
        boolean isSuccessed = false;
        String contents = "";
        String result = "";
        try {
            contents = SenderDataProvider.buildToJSONData(getBaseContext(),
                    MessageCode.GET_DATA_STAUS_REQ, new GetDataStatusReq());
            result = OpenUrlPostUtils.accessNetworkByPost(url, contents, false);

            JSONObject jsonObject;
            String bodyResult = "";

            if (TextUtils.isEmpty(result))
                return isSuccessed;
            jsonObject = new JSONObject(result);
            bodyResult = jsonObject.getString("body");

            jsonObject = null;
            jsonObject = new JSONObject(bodyResult);

            if (jsonObject.has("errorCode") && jsonObject.getInt("errorCode") == 0)
                isSuccessed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccessed;
    }

    /**
     * 注册广播：告诉其它界面初始化完成
     * @param action
     */
    public void sendBrocastToHome(String action) {
        Intent totalIntent = new Intent(action);
        sendBroadcast(totalIntent);
    }
    
    
    /**
     * 下载动画
     * @param pacName
     * @param versionCode
     * @param drawable
     * @param fromX
     * @param fromY
     */
    public static void startDownloadAnim(String pacName, int versionCode,
            Drawable drawable, int fromX, int fromY){
        if(mPressInstallButtonAnimView != null)
            mPressInstallButtonAnimView.startDownloadAnim(
                pacName, 
                versionCode, 
                drawable, 
                fromX, 
                mDownloadLocation[0], 
                (fromY-mStatusBarHeight), 
                mDownloadLocation[1]);
    }
    
    
    /**
     * 查询奖品发放
     */
    private void startCampaignsQueryTask() {
    	CampaignsTimerTask campaignsTimerTask = new CampaignsTimerTask(getApplicationContext(), mHandler, QUERY_GIFT_RECEPTION);
    	Timer timer = new Timer();
    	timer.schedule(campaignsTimerTask, 2000);
    }
    
    
    @Override
    public void onBackPressed() {
    	Activity activity = null;
    	if(mHost != null)
    		activity = getLocalActivityManager().getActivity(mHost.getCurrentTabTag());  
    	if (activity != null) {  
    		activity.onBackPressed();  
    	} 
    }
}
