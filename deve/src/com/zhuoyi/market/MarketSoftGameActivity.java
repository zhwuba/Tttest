package com.zhuoyi.market;

import java.util.ArrayList;
import java.util.List;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.market.account.login.BaseHtmlActivity;
import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.data.TopicInfoBto;
import com.market.net.response.GetMarketFrameResp;
import com.market.statistics.ReportFlag;
import com.market.view.PagerSlidingTabStrip;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.home.HomeView;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;
import com.zhuoyi.market.view.CustomViewFactory;

/**
 * 应用和游戏界面
 * @author dream.zhou
 *
 */
public class MarketSoftGameActivity extends DownloadTabBaseActivity implements OnPageChangeListener, DownloadCallBackInterface{

    private ViewPager mViewPager = null;
    private PagerSlidingTabStrip mPagerSlidingTabStrip = null;
    private ArrayList<View> mViews = null;
    private ArrayList<String> mTitles = null;
    
    private HomeView mRecommendView = null;
    private AbsCustomView mCategoryView = null;
    private AbsCustomView mRankView = null;
    private AbsCustomView mSoftNewView = null;
    private View mGameGiftView = null;

    private AbsCustomView mCurrLogView = null;
    
    private LocalActivityManager mLocalActivityManager = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
 
        setContentView(R.layout.common_tab_layout);
        View baseView = findViewById(R.id.base_layout);
		MarketUtils.setBaseLayout(baseView, this.getApplicationContext());

        mLocalActivityManager = new LocalActivityManager(this, true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);

        initView();
        
        childViewEntry(mRecommendView);
        mRecommendView.entryRecommendView();
        mRecommendView.startRequestRecomendSoftAndGame();
    }
    
    
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            updateCurrentPage();
        }
        super.onWindowFocusChanged(hasFocus);
    }      
    
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (mCurrLogView != null) {
            mCurrLogView.entryView();
        }
        
        if (mLocalActivityManager != null) {
        	mLocalActivityManager.getActivity("gameGift").onWindowFocusChanged(true);
        }
        
    }

    @Override
    protected void onPause() {
        currChildViewExit();
        
        super.onPause();
    }


    @Override
    public void onDestroy() {
    	super.onDestroy();
    	if (mLocalActivityManager != null)
            mLocalActivityManager.destroyActivity("gameGift", false);
    	
        if (mRecommendView != null)
            mRecommendView.freeViewResource();
        
        if (mCategoryView != null)
            mCategoryView.freeViewResource();
        
        if (mRankView != null)
            mRankView.freeViewResource();
        
        if (mSoftNewView != null)
            mSoftNewView.freeViewResource();
    }
    
    
    private void initView(){

        mViewPager = (ViewPager) findViewById(R.id.common_page);
        
        Intent intent = getIntent();
        int[] viewTypes = intent.getIntArrayExtra("viewTypes");
        boolean isSoftNew = true;
        if (CustomViewFactory.VIEW_GAME_GIFT == viewTypes[2]) {
            isSoftNew = false;
        }
        
        mRecommendView = (HomeView) CustomViewFactory.create(viewTypes[0], getApplicationContext(), this);
        mRecommendView.setNeedPaddingTop(true);
        mCategoryView = CustomViewFactory.create(viewTypes[1], getApplicationContext(), this);
        if (isSoftNew) {
            mLocalActivityManager = null;
            mSoftNewView = CustomViewFactory.create(viewTypes[2], this, this);
        } else {
            mGameGiftView = getGameGiftView();
            try {
            	View view = mGameGiftView.findViewWithTag("statusBarView");
            	view.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }
        
        mRankView = CustomViewFactory.create(viewTypes[3], getApplicationContext(), this);

        
        mViews = new ArrayList<View>();
        mViews.add(mRecommendView.getRootView());
        mViews.add(mCategoryView.getRootView());
        if (isSoftNew) {
            mViews.add(mSoftNewView.getRootView());
        } else {
            mViews.add(mGameGiftView);
        }
        mViews.add(mRankView.getRootView());
        
        mTitles = new ArrayList<String>();
        mTitles.add(getResources().getString(R.string.home_apps_recommend)); 
        mTitles.add(getResources().getString(R.string.home_apps_assort));
        if (isSoftNew) {
            mTitles.add(getResources().getString(R.string.home_apps_new));
        } else {
            mTitles.add(getResources().getString(R.string.home_game_gift));
        }
        mTitles.add(getResources().getString(R.string.home_apps_rank)); 

        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {

                return arg0 == arg1;
            }

            @Override
            public int getCount() {

                return mViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                    Object object) {
                container.removeView(mViews.get(position));

            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles.get(position);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViews.get(position));
                return mViews.get(position);
            }

        });
        
        ArrayList<ListView> lists = new ArrayList<ListView>();
        lists.add(mRecommendView.getListView());
        lists.add(mCategoryView.getListView());
        if (isSoftNew) {
            lists.add(mSoftNewView.getListView());
        } else {
            lists.add(null);
        }
        lists.add(mRankView.getListView());
        
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.common_tab);
        mPagerSlidingTabStrip.setTabPaddingLeftRight(20);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setChildListViewArray(lists);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.common_subtitle_bg));
        mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);
        mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);
        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
        mPagerSlidingTabStrip.setTextSize(getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
        mPagerSlidingTabStrip.setShouldExpand(true);
        
        updateWebByPosition(0);
    }
    
    
    private View getGameGiftView() {

		GetMarketFrameResp resp = MarketApplication.getMarketFrameResp();
		Intent intent = new Intent(this.getParent(), BaseHtmlActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("wbUrl", "");
		intent.putExtra("from_path", ReportFlag.FROM_GAIN_GIFT);
		intent.putExtra("titleBar",false);
		intent.putExtra("showExitDialog", true);
		if(resp != null) {
			List<TopicInfoBto> topicList =  resp.getChannelList().get(3).getTopicList();
			TopicInfoBto topicInfoBto = null;
			if(topicList.size() > 3) {
				topicInfoBto =  topicList.get(2);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				String url = topicInfoBto.getWbUrl();
				intent.putExtra("wbUrl", url);
			}
		}
        
        return mLocalActivityManager.startActivity("gameGift", intent).getDecorView();
    }
    
    
    /*DownloadTabBaseActivity 抽象函数实现部分 start*/
    @Override
    protected void onDownloadServiceBind() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onNoEnoughSpace(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
        
    }

    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
    	updateCurrentPage();
    }

    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {
        
    }

    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }
    /*DownloadTabBaseActivity 抽象函数实现部分 end*/


    /*DownloadCallBackInterface 接口实现部分start*/
    @Override
    public void startDownloadApp(String pacName, String appName,
            String filePath, String md5, String url, String topicId, String type, int verCode,
            int appId, long totalSize) {
        try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void startIconAnimation(String pacName, int versionCode,
            Drawable drawable, int fromX, int fromY) {
        Splash.startDownloadAnim(pacName, versionCode, drawable, fromX, fromY);
    }
    /*DownloadCallBackInterface 接口实现部分end*/

    
    /*OnPageChangeListener 接口实现部分start*/
    @Override
    public void onPageScrollStateChanged(int arg0) {
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        
    }

    @Override
    public void onPageSelected(int position) {
        switch(position){
        case 0:
            childViewEntry(mRecommendView);
            break;
        case 1:
            childViewEntry(mCategoryView);
            break;
        case 2:
            if (mSoftNewView != null) {
                childViewEntry(mSoftNewView);
            } else {
                currChildViewExit();
            }
            break;
        case 3:
            childViewEntry(mRankView);
            break;
        default:
            break;
        }
        
        updateWebByPosition(position);
    }
    /*OnPageChangeListener 接口实现部分end*/
    
    private void childViewEntry(AbsCustomView view) {
        if (mCurrLogView != null && mCurrLogView != view) {
            mCurrLogView.exitView();
        }
        view.entryView();
        mCurrLogView = view;
    }
    
    
    private void currChildViewExit() {
        if (mCurrLogView != null) {
            mCurrLogView.exitView();
            mCurrLogView = null;
        }
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
	
	private void updateCurrentPage() {
        int curPage = mViewPager.getCurrentItem();
        if (curPage == 0) {
            mRecommendView.notifyDataSetChanged(null);
        } else if (curPage == 2) {
            if (mSoftNewView != null)
                mSoftNewView.notifyDataSetChanged(null);
        } else if (curPage == 3) {
            mRankView.notifyDataSetChanged(null);
        }
    }

	
	/**
	 * 如果在web页,则通知web
	 * @param position
	 */
	private void updateWebByPosition(int position) {
		if(mLocalActivityManager == null) return; 
		BaseHtmlActivity baseHtmlActivity = (BaseHtmlActivity) mLocalActivityManager.getActivity("gameGift");
        if(position == 2) {
        	baseHtmlActivity.setShouldCallJs(true);
        	baseHtmlActivity.callJsRefresh();
        } else {
        	baseHtmlActivity.setShouldCallJs(false);
        }
	}
}
