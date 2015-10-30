package com.freeme.themeclub.wallpaper.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.AppFeature;
import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.os.DaemonAsyncTask;
import com.freeme.themeclub.wallpaper.resource.Resource;
import com.freeme.themeclub.wallpaper.resource.ResourceSet;
import com.freeme.themeclub.wallpaper.view.BatchResourceHandler;
import com.freeme.themeclub.wallpaper.widget.AsyncAdapter;
import com.freeme.themeclub.wallpaper.widget.AsyncImageAdapter;
import com.freeme.themeclub.wallpaper.widget.DataGroup;

public abstract class ResourceAdapter extends AsyncImageAdapter<Resource> {
	
	protected static final LinearLayout.LayoutParams mRootDataParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);
	
	// XXX
	private static final boolean __WALLPAPER_ROW_WITH_PADDING = false;
	
	public Activity mContext;
	public BaseFragment mFragment;
    protected LayoutInflater mInflater;
    
    private boolean mDecodeImageLowQuality;
    
    protected Bundle mMetaData;
    
    private BatchResourceHandler mBatchHandler;
    
    protected ResourceSet mResourceSet;
    public int mResouceType = 0;
	public int mDisplayType = 0;
	
    protected String mResourceSetPackage;
    protected String mResourceSetSubpackage;
    
    private int mItemHorizontalSpacing = 0;
    private int mItemVerticalSpaceing = 0;
    private int mThumbnailHeight = 0;
    private int mThumbnailWidth = 0;
	
    protected String mCurrentUsingPath;
    
	public ResourceAdapter(Context context, Bundle metaData) {
        this(null, context, metaData);
    }
	
	public ResourceAdapter(BaseFragment fragment, Bundle metaData) {
        this(fragment, fragment.getActivity(), metaData);
    }
	
	private ResourceAdapter(BaseFragment fragment, Context context, Bundle metaData) {
        mItemHorizontalSpacing = -1;
        mItemVerticalSpaceing = -1;
        mFragment = fragment;
        mContext = (Activity) context;
        
        if (mFragment == null && mContext == null) {
            throw new RuntimeException("invalid parameters: fragment and activity can not both be null.");
        } else {
			mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			mDecodeImageLowQuality = mContext.getResources().getBoolean(
					R.bool.resource_decode_image_with_low_quality);

            mMetaData = metaData;
            
            refreshDataSet();
            
            mResouceType = mMetaData.getInt(IntentConstants.EXTRA_RESOURCE_FLAG);
            
            mDisplayType = mMetaData.getInt(IntentConstants.EXTRA_DISPLAY_TYPE);
            setDataPerLine(2);
            setAutoLoadMoreStyle(DOWNWARDS);
            setPreloadOffset(30 / (2 * getDataPerLine()));
            
            resolveThumbnailSize();
        }
    }
	
	public void refreshDataSet() {
	    mResourceSetPackage = mMetaData.getString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE);
	    mResourceSetSubpackage = mMetaData.getString(IntentConstants.EXTRA_RESOURCE_SET_SUBPACKAGE);
	    
	    mResourceSet = ResourceSet.getInstance(mResourceSetPackage + mResourceSetSubpackage);
	    setDataSet((List<DataGroup<Resource>>) mResourceSet);
	}
	
	private void resolveThumbnailSize() {
		int horizontalPadding = 0;
		
		// calc with and height within padding.
		if (__WALLPAPER_ROW_WITH_PADDING) {
			Rect rect = new Rect();
			Drawable bgItem = mContext.getResources().getDrawable(
					R.drawable.list_medium_single_item_background_normal);
			bgItem.getPadding(rect);
			
			horizontalPadding = rect.left + rect.right;
		}
		
		Pair<Integer, Integer> size = ResourceHelper.getThumbnailSize(mContext,
				mDisplayType, horizontalPadding);
		mThumbnailWidth = /*size.first*/mContext.getResources().getDimensionPixelSize(R.dimen.wallpaper_preview_w);
		mThumbnailHeight = /*size.second*/mContext.getResources().getDimensionPixelSize(R.dimen.wallpaper_preview_h);
	    
	    mItemHorizontalSpacing = /*ResourceHelper.getThumbnailGap(mContext)*/mContext.getResources().getDimensionPixelSize(R.dimen.wallpaper_preview_h_spacing);
	    mItemVerticalSpaceing = mItemHorizontalSpacing/*mContext.getResources().getDimensionPixelSize(R.dimen.wallpaper_preview_v_spacing)*/;
	}

	@Override
    public boolean isEnabled(int position) {
        return false;
    }
	
	@Override
	protected boolean useLowQualityDecoding() {
        return mDecodeImageLowQuality;
    }
	
	protected View bindContentView(View view, List<Resource> data,
			int position, int groupPos, int group) {
		LinearLayout root = (LinearLayout) view;
        
        if (root == null) {
        	root = new LinearLayout(mContext);
        	root.setOrientation(LinearLayout.VERTICAL);
        	
        	// 0
            TextView tv = new TextView(mContext, null, android.R.attr.listSeparatorTextViewStyle);
            tv.setBackgroundResource(R.drawable.list_item_with_title_background);
            tv.setVisibility(View.GONE);
            root.addView(tv, mRootDataParams);
            
            // 1
            LinearLayout dataLayout = new LinearLayout(mContext);
            dataLayout.setOrientation(LinearLayout.HORIZONTAL);
            dataLayout.setGravity(Gravity.CENTER);
            root.addView(dataLayout, mRootDataParams);
            
            int horizontalTotalItem = getDataPerLine();
            for (int i = 0; i < horizontalTotalItem; i++) {
                View itemView = mInflater.inflate(ResourceHelper.getThumbnailViewResource(mDisplayType), null);
                dataLayout.addView(itemView, getItemViewLayoutParams(itemView, i, horizontalTotalItem));
            }
            dataLayout.setEnabled(horizontalTotalItem <= 1);
            
            root.setDrawingCacheEnabled(true);
        }
        
        TextView dividerText = (TextView) root.getChildAt(0);
        dividerText.setVisibility(View.GONE);
        
        LinearLayout dataLayout = (LinearLayout) root.getChildAt(1);
        
        // set background within padding.
        if (__WALLPAPER_ROW_WITH_PADDING) {
        	setListItemViewBackground(dataLayout, groupPos, group);
        }
        
        if (getDataPerLine() == 1) {
        	dataLayout.setTag(new Pair<Integer, Integer>(groupPos * getDataPerLine(), group));
            if (mBatchHandler != null) {
            	dataLayout.setOnClickListener(mBatchHandler.getResourceClickListener());
            }
        }
        
        for (int i = 0; i < getDataPerLine(); ++i) {
            View itemView = dataLayout.getChildAt(i);
            if (i < data.size()) {
            	itemView.setVisibility(View.VISIBLE);
                Resource res = data.get(i);
                if (res.getDividerTitle() != null) {
                	dividerText.setVisibility(View.VISIBLE);
                	dividerText.setText(res.getDividerTitle());
                }
                bindView(itemView, res, groupPos * getDataPerLine() + i, group);
            } else {
            	itemView.setVisibility(View.INVISIBLE);
            }
        }
        
        return root;
	}
	
	@Override
	protected void bindPartialContentView(View view, Resource data, int offset,
			List<Object> partialData ,int pos, int posOfTotal) {
        if (ResourceHelper.isMultipleView(mDisplayType)) {
        	// ignore
        } else {
        	LinearLayout dataLayout = (LinearLayout) ((LinearLayout) view).getChildAt(1);
            setThumbnail((ImageView) dataLayout.getChildAt(offset).findViewById(R.id.thumbnail), 
            		data, 0, partialData, true , pos, posOfTotal);
        }
	}

	protected void bindView(View view, Resource resourceItem, int groupIndex, 
			int group) {
		if (mBatchHandler != null) {
            ImageView imageview = (ImageView) view.findViewById(R.id.thumbnail);
            if (resourceItem != null) {
                imageview.setTag(new Pair<Integer, Integer>(groupIndex, group));
                imageview.setOnClickListener(mBatchHandler.getResourceClickListener());
                imageview.setVisibility(View.VISIBLE);
                
                setResourceFlag(view, resourceItem);
                
                bindText(view, R.id.title, resourceItem, "NAME");
            } else {
                ((View) imageview.getParent()).setVisibility(View.INVISIBLE);
                View title = view.findViewById(R.id.title);
                if (title != null) {
                	title.setVisibility(View.INVISIBLE);
                }
            }
        }
	}
	
	protected LinearLayout.LayoutParams getItemViewLayoutParams(View itemView,
			int horizontalPos, int horizontalCount) {
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				mThumbnailWidth, mThumbnailHeight);
		if (horizontalCount >= 2) {
			param.leftMargin = 8;
			param.rightMargin = 8;
			param.topMargin = 6;
			param.bottomMargin =6;
		}
		return param;
	}
	
	protected int getBottomFlagId(Resource resourceItem) {
		/*/
		return (resourceItem.getLocalPath().equals(mCurrentUsingPath)) ? R.drawable.flag_using
				: 0;
		/*/ 
		return 0;
		//*/
	}
    
    protected int getCenterFlagId(Resource resourceItem) {
        return 0;
    }
    
    protected int getTopFlagId(Resource resourceItem) {
    	return (resourceItem.getLocalPath().equals(mCurrentUsingPath)) ? R.drawable.flag_using
				: 0;
    }
    
    protected final Object getRegisterAsyncTaskObserver() {
        return  (mFragment != null) ? mFragment : mContext;
    }
    
    public void setResourceBatchHandler(BatchResourceHandler handler) {
        mBatchHandler = handler;
    }
    
    @Override
    protected AsyncAdapter<Resource>.AsyncLoadPartialDataTask getLoadPartialDataTask() {
        AsyncImageAdapter<Resource>.AsyncLoadImageTask task = this.new AsyncLoadImageTask();
        task.setJobPool(new DaemonAsyncTask.StackJobPool<Object>());
        task.setTargetSize(mThumbnailWidth, mThumbnailHeight);
        task.setScaled(true);
        return task;
    }
    
	protected List<Object> getCacheKeys(Resource data) {
        ArrayList<Object> cacheKeys = new ArrayList<Object>();
        final int total = ResourceHelper.isMultipleView(mDisplayType) ? 2 : 1;
        for (int i = 0; i < total; i++) {
        	String thumbnailPath = data.getLocalThumbnail(i);
            if (thumbnailPath != null) {
            	cacheKeys.add(thumbnailPath);
            }
        }
        return cacheKeys;
	}
	
	@Override
	protected boolean isValidKey(Object key, Resource data, int position) {
		String localPath = (String) key;
        File file = new File(localPath);
        
        if (file.exists()) {
            if (checkResourceModifyTime() 
            		&& file.lastModified() < data.getFileModifiedTime()) {
                file.delete();
                return false;
            }
            return super.isValidKey(key, data, position);
        }
        return false;
    }
	
	protected boolean checkResourceModifyTime() {
	    return true;
	}

	private void setListItemViewBackground(View view, int groupPos, int group) {
        int resId;
        resId = R.drawable.list_medium_single_item_background/*_normal*/;
        view.setBackgroundResource(resId);
    }
	
	private void bindText(View view, int id, Resource resourceItem, String key) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView != null) {
        	textView.setText(resourceItem.getInformation().getString(key));
        	textView.setVisibility(View.VISIBLE);
        }
    }
	
	private void setThumbnail(ImageView view, Resource data, int index, List<Object> partialData, boolean showEmpty
            /**/, int pos, int posOfTotal/**/) {
        if (view != null) {
            // ---
            if (AppFeature.FEATURE_LOCAL_RESOURCELIST_USE_THUMBNAIL) {
                final int group = mResouceType == ResourceConstants.R_LOCKSCREEN_WALLPAER 
                    ? 0 // lockscreen
                    : 1; // homescreen
                final int[] thumbIds = AppFeature.FEATURE_LOCAL_RESOURCELIST_THUMBNAIL[group];
                final int size = thumbIds.length;
                if (posOfTotal < 0) { posOfTotal = 0; }
                if (posOfTotal > size - 1) { /*posOfTotal = size - 1;*/
                	if (partialData == null || partialData.isEmpty()) {
                    	view.setImageBitmap(null);
                    } else {
                    	view.setImageBitmap((Bitmap) partialData.get(index));
                    }
                }else{
                	view.setImageResource(thumbIds[posOfTotal]);
                }
            } else 
            // +++
            if (partialData == null || partialData.isEmpty()) {
            	view.setImageBitmap(null);
            } else {
            	view.setImageBitmap((Bitmap) partialData.get(index));
            }
        }
    }
	
	private void setResourceFlag(View view, Resource resourceItem) {
        if (resourceItem != null) {
            view.findViewById(R.id.root_flag).setVisibility(View.VISIBLE);
            
			((ImageView) view.findViewById(R.id.top_flag))
					.setImageResource(getTopFlagId(resourceItem));
			((ImageView) view.findViewById(R.id.center_flag))
					.setImageResource(getCenterFlagId(resourceItem));
			((ImageView) view.findViewById(R.id.bottom_flag))
					.setImageResource(getBottomFlagId(resourceItem));
        }
    }
	
	public boolean updateCurrentUsingPath(String path) {
        if (!TextUtils.equals(mCurrentUsingPath, path)) {
            mCurrentUsingPath = path;
            mMetaData.putString(IntentConstants.EXTRA_CURRENT_USING_PATH, 
            		mCurrentUsingPath);
            return true;
        }
        return false;
    }
	
	public ResourceSet getResourceSet() {
        return mResourceSet;
    }
	
	public void clearResourceSet() {
        if (mResourceSet != null) {
            mResourceSet.clear();
        }
    }

    public void clearResourceSet(int group) {
        if (mResourceSet != null && mResourceSet.size() > group) {
            mResourceSet.get(group).clear();
        }
    }
}
