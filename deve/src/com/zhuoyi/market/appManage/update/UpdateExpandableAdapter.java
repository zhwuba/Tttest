package com.zhuoyi.market.appManage.update;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.download.common.DownBaseInfo;
import com.market.download.common.DownloadSettings;
import com.market.download.updates.AppUpdateManager;
import com.market.download.updates.AppUpdateManager.GroupData;
import com.market.download.updates.UpdateAppDisplayInfo;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadManager;
import com.market.download.util.Util;
import com.market.statistics.ReportFlag;
import com.market.view.UpdateExpandLayout;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

@SuppressLint("ResourceAsColor")
public class UpdateExpandableAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "UpdateExpandableAdapter";
    
	private LayoutInflater inflater;
	private PackageManager mPkgManager;
	
	private AppUpdateManager mAppUpManager;
	
	private LinearLayout mUpdateAllView;
	private TextView mUpdateAllBtn;
	private TextView mNotifyTextView;
	private LinearLayout mUpdateLayout;
	private boolean isAutoUpdateOpen;
	
	private AsyncImageCache mAsyncImageCache;
	private Context mContext;
	private MarketUpdateActivity mActivity;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	//private HashMap<String, AppInfoBto> mAppInfoMap = new HashMap<String, AppInfoBto>();
	
	private UpdateManagerView mUpdateView;
	
	private HashMap<String, UnupDataViews> mUnupItemViewMap = new HashMap<String, UnupDataViews>();
	private HashMap<UnupDataViews, String> mItemViewPkgNameMap = new HashMap<UnupDataViews, String>();
	private HashMap<String, Boolean> mItemCollapseMap = new HashMap<String, Boolean>();		//保存item的收缩状态
	
	private boolean mIsSystemApp = false;
	private boolean mHasUpdateApp = true;
	private static final int TYPE_MAX_COUNT = 2;
	
	public UpdateExpandableAdapter(MarketUpdateActivity activity ,DownloadCallBackInterface callBack, UpdateManagerView updateView){
		//mAppInfoMap = drawableMap;
		mActivity = activity;
		mContext = activity.getApplicationContext();
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
		mAppUpManager = AppUpdateManager.getStaticInstance();
		AppUpdateManager.setActivityHandler(mHandler);
		
		mUpdateView = updateView;
		
		inflater = LayoutInflater.from(mContext);
		mPkgManager = mContext.getPackageManager();
		
		mUpdateAllView = updateView.getUpdateAllView();
		mUpdateAllBtn = updateView.getUpdateAllBtn();
		mNotifyTextView = updateView.getNotifyTextView();
		mUpdateLayout = updateView.getUpdateLayout();
		mUpdateAllBtn.setOnClickListener(mUpdateAllListener);
		mAsyncImageCache = AsyncImageCache.from(mContext);
		
		try {
			mIsSystemApp = isSystemApp(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSystemApp(PackageInfo pInfo) {  
		return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);  
	}  

	public void initAdapter(){
	    AppUpdateManager.initUpdateAppInfoList(mContext);
		setGroupsData();
		refreshExternelViews();
	}
	
	
	View.OnClickListener mUpdateAllListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
		    updateAllApps();
		}
	};
	
	
	public void updateAllApps() {
        if(MarketUtils.getAPNType(mContext) != -1){
        	
        	if (mUpdateAllView != null && mUpdateAllView.getVisibility() == View.VISIBLE) {
        		mUpdateAllBtn.setEnabled(false);
        	}
        	
            new Thread() {
                public void run() {
                    String pkgName = null;
                    UpdateAppDisplayInfo disInfo = null;
                    int length = mUninstallInfoList.size();
                    for(int i=0; i < length; i++){
                        try {
                            pkgName = mUninstallInfoList.get(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }
                        disInfo = getUpdateAppInfo(pkgName);
                        if(disInfo.getDiffDownUrl()==null) {
                            //不是增量更新
                            if(mDownloadCallBack != null && mDownloadCallBack.get() != null && disInfo != null) {
                                mDownloadCallBack.get().startDownloadApp(pkgName,
                                        disInfo.getAppName(),
                                        null,
                                        disInfo.getMd5(),
                                        disInfo.getDownUrl(),
                                        ReportFlag.TOPIC_NULL,
                                        ReportFlag.FROM_UPDATE_MANA,
                                        disInfo.getVerCode(),
                                        disInfo.getApkId(),
                                        disInfo.getFileSize());
                                
                                /**
                                 * 在扩展菜单展开时，点击“更新”按钮，收起扩展菜单并隐藏相关“详情”、“卸载”、“忽略更新”
                                 */
                                mItemCollapseMap.remove(pkgName);
                            }
                        } else {
                            //是增量更新
                            try {
                                if(mActivity.addDiffDownload(pkgName, disInfo.getAppName(), disInfo.getMd5(), disInfo.getDownUrl(), ReportFlag.TOPIC_NULL,
                                        ReportFlag.FROM_UPDATE_MANA, disInfo.getVerCode(), disInfo.getApkId(), disInfo.getDiffDownUrl(), disInfo.getFileSize(), disInfo.getDiffPatchSize())) {
                                    mItemCollapseMap.remove(pkgName);
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
//                    notifyDataSetChanged();
                }
            }.start();
        }else{
            Toast.makeText(mContext, R.string.no_connect_hint, Toast.LENGTH_SHORT).show();
        }
    }
	
	
	public void refreshExternelViews() {
		isAutoUpdateOpen = DownloadSettings.getUserUpdateAutoFlag(mContext);

		int uninstallNum = 0, updatedNum = 0;
		if(mUninstallInfoList != null) {
			uninstallNum = mUninstallInfoList.size();
		}
		if(mUpdatedInfoList != null) {
			updatedNum = mUpdatedInfoList.size();
		}

		//设置全部安装按钮的显示状态
		if(uninstallNum > 0 || updatedNum > 0) {
			if(uninstallNum > 0) {
				if (AppUpdateManager.getDownCompleteNum() - AppUpdateManager.getIgnoreDownloadCompletedApp(mContext).size() >= uninstallNum) {
					mUpdateAllBtn.setText(R.string.update_listView_install_all_btn);
				} else {
					mUpdateAllBtn.setText(R.string.update_listView_update_all_btn);
				}

				mUpdateAllView.setVisibility(View.VISIBLE);
			} else {
			    mUpdateAllView.setVisibility(View.GONE);
			}
		}
		
		long downloadSize = 0L;
		long autoDownloadSize = AppUpdateManager.getCurrAutoDownloadSize();
		UpdateAppDisplayInfo appInfo = null;
		String apkName = "";
		//可更新列表
		for (int i=0; i<uninstallNum; i++) {
		    
		    try {
                apkName = mUninstallInfoList.get(i);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            if (TextUtils.isEmpty(apkName)) continue;
		    
		    appInfo = getUpdateAppInfo(apkName);
		    if (appInfo == null) continue;
		    
		    if (appInfo.isAutoDownloaded()) {
//		        autoDownloadSize = autoDownloadSize + appInfo.getFileSize();
		    } else {
		        downloadSize = downloadSize + appInfo.getFileSize();
		    }
		}
		//已更新列表
//        for (int i=0; i<updatedNum; i++) {
//            
//            try {
//                apkName = mUpdatedInfoList.get(i);
//            } catch (IndexOutOfBoundsException e) {
//                continue;
//            }
//            if (TextUtils.isEmpty(apkName)) continue;
//            
//            appInfo = getUpdateAppInfo(apkName);
//            if (appInfo == null) continue;
//            
//            if (appInfo.isAutoDownloaded()) {
//                autoDownloadSize = autoDownloadSize + appInfo.getFileSize();
//            }
//        }
        
        if(downloadSize >= 0.01 && autoDownloadSize >= 0.01) {
            String notifyText = mContext.getString(R.string.update_saved, getFileSizeSum(downloadSize), getFileSizeSum(autoDownloadSize));
            mNotifyTextView.setText(notifyText);
            if (mUpdateLayout.getVisibility() != View.VISIBLE) {
                mUpdateLayout.setVisibility(View.VISIBLE);
            }
        } else if (downloadSize >= 0.01 && autoDownloadSize < 0.01) {
            String notifyText = mContext.getString(R.string.update_saved2, getFileSizeSum(downloadSize));
            mNotifyTextView.setText(notifyText);
            if (mUpdateLayout.getVisibility() != View.VISIBLE) {
                mUpdateLayout.setVisibility(View.VISIBLE);
            }
        } else if (downloadSize < 0.01 && autoDownloadSize >= 0.01) {
            String notifyText = mContext.getString(R.string.update_saved3, getFileSizeSum(autoDownloadSize));
            mNotifyTextView.setText(notifyText);
            if (mUpdateLayout.getVisibility() != View.VISIBLE) {
                mUpdateLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (mUpdateLayout.getVisibility() == View.VISIBLE) {
                mUpdateLayout.setVisibility(View.GONE);
            }
        }
	}
	
	
    public String getFileSizeSum(long size) {
        String s = "";
        if (size < 0.01) {
            s = "0.00B";
        } else if (size < 1024) {
            s = new DecimalFormat("#.00").format(size);
            s += "B";
        } else if (size < 1024 * 1024) {
            float size1 = (float) (size / (1024.00));
            s = new DecimalFormat("#.00").format(size1);
            s += "KB";
        } else {
            float size1 = (float) (size / (1024.00 * 1024));
            s = new DecimalFormat("#.00").format(size1);
            s += "MB";
        }
        return s;
    }
	
	
	private void syncUpdateAllBtnState(){
		int uninstallNum = mUninstallInfoList.size();
		if(uninstallNum > 0){
		    if (AppUpdateManager.getDownCompleteNum() -  AppUpdateManager.getIgnoreDownloadCompletedApp(mContext).size() >= uninstallNum) {
                mUpdateAllBtn.setText(R.string.update_listView_install_all_btn);
            }else {
                mUpdateAllBtn.setText(R.string.update_listView_update_all_btn);
            }
            
		    mUpdateAllView.setVisibility(View.VISIBLE);
		}else{
		    mUpdateAllView.setVisibility(View.GONE);
		}
	}
	
	
	public void setGroupsData(){
		mGroups = AppUpdateManager.getUpdatedAppInfoList(mContext);
		
		//must get relative download event info after get updated app info list, unless will mistake
		mDownEventInfoMap = AppUpdateManager.getRelativeDownEventInfoMap();
		
		mUninstallInfoList = AppUpdateManager.getUninstallInfoList();
		mUpdatedInfoList = AppUpdateManager.getUpdatedInfoList();
	}
	
	private Handler mHandler = new Handler(){
		
		public void handleMessage(Message msg){
			int what = msg.what;
			switch(what){
				case AppUpdateManager.MSG_REFRESH_UPDATE_SCREEN:
					String pkgName = (String)msg.obj;
					Util.log(TAG, "handleMessage", "refresh update screen msg, package name: " + pkgName);
					UnupDataViews dataViews = mUnupItemViewMap.get(pkgName);
					if(dataViews != null){
						String currDateViewsPkgName = mItemViewPkgNameMap.get(dataViews);
						Util.log(TAG, "handleMessage", "refresh update screen msg, data view: " + currDateViewsPkgName);
						if(currDateViewsPkgName != null && pkgName.equals(currDateViewsPkgName)){
							UpdateAppDisplayInfo appInfo = getUpdateAppInfo(pkgName);
							if(!appInfo.isInstalled()){
								setUnupViewData(dataViews, appInfo);
							}
						} else {
						    notifyDataSetChanged();
						}
					}
					syncUpdateAllBtnState();
					refreshExternelViews();
					break;
					
				case AppUpdateManager.MSG_RESET_LIST:
					//setGroupsData();
					mUpdateView.resetUpdateInfo();
					notifyDataSetChanged();
					break;
					
				case AppUpdateManager.MSG_UPDATE_INSTALLED:
					notifyDataSetChanged();
					break;
			}
		}
	};
	
	private HashMap<String, DownloadEventInfo> mDownEventInfoMap;
	
	private List<GroupData> mGroups;
	
	private ArrayList<String> mUninstallInfoList;
	private ArrayList<String> mUpdatedInfoList;
	
	public DownloadEventInfo getDownloadEventInfo(String pkgName){
		return mDownEventInfoMap.get(pkgName);
	}
	
	private UpdateAppDisplayInfo getUpdateAppInfo(String pkgName){
		UpdateAppDisplayInfo updateInfo = null;
		if(mAppUpManager != null){
			updateInfo = mAppUpManager.getUpdateAppInfo(pkgName);
		}else{
			updateInfo = AppUpdateManager.getUpdateAppInfo(mContext, pkgName);
		}
		return updateInfo;
	}
	
	
	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return mGroups.get(groupPosition).groupType;
	}

	@Override
	public int getChildTypeCount() {
		return TYPE_MAX_COUNT;
	}

	
	@Override
	public int getGroupTypeCount() {
		return mGroups.size();
	}
	
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if(groupPosition == AppUpdateManager.UNUPDATE_GROUP && !mHasUpdateApp) {
			return "";
		}
		if(mGroups.size() <= groupPosition) {
		    return "";
		}
		ArrayList<String> updateInfoList = mGroups.get(groupPosition).updateAppInfoList;
		if(updateInfoList.size() <= childPosition) {
		    return "";
		}
		
		return mGroups.get(groupPosition).updateAppInfoList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		int count = mGroups.get(groupPosition).updateAppInfoList.size();
		if(groupPosition == AppUpdateManager.UNUPDATE_GROUP ){
			if(count <= 0){
				mHasUpdateApp = false;
				count = 1;
			}else{
				mHasUpdateApp = true;
			}
		}
		return count;
	}
	
	@Override
	public int getGroupCount() {
		return mGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}
	
	
	
	
	/*
	 * start for group view
	 */
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		
		GroupViewHolder holder = null;
		if (convertView == null) {
			holder = new GroupViewHolder();
			convertView = inflater.inflate(R.layout.update_group_title_view, null);
			holder.name = (TextView) convertView.findViewById(R.id.update_group_title_name);
			holder.update = (TextView) convertView.findViewById(R.id.update_group_title_update);
			holder.ignore = (TextView) convertView.findViewById(R.id.update_group_title_ignore);
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		
		setGroupViewData(holder, groupPosition);
		
		return convertView;
	}
	
	
	public static class GroupViewHolder {
		public TextView name;
		public TextView update;
		public TextView ignore;
	}
	
	
	private void setGroupViewData(GroupViewHolder holder, int position) {
		String groupText = ((GroupData)getGroup(position)).groupName;
		holder.name.setText(groupText);
		switch (position) {
		case AppUpdateManager.UNUPDATE_GROUP:
			
			//零流量更新
			setUserUpdateAuto(holder.update, false);
			holder.update.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setUserUpdateAuto((TextView) v, true);
				}
				
			});
			
			//忽略更新
    		int count = AppUpdateManager.getUpdateIgnoreList(mContext).size();
	        if (count > 0) {
	            if (holder.ignore.getVisibility() != View.VISIBLE)
	            	holder.ignore.setVisibility(View.VISIBLE);
	            holder.ignore.setText(String.format(mContext.getText(R.string.update_ignore_num).toString(), count));
	            holder.ignore.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(mContext, UpdateIgnoreActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(intent);
					}
					
				});
	        } else {
	            if (holder.ignore.getVisibility() == View.VISIBLE)
	            	holder.ignore.setVisibility(View.GONE);
	        }
			break;
		case AppUpdateManager.UPDATED_GROUP:
			if (holder.update.getVisibility() == View.VISIBLE) {
				holder.update.setVisibility(View.GONE);
			}
			
			if (holder.ignore.getVisibility() == View.VISIBLE) {
				holder.ignore.setVisibility(View.GONE);
			}
			break;
		}
	}
	
	
	private void setUserUpdateAuto(TextView view, boolean click) {
		Drawable drawable = null;
		boolean autoFlag = DownloadSettings.getUserUpdateAutoFlag(mContext);
		
		if (click) {
			autoFlag = !autoFlag;
			DownloadSettings.setUserUpdateAutoFlag(mContext, autoFlag);
		} 
		
		if (autoFlag) {
			if (view.getVisibility() == View.VISIBLE) {
				view.setVisibility(View.GONE);
			}
		} else {
			if (view.getVisibility() != View.VISIBLE) {
				view.setVisibility(View.VISIBLE);
			}
			drawable = mContext.getResources().getDrawable(R.drawable.update_unselect);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			view.setCompoundDrawables(null, null, drawable, null);
		}
		
		if (click) {
			refreshExternelViews();
			notifyDataSetChanged();
		}
	}
	
	
	/*
	 * start for childView
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		String pkgName = (String)getChild(groupPosition, childPosition);
		UpdateAppDisplayInfo appInfo = getUpdateAppInfo(pkgName);
		int groupType = getChildType(groupPosition, childPosition);

		switch(groupType) {
		case AppUpdateManager.UNUPDATE_GROUP:
			if(!mHasUpdateApp) {
				//无可更新应用
				convertView = getNoUpdateView();
			} else {
				UnupDataViews unupDataViews = null;
				if(appInfo == null) {
					convertView = View.inflate(mContext,R.layout.layout_update_item_empty, null);
					return convertView;
				}
				if(convertView == null || convertView.getTag() == null) {
					convertView = View.inflate(mContext,R.layout.update_list, null);
					unupDataViews = initUnupDataViews(convertView);
				} else {
					unupDataViews = (UnupDataViews) convertView.getTag();
				}
				mAsyncImageCache.displayImage(true,
						unupDataViews.mIcon,
						R.drawable.picture_bg1_big,
						new AsyncImageCache.UpdateAppImageGenerator(pkgName, mPkgManager),
						true);

				setUnupDataListener(unupDataViews, appInfo);
				setUnupViewData(unupDataViews, appInfo);
				mUnupItemViewMap.put(pkgName, unupDataViews);
				mItemViewPkgNameMap.put(unupDataViews, pkgName);
			}
			break;

		case AppUpdateManager.UPDATED_GROUP:
			UpedDataViews upedDataViews = null;
			if(convertView == null || convertView.getTag() == null) {
				convertView = inflater.inflate(R.layout.updated_list, parent,false);
				upedDataViews = initUpedDataViews(convertView);
			} else {
				upedDataViews = (UpedDataViews)convertView.getTag();
			}

			mAsyncImageCache.displayImage(true,
					upedDataViews.mIcon,
					R.drawable.picture_bg1_big,
					new AsyncImageCache.UpdateAppImageGenerator(pkgName, mPkgManager),
					true);
			setUpedViewData(upedDataViews, appInfo);
			break;

		}

		return convertView;
	}
	
	private View getNoUpdateView(){
		TextView textView= new TextView(mContext);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,MarketUtils.dipToPixels(mContext, 140.0f));
		textView.setLayoutParams(layoutParams);
		textView.setText(mContext.getResources().getString(R.string.myself_no_update));
		textView.setGravity(Gravity.CENTER);
		textView.setTextColor(R.color.detail_show_left_text);
		textView.setTag(null);
		return textView;
	}
	
	
	private void setUnupViewData(UnupDataViews itemDataViews, UpdateAppDisplayInfo appInfo){
		String pkgName = appInfo.getPkgName();
		long apkSize = appInfo.getFileSize();
		String fileSizeStr = MarketUtils.humanReadableByteCount(apkSize,false);
		DownloadEventInfo downEventInfo = getDownloadEventInfo(pkgName);
		int downloadState = DownBaseInfo.STATE_READY;
		int downloadArray = DownloadEventInfo.ARRAY_UPDATE;
		long curSize = -1;
		
		if(downEventInfo != null){
			downloadState = downEventInfo.getCurrState();
			downloadArray = downEventInfo.getEventArray();
			curSize = downEventInfo.getCurrDownloadSize();
		} else {
		    curSize = appInfo.getCurrDownSize();
		}

		itemDataViews.mAppSize.setText(fileSizeStr);
		itemDataViews.mAppName.setText(appInfo.getAppName());
		itemDataViews.mVersionChange.setText(appInfo.getVerUpStr());
		
		if(downEventInfo != null && downloadArray <= DownloadEventInfo.ARRAY_PAUSED) {
			itemDataViews.mUpdateExpandLayout.setmBottomEnable(true);
		} else {
			itemDataViews.mUpdateExpandLayout.setmBottomEnable(false);
		}
		
		if(mItemCollapseMap.containsKey(pkgName)) {
			boolean collapse = mItemCollapseMap.get(pkgName);
			if(collapse != itemDataViews.mUpdateExpandLayout.getCollapse()) {
				itemDataViews.mUpdateExpandLayout.setCollapse(mItemCollapseMap.get(pkgName));
			}
		} else {
			itemDataViews.mUpdateExpandLayout.setCollapse(true);
		}
		
		if(MarketUtils.isSystemApp(mContext, pkgName)) {	//系统应用，不可卸载
			itemDataViews.mUpdateExpandLayout.getUninstallView().setEnabled(false);
		} else {
			itemDataViews.mUpdateExpandLayout.getUninstallView().setEnabled(true);
		}
		String updateDesc = appInfo.getmVerUptDes();
		if(TextUtils.isEmpty(updateDesc)) {
			itemDataViews.mUpdateExpandLayout.setFirstLineText(mContext.getText(R.string.update_time).toString() 
					+ (appInfo.getmVerUptTime() == null ? MarketUtils.formatTime(System.currentTimeMillis()) : appInfo.getmVerUptTime()));
			itemDataViews.mUpdateExpandLayout.setText("");
		} else {
			itemDataViews.mUpdateExpandLayout.setFirstLineText(mContext.getText(R.string.update_version_new_content).toString());
			itemDataViews.mUpdateExpandLayout.setText(updateDesc);
		}
		if(appInfo.isAutoDownloaded() && appInfo.isApkFileExist() && isAutoUpdateOpen) {
			//零流量
			itemDataViews.mApkSizeCover.setVisibility(View.VISIBLE);
			itemDataViews.mHasDownTV.setVisibility(View.VISIBLE); 
			itemDataViews.mHasDownTV.setText(mContext.getResources().getString(R.string.update_view_undown_text));
            
			if (itemDataViews.mDownload.getVisibility() == View.VISIBLE) {
                itemDataViews.mDownload.setVisibility(View.GONE);
            }
            
            if (itemDataViews.mUnDownload.getVisibility() != View.VISIBLE) {
                itemDataViews.mUnDownload.setVisibility(View.VISIBLE);
            }
			
//            itemDataViews.mOperationBtn.setClickable(true);
//            itemDataViews.mOperationBtn.setText(R.string.install);
//            itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_install_btn);
//            itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_install_color));
//            itemDataViews.mOperationBtn.setOnClickListener(mInstallListener);
			
		} else {
		    itemDataViews.mApkSizeCover.setVisibility(View.INVISIBLE);
            itemDataViews.mHasDownTV.setVisibility(View.INVISIBLE);
		}
		if(appInfo.getDiffDownUrl() != null) {
			//增量
			itemDataViews.mApkSizeCover.setVisibility(View.VISIBLE);
			itemDataViews.mHasDownTV.setVisibility(View.VISIBLE);
			itemDataViews.mHasDownTV.setText(MarketUtils.humanReadableByteCount(appInfo.getDiffPatchSize(),false));
		}
		if(downloadState == DownBaseInfo.STATE_READY || (downloadState >= DownBaseInfo.STATE_DOWNLOAD_COMPLETE && !appInfo.isApkFileExist())){
			if(downloadArray == DownloadEventInfo.ARRAY_WAITING && DownloadManager.getDownloadingNumber() > 0){
				itemDataViews.mOperationBtn.setClickable(true);
				itemDataViews.mOperationBtn.setText(R.string.waiting);
				itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_installing_btn);
				itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
				itemDataViews.mOperationBtn.setOnClickListener(mPauseListener);
				
			}else{
			    itemDataViews.mOperationBtn.setClickable(true);
			    itemDataViews.mOperationBtn.setText(R.string.update);
			    itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_update_btn);
			    itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_update_color));
			    itemDataViews.mOperationBtn.setOnClickListener(mDownloadListener);
				
			}
			if (itemDataViews.mDownload.getVisibility() == View.VISIBLE) {
                itemDataViews.mDownload.setVisibility(View.GONE);
            }
            if (itemDataViews.mUnDownload.getVisibility() != View.VISIBLE) {
                itemDataViews.mUnDownload.setVisibility(View.VISIBLE);
            }
		}else if(downloadState == DownBaseInfo.STATE_DOWNLOADING){
			
			String speed = MarketUtils.getCountSpeedInfo(downEventInfo.getDownloadSpeed());
			itemDataViews.mSpeed.setText(speed); 
			
			if (itemDataViews.mDownload.getVisibility() != View.VISIBLE) {
			    itemDataViews.mDownload.setVisibility(View.VISIBLE);
			}
			
			if (itemDataViews.mUnDownload.getVisibility() == View.VISIBLE) {
			    itemDataViews.mUnDownload.setVisibility(View.GONE);
			}
			
			if(appInfo.getDiffDownUrl() != null) {
				//增量
				long noNeedDownSize = apkSize - appInfo.getDiffPatchSize();
				float p =  (float) noNeedDownSize/apkSize * 1.0f;
				int hasDownProgress = (int)(p * 100);
				
				float p1 = (float) (curSize + noNeedDownSize) / apkSize*1.0f;
				int currDownProgress = (int) (p1 * 100);
				if(currDownProgress >= 100) {
					currDownProgress = 99;
				} else if(currDownProgress == 0) {
					currDownProgress = 1;
				}
				
				String currentSizeString, totalSizeString;
				if(curSize+noNeedDownSize > 0)
					currentSizeString = MarketUtils.humanReadableByteCount(curSize+noNeedDownSize,false).replaceAll(" ", ""); 
				else
					currentSizeString = "0.00B";
				
				if (apkSize > 0)
					totalSizeString = MarketUtils.humanReadableByteCount(apkSize,false).replaceAll(" ", "");
				else 
					totalSizeString = mContext.getResources().getString(R.string.unknow_data);
				
				itemDataViews.mProgressBar.setProgress(hasDownProgress);
				itemDataViews.mProgressBar.setSecondaryProgress(currDownProgress);
				itemDataViews.mDownloadedSizeText.setText(currentSizeString);
				itemDataViews.mTotalSizeText.setText("/" + totalSizeString);
			} else {
				float _pecent = (float) curSize / apkSize*1.0f;
				int progress = (int)(_pecent*100);
				if(progress>100){
					progress = 100;
				} else if(progress == 0){
					progress = 1;
				}
				
				String currentSizeString, totalSizeString;
				if(curSize > 0)
					currentSizeString = MarketUtils.humanReadableByteCount(curSize,false).replaceAll(" ", ""); 
				else
					currentSizeString = "0.00B";
				
				if (apkSize > 0)
					totalSizeString = MarketUtils.humanReadableByteCount(apkSize,false).replaceAll(" ", "");
				else 
					totalSizeString = mContext.getResources().getString(R.string.unknow_data);
				
				itemDataViews.mProgressBar.setProgress(progress);
				itemDataViews.mDownloadedSizeText.setText(currentSizeString);
				itemDataViews.mTotalSizeText.setText("/" + totalSizeString);
			}
			itemDataViews.mOperationBtn.setClickable(true);
			itemDataViews.mOperationBtn.setText(R.string.pause);
			itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_installing_btn);
			itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
			itemDataViews.mOperationBtn.setOnClickListener(mPauseListener);
			
		}else if(downloadState > DownBaseInfo.STATE_DOWNLOADING && downloadState < DownBaseInfo.STATE_DOWNLOAD_COMPLETE){
		    if (itemDataViews.mDownload.getVisibility() == View.VISIBLE) {
                itemDataViews.mDownload.setVisibility(View.GONE);
            }
            if (itemDataViews.mUnDownload.getVisibility() != View.VISIBLE) {
                itemDataViews.mUnDownload.setVisibility(View.VISIBLE);
            }
            itemDataViews.mOperationBtn.setClickable(true);
            itemDataViews.mOperationBtn.setText(R.string.update);
            itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_update_btn);
            itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_update_color));
            itemDataViews.mOperationBtn.setOnClickListener(mDownloadListener);
			
		}else{
		    if (itemDataViews.mDownload.getVisibility() == View.VISIBLE) {
                itemDataViews.mDownload.setVisibility(View.GONE);
            }
            if (itemDataViews.mUnDownload.getVisibility() != View.VISIBLE) {
                itemDataViews.mUnDownload.setVisibility(View.VISIBLE);
            }
            if(downloadState == DownBaseInfo.STATE_INSTALLING){
			    itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_installing_btn_normal);
			    itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
			    itemDataViews.mOperationBtn.setText(R.string.installing);
			    itemDataViews.mOperationBtn.setClickable(false);
			
			}else{
			    itemDataViews.mOperationBtn.setClickable(true);
			    itemDataViews.mOperationBtn.setText(R.string.install);
			    itemDataViews.mOperationBtn.setBackgroundResource(R.drawable.common_install_btn);
			    itemDataViews.mOperationBtn.setTextColor(mContext.getResources().getColor(R.color.common_app_install_color));
			    itemDataViews.mOperationBtn.setOnClickListener(mInstallListener);
			}
		}

		if (mUpdateAllView != null && mUpdateAllView.getVisibility() == View.VISIBLE) {
			boolean enabled = isAllInstallBtnEnabled();
			mUpdateAllBtn.setEnabled(enabled);
		}
	}
	
	private UnupDataViews initUnupDataViews(View convertView){
		UnupDataViews itemDataViews = new UnupDataViews();
		itemDataViews.mAppName = (TextView) convertView.findViewById(R.id.app_name);
		itemDataViews.mIcon = (ImageView) convertView.findViewById(R.id.app_image);
		itemDataViews.mDownloadedSizeText = (TextView) convertView.findViewById(R.id.list_downloaded_size);
		itemDataViews.mTotalSizeText = (TextView) convertView.findViewById(R.id.list_total_size);
		
		itemDataViews.mProgressBar = (ProgressBar) convertView.findViewById(R.id.downProgressbar);
		
		itemDataViews.mVersionChange = (TextView) convertView.findViewById(R.id.version_change);
		itemDataViews.mAppSize = (TextView) convertView.findViewById(R.id.list_fileSize_textView);
		itemDataViews.mApkSizeCover = (View) convertView.findViewById(R.id.list_fileSize_coverView);
		itemDataViews.mHasDownTV = (TextView) convertView.findViewById(R.id.list_fileSize_undown_notify_textView);
		
		itemDataViews.mOperationBtn = (TextView) convertView.findViewById(R.id.list_operation_bt);
		
		itemDataViews.mUpdateContent = (TextView) convertView.findViewById(R.id.update_content_text);
		itemDataViews.mUnUpdateLayout = (RelativeLayout) convertView.findViewById(R.id.update_layout);
		itemDataViews.mUpdateExpandLayout = (UpdateExpandLayout) convertView.findViewById(R.id.update_expand_view);
		
		itemDataViews.mSpeed = (TextView) convertView.findViewById(R.id.list_download_speed);
		itemDataViews.mDownload = (LinearLayout) convertView.findViewById(R.id.app_download);
		itemDataViews.mUnDownload = (LinearLayout) convertView.findViewById(R.id.app_undownload);
		
		convertView.setTag(itemDataViews);
		
		return itemDataViews;
	}
	
	
	private void setUnupDataListener(final UnupDataViews dataViews, UpdateAppDisplayInfo appInfo){
		final String pkgName = appInfo.getPkgName();
		dataViews.mOperationBtn.setTag(R.id.tag_operation_data, appInfo);
		dataViews.mOperationBtn.setTag(R.id.tag_operation_view, dataViews);
		
		dataViews.mIcon.setTag(R.id.tag_update_id, appInfo.getApkId());
		
		dataViews.mIcon.setOnClickListener(mIconListenrer);
	
		dataViews.mUpdateExpandLayout.getEntryDetailView().setTag(R.id.tag_expand_flag, pkgName);
		dataViews.mUpdateExpandLayout.getUninstallView().setTag(R.id.tag_expand_flag, pkgName);
		dataViews.mUpdateExpandLayout.getIgnoreView().setTag(R.id.tag_expand_flag, pkgName);
		dataViews.mUpdateExpandLayout.getEntryDetailView().setOnClickListener(mExpandListener);
		dataViews.mUpdateExpandLayout.getUninstallView().setOnClickListener(mExpandListener);
		dataViews.mUpdateExpandLayout.getIgnoreView().setOnClickListener(mExpandListener);
		
		dataViews.mUnUpdateLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!dataViews.mUpdateExpandLayout.expandEnable()) return;
				//保证只有一个item 是展开状态
				mItemCollapseMap.clear();
				mItemCollapseMap.put(pkgName, dataViews.mUpdateExpandLayout.collapseClick());
				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						int bottomHeight = mContext.getResources().getDimensionPixelSize(R.dimen.update_all_layout_height);
						Rect rect = new Rect();
						dataViews.mUnUpdateLayout.getLocalVisibleRect(rect);
						
						int[] location = new int[2];
						mUpdateAllBtn.getLocationOnScreen(location);
						
						int marginTop = dataViews.mUnUpdateLayout.getTop();
						int unUpdateLayoutHeight = dataViews.mUnUpdateLayout.getHeight();
						if(rect.bottom < dataViews.mUnUpdateLayout.getHeight() || 
								(marginTop + unUpdateLayoutHeight) > location[1] - mContext.getResources().getDimensionPixelOffset(R.dimen.title_heigh) && !dataViews.mUpdateExpandLayout.getCollapse()) {	//扩展项被遮盖住,让listView上滑
							mUpdateView.getmExpandableListView().smoothScrollBy(unUpdateLayoutHeight -  mUpdateView.getmExpandableListView().getHeight() + marginTop
									+  bottomHeight, 300);
						}
					}
				}, 200);
				notifyDataSetChanged();
			}
		});
		
	}
	
	
	android.view.View.OnClickListener mIconListenrer = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int refId = Integer.parseInt(v.getTag(R.id.tag_update_id).toString());
			MarketUtils.startDetailActivity(mContext, refId, ReportFlag.FROM_UPDATE_MANA, -1, null);
		}
	};
	
	
	OnClickListener mExpandListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			UpdateAppDisplayInfo disInfo = null;
			switch (v.getId()) {
			case R.id.update_entry_deitail:
				disInfo = getUpdateAppInfo(v.getTag(R.id.tag_expand_flag).toString());
				MarketUtils.startDetailActivity(mContext, disInfo.getApkId(), ReportFlag.FROM_UPDATE_MANA, -1 , null);
				break;

			case R.id.update_uninstall:
				disInfo = getUpdateAppInfo(v.getTag(R.id.tag_expand_flag).toString());
				uninstallApp(disInfo.getPkgName(), disInfo.getAppName());
				break;
			
			case R.id.update_ignore:
				disInfo = getUpdateAppInfo(v.getTag(R.id.tag_expand_flag).toString());
				String pkgName = disInfo.getPkgName();
				AppUpdateManager.ignoreUpdateByPkgName(v.getContext(), pkgName);
				Toast.makeText(mContext, String.format(mContext.getText(R.string.update_ignore_app_name).toString(), disInfo.getAppName()), Toast.LENGTH_SHORT).show();
				if(mItemCollapseMap.containsKey(pkgName)) {
					mItemCollapseMap.put(pkgName,true);
				}
				
				try {
					mActivity.ignoreUpdate(pkgName, disInfo.getVerCode());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
				notifyDataSetChanged();
				break;

			}
		}
	};
	
	View.OnClickListener mDownloadListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
			if(MarketUtils.getAPNType(mContext) != -1){
			    UpdateAppDisplayInfo disInfo = (UpdateAppDisplayInfo) v.getTag(R.id.tag_operation_data);
			    UnupDataViews dataViews = (UnupDataViews) v.getTag(R.id.tag_operation_view);
			    
			    dataViews.mOperationBtn.setText(R.string.waiting);
			    dataViews.mOperationBtn.setOnClickListener(mPauseListener);

				if(disInfo.getDiffDownUrl()==null) {
					//不是增量更新
					if(mDownloadCallBack != null && mDownloadCallBack.get() != null){
						mDownloadCallBack.get().startDownloadApp(disInfo.getPkgName(),
								disInfo.getAppName(),
								null,
								disInfo.getMd5(),
								disInfo.getDownUrl(),
								ReportFlag.TOPIC_NULL,
								ReportFlag.FROM_UPDATE_MANA,
								disInfo.getVerCode(),
								disInfo.getApkId(),
								disInfo.getFileSize());
						
						/**
						 * 在扩展菜单展开时，点击“更新”按钮，收起扩展菜单并隐藏相关“详情”、“卸载”、“忽略更新”
						 */
						mItemCollapseMap.remove(disInfo.getPkgName());
						dataViews.mUpdateExpandLayout.setCollapse(true);
					}
				} else {
					//增量更新
					try {
						if(mActivity.addDiffDownload(disInfo.getPkgName(), disInfo.getAppName(), disInfo.getMd5(), disInfo.getDownUrl(), ReportFlag.TOPIC_NULL,
								ReportFlag.FROM_UPDATE_MANA, disInfo.getVerCode(), disInfo.getApkId(), disInfo.getDiffDownUrl(), disInfo.getFileSize(), disInfo.getDiffPatchSize())) {
							
							/**
							 * 在扩展菜单展开时，点击“更新”按钮，收起扩展菜单并隐藏相关“详情”、“卸载”、“忽略更新”
							 */
							mItemCollapseMap.remove(disInfo.getPkgName());
							dataViews.mUpdateExpandLayout.setCollapse(true);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
	
				}
				
				
			}else{
				Toast.makeText(mContext, R.string.no_connect_hint, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	View.OnClickListener mPauseListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
		    UpdateAppDisplayInfo updateInfo = (UpdateAppDisplayInfo) v.getTag(R.id.tag_operation_data);
            UnupDataViews dataViews = (UnupDataViews) v.getTag(R.id.tag_operation_view);
            dataViews.mOperationBtn.setText(R.string.update);
            dataViews.mOperationBtn.setOnClickListener(mDownloadListener);
            
			if(mDownloadCallBack != null && mDownloadCallBack.get() != null){
				mDownloadCallBack.get().downloadPause(updateInfo.getPkgName(), updateInfo.getVerCode());
			}
			
		}
	};
	
	View.OnClickListener mInstallListener = new View.OnClickListener(){
		@Override
		public void onClick(View v) {
		    UpdateAppDisplayInfo disInfo = (UpdateAppDisplayInfo) v.getTag(R.id.tag_operation_data);
            UnupDataViews dataViews = (UnupDataViews) v.getTag(R.id.tag_operation_view);
		    
            if(disInfo.getDiffDownUrl()==null) {
                //不是增量更新
                if(mDownloadCallBack != null && mDownloadCallBack.get() != null){
                    mDownloadCallBack.get().startDownloadApp(disInfo.getPkgName(),
                            disInfo.getAppName(),
                            null,
                            disInfo.getMd5(),
                            disInfo.getDownUrl(),
                            ReportFlag.TOPIC_NULL,
                            ReportFlag.FROM_UPDATE_MANA,
                            disInfo.getVerCode(),
                            disInfo.getApkId(),
                            disInfo.getFileSize());
                    
                    /**
                     * 在扩展菜单展开时，点击“更新”按钮，收起扩展菜单并隐藏相关“详情”、“卸载”、“忽略更新”
                     */
                    mItemCollapseMap.remove(disInfo.getPkgName());
                    dataViews.mUpdateExpandLayout.setCollapse(true);
                }
            } else {
                //增量更新
                try {
                    if(mActivity.addDiffDownload(disInfo.getPkgName(), disInfo.getAppName(), disInfo.getMd5(), disInfo.getDownUrl(), ReportFlag.TOPIC_NULL,
                            ReportFlag.FROM_UPDATE_MANA, disInfo.getVerCode(), disInfo.getApkId(), disInfo.getDiffDownUrl(), disInfo.getFileSize(), disInfo.getDiffPatchSize())) {
                        
                        /**
                         * 在扩展菜单展开时，点击“更新”按钮，收起扩展菜单并隐藏相关“详情”、“卸载”、“忽略更新”
                         */
                        mItemCollapseMap.remove(disInfo.getPkgName());
                        dataViews.mUpdateExpandLayout.setCollapse(true);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
            
		}
	};
	
	public static class UnupDataViews{
		public TextView mAppName;
		public ImageView mIcon;
		public TextView mVersionChange;
		public TextView mAppSize;
		public View mApkSizeCover;
		public TextView mHasDownTV;
		public ProgressBar mProgressBar;
		public TextView mDownloadedSizeText;
		public TextView mTotalSizeText;
		public TextView mOperationBtn;
		public TextView mUpdateContent;
		public RelativeLayout mUnUpdateLayout;
		public UpdateExpandLayout mUpdateExpandLayout;
		public TextView mSpeed;
		public LinearLayout mDownload;
		public LinearLayout mUnDownload;
	}
	
	

	private void setUpedViewData(UpedDataViews itemDataViews, UpdateAppDisplayInfo appInfo){
	    
	    if (appInfo == null || itemDataViews == null) return;
	    
		String fileSizeStr = MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false);
		itemDataViews.mAppSize.setText(fileSizeStr);
		
		itemDataViews.mAppName.setText(appInfo.getAppName());
		itemDataViews.mVersionChange.setText(appInfo.getVerUpStr());
		
		if (appInfo.isAutoDownloaded()) {
		    if (itemDataViews.mWifiUpdate.getVisibility() != View.VISIBLE)
		        itemDataViews.mWifiUpdate.setVisibility(View.VISIBLE);
		} else {
		    if (itemDataViews.mWifiUpdate.getVisibility() == View.VISIBLE)
		        itemDataViews.mWifiUpdate.setVisibility(View.GONE); 
		}
		
		itemDataViews.mOpenBtn.setOnClickListener(new OpenAppClickListener(appInfo.getPkgName()));
	}
	
	
	private UpedDataViews initUpedDataViews(View convertView){
		UpedDataViews itemDataViews = new UpedDataViews();
		itemDataViews.mAppName = (TextView) convertView.findViewById(R.id.app_name);
		itemDataViews.mIcon = (ImageView) convertView.findViewById(R.id.app_image);
		
		itemDataViews.mVersionChange = (TextView) convertView.findViewById(R.id.version_change);
		itemDataViews.mAppSize = (TextView) convertView.findViewById(R.id.list_fileSize_textView);
		itemDataViews.mOpenBtn = (TextView) convertView.findViewById(R.id.open_btn);
		
		itemDataViews.mWifiUpdate = (TextView) convertView.findViewById(R.id.list_wifi_tip_textView);
		
		convertView.setTag(itemDataViews);
		
		return itemDataViews;
	}
	
	public static class UpedDataViews{
		public TextView mAppName;
		public ImageView mIcon;
		public TextView mVersionChange;
		public TextView mAppSize; 
		public TextView mOpenBtn;
		public TextView mWifiUpdate;
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		initAdapter();
	}
	
	public void uninstallApp(final String pkgName, String appName) {
		if(!mIsSystemApp) {
			uninstallApk(mContext, pkgName);
		} else {

			final Dialog dialog = new Dialog(mActivity, R.style.MyMarketDialog);
			dialog.setContentView(R.layout.uninstall_dialog);
			dialog.setCanceledOnTouchOutside(true);
			Button cancel = (Button) dialog.findViewById(R.id.uninstall_dialog_cancel_button);
			Button sure = (Button) dialog.findViewById(R.id.uninstall_dialog_ok_button);
			ImageView icon = (ImageView) dialog.findViewById(R.id.uninstall_app_icon);
			TextView appText = (TextView) dialog.findViewById(R.id.uninstall_app_name);

			mAsyncImageCache.displayImage(true,
					icon,
					R.drawable.picture_bg1_big,
					new AsyncImageCache.UpdateAppImageGenerator(pkgName, mPkgManager),
					true);
			appText.setText(appName);
			cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.cancel();
				}
			});

			sure.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog.dismiss();
					if(!MarketUtils.uninstallAPK(pkgName)) {
						Toast.makeText(mContext, R.string.app_uninstall_failed, Toast.LENGTH_SHORT).show();
					} else {
						mUpdateView.resetUpdateInfo();
						notifyDataSetChanged();
						Toast.makeText(mContext, R.string.app_has_uninstalled, Toast.LENGTH_SHORT).show();
					}
				}
			});

			dialog.show();
		}
	}
	
	public void freeViewResource() {
		mAppUpManager.releaseRes();
		mAppUpManager = null;
	}
	
	
	public static void uninstallApk(Context context, String packageName) {  
	    Uri uri = Uri.parse("package:" + packageName);  
	    Intent intent = new Intent(Intent.ACTION_DELETE, uri);
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startActivity(intent);  
	} 
	
	
	/**
	 * 打开应用
	 * @author dream.zhou
	 *
	 */
    class OpenAppClickListener implements OnClickListener  {
        
        private String mPkgName = null;
        
        public OpenAppClickListener(String pkgName) {
            mPkgName = pkgName;
        }

        @Override
        public void onClick(View v) {
            try{
                if (!TextUtils.isEmpty(mPkgName)) {
                    Context context = v.getContext();
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(mPkgName);
                    if (i == null) {
                        i = new Intent(mPkgName);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    context.startActivity(i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private boolean isAllInstallBtnEnabled() {
    	
    	DownloadEventInfo appInfo = null;
    	String apkName = null;
    	int uninstallNum = 0;
		if(mUninstallInfoList != null) {
			uninstallNum = mUninstallInfoList.size();
		}

		for (int i=0; i<uninstallNum; i++) {
		    
		    try {
                apkName = mUninstallInfoList.get(i);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            if (TextUtils.isEmpty(apkName)) continue;
		    
		    appInfo = getDownloadEventInfo(apkName);
		    
		    int state = -1;
		    if (appInfo != null) {
		    	state = appInfo.getEventArray();
		    }	
		    if (state != DownloadEventInfo.ARRAY_DOWNLOADING
		    		&& state != DownloadEventInfo.ARRAY_WAITING) {
		    	return true;
		    }
		}
		
		return false;
    }
}
