package com.zhuoyi.market.topic;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.SubjectInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class TopicNewAdapter extends BaseAdapter {

	private Context mContext;
	private List<SubjectInfoBto> mList;

	private boolean mRefreshIcon = true;
	private int mMapWidth,mMapHeight, mTopicSmallSize;
	private static final String TOPIC_RELEASE_TAG = "topic";
	
	public TopicNewAdapter(Context context, List<SubjectInfoBto> list)
	{
		mContext = context;
		mList = list;
		mMapWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_item_width);
		mMapHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_big_height);
		mTopicSmallSize = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_small_size);
		
	}
	
	public void setMyList(List<SubjectInfoBto> list) {
		mList = list;
	}
	
	public void releaseAdapterInfo()
	{
	    if(mList!=null)
	        mList.clear();
	    mList = null;
	}
	
	public int getCount() {
		return mList == null ? 0:mList.size();
	}
 
	
	public Object getItem(int position) {
		try {
			return mList==null?null:mList.get(position);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	
	public long getItemId(int position) {
		return 0;
	}

	
	public void allowRefreshIcon(boolean status) {
		mRefreshIcon = status;
	}
	
	
	public boolean isAllowRefreshIcon() {
		return mRefreshIcon;
	}

	
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewCache holder;
		if(convertView==null) {
			holder = new ViewCache();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.discover_item_type_2, null);
			holder.mTopicTitle = (TextView) convertView.findViewById(R.id.item_title);
			holder.mTopicAll = (TextView) convertView.findViewById(R.id.item_all);
			holder.mTopicBig = (ImageView)convertView.findViewById(R.id.topic_big);
			holder.mTopic1 = (ImageView)convertView.findViewById(R.id.topic_1);
			holder.mTopic2 = (ImageView)convertView.findViewById(R.id.topic_2);
			holder.mTopic3 = (ImageView)convertView.findViewById(R.id.topic_3);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewCache)convertView.getTag(); 
		}
		
		holder.mTopicAll.setVisibility(View.GONE);
		SubjectInfoBto appinfo = mList.get(position);
		holder.mTopicTitle.setText(appinfo.getTitle());
		showBigTopic(holder, appinfo);
		showSmallTopic(holder, appinfo.getAppList());
		
		return convertView;
	}
	
	static class ViewCache {
		TextView  mTopicTitle;
		TextView  mTopicAll;
		ImageView mTopicBig;
		ImageView mTopic1;
		ImageView mTopic2;
		ImageView mTopic3;
	}
	
	private void showBigTopic(ViewCache holder, SubjectInfoBto appinfo) {
		String imageUrl = appinfo.getImageUrl();
		int defaultPicBigIcon = -1;
		if(MarketUtils.isNoPicModelReally())  
			defaultPicBigIcon = R.drawable.logo_no_network;
		else
			defaultPicBigIcon = R.drawable.logo_no_network_withtxt;
		
		holder.mTopicBig.setImageResource(defaultPicBigIcon);
		holder.mTopicBig.setVisibility(View.VISIBLE);
		AsyncImageCache.from(mContext).displayImage(mRefreshIcon,false,holder.mTopicBig, defaultPicBigIcon,mMapWidth,mMapHeight,
				new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(imageUrl),imageUrl), false,true,false,"topic");
		
	}

	private void showSmallTopic(ViewCache viewHolder,List<AppInfoBto> appInfos) {
        if (appInfos == null || appInfos.size() == 0) {
            viewHolder.mTopic1.setVisibility(View.GONE);
            viewHolder.mTopic2.setVisibility(View.GONE);
            viewHolder.mTopic3.setVisibility(View.GONE);
            return;
        }
        int defaultPic = R.drawable.topic_small_default;
        viewHolder.mTopic1.setVisibility(View.VISIBLE);
        AsyncImageCache.from(mContext).displayImage(true, false, viewHolder.mTopic1, defaultPic, mTopicSmallSize, mTopicSmallSize,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(0).getImgUrl()), appInfos.get(0).getImgUrl()), false, true, false, TOPIC_RELEASE_TAG);
        if (appInfos.size() > 1) {
            viewHolder.mTopic2.setVisibility(View.VISIBLE);
            AsyncImageCache.from(mContext).displayImage(true, false, viewHolder.mTopic2, defaultPic, mTopicSmallSize, mTopicSmallSize,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(1).getImgUrl()), appInfos.get(1).getImgUrl()), false, true, false, TOPIC_RELEASE_TAG);
        }
        if (appInfos.size() > 2) {
            viewHolder.mTopic3.setVisibility(View.VISIBLE);
            AsyncImageCache.from(mContext).displayImage(true, false, viewHolder.mTopic3, defaultPic, mTopicSmallSize, mTopicSmallSize,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfos.get(2).getImgUrl()), appInfos.get(2).getImgUrl()), false, true, false, TOPIC_RELEASE_TAG);
        }
    }

}
