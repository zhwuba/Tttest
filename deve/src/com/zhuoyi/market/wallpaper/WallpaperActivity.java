package com.zhuoyi.market.wallpaper;

import java.util.ArrayList;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.net.MessageCode;
import com.market.view.CommonLoadingManager;
import com.market.view.PagerSlidingTabStrip;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class WallpaperActivity extends Activity implements OnPageChangeListener {

    private ViewPager mViewPager = null;
    private PagerSlidingTabStrip mPagerSlidingTabStrip = null;
    private ArrayList<View> mViews = null;
    private ArrayList<String> mTitles = null;
    
    private DisplayWallpaperView mHot = null;
    private DisplayWallpaperView mNew = null;
    private DisplayWallpaperView mCategory = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_parent);
        
        int[] wDisplay = getWindowDisplay();
        
        mHot = new DisplayWallpaperView(WallpaperView.TYPE_DISPLAY, DisplayWallpaperView.NET_TYPE_HOTEST, wDisplay, MessageCode.GET_WALLPAPER_LIST_REQ, null);
        mHot.getDataFirstIn();
        mNew = new DisplayWallpaperView(WallpaperView.TYPE_DISPLAY, DisplayWallpaperView.NET_TYPE_NEWEST, wDisplay, MessageCode.GET_WALLPAPER_LIST_REQ, null);
        mCategory = new DisplayWallpaperView(WallpaperView.TYPE_CATEGORY, DisplayWallpaperView.NET_TYPE_CATEGORY, wDisplay , MessageCode.GET_WALLPAPER_LIST_REQ, null);
        
        initView();
    }
    
    
	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		if (mViewPager != null) {
    		int currentItem = mViewPager.getCurrentItem();
    		statisticLog(currentItem);
		}
	}
	
	
	@Override
    protected void onPause() {
        if (mCurrLogDes != null) {
            UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            mCurrLogDes = null;
        }

        super.onPause();
    }
    
    
    private void initView() {
        
        mViews = new ArrayList<View>();
        mViews.add(mHot.getView());
        mViews.add(mNew.getView());
        mViews.add(mCategory.getView());
        
        mTitles = new ArrayList<String>();
        mTitles.add(getResources().getString(R.string.trend)); 
        mTitles.add(getResources().getString(R.string.newest));
        mTitles.add(getResources().getString(R.string.assort));
        
        mViewPager = (ViewPager) findViewById(R.id.common_page);
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

        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.common_tab);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);
        mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);
        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
        mPagerSlidingTabStrip.setTextSize(getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
        mPagerSlidingTabStrip.setShouldExpand(true);
    }
    
    
    private int[] getWindowDisplay() {
        int[] display = {0,0};
        WindowManager wm = this.getWindowManager();
        display[0] = wm.getDefaultDisplay().getWidth();
        display[1] = wm.getDefaultDisplay().getHeight();
        return display;
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
    	switch(arg0) {
    	case 0:
    		break;
    	case 1:
    		if (mNew != null)
    			mNew.getDataFirstIn();
    		break;
    	case 2:
    		if (mCategory != null)
    			mCategory.getDataFirstIn();
    		break;
    	}
        statisticLog(arg0);
    }
    
    
    private void statisticLog(int pageIndex) {
        switch (pageIndex) {
        case 0:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_WALL_PAPER_HOT);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
            
        case 1:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_WALL_PAPER_NEW);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
            
        case 2:
            if (mCurrLogDes != null) {
                UserLogSDK.logActivityExit(getApplicationContext(), mCurrLogDes);
            }
            mCurrLogDes = UserLogSDK.getKeyDes(LogDefined.ACTIVITY_WALL_PAPER_SORT);
            UserLogSDK.logActivityEntry(getApplicationContext(), mCurrLogDes);
            break;
        }
    }
}
