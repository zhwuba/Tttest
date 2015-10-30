package com.zhuoyi.market.appManage.download;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.market.net.data.AppInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class DownloadRecommendAdapter extends BaseAdapter {

	private Context mContext;
	private List<AppInfoBto> mList;
	private AsyncImageCache mAsyncImageCache = null;
	private String mCountM= "";
	private String mDownloadStr = "";
	public DownloadRecommendAdapter(Context context, List<AppInfoBto> list) {
		this.mContext = context;
		this.mList = list;
		mCountM = mContext.getResources().getString(R.string.ten_thousand);
		mDownloadStr = mContext.getResources().getString(R.string.download_str);
		mAsyncImageCache = AsyncImageCache.from(context);
	}

	public void setMyList(List<AppInfoBto> list) {
		this.mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList==null?null:mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewCache holder;
		String packageName = "";
		String imageUrl = "";
		StringBuilder count_string = new StringBuilder();
		int count = 0;
		final int curPosition = position;

		if(convertView==null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.download_recommend_item_view, parent,false); 
			holder = new ViewCache();
			holder.app_icon = (ImageView)convertView.findViewById(R.id.app_image);
			holder.app_name = (TextView)convertView.findViewById(R.id.app_name);
			holder.app_num = (TextView)convertView.findViewById(R.id.app_num);
			convertView.setTag(holder); 
		} else {
			holder = (ViewCache)convertView.getTag();
		}
		
		AppInfoBto appInfoBto= mList.get(position);
		packageName = appInfoBto.getPackageName();
		imageUrl = appInfoBto.getImgUrl(); 	
		holder.app_name.setText(appInfoBto.getName());
		
        count = appInfoBto.getDownTimes();
        if (count < 100000) {
            if (count < 100) {
                count = 100;
            }
            count_string.append(count);
            
        } else {
            if (count > 600000)
                count_string.append("100" + mCountM);
            else if (count > 500000)
                count_string.append("50" + mCountM);
            else if (count > 300000)
                count_string.append("30" + mCountM);
            else if (count > 200000)
                count_string.append("20" + mCountM);
            else if (count >= 100000)
                count_string.append("10" + mCountM);
            else
                count_string.append(count);
        }
        count_string.append(mDownloadStr);
		holder.app_num.setText(count_string);
		
		convertView.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                AppInfoBto applistField = (AppInfoBto) mList.get(curPosition);

                MarketUtils.startAppDetailActivity(mContext, applistField, ReportFlag.FROM_DOWN_MANA_RECOMMEND);
            }
        });
		
		mAsyncImageCache.displayImage(true, holder.app_icon, R.drawable.picture_bg1_big, new AsyncImageCache.NetworkImageGenerator(packageName,imageUrl), true);
		return convertView;
	}

	static class ViewCache{
		ImageView 	app_icon;        
		TextView 	app_name;
		TextView    app_num;
	}
	
	@Override 
	public void unregisterDataSetObserver(DataSetObserver observer) {
	     if (observer != null) {
	         super.unregisterDataSetObserver(observer);
	     }
	 }
	
}
