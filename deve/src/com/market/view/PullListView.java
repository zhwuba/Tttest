package com.market.view;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.market.view.ILoadingLayout.State;

public class PullListView extends ListView implements OnScrollListener,OnTouchListener{

	public static String TAG = "PullListView";
	private int mEndIndex;
	private Context mContext;
	private boolean mIsToTop = false;

	private float mTouchdownY, mTouchdownX;
	
	/**用于滑到底部自动加载的Footer*/
	private LoadingLayout mLoadMoreFooterLayout;
    
	private OnRefreshListener mOnRefreshListener;
	private ArrayList<FixedViewInfo> mFooterViewInfos;
	private Handler mHandler;

	public PullListView(Context context){
		super(context);
		mContext = context;
		mHandler = getHandler();
//		mLoadingView = View.inflate(context, R.layout.layout_app_detail_comment_loading, null);
//		this.addFooterView(mLoadMoreFooterLayout, null, false);
		this.setOnScrollListener(this);
		this.setOnTouchListener(this);
	}

	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mHandler = getHandler();
//		mLoadingView = View.inflate(context, R.layout.layout_app_detail_comment_loading
//				, null);
//		this.addFooterView(mLoadMoreFooterLayout, null, false);
		this.setOnScrollListener(this);
		this.setOnTouchListener(this);
	}


	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mEndIndex = firstVisibleItem + visibleItemCount;
		if(mEndIndex >= totalItemCount){
			mEndIndex = totalItemCount - 1;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (hasMoreData() && mLoadMoreFooterLayout.getState() != State.REFRESHING) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE 
					|| scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				if (isReadyForPullUp()) {
					startLoading();
				}
			}
		}
	}

	public void onPullUpRefreshComplete() {
        
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(State.RESET);
        }
    }
	
	protected void startLoading() {
        if (null != mLoadMoreFooterLayout) {
            mLoadMoreFooterLayout.setState(State.REFRESHING);
        }
        
        if(mOnRefreshListener != null){
        	mOnRefreshListener.onRefresh();
        }
    }
	
	
	protected boolean isReadyForPullUp() {
		return isLastItemVisible();
	}


	/**
	 * 表示是否还有更多数据
	 * 
	 * @return true表示还有更多数据
	 */
	 private boolean hasMoreData() {
		if ((null != mLoadMoreFooterLayout) && (mLoadMoreFooterLayout.getState() == State.NO_MORE_DATA)) {
			return false;
		}

		return true;
	 }
	 
	 /**
	  * 设置是否有更多数据的标志
	  * 
	  * @param hasMoreData true表示还有更多的数据，false表示没有更多数据了
	  */
	 public void setHasMoreData(boolean hasMoreData) {
		 if (!hasMoreData) {
			 if (null != mLoadMoreFooterLayout) {
				 mLoadMoreFooterLayout.setState(State.NO_MORE_DATA);
			 }
		 }
	 }


	 @Override
	 public void setAdapter(ListAdapter adapter) {
//		 addFooterView(mLoadingView, null, false);
		 super.setAdapter(adapter);
	 }

	 @Override
	 public boolean onTouch(View v, MotionEvent event) {
		 
		 if(event.getPointerCount() > 1) return false;

		 switch (event.getAction()) {
		 case MotionEvent.ACTION_DOWN:
			 actionDown(event);
			 break;
		 case MotionEvent.ACTION_MOVE:
			 actionMove(event);
			 break;
		 case MotionEvent.ACTION_UP:
			 actionUp(event);
			 break;

		 default:
			 break;
		 }

		 return false;
	 }


	 public void actionDown(MotionEvent event){
		 mTouchdownY = event.getY();
	 }

	 public void actionMove(MotionEvent event){
		if(mTouchdownY - event.getY() > 5){
			mIsToTop = true;
		}else{
			mIsToTop = false;
		}
	 }

	 public void actionUp(MotionEvent event){
	 }

	 public void setOnRefreshListener(OnRefreshListener onRefreshListener){
		 mOnRefreshListener = onRefreshListener;
	 }


	 public interface OnRefreshListener{
		 public void onRefresh();
	 }


	 public void setScrollLoadEnabled(boolean scrollLoadEnabled) {

		 if (scrollLoadEnabled) {
			 // 设置Footer
			 if (null == mLoadMoreFooterLayout) {
				 mLoadMoreFooterLayout = new FooterLoadingLayout(getContext());
			 }

			 if (null == mLoadMoreFooterLayout.getParent()) {
				 this.addFooterView(mLoadMoreFooterLayout, null, false);
			 }
			 mLoadMoreFooterLayout.show(true);
		 } else {
			 if (null != mLoadMoreFooterLayout) {
				 mLoadMoreFooterLayout.show(false);
			 }
		 }
	 }


	 /**
	  * 判断最后一个child是否完全显示出来
	  * 
	  * @return true完全显示出来，否则false
	  */
	 private boolean isLastItemVisible() {
		 final Adapter adapter = this.getAdapter();

		 if (null == adapter || adapter.isEmpty()) {
			 return true;
		 }

		 if(!mIsToTop){
			 return false;
		 }
		 final int lastItemPosition = adapter.getCount() - 1;
		 final int lastVisiblePosition = this.getLastVisiblePosition();

		 /**
		  * This check should really just be: lastVisiblePosition == lastItemPosition, but ListView
		  * internally uses a FooterView which messes the positions up. For me we'll just subtract
		  * one to account for it and rely on the inner condition which checks getBottom().
		  */
		 if (lastVisiblePosition >= lastItemPosition - 1) {
			 final int childIndex = lastVisiblePosition - this.getFirstVisiblePosition();
			 final int childCount = this.getChildCount();
			 final int index = Math.min(childIndex, childCount - 1);
			 final View lastVisibleChild = this.getChildAt(index);
			 if (lastVisibleChild != null) {
				 return lastVisibleChild.getBottom() <= this.getBottom();
			 }
		 }

		 return false;
	 }

}
