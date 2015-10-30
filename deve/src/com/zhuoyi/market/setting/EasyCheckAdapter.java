package com.zhuoyi.market.setting;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyi.market.setting.EasyCheckActivity;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;
import com.zhuoyi.market.R;
import com.market.net.data.AppInfoBto;

public class EasyCheckAdapter extends BaseAdapter
{
    private Context mCtx;

    private List<AppInfoBto> mLists;

    public EasyCheckAdapter(Context ctx, List<AppInfoBto> list)
    {
        mCtx = ctx;
        mLists = list;
    }

    @Override
    public int getCount()
    {
        if (mLists != null)
        {
            return mLists.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0)
    {
        if (mLists != null)
        {
            return mLists.get(arg0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int arg0)
    {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View root = null;

        root = View.inflate(mCtx, R.layout.ycb_list_item, null);

        if (root != null)
        {
            final int _mPosition = position;
            TextView name = (TextView) root.findViewById(R.id.ycb_item_name);
            ImageView icon = (ImageView) root.findViewById(R.id.ycb_item_icon);
            RelativeLayout replace = (RelativeLayout) root.findViewById(R.id.replace_btn);

            AppInfoBto info = mLists.get(position);
            name.setText(info.getName());

            String fileName = info.getPackageName();
            Bitmap bitmap = BitmapUtiles.convertFileToBitmap(fileName);
            if (bitmap == null)
            {
                bitmap = BitmapUtiles.drawableToBitmap((Drawable) info.getDrawable());
                if (bitmap != null)
                    BitmapUtiles.saveBitmapToFile(fileName, bitmap);
            }
            if (bitmap == null)
            {
                icon.setImageResource(R.drawable.icon);
            }
            else
            {
                icon.setImageBitmap(bitmap);
            }

            replace.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EasyCheckActivity.sendToTarget(EasyCheckActivity.UPDATE_APK, _mPosition);
                }
            });
        }

        return root;
    }

    public void refreshList(List<AppInfoBto> list)
    {
        mLists = list;
        super.notifyDataSetChanged();
    }
}
