package com.freeme.themeclub;

import java.util.List;
import java.util.Map;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class YouLikeAdapter extends BaseAdapter{
    private AsyncImageCache mAsyncImageCache;
    private Context mContext;
    private List<Map<String, Object>> list;

    private int mImageWidth;
    private int mImageHeight;

    public YouLikeAdapter(Context context,
            List<Map<String, Object>> list) {
        this.mContext = context;
        this.list = list;
        mAsyncImageCache=AsyncImageCache.from(mContext);

        mImageWidth = context.getResources().getDimensionPixelSize(
                R.dimen.theme_preview_w);
        mImageHeight = context.getResources().getDimensionPixelSize(
                R.dimen.theme_preview_h);

    }

    public void setMyList(List<Map<String, Object>> list) {
        this.list = list;
    }

    public int getCount() {
        return list.size();
    }
    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        String imageUrl = "";
        if(convertView==null){
            convertView = OnlineThemesUtils.getContentViewByLayout(
                    mContext, R.layout.theme_item);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.icon);
            holder.status = (ImageView) convertView
                    .findViewById(R.id.status);
            holder.text = (TextView) convertView
                    .findViewById(R.id.text);
            holder.downloadTimes = (TextView) convertView
                    .findViewById(R.id.download_times);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }
            Map<String, Object> map = list.get(position);

            imageUrl = (String) map.get("logoUrl");
            mAsyncImageCache.displayImage(holder.icon,
                    R.drawable.theme_no_default, mImageWidth,
                    mImageHeight,
                    new AsyncImageCache.NetworkImageGenerator(imageUrl,
                            imageUrl));

            holder.status.setImageResource(R.drawable.status_downloaded);
            String isInstalled = map.get("isDownloaded").toString();
            if (Boolean.valueOf(isInstalled)) {
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.INVISIBLE);
            }
            holder.text.setText(map.get("name").toString());
            holder.downloadTimes.setText(map.get("dnCnt")+
                    mContext.getResources().getString(R.string.uses_times));
            return convertView;
        }

        class ViewHolder {
            ImageView icon;
            ImageView status;
            TextView text;
            TextView downloadTimes;
        }

    }
