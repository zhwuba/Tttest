package com.zhuoyi.market.appManage.update;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.market.download.updates.AppUpdateManager;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;


public class IgnoreManagerView {

	public static final int IGNORE_REFRESH = 0;
	
	private View mIgnoreView;
	private ListView mIgnoreListView;
	private IgnoreUpdateAdapter mIgnoreUpdateAdapter;
	private CommonSubtitleView mCommonSubtitleView;
	private Context mContext;
	private Handler mHandler;
	
	public IgnoreManagerView(Context context) {
		initView(context);
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case IGNORE_REFRESH:
					if(mCommonSubtitleView != null) { 
						mCommonSubtitleView.setSubtitleName(String.format(mContext.getText(R.string.update_ignore_num).toString(), AppUpdateManager.getUpdateIgnoreList(mContext).size()));
					}
					break;
				}
			}
			
		};
	}
	
	
	public void initView(Context context) {
		mContext = context;
		mIgnoreView = View.inflate(context, R.layout.update_manager_ignore_view, null);
		mIgnoreListView = (ListView) mIgnoreView.findViewById(R.id.update_ignore_list);
	}
	
	
	public View getView() {
		if(mCommonSubtitleView != null) {
			mCommonSubtitleView.setSubtitleName(String.format(mContext.getText(R.string.update_ignore_num).toString(), AppUpdateManager.getUpdateIgnoreList(mContext).size()));
		}
		mIgnoreUpdateAdapter = new IgnoreUpdateAdapter(mContext, mHandler);
		mIgnoreListView.setAdapter(mIgnoreUpdateAdapter);
		return mIgnoreView;
	}
	
	
	public void onResume() {
		if(mIgnoreUpdateAdapter != null) {
			mIgnoreUpdateAdapter.notifyDataSetChanged();
		}
	}
	
	public void setTttleView(CommonSubtitleView commonSubtitleView) {
		mCommonSubtitleView = commonSubtitleView;
	}
}
