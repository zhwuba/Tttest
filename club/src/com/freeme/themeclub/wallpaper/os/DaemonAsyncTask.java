package com.freeme.themeclub.wallpaper.os;

import android.os.AsyncTask;
import android.util.Pair;

import java.util.Stack;


public class DaemonAsyncTask<Job, JobResult> extends
		ObservableAsyncTask<Void, Pair<Job, JobResult>, Void> {
	
    public static interface JobPool<T> {
        void addJob(T job);
        void finishJob(T job);
        T getNextJob();
        boolean isEmpty();
    }

    public static class StackJobPool<T> implements JobPool<T> {

        private Stack<T> mJobs = null;

        public StackJobPool() {
            mJobs = new Stack<T>();
        }
        
        public synchronized void addJob(T job) {
            if (!mJobs.contains(job)) {
                mJobs.push(job);
            }
        }

        public synchronized void finishJob(T job) {
        }

        public synchronized T getNextJob() {
        	if (mJobs.empty()) {
        		return null;
        	}
        	return mJobs.pop();
        }

        public synchronized boolean isEmpty() {
        	return mJobs.empty();
        }
    }

    
    static {
        AsyncTask.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private JobPool<Job> mJobPool;
    private byte[] mLocker;
    private boolean mNeedStop = false;

    public DaemonAsyncTask() {
        mLocker = new byte[0];
    }

    public void addJob(Job job) {
        mJobPool.addJob(job);
		synchronized (mLocker) {
			mLocker.notifyAll();
		}
    }

	@SuppressWarnings("unchecked")
	protected Void doInBackground(Void... params) {
    	while (!mNeedStop) {
			Job job = mJobPool.isEmpty() ? null : mJobPool.getNextJob();
            
            if (job != null) {
            	if (realDoJob(job)) {
					Pair<Job, JobResult> entry = new Pair<Job, JobResult>(job,
							doJob(job));
					publishProgress(entry);
            	}
                mJobPool.finishJob(job);
            } else {
            	synchronized (mLocker) {
            		try {
            			mLocker.wait();
            		} catch (InterruptedException e) {
            			e.printStackTrace();
            		}
            	}
            }
    	}
		return null;
    }

    protected JobResult doJob(Job job) {
        return null;
    }

    protected boolean realDoJob(Job job) {
        return true;
    }

    public void setJobPool(JobPool<Job> jobs) {
        mJobPool = jobs;
    }

    public void setLocker(byte[] locker) {
        mLocker = locker;
    }

	public void stop() {
		mNeedStop = true;
		synchronized (mLocker) {
			mLocker.notifyAll();
		}
	}
}