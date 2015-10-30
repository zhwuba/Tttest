package com.zhuoyi.market.discovery;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetApkListByPageReq;
import com.market.net.request.GetTopicReq;
import com.market.net.response.GetApkListByPageResp;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

public class NovelView implements OnScrollListener {

    private int mStartIndex;
    private int mEndIndex;
    private Context mContext;
    private View mView;
    private ListView mListView;
    private LinearLayout mSearch_loading;
    private LinearLayout mRefresh_linearLayout_id;
    private Button mRefreshButton;
    private Handler mHandler;
    private List<AppInfoBto> mListViewData;
    private SingleLineItemAdapter mListViewAdapter = null;
    private int mGetListIndex = 0;
    private int mBaseNumber = 0;
    private int mFirstGetDataNum = 0;
    private final int REQ_DATA_NUM_ONCE = 16;
    private final int UPDATE_PAGE_MSG = 1;
    private final int REFRESH_LIST = 2;
    private boolean mRefreshFinished = true;
    private boolean mGetListDataFail = false;
    private boolean mIsToBottom = false;
    private GetTopicReq mTopicReq;
    private int mAssemblyId = -1;
    private boolean mInitFinish = false;
    private String mReportFlag;

    private View mFooter = null;
    private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;
    private int mTopicId = -1;

    // private DownloadCallBackInterface mDownloadCallBack = null;
    private WeakReference<DownloadCallBackInterface> mDownloadCallBack;

    public NovelView(Context context, DownloadCallBackInterface downloadCallback, int topicId, String reportFlag) {
        mContext = context;
        // mDownloadCallBack = downloadCallback;
        mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(downloadCallback);
        mReportFlag = reportFlag;
        this.mTopicId = topicId;
        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mView = tLayoutInflater.inflate(R.layout.novel_view, null);
        mTopicReq = new GetTopicReq();
        mFooter = tLayoutInflater.inflate(R.layout.foot, null);
    }

    public View getMyView() {
        return mView;
    }

    public void entryOneColView() {
        if (mInitFinish)
            return;
        findViews();
        initViews();
        mInitFinish = true;
    }

    private void findViews() {

        mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);

        mListView = getListView();

        mListView.setOnScrollListener(this);

        mSearch_loading = (LinearLayout) mView.findViewById(R.id.novel_loading);
        mRefresh_linearLayout_id = (LinearLayout) mView.findViewById(R.id.novel_refresh);
        mRefreshButton = (Button) mRefresh_linearLayout_id.findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarketUtils.getAPNType(mContext) == -1) {
                    Toast
                        .makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT)
                        .show();
                    return;
                }
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
                int getDataSuccess = 0;

                if (mContext == null)
                    return;
                switch (message.what) {
                case UPDATE_PAGE_MSG:
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
                        list = getTopicApplist(mTopicResp);
                    }

                    int firstDataNum = 0;
                    if (list != null && list.size() > 0) {

                        AppInfoBto appInfo = null;
                        int firstListSize = list.size();
                        for (i = 0; i < firstListSize; i++) {
                            appInfo = list.get(i);
                            appInfo
                                .setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(), false));
                            if (appInfo.getIsShow()) {
                                mListViewData.add(appInfo);
                                firstDataNum++;
                            }
                        }
                        list = null;
                        if (mGetListIndex == 0 && mListViewAdapter == null) {
                            if (mListViewAdapter == null) {
                                mListViewAdapter = new SingleLineItemAdapter(mContext, mDownloadCallBack.get(),
                                    Util.getWaterFlowLayoutId());
                                mListView.addFooterView(mFooter);
                                mListView.setAdapter(mListViewAdapter);
                            }
                            mFirstGetDataNum = firstListSize;
                            mListViewAdapter.setDatas(mListViewData);
                            mListViewAdapter.setReportFlag(mReportFlag);
                            mListViewAdapter.setTopicId(mTopicId);
                            mListViewAdapter.notifyDataSetChanged();
                        }

                        mGetListDataFail = false;
                    } else {
                        mGetListDataFail = true;
                    }
                    mRefreshFinished = true;
                    if (!mGetListDataFail && firstDataNum == 0) {
                        startRequestData();
                    } else {
                        setLayoutVisibility(true);
                    }
                    break;
                case REFRESH_LIST:
                    GetApkListByPageResp listResp = null;
                    getDataSuccess = message.arg1;
                    map = (HashMap<String, Object>) message.obj;
                    mGetListDataFail = true;
                    int currDataNum = 0;
                    if (getDataSuccess == 1) {
                        if (map != null) {
                            listResp = (GetApkListByPageResp) map.get("listByPage");
                            map.clear();
                            list = listResp.getAppList();
                        }

                        if (list != null && list.size() > 0) {
                            AppInfoBto appInfo = null;
                            int listSize = list.size();
                            for (i = 0; i < listSize; i++) {
                                appInfo = list.get(i);
                                appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),
                                    false));
                                if (appInfo.getIsShow()) {
                                    mListViewData.add(appInfo);
                                    currDataNum++;
                                }
                            }
                            list.clear();

                            /*
                             * if(listSize < REQ_DATA_NUM_ONCE) { mFooterProgress.setVisibility(View.GONE);
                             * mFooterText.setText(mContext.getString(R.string.loaded_all_data)); mIsToBottom = true;
                             * if(mListViewAdapter!=null) mListViewAdapter.allowRefreshIcon(true); }
                             */

                            if (currDataNum > 0) {
                                mListViewAdapter.setDatas(mListViewData);
                                mListViewAdapter.notifyDataSetChanged();
                            }

                            mGetListDataFail = false;
                        } else {
                            mFooterProgress.setVisibility(View.GONE);
                            mFooterText.setText(mContext.getString(R.string.loaded_all_data));
                            mIsToBottom = true;
                            if (mListViewAdapter != null)
                                mListViewAdapter.allowRefreshIcon(true);
                        }
                    }
                    mRefreshFinished = true;
                    if (!mGetListDataFail && currDataNum == 0) {
                        startRequestData();
                    } else {
                        setLayoutVisibility(true);
                    }
                    break;
                }
                super.handleMessage(message);
            }
        };
        startRequestData();
    }

    public ListView getListView() {
        if (mListView == null) {
            mListView = (ListView) mView.findViewById(R.id.novel_list);
        }
        return mListView;
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
            if (mListViewAdapter != null) {
                mListViewAdapter.allowRefreshIcon(true);
                // mListViewAdapter.notifyDataSetChanged();
                asyncLoadImage();
            }
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        case OnScrollListener.SCROLL_STATE_FLING:
            if (mListViewAdapter != null)
                mListViewAdapter.allowRefreshIcon(false);
            break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        switch (view.getId()) {
        case R.id.novel_list:
            mStartIndex = firstVisibleItem;
            mEndIndex = firstVisibleItem + visibleItemCount;
            if (mEndIndex >= totalItemCount) {
                mEndIndex = totalItemCount - 1;
            }

            if (mListView.getVisibility() == View.VISIBLE && mIsToBottom == false
                && view.getLastVisiblePosition() + MarketUtils.LESS_ITEM_NUM >= (view.getCount() - 1)
                && mRefreshFinished && mAssemblyId != -1 && MarketUtils.getAPNType(mContext) != -1) {
                if (!mGetListDataFail) {
                    mGetListIndex = mBaseNumber * REQ_DATA_NUM_ONCE + mFirstGetDataNum;
                    mBaseNumber++;
                }
                mRefreshFinished = false;
                GetApkListByPageReq req = new GetApkListByPageReq();
                req.setAssemblyId(mAssemblyId);
                req.setStart(mGetListIndex);
                req.setFixedLength(REQ_DATA_NUM_ONCE);
                String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GET_APK_LIST_BY_PAGE, req);
                StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST, MessageCode.GET_APK_LIST_BY_PAGE,
                    contents);
            }
            break;
        }
    }


    private void startRequestData() {
        setLayoutVisibility(false);
        if (mListViewAdapter != null) {
            if (mIsToBottom == false && mRefreshFinished && mAssemblyId != -1) {
                if (!mGetListDataFail) {
                    mGetListIndex = mBaseNumber * REQ_DATA_NUM_ONCE + mFirstGetDataNum;
                    mBaseNumber++;
                }
                mRefreshFinished = false;
                GetApkListByPageReq req = new GetApkListByPageReq();
                req.setAssemblyId(mAssemblyId);
                req.setStart(mGetListIndex);
                req.setFixedLength(REQ_DATA_NUM_ONCE);
                String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GET_APK_LIST_BY_PAGE, req);
                System.out.println("content2:" + contents);
                StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST, MessageCode.GET_APK_LIST_BY_PAGE,
                    contents);
            }
        } else {
            if (mRefreshFinished) {
                try {
                    mRefreshFinished = false;
                    mTopicReq.setTopicId(mTopicId);
                    String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GET_TOPIC_LIST,
                        mTopicReq);
                    StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_TOPIC_LIST,
                        contents);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void scrollToTop() {
        mListView.setSelection(0);
    }

    private List<AppInfoBto> getTopicApplist(GetTopicResp resp) {
        System.out.println("resp:" + resp);
        List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();
        AssemblyInfoBto assemblyInfo = null;

        if (assemblyList == null || assemblyList.size() <= 0)
            return null;
        assemblyInfo = assemblyList.get(0);
        if (assemblyInfo == null)
            return null;

        mAssemblyId = assemblyInfo.getAssemblyId();

        if (assemblyInfo.getAppInfoListSize() <= 0)
            return null;

        return assemblyInfo.getAppInfoList();
    }

    private void setLayoutVisibility(boolean refreshButtonVisible) {
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
        Drawable drawable = null;
        ImageView imageView = null;
        AppInfoBto info = null;
        Drawable tempDrawable = mContext.getResources().getDrawable(R.drawable.picture_bg1_big);
        ConstantState tempState = tempDrawable.getConstantState();

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

            drawable = imageView.getDrawable();
            if (drawable == null || drawable.getConstantState().equals(tempState)) {
                AsyncImageCache.from(mContext).displayImage(mListViewAdapter.isAllowRefreshIcon(), imageView,
                    R.drawable.picture_bg1_big,
                    new AsyncImageCache.NetworkImageGenerator(info.getPackageName(), info.getImgUrl()), true);
            }
        }
        tempDrawable.setCallback(null);
        tempDrawable = null;
    }

    public void freeResource(){
        if (mListViewData != null) {
            mListViewData.clear();
        }
        if (mListViewAdapter != null) {
            mListViewAdapter.freeImageCache();
        }
        if (mHandler != null) {
            mHandler = null;
        }
    }
    
    
}
