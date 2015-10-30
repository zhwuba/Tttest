package com.zhuoyi.market;

import java.util.ArrayList;

import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.market.view.PagerSlidingTabStrip;
import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;
import com.zhuoyi.market.view.CustomViewFactory;
import com.market.view.PressInstallButtonAnimView;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * 通用可左右滑动tab的Activity
 * @author JLu
 *
 */
public class CommonTabActivity extends DownloadTabBaseActivity implements DownloadCallBackInterface,OnPageChangeListener {
	
	private ArrayList<View> viewList = null;
	private ArrayList<String> titleList = null;
	
	private AbsCustomView mCurrLogView = null;
	
	private AbsCustomView mLeftView,mRightView;
	private String mTitleName;
	
	private CommonSubtitleView mTitleBar;
	private ViewPager mViewPager;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	
    private PressInstallButtonAnimView mPressInstallButtonAnimView = null;
    private int[] mDownloadLocation = {0,0};
    private int mStatusBarHeight = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_tab_activity_layout);
		findViews();
		initViews();
		childViewEntry(mLeftView);
	}
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            mTitleBar.setDownloadStatus();
            if (mViewPager.getCurrentItem() == 0) {
                mLeftView.notifyDataSetChanged(null);
            } else {
                mRightView.notifyDataSetChanged(null);
            }
        }
        
        if(mDownloadLocation[0] == 0 || mDownloadLocation[1] == 0){
            
            Rect frame = new Rect();  
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  
            mStatusBarHeight = frame.top; 
            int downloadWidth = mTitleBar.getDownloadWidth();
            int downloadHeight = mTitleBar.getDownloadHeight();

            mDownloadLocation = mTitleBar.getDownloadLocation();
            mDownloadLocation[0] = mDownloadLocation[0] - downloadWidth/4;
            mDownloadLocation[1] = mDownloadLocation[1] - downloadHeight/2;
        }
        super.onWindowFocusChanged(hasFocus);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		CommonLoadingManager.get().showLoadingAnimation(this);
		
		mCurrLogView.entryView();
	}
	
	@Override
    protected void onPause() {
	    mCurrLogView.exitView();
	    
        super.onPause();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTitleBar != null)
            mTitleBar.unRegisteredReceiver();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
         }
         return super.onKeyDown(keyCode, event);
     }

	private void findViews() {
		Intent intent = getIntent();
		mTitleName = intent.getStringExtra("titleName");
		int[] viewTypes = intent.getIntArrayExtra("viewTypes");
		
		mLeftView = CustomViewFactory.create(viewTypes[0], getApplicationContext(), this);
		mRightView = CustomViewFactory.create(viewTypes[1], getApplicationContext(), this);
		
		viewList = new ArrayList<View>();
		viewList.add(mLeftView.getRootView());
		viewList.add(mRightView.getRootView());
		
		titleList = new ArrayList<String>();
		titleList.add(intent.getStringExtra("leftTabName"));
		titleList.add(intent.getStringExtra("rightTabName"));
		
		mTitleBar = (CommonSubtitleView)findViewById(R.id.title_bar);
		mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);
		mViewPager =  (ViewPager) findViewById(R.id.viewpager);
		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
	}
	
	private void initViews() {
		mTitleBar.setSubtitleName(mTitleName);
		mTitleBar.showSearchBtn(true);
		mTitleBar.registeredReceiver();
		
		mViewPager.setAdapter(new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return viewList.size();
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(viewList.get(position));
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return titleList.get(position);
			}
			

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));
				return viewList.get(position);
			}
		});
		
		ArrayList<ListView> listviewArray = new ArrayList<ListView>();
		listviewArray.add(mLeftView.getListView());
		listviewArray.add(mRightView.getListView());
		
		mPagerSlidingTabStrip.setViewPager(mViewPager);
		mPagerSlidingTabStrip.setOnPageChangeListener(this);
		mPagerSlidingTabStrip.setChildListViewArray(listviewArray);
		
		mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.common_subtitle_bg));
		mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);
		mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);
		mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);
		mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);
		mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
		mPagerSlidingTabStrip.setShouldExpand(true);
		
	}

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
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onSdcardLost(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onDownloadComplete(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onInstalling(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onInstallSuccess(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onInstallFailed(DownloadEventInfo eventInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onFileNotFound(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	protected void onFileNotUsable(DownloadEventInfo eventInfo) {
		if (mViewPager.getCurrentItem() == 0) {
			mLeftView.notifyDataSetChanged(null);
		} else {
			mRightView.notifyDataSetChanged(null);
		}
	}

	@Override
	public void startDownloadApp(String pacName, String appName,
			String filePath, String md5, String url, String topicId,
			String type, int verCode, int appId, long totalSize) {
		try {
			addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void startIconAnimation(String pacName, int versionCode,
			Drawable drawable, int fromX, int fromY) {
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

	@Override
	public boolean downloadPause(String pkgName, int verCode) {
		try {
			return pauseDownloadApk(pkgName, verCode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		switch (arg0) {
		case 0:
		    childViewEntry(mLeftView);
			break;
		case 1:
		    childViewEntry(mRightView);
		    break;
		}
	}
	
	
	private void childViewEntry(AbsCustomView view) {
        if (mCurrLogView != null && mCurrLogView != view) {
            mCurrLogView.exitView();
        }
        view.entryView();
        mCurrLogView = view;
    }
}
