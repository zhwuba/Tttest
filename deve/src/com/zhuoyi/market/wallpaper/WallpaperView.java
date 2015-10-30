package com.zhuoyi.market.wallpaper;

import com.market.net.data.WallpaperInfoBto;
import com.market.view.SearchLoadingLayout;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.AsyncImageCache;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public abstract class WallpaperView implements OnItemClickListener, OnScrollListener {
    
    public static final int TYPE_NONE = 0;
    public static final int TYPE_DISPLAY = 1;
    public static final int TYPE_CATEGORY = 2;
    
    private static final int LESS_ITEM_NUM = 6;

    private Context mContext = null;
    private View mFooter = null;
    private View mView = null;
    private GridView mGridView = null;
    private SearchLoadingLayout mSearchLoadingLayout = null;
    private LinearLayout mRefresh = null;
    private Button mRefreshButton = null;
    
    private DisplayWallpaperStorage mDisplayWallpaperStorage = null;
    private WallpaperAdapter mWallpaperAdapter = null;
    
    private boolean mReqFinish = true;
    private boolean mReqBottom = false;
    public boolean mFirstReq = false;
    
    private int mStartIndex = 0;
	private int mEndIndex = 0;
	private int mItemHeight = 0;
    private int mItemWidth = 0;
    
    private int mLocalType = TYPE_NONE;
    
    public WallpaperView(int type, int wWidth) {
        int hSpace = 0;
        int vSpace = 0;
        int colNum = 0;
        mContext = MarketApplication.getRootContext();
        mFooter = View.inflate(mContext, R.layout.foot, null);
        mLocalType = type;
        switch (type) {
        case TYPE_DISPLAY:
        	mItemWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_item_width);
            mItemHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_item_height);
            
            hSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_item_space_horizontal);
            vSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_item_space_vertical);
            
            colNum = 3;
            break;
        case TYPE_CATEGORY:
        	mItemWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_assort_width); 
            mItemHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_assort_height);
            
            hSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_assort_space_horizontal);
            vSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_assort_space_vertical);
            
            mFooter.setVisibility(View.INVISIBLE);
            colNum = 2;
            break;
        }

        
        mDisplayWallpaperStorage = new DisplayWallpaperStorage();
        mWallpaperAdapter = new WallpaperAdapter(mDisplayWallpaperStorage, mItemHeight, mItemWidth, mLocalType);
        mWallpaperAdapter.setColNum(colNum);

        int footerWidth = mItemWidth * colNum + hSpace * (colNum - 1);
        mWallpaperAdapter.setFooter(mFooter, footerWidth, mContext.getResources().getDimensionPixelOffset(R.dimen.dip30));
        
        initView(colNum, mItemWidth, hSpace, vSpace, wWidth);
        
    }  
    
    
    private void initView(int colNum, int colWidth, int hSpace, int vSpace, int wWidth) {
        
        int leftRightSpace = (wWidth - colWidth * colNum - hSpace * (colNum - 1)) / 2;
        int topSpace = mContext.getResources().getDimensionPixelOffset(R.dimen.wallpaper_item_space_top);
        
        if (leftRightSpace < 0) {
            leftRightSpace = 0;
        }
        
        mView = View.inflate(mContext, R.layout.wallpaper_layout, null);
        mGridView = (GridView)mView.findViewById(R.id.wallpaper_gridview);
        mGridView.setPadding(leftRightSpace, topSpace, leftRightSpace, 0);
        mGridView.setNumColumns(colNum);
        mGridView.setHorizontalSpacing(hSpace);
        mGridView.setVerticalSpacing(vSpace);
 
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(this);
        mGridView.setAdapter(mWallpaperAdapter);
        mGridView.setVisibility(View.INVISIBLE);
        
        mSearchLoadingLayout = (SearchLoadingLayout) mView.findViewById(R.id.search_loading);
        mSearchLoadingLayout.setVisibility(View.VISIBLE);
        mRefresh = (LinearLayout) mView.findViewById(R.id.refresh_linearLayout_id);
        mRefreshButton = (Button) mRefresh.findViewById(R.id.refresh_btn);
        mRefreshButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mRefresh.getVisibility() == View.VISIBLE) {
                    mRefresh.setVisibility(View.GONE);
                }
                if (mSearchLoadingLayout.getVisibility() != View.VISIBLE) {
                    mSearchLoadingLayout.setVisibility(View.VISIBLE);
                }
                mFirstReq = true;
                getData();
            }
            
        });
    }
    
    
    public View getView() {
        return mView;
    }
    
    
    public DisplayWallpaperStorage getDisplayWallpaperStorage() {
        return mDisplayWallpaperStorage;
    }
    
    
    public void notifyDataSetChanged() {
        if (mWallpaperAdapter != null)
            mWallpaperAdapter.notifyDataSetChanged();
    }
    
    
    public void setReqFinish(boolean finish) {
        mReqFinish = finish;
    }
    
    
    public boolean getReqFinish() {
    	return mReqFinish;
    }
    
    
    public void setReqSuccess(boolean success) {
        if (success) {
            if (mFirstReq) {
                if (mRefresh.getVisibility() == View.VISIBLE) {
                    mRefresh.setVisibility(View.GONE);
                }
                if (mSearchLoadingLayout.getVisibility() == View.VISIBLE) {
                    mSearchLoadingLayout.setVisibility(View.GONE);
                }
                
                if (mGridView != null)
                	mGridView.setVisibility(View.VISIBLE);
            }
        } else {
            if (mFirstReq) {
                if (mRefresh.getVisibility() != View.VISIBLE) {
                    mRefresh.setVisibility(View.VISIBLE);
                }
                if (mSearchLoadingLayout.getVisibility() == View.VISIBLE) {
                    mSearchLoadingLayout.setVisibility(View.GONE);
                }
            }
        }
        mFirstReq = false;
    }
    
    
    public void setReqBottom(boolean bottom) {
        mReqBottom = bottom;
        if (TYPE_DISPLAY == mLocalType && mReqBottom) {
        	if (mFooter != null) {
        		mFooter.findViewById(R.id.footer_progress).setVisibility(View.GONE);
        		TextView tv = (TextView) mFooter.findViewById(R.id.footer_textview);
        		tv.setText(R.string.loaded_all_data);
        	}
            //Toast.makeText(mContext, R.string.wallpaper_loading_success, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
    	WallpaperInfoBto info = null;
    	try {
    		info = mDisplayWallpaperStorage.getDisplayWallpaperInfo(arg2);
    	} catch (Exception e) {
    		info = null;
    	}
    	if (info == null) return;
    	
        Intent intent = null;
        switch (mLocalType) {
        case TYPE_DISPLAY:
            intent = new Intent(mContext, WallpaperDetail.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("download_url", info.getImageUrl());
            intent.putExtra("download_name", info.getAppName());
            intent.putExtra("download_id", info.getAppId());
            mContext.startActivity(intent);
            break;
        case TYPE_CATEGORY:
            intent = new Intent(mContext, WallpaperCategoryDetail.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("display_name", info.getAppName());
            intent.putExtra("display_code", info.getCode());
            mContext.startActivity(intent); 
            break;
        }
    }


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub
    	mStartIndex = firstVisibleItem;
		mEndIndex = firstVisibleItem + visibleItemCount;
		if (mEndIndex >= totalItemCount) {
			mEndIndex = totalItemCount - 1;
		}
		
        if (mReqFinish && !mReqBottom
                && view.getLastVisiblePosition() + LESS_ITEM_NUM >= (view.getCount() -1)) {
            getData();
        }
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
		switch (scrollState) { 
		case OnScrollListener.SCROLL_STATE_IDLE:
			if(mWallpaperAdapter!=null) {		
				mWallpaperAdapter.setAllowRefreshIcon(true);
				asyncLoadImage();
			}
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
		case OnScrollListener.SCROLL_STATE_FLING:
			if(mWallpaperAdapter!=null)
				mWallpaperAdapter.setAllowRefreshIcon(false);
			break;
		}
    }
    
    
	private void asyncLoadImage() {
		if(mWallpaperAdapter == null) {
			return;
		}
		ImageView imageView = null;
		WallpaperInfoBto info = null;
		
		for (; mStartIndex <= mEndIndex; mStartIndex++) {
			info = (WallpaperInfoBto) mWallpaperAdapter.getItem(mStartIndex);
			if (info == null) {
				continue;
			}
			
			String url = info.getThumbImageUrl();
			if (TextUtils.isEmpty(url)) {
				continue;
			}
			
			imageView = (ImageView) mGridView.findViewWithTag(url);
			if(imageView == null) {
				continue;
			}
			
			if (mWallpaperAdapter.getAllowRefreshIcon() == false) {
				break;
			}
			
			int defaultResId = R.drawable.wallpaper_def_bg;
			int resId = (Integer) imageView.getTag(R.id.tag_image_resid);
			if (resId == defaultResId) {
		        AsyncImageCache.from(mContext).displayImage(
		        		mWallpaperAdapter.getAllowRefreshIcon(),
		                true,
		                imageView,
		                R.drawable.wallpaper_def_bg,
		                mItemWidth,
		                mItemHeight,
		                new AsyncImageCache.NetworkImage565Generator(url, url), 
		                false, 
		                false, 
		                false, 
		                null);
			}
		}    
	}
    
    
    public abstract void getData();
}
