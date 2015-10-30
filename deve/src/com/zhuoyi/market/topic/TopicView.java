package com.zhuoyi.market.topic;

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

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.SubjectInfoBto;
import com.market.net.request.GetSubjectReq;
import com.market.net.response.GetSubjectResp;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class TopicView implements OnScrollListener, OnItemClickListener {

	private final int UPDATE_PAGE_MSG = 1;

	private int mStartIndex;
	private int mEndIndex;
	private Context mContext;
	private int mTopicId;
	private View mView;
	private ListView mTopic_listView;
	private View mFooter;
	private ProgressBar mFooterProgress = null;
    private TextView mFooterText = null;
	private List<SubjectInfoBto> mTopic_listNew;
	private LinearLayout mTopic_loading,mTopic_refresh_layout;
	private Button mTopic_refresh;
	private Handler mHandler;
	private boolean mRefreshFinished = false;
	private boolean mIsToBottom = false;
	private TopicNewAdapter mTopic_adapter;
	private GetSubjectReq mSubjectReq;
	private int mCID;
	private String mTopicName;
	private String mTopicInfo;
	private String mTopicImageName;
	private int mMapWidth,mMapHeight;
	
	private int index = -1;
	
	public TopicView(Context context) {
		mContext = context;
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mView = tLayoutInflater.inflate(R.layout.layout_topic, null);
		mSubjectReq = new GetSubjectReq();
		mMapWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.discover_item_width);
		mMapHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.discover_topic_big_height);
	}

	public View getMyView() {
		return mView;
	}
  
	
	public void entryTopicView(int topicId) {
		mTopicId = topicId;
		findViews();
		initViews();
		
	}
	
	
	public void freeViewResource() {
	    if(mTopic_adapter!=null)
	        mTopic_adapter.releaseAdapterInfo();
	    
	    //AsyncImageCache.from(mContext.getApplicationContext()).cleanMemoryReleaseCache("topic");
	}
	
	
	private void findViews() {
		mTopic_listView = (ListView) mView.findViewById(R.id.topic_list);
		mFooter = LayoutInflater.from(mContext).inflate(R.layout.foot, null);
		mFooterProgress = (ProgressBar) mFooter.findViewById(R.id.footer_progress);
        mFooterText = (TextView) mFooter.findViewById(R.id.footer_textview);
		mTopic_listView.setOnScrollListener(this);
		mTopic_listView.setOnItemClickListener(this) ;
		mTopic_listView.setDrawingCacheEnabled(false);
		mTopic_listView.setDrawingCacheEnabled(false);
		mTopic_loading = (LinearLayout) mView.findViewById(R.id.topic_loading);
		mTopic_refresh_layout = (LinearLayout) mView.findViewById(R.id.topic_refresh_layout);
		mTopic_refresh = (Button) mView.findViewById(R.id.refresh_btn);
		mTopic_refresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (MarketUtils.getAPNType(mContext) == -1) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				if (mRefreshFinished) {
					mRefreshFinished = false;
					startRequestTopic();
					mTopic_loading.setVisibility(View.VISIBLE);
					mTopic_listView.setVisibility(View.GONE);
					mTopic_refresh_layout.setVisibility(View.GONE);
				}
			}
			
		});
		mTopic_loading.setVisibility(View.VISIBLE);
		mTopic_listView.setVisibility(View.GONE);
	}

	
	private void initViews() {
		mTopic_listNew = new ArrayList<SubjectInfoBto>();
		mHandler = new Handler() {
			public void handleMessage(Message message) {
				HashMap<String, Object> map;
				int i = 0;
				switch (message.what) {
				case UPDATE_PAGE_MSG:
				    GetSubjectResp mSubjectResp = null;
				    map = (HashMap<String, Object>) message.obj;
					mTopic_loading.setVisibility(View.GONE);
				    if(map != null && map.size() > 0) {
				    	mSubjectResp = (GetSubjectResp) map.get("subjectResp");
				    	map.clear();
				    	index = mSubjectResp.getIndex();
				    	List<SubjectInfoBto> info = mSubjectResp.getSubject();
				    	if(info == null || info.size() <= 0) {
				    		mIsToBottom = true;
					        mFooterProgress.setVisibility(View.GONE);
	                        mFooterText.setText(mContext.getString(R.string.loaded_all_data));
				    		return;
				    	}
				    	int size = info.size();
				    	for(i = 0;i < size; i++) {
				    		if(info.get(i) != null)
				    			mTopic_listNew.add(info.get(i));
				    	}
				    	info.clear();
				    	if(mTopic_adapter == null) {
				    		mTopic_adapter = new TopicNewAdapter(mContext, mTopic_listNew);
				    		mTopic_listView.addFooterView(mFooter);
				    		mTopic_listView.setAdapter(mTopic_adapter); 
				    	}
				    	mTopic_adapter.notifyDataSetChanged();
				    	if(index == -1) {
				    		 mIsToBottom = true;
				    		 mFooterProgress.setVisibility(View.GONE);
				    		 mFooterText.setText(mContext.getString(R.string.loaded_all_data));
                        }
				        mTopic_refresh_layout.setVisibility(View.GONE);
                        mTopic_listView.setVisibility(View.VISIBLE);  
				    } else {
				        if (index == -1) 
				            mTopic_refresh_layout.setVisibility(View.VISIBLE);
                        else 
                            mTopic_refresh_layout.setVisibility(View.GONE);
				    }
					
			        mRefreshFinished = true;
					break;
				default:
					break;
				}
			}
		};
		startRequestTopic();
	}

	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(mTopic_listNew != null && mTopic_listNew.size() > position) {

			SubjectInfoBto appInfoBto = mTopic_listNew.get(position);
			String imageUrl = appInfoBto.getImageUrl();
			mCID = appInfoBto.getTopicId();
			mTopicName = appInfoBto.getTitle();
			mTopicInfo = appInfoBto.getDesc();
			mTopicImageName = appInfoBto.getImageUrl();
			Intent intent = new Intent(mContext, TopicInfoActivity.class);
			intent.putExtra("mCID", mCID);
			intent.putExtra("position", position);
			intent.putExtra("mTopicName", mTopicName);
			intent.putExtra("mTopicInfo", mTopicInfo);
			intent.putExtra("mTopicImage", mTopicImageName);
			intent.putExtra("imageUrl", imageUrl);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}
	
	
	public void startRequestTopic() {
        try {
        	mSubjectReq.setTopicId(mTopicId);
            mSubjectReq.setIndex(0);
            String contents = SenderDataProvider.buildToJSONData(mContext, MessageCode.GET_SUBJECT_DATA_REQ, mSubjectReq);
            StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_SUBJECT_DATA_REQ, contents);
        	
        }catch(Exception e) {
            e.printStackTrace();
        }
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		switch (view.getId()) {
		case R.id.topic_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount) {
				mEndIndex = totalItemCount - 1;
			}

			if (mIsToBottom==false 
			        && view.getLastVisiblePosition() + MarketUtils.LESS_ITEM_NUM >= (view.getCount() - 1) 
			        && mRefreshFinished) {

                mRefreshFinished = false;
                
                mSubjectReq.setIndex(index);
                mSubjectReq.setTopicId(mTopicId);
                String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_SUBJECT_DATA_REQ,mSubjectReq);
                StartNetReqUtils.execListByPageRequest(mHandler, UPDATE_PAGE_MSG, MessageCode.GET_SUBJECT_DATA_REQ, contents);
            }
            break;
        }
	}

	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mTopic_adapter!=null) {		
				mTopic_adapter.allowRefreshIcon(true);
				asyncLoadImage();
				//mTopic_adapter.notifyDataSetChanged();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			if(mTopic_adapter!=null)
				mTopic_adapter.allowRefreshIcon(false);
			break;
		}
	}

	
	public void asyncLoadImage() {
		if(mTopic_adapter == null) {
			return;
		}
		ImageView imageView = null;
		SubjectInfoBto info = null;
		String key;
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (SubjectInfoBto) mTopic_adapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			key = MarketUtils.getImgUrlKey(info.getImageUrl());
			imageView = (ImageView) mTopic_listView.findViewWithTag(key);
			if(imageView == null) {
				continue;
			}
			if (mTopic_adapter.isAllowRefreshIcon() == false) {
				break;
			}
			
			int defaultResId = R.drawable.logo_no_network_withtxt;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
				
				AsyncImageCache.from(mContext).displayImage(
						mTopic_adapter.isAllowRefreshIcon(),false,
						imageView,
						R.drawable.logo_no_network_withtxt,mMapWidth, mMapHeight,
						new AsyncImageCache.NetworkImage565Generator(key, info.getImageUrl()), false, true, false, "topic");
			}
			
		}    
	}

}
