package com.freeme.themeclub.wallpaper.os;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class ObservableAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

    private String mId;
    private List<AsyncTaskObserver<Params, Progress, Result>> mObservers;

    public ObservableAsyncTask() {
        mObservers = new ArrayList<AsyncTaskObserver<Params, Progress, Result>>();
        mId = Long.toString(super.hashCode());
    }

    @SuppressWarnings("unchecked")
    @Override
	public boolean equals(Object o) {
		if (o instanceof ObservableAsyncTask) {
			return mId
					.equals(((ObservableAsyncTask<Params, Progress, Result>) o)
							.getId());
		}
        return false;
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }
    
    public void addObserver(AsyncTaskObserver<Params, Progress, Result> observer) {
        if (observer != null) mObservers.add(observer);
    }
    
    public void removeObserver(AsyncTaskObserver<Params, Progress, Result> observer) {
        if (observer != null) mObservers.remove(observer);
    }
    
    public String getId() {
        return mId;
    }
    
    public void setId(String id) {
        mId = id;
    }

    @Override
	protected void onCancelled() {
		super.onCancelled();
		synchronized (mObservers) {
			for (Iterator<AsyncTaskObserver<Params, Progress, Result>> i = mObservers
					.iterator(); i.hasNext(); i.next().onCancelled())
				;
		}
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		synchronized (mObservers) {
			for (Iterator<AsyncTaskObserver<Params, Progress, Result>> i = mObservers
					.iterator(); i.hasNext(); i.next().onPostExecute(result))
				;
		}
	}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
		synchronized (mObservers) {
			for (Iterator<AsyncTaskObserver<Params, Progress, Result>> i = mObservers
					.iterator(); i.hasNext(); i.next().onPreExecute())
				;
		}
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
		synchronized (mObservers) {
			for (Iterator<AsyncTaskObserver<Params, Progress, Result>> i = mObservers
					.iterator(); i.hasNext(); i.next().onProgressUpdate(values))
				;
		}
    }
}