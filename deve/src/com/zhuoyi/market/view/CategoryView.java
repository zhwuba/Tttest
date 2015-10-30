package com.zhuoyi.market.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyi.market.CategoryDetailActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.CategoryAdapter;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.request.GetSoftGameTopicReq;
import com.market.net.response.GetTopicResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 应用或游戏的分类页面
 * @author JLu
 *
 */
public class CategoryView extends AbsCustomView {
	private View mView;
	private GetSoftGameTopicReq mGetSoftGameTopicReq;
	private Handler mHandler;
	private static final int UPDATE_PAGE_MSG = 1;
	
	private LinearLayout mRequstLoading;
	private LinearLayout mRefreshLayout;
	/**
	 * 热门分类布局
	 */
	private View mHotCategoryView;
	/**
	 * 热门分类ImageView集合
	 */
	private List<ImageView> mHotImgs = new ArrayList<ImageView>();
	/**
	 * 热门分类图片url集合
	 */
	private List<String> mHotImgUrls = new ArrayList<String>();
	
	private ListView mListView;
	@Override
	public ListView getListView() {
		return mListView;
	}
	
	private CategoryAdapter mListViewAdapter;
	private Button mRefreshButton;
	
	private static final int CATEGORY_VIEW = 0;
	private static final int LOADING_VIEW = 1;
	private static final int REFRESH_VIEW = 2;
	
	private int[] mHotItemList,mHotIconList,mHotNameList;
	
	
	/**
	 * 刷新结束的flag,防止反复点击刷新按钮
	 */
	private boolean mRefreshFinished = false;
	
	
	private int mChannelIndex = -1;
    private int mTopicIndex = -1;
    private String mSaveFrameName;
    
	/**
	 * 判断是否已经实例化过
	 */
	private boolean mInitFinished = false;
	
	private String mReportFlag = null;
	
	/**
	 * <code>mHotViewStyle</code> - {style: 1 矩形图标 2圆形图标 0:默认展示}.
	 */
	private int mHotViewStyle = 0;
	
	/**
	 * <code>mCategoryViewStyle</code> - {style:1 靠左展示 2:平铺展示 0:默认展示}.
	 */
	private int mCategoryViewStyle = 0;
	
	/**
	 * <code>resId</code> - {热门分类默认加载图片id}.
	 */
	private int resId;
	
	private String mDetailLogTag = null;

	/**
	 * 请求应用或游戏的分类信息会用到后两个参数
	 * @param context
	 * @param channelIndex channelList的下标 用于定位channelID
	 * @param topicIndex  topicList的下标 用于定位topicID 
	 * @param saveFrameName 保存缓存数据的文件名, 传null或""代表不读/写缓存数据
	 * @param reportFlag 统计参数
	 */
	public CategoryView(Context context,int channelIndex,int topicIndex,String saveFrameName, String reportFlag) {
		super(context);
		
		mReportFlag = reportFlag;
		mChannelIndex = channelIndex;
		mTopicIndex = topicIndex;
		mSaveFrameName = saveFrameName;
		mView = LayoutInflater.from(context).inflate(R.layout.layout_category, null);
		mListView = (ListView) mView.findViewById(R.id.category_listview);
		mGetSoftGameTopicReq = new GetSoftGameTopicReq();
		
	}
	
	
	public CategoryView setDetailLogTag(String logTag) {
	    mDetailLogTag = logTag;
	    return this;
	}
	
	
	/**
	 * 无图模式开启再关闭回来刷新页面
	 */
//	public void notifyDataSetChanged() {
//		if(mListViewAdapter != null) {
//			mListViewAdapter.notifyDataSetChanged();
//		}
//		for(int i=0;i<mHotImgs.size();i++) {
//			AsyncImageCache.from(mContext).displayImage(true,mHotImgs.get(i), R.drawable.category_hot_icon_bg,
//	                new AsyncImageCache.NetworkImageGenerator(Util.getImgUrlKey(mHotImgUrls.get(i)),mHotImgUrls.get(i)), false);
//		}
//	}
//
//	
//	public View getMyView() {
//		return mView;
//	}
//	
//	
//	public void entryCategoryView() {
//	    if(mInitFinished)
//	        return;
//	    
//	    findViews();
//		initViews();
//		
//		 mInitFinished = true;
//	}
	
	
	private void findViews() {
		mHotItemList = new int[] {R.id.item01,R.id.item02,R.id.item03,R.id.item04};
		mHotIconList = new int[] {R.id.icon01,R.id.icon02,R.id.icon03,R.id.icon04};
		mHotNameList = new int[] {R.id.name01,R.id.name02,R.id.name03,R.id.name04};
		
		
		mRequstLoading = (LinearLayout) mView.findViewById(R.id.search_loading);
		mRefreshLayout = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout);
		mRefreshButton = (Button) mView.findViewById(R.id.refresh_btn);
	}
	

	private void initViews() {
		mRefreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (MarketUtils.getAPNType(mContext) == -1) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}

				if (mRefreshFinished) {
					mRefreshFinished = false;
					startRequestTopicData();
				}
			}
			
		});
		
		mHandler = new Handler() {
			public void handleMessage(Message message) {
			
				switch (message.what) {
				case UPDATE_PAGE_MSG: 
					HashMap<String, Object> map;
					GetTopicResp resp = null;
					map = (HashMap<String, Object>)message.obj;

					if(map != null && map.size()>0) {
						resp = (GetTopicResp) map.get("topicResp");
						map.clear();
						FrameInfoCache.saveFrameInfoToStorage(resp, mSaveFrameName);
					} else {
						resp = (GetTopicResp) FrameInfoCache.getFrameInfoFromStorage(mSaveFrameName);
					}
					try {
						List<AssemblyInfoBto> assemblyList = resp.getAssemblyList();
						List<AppInfoBto> hotCategoryList = assemblyList.get(0).getAppInfoList();
						mHotViewStyle = assemblyList.get(0).getStyle();
						mCategoryViewStyle = assemblyList.get(1).getStyle();
						//设置热门分类内容
						setHotCategoryData(hotCategoryList);
						
						//设置二级分类内容
						List<AppInfoBto> categoryList = assemblyList.get(1).getAppInfoList();
						mListViewAdapter.setDatas(categoryList);
						mListViewAdapter.notifyDataSetChanged();

						show(CATEGORY_VIEW);
					} catch(NullPointerException e) {
						show(REFRESH_VIEW);
					} catch(IndexOutOfBoundsException e) {
						show(REFRESH_VIEW);
					}
					mRefreshFinished = true;
					break;	

				}
				
				super.handleMessage(message);
			}
		}; // mHandler结束
		startRequestTopicData();

	}
	
	
	/**
	 * 设置热门分类的数据
	 */
	private void setHotCategoryData(List<AppInfoBto> list) {
	    setViewData();
        TextView titleName = (TextView)mHotCategoryView.findViewById(R.id.category_title_name);
        if(mChannelIndex == 2) {
            titleName.setText(mContext.getResources().getString(R.string.soft_category));
        } else {
            titleName.setText(mContext.getResources().getString(R.string.game_category));
        }
		LinearLayout ll = null;
		ImageView iv = null;
		TextView tv = null;
		String imgUrl = null;
		
		int length = list.size() > mHotItemList.length ? mHotItemList.length : list.size();
		
		for(int i=0;i<length;i++) {
			ll = (LinearLayout)mHotCategoryView.findViewById(mHotItemList[i]);
			iv = (ImageView)mHotCategoryView.findViewById(mHotIconList[i]);
			tv = (TextView)mHotCategoryView.findViewById(mHotNameList[i]);
			
			final AppInfoBto appInfo = list.get(i);
			imgUrl = appInfo.getImgUrl();
			AsyncImageCache.from(mContext).displayImage(true,iv, resId,
	                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(imgUrl),imgUrl), false);
			tv.setText(appInfo.getName());
			ll.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, CategoryDetailActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("channelIndex", mChannelIndex);
					intent.putExtra("level2Id", appInfo.getParentId());
					intent.putExtra("level3Id", appInfo.getRefId());
					intent.putExtra("categoryName", appInfo.getName());
					intent.putExtra("parentName", appInfo.getParentName());
					intent.putExtra("reportFlag", mReportFlag);
					intent.putExtra("detailLogTag", mDetailLogTag);
					mContext.startActivity(intent);
				}
			});
			mHotImgs.add(i, iv);
			mHotImgUrls.add(i, imgUrl);
		}
//		list.clear();
		list = null;
	}
	
	
	/**
	 * 请求分类数据
	 */
	private void startRequestTopicData() {
		show(LOADING_VIEW);
		try {
			String contents = "";
			mGetSoftGameTopicReq.setChannelIndex(mChannelIndex);
			mGetSoftGameTopicReq.setTopicIndex(mTopicIndex);
			contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_SOFT_GAME_TOPIC,mGetSoftGameTopicReq);
			StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_SOFT_GAME_TOPIC, contents);

		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 切换页面显示状态
	 * @param whichView 要显示的页面flag
	 */
	private void show(int whichView) {
		switch(whichView) {
		case CATEGORY_VIEW:
			mListView.setVisibility(View.VISIBLE);
			mRequstLoading.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case LOADING_VIEW:
			mListView.setVisibility(View.GONE);
			mRequstLoading.setVisibility(View.VISIBLE);
			mRefreshLayout.setVisibility(View.GONE);
			break;
		case REFRESH_VIEW:
			mListView.setVisibility(View.GONE);
			mRequstLoading.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	public void freeViewResource() {
	    super.freeViewResource();
//        if(mSortAdapter!=null)
//            mSortAdapter.freeImageCache();
	      mHandler = null;
    }


	@Override
	public void entryView() {
	    super.entryView();     //it must be called
	    
		if(mInitFinished)
	        return;
	    
	    findViews();
		initViews();
		
		 mInitFinished = true;
	}


	@Override
	public View getRootView() {
		return mView;
	}


	@Override
	public void notifyDataSetChanged(String pkgName) {
		if(mListViewAdapter != null) {
			mListViewAdapter.notifyDataSetChanged();
		}
		for(int i=0;i<mHotImgs.size();i++) {
			AsyncImageCache.from(mContext).displayImage(true,mHotImgs.get(i), resId,
	                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(mHotImgUrls.get(i)),mHotImgUrls.get(i)), false);
		}
		
	}
	
	private void setViewData(){
	    if (mHotViewStyle == 1) {
	        resId = R.drawable.category_b_hot_icon_bg;
	        mHotCategoryView = LayoutInflater.from(mContext).inflate(R.layout.layout_category_view_b_hot_item, null);
        } else if(mHotViewStyle == 2) {
            resId = R.drawable.category_a_hot_icon_bg;
            mHotCategoryView = LayoutInflater.from(mContext).inflate(R.layout.layout_category_view_a_hot_item, null);
        } else {
            resId = R.drawable.category_a_hot_icon_bg;
            mHotCategoryView = LayoutInflater.from(mContext).inflate(R.layout.layout_category_view_default_hot, null);
        }
	    
	    if (mCategoryViewStyle == 1) {
	        mListViewAdapter = new CategoryAdapter(mContext,R.layout.layout_category_view_a_item,mReportFlag);
        } else if (mCategoryViewStyle == 2) {
            mListViewAdapter = new CategoryAdapter(mContext,R.layout.layout_category_view_b_item,mReportFlag);
        } else {
            mListViewAdapter = new CategoryAdapter(mContext,R.layout.layout_category_view_default_item,mReportFlag);
        }
	    
	    mListViewAdapter.setDetailLogTag(mDetailLogTag);
	    mListViewAdapter.setChannelIndex(mChannelIndex);
        mListView.setItemsCanFocus(false);
        mListView.addHeaderView(mHotCategoryView);
        mListView.setAdapter(mListViewAdapter);
	    
	}
	
}
