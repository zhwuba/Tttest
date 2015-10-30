package com.zhuoyi.market.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.market.download.baseActivity.DownloadTabBaseActivity;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.market.view.PagerSlidingTabStrip;
import com.market.view.PressInstallButtonAnimView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

public class NovelActivity extends DownloadTabBaseActivity implements DownloadCallBackInterface, OnClickListener,
    OnPageChangeListener {

    private static final int GET_TOPIC_DATA = 0;
    private ArrayList<View> viewList = null;
    private ArrayList<String> titleList = null;
    private ArrayList<ListView> mListviewList = null;
    private NovelView mHotNovelView = null;
    private NovelView mNewNovelView = null;
    private int mChannelIndex = -1;
    private int mTopicIndex = -1;
    private String mTitleName;

    private CommonSubtitleView mTitleBar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;
    private PressInstallButtonAnimView mPressInstallButtonAnimView = null;
    private int[] mDownloadLocation = { 0, 0 };
    private int mStatusBarHeight = 0;

    private LinearLayout mSearchLoading;
    private LinearLayout mRefresh_linearLayout_id;
    private Button mRefreshButton;
    private List<AppInfoBto> mNovelTabList = null;
    
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.novel_tab_layout);
        findViews();
    }

    @Override
    protected void onResume() {
        CommonLoadingManager.get().showLoadingAnimation(this);
        super.onResume();
    }

    private void findViews() {
        Intent intent = getIntent();
        mChannelIndex = intent.getIntExtra("channelIndex", -1);
        mTopicIndex = intent.getIntExtra("topicIndex", -1);
        mTitleName = intent.getStringExtra("titleName");
        mTitleBar = (CommonSubtitleView) findViewById(R.id.novel_title);
        mTitleBar.setSubtitleName(mTitleName);
        mTitleBar.showSearchBtn(true);
        mTitleBar.registeredReceiver();
        mPressInstallButtonAnimView = (PressInstallButtonAnimView) findViewById(R.id.common_download_anim);
        mViewPager = (ViewPager) findViewById(R.id.novel_page);
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.novel_tab);
        mSearchLoading = (LinearLayout) findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) findViewById(R.id.refresh_linearLayout_id);
        mRefreshButton = (Button) mRefresh_linearLayout_id.findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarketUtils.getAPNType(getApplicationContext()) == -1) {
                    Toast.makeText(getApplicationContext(),
                        getApplicationContext().getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }
                startTopicRequest();
            }
        });
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                HashMap<String, Object> map;
                switch (message.what) {
                case GET_TOPIC_DATA:
                    GetTopicResp mTopicResp = null;
                    map = (HashMap<String, Object>) message.obj;

                    if (map != null && map.size() > 0) {
                        mTopicResp = (GetTopicResp) map.get("topicResp");
                        map.clear();
                        FrameInfoCache.saveFrameInfoToStorage(mTopicResp, "novelFrame");
                    } else {
                        mTopicResp = (GetTopicResp) FrameInfoCache.getFrameInfoFromStorage("novelFrame");
                    }
                    if (mTopicResp != null) {
                        List<AssemblyInfoBto> assemblyList = mTopicResp.getAssemblyList();
                        if (assemblyList != null && assemblyList.size() > 1) {
                            initTabData(assemblyList.get(0).getAppInfoList(), assemblyList.get(1).getAppInfoList());// 请求数据
                            setLayoutVisibility(false);
                        } else {
                            setLayoutVisibility(true);
                        }
                    } else {
                        setLayoutVisibility(true);
                    }
                    break;
                }
                super.handleMessage(message);
            }
        };
        startTopicRequest();
    }

    private void startTopicRequest() {
        setLayoutVisibility(false);
        try {
            GetTopicReq mTopicReq = new GetTopicReq();
            mTopicReq.setChannelIndex(mChannelIndex);
            mTopicReq.setTopicIndex(mTopicIndex);
            String contents = SenderDataProvider.buildToJSONData(NovelActivity.this, MessageCode.GET_TOPIC_LIST,
                mTopicReq);
            StartNetReqUtils.execListByPageRequest(mHandler, GET_TOPIC_DATA, MessageCode.GET_TOPIC_LIST, contents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initTabData(List<AppInfoBto> one, List<AppInfoBto> two) {
        viewList = new ArrayList<View>();
        titleList = new ArrayList<String>();
        mNovelTabList = new ArrayList<AppInfoBto>();
        mListviewList = new ArrayList<ListView>();
        if (one != null && one.size() > 0) {
            mNovelTabList.add(one.get(0));
            mHotNovelView = new NovelView(getApplicationContext(), this, one.get(0).getRefId(), ReportFlag.FROM_NULL);
            viewList.add(mHotNovelView.getMyView());
            titleList.add(getResources().getString(R.string.discover_novel_tab_hot));
            mListviewList.add(mHotNovelView.getListView());
        }
        if (two != null && two.size() > 0) {
            mNovelTabList.add(two.get(0));
            mNewNovelView = new NovelView(getApplicationContext(), this, two.get(0).getRefId(), ReportFlag.FROM_NULL);
            viewList.add(mNewNovelView.getMyView());
            titleList.add(getResources().getString(R.string.discover_novel_tab_latest));
            mListviewList.add(mNewNovelView.getListView());
        }
        setLayoutViews();
        if (mHotNovelView != null) {
            mHotNovelView.entryOneColView();
        }
    }

    private void setLayoutViews() {

        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return viewList == null ? 0 : viewList.size();
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

        mPagerSlidingTabStrip.setTabPaddingLeftRight(20);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        mPagerSlidingTabStrip.setChildListViewArray(mListviewList);
        mPagerSlidingTabStrip.setOnPageChangeListener(this);
        mPagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.common_subtitle_bg));
        mPagerSlidingTabStrip.setTextColorResource(R.color.tab_top_text_normal);
        mPagerSlidingTabStrip.setIndicatorColorResource(R.color.tab_top_selected);
        mPagerSlidingTabStrip.setUnderlineColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setDividerColorResource(R.color.common_subtitle_bg);
        mPagerSlidingTabStrip.setIndicatorHeight(getResources().getDimensionPixelSize(R.dimen.indicator_height));
        mPagerSlidingTabStrip.setTextSize(getResources().getDimensionPixelSize(R.dimen.sliding_tab_text_size));
        mPagerSlidingTabStrip.setShouldExpand(true);

    }

    private void setLayoutVisibility(boolean refreshButtonVisible) {
        if (mNovelTabList != null && mNovelTabList.size() > 0) {
            if (mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                mRefresh_linearLayout_id.setVisibility(View.GONE);
            }
            if (mSearchLoading.getVisibility() == View.VISIBLE) {
                mSearchLoading.setVisibility(View.GONE);
            }
            mPagerSlidingTabStrip.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
        } else {
            if (refreshButtonVisible) {
                if (mSearchLoading.getVisibility() == View.VISIBLE) {
                    mSearchLoading.setVisibility(View.GONE);
                }
                if (mRefresh_linearLayout_id.getVisibility() == View.GONE) {
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                }
            } else {
                if (mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                    mRefresh_linearLayout_id.setVisibility(View.GONE);
                }
                if (mSearchLoading.getVisibility() == View.GONE) {
                    mSearchLoading.setVisibility(View.VISIBLE);
                }
            }
            mPagerSlidingTabStrip.setVisibility(View.GONE);
            mViewPager.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDownloadServiceBind() {

    }

    @Override
    protected void onApkDownloading(DownloadEventInfo eventInfo) {

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
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDownloadComplete(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onInstalling(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub
        updateCurrentPage();
    }

    @Override
    protected void onInstallSuccess(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onInstallFailed(DownloadEventInfo eventInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onFileNotFound(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    protected void onFileNotUsable(DownloadEventInfo eventInfo) {
        updateCurrentPage();
    }

    @Override
    public void startIconAnimation(String pacName, int versionCode, Drawable drawable, int fromX, int fromY) {
        // Splash.startDownloadAnim(pacName, versionCode, drawable, fromX, fromY);
        if (mPressInstallButtonAnimView != null)
            mPressInstallButtonAnimView.startDownloadAnim(pacName, versionCode, drawable, fromX, mDownloadLocation[0],
                (fromY - mStatusBarHeight), mDownloadLocation[1]);
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
            mHotNovelView.entryOneColView();
            break;
        case 1:
            mNewNovelView.entryOneColView();
            break;
        default:
            break;
        }

    }

    @Override
    public void startDownloadApp(String pacName, String appName, String filePath, String md5, String url,
        String topicId, String type, int verCode, int appId, long totalSize) {
        try {
            addDownloadApkWithoutNotify(pacName, appName, md5, url, topicId, type, verCode, appId, totalSize);
        } catch (RemoteException e) {
            e.printStackTrace();
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            mTitleBar.setDownloadStatus();
            updateCurrentPage();
        }
        if (mDownloadLocation[0] == 0 || mDownloadLocation[1] == 0) {

            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            mStatusBarHeight = frame.top;
            int downloadWidth = mTitleBar.getDownloadWidth();
            int downloadHeight = mTitleBar.getDownloadHeight();

            mDownloadLocation = mTitleBar.getDownloadLocation();
            mDownloadLocation[0] = mDownloadLocation[0] - downloadWidth / 4;
            mDownloadLocation[1] = mDownloadLocation[1] - downloadHeight / 2;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void updateCurrentPage() {
        if (mViewPager == null || mHotNovelView == null || mNewNovelView == null) {
            return;
        }
        if (mViewPager.getCurrentItem() == 0) {
            mHotNovelView.notifyDataSetChanged(null);
        } else {
            mNewNovelView.notifyDataSetChanged(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mViewPager != null) {
            mViewPager.removeAllViews();
        }
        if(mHotNovelView != null) {
            mHotNovelView.freeResource();
            mHotNovelView = null;
        }
        if(mNewNovelView != null) {
            mNewNovelView.freeResource();
            mNewNovelView=null;
        }
        if(mListviewList != null) {
            mListviewList.clear();
            mListviewList = null;
        }
        if (mNovelTabList != null) {
            mNovelTabList.clear();
            mNovelTabList = null;
        }
        if (titleList != null) {
            titleList.clear();
            titleList = null;
        }
        if (viewList != null) {
            viewList.clear();
            viewList = null;
        }
        if (mTitleBar != null) {
            mTitleBar.unRegisteredReceiver();
        }
        if (mHandler != null) {
            mHandler = null;
        }
    }
    
}
