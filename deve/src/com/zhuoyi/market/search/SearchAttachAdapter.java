package com.zhuoyi.market.search;

import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.KeyWordInfoBto;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AppOperatorUtils;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;

public class SearchAttachAdapter extends BaseAdapter {
	private Context mContext;
	
	private List<KeyWordInfoBto> mList;
	public void setDataList(List<KeyWordInfoBto> list) {
		mList = list;
	}
	
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	
	//0不带icon，1带icon
	public static final int TYPE_ITEM_NORMAL = 0,TYPE_ITEM_ICON = 1;

	public SearchAttachAdapter(Context context,List<KeyWordInfoBto> list,DownloadCallBackInterface callBack) {
		this.mContext = context;
		mList = list;
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callBack);
	}


	@Override
	public int getCount() {
		return mList.size();
	}
	

	@Override
	public int getItemViewType(int position) {
		
		//只有第一条数据，或者带官方的应用显示icon
		KeyWordInfoBto keyWord = mList.get(position);
		if (position == 0 || keyWord.getSearchDownload() == 1) {
			return TYPE_ITEM_ICON;
		} else {
			return TYPE_ITEM_NORMAL;
		}
	}
	
	
	public boolean isSearchApp (int position) {
		
		if (mList == null || mList.size() == 0 || position >= mList.size()) {
			return false;
		}
		KeyWordInfoBto keyWord = mList.get(position);
		if (position == 0 || keyWord.getSearchDownload() == 1) {
			return true;
		} else {
			String giftType = keyWord.getAppInfoBto().getActivity();;
			if (isTypeExist(giftType, "0") || isTypeExist(giftType, "2")) {
				return true;
			}
		}
		return false;
	}

	
	@Override
	public int getViewTypeCount() {
		return 3;
	}
	

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder1 viewHolder1 = null;
		ViewHolder2 viewHolder2 = null;
		int needIcon = getItemViewType(position);
		switch (needIcon) {
		case TYPE_ITEM_NORMAL:
			if (convertView != null && convertView.getTag() instanceof ViewHolder1) {
				viewHolder1 = (ViewHolder1) convertView.getTag();
			} else {
				viewHolder1 = new ViewHolder1();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.search_attach_item_normal, null);
				viewHolder1.text = (TextView) convertView.findViewById(R.id.search_attem_textview);
				viewHolder1.giftBagImg = (ImageView) convertView.findViewById(R.id.gift_bag_img);
				viewHolder1.giftIntegralImg = (ImageView) convertView.findViewById(R.id.gift_integral_img);
				convertView.setTag(viewHolder1);
			}
			viewHolder1.text.setText(mList.get(position).getKey());
			setGiftImage(viewHolder1.giftBagImg, viewHolder1.giftIntegralImg, mList.get(position));
			break;
		case TYPE_ITEM_ICON:
			if (convertView != null && convertView.getTag() instanceof ViewHolder2) {
				viewHolder2 = (ViewHolder2)convertView.getTag();
			} else {
                viewHolder2 = new ViewHolder2();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.search_attach_item_app, null);
                viewHolder2.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
                viewHolder2.appName = (TextView) convertView.findViewById(R.id.app_name);
                viewHolder2.giftBagImg = (ImageView) convertView.findViewById(R.id.gift_bag_img);
                viewHolder2.giftIntegralImg = (ImageView) convertView.findViewById(R.id.gift_integral_img);
                viewHolder2.giftOfficialImg = (ImageView) convertView.findViewById(R.id.gift_official_img);
                viewHolder2.appSize = (TextView) convertView.findViewById(R.id.app_size);
                viewHolder2.installBtn = (TextView) convertView.findViewById(R.id.install_btn);
                convertView.setTag(viewHolder2);
			}
			initItemApp(viewHolder2,mList.get(position));
			break;
		}
		return convertView;
	}

	private void initItemApp(ViewHolder2 viewHolder2,KeyWordInfoBto keyWord) {
		final AppInfoBto appInfo = keyWord.getAppInfoBto();
		
		viewHolder2.appName.setText(appInfo.getName());
		viewHolder2.appSize.setText(MarketUtils.humanReadableByteCount(appInfo.getFileSize(),false));
		AsyncImageCache.from(mContext).displayImage(true,viewHolder2.appIcon, R.drawable.picture_bg1_big,
                new AsyncImageCache.NetworkImageGenerator(appInfo.getPackageName(),appInfo.getImgUrl()), true);
		
		//官方标识
		if (keyWord.getSearchDownload() == 1) {
        	if (viewHolder2.giftOfficialImg.getVisibility() != View.VISIBLE)
        		viewHolder2.giftOfficialImg.setVisibility(View.VISIBLE);
        } else {
        	if (viewHolder2.giftOfficialImg.getVisibility() == View.VISIBLE)
        		viewHolder2.giftOfficialImg.setVisibility(View.GONE);
        }
		
		//下载有礼、下载积分
		setGiftImage(viewHolder2.giftBagImg, viewHolder2.giftIntegralImg, keyWord);
		
		//安装按钮
		initInstallBtn(viewHolder2.installBtn, appInfo);
	}


    private void setGiftImage(ImageView giftBagImg, ImageView giftIntegralImg, KeyWordInfoBto keyWord) {
        String giftType = keyWord.getAppInfoBto().getActivity();
        if (isTypeExist(giftType, "2")) {
        	if (giftBagImg.getVisibility() != View.VISIBLE)
        		giftBagImg.setVisibility(View.VISIBLE);
        } else {
        	if (giftBagImg.getVisibility() == View.VISIBLE)
        		giftBagImg.setVisibility(View.GONE);
        }
        
        if (isTypeExist(giftType, "0")) {
        	if (giftIntegralImg.getVisibility() != View.VISIBLE)
        		giftIntegralImg.setVisibility(View.VISIBLE);
        } else {
        	if (giftIntegralImg.getVisibility() == View.VISIBLE)
        		giftIntegralImg.setVisibility(View.GONE);
        }
    }
    
    
    /**
     * 获取某应用是否参与了某项活动
     * @param typeAll 格式：0或者2或者0,2或者2,0
     * @param type 0=积分；2=礼包
     * @return 返回该应用是否参与某项活动
     */
    private boolean isTypeExist(String typeAll, String type) {
    	if (TextUtils.isEmpty(typeAll)) return false;
    	return typeAll.contains(type) ? true:false;
    }
	
	
	private void initInstallBtn(TextView installBtn, AppInfoBto appInfo) {
        AppOperatorUtils.initBtnState(mContext, installBtn, appInfo.getPackageName(), appInfo.getVersionCode(), "");
        installBtn.setOnClickListener(new AppOperatorUtils.CommonAppClick(mContext, appInfo, mDownloadCallBack, ReportFlag.TOPIC_NULL,ReportFlag.FROM_SEARCH,false));
	}
	

	static class ViewHolder1 {
		TextView text;
		TextView installBtn;
		ImageView giftBagImg;
		ImageView giftIntegralImg;
	}
	
	
	static class ViewHolder2 {
		ImageView appIcon;
		TextView appName;
		ImageView giftBagImg;
		ImageView giftIntegralImg;
		ImageView giftOfficialImg;
		TextView appSize;
		TextView installBtn;
	}

}
