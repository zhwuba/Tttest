package com.zhuoyi.market.appManage.update;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.download.updates.AppUpdateManager;
import com.market.download.updates.UpdateAppDisplayInfo;
import com.market.view.UpdateExpandLayout;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.system.util.AppInfoUtils;


public class IgnoreUpdateAdapter extends BaseAdapter {

	private ArrayList<String> mIgnoreList;
	private PackageManager mPkgManager;
	private HashMap<String, Boolean> mItemCollapseMap = new HashMap<String, Boolean>();		//保存item的收缩状态
	private Handler mHandler;
	private Context mContext;
	
	public IgnoreUpdateAdapter(Context context, Handler handler) {
		mIgnoreList = AppUpdateManager.getUpdateIgnoreList(context);
		mHandler = handler;
		mContext = context;
		initIgnoreList(mIgnoreList);
	}
	
	
	@Override
	public int getCount() {
		return mIgnoreList.size();
	}


	@Override
	public Object getItem(int position) {
		return mIgnoreList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Context context = parent.getContext();
		if(mPkgManager == null) {
			mPkgManager = context.getPackageManager();
		}
		if(convertView == null) {
			convertView = View.inflate(context, R.layout.update_manager_ignore_item, null);
			viewHolder = new ViewHolder();
			viewHolder.appImage = (ImageView) convertView.findViewById(R.id.ignore_app_image);
			viewHolder.appName = (TextView) convertView.findViewById(R.id.ignore_app_name);
			viewHolder.appSize = (TextView) convertView.findViewById(R.id.ignore_list_fileSize_textView);
			viewHolder.appVersionChange = (TextView) convertView.findViewById(R.id.ignore_version_change);
			viewHolder.ignoreExpandLayout = (UpdateExpandLayout) convertView.findViewById(R.id.ignore_expand_view);
			viewHolder.appIgnore = (TextView) convertView.findViewById(R.id.ignore_cancel);
			viewHolder.ignoreLayout = (RelativeLayout) convertView.findViewById(R.id.ignore_layout);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		UpdateAppDisplayInfo updateAppDisplayInfo = AppUpdateManager.getUpdateAppInfo(context, mIgnoreList.get(position));
		if(updateAppDisplayInfo != null) {
			bindViewData(context, viewHolder, updateAppDisplayInfo);
		}
		
		return convertView;
	}

	
	class ViewHolder {
		ImageView appImage;
		TextView appName;
		TextView appVersionChange;
		TextView appSize;
		TextView appIgnore;
		UpdateExpandLayout ignoreExpandLayout;
		RelativeLayout ignoreLayout;
	}
	
	
	private void bindViewData(Context context, final ViewHolder viewHolder, final UpdateAppDisplayInfo updateAppDisplayInfo) {
		AsyncImageCache.from(context).displayImage(true,
				viewHolder.appImage,
				R.drawable.picture_bg1_big,
				new AsyncImageCache.UpdateAppImageGenerator(updateAppDisplayInfo.getPkgName(), mPkgManager),
				true);

		final String pkgName = updateAppDisplayInfo.getPkgName();
		viewHolder.appName.setText(updateAppDisplayInfo.getAppName());
		viewHolder.appVersionChange.setText(updateAppDisplayInfo.getVerUpStr());
		viewHolder.appSize.setText(MarketUtils.humanReadableByteCount(updateAppDisplayInfo.getFileSize(), false));
		
		/**
		 *  更新内容第一行单独显示
		 */
		String updateDesc = updateAppDisplayInfo.getmVerUptDes();
		if(TextUtils.isEmpty(updateDesc)) {
			viewHolder.ignoreExpandLayout.setFirstLineText(context.getText(R.string.update_time).toString() 
					+	(updateAppDisplayInfo.getmVerUptTime() == null ? MarketUtils.formatTime(System.currentTimeMillis()) : updateAppDisplayInfo.getmVerUptTime()));
			viewHolder.ignoreExpandLayout.setText("");
		} else {
			viewHolder.ignoreExpandLayout.setFirstLineText(context.getText(R.string.update_version_new_content).toString());
			viewHolder.ignoreExpandLayout.setText(updateDesc);
		}

		/**
		 *  保存每个Item展开状态
		 */
		viewHolder.ignoreExpandLayout.isShowBottom(false);
		if(mItemCollapseMap.containsKey(pkgName)) {
			viewHolder.ignoreExpandLayout.setCollapse(mItemCollapseMap.get(pkgName));
		} else {
			viewHolder.ignoreExpandLayout.setCollapse(true);
		}
		
		
		
		viewHolder.ignoreLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//保证只有一个item 是展开状态
				mItemCollapseMap.clear();
				mItemCollapseMap.put(pkgName, viewHolder.ignoreExpandLayout.collapseClick());
				notifyDataSetChanged();
			}
		});
		
		viewHolder.appIgnore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AppUpdateManager.cancelIgnoreUpdateByPkgName(v.getContext(), updateAppDisplayInfo.getPkgName());
				mIgnoreList = AppUpdateManager.getUpdateIgnoreList(v.getContext());
				notifyDataSetChanged();
				mHandler.sendEmptyMessage(IgnoreManagerView.IGNORE_REFRESH);
			}
		});
	}
	
	
	private void initIgnoreList(ArrayList<String> ignoreList) {
		for (int i = 0; i < ignoreList.size(); i++) {
			UpdateAppDisplayInfo updateAppDisplayInfo = AppUpdateManager.getUpdateAppInfo(mContext, ignoreList.get(i));
			try {
				if(updateAppDisplayInfo == null || !AppInfoUtils.isApkExist(mContext, updateAppDisplayInfo.getPkgName())
						|| mContext.getPackageManager().getPackageInfo(updateAppDisplayInfo.getPkgName(), 0).versionCode >= updateAppDisplayInfo.getVerCode()) {
					mIgnoreList.remove(ignoreList.get(i));
					AppUpdateManager.removeIgnoreApp(mContext, updateAppDisplayInfo.getPkgName());
					mHandler.sendEmptyMessage(IgnoreManagerView.IGNORE_REFRESH);
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				mIgnoreList.remove(ignoreList.get(i));
				AppUpdateManager.removeIgnoreApp(mContext, updateAppDisplayInfo.getPkgName());
				mHandler.sendEmptyMessage(IgnoreManagerView.IGNORE_REFRESH);
			}
		}
	}


	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		initIgnoreList(mIgnoreList);
		mHandler.sendEmptyMessage(IgnoreManagerView.IGNORE_REFRESH);
	}
}
