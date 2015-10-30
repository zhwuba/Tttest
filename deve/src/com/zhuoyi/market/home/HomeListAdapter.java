package com.zhuoyi.market.home;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.market.net.data.DiscoverAppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.adapter.CommonListAdapter;
import com.zhuoyi.market.adapter.ViewHolder;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.discovery.DiscoverAdapter.TopicHolder;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 首页精选列表Adapter
 * @author JLu
 *
 */
public class HomeListAdapter extends CommonListAdapter<HomeListBean> {

	private Context mContext;

	private LayoutInflater inflater;
	

	/*对应值分别为 00 四个topicButton, 01 单行三列样式, 02 专题带三个应用,  03榜单带三列应用 ,  04 榜单带三行应用,  05 单行应用, 06 专题图片 */
	public static final int ITEM_VIEW_TYPE_00 = 0,ITEM_VIEW_TYPE_01 = 1,ITEM_VIEW_TYPE_02 = 2,
			ITEM_VIEW_TYPE_03 = 3,ITEM_VIEW_TYPE_04 = 4,ITEM_VIEW_TYPE_05 = 5,ITEM_VIEW_TYPE_06 = 6 ;
	
	
	public HomeListAdapter(Context context, DownloadCallBackInterface callBack) {
		super(context, callBack);
		
		mContext = context;
		
		inflater = LayoutInflater.from(context);
		
	}
	

	@Override
	public int getItemViewType(int position) {
		try {
			return getItem(position).getItemType();
		} catch(NullPointerException e) {
			return -1;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return 7;
	}
	
	
	public void freeImageCache() {
	    //mContext = null;
	    
	    if (mDatas != null)
	        mDatas.clear();
	    
	    mDatas = null;
	    
	    mExisted = "end";
/*	    if(mAsyncImageCache!=null)
	        mAsyncImageCache.stop();
	    if(mList!=null)
	        mList.clear();*/
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewCache holder ;
		HomeListBean bean ;
		try { 
			switch(getItemViewType(position)) {
			case ITEM_VIEW_TYPE_01:
				if(convertView==null) {  
					convertView = inflater.inflate(R.layout.home_list_item_type01, parent,false);
					holder = new ViewCache(); 
					
					holder.left_parent_layout = (RelativeLayout) convertView.findViewById(R.id.left_parent_layout);
					holder.left_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.list_icon);				
					holder.left_app_name = (TextView) holder.left_parent_layout.findViewById(R.id.list_app_name);
					holder.left_app_size = (TextView) holder.left_parent_layout.findViewById(R.id.list_app_install_count);
					holder.left_install = (TextView) holder.left_parent_layout.findViewById(R.id.install_button);
					holder.left_corner_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.corner_icon);

					holder.middle_parent_layout = (RelativeLayout) convertView.findViewById(R.id.middle_parent_layout);
					holder.middle_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.list_icon);				
					holder.middle_app_name = (TextView) holder.middle_parent_layout.findViewById(R.id.list_app_name);
					holder.middle_app_size = (TextView) holder.middle_parent_layout.findViewById(R.id.list_app_install_count);
					holder.middle_install = (TextView) holder.middle_parent_layout.findViewById(R.id.install_button);
					holder.middle_corner_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.corner_icon);

					holder.right_parent_layout = (RelativeLayout) convertView.findViewById(R.id.right_parent_layout);
					holder.right_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.list_icon);				
					holder.right_app_name = (TextView) holder.right_parent_layout.findViewById(R.id.list_app_name);
					holder.right_app_size = (TextView) holder.right_parent_layout.findViewById(R.id.list_app_install_count);
					holder.right_install = (TextView) holder.right_parent_layout.findViewById(R.id.install_button);
					holder.right_corner_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.corner_icon);
					holder.divider = (View) convertView.findViewById(R.id.divider);
					convertView.setTag(holder);
				} else {
					holder = (ViewCache)convertView.getTag(); 
				}

			    bean = getItem(position);
				fillVericalAppData(holder.left_parent_layout,holder.left_icon,holder.left_app_name,holder.left_app_size,holder.left_install,holder.left_corner_icon,bean.getAppInfo01(),bean.getAssemblyId());
				fillVericalAppData(holder.middle_parent_layout,holder.middle_icon,holder.middle_app_name,holder.middle_app_size,holder.middle_install,holder.middle_corner_icon,bean.getAppInfo02(),bean.getAssemblyId());
				fillVericalAppData(holder.right_parent_layout,holder.right_icon,holder.right_app_name,holder.right_app_size,holder.right_install,holder.right_corner_icon,bean.getAppInfo03(),bean.getAssemblyId());
				int currentPosition = position;
				if (++currentPosition <getCount()) {
                    if (getItemViewType(currentPosition) == ITEM_VIEW_TYPE_01) {
                        holder.divider.setVisibility(View.GONE);
                    }
                }
				break;
			case ITEM_VIEW_TYPE_02: //专题--同发现专题
				if(convertView==null) {  
					convertView = inflater.inflate(R.layout.home_list_item_type02, parent,false);
					holder = new ViewCache(); 		
					holder.title = (TextView) convertView.findViewById(R.id.title);
					holder.topicImg = (ImageView) convertView.findViewById(R.id.topic_img);
					holder.bottom = (RelativeLayout) convertView.findViewById(R.id.bottom);
					
					holder.left_icon = (ImageView) convertView.findViewById(R.id.left_topic);				
					holder.middle_icon = (ImageView) convertView.findViewById(R.id.middle_topic);				
					holder.right_icon = (ImageView) convertView.findViewById(R.id.right_topic);				
					convertView.setTag(holder);
				} else {
					holder = (ViewCache)convertView.getTag(); 
				}
				
				int imgWidth = mContext.getResources().getDimensionPixelSize(R.dimen.discover_item_width);
				int imgHeight = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_big_height);
				int mTopicSmallSize = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_small_size);
				
				bean = getItem(position);
				if (TextUtils.isEmpty(bean.getTitleName())) {
				    holder.title.setVisibility(View.GONE);
                } else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(bean.getTitleName());
                }
				final AppInfoBto appInfo = bean.getAppInfo00();
				int defaultPic = -1;
		        if(MarketUtils.isNoPicModelReally()) {
		            defaultPic = R.drawable.logo_no_network;
		        } else {
		            defaultPic = R.drawable.logo_no_network_withtxt;
		        }
				AsyncImageCache.from(mContext).displayImage(mRefreshIcon,false,holder.topicImg, defaultPic,imgWidth,imgHeight,
						new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(bean.getTopicImgUrl()),bean.getTopicImgUrl()), false,true,false,"");
				
				showSmailTopic(holder.left_icon, bean.getAppInfo01(), mTopicSmallSize);
				showSmailTopic(holder.middle_icon, bean.getAppInfo02(), mTopicSmallSize);
				showSmailTopic(holder.right_icon, bean.getAppInfo03(), mTopicSmallSize);
				
				convertView.setOnClickListener(new TopicItemOnClickListener(appInfo,false));
				break;
			case ITEM_VIEW_TYPE_03:
				//榜单带三列
				if(convertView==null) {  
					convertView = inflater.inflate(R.layout.home_list_item_type03, parent,false);
					holder = new ViewCache(); 			
					holder.title = (TextView) convertView.findViewById(R.id.item_title);
					
					holder.left_parent_layout = (RelativeLayout) convertView.findViewById(R.id.left_parent_layout);
					holder.left_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.list_icon);				
					holder.left_app_name = (TextView) holder.left_parent_layout.findViewById(R.id.list_app_name);
					holder.left_app_size = (TextView) holder.left_parent_layout.findViewById(R.id.list_app_install_count);
					holder.left_install = (TextView) holder.left_parent_layout.findViewById(R.id.install_button);
					holder.left_corner_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.corner_icon);

					holder.middle_parent_layout = (RelativeLayout) convertView.findViewById(R.id.middle_parent_layout);
					holder.middle_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.list_icon);				
					holder.middle_app_name = (TextView) holder.middle_parent_layout.findViewById(R.id.list_app_name);
					holder.middle_app_size = (TextView) holder.middle_parent_layout.findViewById(R.id.list_app_install_count);
					holder.middle_install = (TextView) holder.middle_parent_layout.findViewById(R.id.install_button);
					holder.middle_corner_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.corner_icon);

					holder.right_parent_layout = (RelativeLayout) convertView.findViewById(R.id.right_parent_layout);
					holder.right_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.list_icon);				
					holder.right_app_name = (TextView) holder.right_parent_layout.findViewById(R.id.list_app_name);
					holder.right_app_size = (TextView) holder.right_parent_layout.findViewById(R.id.list_app_install_count);
					holder.right_install = (TextView) holder.right_parent_layout.findViewById(R.id.install_button);
					holder.right_corner_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.corner_icon);
					convertView.setTag(holder);
				} else {
					holder = (ViewCache)convertView.getTag(); 
				}
				
				bean = getItem(position);
				if (TextUtils.isEmpty(bean.getTitleName())) {
                    holder.title.setVisibility(View.GONE);
                } else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(bean.getTitleName());
                }
				
				fillVericalAppData(holder.left_parent_layout,holder.left_icon,holder.left_app_name,holder.left_app_size,holder.left_install,holder.left_corner_icon,bean.getAppInfo01(),bean.getAssemblyId());
				fillVericalAppData(holder.middle_parent_layout,holder.middle_icon,holder.middle_app_name,holder.middle_app_size,holder.middle_install,holder.middle_corner_icon,bean.getAppInfo02(),bean.getAssemblyId());
				fillVericalAppData(holder.right_parent_layout,holder.right_icon,holder.right_app_name,holder.right_app_size,holder.right_install,holder.right_corner_icon,bean.getAppInfo03(),bean.getAssemblyId());

				break;
			case ITEM_VIEW_TYPE_04:
				//榜单带三行应用
				if(convertView==null) {  
					convertView = inflater.inflate(R.layout.home_list_item_type04, parent,false);
					holder = new ViewCache(); 			
					holder.title = (TextView) convertView.findViewById(R.id.item_title);
					
					holder.left_parent_layout = (RelativeLayout) convertView.findViewById(R.id.left_parent_layout);
					holder.left_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.app_icon_img);				
					holder.left_app_name = (TextView) holder.left_parent_layout.findViewById(R.id.app_name_txt);
					holder.left_app_size = (TextView) holder.left_parent_layout.findViewById(R.id.app_size_text);
					holder.left_downloadNum = (TextView) holder.left_parent_layout.findViewById(R.id.download_times_txt);
					holder.left_description = (TextView) holder.left_parent_layout.findViewById(R.id.app_desc);
					holder.left_install = (TextView) holder.left_parent_layout.findViewById(R.id.state_app_btn);
					holder.left_corner_icon = (ImageView) holder.left_parent_layout.findViewById(R.id.corner_icon);
					holder.left_appRatingStar = (RatingBar) holder.left_parent_layout.findViewById(R.id.app_ratingview);
					holder.left_officialIcon = (ImageView) holder.left_parent_layout.findViewById(R.id.official_icon);

					holder.middle_parent_layout = (RelativeLayout) convertView.findViewById(R.id.middle_parent_layout);
					holder.middle_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.app_icon_img);				
					holder.middle_app_name = (TextView) holder.middle_parent_layout.findViewById(R.id.app_name_txt);
					holder.middle_app_size = (TextView) holder.middle_parent_layout.findViewById(R.id.app_size_text);
					holder.middle_downloadNum = (TextView) holder.middle_parent_layout.findViewById(R.id.download_times_txt);
					holder.middle_description = (TextView) holder.middle_parent_layout.findViewById(R.id.app_desc);
					holder.middle_install = (TextView) holder.middle_parent_layout.findViewById(R.id.state_app_btn);
					holder.middle_corner_icon = (ImageView) holder.middle_parent_layout.findViewById(R.id.corner_icon);
					holder.middle_appRatingStar = (RatingBar) holder.middle_parent_layout.findViewById(R.id.app_ratingview);
					holder.middle_officialIcon = (ImageView) holder.middle_parent_layout.findViewById(R.id.official_icon);

					holder.right_parent_layout = (RelativeLayout) convertView.findViewById(R.id.right_parent_layout);
					holder.right_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.app_icon_img);				
					holder.right_app_name = (TextView) holder.right_parent_layout.findViewById(R.id.app_name_txt);
					holder.right_app_size = (TextView) holder.right_parent_layout.findViewById(R.id.app_size_text);
					holder.right_downloadNum = (TextView) holder.right_parent_layout.findViewById(R.id.download_times_txt);
					holder.right_description = (TextView) holder.right_parent_layout.findViewById(R.id.app_desc);
					holder.right_install = (TextView) holder.right_parent_layout.findViewById(R.id.state_app_btn);
					holder.right_corner_icon = (ImageView) holder.right_parent_layout.findViewById(R.id.corner_icon);
					holder.right_appRatingStar = (RatingBar) holder.right_parent_layout.findViewById(R.id.app_ratingview);
					holder.right_officialIcon = (ImageView) holder.right_parent_layout.findViewById(R.id.official_icon);
					
					convertView.setTag(holder);
				} else {
					holder = (ViewCache)convertView.getTag(); 
				}
				
				bean = getItem(position);
				if (TextUtils.isEmpty(bean.getTitleName())) {
                    holder.title.setVisibility(View.GONE);
                } else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(bean.getTitleName());
                }
				
				fillSingleLineAppData(holder.left_parent_layout, holder.left_icon, holder.left_app_name, holder.left_app_size, holder.left_downloadNum, 
						holder.left_description,holder.left_install, holder.left_corner_icon, holder.left_appRatingStar,holder.left_officialIcon,bean.getAppInfo01(),
						true, false, bean.getAssemblyId());
				fillSingleLineAppData(holder.middle_parent_layout, holder.middle_icon, holder.middle_app_name, holder.middle_app_size, holder.middle_downloadNum, 
						holder.middle_description,holder.middle_install, holder.middle_corner_icon, holder.middle_appRatingStar,holder.middle_officialIcon,bean.getAppInfo02(),
						true, false, bean.getAssemblyId());
				fillSingleLineAppData(holder.right_parent_layout, holder.right_icon, holder.right_app_name, holder.right_app_size, holder.right_downloadNum, 
						holder.right_description,holder.right_install, holder.right_corner_icon, holder.right_appRatingStar,holder.right_officialIcon,bean.getAppInfo03(),
						true, false, bean.getAssemblyId());
				
				break;
			case ITEM_VIEW_TYPE_05:
				//单行列表	
				ViewHolder commonHolder = ViewHolder.get(mContext, convertView, R.layout.home_single_line_item, parent, position);
				bean = getItem(position);
				final AppInfoBto appInfo01 = bean.getAppInfo01();
				
				ImageView icon = commonHolder.getView(R.id.app_icon_img);
				TextView appName = commonHolder.getView(R.id.app_name_txt);
				TextView appSize = commonHolder.getView(R.id.app_size_text);
				TextView downloadNum = commonHolder.getView(R.id.download_times_txt);
				TextView description = commonHolder.getView(R.id.app_desc);
				TextView installBtn = commonHolder.getView(R.id.state_app_btn);
				ImageView cornerIcon = commonHolder.getView(R.id.corner_icon);
				RatingBar appRatingStar = commonHolder.getView(R.id.app_ratingview);
				RelativeLayout rlParent = commonHolder.getView(R.id.rlParent);
				ImageView officialIcon = commonHolder.getView(R.id.official_icon);
				View divider = commonHolder.getView(R.id.divider);
				
				fillSingleLineAppData(rlParent, icon, appName, appSize, downloadNum, description,
						installBtn, cornerIcon, appRatingStar,officialIcon,appInfo01,
						true, false, bean.getAssemblyId());
				
				convertView = commonHolder.getConvertView();
				/*if(getItemViewType(position+1) != 5) {
					divider.setVisibility(View.VISIBLE);
				} else {
					divider.setVisibility(View.GONE);
				}*/
				break;
			case ITEM_VIEW_TYPE_06:
				//专题大图
				if(convertView==null) {  
					convertView = inflater.inflate(R.layout.home_list_item_type06, parent,false);
					holder = new ViewCache(); 			
					holder.topicImg = (ImageView) convertView.findViewById(R.id.topic_img);
					convertView.setTag(holder);
				} else {
					holder = (ViewCache)convertView.getTag(); 
				}
				
				int imgWidth01 = mContext.getResources().getDimensionPixelSize(R.dimen.discover_item_width);
				int imgHeight01 = mContext.getResources().getDimensionPixelSize(R.dimen.discover_topic_big_height);
				int picId = -1;
                if(MarketUtils.isNoPicModelReally()) {
                    picId = R.drawable.logo_no_network;
                } else {
                    picId = R.drawable.logo_no_network_withtxt;
                }
				bean = getItem(position);
				final AppInfoBto appInfo02 = bean.getAppInfo00();
				AsyncImageCache.from(mContext).displayImage(mRefreshIcon,false,holder.topicImg, picId,imgWidth01,imgHeight01,
						new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(bean.getTopicImgUrl()),bean.getTopicImgUrl()), false,true,false,"");
				holder.topicImg.setOnClickListener(new TopicItemOnClickListener(appInfo02,true));
				
			}

		} catch(OutOfMemoryError e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return convertView;
	}
	
	
	private void fillVericalAppData(RelativeLayout parentLayout,ImageView icon,TextView app_name,  TextView app_size,
			TextView install,ImageView corner_icon,AppInfoBto appInfo,int assemblyId) {
		
		if(appInfo == null) {
			parentLayout.setVisibility(View.INVISIBLE);
			return;
		} else {
			parentLayout.setVisibility(View.VISIBLE);
		}
		
		String imageUrl = appInfo.getImgUrl();
		CornerIconInfoBto cornerIcon = appInfo.getCornerMarkInfo();

		if(cornerIcon.getType() > 0) {
			corner_icon.setVisibility(View.VISIBLE);
			AsyncImageCache.from(mContext).displayImage(corner_icon, cornerIcon,0);
		} else {
			corner_icon.setVisibility(View.GONE);
		}
		AsyncImageCache.from(mContext).displayImage(mRefreshIcon,icon, R.drawable.picture_bg1_big,new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(),imageUrl), true);
		app_name.setText(appInfo.getName());			
		app_size.setText(appInfo.getFileSizeString());

		initInstallBtn(install,appInfo,icon, true, false, assemblyId);
		parentLayout.setOnClickListener(new AppItemOnClickListener(appInfo, true, false, assemblyId));
	
	}
	
	
	private class TopicItemOnClickListener implements OnClickListener {
		private AppInfoBto appInfo;
		private boolean isPassInstall;
		
		public TopicItemOnClickListener(AppInfoBto appInfo,boolean isPassInstall) {
			this.appInfo = appInfo;
			this.isPassInstall = isPassInstall;
		}

		@Override
		public void onClick(View v) {
			MarketUtils.startActivityFromStartUpAd(mContext,appInfo,isPassInstall);
		}
		
	}
	
	
	public byte[] createBitByteArray(Bitmap bitmap) {
		if (null == bitmap)
			return null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		return os.toByteArray();
	}
	
	private void showSmailTopic(ImageView icon,AppInfoBto appInfo,int imageSize) {
        if (appInfo == null) {
            icon.setVisibility(View.GONE);
            return;
        }
        int defaultPic = R.drawable.topic_small_default;
        icon.setVisibility(View.VISIBLE);
        AsyncImageCache.from(mContext).displayImage(mRefreshIcon, false, icon, defaultPic, imageSize, imageSize,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(appInfo.getPackageName()), appInfo.getImgUrl()), false, true, false, "");
    }
	
	private class ViewCache {
		TextView title;
		ImageView topicImg;
		View divider;
		TextView lookAll;
		
		RelativeLayout left_parent_layout;
		ImageView 	left_icon;        
		TextView 	left_app_name;  
		TextView 	left_app_size;
		TextView	left_install;
		ImageView   left_corner_icon;
		TextView left_downloadNum;
		TextView left_description;
		RatingBar left_appRatingStar;
		ImageView left_officialIcon;

		RelativeLayout middle_parent_layout;
		ImageView 	middle_icon;        
		TextView 	middle_app_name;  
		TextView 	middle_app_size;
		TextView	middle_install;	
		ImageView   middle_corner_icon;
		TextView middle_downloadNum;
		TextView middle_description;
		RatingBar middle_appRatingStar;
		ImageView middle_officialIcon;

		RelativeLayout right_parent_layout;
		ImageView 	right_icon;        
		TextView 	right_app_name;  
		TextView 	right_app_size;
		TextView	right_install;
		ImageView   right_corner_icon;
		TextView right_downloadNum;
		TextView right_description;
		RatingBar right_appRatingStar;
		ImageView right_officialIcon;
		
		RelativeLayout bottom;
		
	}


	@Override
	public void convert(ViewHolder holder, HomeListBean bean, int position) {
		// TODO Auto-generated method stub
		
	}
	

}
