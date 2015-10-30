package com.zhuoyi.market.adapter;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;

/**
 * 单行item布局列表Adapter
 * @author JLu
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SingleLineItemAdapter extends CommonListAdapter<AppInfoBto> {
	
	private boolean mIsShowIndex = false;
	private boolean mIsShowRankChange = false;

	
	/**
	 * 
	 * @param context
	 * @param list
	 * @param callBack
	 * @param layoutId  item的布局Id
	 */
	public SingleLineItemAdapter(Context context, List<AppInfoBto> list, DownloadCallBackInterface callBack,int layoutId) {
		super(context, layoutId,list,callBack);
	}
	
	public SingleLineItemAdapter(Context context, DownloadCallBackInterface callBack,int layoutId) {
		super(context, layoutId,callBack);
	}
	
	public void setShowIndexParam(boolean isShow) {
	    mIsShowIndex = isShow;
	}
	
	public void freeImageCache() {
	    /*mContext = null;
	    mList.clear();
	    mList = null;
	    mDownloadCallBack = null;*/
		
/*	    if(mAsyncImageCache!=null)
	        mAsyncImageCache.stop();*/
		if (mDatas != null) {
			mDatas.clear();
			mDatas = null;
		}
		if (mContext != null)
			mContext = null;
	}
	
	
	public void setShowRankChange(boolean show) {
		mIsShowRankChange = show;
	}
	
	
	@Override
	public void convert(ViewHolder holder, final AppInfoBto appInfo, int position) {
		ImageView icon = holder.getView(R.id.app_icon_img);
		TextView appName = holder.getView(R.id.app_name_txt);
		TextView appRankChange = holder.getView(R.id.app_change_in_rank);
		TextView appSize = holder.getView(R.id.app_size_text);
		TextView downloadNum = holder.getView(R.id.download_times_txt);
		TextView description = holder.getView(R.id.app_desc);
		TextView appRanking = holder.getView(R.id.text_sort);
		TextView installBtn = holder.getView(R.id.state_app_btn);
		ImageView cornerIcon = holder.getView(R.id.corner_icon);
		RatingBar appRatingStar = holder.getView(R.id.app_ratingview);
		RelativeLayout rlParent = holder.getView(R.id.rlParent);
		ImageView officialIcon = holder.getView(R.id.official_icon);
		
		if(mIsShowIndex) {
			appRanking.setVisibility(View.VISIBLE);
			appRanking.setText(""+(position+1));
			if (position == 0) {
			    appRanking.setBackgroundResource(R.drawable.rank_top_1);
            } else if (position == 1) {
                appRanking.setBackgroundResource(R.drawable.rank_top_2);
            } else if (position == 2) {
                appRanking.setBackgroundResource(R.drawable.rank_top_3);
            } else if (position < 99) {
                appRanking.setBackgroundResource(R.drawable.rank_top_other);
            } else {
                appRanking.setVisibility(View.GONE);
            }
			
		} else {
			appRanking.setVisibility(View.GONE);
		}

		if(mIsShowRankChange) {
			//appRankChange.setVisibility(View.VISIBLE);
			appName.setMaxWidth(mContext.getResources().getDimensionPixelOffset(R.dimen.fastrising_rank_change_appname_max_width));
			setRankChange(appInfo.getRiseVal(), position, appRankChange);
		} else {
			appRankChange.setVisibility(View.GONE);
		}
		
		fillSingleLineAppData(rlParent, icon, appName, appSize, downloadNum, 
				description,installBtn, cornerIcon, appRatingStar,officialIcon, appInfo, false, false, 0);
		
	}

	
    /**
     * 飙升中排名变化
     * 20151021:只显示上升的
     * @param position
     * @param rankChange
     */
    private void setRankChange(int riseVal, int position, TextView rankChange) {
    	Drawable drawable = null;
    	Resources resources = mContext.getResources();
//    	if(riseVal < 0) {
//    		riseVal = -riseVal;
//    		drawable = resources.getDrawable(R.drawable.rank_change_down);
//    		rankChange.setTextColor(resources.getColor(R.color.fastrising_rankdown_text));
//    	} else if(riseVal == 0)
//    		drawable = resources.getDrawable(R.drawable.rank_change_equal);
//    	else {
//    		drawable = resources.getDrawable(R.drawable.rank_change_up);
//    		rankChange.setTextColor(resources.getColor(R.color.fastrising_rankup_text));
//    	}
//    	
//    	rankChange.setText(riseVal == 0 ? "" : riseVal + "");
//    	drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//    	rankChange.setCompoundDrawables(drawable, null, null, null);
//    	drawable = null;
    	
    	if (riseVal > 0) {
    		rankChange.setVisibility(View.VISIBLE);
    		drawable = resources.getDrawable(R.drawable.rank_change_up);
    		rankChange.setTextColor(resources.getColor(R.color.fastrising_rankup_text));
    		rankChange.setText(""+riseVal);
        	drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        	rankChange.setCompoundDrawables(drawable, null, null, null);
        	drawable = null;
    	} else {
    		rankChange.setVisibility(View.GONE);
    	}
    }



}
