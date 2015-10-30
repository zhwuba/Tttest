package com.freeme.themeclub.wallpaper.widget;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.wallpaper.AppFeature;
import com.freeme.themeclub.wallpaper.cache.DataCache;
import com.freeme.themeclub.wallpaper.os.DaemonAsyncTask;
import com.freeme.themeclub.wallpaper.os.ObservableAsyncTask;

public abstract class AsyncAdapter<T> extends BaseAdapter {

    public abstract class AsyncLoadDataTask extends ObservableAsyncTask<Void, T, List<T>> {
        private boolean mFirstTimeLoad;
        private int mGroup = 0;
        private List<T> mResultDataSet;
        private List<T> mTempDataSet;
        private List<AsyncAdapter.AsyncLoadDataVisitor<T>> mVisitors;

        public AsyncLoadDataTask() {
            mFirstTimeLoad = true;
            mResultDataSet = new ArrayList<T>();
            mTempDataSet = new ArrayList<T>();
            mVisitors = new ArrayList<AsyncAdapter.AsyncLoadDataVisitor<T>>();
        }

        public void setGroup(int group) {
            mGroup = group;
        }

        private boolean realNeedExecuteTask() {
            final int size = mVisitors.size();
            for (int i = 0; i < size; ++i) {
                if (mVisitors.get(i).dataChanged()) {
                    return true;
                }
            }
            return false;
        }

        public void addVisitor(AsyncAdapter.AsyncLoadDataVisitor<T> visitor) {
            mVisitors.add(visitor);
        }

        protected List<T> doInBackground(Void... params) {
            final int size = mVisitors.size();
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    mVisitors.get(i).loadData(this);
                }
            } else {
                T[] partialDataSet = null;
                for (int index = 0; (partialDataSet = loadData(index)) != null; index += partialDataSet.length) {
                    Collections.addAll(mResultDataSet, partialDataSet);

                    publishProgress(partialDataSet);
                }
            }
            return mResultDataSet;
        }

        protected abstract T[] loadData(int index);

        // for visitor
        public void onLoadData(T... partialDataSet) {
            Collections.addAll(mResultDataSet, partialDataSet);
            publishProgress(partialDataSet);
        }

        @Override
        protected void onPostExecute(List<T> result) {
            if (mLoadUsingCache && !mFirstTimeLoad) {
                getDataGroup(mGroup).clear();
                getDataGroup(mGroup).addAll(mTempDataSet);
                notifyDataSetChanged();
            }
            setForceToLoadData(false);

            super.onPostExecute(result);

            postExecuteTask(this);
            postLoadData(result);
        }

        @Override
        protected void onPreExecute() {
            if (!mForceToLoadData && !realNeedExecuteTask()) {
                cancel(false);
                postExecuteTask(this);
            } else {
                final int size = getDataGroup(mGroup).size();
                mFirstTimeLoad = (size == 0);
                if (!mLoadUsingCache || mFirstTimeLoad) {
                    getDataGroup(mGroup).clear();
                } else {
                    mTempDataSet.clear();
                }
                super.onPreExecute();
            }
        }

        @Override
        protected void onProgressUpdate(T... values) {
            for (int i = 0; i < values.length; ++i) {
                if (!mLoadUsingCache || mFirstTimeLoad) {
                    getDataGroup(mGroup).add(values[i]);
                } else {
                    mTempDataSet.add(values[i]);
                }
            }
            if (!mLoadUsingCache || mFirstTimeLoad) {
                notifyDataSetChanged();
            }

            super.onProgressUpdate(values);
        }
    }

    public interface AsyncLoadDataVisitor<T> {
        boolean dataChanged();
        void loadData(AsyncAdapter<T>.AsyncLoadDataTask task);
        void setKeyword(String keyword);
    }

    public abstract class AsyncLoadMoreDataTask extends ObservableAsyncTask<Void, T, List<T>> {
        private boolean mClearData = false;
        private int mGroup = 0;
        private AsyncLoadMoreParams mLoadParams = null;

        public void setClearData(boolean clearData) {
            mClearData = clearData;
        }

        public void setGroup(int group) {
            mGroup = group;
        }

        public void setLoadParams(AsyncLoadMoreParams loadParams) {
            mLoadParams = loadParams;
        }

        protected List<T> doInBackground(Void... params) {
            if (mLoadParams == null) {
                return null;
            }
            List<T> dataSet = loadMoreData(mLoadParams);
            if (mLoadParams.upwards) {
                mReachTop = (dataSet != null && dataSet.size() == 0);
            } else {
                mReachBottom = (dataSet != null && dataSet.size() == 0);
            }
            return dataSet;
        }

        protected abstract List<T> loadMoreData(AsyncLoadMoreParams loadParams);

        @Override
        protected void onPostExecute(List<T> result) {
            if (mClearData && result != null) {
                getDataGroup(mGroup).clear();
            }
            if (result != null) {
                final int size = result.size();
                for (int i = 0; i < size; i++) {
                    getDataGroup(mGroup).add(result.get(i));
                }
            }
            notifyDataSetChanged();

            super.onPostExecute(result);

            postExecuteTask(this);
            postLoadMoreData(result);
        }
    }

    public static class AsyncLoadMoreParams {
        public int cursor;
        public boolean upwards;
    }

    public abstract class AsyncLoadPartialDataTask extends DaemonAsyncTask<Object, Object> {
        Set<Object> mDoingJobs;

        public AsyncLoadPartialDataTask() {
            mDoingJobs = Collections.synchronizedSet(new HashSet<Object>());
        }

        public boolean containJob(Object obj) {
            return mDoingJobs.contains(obj);
        }

        @Override
        protected void onProgressUpdate(Pair<Object, Object>... values) {
            if (values != null && values.length != 0) {
                Object key = values[0].first;
                Object value = values[0].second;
                if (value != null) {
                    sPartialDataCache.put(key, value);
                    notifyDataSetChanged();
                }
                mDoingJobs.remove(key);

                super.onProgressUpdate(values);
            }
        }

        @Override
        protected boolean realDoJob(Object job) {
            boolean ret = (!sPartialDataCache.containsKey(job) && !mDoingJobs
                    .contains(job));
            if (ret) {
                mDoingJobs.add(job);
            }
            return ret;
        }
    }


    public static final int BOTH = 3;
    public static final int DOWNWARDS = 2;
    public static final int NONE = 0;
    public static final int UPWARDS = 1;

    private DataCache<Object, Object> sPartialDataCache = new DataCache<Object, Object>(100) {
        private static final long serialVersionUID = 1L;
        @Override
        protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
            boolean ret = super.removeEldestEntry(eldest);
            if (ret) {
                try {
                    Bitmap b = (Bitmap) eldest.getValue();
                    if (b != null) {
                        b.recycle();
                    }
                } catch (Exception e) {
                }
            }
            return ret;
        }
    };
    private boolean mAutoLoadDownwardsMore = false;
    private boolean mAutoLoadUpwardsMore = false;
    private int mDataPerLine;
    private List<DataGroup<T>> mDataSet;
    private boolean mForceToLoadData = false;
    private AsyncAdapter<T>.AsyncLoadPartialDataTask mLoadPartialDataTask = null;
    private boolean mLoadUsingCache = false;
    private int mPreloadOffset = 0;
    protected boolean mReachBottom = false;
    protected boolean mReachTop = false;
    private byte[] mTaskLocker = new byte[0];
    private Set<ObservableAsyncTask<?, ?, ?>> mTaskSet;

    public AsyncAdapter() {
        mLoadUsingCache = true;
        mDataPerLine = 1;
        mDataSet = new ArrayList<DataGroup<T>>();

        init();
    }

    protected void init() {
        mTaskSet = new HashSet<ObservableAsyncTask<?, ?, ?>>();
    }

    private DataGroup<T> getDataGroup(int group) {
        synchronized (this) {
            if (mDataSet.size() == group) {
                mDataSet.add(new DataGroup<T>());
            }
        }
        return mDataSet.get(group);
    }

    private void loadData(int group, AsyncAdapter<T>.AsyncLoadDataTask task) {
        if (task != null) {
            task.setId("loadData-" + group);
            task.setGroup(group);
            if (preExecuteTask(task)) {
                try {
                    task.executeOnExecutor(MainActivity.fixedThreadPool,new Void[0]);
                } catch (IllegalStateException e) { 
                }
            }
        }
    }

    private void loadMoreData(boolean upwards, boolean usingCache, int group,
            AsyncAdapter<T>.AsyncLoadMoreDataTask task) {
        if (task != null) {
            AsyncLoadMoreParams params = new AsyncLoadMoreParams();
            params.upwards = upwards;
            params.cursor = (upwards || getDataCount(group) == 0) ? 0
                    : getDataCount(group);
            if (usingCache) {
                List<T> result = loadCacheData(params);
                if (result != null) {
                    final int size = result.size();
                    for (int i = 0; i < size; i++) {
                        getDataGroup(group).add(result.get(i));
                    }
                }
                task.setClearData(true);
            }
            task.setLoadParams(params);
            task.setId("loadMoreData-" + group);
            task.setGroup(group);
            if (preExecuteTask(task)) {
                try {
                    task.execute(new Void[0]);
                } catch (IllegalStateException e) {
                }
            }
        }
    }

    private void postExecuteTask(ObservableAsyncTask<?, ?, ?> task) {
        synchronized (mTaskLocker) {
            mTaskSet.remove(task);
        }
    }

    private boolean preExecuteTask(ObservableAsyncTask<?, ?, ?> task) {
        synchronized (mTaskLocker) {
            if (mTaskSet.contains(task)) {
                return false;
            } else {
                mTaskSet.add(task);
                return true;
            }
        }
    }

    protected abstract View bindContentView(View view, List<T> data, int position, int groupPos, int group);

    protected abstract void bindPartialContentView(View view, T data, int offset, List<Object> partialData /**/,int pos, int posOfTotal/**/);

    protected abstract List<Object> getCacheKeys(T data);

    public int getCount() {
        int total = 0;
        final int size = mDataSet.size();
        for (int i = 0; i < size; i++) {
            total += getCount(i);
        }
        return total;
    }

    protected int getCount(int group) {
        int total = getDataCount(group);
        if (total == 0) {
            return 0;
        }
        return (total - 1) / mDataPerLine + 1;
    }

    public int getDataCount(int group) {
        return getDataGroup(group).size();
    }

    public T getDataItem(int index, int group) {
        return getDataGroup(group).get(index);
    }

    public int getDataPerLine() {
        return mDataPerLine;
    }

    /**
     * position
     *           _____ _____ _____ 
     *          |     |     |     |    mDataPerLine = 3
     *    0     |__0__|__0__|__0__|    Group0 : len = 7 , 3 rows
     *          | 1,0 |     |     |    
     *    1     |__0__|__0__|__0__|
     *          | 2,0 |     |     |
     *    2     |__0__|_____|_____|
     *          | 0,1 |     |     |
     *    3     |__1__|__1__|_____|    Group1 : len = 2 , 1 row
     * @param position
     * @return
     */
    public Pair<Integer, Integer> getGroupPosition(int position) {
        int total = 0;
        int count = 0;
        for (int i = 0; i < mDataSet.size(); ++i, total += count) {
            count = getCount(i);
            if (position < total + count) { // groupNo in group, group
                return new Pair<Integer, Integer>(position - total, i);
            }
        }
        return null;
    }

    public List<T> getItem(int position) {
        Pair<Integer, Integer> groupPosition = getGroupPosition(position);
        return getItem(groupPosition.first, groupPosition.second);
    }

    protected List<T> getItem(int groupPos, int group) {
        int index = groupPos * mDataPerLine;
        int length = Math.min(mDataPerLine, getDataCount(group) - index);

        ArrayList<T> data = new ArrayList<T>();
        for (int i = 0; i < length; i++) {
            data.add(getDataItem(index + i, group));
        }
        return data;
    }

    public long getItemId(int position) {
        return position;
    }

    protected List<AsyncAdapter<T>.AsyncLoadDataTask> getLoadDataTask() {
        return null;
    }

    protected List<AsyncAdapter<T>.AsyncLoadMoreDataTask> getLoadMoreDataTask() {
        return null;
    }

    protected AsyncAdapter<T>.AsyncLoadPartialDataTask getLoadPartialDataTask() {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (getCount() == 0) {
            return null;
        }

        Pair<Integer, Integer> groupPosition = getGroupPosition(position);
        List<T> bindData = getItem(groupPosition.first, groupPosition.second);

        convertView = bindContentView(convertView, bindData, position, groupPosition.first, groupPosition.second);

        for (int i = bindData.size() - 1; i >= 0; i--) {
            loadPartialData(convertView, bindData.get(i), i /**/, position, position * mDataPerLine + i/**/);
        }

        if (!mReachTop && position == mPreloadOffset && mAutoLoadUpwardsMore) {
            loadMoreData(true, false);
        }
        // XXX else
        else 
            if (!mReachBottom && position == (getCount() - 1) - mPreloadOffset && mAutoLoadDownwardsMore) {
                loadMoreData(false, false);
            }
        return convertView;
    }

    protected boolean isValidKey(Object key, T data, int position) {
        return sPartialDataCache.containsKey(key);
    }

    protected List<T> loadCacheData(AsyncLoadMoreParams params) {
        return null;
    }

    public void loadData() {
        List<AsyncAdapter<T>.AsyncLoadDataTask> tasks = getLoadDataTask();
        if (tasks != null) {
            final int size = tasks.size();
            for (int i = 0; i < size; ++i) {
                loadData(i, tasks.get(i));
            }
        }
    }

    public void loadData(int group) {
        List<AsyncAdapter<T>.AsyncLoadDataTask> tasks = getLoadDataTask();
        if (tasks != null && tasks.size() > group) {
            loadData(group, tasks.get(group));
        }
    }

    public void loadMoreData(boolean upwards, boolean usingCache) {
        List<AsyncAdapter<T>.AsyncLoadMoreDataTask> tasks = getLoadMoreDataTask();
        if (tasks != null) {
            final int size = tasks.size();
            for (int i = 0; i < size; ++i) {
                loadMoreData(upwards, usingCache, i, tasks.get(i));
            }
        }
    }

    public void loadMoreData(boolean upwards, boolean usingCache, int group) {
        List<AsyncAdapter<T>.AsyncLoadMoreDataTask> tasks = getLoadMoreDataTask();
        if (tasks != null && tasks.size() > group) {
            loadMoreData(upwards, usingCache, group, tasks.get(group));
        }
    }

    protected void loadPartialData(View view, T data, int offset /**/,int pos, int posOfTotal/**/) {
        if (mLoadPartialDataTask == null
                || mLoadPartialDataTask.getStatus() == AsyncTask.Status.FINISHED) {
            mLoadPartialDataTask = getLoadPartialDataTask();
            if (mLoadPartialDataTask == null) {
                return;
            }
        }

        if (mLoadPartialDataTask.getStatus() == AsyncTask.Status.PENDING) {
            try {
                mLoadPartialDataTask.execute(new Void[0]);
            } catch (IllegalStateException e) {
            }
        }


        ArrayList<Object> partialData = new ArrayList<Object>();

        // ---
        if (AppFeature.FEATURE_LOCAL_RESOURCELIST_USE_THUMBNAIL) {
            // +++

            List<Object> keys = getCacheKeys(data);
            final int size = keys.size();
            for (int i = 0; i < size; ++i) {
                Object key = keys.get(i);
                if (isValidKey(key, data, i)) {
                    partialData.add(sPartialDataCache.get(key));
                } else if (!mLoadPartialDataTask.containJob(key)) {
                    mLoadPartialDataTask.addJob(key);
                }
            }
            // ---
        }
        // +++
        bindPartialContentView(view, data, offset, partialData /**/, pos, posOfTotal/**/);
    }

    public boolean loadingData() {
        return !mTaskSet.isEmpty();
    }

    public void onStop() {
        if (mLoadPartialDataTask != null) {
            mLoadPartialDataTask.stop();
        }
    }

    protected void postLoadData(List<T> list) {
    }

    protected void postLoadMoreData(List<T> list) {
    }

    protected void postLoadPartialData(List<Object> list) {
    }

    public void setAutoLoadMoreStyle(int autoLoadMoreStyle) {
        mAutoLoadUpwardsMore = ((autoLoadMoreStyle & UPWARDS) != 0);
        mAutoLoadDownwardsMore = ((autoLoadMoreStyle & DOWNWARDS) != 0);
    }

    public void setDataPerLine(int dataPerLine) {
        mDataPerLine = dataPerLine;
    }

    public void setDataSet(List<DataGroup<T>> dataSet) {
        mDataSet = dataSet;
        notifyDataSetChanged();
    }

    public void setForceToLoadData(boolean isForceToLoadData) {
        mForceToLoadData = isForceToLoadData;
    }

    public void setLoadUsingCache(boolean usingCache) {
        mLoadUsingCache = usingCache;
    }

    public void setPreloadOffset(int preloadOffset) {
        mPreloadOffset = preloadOffset;
    }

    public void stopAllAsynLoadTask() {
        onStop();
        synchronized (mTaskLocker) {
            for (Iterator<ObservableAsyncTask<?, ?, ?>> i = mTaskSet.iterator(); i
                    .hasNext(); i.next().cancel(true))
                ;

            mTaskSet.clear();
        }
    }
}
