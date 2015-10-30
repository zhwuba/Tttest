package com.zhuoyi.market.appManage.update;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.market.download.updates.AppUpdateManager;
import com.market.download.util.Util;
import com.market.net.data.AppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.badger.ShortcutBadger;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;

public class UpdateManagerView {
	
	
	private View mUpdateView;
	
	private LinearLayout mUpdateAllView;
	private TextView mUpdateAllBtn;
	private TextView mNotifyTextView;
	private LinearLayout mUpdateManaRL;
	private TextView mNoUpdateTextView;
	private ExpandableListView mExpandableListView;
	private LinearLayout mUpdateLayout;
	private boolean mDisplayList;
	private Context mContext;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	private UpdateExpandableAdapter mUpdateAdapter;
	
	public UpdateManagerView(MarketUpdateActivity marketUpdateActivity){
		mContext = marketUpdateActivity.getApplicationContext();
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(marketUpdateActivity);
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		mUpdateView = tLayoutInflater.inflate(R.layout.update_manager, null);
		mUpdateLayout = (LinearLayout) mUpdateView.findViewById(R.id.update_all);
		mUpdateAllView = (LinearLayout) mUpdateView.findViewById(R.id.update_all_view);
		mUpdateAllBtn = (TextView)mUpdateView.findViewById(R.id.update_all_btn);
		mNotifyTextView = (TextView)mUpdateView.findViewById(R.id.economizedNotify);
		mUpdateManaRL = (LinearLayout)mUpdateView.findViewById(R.id.update_list_manager_rl);
		mNoUpdateTextView = (TextView)mUpdateView.findViewById(R.id.no_update_textView);
		
		mExpandableListView = (ExpandableListView) mUpdateView.findViewById(R.id.list);
		mExpandableListView.setDrawingCacheEnabled(false);
		//mAppInfoBtoMap = new HashMap<String, AppInfoBto>();
		mUpdateAdapter = new UpdateExpandableAdapter(marketUpdateActivity,mDownloadCallBack.get(), this);
		resetUpdateInfo();
		
		//给listview底部增加一定高度使其不会被"全部更新"布局遮盖
		View view= new View(mContext);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.update_all_layout_height);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,height);
		view.setLayoutParams(layoutParams);
		mExpandableListView.addFooterView(view);
		mExpandableListView.setFooterDividersEnabled(false);
		
		
		mExpandableListView.setAdapter(mUpdateAdapter);
		mExpandableListView.setGroupIndicator(null);
		expandGroup();
		
		mExpandableListView.setOnGroupClickListener(new OnGroupClickListener(){
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id){
				return true;
			}
		});
		
	}
	
	public View getMyView(){
		return mUpdateView;
	}
	
	public void updateAllApps() {
	    if (mUpdateAdapter != null) {
	        mUpdateAdapter.updateAllApps();
	    }
	}
	
	
	public ListView getListView(){
		return mExpandableListView;
	}
	
	public void myResume(){
		if(mDisplayList){
			mUpdateAdapter.notifyDataSetChanged();
			ShortcutBadger mShortcutBadger = ShortcutBadger.getBadgerImpl(mContext);
			if(mShortcutBadger != null) {
				if (!Util.getCampaignsNotifyFlag(mContext)) {
					mShortcutBadger.clearBadge();
				} else {
					Util.displayNumOnLauncher(mContext, 1);
				}
			}
		}
	}
	
	public void onDestory(){
		if(mUpdateAdapter != null) {
			mUpdateAdapter.freeViewResource();
		}
	}
	
	
	public LinearLayout getUpdateAllView () {
	    return mUpdateAllView;
	}
	
	
	public TextView getUpdateAllBtn(){
		return mUpdateAllBtn;
	}
	
	public TextView getNotifyTextView(){
		return mNotifyTextView;
	}
	
	public LinearLayout getUpdateLayout(){
		return mUpdateLayout;
	}
	
	private void displayListOrNoUpdate(boolean displayList){
		mDisplayList = displayList;
		if(displayList){
			mUpdateManaRL.setVisibility(View.VISIBLE);
			mNoUpdateTextView.setVisibility(View.INVISIBLE);
			
		}else{
			mUpdateManaRL.setVisibility(View.INVISIBLE);
			mNoUpdateTextView.setVisibility(View.VISIBLE);
		}
	}
	
	
	public void resetUpdateInfo(){

	    //初始化更新数据
	    mUpdateAdapter.initAdapter();
	    
	    //根据更新数据来显示布局
		List<AppInfoBto> appInfoList = MarketApplication.getAppUpdateList();
		if(appInfoList == null || appInfoList.size() == 0){
			List<String> updateAppList = AppUpdateManager.getUninstallInfoList();
			if(updateAppList != null && updateAppList.size() > 0){
				displayListOrNoUpdate(true);
				return;
			}
				
			ArrayList<String> updatedAppList = AppUpdateManager.getUpdatedInfoList();
			if(updatedAppList != null && updatedAppList.size() > 0){
				displayListOrNoUpdate(true);
				return;
			}
			
			displayListOrNoUpdate(false);
		}else{
			displayListOrNoUpdate(true);
		}
	}
	
	
	private void expandGroup(){
		int count = mUpdateAdapter.getGroupCount();
		for(int i = 0; i < count; i++){
			mExpandableListView.expandGroup(i);
		}
	}
		
	public void uninstallRefresh(String pkgName) {
		List<String> updateList = AppUpdateManager.getUninstallInfoList();
		for (String packageName : updateList) {
			if(packageName.equals(pkgName)) {
				resetUpdateInfo();
				mUpdateAdapter.notifyDataSetChanged();
				break;
			}
		}
	}
	
	public ExpandableListView getmExpandableListView() {
		return mExpandableListView;
	}
}
