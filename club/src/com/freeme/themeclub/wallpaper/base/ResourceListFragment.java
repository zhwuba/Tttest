package com.freeme.themeclub.wallpaper.base;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

import com.freeme.themeclub.wallpaper.LocalResourceListFragment;
import com.freeme.themeclub.wallpaper.ResourceHelper;
import com.freeme.themeclub.wallpaper.os.AsyncTaskObserver;
import com.freeme.themeclub.wallpaper.resource.Resource;
import com.freeme.themeclub.wallpaper.view.BatchResourceHandler;

import com.freeme.themeclub.R;

public abstract class ResourceListFragment extends BaseFragment implements
AsyncTaskObserver<Void, Resource, List<Resource>> {

    protected Activity mActivity;
    protected Bundle mMetaData;
    protected ResourceAdapter mAdapter;

    //protected int mDisplayType = 0;
    protected String mResourceSetName;
    protected String mResourceSetPackage;

    // ui
    protected ListView mListView;
    protected View mProgressBar;

    protected BatchResourceHandler mBatchHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(getContentView(), null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();

        Intent intent = mActivity.getIntent();
        //if (intent != null) {
        if (this instanceof LocalResourceListFragment) {
            //mMetaData = intent.getBundleExtra(IntentConstants.EXTRA_META_DATA_FOR_LOCAL);
            mMetaData=new Bundle();
            mMetaData.putInt(IntentConstants.EXTRA_RESOURCE_FLAG, 
                    ResourceConstants.R_DESKTOP_WALLPAER);
            mMetaData=ResourceHelper.buildDefaultMetaData(mMetaData, IntentConstants.ACTION_SET_WALLPAPER, getActivity());

        } else {
            mMetaData = intent.getBundleExtra(IntentConstants.EXTRA_META_DATA_FOR_ONLINE);
        }
        //}
        if (mMetaData == null) {
            throw new RuntimeException("meta-data can not be null. fragment : " + getClass().getName());
        } else {
            pickMetaData(mMetaData);
            addMetaData(mMetaData);

            setupUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.updateCurrentUsingPath(mMetaData.getString(IntentConstants.EXTRA_CURRENT_USING_PATH));
        mAdapter.notifyDataSetChanged();

        String[] folders = mMetaData.getStringArray(IntentConstants.EXTRA_SOURCE_FOLDERS);
        final int size = folders.length;
        for (int i = 0; i < size; i++) {
            ResourceHelper.getFolderInfoCache(folders[i]);
        }
    }

    @Override
    public void onStop() {
        mAdapter.onStop();
        super.onStop();
    }

    protected void setupUI() {
        mListView = (ListView) getView().findViewById(android.R.id.list);
        View listHeader = getHeaderView();
        if (listHeader != null) { 
            mListView.addHeaderView(listHeader); 
        }

        mAdapter = getAdapter();
        mBatchHandler = getBatchOperationHandler();
        mAdapter.setResourceBatchHandler(mBatchHandler);
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setDividerHeight(0);
        mProgressBar = getView().findViewById(android.R.id.progress);
    }

    protected void pickMetaData(Bundle metaData) {
        mResourceSetPackage = metaData.getString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE);
        mResourceSetName = metaData.getString(IntentConstants.EXTRA_RESOURCE_SET_PACKAGE);

        //mDisplayType = metaData.getInt(IntentConstants.EXTRA_DISPLAY_TYPE);
    }

    protected void addMetaData(Bundle metaData) {
    }

    protected abstract ResourceAdapter getAdapter();

    protected abstract int getContentView();

    protected View getHeaderView() {
        return null;
    }

    protected BatchResourceHandler getBatchOperationHandler() {
        BatchResourceHandler handler = new BatchResourceHandler(this, mAdapter);
        return handler;
    }

    /// 
    protected Pair<String, String> getResourceDetailActivity() {
        return new Pair<String, String>(
                mMetaData
                .getString(IntentConstants.EXTRA_DETAIL_ACTIVITY_PACKAGE),
                mMetaData
                .getString(IntentConstants.EXTRA_DETAIL_ACTIVITY_CLASS));
    }

    public void startDetailActivityForResource(Pair<Integer, Integer> position) {
        Intent target = new Intent();

        Pair<String, String> activityPath = getResourceDetailActivity();
        target.setClassName(activityPath.first, activityPath.second);
        target.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mMetaData.putInt(IntentConstants.EXTRA_RESOURCE_INDEX, position.first);
        mMetaData.putInt(IntentConstants.EXTRA_RESOURCE_GROUP, position.second);
        target.putExtra(IntentConstants.EXTRA_META_DATA, mMetaData);

        target.putExtras(mMetaData);

        startActivityForResult(target, 1);
    }

    /// 
    @Override
    public void onCancelled() {
        mProgressBar.setVisibility(View.GONE);
    }
    @Override
    public void onPreExecute() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
    @Override
    public void onProgressUpdate(Resource... values) {
        // ignore
    }
    @Override
    public void onPostExecute(List<Resource> result) {
        mProgressBar.setVisibility(View.GONE);
    }
}
