package com.zhuoyi.market.ranklist;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetModelTopicRequest;
import com.market.net.response.GetModelTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.market.view.CommonMainTitleView;
import com.market.view.SearchLoadingLayout;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;

/**
 * 榜单
 */
public class RankListView extends AbsCustomView implements OnClickListener, OnScrollListener{

	public static final int GET_RANKLIST_INFO = 0;

	private Context mContext;
	private View mRootView;
	private SearchLoadingLayout mLoadingLayout;
	private LinearLayout mRefreshLayout;
	private Button mRefreshButton;
	private Handler mHandler;
	private int mTopicId;
	private RankListAdapter mRankListAdapter;
	private DownloadCallBackInterface mDownloadCallBack;
	private ListView mRankListView;
	private int mStartIndex,mEndIndex;
	private int mChannelIndex,mTopicIndex;
	private String mSaveFrameName = null;
	
	public RankListView(Context context,DownloadCallBackInterface downloadCallback,int channelIndex, int topicIndex,String saveFrameName,String reportFlag,int dataIndex) {
		super(context);
		mContext = context;
		mChannelIndex = channelIndex;
		mTopicIndex = topicIndex;
		mTopicId = getTopicId(mChannelIndex, mTopicIndex);
		mSaveFrameName = saveFrameName;
		mRootView = View.inflate(context, R.layout.layout_ranklist, null);
		mDownloadCallBack = downloadCallback;
		initView();
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case GET_RANKLIST_INFO:
					bindRankListData((HashMap<String, Object>) msg.obj);
					break;

				default:
					break;
				}
			}
		};
	}
	
	
    public static int getTopicId(int channelIndex, int topicIndex) {
        int topicId = -1;
        try {
            topicId = MarketApplication.getMarketFrameResp().getChannelList().get(channelIndex).getTopicList()
                .get(topicIndex).getTopicId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return topicId;
    }
	

	private void bindRankListData(HashMap<String, Object> map) {
		GetModelTopicResp getModelTopicResp = null;
		if (map != null && map.size()>0) {
			getModelTopicResp = (GetModelTopicResp) map.get("modelTopicInfo");
			FrameInfoCache.saveFrameInfoToStorage(getModelTopicResp, mSaveFrameName);
		} else {
			getModelTopicResp = (GetModelTopicResp) FrameInfoCache.getFrameInfoFromStorage(mSaveFrameName);
		}
		if(getModelTopicResp != null && getModelTopicResp.getModelListInfoBto().getModel() != null) {
			List<AssemblyInfoBto> assemblyInfoBtos =  getModelTopicResp.getModelListInfoBto().getModel().getAssemblyList();
			if(assemblyInfoBtos != null && assemblyInfoBtos.size() > 0) {
				show(CONTENT_VIEW);
				mRankListAdapter = new RankListAdapter(mContext, mDownloadCallBack);
				mRankListAdapter.setReportFlag(ReportFlag.FROM_NULL);
				mRankListAdapter.setTopicId(mTopicId);
				mRankListAdapter.setDatas(assemblyInfoBtos);
				mRankListView.setAdapter(mRankListAdapter);
			} else {
				show(REFRESH_VIEW);
			}
		} else {
			show(REFRESH_VIEW);
		}
	}


	private void initView() {
		if(mRootView != null) {
			mLoadingLayout = (SearchLoadingLayout) mRootView.findViewById(R.id.search_loading);
			mRefreshLayout = (LinearLayout) mRootView.findViewById(R.id.refresh_linearLayout_id);
			mRefreshButton = (Button) mRootView.findViewById(R.id.refresh_button);
			mRefreshButton.setOnClickListener(this);

			mRankListView = (ListView) mRootView.findViewById(R.id.rank_list);
			mRankListView.setOnScrollListener(this);
		}
	}


	@Override
	public ListView getListView() {
		return mRankListView;
	}


	@Override
	public void entryView() {
		super.entryView();
		show(LOADING_VIEW);
		requestRankListInfo(mContext);
	}

	@Override
	public View getRootView() {
		return mRootView;
	}

	@Override
	public void notifyDataSetChanged(String pkgName) {
		if (mRankListAdapter != null)
			mRankListAdapter.notifyDataSetChanged();
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refresh_button:
			if (MarketUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				return;
			}
			show(LOADING_VIEW);
			requestRankListInfo(mContext);
			break;

		default:
			break;
		}
	}


	private void requestRankListInfo(Context context) {
		String contents = "";
		GetModelTopicRequest mReq = new GetModelTopicRequest();
		mReq.setTopicId(mTopicId);
		mReq.setChannelIndex(mChannelIndex);
		mReq.setTopicIndex(mTopicIndex);
		contents = SenderDataProvider.buildToJSONData(context,
				MessageCode.GET_MODEL_TOPIC_REQ, mReq);
		try {
			StartNetReqUtils.execMarketRequest(mHandler, GET_RANKLIST_INFO, MessageCode.GET_MODEL_TOPIC_REQ, contents);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}


	private static final int CONTENT_VIEW=0,LOADING_VIEW=1, REFRESH_VIEW=2;
	private void show(int whichView) {
		switch(whichView) {
		case CONTENT_VIEW:
			mLoadingLayout.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case LOADING_VIEW:
			mLoadingLayout.setVisibility(View.VISIBLE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case REFRESH_VIEW:
			mLoadingLayout.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			CommonMainTitleView.setNeedHotWordChange(true);
			if(mRankListAdapter!=null) {		
				mRankListAdapter.allowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			CommonMainTitleView.setNeedHotWordChange(false);
			if(mRankListAdapter!=null)
				mRankListAdapter.allowRefreshIcon(false);
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mStartIndex = firstVisibleItem;
		mEndIndex = firstVisibleItem + visibleItemCount;
		if (mEndIndex >= totalItemCount) {
			mEndIndex = totalItemCount - 1;
		}
	}

	private void asyncLoadImage() {

		if(mRankListAdapter == null) {
			return;
		}
		AssemblyInfoBto assemblyInfoBto = null;
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			assemblyInfoBto = (AssemblyInfoBto) mRankListAdapter.getItem(mStartIndex);
			if (assemblyInfoBto == null) {
				continue;
			}
			List<AppInfoBto> appInfoBtos = assemblyInfoBto.getAppInfoList();
			for(AppInfoBto appInfoBto : appInfoBtos) {
				loadImage(appInfoBto,(ImageView)mRankListView.findViewWithTag(appInfoBto.getPackageName()));
			}
			if (mRankListAdapter.isAllowRefreshIcon() == false) {
				break;
			}

		}    
	}
	
	/**
	 * @param imageView
	 * 加载小图
	 */
	private void loadImage(AppInfoBto info, ImageView imageView) {
		if(imageView == null) return;
		int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
		int defaultResId =  R.drawable.picture_bg1_big;
		if (resId == defaultResId) {
			AsyncImageCache.from(mContext).displayImage(
					mRankListAdapter.isAllowRefreshIcon(),
					imageView,
					defaultResId,
					new AsyncImageCache.NetworkImageGenerator(info.getPackageName(),info.getImgUrl()), true);
		}
	}
}
