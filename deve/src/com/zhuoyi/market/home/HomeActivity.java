package com.zhuoyi.market.home;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.market.account.login.BaseHtmlActivity;
import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.data.TopicInfoBto;
import com.market.net.response.GetMarketFrameResp;
import com.market.view.PagerSlidingTabStrip;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.home.HomeAdView.GiftTipCallBack;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.gallery.AsyncImageLoader;

/**
 * 精选界面
 * 
 * @author dream.zhou
 */
public class HomeActivity extends DownloadTabBaseActivity implements GiftTipCallBack, DownloadCallBackInterface, OnPageChangeListener {
    public static final String ACTION_BAR_BACKGROUND_COLOR = "ff7f46";
    public static final String START_HOME_ACTION = "com.zhuoyi.market.start.home";

    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private ImageView mFloatingView = null;
    private MyViewPager mViewPager;
    private HomeFragmentAdapter mViewPagerAdapter;

    private SelectFragment selectFragment;
    private RankListFragment rankListFragment;
    private SpecialFragment specialFragment;
    private NecessaryFragment necessaryFragment;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initView();
            initFloatingViewData();
        }
    };
    
    private BroadcastReceiver exitDialogReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            HomeActivity.this.onBackPressed();
        }

    };
    
    private static final int HANDLER_GET_DRAWABLE_SUCCESS = 0;
    private Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case HANDLER_GET_DRAWABLE_SUCCESS:
    			Bundle bundle = msg.getData();
    			initFloatingView((Drawable) msg.obj, bundle.getString("webUrl"));
    			break;
    		}
    	}
    };
    

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.common_home_layout);
        View baseView = findViewById(R.id.base_layout);
        MarketUtils.setBaseLayout(baseView, this.getApplicationContext());
        registerStartHomeReceiver();
        registerExitDialogReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            statisticLog(currentItem);
        }
//        UserLogSDK.logActivityEntry(getApplicationContext(), UserLogSDK.getKeyDes(LogDefined.ACTIVITY_MAIN));
    }

    @Override
    protected void onPause() {
        if (mCurrLogDes != null) {
            UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            mCurrLogDes = null;
        }

        super.onPause();
    }
    
    
    @Override
	public void finish() {
    	super.finish();
    }
    

    @Override
    protected void onDestroy() {
        unregisterStartHomeReceiver();
        unregisterExitDialogReceiver();
        if (selectFragment != null) {
    		selectFragment.releaseRes();
    	}
        super.onDestroy();
    }
    

    private void initView() {
        mFloatingView = (ImageView) findViewById(R.id.floating_view);
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.top_tabs);

        mViewPagerAdapter = new HomeFragmentAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mPagerSlidingTabStrip.setTabPaddingLeftRight(20);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setTextSize(getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
        mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal); // 未选中字体颜色
        mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected); // 指示线
        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg); // 下划线
        mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg); // 分割线
        mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
        mPagerSlidingTabStrip.setTabPaddingLeftRight(20);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        
        ArrayList<ListView> lists = new ArrayList<ListView>();
        lists.add(selectFragment == null ? null : selectFragment.getListView());
        lists.add(rankListFragment == null ? null : rankListFragment.getListView());
        lists.add(null);
        lists.add(null);
        mPagerSlidingTabStrip.setChildListViewArray(lists);
        
        if (mViewPager != null) {
            int currentItem = mViewPager.getCurrentItem();
            statisticLog(currentItem);
        }
    }

    
    private void initFloatingViewData() {
        new Thread() {
        	@Override
        	public void run() {
            	int type = -1;
            	String imageUrl = null;
            	String webUrl = null;
            	Drawable drawable = null;
            	try {
        	        GetMarketFrameResp frameResp = MarketApplication.getMarketFrameResp();
        	        TopicInfoBto topicInfo = frameResp.getChannelList().get(0).getTopicList().get(4);
        	        type = topicInfo.getTopicType();
        	        imageUrl = topicInfo.getImgUrl();
        	        webUrl = topicInfo.getWbUrl();
                } catch (Exception e) {
                	e.printStackTrace();
                }
            	
            	switch (type) {
            	//幸运红包
            	case 9:
            		drawable = getResources().getDrawable(R.drawable.hongbao);
            		break;
            	//WebView
            	case 4:
            		drawable = AsyncImageLoader.loadImageFromUrl(imageUrl, "huodong_" + MarketUtils.getMD5(imageUrl));
            		break;
            	}
            	
            	if (drawable != null) {
            		Bundle bundle = new Bundle();
            		bundle.putString("webUrl", webUrl);
            		Message msg = new Message();
            		msg.what = HANDLER_GET_DRAWABLE_SUCCESS;
            		msg.setData(bundle);
            		msg.obj = drawable;
            		
            		mHandler.sendMessage(msg);
            	}
        	}
        }.start();
    }

    
    private void initFloatingView(Drawable drawable, final String webUrl) {
    	
    	if (TextUtils.isEmpty(webUrl) || drawable == null) return;
    	
    	if (mFloatingView != null) {
    		mFloatingView.setImageDrawable(drawable);
            mFloatingView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
	                Intent topicIntent = new Intent(HomeActivity.this, BaseHtmlActivity.class);
	                topicIntent.putExtra("wbUrl", webUrl);
	                startActivity(topicIntent);
                }
            });
    	}
    }
    

    public void registerStartHomeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_HOME_ACTION);
        registerReceiver(mReceiver, filter);
    }
    
    public void registerExitDialogReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zhuoyi.exit.dialog");
        registerReceiver(exitDialogReceiver, filter);
    }
    
    
    private void unregisterStartHomeReceiver() {
        unregisterReceiver(mReceiver);
    }
    
    private void unregisterExitDialogReceiver() {
        unregisterReceiver(exitDialogReceiver);
    }
    
    public class HomeFragmentAdapter extends FragmentPagerAdapter {

        private DownloadCallBackInterface mDownloadCallback;
        private ArrayList<String> mTitles;

        public HomeFragmentAdapter(FragmentManager fm, DownloadCallBackInterface downloadCallback) {
            super(fm);
            this.mDownloadCallback = downloadCallback;
            mTitles = new ArrayList<String>();
            mTitles.add(getResources().getString(R.string.home_apps_selected));
            mTitles.add(getResources().getString(R.string.home_apps_ranklist));
            mTitles.add(getResources().getString(R.string.home_special_button3));
            mTitles.add(getResources().getString(R.string.necessary));
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                selectFragment = SelectFragment.newIntance(position, MarketApplication.getRootContext(), mDownloadCallback);
                return selectFragment;
            case 1:
                rankListFragment = RankListFragment.newIntance(position, mDownloadCallback);
                return rankListFragment;
            case 2:
                specialFragment = SpecialFragment.newIntance(position);
                return specialFragment;
            case 3:
                necessaryFragment = NecessaryFragment.newIntance(position, MarketApplication.getRootContext(), mDownloadCallback);
                return necessaryFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles.get(position);
        }

    }

    /* DownloadTabBaseActivity 抽象函数实现部分 start */
    @Override
    protected void onDownloadServiceBind() {

    }

    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo) {

    }

    @Override
    protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {

    }

    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {

    }

    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        updateCurrentFragment();
    }

    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {

    }

    /* DownloadTabBaseActivity 抽象函数实现部分 end */

    /* DownloadCallBackInterface 接口实现部分start */
    @Override
    public void startDownloadApp(String pacName, String appName, String filePath, String md5, String url,
        String topicId, String type, int verCode, int appId, long totalSize) {
        try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type,
                verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startIconAnimation(String pacName, int versionCode, Drawable drawable, int fromX, int fromY) {
        Splash.startDownloadAnim(pacName, versionCode, drawable, fromX, fromY);
    }

    /* DownloadCallBackInterface 接口实现部分end */

    @Override
    public void tellSplashShowPage(int page) {

    }

    @Override
    public boolean downloadPause(String pkgName, int verCode) {
        try {
            return pauseDownloadApk(pkgName, verCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            updateCurrentFragment();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void updateCurrentFragment() {
        if (mViewPager == null) {
            return;
        }
        int currentItem = mViewPager.getCurrentItem();
        switch (currentItem) {
        case 0:
            if (selectFragment != null)
                selectFragment.refreshData();
            break;
        case 1:
            if (rankListFragment != null)
                rankListFragment.refreshData();
            break;
        case 2:
            break;
        case 3:
            if (necessaryFragment != null)
                necessaryFragment.refreshData();
            break;
        default:
            break;
        }
    }

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	
	private String mCurrLogDes = null;
	

	@Override
	public void onPageSelected(int arg0) {
	    statisticLog(arg0);
		switch (arg0) {
		case 0:
//		    if (mCurrLogDes != null) {
//		        UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
//		    }
//		    mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_MAIN);
//		    UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
			break;
		case 1:
//		    if (mCurrLogDes != null) {
//                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
//            }
//            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_RANK_LIST);
//            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
			if (rankListFragment != null) {
				rankListFragment.entryView();
			}
			break;
		case 2:
//		    if (mCurrLogDes != null) {
//                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
//                mCurrLogDes = null;
//            }
			break;
		case 3:
//		    if (mCurrLogDes != null) {
//                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
//            }
//            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GOTTA);
//            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
			if (necessaryFragment != null) {
				necessaryFragment.entryNecessaryView();
			}
			break;
		}
	}
	
	
	private void statisticLog(int pageIndex) {
	    switch (pageIndex) {
        case 0:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_MAIN);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
        case 1:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_RANK_LIST);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
        case 2:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
                mCurrLogDes = null;
            }
            break;
        case 3:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_GOTTA);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
        }
	}
}
