package com.zhuoyi.market.adapter;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AppOperatorUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * listView通用Adapter,继承自CommonAdatper
 * 对于单布局样式,传人布局id,实现convert方法即可
 * 对于多布局样式,布局id可传-1,或调用对应构造方法. 可直接复写getView()方法,不必实现convert方法  
 * 当前直接实现类 HomeListAdapter , MyListNewAdapter
 * @author JLu
 *
 */
public abstract class CommonListAdapter<T> extends CommonAdapter<T> {
	
	private static String STR_UNIT_TEN_THOUSAND = "";
	private static String STR_DOWNLOAD_TIMES = "";
	private static final int OFFICAL_VISIBLE = 1;
	
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	
	protected String mReportFlag = "";
	public void setReportFlag(String flag) {
	    mReportFlag = flag;
	}
	
	protected int mTopicId = -1;
	public void setTopicId(int topicId) {
		mTopicId = topicId;
	}
	
	protected boolean mRefreshIcon = true;
	public void allowRefreshIcon(boolean status) {
		mRefreshIcon = status;
	}
	public boolean isAllowRefreshIcon() {
		return mRefreshIcon;
	}
	

	/**
	 * @param context
	 * @param callBack
	 */
	public CommonListAdapter(Context context, DownloadCallBackInterface callBack) {
		super(context, -1);
		STR_UNIT_TEN_THOUSAND = mContext.getResources().getString(R.string.ten_thousand);
		STR_DOWNLOAD_TIMES = mContext.getResources().getString(R.string.download_str);
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
	}

	/**
	 * @param context
	 * @param layoutId convertView将要inflate的布局id
	 * @param callBack
	 */
	public CommonListAdapter(Context context, int layoutId, DownloadCallBackInterface callBack) {
		super(context, layoutId);
		STR_UNIT_TEN_THOUSAND = mContext.getResources().getString(R.string.ten_thousand);
		STR_DOWNLOAD_TIMES = mContext.getResources().getString(R.string.download_str);
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
	}

	/**
	 * 
	 * @param context
	 * @param layoutId convertView将要inflate的布局id
	 * @param datas listView的数据,也可在实例化后通过setter方法传入
	 * @param callBack
	 */
	public CommonListAdapter(Context context, int layoutId, List<T> datas, DownloadCallBackInterface callBack) {
		super(context, layoutId, datas);
		STR_UNIT_TEN_THOUSAND = mContext.getResources().getString(R.string.ten_thousand);
		STR_DOWNLOAD_TIMES = mContext.getResources().getString(R.string.download_str);
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
	}
	
	
	protected void fillSingleLineAppData(RelativeLayout rlParent, ImageView icon, TextView appName, TextView appSize, TextView downloadNum, TextView description,
			TextView installBtn, ImageView cornerIcon, RatingBar appRatingStar, ImageView officialIcon, final AppInfoBto appInfo,
			boolean mergeChildId, boolean isTopicChild, int childId) {
		String imageUrl = appInfo.getImgUrl(); 
		String desc = appInfo.getBriefDesc();
		String searchDesc = appInfo.getDescription();
		CornerIconInfoBto cornerIconInfo = appInfo.getCornerMarkInfo();
		
		appName.setText(appInfo.getName());
		appSize.setText(appInfo.getFileSizeString());
		appRatingStar.setRating(appInfo.getStars());
		
		String downloadNumStr = getDownloadNumStr(appInfo.getDownTimes());
		downloadNum.setText(downloadNumStr);
		
		if(!TextUtils.isEmpty(desc)) {
			description.setVisibility(View.VISIBLE);
			description.setText(desc);
		} else if(!TextUtils.isEmpty(searchDesc)) {
			description.setVisibility(View.VISIBLE);
			description.setText(searchDesc);
		} else {
			description.setVisibility(View.GONE);
		} 
		initInstallBtn(installBtn,appInfo,icon, mergeChildId, isTopicChild, childId);
		installBtn.setTag(R.id.water_flow_tag, true);
		rlParent.setTag(R.id.water_flow_tag, true);
		rlParent.setOnClickListener(new AppItemOnClickListener(appInfo, mergeChildId, isTopicChild, childId));
		
		//动态添加官方标识到 应用名后
		if(appInfo.getOfficialLogo() == OFFICAL_VISIBLE) {
			officialIcon.setVisibility(View.VISIBLE);
			officialIcon.setImageResource(R.drawable.official);
		} else {
			officialIcon.setVisibility(View.GONE);
			officialIcon.setImageResource(0);
		}
		
		
		if(cornerIconInfo!=null && cornerIconInfo.getType() > 0) {
				cornerIcon.setVisibility(View.VISIBLE);
			AsyncImageCache.from(mContext).displayImage(cornerIcon, cornerIconInfo, 1);
		} else {
			if(cornerIcon.getVisibility() != View.GONE)
				cornerIcon.setVisibility(View.GONE);
		}
		
		int id = R.drawable.picture_bg1_big;
		/*if (ReportFlag.FROM_HOMEPAGE.equals(mReportFlag)) {
		    id = R.drawable.picture_bg1_big;
		}*/
		AsyncImageCache.from(mContext).displayImage(mRefreshIcon,icon, id,
				new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(),imageUrl), true);
	}
	
	
	protected void initInstallBtn(TextView install,AppInfoBto appInfo,ImageView icon, boolean mergeChildId, boolean isTopicChild, int childId) {
		AppOperatorUtils.initBtnState(mContext, install, appInfo.getPackageName(), appInfo.getVersionCode(), icon);
		String downFlag = null;
		if (mergeChildId) {
		    downFlag = ReportFlag.getReportFlag(mReportFlag, mTopicId, false, isTopicChild ? ReportFlag.CHILD_ID_TYPE_TOPICID : ReportFlag.CHILD_ID_TYPE_ASSEMBLY, childId);
		} else {
		    downFlag = mReportFlag;
		}
		
		install.setOnClickListener(new AppOperatorUtils.CommonAppClick(mContext, appInfo, mDownloadCallBack, Integer.toString(mTopicId), downFlag,false));
	}


	protected String getDownloadNumStr(int count) {
		StringBuilder count_string = new StringBuilder();

		if(count<100000) {
			count_string.append(count);
		} else {
			if(count>600000)
				count_string.append(">100"+STR_UNIT_TEN_THOUSAND);
			else if(count>500000)
				count_string.append(">50"+STR_UNIT_TEN_THOUSAND);
			else if(count>300000)
				count_string.append(">30"+STR_UNIT_TEN_THOUSAND);
			else if(count>200000)
				count_string.append(">20"+STR_UNIT_TEN_THOUSAND);
			else
				count_string.append(">10"+STR_UNIT_TEN_THOUSAND);
		}
		count_string.append(STR_DOWNLOAD_TIMES);
		return count_string.toString();
	}

	
	protected class AppItemOnClickListener implements OnClickListener {
		private AppInfoBto appInfo;
		
		private String reportFlag;
		
		public AppItemOnClickListener(AppInfoBto appInfo, boolean mergeChildId, boolean isTopicChild, int childId) {
			this.appInfo = appInfo;
			if (mergeChildId) {
			    reportFlag = ReportFlag.getReportFlag(mReportFlag, mTopicId, false, isTopicChild ? ReportFlag.CHILD_ID_TYPE_TOPICID : ReportFlag.CHILD_ID_TYPE_ASSEMBLY, childId);
	        } else {
	            reportFlag = mReportFlag;
	        }
		}

		@Override
		public void onClick(View v) {
			MarketUtils.startAppDetailActivity(v.getContext(), appInfo, reportFlag, mTopicId);
		}
		
	}


}
