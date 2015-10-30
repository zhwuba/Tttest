package com.zhuoyi.market.appManage.favorite;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.TextView;

import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.db.FavoriteDao;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.appManage.favorite.MyFavoriteListAdapter;
import com.zhuoyi.market.utils.AsyncImageCache;

public class FavoriteView  implements OnScrollListener {

	private View mView;
	private Context mContext;

	private int mStartIndex;
	private int mEndIndex;
	private ListView mFavorite_listView;
	private TextView mNofavorite;
	private FavoriteDao mFavoriteDao;
	private List<FavoriteInfo> mFavoriteInfo;
	private MyFavoriteListAdapter mFavoriteAdapter;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	
	public Handler mHandler;
	public static final int REMOVE_FAVORITE = 1;

	public FavoriteView(Context context, DownloadCallBackInterface downloadCallback) {
		this.mContext = context;
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(downloadCallback);
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mView = tLayoutInflater.inflate(R.layout.favorite_list, null);

	}

	public View getMyView() {
		return mView;
	}

	public void entryFavoriteView() {
		initViews();
	}

	public void freeViewResource() {
		if(mHandler!=null)
		{
			mHandler.removeMessages(REMOVE_FAVORITE);
			mHandler = null;
		}
	}
	
	public void resume() {
		mFavoriteDao = new FavoriteDao(mContext);
		mFavoriteInfo = mFavoriteDao.getAllInfos();
		mFavoriteAdapter.setListData(mFavoriteInfo);
		setViewLayout();
		mFavoriteAdapter.notifyDataSetChanged();
	}
	
	public int getFavoritesCount() {
		if(mFavoriteInfo == null)
			return 0;
		return mFavoriteInfo.size();
	}
	
	public ListView getListView() {
		if(mFavorite_listView == null) {
			mFavorite_listView = (ListView) mView.findViewById(R.id.favorite_list);
		}
		return mFavorite_listView;
	}

	private void initViews() {
		mFavoriteDao = new FavoriteDao(mContext);
		mFavoriteInfo = mFavoriteDao.getAllInfos();
		mFavorite_listView = getListView();
		mFavorite_listView.setOnScrollListener(this);
		mNofavorite = (TextView) mView.findViewById(R.id.nofavorite);
		setViewLayout();

		Rect frame = new Rect();  
        ((Activity)mDownloadCallBack.get()).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);  

		mFavoriteAdapter = new MyFavoriteListAdapter(mContext, mDownloadCallBack.get(), mFavoriteInfo);
		mFavorite_listView.setAdapter(mFavoriteAdapter);

		mHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case REMOVE_FAVORITE:
					int list_index = msg.arg1;
					mFavoriteInfo = mFavoriteDao.getAllInfos();
					if(list_index <= mFavoriteInfo.size() - 1 && mFavoriteInfo.size() != 0){
						FavoriteInfo favoriteInfo = mFavoriteInfo.get(list_index);
						if(favoriteInfo != null)
						{
							mFavoriteInfo.remove(favoriteInfo);
							mFavoriteDao.delete(favoriteInfo.getAppPackageName());
							mFavoriteAdapter.setListData(mFavoriteInfo);
							mFavoriteAdapter.notifyDataSetChanged();
						}
					}
					setViewLayout();
					break;
				default:
					break;
				}
			}
		};

		
	}

	private void setViewLayout()
	{
		if (null != mFavoriteInfo && mFavoriteInfo.size() == 0)
		{
			mNofavorite.setVisibility(View.VISIBLE);
			mFavorite_listView.setVisibility(View.GONE);
		}
		else
		{
			mNofavorite.setVisibility(View.GONE);
			mFavorite_listView.setVisibility(View.VISIBLE);
		}
	}

	
	private void asyncLoadImage() {
		if(mFavoriteAdapter == null) {
			return;
		}
		ImageView imageView = null;
		FavoriteInfo info = null;
		
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (FavoriteInfo) mFavoriteAdapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			
			imageView = (ImageView) mFavorite_listView.findViewWithTag(info.getAppPackageName());
			if(imageView == null) {
				continue;
			}
			
			if (mFavoriteAdapter.isAllowRefreshIcon() == false) {
				break;
			}
			
			int defaultResId = R.drawable.picture_bg1_big;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
				AsyncImageCache.from(mContext).displayImage(
						mFavoriteAdapter.isAllowRefreshIcon(),
						imageView,
						R.drawable.picture_bg1_big,
						new AsyncImageCache.NetworkImageGenerator(info
								.getAppPackageName(), info.getUrl()), true);
			}
		}    
	}



	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		switch (view.getId())
		{
		case R.id.favorite_list:
			mStartIndex = firstVisibleItem;
			mEndIndex = firstVisibleItem + visibleItemCount;
			if (mEndIndex >= totalItemCount) 
			{
				mEndIndex = totalItemCount - 1;
			}

		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mFavoriteAdapter!=null)
			{		
				mFavoriteAdapter.allowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			if(mFavoriteAdapter!=null)
				mFavoriteAdapter.allowRefreshIcon(false);
			break;
		}
	}


	public void notifyDataSetChanged(String pacName) {

		if(mFavorite_listView == null
				|| mFavoriteAdapter == null)return;

		if(pacName == null) {
			mFavoriteAdapter.notifyDataSetChanged();
			return;
		}

		int first = mFavorite_listView.getFirstVisiblePosition();
		int last = mFavorite_listView.getLastVisiblePosition();

		for(int i=first; i<=last; i++) {
			if(pacName.equals(mFavoriteInfo.get(i).getAppPackageName())) {
				mFavoriteAdapter.notifyDataSetChanged();
				break;
			}
		}
	}
}
