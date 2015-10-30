package com.zhuoyi.market.home;

import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.necessary.NecessaryView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NecessaryFragment extends Fragment{
	
	private Context mContext;
	private DownloadCallBackInterface mDownloadCallback;
	private NecessaryView mNecessaryView;
	private int mPosition;
	private boolean mFirstInter = true;
	
	public static NecessaryFragment newIntance(int position, Context mContext, DownloadCallBackInterface downloadCallback) {
		NecessaryFragment fragment = new NecessaryFragment();
		fragment.mContext = mContext;
		fragment.mDownloadCallback = downloadCallback;
		fragment.mPosition = position;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mNecessaryView = new NecessaryView(mContext, mDownloadCallback,mPosition, 0, 3);
		View view = mNecessaryView.getView();
		
		return view;
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    if (mNecessaryView != null)
	    	mNecessaryView.freeViewResource();
	}
	
	
	public void entryNecessaryView() {
		if (mNecessaryView != null && mFirstInter) {
			mNecessaryView.entryNecessaryView();
			mFirstInter = false;
		}
	}
	
	
	public void refreshData() {
		if (mNecessaryView != null)
			mNecessaryView.notifyDataSetChanged();
    }
	
}
