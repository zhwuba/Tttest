package com.zhuoyi.market.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.view.CustomViewFactory;

public class SelectFragment extends Fragment {
    private int mPosition;
    private Context mContext;
    private HomeView homeView;
    private DownloadCallBackInterface mDownloadCallback = null;


    public static SelectFragment newIntance(int position, Context context,
        DownloadCallBackInterface downloadCallback) {
        SelectFragment fragment = new SelectFragment();
        fragment.mPosition = position;
        fragment.mContext = context;
        fragment.mDownloadCallback = downloadCallback;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onPause() {
        homeView.getHomeAdView().setHomeShow(false);
        
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        
        homeView.getHomeAdView().setHomeShow(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeView = (HomeView) CustomViewFactory.create(CustomViewFactory.VIEW_HOME, mContext, mDownloadCallback);
        homeView.entryRecommendView();
        homeView.setNeedPaddingTop(false);
        View view = homeView.getMyView();
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (homeView != null)
        	homeView.startRequestRecomendHome();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    
    
    public void refreshData() {
    	if (homeView != null)
    		homeView.notifyDataSetChanged(null);
    }
    
    
    public ListView getListView() {
    	if (homeView != null) {
    		return homeView.getListView();
    	} else {
    		return null;
    	}
    }
    
    
    public void releaseRes() {
    	if (homeView != null) {
    		homeView.freeViewResource();
    	}
    }
    
}
