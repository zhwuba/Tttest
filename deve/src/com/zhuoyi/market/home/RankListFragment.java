package com.zhuoyi.market.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.ranklist.RankListView;
import com.zhuoyi.market.view.AbsCustomView;
import com.zhuoyi.market.view.CustomViewFactory;

public class RankListFragment extends Fragment {

	private AbsCustomView mRankListView;
	private DownloadCallBackInterface mDownloadCallBack;
	private boolean mFirstInter = true;
	
	public static RankListFragment newIntance(int position,DownloadCallBackInterface downloadCallBack) {
		RankListFragment fragment = new RankListFragment();
		fragment.mDownloadCallBack = downloadCallBack;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRankListView = CustomViewFactory.create(CustomViewFactory.VIEW_RANKLIST, this.getActivity().getApplicationContext(), mDownloadCallBack);
		View view = mRankListView.getRootView();
		return view;
	}

	
	 @Override
	    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
	        super.onViewCreated(view, savedInstanceState);
	    }
	

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mRankListView != null)
			mRankListView.freeViewResource();
	}
	
	
	public void entryView() {
		if (mRankListView != null && mFirstInter) {
			mFirstInter = false;
			mRankListView.entryView();
		}
	}
	
	
    public ListView getListView() {
    	if (mRankListView != null) {
    		return mRankListView.getListView();
    	} else {
    		return null;
    	}
    }


	public void refreshData() {
		if (mRankListView != null)
			mRankListView.notifyDataSetChanged(null);
    }
	
}
