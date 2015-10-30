package com.zhuoyi.market.ranklist;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.data.ChannelInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.market.net.data.TopicInfoBto;
import com.market.net.response.GetMarketFrameResp;
import com.zhuoyi.market.OneColModelActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.CommonListAdapter;
import com.zhuoyi.market.adapter.ViewHolder;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.view.CustomViewFactory;
public class RankListAdapter extends CommonListAdapter<AssemblyInfoBto> {

	private int mTopImgWidth;
	private int mTopImgHeight;
	
	public RankListAdapter(Context context, DownloadCallBackInterface callBack) {
		super(context,callBack);
		mTopImgWidth = mContext.getResources().getDimensionPixelSize(R.dimen.ranklist_item_img_top_width);
		mTopImgHeight= mContext.getResources().getDimensionPixelSize(R.dimen.ranklist_item_img_top_height);
	}

	@Override
	public void convert(ViewHolder holder, AssemblyInfoBto bean, int position) {
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewCache holder;
		final AssemblyInfoBto assemblyInfoBto;
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_model_topic_item, parent, false);
			holder = new ViewCache(); 			
//			holder.title = (TextView) convertView.findViewById(R.id.top_title);
			holder.topImg = (ImageView) convertView.findViewById(R.id.top_img);
			holder.lookAll = (TextView) convertView.findViewById(R.id.look_all);
			holder.bottom_layout = (LinearLayout) convertView.findViewById(R.id.bottom_layout);
			
			holder.left_parent_layout_top = (RelativeLayout) convertView.findViewById(R.id.left_parent_layout_top);
			holder.left_icon_top  = (ImageView) holder.left_parent_layout_top.findViewById(R.id.list_icon);				
			holder.left_app_name_top  = (TextView) holder.left_parent_layout_top.findViewById(R.id.list_app_name);
			holder.left_downloadNum_top  = (TextView) holder.left_parent_layout_top.findViewById(R.id.list_app_install_count);
			holder.left_install_top  = (TextView) holder.left_parent_layout_top.findViewById(R.id.install_button);
			holder.left_corner_icon_top  = (ImageView) holder.left_parent_layout_top .findViewById(R.id.corner_icon);

			holder.middle_parent_layout_top  = (RelativeLayout) convertView.findViewById(R.id.middle_parent_layout_top);
			holder.middle_icon_top  = (ImageView) holder.middle_parent_layout_top .findViewById(R.id.list_icon);				
			holder.middle_app_name_top  = (TextView) holder.middle_parent_layout_top .findViewById(R.id.list_app_name);
			holder.middle_downloadNum_top  = (TextView) holder.middle_parent_layout_top .findViewById(R.id.list_app_install_count);
			holder.middle_install_top  = (TextView) holder.middle_parent_layout_top .findViewById(R.id.install_button);
			holder.middle_corner_icon_top  = (ImageView) holder.middle_parent_layout_top.findViewById(R.id.corner_icon);

			holder.right_parent_layout_top  = (RelativeLayout) convertView.findViewById(R.id.right_parent_layout_top);
			holder.right_icon_top  = (ImageView) holder.right_parent_layout_top.findViewById(R.id.list_icon);				
			holder.right_app_name_top  = (TextView) holder.right_parent_layout_top.findViewById(R.id.list_app_name);
			holder.right_downloadNum_top  = (TextView) holder.right_parent_layout_top.findViewById(R.id.list_app_install_count);
			holder.right_install_top  = (TextView) holder.right_parent_layout_top.findViewById(R.id.install_button);
			holder.right_corner_icon_top  = (ImageView) holder.right_parent_layout_top.findViewById(R.id.corner_icon);


			holder.left_parent_layout_bottom = (RelativeLayout) convertView.findViewById(R.id.left_parent_layout_bottom);
			holder.left_icon_bottom  = (ImageView) holder.left_parent_layout_bottom.findViewById(R.id.list_icon);				
			holder.left_app_name_bottom  = (TextView) holder.left_parent_layout_bottom.findViewById(R.id.list_app_name);
			holder.left_downloadNum_bottom  = (TextView) holder.left_parent_layout_bottom.findViewById(R.id.list_app_install_count);
			holder.left_install_bottom  = (TextView) holder.left_parent_layout_bottom.findViewById(R.id.install_button);
			holder.left_corner_icon_bottom  = (ImageView) holder.left_parent_layout_bottom.findViewById(R.id.corner_icon);
			
			holder.middle_parent_layout_bottom  = (RelativeLayout) convertView.findViewById(R.id.middle_parent_layout_bottom);
			holder.middle_icon_bottom  = (ImageView) holder.middle_parent_layout_bottom.findViewById(R.id.list_icon);				
			holder.middle_app_name_bottom  = (TextView) holder.middle_parent_layout_bottom.findViewById(R.id.list_app_name);
			holder.middle_downloadNum_bottom  = (TextView) holder.middle_parent_layout_bottom.findViewById(R.id.list_app_install_count);
			holder.middle_install_bottom  = (TextView) holder.middle_parent_layout_bottom.findViewById(R.id.install_button);
			holder.middle_corner_icon_bottom  = (ImageView) holder.middle_parent_layout_bottom.findViewById(R.id.corner_icon);
			
			holder.right_parent_layout_bottom  = (RelativeLayout) convertView.findViewById(R.id.right_parent_layout_bottom);
			holder.right_icon_bottom  = (ImageView) holder.right_parent_layout_bottom.findViewById(R.id.list_icon);				
			holder.right_app_name_bottom  = (TextView) holder.right_parent_layout_bottom.findViewById(R.id.list_app_name);
			holder.right_downloadNum_bottom  = (TextView) holder.right_parent_layout_bottom.findViewById(R.id.list_app_install_count);
			holder.right_install_bottom  = (TextView) holder.right_parent_layout_bottom.findViewById(R.id.install_button);
			holder.right_corner_icon_bottom  = (ImageView) holder.right_parent_layout_bottom.findViewById(R.id.corner_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewCache)convertView.getTag(); 
		}
		
		assemblyInfoBto = getItem(position);
		List<AppInfoBto> appInfoBtos = assemblyInfoBto.getAppInfoList();
//		holder.title.setText(assemblyInfoBto.getAssName());
		holder.lookAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mContext, OneColModelActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("titleName", assemblyInfoBto.getAssName());
				intent.putExtra("viewType", CustomViewFactory.VIEW_RANKLIST_ALL);
				intent.putExtra("assemblyId", assemblyInfoBto.getAssemblyId());
				mContext.startActivity(intent);
			}
		});
		holder.topImg.setImageResource(-1);
		AsyncImageCache.from(mContext).displayImage(true,true,holder.topImg, -1, mTopImgWidth,mTopImgHeight,
				new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(assemblyInfoBto.getAssIconUrl()),assemblyInfoBto.getAssIconUrl()), false,true,false,"");
		
		bindItemData(holder, appInfoBtos, assemblyInfoBto.getAssemblyId());
		return convertView;
	}
	
	
	private void bindItemData(ViewCache holder, List<AppInfoBto> appInfoBtos, int assemblyId) {
		if(appInfoBtos == null) return;
		try {
			fillSingleLineAppData(holder.left_parent_layout_top, holder.left_icon_top, holder.left_app_name_top, holder.left_downloadNum_top,
					holder.left_install_top, holder.left_corner_icon_top, appInfoBtos.get(0),
					true, false, assemblyId);
			fillSingleLineAppData(holder.middle_parent_layout_top, holder.middle_icon_top, holder.middle_app_name_top, holder.middle_downloadNum_top,
					holder.middle_install_top, holder.middle_corner_icon_top, appInfoBtos.get(1),
                    true, false, assemblyId);
			fillSingleLineAppData(holder.right_parent_layout_top, holder.right_icon_top, holder.right_app_name_top, holder.right_downloadNum_top,
					holder.right_install_top, holder.right_corner_icon_top, appInfoBtos.get(2),
                    true, false, assemblyId);
			
			//少于6个 大于3个 不显示下面一行
			if(appInfoBtos.size() < 6) {
				holder.bottom_layout.setVisibility(View.GONE);
				return; 
			}
			fillSingleLineAppData(holder.left_parent_layout_bottom, holder.left_icon_bottom, holder.left_app_name_bottom, holder.left_downloadNum_bottom,
					holder.left_install_bottom, holder.left_corner_icon_bottom, appInfoBtos.get(3),
                    true, false, assemblyId);
			fillSingleLineAppData(holder.middle_parent_layout_bottom, holder.middle_icon_bottom, holder.middle_app_name_bottom, holder.middle_downloadNum_bottom,
					holder.middle_install_bottom, holder.middle_corner_icon_bottom, appInfoBtos.get(4),
                    true, false, assemblyId);
			fillSingleLineAppData(holder.right_parent_layout_bottom, holder.right_icon_bottom, holder.right_app_name_bottom, holder.right_downloadNum_bottom,
					holder.right_install_bottom, holder.right_corner_icon_bottom, appInfoBtos.get(5),
                    true, false, assemblyId);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		
	}
	
	
	protected void fillSingleLineAppData(RelativeLayout rlParent, ImageView icon, TextView appName, TextView downloadNum,
			TextView installBtn, ImageView cornerIcon, final AppInfoBto appInfo,
			boolean mergeChildId, boolean isTopicChild, int childId) {
		String imageUrl = appInfo.getImgUrl(); 
		CornerIconInfoBto cornerIconInfo = appInfo.getCornerMarkInfo();
		
		appName.setText(appInfo.getName());
		
		String downloadNumStr = getDownloadNumStr(appInfo.getDownTimes());
		downloadNum.setText(downloadNumStr);
		
		initInstallBtn(installBtn,appInfo,icon, mergeChildId, isTopicChild, childId);
		installBtn.setTag(R.id.water_flow_tag, true);
		rlParent.setTag(R.id.water_flow_tag, true);
		rlParent.setOnClickListener(new AppItemOnClickListener(appInfo, mergeChildId, isTopicChild, childId));
		
//		//动态添加官方标识到 应用名后
//		if(appInfo.getOfficialLogo() == OFFICAL_VISIBLE) {
//			officialIcon.setVisibility(View.VISIBLE);
//			officialIcon.setImageResource(R.drawable.official);
//		} else {
//			officialIcon.setVisibility(View.GONE);
//			officialIcon.setImageResource(0);
//		}
		
		
		if(cornerIconInfo!=null && cornerIconInfo.getType() > 0) {
				cornerIcon.setVisibility(View.VISIBLE);
			AsyncImageCache.from(mContext).displayImage(cornerIcon, cornerIconInfo, 0);
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
	
	
	
	private class ViewCache {
		TextView title;
		ImageView topImg;
		TextView lookAll;
		
		LinearLayout bottom_layout;
		
		RelativeLayout left_parent_layout_top;
		ImageView 	left_icon_top;        
		TextView 	left_app_name_top;  
		TextView	left_install_top;
		ImageView   left_corner_icon_top;
		TextView left_downloadNum_top;

		RelativeLayout middle_parent_layout_top;
		ImageView 	middle_icon_top;        
		TextView 	middle_app_name_top;  
		TextView	middle_install_top;	
		ImageView   middle_corner_icon_top;
		TextView middle_downloadNum_top;

		RelativeLayout right_parent_layout_top;
		ImageView 	right_icon_top;        
		TextView 	right_app_name_top;  
		TextView	right_install_top;
		ImageView   right_corner_icon_top;
		TextView right_downloadNum_top;

		RelativeLayout left_parent_layout_bottom;
		ImageView 	left_icon_bottom;        
		TextView 	left_app_name_bottom;  
		TextView	left_install_bottom;
		ImageView   left_corner_icon_bottom;
		TextView left_downloadNum_bottom;
		
		RelativeLayout middle_parent_layout_bottom;
		ImageView 	middle_icon_bottom;        
		TextView 	middle_app_name_bottom;  
		TextView	middle_install_bottom;	
		ImageView   middle_corner_icon_bottom;
		TextView middle_downloadNum_bottom;
		
		RelativeLayout right_parent_layout_bottom;
		ImageView 	right_icon_bottom;        
		TextView 	right_app_name_bottom;  
		TextView	right_install_bottom;
		ImageView   right_corner_icon_bottom;
		TextView right_downloadNum_bottom;
	}
	
}
