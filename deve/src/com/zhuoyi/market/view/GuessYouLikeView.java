package com.zhuoyi.market.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.download.util.Util;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.request.GetYouLikeAppReq;
import com.market.net.response.GuessYouLikeResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonMainTitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

public class GuessYouLikeView extends AbsCustomView implements OnScrollListener, OnItemClickListener {

    private int mStartIndex;
    private int mEndIndex;
    private View mView;
    private LinearLayout mSearch_loading;
    private LinearLayout mRefresh_linearLayout_id;
    private LinearLayout mCannotGuessYou;
    private Button mRefreshButton;
    private Handler mHandler;
    private List<AppInfoBto> mListViewData;
    private SingleLineItemAdapter mListViewAdapter = null;
    private final int UPDATE_PAGE_MSG = 1;
    private boolean mRefreshFinished = true;
    private boolean mGetListDataFail = false;
    private boolean mInitFinish = false;
    private String mSaveFrameName;
    private int mReqCount = 0;

    private View mFooter = null;
    private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;

    private WeakReference<DownloadCallBackInterface> mDownloadCallBack;

    private ListView mListView;


    public ListView getListView() {
        return mListView;
    }


    /**
     * 构造
     * @param context
     * @param downloadCallback 下载回调接口
     * @param saveFrameName 保存缓存数据的文件名, 传null或""代表不读/写缓存数据
     */
    public GuessYouLikeView(Context context,
            DownloadCallBackInterface downloadCallback, String saveFrameName) {
        super(context);
        mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(downloadCallback);
        mSaveFrameName = saveFrameName;

        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mView = tLayoutInflater.inflate(R.layout.layout_recommendation, null);
        mListView = (ListView) mView.findViewById(R.id.recommmendation_list);
        mFooter = tLayoutInflater.inflate(R.layout.foot, null);
    }


    public View getRootView() {
        return mView;
    }


    public void entryView() {
        super.entryView();     //it must be called
        
        if (mInitFinish)
            return;

        findViews();
        initViews();

        mInitFinish = true;

    }


    public void freeViewResource() {
        super.freeViewResource();
        
        if (mListViewAdapter != null)
            mListViewAdapter.freeImageCache();

        mHandler = null;

    }


    private void findViews() {

        mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);

        mFooterProgress.setVisibility(View.GONE);
        mFooterText.setText(mContext.getString(R.string.loaded_all_data));

        mListView.setOnScrollListener(this);
        mListView.setOnItemClickListener(this);

        mCannotGuessYou = (LinearLayout) mView.findViewById(R.id.nolist);
        mSearch_loading = (LinearLayout) mView.findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout_id);
        mRefreshButton = (Button) mView.findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarketUtils.getAPNType(mContext) == -1) {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }
                mReqCount = 0;
                startRequestData();
            }
        });
    }


    private void initViews() {
        mListViewData = new ArrayList<AppInfoBto>();
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                HashMap<String, Object> map;
                List<AppInfoBto> list = null;
                int i = 0;

                if (mContext == null)
                    return;
                switch (message.what) {
                case UPDATE_PAGE_MSG:
                    GuessYouLikeResp mGuessYouLikeResp = null;
                    map = (HashMap<String, Object>) message.obj;

                    if (map != null && map.size() > 0) {
                        mGuessYouLikeResp = (GuessYouLikeResp) map.get("guessYouLike");
                        map.clear();
                        FrameInfoCache.saveFrameInfoToStorage(mGuessYouLikeResp, mSaveFrameName);
                    } else {
                        mGuessYouLikeResp = (GuessYouLikeResp) FrameInfoCache.getFrameInfoFromStorage(mSaveFrameName);
                    }
                    if (mGuessYouLikeResp != null) {
                        list = mGuessYouLikeResp.getAppList();
                    }

                    int firstDataNum = 0;
                    if (list != null && list.size() > 0) {

                        AppInfoBto appInfo = null;
                        int firstListSize = list.size();
                        for (i = 0; i < firstListSize; i++) {
                            appInfo = list.get(i);
                            appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(), false));
                            if (appInfo.getIsShow()) {
                                mListViewData.add(appInfo);
                                firstDataNum++;
                            }
                        }
                        list = null;
                        if (mListViewAdapter == null) {
                            mListViewAdapter = new SingleLineItemAdapter(mContext, mListViewData, mDownloadCallBack.get(), Util.getWaterFlowLayoutId());
                            mListViewAdapter.setReportFlag(ReportFlag.FROM_GUESS_YOU_LIKE);
                            mListViewAdapter.setTopicId(-1);

                            mListView.addFooterView(mFooter);
                            mListView.setAdapter(mListViewAdapter);
                            mListViewAdapter.notifyDataSetChanged();
                        }

                        mGetListDataFail = false;
                    } else {
                        mGetListDataFail = true;
                    }
                    mRefreshFinished = true;
                    if (!mGetListDataFail && firstDataNum == 0) {
                        mReqCount++;
                        if (mReqCount > 3) {
                            setLayoutVisibility(true, true);
                        } else {
                            startRequestData();
                        }
                    } else {
                        setLayoutVisibility(true, false);
                    }
                    break;
                }
                super.handleMessage(message);
            }
        };
        mReqCount = 0;
        startRequestData();
    }


    public void notifyDataSetChanged(String pacName) {

        if (mListView == null || mListViewAdapter == null)
            return;

        if (pacName == null) {
            mListViewAdapter.notifyDataSetChanged();
            return;
        }

        int first = mListView.getFirstVisiblePosition();
        int last = mListView.getLastVisiblePosition();

        for (int i = first; i <= last; i++) {
            if (pacName.equals(mListViewData.get(i).getPackageName())) {
                mListViewAdapter.notifyDataSetChanged();
                break;
            }
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:
            CommonMainTitleView.setNeedHotWordChange(true);
            if (mListViewAdapter != null) {
                mListViewAdapter.allowRefreshIcon(true);
                asyncLoadImage();
            }
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        case OnScrollListener.SCROLL_STATE_FLING:
            CommonMainTitleView.setNeedHotWordChange(false);
            if (mListViewAdapter != null)
                mListViewAdapter.allowRefreshIcon(false);
            break;
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        switch (view.getId()) {
        case R.id.recommmendation_list:
            mStartIndex = firstVisibleItem;
            mEndIndex = firstVisibleItem + visibleItemCount;
            if (mEndIndex >= totalItemCount) {
                mEndIndex = totalItemCount - 1;
            }
            break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        // see the detail of the app
        if (null != mListViewAdapter && null != mListViewData
                && mListViewData.size() > 0 && position < mListViewData.size()) {
            AppInfoBto mAppInfoBto = (AppInfoBto) mListViewData.get(position);
            MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, ReportFlag.FROM_GUESS_YOU_LIKE, -1);
        }
    }


    private void startRequestData() {
        setLayoutVisibility(false, false);
        if (mRefreshFinished) {
            try {
                mRefreshFinished = false;

                GetYouLikeAppReq req = new GetYouLikeAppReq();
                String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GUESS_YOU_LIKE, req);
                StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GUESS_YOU_LIKE, contents);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void setLayoutVisibility(boolean refreshButtonVisible, boolean showTip) {

        if (showTip) {
            if (mCannotGuessYou.getVisibility() == View.GONE) {
                mCannotGuessYou.setVisibility(View.VISIBLE);
            }
            if (mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                mRefresh_linearLayout_id.setVisibility(View.GONE);
            }
            if (mSearch_loading.getVisibility() == View.VISIBLE) {
                mSearch_loading.setVisibility(View.GONE);
            }
            if (mListView.getVisibility() == View.VISIBLE) {
                mListView.setVisibility(View.GONE);
            }
            return;
        } else {
            if (mCannotGuessYou.getVisibility() == View.VISIBLE) {
                mCannotGuessYou.setVisibility(View.GONE);
            }
        }

        if (mListViewData != null && mListViewData.size() > 0) {
            if (mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                mRefresh_linearLayout_id.setVisibility(View.GONE);
            }
            if (mSearch_loading.getVisibility() == View.VISIBLE) {
                mSearch_loading.setVisibility(View.GONE);
            }
            if (mListView.getVisibility() == View.GONE) {
                mListView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mListView.getVisibility() == View.VISIBLE) {
                mListView.setVisibility(View.GONE);
            }
            if (refreshButtonVisible) {
                if (mSearch_loading.getVisibility() == View.VISIBLE) {
                    mSearch_loading.setVisibility(View.GONE);
                }
                if (mRefresh_linearLayout_id.getVisibility() == View.GONE) {
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                }
            } else {
                if (mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                    mRefresh_linearLayout_id.setVisibility(View.GONE);
                }
                if (mSearch_loading.getVisibility() == View.GONE) {
                    mSearch_loading.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    private void asyncLoadImage() {
        if (mListViewAdapter == null) {
            return;
        }
        ImageView imageView = null;
        AppInfoBto info = null;

        for (; mStartIndex <= mEndIndex; mStartIndex++) {
            info = (AppInfoBto) mListViewAdapter.getItem(mStartIndex);
            if (info == null) {
                continue;
            }

            imageView = (ImageView) mListView.findViewWithTag(info.getPackageName());
            if (imageView == null) {
                continue;
            }

            if (mListViewAdapter.isAllowRefreshIcon() == false) {
                break;
            }

            int defaultResId = R.drawable.picture_bg1_big;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
                AsyncImageCache.from(mContext).displayImage(
                        mListViewAdapter.isAllowRefreshIcon(),
                        imageView,
                        R.drawable.picture_bg1_big,
                        new AsyncImageCache.NetworkImageGenerator(info.getPackageName(), info.getImgUrl()), true);
            }
        }
    }

}
