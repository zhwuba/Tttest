package com.zhuoyi.market.necessary;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;

public class NecessaryFirstInAdapter extends BaseAdapter{

	private List<List<AppInfoBto>> mList;

	private boolean mIsRefrashing = false;
	
	private int mStartEffectPos = 0;
	private AlphaAnimation alpha;
	private ScaleAnimation scale;
	private LayoutAnimationController mLayoutAnimController;
	private boolean mRefreshIcon = true;
	private AsyncImageCache mAsyncImageCache;
	private LayoutInflater inflater;
	private String mReportFlag = "";
	
	private ItemClickCallBack mCallBack;
	
	public NecessaryFirstInAdapter(Context context, List<List<AppInfoBto>> list,ItemClickCallBack callBack)
	{
		if (list == null) {
			list = new ArrayList<List<AppInfoBto>>();
		}else {
			mList = list;
		}
		
		mCallBack = callBack;
			
		mStartEffectPos = 0;
		alpha =new AlphaAnimation(0, 1); 
		alpha.setDuration(800); 
		
		scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setDuration(800); 
		
		mLayoutAnimController =new LayoutAnimationController(alpha); 
		mLayoutAnimController.setDelay(0.5f);
		mLayoutAnimController.setOrder(LayoutAnimationController.ORDER_RANDOM); 
		
		mAsyncImageCache = AsyncImageCache.from(context);
		inflater = LayoutInflater.from(context);
	}
	
	
	public void setReportFlag(String flag) {
	    mReportFlag = flag;
	}

	
	public void setMyList(List<List<AppInfoBto>> list) {
		mList = list;
	}
	
	@Override
	public int getCount() {
		return mList.size();
	}
 
	@Override
	public Object getItem(int position) {
		try {
			return mList==null?null:mList.get(position);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void allowRefreshIcon(boolean status) {
		mRefreshIcon = status;
	}

	public boolean isAllowRefreshIcon() {
		return mRefreshIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		//View view;
		String imageUrl = "";
		int listindex = 0;
		final ViewCache holder ;
		
		if(mIsRefrashing)
			return null;
		else
			mIsRefrashing = true;	 
		try {
			if (convertView == null) { 
				convertView = inflater.inflate(R.layout.installed_necessary_list, parent,false);
				holder = new ViewCache(); 			
				holder.left_relativelayout = (RelativeLayout)convertView.findViewById(R.id.left_parent_layout);
				holder.left_icon = (ImageView)holder.left_relativelayout.findViewById(R.id.list_icon);				
				holder.left_app_name = (TextView)holder.left_relativelayout.findViewById(R.id.list_app_name);
				holder.left_app_size = (TextView)holder.left_relativelayout.findViewById(R.id.list_app_install_count);
				holder.left_select = (ImageView)holder.left_relativelayout.findViewById(R.id.text_select);
				holder.left_install = (TextView)holder.left_relativelayout.findViewById(R.id.install_button);
				holder.left_install.setVisibility(View.GONE);
				holder.left_select.setVisibility(View.VISIBLE);
				
				holder.middle_relativelayout = (RelativeLayout)convertView.findViewById(R.id.middle_parent_layout);
				holder.middle_icon = (ImageView)holder.middle_relativelayout.findViewById(R.id.list_icon);				
				holder.middle_app_name = (TextView)holder.middle_relativelayout.findViewById(R.id.list_app_name);
				holder.middle_app_size = (TextView)holder.middle_relativelayout.findViewById(R.id.list_app_install_count);
				holder.middle_select = (ImageView)holder.middle_relativelayout.findViewById(R.id.text_select);
				holder.middle_install = (TextView)holder.middle_relativelayout.findViewById(R.id.install_button);
				holder.middle_install.setVisibility(View.GONE);
				holder.middle_select.setVisibility(View.VISIBLE);
				
				holder.right_relativelayout = (RelativeLayout)convertView.findViewById(R.id.right_parent_layout);
				holder.right_icon = (ImageView)holder.right_relativelayout.findViewById(R.id.list_icon);				
				holder.right_app_name = (TextView)holder.right_relativelayout.findViewById(R.id.list_app_name);
				holder.right_app_size = (TextView)holder.right_relativelayout.findViewById(R.id.list_app_install_count);
				holder.right_select = (ImageView)holder.right_relativelayout.findViewById(R.id.text_select);
				holder.right_install = (TextView)holder.right_relativelayout.findViewById(R.id.install_button);
				holder.right_install.setVisibility(View.GONE);
				holder.right_select.setVisibility(View.VISIBLE);
				convertView.setTag(holder);
			} else {
				holder = (ViewCache)convertView.getTag();
				holder.right_relativelayout.setVisibility(View.VISIBLE);
			}

			List<AppInfoBto> map= (List<AppInfoBto>) getItem(position);
			
			for(AppInfoBto appInfo: map) {
				final int defaultImageId = R.drawable.picture_bg1_big;
				if(appInfo!=null) {
					imageUrl = appInfo.getImgUrl();
					if (listindex % 3 == 0) {
						holder.left_relativelayout.setVisibility(View.VISIBLE);
						mAsyncImageCache.displayImage(mRefreshIcon, holder.left_icon, defaultImageId, new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(), imageUrl), true);
						holder.left_app_name.setText(appInfo.getName());
						holder.left_app_size.setText(appInfo.getFileSizeString());

						if (NecessaryDialogActivity.isSelect[(position * 3)]) {
							holder.left_select.setImageResource(R.drawable.onekey_install_select);
						} else {
							holder.left_select.setImageResource(R.drawable.onekey_install_unselect);
						}
					} else if(listindex % 3 == 1) {
						holder.middle_relativelayout.setVisibility(View.VISIBLE);
						mAsyncImageCache.displayImage(mRefreshIcon, holder.middle_icon, defaultImageId, new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(), imageUrl), true);
						holder.middle_app_name.setText(appInfo.getName());
						holder.middle_app_size.setText(appInfo.getFileSizeString());
						
						if (NecessaryDialogActivity.isSelect[(position * 3 + 1)]) {
							holder.middle_select.setImageResource(R.drawable.onekey_install_select);
						} else {
							holder.middle_select.setImageResource(R.drawable.onekey_install_unselect);
						}
					} else {
						holder.right_relativelayout.setVisibility(View.VISIBLE);
						mAsyncImageCache.displayImage(mRefreshIcon, holder.right_icon, defaultImageId, new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(), imageUrl), true);
						holder.right_app_name.setText(appInfo.getName());
						holder.right_app_size.setText(appInfo.getFileSizeString());

						if (NecessaryDialogActivity.isSelect[(position * 3 + 2)]) {
							holder.right_select.setImageResource(R.drawable.onekey_install_select);
						} else {
							holder.right_select.setImageResource(R.drawable.onekey_install_unselect);
						}
					}
					listindex++;
				}
			}
				
			if(listindex % 3 == 1) {
				holder.middle_relativelayout.setVisibility(View.INVISIBLE);
				holder.right_relativelayout.setVisibility(View.INVISIBLE);
			} else if(listindex % 3 == 2) {
				holder.right_relativelayout.setVisibility(View.INVISIBLE);
			}

			holder.left_relativelayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mCallBack.leftIetmClick(position, (ImageView)v.findViewById(R.id.text_select));
				}
			});
			
			holder.middle_relativelayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mCallBack.middleIetmClick(position, (ImageView)v.findViewById(R.id.text_select));
				}
			});
			
			holder.right_relativelayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mCallBack.rightIetmClick(position, (ImageView)v.findViewById(R.id.text_select));
				}
			});

			if (position > mStartEffectPos) {
				mStartEffectPos = position;
			}

			mIsRefrashing = false;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			mIsRefrashing = false;
			e.printStackTrace();
		}
		
		return convertView;
	}

	static class ViewCache {
		RelativeLayout left_relativelayout;
		ImageView 	left_icon;        
		TextView 	left_app_name;  
		TextView 	left_app_size;
		ImageView	left_select;
		TextView    left_install;
		
		RelativeLayout middle_relativelayout;
		ImageView 	middle_icon;        
		TextView 	middle_app_name;  
		TextView 	middle_app_size;
		ImageView	middle_select;
		TextView    middle_install;

		RelativeLayout right_relativelayout;
		ImageView 	right_icon;        
		TextView 	right_app_name;  
		TextView 	right_app_size;
		ImageView	right_select;
		TextView    right_install;
	}
	
	public void freeImageCache() {
        if (mList != null) {
            mList.clear();
        }
	    if(mAsyncImageCache!=null) {
            mAsyncImageCache.stop();
            mAsyncImageCache.releaseRes();
	    }
    }
	
	public interface ItemClickCallBack {
		
	    public void leftIetmClick(int position,ImageView select);
	    
	    public void middleIetmClick(int position,ImageView select);
	    
	    public void rightIetmClick(int position,ImageView select);
	}


}


