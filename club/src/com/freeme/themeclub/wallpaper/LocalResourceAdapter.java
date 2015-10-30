package com.freeme.themeclub.wallpaper;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.wallpaper.base.BaseFragment;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.base.ResourceAdapter;
import com.freeme.themeclub.wallpaper.local.ImageResourceFolder;
import com.freeme.themeclub.wallpaper.os.AsyncTaskObserver;
import com.freeme.themeclub.wallpaper.resource.Resource;
import com.freeme.themeclub.wallpaper.widget.AsyncAdapter;

public class LocalResourceAdapter extends ResourceAdapter {

	protected String mKeyword;
	protected List<AsyncAdapter.AsyncLoadDataVisitor<Resource>> mVisitors;
	
	public LocalResourceAdapter(Context context, Bundle metaData) {
		super(context, metaData);
		mVisitors = null;
	}
	
	public LocalResourceAdapter(BaseFragment fragment, Bundle metaData) {
		super(fragment, metaData);
		mVisitors = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<AsyncAdapter<Resource>.AsyncLoadDataTask> getLoadDataTask() {
        ArrayList<AsyncAdapter<Resource>.AsyncLoadDataTask> tasks = 
        		new ArrayList<AsyncAdapter<Resource>.AsyncLoadDataTask>();
        
        AsyncLoadResourceTask task = new AsyncLoadResourceTask();
		task.addObserver((AsyncTaskObserver<Void, Resource, List<Resource>>) 
				getRegisterAsyncTaskObserver());
        
        List<AsyncAdapter.AsyncLoadDataVisitor<Resource>> visitors = getVisitors();
        final int size = visitors.size();
        for (int i = 0; i < size; i++) {
        	task.addVisitor(visitors.get(i));
        }

        tasks.add(task);
        return tasks;
    }

    protected AsyncAdapter.AsyncLoadDataVisitor<Resource> getVisitor(String folder) {
    	AsyncAdapter.AsyncLoadDataVisitor<Resource> result = 
    			new ImageResourceFolder(mContext, mMetaData, folder);
        return result;
    }
    
    protected List<AsyncAdapter.AsyncLoadDataVisitor<Resource>> generateVisitors(String[] folders) {
		ArrayList<AsyncAdapter.AsyncLoadDataVisitor<Resource>> visitors
				= new ArrayList<AsyncAdapter.AsyncLoadDataVisitor<Resource>>();
		
        for (int i = 0; i < folders.length; i++) {
        	visitors.add(getVisitor(folders[i]));
        }

        return visitors;
    }
    
    protected List<AsyncAdapter.AsyncLoadDataVisitor<Resource>> getVisitors() {
        if (mVisitors == null) {
            mVisitors = generateVisitors(
            		mMetaData.getStringArray(IntentConstants.EXTRA_SOURCE_FOLDERS));
        }
        return mVisitors;
    }
    
    public void clearVisitors() {
    	mVisitors = null;
    }
    
	@Override
	protected boolean checkResourceModifyTime() {
        return false;
    }
	
	public void setKeyword(String keyword) {
        mKeyword = keyword;
    }

	
	public class AsyncLoadResourceTask extends AsyncLoadDataTask {
		protected Resource[] loadData(int index) {
			return null;
		}
    }
}
