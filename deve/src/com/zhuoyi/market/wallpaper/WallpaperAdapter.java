package com.zhuoyi.market.wallpaper;

import com.market.net.data.WallpaperInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.AsyncImageCache;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class WallpaperAdapter extends BaseAdapter {
    
    private Context mContext = null;
    private DisplayWallpaperStorage mDisplayWallpaperStorage = null;
    private int mItemHeight = 0;
    private int mItemWidth = 0;
    private int mLocalType = WallpaperView.TYPE_NONE;
    
    private View mFooter = null;
    private int mColNum = 0;
    private int mFooterHeight = 0;
    private int mFooterWidth = 0;
    
    private boolean mAllowRefreshIcon = true;
    
    public WallpaperAdapter(DisplayWallpaperStorage storage, int height, int width, int localType) {
        mDisplayWallpaperStorage = storage;
        mContext = MarketApplication.getRootContext();
        
        mItemHeight = height;
        mItemWidth = width;
        mLocalType = localType;
    }
    
    
    public void setColNum(int colNum) {
    	mColNum = colNum;
    }

    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
    	int more = getAvailableCount() % mColNum;
    	if (more != 0) {
    		more = mColNum - more;
    	}
        return mDisplayWallpaperStorage.getDisplayCount() + more + mColNum;
    }
    
    
    public int getAvailableCount() {
    	return mDisplayWallpaperStorage.getDisplayCount();
    }


    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
    	try {
    		return mDisplayWallpaperStorage.getDisplayWallpaperInfo(position);
    	} catch (Exception e) {
    		return null;
    	}
    }


    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
    	int availableCount = getAvailableCount();
    	int totleCount = getCount();
        if (position >= availableCount) {
        	if (position >= totleCount - mColNum) {
        		if (position % mColNum == 1) {
	        		return mFooter;
	        	} else {
	                convertView = new View(parent.getContext());
	                convertView.setVisibility(View.INVISIBLE);
	                convertView.setMinimumHeight(mFooterHeight);
	                return convertView;
	        	}
        	} else {
        		convertView = new View(parent.getContext());
                convertView.setMinimumHeight(mItemHeight);
                convertView.setMinimumWidth(mItemWidth);
                convertView.setVisibility(View.INVISIBLE);
                return convertView;
        	}
        }
        
        ViewCache holder = null;
        if (convertView != null && convertView.getTag() instanceof ViewCache) {
            holder = (ViewCache)convertView.getTag();
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.wallpaper_item, parent, false); 
            holder = new ViewCache();
            holder.wallpaper_image = (ImageView)convertView.findViewById(R.id.wallpaper_image);
            holder.wallpaper_title = (TextView)convertView.findViewById(R.id.wallpaper_title);
            
            LayoutParams para = holder.wallpaper_image.getLayoutParams();  
            para.height = mItemHeight;  
            para.width = mItemWidth;  
            holder.wallpaper_image.setLayoutParams(para); 
            
            convertView.setTag(holder); 
        }
        
        convertView.setMinimumHeight(mItemHeight);
        setHolderData(holder, mDisplayWallpaperStorage.getDisplayWallpaperInfo(position));
        return convertView;
    }
    
    
    private void setHolderData(ViewCache holder, WallpaperInfoBto info) {
        String url = info.getThumbImageUrl();
        String title = info.getAppName();
        if (TextUtils.isEmpty(title) || mLocalType != WallpaperView.TYPE_CATEGORY) {
            if (holder.wallpaper_title.getVisibility() == View.VISIBLE)
                holder.wallpaper_title.setVisibility(View.GONE);
        } else {
            if (holder.wallpaper_title.getVisibility() != View.VISIBLE)
                holder.wallpaper_title.setVisibility(View.VISIBLE);
            holder.wallpaper_title.setText(title);
        }
        
        if (TextUtils.isEmpty(url)) return;
        AsyncImageCache.from(mContext).displayImage(
        		mAllowRefreshIcon,
                true,
                holder.wallpaper_image,
                R.drawable.wallpaper_def_bg,
                mItemWidth,
                mItemHeight,
                new AsyncImageCache.NetworkImage565Generator(url, url), 
                false, 
                false, 
                false, 
                null);
    }
    
    
    public void setFooter(View view, int footerWidth, int footerHeight) {
    	mFooterHeight = footerHeight;
    	mFooterWidth = footerWidth;
    	mFooter = view;
    	
    	GridView.LayoutParams params = new GridView.LayoutParams(mFooterWidth, mFooterHeight);
    	mFooter.setLayoutParams(params);
    	
    	//mFooter.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());

        //mFooter.setBackgroundResource(R.color.white);
        mFooter.setMinimumHeight(mFooterHeight);
    }
    
    
    public void setAllowRefreshIcon(boolean allow) {
    	mAllowRefreshIcon = allow;
    }
    
    
    public boolean getAllowRefreshIcon() {
    	return mAllowRefreshIcon;
    }
    
    
    static class ViewCache{
        ImageView wallpaper_image;  
        TextView wallpaper_title;
    }


}
