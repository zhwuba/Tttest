package com.zhuoyi.market.appdetail;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.DensityUtil;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.MarketUtils.FileManage;

public class DetailRecommendView {

	private Context mContext;
	private List<AppInfoBto> mAppInfoBtos;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	private OnItemClick mOnItemClick;
	private List<ViewCache> mViewCaches;
	private String mReportFlag;
	
	public DetailRecommendView(Context context, String reportFlag) {
		mContext = context;
		mReportFlag = reportFlag;
		mViewCaches = new ArrayList<DetailRecommendView.ViewCache>();
	}


	public View getView(List<AppInfoBto> appInfoBtos, DownloadCallBackInterface callback) {
		LinearLayout recommendView = new LinearLayout(mContext);
		recommendView.setOrientation(LinearLayout.HORIZONTAL);
		recommendView.setGravity(Gravity.CENTER);
		recommendView.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		mAppInfoBtos = appInfoBtos;
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callback);
		mViewCaches.clear();
		int size = mAppInfoBtos.size();
		for (int i = 0; i < size; i++) {
			View view = View.inflate(mContext, R.layout.recommend_app_gridview, null);
			ViewCache viewCache =  new ViewCache();
			viewCache.app_icon = (ImageView) view.findViewById(R.id.app_image);
			viewCache.app_name = (TextView) view.findViewById(R.id.app_name);
			viewCache.install_btn = (TextView) view.findViewById(R.id.install_button);
			viewCache.app_downtime = (TextView) view.findViewById(R.id.app_download_percent);
			
			viewCache.position = i;
			view.setTag(viewCache);
			bindData(mAppInfoBtos.get(i), viewCache);
			if(i == 0) {
				view.setPadding(0, 0, mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_recommend_margin), 0);
			} else if(i == size - 1) {
				view.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_recommend_margin), 0, 0, 0);
			} else {
				view.setPadding(mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_recommend_margin), 0, 
						mContext.getResources().getDimensionPixelOffset(R.dimen.app_detail_recommend_margin) , 0);
			}
 			
			mViewCaches.add(viewCache);
			recommendView.addView(view);
		}
		return recommendView;
	}

	
	private void bindData(final AppInfoBto appInfoBto, final ViewCache holder) {
		String packageName = appInfoBto.getPackageName();
		String imageUrl = appInfoBto.getImgUrl(); 
		String versionCode = appInfoBto.getVersionCode()+"";
		int downloadTime = appInfoBto.getDownTimes();
		String downloadPercent = appInfoBto.getPercent();
		holder.app_name.setText(appInfoBto.getName());
		
		if(downloadTime > 0) {
			holder.app_downtime.setText(getDownNum(mContext, downloadTime) + mContext.getString(R.string.download_str));
		} else if(!TextUtils.isEmpty(downloadPercent)){
			holder.app_downtime.setText(downloadPercent + mContext.getString(R.string.download_percent));
		} else {
			holder.app_downtime.setVisibility(View.GONE);
		}
		
		AsyncImageCache.from(mContext).displayImage(true, holder.app_icon, R.drawable.picture_bg1_big, new AsyncImageCache.NetworkImageGenerator(packageName,imageUrl), true);
		
		if(MarketUtils.checkInstalled(mContext,packageName)&& MarketUtils.isEqualsVersionCode(mContext, versionCode, packageName))
		{
			holder.install_btn.setBackgroundResource(R.drawable.common_open_btn);
			holder.install_btn.setTextColor(mContext.getResources().getColor(R.color.common_app_open_color));
			holder.install_btn.setText(R.string.open);
			holder.install_btn.setTag("open");
		}
		else
		{
			holder.install_btn.setBackgroundResource(R.drawable.common_install_btn);
			holder.install_btn.setTextColor(mContext.getResources().getColor(R.color.common_app_install_color));
			holder.install_btn.setText(R.string.install);
			holder.install_btn.setTag("install");
		}
		
		holder.install_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String packageName = "";
				String appName = "";
				String filePath = "";
				String downloadUrl = "";
				AppInfoBto mapData= appInfoBto;
				if(mapData==null) {
					return;
				}
				packageName = mapData.getPackageName();
				appName = mapData.getName();
				downloadUrl = mapData.getDownUrl();
				String sdPath1 = MarketUtils.FileManage.getSDPath();
				if(v.getTag().toString().equals("open")) {
				    MarketUtils.openCurrentActivity(mContext, mapData.getPackageName());
					return;
				}
				
				if(MarketUtils.getAPNType(mContext) == -1) {
					Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				else if (TextUtils.isEmpty(sdPath1)) {
					Toast.makeText(mContext, mContext.getResources().getString(R.string.no_sd_card),Toast.LENGTH_SHORT).show();
					return;
				}
				else {
					if (mDownloadCallBack != null && mDownloadCallBack.get() != null)
						mDownloadCallBack.get().startDownloadApp(
                            packageName, 
                            appName, 
                            filePath, 
                            mapData.getMd5(), 
                            downloadUrl,
                            ReportFlag.TOPIC_NULL,
                            mReportFlag, 
                            mapData.getVersionCode(), 
                            mapData.getRefId(),
                            mapData.getFileSize());
				}
			}
		});
		
		holder.app_icon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mOnItemClick != null) {
					mOnItemClick.OnItemClickListener(holder.position);
				}
			}
		});
	}
	
	
	public interface OnItemClick{
		public void OnItemClickListener(int position);
	}
	
	
	public void setOnItemClickListener(OnItemClick mOnItemClick) {
		this.mOnItemClick = mOnItemClick;
	}
	

	public void notifyDataSetChanged() {
		if(mViewCaches != null && mAppInfoBtos != null) {
			int size = mViewCaches.size();
			for(int i = 0; i < mAppInfoBtos.size(); i++) {
				if( i < size) {
					bindData(mAppInfoBtos.get(i), mViewCaches.get(i));
				}
			}
		}
	}
	
	private StringBuffer getDownNum(Context context, long count) {
		StringBuffer count_string = new StringBuffer();
			if (count > 600000)
				count_string.append(">100").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 500000)
				count_string.append(">50").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 300000)
				count_string.append(">30").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 200000)
				count_string.append(">20").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count >= 100000)
				count_string.append(">10").append(
						context.getResources().getString(R.string.ten_thousand));
			else if (count > 10000) 
				count_string.append(count / 10000 + context.getResources().getString(R.string.ten_thousand));
			else 
				count_string.append("<1" + context.getResources().getString(R.string.ten_thousand));
			
		return count_string;
	}
	
	static class ViewCache {
		ImageView 	app_icon;        
		TextView 	app_name;  
		TextView install_btn;
		TextView app_downtime;
		int position;
	}
}
