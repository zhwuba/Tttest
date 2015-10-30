package com.zhuoyi.market.home;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
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

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.data.ModelInfoBto;
import com.market.net.data.TopicInfoBto;
import com.market.net.request.GetModelApkListByPageReq;
import com.market.net.request.GetModelTopicRequest;
import com.market.net.response.GetMarketFrameResp;
import com.market.net.response.GetModelApkListByPageResp;
import com.market.net.response.GetModelTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.view.CommonMainTitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.AbsCustomView;


public class HomeView extends AbsCustomView implements OnClickListener, OnScrollListener {

	private Context mContext;
	private View mView;
	private ListView mHomeListView;
	private LinearLayout mSearch_loading;
	private LinearLayout mRefresh_linearLayout_id;
	private Button mRefreshButton;
	private HomeListAdapter mHomeListAdapter;
	private Handler mHandler;

	private boolean isHomeShow = false;
	
	private boolean mIsToBottom = false;
	private boolean mRefreshFinished = false;
    private final int UPDATE_HOME_MSG = 2;
    private final int UPDATE_SOFR_GAME_MSG = 3;
	private final int REFRESH_LIST_NEW = 6;
    private int mHomeAssId = 0;
    private int mModelIndex,mSonIndex;
    private GetMarketFrameResp mMarketFrameResp;
    private int mStartIndex,mEndIndex;
    private boolean mInitFinish = false;
      
    private View mFooter = null;
    private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;
    private DownloadCallBackInterface mDownloadCallBack;
    private int mPosition;
    
    private int mChannelIndex = -1;
    private int mTopicIndex = -1;
    private int mTopicId = -1;
    private String mSaveFrameName = null;
    private String mReportFlag = null;
    private boolean mFromFrame = true;
    private HomeAdView mHomeAdView;
    private boolean isNeedPaddingTop = false;
    
    public HomeView(Context context,DownloadCallBackInterface downloadCallback,int channelIndex, int topicIndex,String saveFrameName,String reportFlag, int position, boolean fromFrame) {
    	super(context);
    	mChannelIndex = channelIndex;
    	mTopicIndex = topicIndex;
    	mSaveFrameName = saveFrameName;
    	mReportFlag = reportFlag;
    	mFromFrame = fromFrame;
    	
		mContext = context;
		mDownloadCallBack = downloadCallback;
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		if (mChannelIndex != 0 || mTopicIndex != 0) {
		    mTopicId = MarketUtils.getTopicId(channelIndex, topicIndex);
		}
		mView = tLayoutInflater.inflate(R.layout.layout_recommend_page, null);
		if(mFromFrame) {
			mHomeAdView = new HomeAdView(mContext);
			mHomeAdView.enterAdView();
		}
		
		mFooter = tLayoutInflater.inflate(R.layout.foot, null);
		mFooter.setBackgroundColor(0xffffffff);
    }
    
    
    public HomeAdView getHomeAdView() {
        return mHomeAdView;
    }
    
	
	public View getMyView() {
		return mView;
	}
	
	
	public void freeViewResource() {
	    if(mHandler!=null)
	        mHandler = null;
	    
	    if(mHomeListAdapter!=null) {
	        mHomeListAdapter.freeImageCache();
	    }
	    if(mHomeAdView != null) {
	    	mHomeAdView.freeViewResource();
	    }
	}
	
	private void findViews() {
	    mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
	    mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
	    
		mHomeListView = getListView();
		mSearch_loading = (LinearLayout) mView.findViewById(R.id.search_loading);
		mRefresh_linearLayout_id = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout_id);	
		mRefreshButton = (Button) mView.findViewById(R.id.refresh_button);
		mRefreshButton.setOnClickListener(this);
		
		mHomeListView.setOnScrollListener(this);
		show(LOADING_VIEW);
	}
	
	private void initViews() {
	
		// set listview header and footer
		mHomeListView.addFooterView(mFooter);
		if(mFromFrame) {
			mHomeListView.addHeaderView(mHomeAdView.getMyView());
		}
		mHomeListAdapter = new HomeListAdapter(mContext, mDownloadCallBack);
		mHomeListAdapter.setReportFlag(mReportFlag);
		mHomeListAdapter.setTopicId(mTopicId);
		mHomeListView.setAdapter(mHomeListAdapter);

		mHandler = new Handler() {
			public void handleMessage(Message message) {
				HashMap<String, Object> map;
				switch (message.what) {
                case UPDATE_HOME_MSG:
                    mMarketFrameResp = MarketApplication.getMarketFrameResp();
                    refreshTheFirstPageData(mMarketFrameResp);
                    break;
                case UPDATE_SOFR_GAME_MSG:
                	try {
                		GetModelTopicResp modelListResp = null;
                		map = (HashMap<String, Object>) message.obj;
                		
                		if (map != null && map.size() > 0) {
                			modelListResp = (GetModelTopicResp) map.get("modelTopicInfo");
                			FrameInfoCache.saveFrameInfoToStorage(modelListResp, mSaveFrameName);
                		} else {
                			modelListResp = (GetModelTopicResp) FrameInfoCache.getFrameInfoFromStorage(mSaveFrameName);
                		}
                		
                		ModelInfoBto infoBto = modelListResp.getModelListInfoBto().getModel();
                		
                		mHomeAssId = infoBto.getAssemblyId();
                		mModelIndex = infoBto.getIndex();
                		mSonIndex = infoBto.getSonIndex();

                		List<AssemblyInfoBto> modelList = modelListResp.getModelListInfoBto().getModel().getAssemblyList();
                		List<HomeListBean> newList = convertData(modelList);
                		mHomeListAdapter.setDatas(newList);
                		mHomeListAdapter.notifyDataSetChanged();
                		show(HOME_LIST_VIEW);
                		mRefreshFinished = true;
                		
                	} catch (Exception e) {
                		mRefreshFinished = true;
                	}
                	
                	break;
                case REFRESH_LIST_NEW:
                	try {
                		GetModelApkListByPageResp modelListResp = null;
                		map = (HashMap<String, Object>) message.obj;
                		modelListResp = (GetModelApkListByPageResp) map.get("modelListByPage");

                		mModelIndex = modelListResp.getIndex();
                		mSonIndex = modelListResp.getSonIndex();
                		if(mSonIndex == -1) {
                			//                			 mHomeListAdapter.allowRefreshIcon(true);
                			mFooterProgress.setVisibility(View.GONE);
                			mFooterText.setText(mContext.getString(R.string.loaded_all_data));
                			mIsToBottom = true; 
                		}

                		List<AssemblyInfoBto> modelList = modelListResp.getAssemblyList();
                		List<HomeListBean> newList = convertData(modelList);
                		mHomeListAdapter.addDatas(newList);
                		mHomeListAdapter.notifyDataSetChanged();
                		mRefreshFinished = true;
                	} catch (Exception e) {
                		mRefreshFinished = true;
                	}
                	
                	break;
                    
				}
				super.handleMessage(message);
			}
		};
		
	}
	
	public ListView getListView() {
		try {
			if(mHomeListView == null) {
				mHomeListView = (ListView) mView.findViewById(R.id.recommmendation_list);
				mHomeListView.setTag(R.id.list_position, mPosition);
			}
			if (isNeedPaddingTop) {
			    int top = mContext.getResources().getDimensionPixelSize(R.dimen.common_listview_margin_top);
			    mHomeListView.setPadding(0, top, 0, 0);
	        }
			return mHomeListView;
		} catch (Exception e) {
			return null;
		}
	}
	

	public void notifyDataSetChanged(String pacName) {
		mHomeListAdapter.notifyDataSetChanged();
	}	

	public void scrollToTop() {
		mHomeListView.setSelection(0);
	}

	
	public void entryRecommendView() {
	    if(mInitFinish)
	        return;
	    mInitFinish = true;
	    
		findViews();
		initViews();
		
	}
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refresh_button:
			if (MarketUtils.getAPNType(mContext) == -1) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
				return;
			}

			if (mRefreshFinished) {
				mRefreshFinished = false;
				
				if (mFromFrame) {
					startRequestRecomendHome();
				} else {
					startRequestRecomendSoftAndGame();
				}
				show(LOADING_VIEW);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
		    CommonMainTitleView.setNeedHotWordChange(true);
			if(mHomeListAdapter!=null) {		
				mHomeListAdapter.allowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
		    CommonMainTitleView.setNeedHotWordChange(false);
			if(mHomeListAdapter!=null)
				mHomeListAdapter.allowRefreshIcon(false);
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
		
		if(mRefreshFinished && view.getLastVisiblePosition()+ MarketUtils.TWO_ROW_NUM >= view.getCount() - 1 
				&& MarketUtils.getAPNType(mContext) != -1 && mIsToBottom==false) {
			mRefreshFinished = false;

			GetModelApkListByPageReq req= new GetModelApkListByPageReq();
			req.setAssemblyId(mHomeAssId);
			req.setIndex(mModelIndex);//最后一个模块的下标
			req.setSonIndex(mSonIndex);//组后一个模块中appInfoList的最后一个AppInfoBto的下标
			String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_MODEL_APK_LIST_BY_PAGE,req);
			StartNetReqUtils.execListByPageRequest(mHandler, REFRESH_LIST_NEW,MessageCode.GET_MODEL_APK_LIST_BY_PAGE,contents);	
		}
	}
	
	
	private List<AppInfoBto> mBackupList = new ArrayList<AppInfoBto>();
	private List<HomeListBean> convertData(List<AssemblyInfoBto> modelList) { 
		
		ArrayList<HomeListBean> homeListData = new ArrayList<HomeListBean>();
		
		for(int i=0;i<modelList.size();i++) {
			AssemblyInfoBto modelItem = modelList.get(i);
			int type = modelItem.getType();
			int style = modelItem.getStyle();
			HomeListBean  bean = null;
			List<AppInfoBto> appInfoList = null;
			
			
			try {
				if(type ==6) {
					switch(style) {
					case 2:
						//带三个应用的专题
						appInfoList = modelItem.getAppInfoList();
						bean = new HomeListBean(); 
						bean.setAssemblyId(modelItem.getAssemblyId());
						bean.setTitleName(modelItem.getAssName());
						bean.setTopicImgUrl(appInfoList.get(0).getImgUrl());
						bean.setAppInfo00(appInfoList.get(0));
						bean.setAppInfo01(appInfoList.get(1).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(1).getFileSize(),false)));
						bean.setAppInfo02(appInfoList.get(2).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(2).getFileSize(),false)));
						bean.setAppInfo03(appInfoList.get(3).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(3).getFileSize(),false)));
						bean.setItemType(2);
						homeListData.add(bean);
						break;
					default:
						//专题大图
						appInfoList = modelItem.getAppInfoList();
						AppInfoBto appInfo = appInfoList.get(0);
						bean = new HomeListBean();
						bean.setAssemblyId(modelItem.getAssemblyId());
						bean.setAppInfo00(appInfo);
						bean.setTopicImgUrl(appInfo.getImgUrl());
						bean.setItemType(6);
						homeListData.add(bean);
						break;
					}
				} else if(type == 16) {
					switch(style) {
					case 2:
						//竖着三个的单列表
						appInfoList = modelItem.getAppInfoList();
						appInfoList.addAll(0, mBackupList);
						mBackupList.clear();
						//过滤已经下发过但是未显示的应用(前一次数据不满一行三个条件的应用)
						Iterator<AppInfoBto> iterator = appInfoList.iterator();
						String threeExisted = mHomeListAdapter.getExisited();
						while(iterator.hasNext()) {
							AppInfoBto appInfo = iterator.next();
							if(!appInfo.getIsShow() 
							        || threeExisted.contains(appInfo.getMd5())) {
								iterator.remove();
							} 
						}
						
						int size = appInfoList.size();
						int validSize = size - size % 3;

						for(int j=0; j<size; j++) {
							AppInfoBto appInfo = appInfoList.get(j);
							if(j<validSize) {
								appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
								switch(j%3) {
								case 0:
									bean = new HomeListBean();
									bean.setAssemblyId(modelItem.getAssemblyId());
									bean.setItemType(1);
									homeListData.add(bean);
									bean.setAppInfo01(appInfo);
									break;
								case 1:
									bean.setAppInfo02(appInfo);
									break;
								case 2:
									bean.setAppInfo03(appInfo);
								}
								mHomeListAdapter.setExisited(appInfo.getMd5());
                                threeExisted = appInfo.getMd5() + ";" + threeExisted;
							} else {
								mBackupList.add(appInfo);
							}
						}
						break;
					default:
						//单行的列表
						appInfoList = modelItem.getAppInfoList();
						appInfoList.addAll(0, mBackupList);
						mBackupList.clear();
						String oneExisted = mHomeListAdapter.getExisited();
						for(int j=0; j<appInfoList.size(); j++) {
							AppInfoBto appInfo = appInfoList.get(j);
							appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
							if(!appInfo.getIsShow() 
							        || oneExisted.contains(appInfo.getMd5())) {
								continue;
							}
							mHomeListAdapter.setExisited(appInfo.getMd5());
							oneExisted = appInfo.getMd5() + ";" + oneExisted;
							
							bean = new HomeListBean();
							bean.setAssemblyId(modelItem.getAssemblyId());
							bean.setItemType(5);
							bean.setAppInfo01(appInfo);
							homeListData.add(bean);
						}
					}
				} else if(type == 17) {
					switch(style) {
					case 2:
						// 榜单带三个竖着应用
						appInfoList = modelItem.getAppInfoList();
						bean = new HomeListBean(); 
						bean.setAssemblyId(modelItem.getAssemblyId());
						bean.setTitleName(modelItem.getAssName());
						bean.setAppInfo01(appInfoList.get(0).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(0).getFileSize(),false)));
						bean.setAppInfo02(appInfoList.get(1).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(1).getFileSize(),false)));
						bean.setAppInfo03(appInfoList.get(2).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(2).getFileSize(),false)));
						bean.setItemType(3);
						homeListData.add(bean);
						break;
					default:
						// 榜单带三个横着应用
						appInfoList = modelItem.getAppInfoList();
						bean = new HomeListBean(); 
						bean.setAssemblyId(modelItem.getAssemblyId());
						bean.setTitleName(modelItem.getAssName());
						bean.setAppInfo01(appInfoList.get(0).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(0).getFileSize(),false)));
						bean.setAppInfo02(appInfoList.get(1).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(1).getFileSize(),false)));
						bean.setAppInfo03(appInfoList.get(2).setFileSizeToString(MarketUtils.humanReadableByteCount(appInfoList.get(2).getFileSize(),false)));
						bean.setItemType(4);
						homeListData.add(bean);
					}
				} else {
					//接收了其他未定义的类型使用单行列表类型
					appInfoList = modelItem.getAppInfoList();
					for(int j=0; j<appInfoList.size(); j++) {
						AppInfoBto appInfo = appInfoList.get(j);
						appInfo.setFileSizeToString(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
						bean = new HomeListBean();
						bean.setAssemblyId(modelItem.getAssemblyId());
						bean.setItemType(5);
						bean.setAppInfo01(appInfo);
						homeListData.add(bean);
					}
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		return homeListData;
	}
	
	
	private void refreshTheFirstPageData(GetMarketFrameResp resp) {
		try {
			List<TopicInfoBto> topicList =  resp.getChannelList().get(mChannelIndex).getTopicList();
			if (topicList.size()==0) {
                return;
            }
			if(mFromFrame && mHomeAdView != null) {
				mHomeAdView.setAdData(resp);
			}
			ModelInfoBto modelInfo = topicList.get(mTopicIndex).getModel();
			List<AssemblyInfoBto> modelList = modelInfo.getAssemblyList();
			mHomeAssId = modelInfo.getAssemblyId();
			mModelIndex = modelInfo.getIndex();
			mSonIndex = modelInfo.getSonIndex();

			List<HomeListBean> homeListData = convertData(modelList);
			mHomeListAdapter.setDatas(homeListData);
			mHomeListAdapter.notifyDataSetChanged();
			
			show(HOME_LIST_VIEW);
			mRefreshFinished = true;
			
		} catch (NullPointerException e) {
			
		} catch (IndexOutOfBoundsException e) {
			
		}
	}
	
	
	/*class TopicListener implements OnClickListener {

		private TopicInfoBto topic;
		private int index;
		
		public TopicListener(TopicInfoBto topic,int index) {
			this.topic = topic;
			this.index = index;
		}
		
		@Override
		public void onClick(View v) {
			switch(topic.getTopicType()) {
			case 0:
				//飙升
				Intent topicIntent = new Intent(mContext, CommonTabActivity.class);
				topicIntent.putExtra("titleName", topic.getTopicName());
				topicIntent.putExtra("leftTabName", mContext.getString(R.string.home_apps_game));
				topicIntent.putExtra("rightTabName", mContext.getString(R.string.home_apps_sw));
				topicIntent.putExtra("viewTypes", new int[]{CustomViewFactory.VIEW_BEST_IN_GAME,CustomViewFactory.VIEW_BEST_IN_SOFT});
				topicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(topicIntent);
				break;
				
			case 1:
				//1xN
				Intent topicIntent1 = new Intent(mContext, OneColModelActivity.class);
				topicIntent1.putExtra("titleName", topic.getTopicName());
				topicIntent1.putExtra("channelIndex", 0);
				topicIntent1.putExtra("topicIndex", index);
				topicIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(topicIntent1);
				break;
			case 2:
				//2xN 
				Intent topicIntent2 = new Intent(mContext,NecessaryInstallActivity.class);
				topicIntent2.putExtra("titleName", topic.getTopicName());
				topicIntent2.putExtra("channelIndex", 0);
				topicIntent2.putExtra("topicIndex", index);
				topicIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(topicIntent2);
				break;
			case 3:
				//Topic_image
				Intent topicIntent3 = new Intent(mContext,TopicImgModelActivity.class);
				topicIntent3.putExtra("titleName", topic.getTopicName());
				topicIntent3.putExtra("channelIndex", 0);
				topicIntent3.putExtra("topicIndex", index);
				topicIntent3.putExtra("logDes", UserLogSDK.getKeyDes(LogDefined.ACTIVITY_MAIN_CLUB));
				topicIntent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(topicIntent3);
				break;
			case 4:
				//webview
				Intent topicIntent4 = new Intent(mContext,BaseHtmlActivity.class);
//				topicIntent4.putExtra("titleName", topic.getTopicName());
				topicIntent4.putExtra("wbUrl", topic.getWbUrl());
//				topicIntent4.putExtra("showImage", true);
				topicIntent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mContext.startActivity(topicIntent4);
				break;
			default:
			}
		}
		
	}*/

	public void startRequestRecomendHome() {
		if(mHandler!=null) {
			mHandler.sendEmptyMessage(UPDATE_HOME_MSG);
		}
	}
	
	public void startRequestRecomendSoftAndGame() {
		String contents = "";
		GetModelTopicRequest mReq = new GetModelTopicRequest();
		mReq.setChannelIndex(mChannelIndex);
		mReq.setTopicIndex(mTopicIndex);
		contents = SenderDataProvider.buildToJSONData(mContext,
				MessageCode.GET_MODEL_TOPIC_REQ, mReq);
		try {
			StartNetReqUtils.execMarketRequest(mHandler, UPDATE_SOFR_GAME_MSG, MessageCode.GET_MODEL_TOPIC_REQ, contents);
		} catch (RejectedExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private static final int HOME_LIST_VIEW=0,LOADING_VIEW=1, REFRESH_VIEW=2;
	private void show(int whichView) {
		switch(whichView) {
		case HOME_LIST_VIEW:
			mHomeListView.setVisibility(View.VISIBLE);
			mSearch_loading.setVisibility(View.GONE);
			mRefresh_linearLayout_id.setVisibility(View.GONE);
			break;
		case LOADING_VIEW:
			mHomeListView.setVisibility(View.GONE);
			mSearch_loading.setVisibility(View.VISIBLE);
			mRefresh_linearLayout_id.setVisibility(View.GONE);
			break;
		case REFRESH_VIEW:
			mHomeListView.setVisibility(View.GONE);
			mSearch_loading.setVisibility(View.GONE);
			mRefresh_linearLayout_id.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	
    /**
     * 是否在首页:true=在首页 false=切换到其它页面
     * @param show
     */
    public void setHomeShow(boolean show) {
        isHomeShow = show;
    }
    
	private void asyncLoadImage() {
		if(mHomeListAdapter == null) {
			return;
		}
		HomeListBean info = null;
		ImageView imageView00, imageView01, imageView02, imageView03;
		AppInfoBto appInfoBto00, appInfoBto01, appInfoBto02, appInfoBto03;
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (HomeListBean) mHomeListAdapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			switch (mHomeListAdapter.getItemViewType(mStartIndex)) {
				case HomeListAdapter.ITEM_VIEW_TYPE_00:
					break;
				case HomeListAdapter.ITEM_VIEW_TYPE_01:
				case HomeListAdapter.ITEM_VIEW_TYPE_03:
				case HomeListAdapter.ITEM_VIEW_TYPE_04:
					appInfoBto01 = info.getAppInfo01();
					appInfoBto02 = info.getAppInfo02();
					appInfoBto03 = info.getAppInfo03();
					imageView01 = (ImageView) mHomeListView.findViewWithTag(appInfoBto01.getPackageName());
					imageView02 = (ImageView) mHomeListView.findViewWithTag(appInfoBto02.getPackageName());
					imageView03 = (ImageView) mHomeListView.findViewWithTag(appInfoBto03.getPackageName());
					loadImage(appInfoBto01, imageView01);
					loadImage(appInfoBto02, imageView02);
					loadImage(appInfoBto03, imageView03);
					
					break;
				case HomeListAdapter.ITEM_VIEW_TYPE_02:
					appInfoBto00 = info.getAppInfo00();
					appInfoBto01 = info.getAppInfo01();
					appInfoBto02 = info.getAppInfo02();
					appInfoBto03 = info.getAppInfo03();
					imageView00 = (ImageView) mHomeListView.findViewWithTag(MarketUtils.getImgUrlKey(info.getTopicImgUrl()));
					imageView01 = (ImageView) mHomeListView.findViewWithTag(appInfoBto01.getPackageName());
					imageView02 = (ImageView) mHomeListView.findViewWithTag(appInfoBto02.getPackageName());
					imageView03 = (ImageView) mHomeListView.findViewWithTag(appInfoBto03.getPackageName());
					loadTopicImage(info, imageView00);
					loadTopicSmallImage(appInfoBto01, imageView01);
					loadTopicSmallImage(appInfoBto02, imageView02);
					loadTopicSmallImage(appInfoBto03, imageView03);
					break;
				
				case HomeListAdapter.ITEM_VIEW_TYPE_05:
					appInfoBto01 = info.getAppInfo01();
					imageView01 = (ImageView) mHomeListView.findViewWithTag(appInfoBto01.getPackageName());
					loadImage(appInfoBto01, imageView01);
					break;
				case HomeListAdapter.ITEM_VIEW_TYPE_06:
					imageView00 = (ImageView) mHomeListView.findViewWithTag(MarketUtils.getImgUrlKey(info.getTopicImgUrl()));
					loadTopicImage(info, imageView00);
					break;
	
				default:
					break;
			}
			if (mHomeListAdapter.isAllowRefreshIcon() == false) {
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
					mHomeListAdapter.isAllowRefreshIcon(),
					imageView,
					defaultResId,
					new AsyncImageCache.NetworkImageGenerator(info.getPackageName(),info.getImgUrl()), true);
		}
	}
	
	/**
	 * 加载专题小图片
	 * @param bean
	 * @param imageView
	 */
	private void loadTopicSmallImage(AppInfoBto info, ImageView imageView) {
	    if(imageView == null) return;
        int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
        int defaultResId = R.drawable.topic_small_default;
        int mTopicSmallSize = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_small_size);
        if (resId == defaultResId) {
            AsyncImageCache.from(mContext).displayImage(mHomeListAdapter.isAllowRefreshIcon(), false, imageView, defaultResId, mTopicSmallSize, mTopicSmallSize,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(info.getPackageName()), info.getImgUrl()), false, true, false, "");
        }
	}
	
	/**
	 * 加载专题图片
	 * @param bean
	 * @param imageView
	 */
	private void loadTopicImage(HomeListBean bean, ImageView imageView) {
		if(imageView == null) return;
		int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
		int imgWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_item_width);
        int imgHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_big_height);
		int defaultResId = -1;
        if(MarketUtils.isNoPicModelReally()) {
            defaultResId = R.drawable.logo_no_network;
        } else {
            defaultResId = R.drawable.logo_no_network_withtxt;
        }
		if(resId == defaultResId) {
		    AsyncImageCache.from(mContext).displayImage(mHomeListAdapter.isAllowRefreshIcon(),false,imageView, defaultResId,imgWidth,imgHeight,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(bean.getTopicImgUrl()),bean.getTopicImgUrl()), false,true,false,"");
		}
	}


	@Override
	public View getRootView() {
		// TODO Auto-generated method stub
		return mView;
	}


	/**
     * {listview是否需要贴顶--精选(贴顶)，推荐页面要有一定的空隙}.
     */
    public void setNeedPaddingTop(boolean isNeedPaddingTop) {
        this.isNeedPaddingTop = isNeedPaddingTop;
    }
}
