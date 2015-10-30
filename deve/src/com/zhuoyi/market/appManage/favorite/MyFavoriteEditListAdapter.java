package com.zhuoyi.market.appManage.favorite;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.utils.AsyncImageCache;

public class MyFavoriteEditListAdapter extends BaseAdapter{
	private Context mContext;
	private List<FavoriteInfo> mFavoriteInfos;
	private AsyncImageCache mAsyncImageCache;
	private boolean allowRefresh = true;
	
	public MyFavoriteEditListAdapter(Context mContext, List<FavoriteInfo> favoriteInfos) {
		this.mContext = mContext;
		this.mFavoriteInfos = favoriteInfos;
		this.mAsyncImageCache = AsyncImageCache.from(mContext);
	}
	
	public void allowRefreshIcon(boolean refresh) {
		allowRefresh = refresh;
	}
	
	
	public boolean getRefreshiconStatu() {
		return allowRefresh;
	}
	

	@Override
	public int getCount() {
		return mFavoriteInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mFavoriteInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Wrapper wrapper;
		ImageView app_icon;
		TextView app_name;
		TextView app_size;
		final ImageView img_select;
		if(convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.favorite_list_item, parent, false);
			wrapper = new Wrapper(convertView);
			convertView.setTag(wrapper);
		} else {
			wrapper = (Wrapper) convertView.getTag();
		}
		
		app_icon = wrapper.getAppIcon();
		app_name = wrapper.getAppName();
		app_size = wrapper.getAppSize();
		img_select = wrapper.getImgSelect();
		img_select.setVisibility(View.VISIBLE);
		
		final FavoriteInfo favoriteInfo = mFavoriteInfos.get(position);
		app_name.setText(favoriteInfo.getAppName());
		app_size.setText(favoriteInfo.getFileSizeSum());
		mAsyncImageCache.displayImage(allowRefresh, app_icon, R.drawable.picture_bg1_big, new AsyncImageCache.NetworkImageGenerator(favoriteInfo.getAppPackageName(),favoriteInfo.getIconUrl()), true);
		
		if(favoriteInfo.isSelect())
			img_select.setBackgroundResource(R.drawable.favorite_edit_selected);
		else
			img_select.setBackgroundResource(R.drawable.favorite_edit_unselected);
		
		img_select.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(favoriteInfo.isSelect()) {
					favoriteInfo.setSelect(false);
					img_select.setBackgroundResource(R.drawable.favorite_edit_unselected);
				} else {
					favoriteInfo.setSelect(true);
					img_select.setBackgroundResource(R.drawable.favorite_edit_selected);
				}
			}
		});
		
		return convertView;
	}
	
	private class Wrapper{
		private View mView;
		private ImageView app_icon;
		private TextView app_name;
		private TextView app_size;
		private ImageView img_select;
		
		public Wrapper(View mView) {
			this.mView = mView;
		}
		
		public ImageView getAppIcon() {
			if(app_icon == null)
				app_icon = (ImageView) mView.findViewById(R.id.list_icon);
			return app_icon;
		}
		public TextView getAppName() {
			if(app_name == null)
				app_name = (TextView) mView.findViewById(R.id.list_app_name);
			return app_name;
		}
		public TextView getAppSize() {
			if(app_size == null)
				app_size = (TextView) mView.findViewById(R.id.list_app_size);
			return app_size;
		}
		
		public ImageView getImgSelect() {
			if(img_select == null)
				img_select = (ImageView) mView.findViewById(R.id.img_select);
			return img_select;
		}
	}

}
