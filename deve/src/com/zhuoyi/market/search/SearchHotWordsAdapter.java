package com.zhuoyi.market.search;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.market.net.data.HotSearchInfoBto;
import com.zhuoyi.market.adapter.CommonAdapter;
import com.zhuoyi.market.adapter.ViewHolder;
import com.zhuoyi.market.utils.AsyncImageCache;

public class SearchHotWordsAdapter extends CommonAdapter<HotSearchInfoBto> {
	
	public SearchHotWordsAdapter(Context context,int layoutId) {
		super(context,layoutId);
	}
	
	
	@Override
	public void convert(ViewHolder holder, HotSearchInfoBto hotData, int positon) {
		ImageView icon = holder.getView(R.id.icon);
		TextView name = holder.getView(R.id.hotword);
		
		setHotWordStyle(name,hotData.getText(),hotData.getColorCode());
		switch(hotData.getType()) {
		case HotSearchInfoBto.TYPE_APP_INFO:
			icon.setVisibility(View.VISIBLE);
			AsyncImageCache.from(mContext).displayImage(true,icon, R.drawable.picture_bg1_big,
                    new AsyncImageCache.NetworkImageGenerator(hotData.getAppInfo().getPackageName(),hotData.getAppInfo().getImgUrl()), true);
			break;
		case HotSearchInfoBto.TYPE_TEXT:
			icon.setVisibility(View.GONE);
			break;
		}
	}
	
	private void setHotWordStyle(TextView tv,String hotword,String textColor) {
		tv.setText(hotword);
		if (TextUtils.isEmpty(textColor)) {
		    tv.setTextColor(mContext.getResources().getColor(R.color.hotword_normal));
        } else {
            tv.setTextColor(Color.parseColor(textColor));
        }
	}

}
