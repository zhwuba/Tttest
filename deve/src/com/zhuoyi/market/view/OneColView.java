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

import com.market.behaviorLog.UserLogSDK;
import com.market.download.userDownload.DownloadManager;
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
import com.market.statistics.ReportFlag;
import com.market.view.CommonMainTitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.SingleLineItemAdapter;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 通用 显示单行列表的view
 * @author JLu
 *
 */
public class OneColView extends AbsCustomView implements OnScrollListener, OnItemClickListener {

	private int mStartIndex;
	private int mEndIndex;
	private View mView;
	private LinearLayout mSearch_loading;
	private LinearLayout mRefresh_linearLayout_id;
	private Button mRefreshButton;
	private Handler mHandler;
	private List<AppInfoBto> mListViewData;
	private SingleLineItemAdapter mListViewAdapter = null;
	private int mGetListIndex = 0;
	private int mBaseNumber = 0;
	private int mFirstGetDataNum = 0;
	private static final int REQ_DATA_NUM_ONCE = 16;
	private static final int UPDATE_PAGE_MSG = 1;
	private static final int REFRESH_LIST = 2;
	private boolean mRefreshFinished = true;
	private boolean mGetListDataFail = false;
	private boolean mIsToBottom = false;
	private GetTopicReq mTopicReq;
	private int mAssemblyId = -1;
	private boolean mInitFinish = false;
	private final int mChannelIndex, mTopicIndex, mTopicId;
	private final int mDataIndex;
	private String mSaveFrameName;
	public String mReportFlag;
	
	private View mFooter = null;
    private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;

	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	
	private ListView mListView;
	@Override
	public ListView getListView() {
		return mListView;
	}
	

	/**
	 * 构造函数
	 * @param context
	 * @param downloadCallback 下载回调接口
	 * @param channelIndex channelList的下标 用于定位channelID
	 * @param topicIndex topicList的下标 用于定位topicID 
	 * @param saveFrameName 保存缓存数据的文件名, 传null或""代表不读/写缓存数据
	 * @param reportFlag 统计参数 未指定请填写 ReportFlag.FROM_NULL
	 * @param dataIndex 选取下发的数据中的哪一列,通常只有一列
	 */
	public OneColView(Context context,DownloadCallBackInterface downloadCallback,int channelIndex, int topicIndex,String saveFrameName,String reportFlag,int dataIndex) {
		this(context, downloadCallback, channelIndex, topicIndex, saveFrameName, reportFlag, dataIndex, -1);
	}

	
	public OneColView(Context context,DownloadCallBackInterface downloadCallback,int channelIndex, int topicIndex,String saveFrameName,String reportFlag,int dataIndex, int assemblyId) {
		super(context);
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(downloadCallback);
		
		mChannelIndex = channelIndex;
		mTopicIndex = topicIndex;
		mDataIndex = dataIndex;
		mSaveFrameName = saveFrameName;
		mReportFlag = reportFlag;
		mAssemblyId = assemblyId;
		
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mView = tLayoutInflater.inflate(R.layout.layout_recommendation, null);
		mListView = (ListView) mView.findViewById(R.id.recommmendation_list);
		mTopicReq = new GetTopicReq();
		mFooter = tLayoutInflater.inflate(R.layout.foot, null);
		
		mTopicId = MarketUtils.getTopicId(channelIndex, topicIndex);
	}
	
	@Override
    public void setTitleName(String titleName) {
        super.setTitleName(titleName);
        if (mTitleName != null) {
            setLogDes(UserLogSDK.getRankChildActivityDes(mTitleName, mAssemblyId));
        }
    }
	
	@Override
	public View getRootView() {
		return mView;
	}
	
	
	@Override
    public void openStatisticsWhenEntry() {
        super.openStatisticsWhenEntry();
        
        if (isStatisticsOpen()) {
            DownloadManager.startServiceReportOffLineLog(mContext, ReportFlag.ACTION_VIEW_COLUMN, mReportFlag);
        }
    }

	
	@Override
	public void entryView() {
	    super.entryView();     //it must be called
	    
		if (mInitFinish)
			return;

		findViews();
		initViews();

		mInitFinish = true;

	}
	
	
	@Override
	public void freeViewResource() {
	    super.freeViewResource();
		if(mListViewAdapter!=null)
			mListViewAdapter.freeImageCache();

		mHandler = null;
	}
	
	
	private void findViews() {
	    
	    mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
	    
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this) ;

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
				startRequestData();
			}
		});
	}

	private void initViews() {
		mListViewData = new ArrayList<AppInfoBto>();
		mHandler = new Handler() {
			public void handleMessage(Message message) {
				HashMap<String, Object> map;
				List<AppInfoBto> list=null;
				int i = 0;
				int getDataSuccess = 0;
				
				if(mContext == null)
				    return;
				switch (message.what) {
				case REFRESH_LIST:
					GetTopicResp mTopicResp = null;
					map = (HashMap<String, Object>) message.obj;

					if(map != null && map.size()>0) {
						mTopicResp = (GetTopicResp) map.get("topicResp");
						
						map.clear();
						FrameInfoCache.saveFrameInfoToStorage(mTopicResp, mSaveFrameName);
					} else {
						mTopicResp = (GetTopicResp) FrameInfoCache.getFrameInfoFromStorage(mSaveFrameName);
					}
					if(mTopicResp != null) {
						list = getTopicApplist(mTopicResp);
					}
					
					int firstDataNum = 0;
					if(list != null && list.size() > 0) {
					    
	                    AppInfoBto appInfo = null;
                        int firstListSize = list.size();
                        for (i = 0; i < firstListSize; i++) {
                            appInfo = list.get(i);
                            appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                            if(appInfo.getIsShow()) {
                                mListViewData.add(appInfo);
                                firstDataNum++;
                            }                           
                        }
                        list = null;
                        if(mGetListIndex==0 && mListViewAdapter==null) {
                            mFirstGetDataNum = firstListSize;
                    		mListViewAdapter = new SingleLineItemAdapter(mContext, mDownloadCallBack.get(), Util.getWaterFlowLayoutId());
                            mListViewAdapter.setDatas(mListViewData);
                            mListViewAdapter.setReportFlag(mReportFlag);
                            mListViewAdapter.setTopicId(mTopicId);
                            //应用或游戏排行显示序号
                    		if((mTopicIndex == 3 && mChannelIndex == 3)
                    		        || (mTopicIndex == 3 &&  mChannelIndex == 2)) {
                    			mListViewAdapter.setShowIndexParam(true);
                    			mListViewAdapter.setShowRankChange(true);
                    		}
                    		//飙升显示序号和排名变动
                    		if(mTopicIndex == 1 && mChannelIndex == 0) {
                    			mListViewAdapter.setShowIndexParam(true);
                    			mListViewAdapter.setShowRankChange(true);
                    		}
                            mListView.addFooterView(mFooter);
                            mListView.setAdapter(mListViewAdapter);
                            mListViewAdapter.notifyDataSetChanged();
                        }
                        
                        mGetListDataFail = false;
					} else {
                        mGetListDataFail = true;
					}
					mRefreshFinished = true;
					if(!mGetListDataFail && firstDataNum == 0) {
					    startRequestData();
					} else {
    					setLayoutVisibility(true);
					}
					break;
				case UPDATE_PAGE_MSG:
					GetApkListByPageResp listResp = null;
					getDataSuccess = message.arg1;
					map = (HashMap<String, Object>)message.obj;
					mGetListDataFail = true;
					int currDataNum = 0;
					if(getDataSuccess == 1) {			
						if (map != null) {
							listResp = (GetApkListByPageResp) map.get("listByPage");
							map.clear();
							list = listResp.getAppList();
						}

						if(list != null && list.size() > 0) {
                            AppInfoBto appInfo = null;
                            int listSize = list.size();
                            for(i=0; i<listSize; i++) {
                                appInfo = list.get(i);
                                appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
                                if(appInfo.getIsShow()){
                                    mListViewData.add(appInfo);   
                                    currDataNum++;
                                }
                            }
                            list = null;
                            
                            if(mGetListIndex==0 && mListViewAdapter==null) {
                                mFirstGetDataNum = listSize;
                                mListViewAdapter = new SingleLineItemAdapter(mContext, mDownloadCallBack.get(), Util.getWaterFlowLayoutId());
                                mListViewAdapter.setDatas(mListViewData);
                                mListViewAdapter.setReportFlag(mReportFlag);
                                mListViewAdapter.setTopicId(mTopicId);
                                //应用或游戏排行显示序号
                        		if((mTopicIndex == 3 && mChannelIndex == 3)
                        		        || (mTopicIndex == 3 &&  mChannelIndex == 2)) {
                        			mListViewAdapter.setShowIndexParam(true);
                        			mListViewAdapter.setShowRankChange(true);
                        		}
                        		//飙升显示序号和排名变动
                        		if(mTopicIndex == 1 && mChannelIndex == 0) {
                        			mListViewAdapter.setShowIndexParam(true);
                        			mListViewAdapter.setShowRankChange(true);
                        		}
                                mListView.addFooterView(mFooter);
                                mListView.setAdapter(mListViewAdapter);
                                mListViewAdapter.notifyDataSetChanged();
                            } else {
                            	if(currDataNum > 0) {
                                    mListViewAdapter.setDatas(mListViewData);
                                    mListViewAdapter.notifyDataSetChanged();
                                }
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

	@Override
	public void notifyDataSetChanged(String pacName) {

		if(mListView == null
				||mListViewAdapter == null)return;

		if(pacName == null) {
			mListViewAdapter.notifyDataSetChanged();
			return;
		}

		int first = mListView.getFirstVisiblePosition();
		int last = mListView.getLastVisiblePosition();

		for(int i=first; i<=last; i++) {
			if(pacName.equals(mListViewData.get(i).getPackageName())) {
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
			if(mListViewAdapter!=null) {		
				mListViewAdapter.allowRefreshIcon(true);
				//mListViewAdapter.notifyDataSetChanged();
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
		    CommonMainTitleView.setNeedHotWordChange(false);
			if(mListViewAdapter!=null)
				mListViewAdapter.allowRefreshIcon(false);
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		switch (view.getId()) {
		case R.id.recommmendation_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount) {
				mEndIndex = totalItemCount - 1;
			}

			if (mListView.getVisibility() == View.VISIBLE 
			        && mIsToBottom==false 
			        && view.getLastVisiblePosition()+MarketUtils.LESS_ITEM_NUM >= (view.getCount() - 1) 
			        && mRefreshFinished
			        && mAssemblyId!=-1
			        && MarketUtils.getAPNType(mContext) != -1) {
				if(!mGetListDataFail) {
					mGetListIndex = mBaseNumber * REQ_DATA_NUM_ONCE + mFirstGetDataNum;
					mBaseNumber++;
				}
				mRefreshFinished = false;
				GetApkListByPageReq req= new GetApkListByPageReq();
				req.setAssemblyId(mAssemblyId);
				req.setStart(mGetListIndex);
                req.setFixedLength(REQ_DATA_NUM_ONCE);
				String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_APK_LIST_BY_PAGE,req);
				StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG,MessageCode.GET_APK_LIST_BY_PAGE,contents);
			}
			break;
		}
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		//see the detail of the app
		if (null != mListViewAdapter 
		        && null != mListViewData 
		        && position < mListViewData.size()) {
		    AppInfoBto mAppInfoBto = (AppInfoBto) mListViewData.get(position);
		    MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, mReportFlag, mTopicId);
		}
	}
	
	
	private void startRequestData() {
	    setLayoutVisibility(false);
	    if(mAssemblyId != -1) {
	    	//如果第一次请求Topic失败,再去请求分页接口
            if (!mIsToBottom && mRefreshFinished) {
                if(!mGetListDataFail) {
                    mGetListIndex = mBaseNumber * REQ_DATA_NUM_ONCE + mFirstGetDataNum;
                    mBaseNumber++;
                }
                mRefreshFinished = false;
                GetApkListByPageReq req= new GetApkListByPageReq();
                req.setAssemblyId(mAssemblyId);
                req.setStart(mGetListIndex);
                req.setFixedLength(REQ_DATA_NUM_ONCE);
                String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_APK_LIST_BY_PAGE,req);
                StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG,MessageCode.GET_APK_LIST_BY_PAGE,contents);
            } else if(mIsToBottom) {
            	//首次下发的第一屏数据都被过滤,并且服务端无更多数据,点击刷新按钮不处理
            	setLayoutVisibility(true);
            }
	    } else {
	    	//第一次进入请求Topic数据,如果失败再次进入该方法请求
	        if(mRefreshFinished){
        		try {
        		    mRefreshFinished = false;
        			mTopicReq.setChannelIndex(mChannelIndex);
        			mTopicReq.setTopicIndex(mTopicIndex);
        			String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_TOPIC_LIST,mTopicReq);
        			StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST, MessageCode.GET_TOPIC_LIST, contents);
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
    		}
		}
	}

	
	private List<AppInfoBto> getTopicApplist(GetTopicResp resp) {
		List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();
		AssemblyInfoBto assemblyInfo = null;

		if(assemblyList==null || assemblyList.size()<=0)
			return null;
		try {
			assemblyInfo = assemblyList.get(mDataIndex);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
		if(assemblyInfo==null)
			return null;

		mAssemblyId = assemblyInfo.getAssemblyId();

		if(assemblyInfo.getAppInfoListSize()<=0)
			return null;

		return assemblyInfo.getAppInfoList();	    
	}
	

	private void setLayoutVisibility(boolean refreshButtonVisible) {
	    if(mListViewData != null && mListViewData.size() > 0) {
	        if(mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
	            mRefresh_linearLayout_id.setVisibility(View.GONE);
	        }
            if(mSearch_loading.getVisibility() == View.VISIBLE) {
                mSearch_loading.setVisibility(View.GONE);
            }
            if(mListView.getVisibility() == View.GONE) {
                mListView.setVisibility(View.VISIBLE);
            }
	    } else {
            if(mListView.getVisibility() == View.VISIBLE) {
                mListView.setVisibility(View.GONE);
            }
            if(refreshButtonVisible) {
                if(mSearch_loading.getVisibility() == View.VISIBLE) {
                    mSearch_loading.setVisibility(View.GONE);
                }
                if(mRefresh_linearLayout_id.getVisibility() == View.GONE) {
                    mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
                }
            } else {
                if(mRefresh_linearLayout_id.getVisibility() == View.VISIBLE) {
                    mRefresh_linearLayout_id.setVisibility(View.GONE);
                }
                if(mSearch_loading.getVisibility() == View.GONE) {
                    mSearch_loading.setVisibility(View.VISIBLE);
                }
            }
	    }
	}
	
	
	private void asyncLoadImage() {
		if(mListViewAdapter == null) {
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
			if(imageView == null) {
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
						new AsyncImageCache.NetworkImageGenerator(info
								.getPackageName(), info.getImgUrl()), true);
			}
		}    
	}

}
