package com.zhuoyi.market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.market.behaviorLog.UserLogSDK;
import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetSoftGameDetailReq;
import com.market.net.response.GetSoftGameDetailResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.market.view.PagerSlidingTabStrip;
import com.market.view.PressInstallButtonAnimView;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.CategoryDetailView;

/**
 * 分类详情页面
 * @author JLu
 *
 */
public class CategoryDetailActivity extends DownloadTabBaseActivity implements OnPageChangeListener, DownloadCallBackInterface {

	private ViewPager mViewPager;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private CommonSubtitleView mTitleBar;
	private PressInstallButtonAnimView mPressInstallButtonAnimView = null;
    private int[] mDownloadLocation = {0,0};
    private int mStatusBarHeight = 0;
	/**
	 * 子view的个数
	 */
	private int mViewCount;
	/**
	 * 要展示的app列表
	 */
	private List<AppInfoBto> mDataList;
	/**
	 * tab标题集合
	 */
	private List<String> mTitleNames;

	/**
	 * viewpager中childView的集合
	 */
	private CategoryDetailView[] mDetailViews;
	/**
	 * 激活位置,缺省在第一个位置"全部"
	 */
	private int mActivePosition = 0;

	private GetSoftGameDetailReq mReq;

	private Handler mHandler;

	private static final int UPDATE_PAGE_MSG = 0;

	//网络请求参数
	private int mReqChannelIndex = -1;
	private int mReqLevel2Id = -1;
	private int mReqLevel3Id = -1;
	private String mReqCategoryName;
	//结束
	private String mParentName;
	
	private Context mContext = null;
    private LinearLayout mSearch_loading = null;
    private LinearLayout mRefresh_linearLayout_id = null;
    private Button mRefreshButton;
    
    private String mReportFlag = null;
    
    private String mLogTag = null;
    
    private CategoryDetailView mCurrLogView = null;
    
    /**
     * 所有子分类的ListView集合
     */
    private ArrayList<ListView> lists = new ArrayList<ListView>();
    
    private MyPagerAdapter mPageAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_category_detail);
		mContext = this.getApplicationContext();
		
		mReportFlag = this.getIntent().getStringExtra("reportFlag");
		mLogTag = this.getIntent().getStringExtra("detailLogTag");
		
		findViews();
		initViews();
		startGetCategoryData();
	}
	
	
    @Override
    protected void onResume() {
    	CommonLoadingManager.get().showLoadingAnimation(this);
        super.onResume();
        
        if (mCurrLogView != null) {
            mCurrLogView.entryView();
        }
    }

	
	
    @Override
    protected void onPause() {
        if (mCurrLogView != null) {
            mCurrLogView.exitView();
        }
        
        super.onPause();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        if (hasFocus) {
            mTitleBar.setDownloadStatus();
            updateCurentList();
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
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mViewPager!=null)
            mViewPager.removeAllViews();
        if(mDetailViews!=null)
            mDetailViews = null;
        if(lists!=null)
            lists.clear();
        if(mDataList!=null)
            mDataList.clear();
        
        if (mTitleBar != null)
            mTitleBar.unRegisteredReceiver();
        
        
    }


    @Override
    public void onBackPressed() {
        finish();
    }


	private void findViews() {
		mViewPager = (ViewPager) findViewById(R.id.category_page);
		mTitleBar = (CommonSubtitleView) findViewById(R.id.title);
		mTitleBar.showSearchBtn(true);
		mTitleBar.registeredReceiver();
		mPressInstallButtonAnimView = (PressInstallButtonAnimView)findViewById(R.id.common_download_anim);

	}


	private void initViews() {
	    
        mSearch_loading = (LinearLayout) findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) findViewById(R.id.refresh_linearLayout_id);
        mRefreshButton = (Button) findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarketUtils.getAPNType(mContext) == -1) {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }
                startGetCategoryData();
            }
        });
	    
		mParentName = getIntent().getStringExtra("parentName");
		mTitleBar.setSubtitleName(mParentName);
		mTitleNames = new ArrayList<String>();
		mTitleNames.add(getResources().getString(R.string.all));
		initViewPager();

		mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {

				HashMap<String, Object> map = null;
				switch (msg.what) {
				case UPDATE_PAGE_MSG:
					GetSoftGameDetailResp resp = null;
					map = (HashMap<String, Object>)msg.obj;
					try { 
					    if(map != null)
					        resp = (GetSoftGameDetailResp)map.get("categoryDetailResp");

						List<AppInfoBto> appList = null;
						List<AssemblyInfoBto> assemblyList = null;

						if (resp != null) {
						    appList = resp.getAppInfoList();
						    assemblyList = resp.getAssemblyList();
						}
						
                        if (assemblyList == null) {
                            mSearch_loading.setVisibility(View.GONE);
                            mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                            return;
                        }

						//获取服务端下发的数据
						List<AppInfoBto> lvl3List = null;
						int assemblyId = 0;
						if(appList!=null) {
							//下发的是二级分类的数据,即"全部"的数据
							lvl3List = assemblyList.get(0).getAppInfoList();
						} else {
							//下发的是三级数据
						    appList = assemblyList.get(0).getAppInfoList();
							lvl3List = assemblyList.get(1).getAppInfoList();
						}
						assemblyId = assemblyList.get(0).getAssemblyId();

						initChildViews(lvl3List,assemblyId);
						
						mViewCount = lvl3List.size() + 1;
						if(mViewCount < 5) {//tab个数小于5,让其撑满整个titlebar
							mPagerSlidingTabStrip.setShouldExpand(true);
							mPagerSlidingTabStrip.setTabPaddingLeftRight(5);
						} else {
							mPagerSlidingTabStrip.setShouldExpand(false);
						}
						mPagerSlidingTabStrip.setVisibility(View.VISIBLE);
						mPagerSlidingTabStrip.setChildListViewArray(lists);
						mPagerSlidingTabStrip.notifyDataSetChanged();
						
						int firstGetDataNum = 0;
						boolean getListDataFail = true;
						if(mDataList == null)
                            mDataList = new ArrayList<AppInfoBto>();
	                    if (appList != null && appList.size() > 0) {
	                        AppInfoBto appInfo = null;
	                        int listSize = appList.size();
	                        firstGetDataNum = listSize;

	                        for(int i=0; i<listSize; i++) {
	                            appInfo = appList.get(i);
	                            appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
	                            if(appInfo.getIsShow()) {
	                                mDataList.add(appInfo); 
	                            }
	                        }
	                        getListDataFail = false;
	                    } else {
	                        getListDataFail = true;
	                    }
	                    
	                    mSearch_loading.setVisibility(View.GONE);
                        mRefresh_linearLayout_id.setVisibility(View.GONE);
                        mDetailViews[mActivePosition].setFirstGetDataNum(firstGetDataNum);
                        mDetailViews[mActivePosition].entryView();
                        mCurrLogView = mDetailViews[mActivePosition];       //for user behavior log
                        if (!getListDataFail) {
                        	mDetailViews[mActivePosition].setData(mDataList);
                        }
                        mPageAdapter.notifyDataSetChanged();
                        mViewPager.setCurrentItem(mActivePosition, true);
						
					} catch(IndexOutOfBoundsException e) {
						//服务端没有下发第一屏数据
						e.printStackTrace();
					} catch(Exception e) {
						e.printStackTrace();
					}

					break;
				}
				super.handleMessage(msg);
			}

		};
	}

	private void initViewPager() {
	    mPageAdapter = new MyPagerAdapter();
		mViewPager.setAdapter(mPageAdapter);

		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.category_tab);
		mPagerSlidingTabStrip.setViewPager(mViewPager);

		mPagerSlidingTabStrip.setOnPageChangeListener(CategoryDetailActivity.this);
		mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.common_subtitle_bg));
		mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);
		mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);
		mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);
		mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);
		mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
		mPagerSlidingTabStrip.setTextSize(getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
	}
	
	
	/**
	 * 初始化各子view
	 * @param lvl3List 从服务端取到的三级列表
	 */
	private void initChildViews(List<AppInfoBto> lvl3List,int assemblyId) {
	    
	    int resId = 0;
	    
		mDetailViews = new CategoryDetailView[lvl3List.size()+1];
		
		mDetailViews[0] = new CategoryDetailView(getApplicationContext(), this, mReportFlag);
		mDetailViews[0].setLogDes(UserLogSDK.getClassDetailActivityDes(mLogTag, mParentName, mParentName));
		lists.add(mDetailViews[0].getListView());
		GetSoftGameDetailReq req = new GetSoftGameDetailReq();
		req.setChannelIndex(mReqChannelIndex);
		req.setParentId(mReqLevel2Id);
		req.setTopicId(-1);
		req.setTopicName(mParentName);
		req.setReqModel(1);
		mDetailViews[0].setReq(req);
		mDetailViews[0].setAssemblyId(assemblyId);

		
		int[] assemblyIds = new int[lvl3List.size()];
		//======判断激活位置并跳转到该位置
		for(int i=0;i<lvl3List.size();i++) {
			//====如果是点击二级分类进来,三级id是-1,不会在三级列表中找到
			resId = lvl3List.get(i).getRefId(); 
			assemblyIds[i] = resId;
			if(resId == mReqLevel3Id) {
				mActivePosition = i + 1;
			}
			mTitleNames.add(lvl3List.get(i).getName());
			
			mDetailViews[i+1] = new CategoryDetailView(getApplicationContext(), this, mReportFlag);
			mDetailViews[i+1].setLogDes(UserLogSDK.getClassDetailActivityDes(mLogTag, mParentName, lvl3List.get(i).getName()));
			lists.add(mDetailViews[i+1].getListView());
			req = new GetSoftGameDetailReq();
			req.setChannelIndex(mReqChannelIndex);
			req.setParentId(mReqLevel2Id);
			req.setTopicId(lvl3List.get(i).getRefId());
			req.setTopicName(lvl3List.get(i).getName());
			req.setReqModel(1);
			mDetailViews[i+1].setReq(req);
			
		}
		//用来请求三级分类的分页数据
		mDetailViews[mActivePosition].setAssemblyId(assemblyId); 
		//用来请求"全部"的分页数据
		mDetailViews[0].setAssemblyIds(assemblyIds);
	}
	

	/**
	 * 请求分类详情数据
	 */
	private void startGetCategoryData() {
	    mSearch_loading.setVisibility(View.VISIBLE);
	    mRefresh_linearLayout_id.setVisibility(View.GONE);
		mReqChannelIndex = getIntent().getIntExtra("channelIndex", -1);
		mReqLevel2Id = getIntent().getIntExtra("level2Id", -1);
		mReqLevel3Id = getIntent().getIntExtra("level3Id", -1);
		mReqCategoryName = getIntent().getStringExtra("categoryName");

		mReq = new GetSoftGameDetailReq();
		try{
			mReq.setChannelIndex(mReqChannelIndex);
			mReq.setParentId(mReqLevel2Id);
			mReq.setTopicId(mReqLevel3Id);
			mReq.setTopicName(mReqCategoryName);
			mReq.setReqModel(0);
			String contents = SenderDataProvider.buildToJSONData(this,MessageCode.GET_SOFT_GAME_DETAIL,mReq);//mContext = this?
			StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_SOFT_GAME_DETAIL, contents);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	class MyPagerAdapter extends PagerAdapter {

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public int getCount() {

			return mViewCount;
		}

		@Override
		public void destroyItem(ViewGroup container, int position,
				Object object) {
			container.removeView(mDetailViews[position].getRootView());

		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitleNames.get(position);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = mDetailViews[position].getRootView();
			container.addView(view);
			return view;
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


	@Override
	public void onPageSelected(int position) {
	    if (mCurrLogView != null) {
	        mCurrLogView.exitView();
	    }
		mDetailViews[position].entryView();
		mCurrLogView = mDetailViews[position];
		mDetailViews[position].startRequestData();
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
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onSdcardLost(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onDownloadHttpError(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onDownloadProgressUpdate(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        
    }


    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
    	updateCurentList();
        
    }


    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurentList();
    }


    @Override
    public void startDownloadApp(String pacName, String appName,
            String filePath, String md5, String url, String topicId, String type, int verCode,
            int appId, long totalSize) {
        // TODO Auto-generated method stub
        try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    @Override
    public void startIconAnimation(String pacName, int versionCode,
            Drawable drawable, int fromX, int fromY) {
        // TODO Auto-generated method stub
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
    
    
    private void updateCurentList(){
        if (mDetailViews != null && mViewPager != null) {
            int curItem = mViewPager.getCurrentItem();
            if (curItem < mDetailViews.length){
                if(mDetailViews[curItem] != null)
                    mDetailViews[curItem].notifyDataSetChanged(null);
            }
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

}
