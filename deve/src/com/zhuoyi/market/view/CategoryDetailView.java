package com.zhuoyi.market.view;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.market.net.data.AssemblyInfoBto;
import com.market.net.request.GetApkListByPageReq;
import com.market.net.request.GetSoftGameDetailReq;
import com.market.net.response.GetApkListByPageResp;
import com.market.net.response.GetSoftGameDetailResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 分类详情中每个tab对应的view
 * @author JLu
 *
 */
public class CategoryDetailView extends AbsCustomView implements OnScrollListener, OnItemClickListener {

	private View mView;
	private ListView mListView;
	public ListView getListView() {
		return mListView;
	}
	private SingleLineItemAdapter mListViewAdapter;
	private static final int UPDATE_PAGE_MSG = 0;
	private static final int REFRESH_LIST = 1;

//	private DownloadCallBackInterface mDownloadCallBack = null;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;

	/**
	 * 分类应用列表的数据
	 */
	private List<AppInfoBto> mListViewData;

	/**
	 * 组件Id(用来请求三级分页数据)
	 */
	private int mAssemblyId = -1;
	public void setAssemblyId(int assemblyId) {
		this.mAssemblyId = assemblyId;
	}
	/**
	 * 组件id列表(用来请求"全部"的分页数据)
	 */
	private int[] mAssemblyIds;
	public void setAssemblyIds(int[] assemblyIds) {
		this.mAssemblyIds = assemblyIds;
	}

	/**
	 * 判断左右滑动时是否需要重新初始化
	 */
	private boolean mInitFinish = false;

	private Handler mHandler;

	/**
	 * 网络请求参数
	 */
	GetSoftGameDetailReq mReq;
	public void setReq(GetSoftGameDetailReq req) {
		this.mReq = req;
		mTopicId = mReq.getTopicId()==-1 ? mReq.getParentId() : mReq.getTopicId();
		//mListViewAdapter.setTopicId(mTopicId);
	}
	
	//分页数据请求参数
	private int mGetListIndex = 0;
	private int mBaseNumber = 0;
	private int mFirstGetDataNum = 0;
	private final int REQ_DATA_NUM_ONCE = 16;
	//end

	private int mStartIndex;
	private int mEndIndex;
	private boolean mIsToBottom = false;
	private boolean mRefreshFinished = true;
	private boolean mGetListDataFail = false;


    private View mFooter = null;
    private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;
    
    private LinearLayout mSearch_loading;
    private LinearLayout mRefresh_linearLayout_id;
    private Button mRefreshButton;
    
    //统计参数
    private String mReportFlag = null;
    private int mTopicId = -1;
    //统计参数 end


	public CategoryDetailView(Context context, DownloadCallBackInterface callBack, String reportFlag) {
		super(context);
	    
		mReportFlag = reportFlag;
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mView = tLayoutInflater.inflate(R.layout.layout_category_detail_item, null);
		mFooter = tLayoutInflater.inflate(R.layout.foot, null);
		mListView = (ListView) mView.findViewById(R.id.category_app_list);
	}


	@Override
	public void entryView() {
	    super.entryView();     //it must be called
	    
		if (mInitFinish)
			return;

		initViews();

		mInitFinish = true;
	}
	
	
	/**
	 * 请求分页数据
	 */
    private void startRequestPageData() {
        setLayoutVisibility(false);
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
            String contents = SenderDataProvider.buildToJSONData(mContext,
                    MessageCode.GET_APK_LIST_BY_PAGE, req);
            StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST,
                    MessageCode.GET_APK_LIST_BY_PAGE, contents);
        }
    }


    /**
     * 请求第一屏数据
     */
	public void startRequestData() {
		if((mListViewData!=null && mListViewData.size()!=0) || !mRefreshFinished) {
			return;
		}
		
	    setLayoutVisibility(false);
	    
		try{
		    mRefreshFinished = false;
			String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_SOFT_GAME_DETAIL,mReq);
			StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_SOFT_GAME_DETAIL, contents);
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param dataList 第一次从分类页面带过来的数据
	 */
	public void setData(List<AppInfoBto> dataList) {
		mListViewData = dataList;
		
        if (mListViewData.size() > 0) {
    		mListViewAdapter.setDatas(mListViewData);
    		mListViewAdapter.notifyDataSetChanged();
        } else {
            startRequestPageData();
        }
	}
	

	public void setFirstGetDataNum(int num){
	    mFirstGetDataNum = num;
	}


	private void initViews() {
	    
	    mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
        
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListViewAdapter = new SingleLineItemAdapter(mContext, null, mDownloadCallBack.get(), Util.getWaterFlowLayoutId());
		mListViewAdapter.setReportFlag(mReportFlag);
		mListViewAdapter.setTopicId(mTopicId);
		mListView.addFooterView(mFooter);
		mListView.setAdapter(mListViewAdapter);
		
        mSearch_loading = (LinearLayout) mView.findViewById(R.id.search_loading);
        mRefresh_linearLayout_id = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout_id);
        mRefreshButton = (Button) mView.findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarketUtils.getAPNType(mContext) == -1) {
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                    return;
                }
                startRequestData();
            }
        });

		mHandler = new Handler() {

			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				HashMap<String, Object> map;
				int getDataSuccess = 0;
				List<AppInfoBto> list = null;
				
				switch (msg.what) {
				case UPDATE_PAGE_MSG:
					GetSoftGameDetailResp resp = null;
					map = (HashMap<String, Object>)msg.obj;
					
					if(map != null)
					{
					    resp = (GetSoftGameDetailResp)map.get("categoryDetailResp");
					    map.clear();
					}

					//二级和三级取app列表的方式不同
					List<AppInfoBto> appList = null;
					List<AssemblyInfoBto> assemblyList = null;
					if(resp != null) {
					    appList = resp.getAppInfoList();
					    assemblyList = resp.getAssemblyList();
					}

					if(appList == null && assemblyList != null) {
					    appList = assemblyList.get(0).getAppInfoList();
						mAssemblyId = assemblyList.get(0).getAssemblyId();
					}
					
					if(assemblyList!=null)
					    assemblyList.clear();
					
                    if (mListViewData == null)
                        mListViewData = new ArrayList<AppInfoBto>();
					int firstDataNum = 0;
					mGetListDataFail = true;
					if (appList != null && appList.size() > 0) {
						AppInfoBto appInfo = null;
                        int listSize = appList.size();
                        mFirstGetDataNum = listSize;
                        
                        for(int i=0; i<listSize; i++) {
                            appInfo = appList.get(i);
                            appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                            if(appInfo.getIsShow()) {
                                mListViewData.add(appInfo);   
                                firstDataNum++;
                            }
                        }
                        appList.clear();
                        
                        if(firstDataNum > 0){
    						mListViewAdapter.setDatas(mListViewData);
    						mListViewAdapter.notifyDataSetChanged();
						}
                        
                        mGetListDataFail = false;
                    } else {
                        mGetListDataFail = true;
                    }
					mRefreshFinished = true;
					if(!mGetListDataFail && firstDataNum == 0) {
					    startRequestPageData();
                    } else {
                        setLayoutVisibility(true);
                    }
					break;
				case REFRESH_LIST:
					GetApkListByPageResp listResp = null;
					getDataSuccess = msg.arg1;
					map = (HashMap<String, Object>)msg.obj;
					mGetListDataFail = true;
					int currDataNum = 0;
					if(getDataSuccess == 1) {			
						if (map != null){
							listResp = (GetApkListByPageResp) map.get("listByPage");
							list = listResp.getAppList();
						}

						if(list != null && list.size() > 0) {
                            AppInfoBto appInfo = null;
                            int listSize = list.size();
                            for(int i=0; i<listSize; i++) {
                                appInfo = list.get(i);
                                appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                                if(appInfo.getIsShow()) {
                                    mListViewData.add(appInfo);   
                                    currDataNum++;
                                }
                            }
                            
                            if(listSize < REQ_DATA_NUM_ONCE) {
                                mFooterProgress.setVisibility(View.GONE);
                                mFooterText.setText(mContext.getString(R.string.loaded_all_data));
                                mIsToBottom = true;
                                if(mListViewAdapter!=null)
                                    mListViewAdapter.allowRefreshIcon(true);
                            }
                            
                            if(currDataNum > 0) {
                                mListViewAdapter.setDatas(mListViewData);
                                mListViewAdapter.notifyDataSetChanged();
                            }
                            
                            mGetListDataFail = false; 
						} else {
						    mFooterProgress.setVisibility(View.GONE);
                            mFooterText.setText(mContext.getString(R.string.loaded_all_data));
                            mIsToBottom = true;
                            if(mListViewAdapter!=null)
                                mListViewAdapter.allowRefreshIcon(true);
						}
					}
					mRefreshFinished = true;
                    if(!mGetListDataFail && currDataNum == 0) {
                        startRequestPageData();
                    } else {
                        setLayoutVisibility(true);
                    }
					break;
					
					
				}
				super.handleMessage(msg);
			}

		};
	}


	private void asyncLoadImage() {
		if(mListViewAdapter == null) {
			return;
		}
		Drawable drawable = null;
		ImageView imageView = null;
		AppInfoBto info = null;
		int defaultResId = R.drawable.picture_bg1_big;
		
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (AppInfoBto) mListViewAdapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			
			imageView = (ImageView) mListView.findViewWithTag(info.getPackageName());
			if(imageView == null) {
				continue;
			}
			
			if (mListViewAdapter.isAllowRefreshIcon() == false) {
				break;
			}
			
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
				AsyncImageCache.from(mContext).displayImage(
						mListViewAdapter.isAllowRefreshIcon(),
						imageView,
						R.drawable.picture_bg1_big,
						new AsyncImageCache.NetworkImageGenerator(info
								.getPackageName(), info.getImgUrl()), true);
			}
		}    
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		switch (view.getId()) {
		case R.id.category_app_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount) {
				mEndIndex = totalItemCount - 1;
			}

			if (mListView.getVisibility() == View.VISIBLE 
					&& mIsToBottom == false 
					&& view.getLastVisiblePosition()+MarketUtils.LESS_ITEM_NUM >= (view.getCount() - 1) 
					&& mRefreshFinished
					&& mAssemblyId!=-1
					&& mInitFinish
					&& MarketUtils.getAPNType(mContext) != -1) {
				if(!mGetListDataFail) {
					mGetListIndex = mBaseNumber * REQ_DATA_NUM_ONCE + mFirstGetDataNum;
					mBaseNumber++;
				}
				mRefreshFinished = false;
				GetApkListByPageReq req= new GetApkListByPageReq();
				req.setAssemblyId(mAssemblyId);
				req.setAssemblyIds(mAssemblyIds);
				req.setStart(mGetListIndex);
				req.setFixedLength(REQ_DATA_NUM_ONCE);
				String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_APK_LIST_BY_PAGE,req);
				StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST,MessageCode.GET_APK_LIST_BY_PAGE,contents);
			}
			break;
		}

	}


	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mListViewAdapter!=null) {		
				mListViewAdapter.allowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			if(mListViewAdapter!=null)
				mListViewAdapter.allowRefreshIcon(false);
			break;
		}
	}


    public void setLayoutVisibility(boolean refreshButtonVisible) {
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


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (null != mListViewAdapter && null != mListViewData && mListViewData.size() > position) {
			AppInfoBto mAppInfoBto = (AppInfoBto) mListViewData.get(position) ;
			MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, mReportFlag, mTopicId);
		}
	}


	@Override
	public View getRootView() {
		return mView;
	}


	@Override
	public void freeViewResource() {
		super.freeViewResource();
	}


	@Override
	public void notifyDataSetChanged(String pkgName) {
		if (mListViewAdapter != null)
			mListViewAdapter.notifyDataSetChanged();
	}
}
