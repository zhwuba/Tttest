package com.freeme.themeclub.wallpaper.view;

import com.freeme.themeclub.wallpaper.LocalResourceListFragment;
import com.freeme.themeclub.wallpaper.base.ResourceAdapter;
import com.freeme.themeclub.wallpaper.base.ResourceListFragment;

import android.app.Activity;
import android.util.Pair;
import android.view.View;

public class BatchResourceHandler {

	protected Activity mActivity;
    protected ResourceAdapter mAdapter;
	protected ResourceListFragment mFragment;
	
	protected boolean mLocalResourcePage;
	
	private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            onClick_Impl(v);
        }
    };
    
    
    public BatchResourceHandler(ResourceListFragment fragment, ResourceAdapter adapter) {
        if (fragment == null || adapter == null) {
            throw new IllegalArgumentException("BatchResourceOperationHandler() parameters can not be null!");
        }
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mAdapter = adapter;
        mLocalResourcePage = (fragment instanceof LocalResourceListFragment);
    }
    
    public View.OnClickListener getResourceClickListener() {
        return mItemClickListener;
    }
    
    @SuppressWarnings("unchecked")
	protected void onClick_Impl(View v) {
        Pair<Integer, Integer> position = (Pair<Integer, Integer>) v.getTag();
        if (position != null) {
            if (mFragment != null) {
                mFragment.startDetailActivityForResource(position);
            }
        }
    }
}
