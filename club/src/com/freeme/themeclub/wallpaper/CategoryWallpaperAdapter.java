package com.freeme.themeclub.wallpaper;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;

public class CategoryWallpaperAdapter extends BaseAdapter{
    private Context mContext;
    ArrayList<Map<String, Object>> list;
    private int mImageWidth;
    private int mImageHeight;
    public CategoryWallpaperAdapter(Context context,ArrayList<Map<String, Object>> listData){
        mContext=context;
        list=listData;

        mImageWidth = context.getResources().getDimensionPixelSize(
                R.dimen.category_preview_w);
        mImageHeight = context.getResources().getDimensionPixelSize(
                R.dimen.category_preview_h);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView=LayoutInflater.from(mContext).inflate(R.layout.gridview_category, null);
            holder=new ViewHolder();

            holder.image=(ImageView) convertView.findViewById(R.id.imageView1);
            holder.name=(TextView) convertView.findViewById(R.id.textView1);

            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
        
        holder.name.setText((CharSequence) list.get(position).get("name"));
        AsyncImageCache.from(mContext).displayImage(holder.image,
                R.drawable.wallpaper_default, mImageWidth,
                mImageHeight,
                new AsyncImageCache.NetworkImageGenerator(list.get(position).get("dnUrls")+"",
                        list.get(position).get("dnUrls")+""));

        return convertView;
    }

    private class ViewHolder{   
        public ImageView image;   
        public TextView name;   
    } 
}