package com.zhuoyi.market.mine;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.market.account.login.BaseHtmlActivity;
import com.market.net.data.IntegralActivityBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.R.layout;
import com.zhuoyi.market.adapter.CommonAdapter;
import com.zhuoyi.market.adapter.ViewHolder;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.AsyncImageCache.GeneralImageGenerator;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.system.util.model.Count;

public class CampaignAdapter extends CommonAdapter<IntegralActivityBto> {

	private Map<Integer, ImageView> mImageViews;
	private boolean mShouldCheckImageLoad = true;

	public CampaignAdapter(Context context, int layoutId) {
		super(context, layoutId);
		mShouldCheckImageLoad = true;
		mImageViews = new HashMap<Integer, ImageView>();
	}

	
	
	@Override
	public int getCount() {
		int count = super.getCount();
		if(count % 3 != 0) {		//显示敬请期待
			count += 1;
		}
		return count;
	}



	@Override
	public void convert(ViewHolder holder, final IntegralActivityBto integralActivityBto, int position) {
		
		if(integralActivityBto != null) {
			LinearLayout layout = holder.getView(R.id.mine_campaign_layout);
			ImageView imageView = holder.getView(R.id.mine_campaign_image);
			TextView textView = holder.getView(R.id.mine_campaign_text);
			String key = MarketUtils.getImgUrlKey(integralActivityBto.getImageUrl());
			textView.setText(integralActivityBto.getActName());
			AsyncImageCache.from(mContext).displayImage(true, false, imageView, R.drawable.integral_load, 0, 0,
					new AsyncImageCache.NetworkImageGenerator(key,integralActivityBto.getImageUrl()), false,true,true,null);
			if(!mImageViews.containsKey(position) || mImageViews.get(position) == null) {
				imageView.setTag(R.drawable.integral_load, integralActivityBto.getImageUrl());
				mImageViews.put(position, imageView);
			}
			layout.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					startHtmlActivity(integralActivityBto.getActName(), integralActivityBto.getActUrl());
				}
			});
			
		}else if(position == getCount() -1) {
			LinearLayout layout = holder.getView(R.id.mine_campaign_layout);
			ImageView imageView = holder.getView(R.id.mine_campaign_image);
			AsyncImageCache.from(mContext).displayImage(true, false, imageView, R.drawable.integral_load, 0, 0,
			new AsyncImageCache.GeneralImageGenerator("campaign_default", null), false,true,true,null);
			TextView textView = holder.getView(R.id.mine_campaign_text);
			textView.setText(R.string.mine_campaign_wait);
		}
	}


	/**
	 * 检测活动图片是否加载成功
	 */
	protected void checkImageLoad() {
		if(mShouldCheckImageLoad) {
			int loadSuccessCount = 0;
			for (ImageView imageView : mImageViews.values()) {
				if((Integer)imageView.getTag(R.id.tag_image_resid) == R.drawable.integral_load) { 
					//当前显示的是默认图
					String imgUrl = (String)imageView.getTag(R.drawable.integral_load);
					String key = MarketUtils.getImgUrlKey(imgUrl);
					AsyncImageCache.from(mContext).displayImage(true,false, imageView, R.drawable.integral_load,0,0,
							new AsyncImageCache.NetworkImageGenerator(key,imgUrl), false,true,true,null);
				} else {
					loadSuccessCount ++;
				}
			}
			if(loadSuccessCount == mImageViews.size()) {	//活动图片全部加载成功
				mShouldCheckImageLoad = false;
			}
		}
	}
	
	private void startHtmlActivity(String title, String url) {
		Intent intent = new Intent(mContext,
				BaseHtmlActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("titleName",title);
		intent.putExtra("wbUrl",url);
		mContext.startActivity(intent);
	}

}
