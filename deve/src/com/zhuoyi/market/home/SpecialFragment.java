package com.zhuoyi.market.home;

import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.market.account.login.BaseHtmlActivity;
import com.market.net.data.TopicInfoBto;
import com.market.net.response.GetMarketFrameResp;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.appResident.MarketApplication;

public class SpecialFragment extends Fragment
{

	private LocalActivityManager mLocalActivityManager;
	private BaseHtmlActivity mBaseHtmlActivity;
	
	public static SpecialFragment newIntance(int position) {
		SpecialFragment fragment = new SpecialFragment();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLocalActivityManager = new LocalActivityManager(this.getActivity().getParent(), true);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        View scoreView = getScoreView();
        mBaseHtmlActivity =  (BaseHtmlActivity) mLocalActivityManager.getActivity("getScore");
        mBaseHtmlActivity.setShouldCallJs(false);
        try {
        	View view = scoreView.findViewWithTag("statusBarView");
        	view.setVisibility(View.GONE);
        } catch (Exception e) {
        }
        
        return scoreView;
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(mBaseHtmlActivity != null) {
			mBaseHtmlActivity.setShouldCallJs(isVisibleToUser);
			mBaseHtmlActivity.callJsRefresh();
		}
	}

	private View getScoreView() {

		GetMarketFrameResp resp = MarketApplication.getMarketFrameResp();
		Intent intent = new Intent(this.getActivity().getParent(), BaseHtmlActivity.class);
		intent.putExtra("wbUrl", "");
		intent.putExtra("from_path", ReportFlag.FROM_EARN_CREDIT);
		intent.putExtra("titleBar",false);
		intent.putExtra("showExitDialog", true);
		if(resp != null) {
			List<TopicInfoBto> topicList =  resp.getChannelList().get(0).getTopicList();
			TopicInfoBto topicInfoBto = null;
			if(topicList.size() > 3) {
				topicInfoBto =  topicList.get(2);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("wbUrl", topicList.get(2).getWbUrl());
			}
		}
		return mLocalActivityManager.startActivity("getScore", intent).getDecorView();
	}

	@Override
	public void onResume() {
		super.onResume();
		 if (mLocalActivityManager != null) {
	        	mLocalActivityManager.getActivity("getScore").onWindowFocusChanged(true);
	     }
	}

	
	@Override
	public void onDestroy() {
		if (mLocalActivityManager != null)
            mLocalActivityManager.destroyActivity("getScore", false);
		super.onDestroy();
	}

	
	
}
