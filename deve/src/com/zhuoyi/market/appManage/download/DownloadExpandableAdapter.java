package com.zhuoyi.market.appManage.download;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.market.download.userDownload.DownloadEventInfo;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.download.DelTipDialog.OnDelTipClickListener;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class DownloadExpandableAdapter extends BaseExpandableListAdapter implements OnDelTipClickListener {

	private Context mContext;
	private ExpandableListView mListView;
	private DisplayDownloadDataStorage mDisplayDownloadDataStorage = null;

	private AsyncImageCache mAsyncImageCache;

	private LayoutInflater inflater;

	private static final int TYPE_Downloading = 0;
	private static final int TYPE_Recommend = 1;
	private static final int TYPE_Downloaded = 2;
	private static final int TYPE_MAX_COUNT = TYPE_Downloaded + 1;
	
	private LinearLayout mGroupView1 = null;
	private LinearLayout mGroupView2 = null;
	private View mRecommendView = null;
	private View mRecommendTitle = null;
	private View mRecommendTitle2 = null;
	private TextView mDownloadNum = null;
	private TextView mDownloadedNum = null;
	private TextView mDownloadedClear = null;
	
	private String mOldHideTag = null;
	private DelTipDialog mDelTipDialog = null;
	
	private WeakReference<DownloadInterface> mCallBack = null;

	/*
	 * 参数1：context对象
	 * 参数2：一级列表数据源
	 * 参数3：二级列表数据源
	 */
	public DownloadExpandableAdapter(Context context, DisplayDownloadDataStorage dataStorage, ExpandableListView listView, WeakReference<DownloadInterface> callback)
	{
		mContext = context.getApplicationContext();
		mListView = listView;
		mCallBack = callback;
		mDisplayDownloadDataStorage = dataStorage;
		mAsyncImageCache = AsyncImageCache.from(mContext);
		inflater = LayoutInflater.from(mContext);
		
		mDelTipDialog = new DelTipDialog(context, this);
		
		initGroupView();
	}
	

	@Override
	public int getGroupCount() {
		return TYPE_MAX_COUNT;
	}
	

	@Override
	public int getChildrenCount(int groupPosition) {
	    if(TYPE_Downloading == groupPosition){
	        return mDisplayDownloadDataStorage.getDownloadSize();
	    }else if(TYPE_Downloaded == groupPosition){
	        return mDisplayDownloadDataStorage.getCompleteSize();
	    }else{
	        return 1;
		}
	}

	    
	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
        if(TYPE_Downloading == groupPosition){
            return mDisplayDownloadDataStorage.getDownloadInfo(childPosition);
        }else if(TYPE_Downloaded == groupPosition){
            return mDisplayDownloadDataStorage.getCompleteInfo(childPosition);
        }else{
            return null;
        }
	}
	

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		if(groupPosition == TYPE_Downloading) {
			return TYPE_Downloading;
		} else if(groupPosition == TYPE_Downloaded) {
			return TYPE_Downloaded;
		}else{
		    return TYPE_Recommend;
		}
	}
	

	@Override
	public int getChildTypeCount() {
		return TYPE_MAX_COUNT;
	}
	

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
	
	
	public void setRecommendView(View view, View title){
	    mRecommendView = view;
	    mRecommendTitle = title;
        mRecommendTitle2 = mRecommendTitle.findViewById(R.id.download_recommend_title2);
	}

	private void initGroupView(){
	    mGroupView1 = (LinearLayout) inflater.inflate(R.layout.download_group_title_view, null);
	    ((TextView)mGroupView1.findViewById(R.id.download_group_title_name)).setText(mContext.getResources().getString(R.string.downloading_now));
	    ((TextView)mGroupView1.findViewById(R.id.download_group_title_clear)).setVisibility(View.GONE);
	    mDownloadNum = (TextView)mGroupView1.findViewById(R.id.download_group_title_num);
        
        mGroupView2 = (LinearLayout) inflater.inflate(R.layout.download_group_title_view, null);
        ((TextView)mGroupView2.findViewById(R.id.download_group_title_name)).setText(mContext.getResources().getString(R.string.downloaded_soft));
        mDownloadedNum = (TextView)mGroupView2.findViewById(R.id.download_group_title_num);
        mDownloadedClear = (TextView)mGroupView2.findViewById(R.id.download_group_title_clear);
        mDownloadedClear.setVisibility(View.GONE);
        mDownloadedClear.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {

                if (mDelTipDialog != null) {
                    int size = mDisplayDownloadDataStorage.getCompleteSize();
                    boolean fileExist = false;
                    for (int i=0; i<size; i++) {
                       if (mDisplayDownloadDataStorage.getCompleteInfo(i).getDisplayFileExist()) {
                           fileExist = true;
                           break;
                       } 
                    }
                    mDelTipDialog.setData(DelTipDialog.TIP_STYLE_2, null, null, null, -1, fileExist, false);
                    mDelTipDialog.show();
                }
             }
        });
	}

	
	public void setDownloadNum(){
	    if(mDownloadNum != null){
	        mDownloadNum.setText("("+mDisplayDownloadDataStorage.getDownloadSize()+")");
	    }
	}
	
	public void setDownloadedNum(){
	    if(mDownloadedNum != null){
            mDownloadedNum.setText("("+mDisplayDownloadDataStorage.getCompleteSize()+")");
        }
    }
	
	public void setDownloadRecommendTitle(){
	    if(mDisplayDownloadDataStorage.isDownload()){
            mRecommendTitle2.setVisibility(View.GONE);
        }else{
            mRecommendTitle2.setVisibility(View.VISIBLE);
        }
	}
	
	public void setDownloadedClearAllView(){
        if(mDisplayDownloadDataStorage.isComplete()){
            if(mDownloadedClear.getVisibility() == View.GONE)
                mDownloadedClear.setVisibility(View.VISIBLE);
        }else{
            if(mDownloadedClear.getVisibility() == View.VISIBLE)
                mDownloadedClear.setVisibility(View.GONE);
        }
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
	    if(groupPosition == TYPE_Downloading){
	        return mGroupView1;
	    }else if(groupPosition == TYPE_Downloaded){
	        return mGroupView2;
	    }else{
	        return mRecommendTitle;
	    }
	}


	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		ViewHolder2 holder2 = null;
		
		int type = getChildType(groupPosition,childPosition);
		switch (type) {
		case TYPE_Downloading:
		    if (convertView == null) {
		        convertView = inflater.inflate(R.layout.download_list, parent,false);
		        holder = getViewHolder(convertView);
		        convertView.setTag(holder);
			} else {
			    holder = (ViewHolder) convertView.getTag();
			}
		    
		    fillHolderData(holder, childPosition, convertView);
			break;
		case TYPE_Downloaded:
		    if (convertView == null) {
		        convertView = inflater.inflate(R.layout.finished_download_list, parent,false);
		        holder2 = getViewHolder2(convertView);
		        convertView.setTag(holder2);
			} else {
			    holder2 = (ViewHolder2) convertView.getTag();
			}
		    
		    fillHolder2Data(holder2, childPosition, convertView);
			break;
		case TYPE_Recommend:
		    return mRecommendView;
		}

		setDownloadNum();
		setDownloadedNum();
		setDownloadRecommendTitle();
		setDownloadedClearAllView();
		
		return convertView;
	}


	private void fillHolderData(ViewHolder holder, int childPosition, View convertView) {

		DisplayDownloadEventInfo displayInfo = mDisplayDownloadDataStorage.getDownloadInfo(childPosition);
		if (displayInfo == null) return;

		holder.mAppName.setText(displayInfo.getDisplayAppName());
		holder.mAppDownloadedSize.setText(displayInfo.getDisplayDownloadedSize());
		holder.mAppAllSize.setText("/"+displayInfo.getDisplayTotleSize());
		holder.mProgressBar.setProgress(displayInfo.getDisplayPecent());
		
		String pkgName = displayInfo.getDownloadEventInfo().getPkgName();
		if ("com.zhuoyi.market".equals(pkgName)) {
		    holder.mIcon.setImageResource(R.drawable.icon);
        } else {
    		int picId = R.drawable.picture_bg1_big;
    		String flag = displayInfo.getDownloadEventInfo().getDownloadFlag();
    		
    		/*if (flag != null && (flag.contains(ReportFlag.FROM_HOMEPAGE)
    		        || flag.contains(ReportFlag.FROM_DOWN_GIFT)
    		        || flag.contains(ReportFlag.FROM_TYD_LAUNCHER)
    				|| flag.contains("/1/HomeRecommend")
    				|| flag.contains("/13/DownloadRegard"))) {
    		    picId = R.drawable.picture_bg1_big;
    		}*/
    
    		mAsyncImageCache.displayImage(true, holder.mIcon, picId, new AsyncImageCache.GeneralImageGenerator(pkgName,null), true);
		}

		switch(displayInfo.getDownloadEventInfo().getEventArray()){
		case DownloadEventInfo.ARRAY_WAITING:
			holder.mImageDownloadButton.setText(R.string.waiting);
			holder.mSpeed.setText(mContext.getString(R.string.waiting));
			break;
		case DownloadEventInfo.ARRAY_DOWNLOADING:
			holder.mImageDownloadButton.setText(R.string.pause);
			holder.mSpeed.setText(displayInfo.getDisplaySpeed());
			break;
		case DownloadEventInfo.ARRAY_PAUSED:
			holder.mImageDownloadButton.setText(R.string.dialog_proceed);
			holder.mSpeed.setText(mContext.getString(R.string.paused));
			break;
		}
		
		String tag = pkgName + displayInfo.getDownloadEventInfo().getVersionCode();
		
		holder.mImageDownloadButton.setOnClickListener(new DownloadingInstallListener(childPosition,holder));
		holder.mImageDownloadButton.setTag(tag + "_btn");
		holder.mSpeed.setTag(tag + "_speed");

		holder.mHideView.setTag(tag + "_hide");
		holder.mHideBar.setTag(tag + "_bar");
		
		if (tag.equals(mOldHideTag)) {
		    if (holder.mHideView.getVisibility() != View.VISIBLE) {
		        holder.mHideView.setVisibility(View.VISIBLE);
		        holder.mHideBar.setBackgroundResource(R.drawable.desc_less);
		    }
		} else {
		    if (holder.mHideView.getVisibility() == View.VISIBLE) {
		        holder.mHideView.setVisibility(View.GONE);
		        holder.mHideBar.setBackgroundResource(R.drawable.desc_more);
            }
		}
		
		convertView.setOnClickListener(new HideBarClickListener(tag));
		
		holder.mHideDel.setOnClickListener(new HideDelClickListener(displayInfo.getDownloadEventInfo(),
		        DelTipDialog.TIP_STYLE_0, false));
		
		int appId = displayInfo.getDownloadEventInfo().getAppId();
        if (appId == 0 || "com.zhuoyi.market".equals(pkgName)) {
            holder.mHideDetail.setEnabled(false);
        } else {
            holder.mHideDetail.setEnabled(true);
            holder.mHideDetail.setOnClickListener(new HideDetailClickListener(appId));
        }
	}
	
	
	private void fillHolder2Data(ViewHolder2 holder2, int childPosition, View convertView) {

		DisplayDownloadEventInfo displayInfo = mDisplayDownloadDataStorage.getCompleteInfo(childPosition);
        if (displayInfo == null) return;
		String pkgName = displayInfo.getDownloadEventInfo().getPkgName();
		
		holder2.mAppName.setText(displayInfo.getDisplayAppName());
		holder2.mAppVersion.setText(displayInfo.getDisplayVersion());
		holder2.mAppSize.setText(displayInfo.getDisplayTotleSize());
		
		if ("com.zhuoyi.market".equals(pkgName)) {
		    holder2.mIcon.setImageResource(R.drawable.icon);
		} else { 
    		int picId = R.drawable.picture_bg1_big;
    		String flag = displayInfo.getDownloadEventInfo().getDownloadFlag();
    		
            /*if (flag != null && (flag.contains(ReportFlag.FROM_HOMEPAGE)
                    || flag.contains(ReportFlag.FROM_DOWN_GIFT)
                    || flag.contains(ReportFlag.FROM_TYD_LAUNCHER)
                    || flag.contains("/1/HomeRecommend")
                    || flag.contains("/13/DownloadRegard"))) {
                picId = R.drawable.picture_bg1_big;
            }*/
            
    		mAsyncImageCache.displayImage(true, holder2.mIcon, picId, new AsyncImageCache.GeneralImageGenerator(pkgName,null), true);
		}
		
		if (displayInfo.getDisplayInstalled()) {
		    holder2.mImageDownloadButton.setText(R.string.open);
            holder2.mImageDownloadButton.setTextColor(mContext.getResources().getColor(R.color.common_app_open_color));
            holder2.mImageDownloadButton.setBackgroundResource(R.drawable.common_open_btn);
		} else if (displayInfo.getDisplayInstalling()) {
			holder2.mImageDownloadButton.setText(R.string.install_now);
            holder2.mImageDownloadButton.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
            holder2.mImageDownloadButton.setBackgroundResource(R.drawable.common_installing_btn_normal);
		} else {
		    holder2.mImageDownloadButton.setText(R.string.install);
            holder2.mImageDownloadButton.setTextColor(mContext.getResources().getColor(R.color.common_app_install_color));
            holder2.mImageDownloadButton.setBackgroundResource(R.drawable.common_install_btn);
		}

		String tag = pkgName + displayInfo.getDownloadEventInfo().getVersionCode();
		holder2.mImageDownloadButton.setOnClickListener(new DownloadedInstallListener(childPosition));
		
		holder2.mHideView.setTag(tag + "_hide");
        holder2.mHideBar.setTag(tag + "_bar");
        
        if (tag.equals(mOldHideTag)) {
            if (holder2.mHideView.getVisibility() != View.VISIBLE) {
                holder2.mHideView.setVisibility(View.VISIBLE);
                holder2.mHideBar.setBackgroundResource(R.drawable.desc_less);
            }
        } else {
            if (holder2.mHideView.getVisibility() == View.VISIBLE) {
                holder2.mHideView.setVisibility(View.GONE);
                holder2.mHideBar.setBackgroundResource(R.drawable.desc_more);
            }
        }
        
        convertView.setOnClickListener(new HideBarClickListener(tag));
		
        holder2.mHideDel.setOnClickListener(new HideDelClickListener(displayInfo.getDownloadEventInfo(),
                DelTipDialog.TIP_STYLE_1, displayInfo.getDisplayInstalled()));
        
        int appId = displayInfo.getDownloadEventInfo().getAppId();
        if (appId == 0 || "com.zhuoyi.market".equals(pkgName)) {
            holder2.mHideDetail.setEnabled(false);
        } else {
            holder2.mHideDetail.setEnabled(true);
            holder2.mHideDetail.setOnClickListener(new HideDetailClickListener(appId));
        }
	}


	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private ViewHolder getViewHolder(View view) {
		ViewHolder holder = new ViewHolder(); 			
		holder.mAppName = (TextView) view.findViewById(R.id.list_tv1);
		holder.mIcon = (ImageView) view.findViewById(R.id.list_iv1); 
		holder.mAppDownloadedSize = (TextView) view.findViewById(R.id.list_downloaded_size);
		holder.mAppAllSize = (TextView) view.findViewById(R.id.list_total_size);
		holder.mSpeed = (TextView)view.findViewById(R.id.list_download_speed);
		holder.mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar_id);
		holder.mImageDownloadButton = (TextView) view.findViewById(R.id.list_download_bt);

		holder.mHideBar = (ImageView) view.findViewById(R.id.list_download_hide_bar);
		holder.mHideView = (LinearLayout) view.findViewById(R.id.list_download_hide);
		holder.mHideDel = (TextView) view.findViewById(R.id.list_download_hide_del);
		holder.mHideDetail = (TextView) view.findViewById(R.id.list_download_hide_detail);
		return holder;
	}

	
	private ViewHolder2 getViewHolder2(View view) {
		ViewHolder2 holder2 = new ViewHolder2(); 			
		holder2.mAppName = (TextView) view.findViewById(R.id.list_tv1);
		holder2.mIcon = (ImageView) view.findViewById(R.id.list_iv1); 
		holder2.mAppVersion = (TextView) view.findViewById(R.id.list_version);
		holder2.mAppSize = (TextView) view.findViewById(R.id.list_tv5);
		holder2.mImageDownloadButton = (TextView) view.findViewById(R.id.list_download_bt);
		
		holder2.mHideBar = (ImageView) view.findViewById(R.id.list_downloaded_hide_bar);
        holder2.mHideView = (LinearLayout) view.findViewById(R.id.list_downloaded_hide);
        holder2.mHideDel = (TextView) view.findViewById(R.id.list_downloaded_hide_del);
        holder2.mHideDetail = (TextView) view.findViewById(R.id.list_downloaded_hide_detail);
		return holder2;
	}


	static class ViewHolder {
		TextView 	mAppName;
		ImageView 	mIcon;
		TextView 	mAppDownloadedSize;
		TextView    mAppAllSize;
		TextView 	mSpeed; 
		TextView   mImageDownloadButton;
		ProgressBar mProgressBar;
		
		LinearLayout mHideView;
		TextView mHideDel;
		TextView mHideDetail;
        ImageView mHideBar;
	}

	static class ViewHolder2 {
		TextView 	mAppName;
		ImageView 	mIcon;
		TextView 	mAppSize;
		TextView 	mAppVersion;
		TextView   mImageDownloadButton;
		
		LinearLayout mHideView;
        TextView mHideDel;
        TextView mHideDetail;
        ImageView mHideBar;
	}
	
	static class ViewHolder3 {
		TextView groupName;
	}


	class DownloadingInstallListener implements OnClickListener  {
		private int position;
		private long mPrvTime = 0;

		public DownloadingInstallListener(int position,ViewHolder holder) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
			long currTime = System.currentTimeMillis();
			if((currTime-mPrvTime)/1000.0f<0.7)
				return;

			mPrvTime = currTime;
			
			int speedTextId = -1;
			int btnTextId = -1;

			DownloadEventInfo downloadInfo = mDisplayDownloadDataStorage.getDownloadEventInfo(position);
			if (downloadInfo.getEventArray() == DownloadEventInfo.ARRAY_PAUSED) {
				String sdPath = MarketUtils.FileManage.getSDPath();

				if (TextUtils.isEmpty(sdPath)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_sd_card), Toast.LENGTH_SHORT).show();
					return;
				} else if (MarketUtils.getAPNType(mContext) == -1) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				} else {
					if (mCallBack != null) {
						DownloadInterface callback = mCallBack.get();
						if (callback != null) {
							callback.downloadStart(downloadInfo.getPkgName(), downloadInfo.getVersionCode());
						}
					}
					speedTextId = R.string.waiting;
					btnTextId = R.string.waiting;
				}
			} else if (downloadInfo.getEventArray() == DownloadEventInfo.ARRAY_DOWNLOADING
			        || downloadInfo.getEventArray() == DownloadEventInfo.ARRAY_WAITING) {

				String pkgName = downloadInfo.getPkgName();
				int verCode = downloadInfo.getVersionCode();

				if (mCallBack != null) {
					DownloadInterface callback = mCallBack.get();
					if (callback != null) {
						callback.downloadPause(pkgName, verCode);
					}
				}
				
				if (mDisplayDownloadDataStorage != null)
			        mDisplayDownloadDataStorage.pauseDownloadInfo(pkgName, verCode);
			    DownloadExpandableAdapter.this.notifyDataSetChanged();

				speedTextId = R.string.paused;
                btnTextId = R.string.dialog_proceed;
			}
			
            String tag = downloadInfo.getPkgName() + downloadInfo.getVersionCode();
            TextView btn = (TextView) mListView.findViewWithTag(tag + "_btn");
            if (btn != null && btnTextId != -1)
                btn.setText(btnTextId);
            
            TextView speed = (TextView) mListView.findViewWithTag(tag + "_speed");
            if (speed != null && speedTextId != -1)
                speed.setText(speedTextId);
		}

	}
	
	
	/**
	 * 点击展示、收起的监听事件
	 * @author dream.zhou
	 *
	 */
    class HideBarClickListener implements OnClickListener  {
        
        private String mTag;
        public HideBarClickListener(String tag) {
            mTag = tag;
        }

        @Override
        public void onClick(View v) {
            
            View newHideView = mListView.findViewWithTag(mTag + "_hide");
            View newBarView = mListView.findViewWithTag(mTag + "_bar");
            
            if (mTag.equals(mOldHideTag)) {
                if (newHideView != null) {
                    if (newHideView.getVisibility() == View.VISIBLE) {
                        newHideView.setVisibility(View.GONE);
                        if (newBarView != null) {
                            newBarView.setBackgroundResource(R.drawable.desc_more);
                        }
                        mOldHideTag = "";
                    } else {
                        newHideView.setVisibility(View.VISIBLE);
                        if (newBarView != null) {
                            newBarView.setBackgroundResource(R.drawable.desc_less);
                        }
                        mOldHideTag = mTag;
                    }
                }
            } else {
                
                setItemHideView();
                
                if (newHideView != null && newHideView.getVisibility() != View.VISIBLE) {
                    newHideView.setVisibility(View.VISIBLE);
                }

                if (newBarView != null) {
                    newBarView.setBackgroundResource(R.drawable.desc_less);
                }
                
                mOldHideTag = mTag;
            }
        }

    }
    
    
    public void setItemHideView(String tag) {
        if (!TextUtils.isEmpty(mOldHideTag) && mOldHideTag.equals(tag)) {
            setItemHideView();
        }
    }
    
    
    /**
     * 隐藏删除/详情按钮
     */
    public void setItemHideView() {
        
        View oldHideView = mListView.findViewWithTag(mOldHideTag + "_hide");
        if (oldHideView != null && oldHideView.getVisibility() == View.VISIBLE) {
            oldHideView.setVisibility(View.GONE);
        }
        
        View oldBarView = mListView.findViewWithTag(mOldHideTag + "_bar");
        if (oldBarView != null) {
            oldBarView.setBackgroundResource(R.drawable.desc_more);
        }
        
        mOldHideTag = "";
    }
    
    /**
     * 弹出框的应用下载完成，消掉弹出框
     * @param flag
     */
    public void dismissDialog(String flag) {
        if (mDelTipDialog != null 
                && mDelTipDialog.isShowing() 
                && flag != null 
                && flag.equals(mDelTipDialog.getCurAppFlag())) {
            mDelTipDialog.dismiss();
        }
    }
    
    
    /**
     * 删除单条记录的监听事件
     * @author dream.zhou
     *
     */
    class HideDelClickListener implements OnClickListener  {
        
        private int mStyle = -1;
        private DownloadEventInfo mInfo = null;
        private boolean mInstalled = false;
        
        public HideDelClickListener(DownloadEventInfo info, int style, boolean installed) {
            mInfo = info;
            mStyle = style;
            mInstalled = installed;
        }

        @Override
        public void onClick(View v) {
            
            if (mInfo == null) return;

            if (mDelTipDialog != null) {
                File f = mInfo.getApkFile();
                boolean fileExist = (f != null && f.exists()) ? true:false;
                mDelTipDialog.setData(mStyle, mInfo.getPkgName(), mInfo.getDownloadFlag(), mInfo.getAppName(), mInfo.getVersionCode(), fileExist, mInstalled);
                mDelTipDialog.show();
            }
            
        }
    }
    
    
    /**
     * 进入详情的监听事件
     * @author dream.zhou
     *
     */
    class HideDetailClickListener implements OnClickListener  {
        
        private int mAppId = 0;
        
        public HideDetailClickListener(int appId) {
            mAppId = appId;
        }

        @Override
        public void onClick(View v) {
            try{
            	String activityUrl = null;
            	if (mDisplayDownloadDataStorage != null) {
            		activityUrl = mDisplayDownloadDataStorage.getActivityUrl(mAppId);
            	} 
            	MarketUtils.startDetailActivity( v.getContext(), mAppId, null, -1, activityUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    

	class DownloadedInstallListener implements OnClickListener  {
		private int position;

		public DownloadedInstallListener(int position) {
			this.position = position;
		}

		@Override
		public void onClick(View v) {
		    
		    DisplayDownloadEventInfo displayInfo = mDisplayDownloadDataStorage.getCompleteInfo(position);
		    if (displayInfo == null) return;
		    
			String packageName = displayInfo.getDownloadEventInfo().getPkgName();
			String sdPath = MarketUtils.FileManage.getSDPath();
			
			if (displayInfo.getDisplayInstalling()) {
				return;
			}
			
			if(displayInfo.getDisplayInstalled() || (!displayInfo.getDisplayInstalled() && !displayInfo.getDisplayFileExist())) {
			    MarketUtils.openCurrentActivity(mContext, packageName);
                return;
			}
				
			if (TextUtils.isEmpty(sdPath)) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.no_sd_card), Toast.LENGTH_SHORT).show();
				return;
			}

			String localPath = displayInfo.getDownloadEventInfo().getApkFile().getAbsolutePath();
			if (TextUtils.isEmpty(localPath)) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.file_break), Toast.LENGTH_SHORT).show();
				return;
			} else {
			    if (mCallBack != null) {
                    DownloadInterface callback = mCallBack.get();
                    if (callback != null) {
                        callback.downloadStart(packageName, displayInfo.getDownloadEventInfo().getVersionCode());
                    }
                }
//				MarketUtils.AppInfoManager.AppInstall(localPath, mContext, packageName, packageName);
			}

		}

	}

	
    @Override
    public void onOkClick(String pkgName, int verCode, boolean downloading, boolean delFile) {
        // TODO Auto-generated method stub
        try {
            if (TextUtils.isEmpty(pkgName) && verCode == -1 && !downloading) {
                //清空下载记录
                delAll(delFile);
                
            } else {
                
                setItemHideView();
                
                //删除单条记录
                if (mCallBack != null) {
					DownloadInterface callback = mCallBack.get();
					if (callback != null) {
						callback.downloadDeleteItem(pkgName, verCode, delFile);
					}
				}
                
                delItem(pkgName, verCode, downloading, delFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void delAll(boolean delFile) {
    	if (mDisplayDownloadDataStorage == null) return;
    	
    	ArrayList<String> downloadedPkgName = new ArrayList<String>();
    	ArrayList<Integer> downloadedPkgVersion = new ArrayList<Integer>();
	    int eventCount = mDisplayDownloadDataStorage.getCompleteSize();
	    if(eventCount > 0){
	        DownloadEventInfo downloadedinfo = null;
		    for(int iCount=0; iCount<eventCount; iCount++){
		        downloadedinfo = mDisplayDownloadDataStorage.getCompleteEventInfo(iCount);
		        if (downloadedinfo == null) continue; 
		        downloadedPkgName.add(downloadedinfo.getPkgName());
		        downloadedPkgVersion.add(downloadedinfo.getVersionCode());
		    }
		    mDisplayDownloadDataStorage.clearComplete(true);
		    
            setDownloadNum();
            setDownloadedNum();
            setDownloadRecommendTitle();
            setDownloadedClearAllView();
            DownloadExpandableAdapter.this.notifyDataSetChanged();
            Toast.makeText(mContext, mContext.getString(R.string.del_all_success), Toast.LENGTH_SHORT).show();
            
            if (mCallBack != null) {
				DownloadInterface callback = mCallBack.get();
				if (callback != null) {
					callback.downloadDeleteAll(downloadedPkgName, downloadedPkgVersion, delFile);
				}
			}
	    }
    }
    
    
    private void delItem(String pkgName, int verCode, boolean downloading, boolean delFile) {
        if (mDisplayDownloadDataStorage == null) return;
        String toastName = null;
        
        if (downloading) {
            toastName = mDisplayDownloadDataStorage.delDownload(pkgName, verCode);
            if (!TextUtils.isEmpty(toastName)) {
                if(!mDisplayDownloadDataStorage.isDownload()){
                    setDownloadNum();
                    setDownloadedNum();
                    setDownloadRecommendTitle();
                    //getRecommendData();
                }
                DownloadExpandableAdapter.this.notifyDataSetChanged();
            }
        } else {
            toastName = mDisplayDownloadDataStorage.delComplete(pkgName, verCode);
            if (!TextUtils.isEmpty(toastName)) {
                if (!mDisplayDownloadDataStorage.isComplete()) {
                    setDownloadNum();
                    setDownloadedNum();
                    setDownloadRecommendTitle();
                    setDownloadedClearAllView();
                }
            	DownloadExpandableAdapter.this.notifyDataSetChanged();
            }
        }
        
        if (!TextUtils.isEmpty(toastName)) {
            if (downloading) {
                toastName = mContext.getString(R.string.del_download_success,toastName);
            } else {
                if (delFile) {
                    toastName = mContext.getString(R.string.del_downloaded_package,toastName);
                } else {
                    toastName = mContext.getString(R.string.del_downloaded_success,toastName);
                }
            }
            Toast.makeText(mContext, toastName, Toast.LENGTH_SHORT).show();
        }
    }
}
