package com.zhuoyi.market.appManage.favorite;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.db.FavoriteDao;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.utils.AsyncImageCache;

public class FavoriteEditView implements OnScrollListener{
	private Context mContext;
	private View mView;
	private ListView mListView;
	private TextView mNoFavorite;
	private MyFavoriteEditListAdapter mAdapter;
	
	private int mStartIndex;
	private int mEndIndex;
	
	private FavoriteDao mFavoriteDao;
	private List<FavoriteInfo> mFavoriteInfos = null;
	
	public static final int REFRESH_VIEW = 0;
	public  Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			boolean hasRemove = false;
			switch (msg.what) {
			case REFRESH_VIEW:
				if(mFavoriteInfos != null && mFavoriteInfos.size() == 0) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.edit_nofavorite_tip), Toast.LENGTH_SHORT).show();
					return;
				}
					
				for(int i = 0;i< mFavoriteInfos.size();i++) {
					FavoriteInfo fi = mFavoriteInfos.get(i);
					if(fi.isSelect()) {
						mFavoriteDao.delete(fi.getAppPackageName());
						mFavoriteInfos.remove(fi);
						mAdapter.notifyDataSetChanged();
						i--;
						hasRemove = true;
					}
				}
				if(!hasRemove)
					Toast.makeText(mContext, mContext.getResources().getString(R.string.edit_noselect_for_delete_tip), Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(mContext, mContext.getResources().getString(R.string.has_canceled_favorites), Toast.LENGTH_SHORT).show();
				setViewVisiable();
				break; 
			default:
				break;
			}
		}
	};
	
	public FavoriteEditView(Context mContext) {
		this.mContext = mContext;
		mView = LayoutInflater.from(mContext).inflate(R.layout.favorite_list, null);
		mFavoriteDao = new FavoriteDao(mContext);
		mFavoriteInfos = mFavoriteDao.getAllInfos();
		initView();
	}
	
	
	public View getView() {
		return mView;
	}
	
	
	private void initView() {
		mListView = (ListView) mView.findViewById(R.id.favorite_list);
		mNoFavorite = (TextView) mView.findViewById(R.id.nofavorite);
		
		setViewVisiable();
		mAdapter = new MyFavoriteEditListAdapter(mContext, mFavoriteInfos);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(mFavoriteInfos.get(position).isSelect()) {
					mFavoriteInfos.get(position).setSelect(false);
				} else {
					mFavoriteInfos.get(position).setSelect(true);
				}
				mAdapter.notifyDataSetChanged();
					
			}
		});
	}


	private void setViewVisiable() {
		if(mFavoriteInfos == null || mFavoriteInfos.size() == 0) {
			mNoFavorite.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
		} else {
			mNoFavorite.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}
	}
	
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mAdapter != null) {
				mAdapter.allowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_FLING:
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			if(mAdapter != null)
				mAdapter.allowRefreshIcon(false);
			break;
		default:
			break;
		}
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		switch (view.getId()) {
		case R.id.favorite_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = mStartIndex + visibleItemCount;
			if(mEndIndex >= totalItemCount)
				mEndIndex = totalItemCount - 1;
			break;
		default:
			break;
		}
	}
	

	private void asyncLoadImage() {
		if(mAdapter == null)
			return;
		
		ImageView mImageView;
		FavoriteInfo mFavoriteInfo;
		for(;mStartIndex <= mEndIndex;mStartIndex++) {
			mFavoriteInfo = (FavoriteInfo) mAdapter.getItem(mStartIndex);
			if(mFavoriteInfo == null)
				continue;
			
			mImageView = (ImageView) mListView.findViewWithTag(mFavoriteInfo.getAppPackageName());
			if(mImageView == null)
				continue;
			
			int resId = (Integer) mImageView.getTag(R.id.tag_image_resid);
			if(resId == R.drawable.picture_bg1_big)
				AsyncImageCache.from(mContext).displayImage(
						mAdapter.getRefreshiconStatu(), 
						mImageView, 
						resId, 
						new AsyncImageCache.NetworkImageGenerator(mFavoriteInfo.getAppPackageName(), mFavoriteInfo.getUrl()), 
						true);
		}
	}
}
