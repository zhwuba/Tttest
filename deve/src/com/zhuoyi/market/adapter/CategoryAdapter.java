package com.zhuoyi.market.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.view.MyGridView;
import com.zhuoyi.market.CategoryDetailActivity;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.LogHelper;
import com.zhuoyi.market.utils.MarketUtils;

/**
 * 热门分类下方分类列表的Adapter
 * 
 * @author JLu
 */
public class CategoryAdapter extends CommonAdapter<AppInfoBto> {
    private String mReportFlag;
    
    private String mDetailLogTag = null;
    
    private int mChannelIndex = -1;


    public void setChannelIndex(int channelIndex) {
        this.mChannelIndex = channelIndex;
    }


    public CategoryAdapter(Context context, int layoutId, String reportFlag) {
        super(context, layoutId);
        mReportFlag = reportFlag;
    }
    
    
    public void setDetailLogTag(String logTag) {
        mDetailLogTag = logTag;
    }


    @Override
    public int getCount() {
        switch (mLayoutId) {
        case R.layout.layout_category_view_a_item://style:1
            return mDatas == null ? 0 : mDatas.size();
        case R.layout.layout_category_view_b_item://style:2
            return mDatas == null ? 0 : mDatas.size();
        default://  按照原来的方式展示
            return mDatas==null ? 0:(mDatas.size()+1)/2;
        }
    }

    class MyOnClickListener implements OnClickListener {
        private AssemblyInfoBto assemblyInfo;
        private AppInfoBto appInfo;


        public MyOnClickListener(AssemblyInfoBto assemblyInfo, AppInfoBto appInfo) {
            this.assemblyInfo = assemblyInfo;
            this.appInfo = appInfo;
        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, CategoryDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("channelIndex", mChannelIndex);
            intent.putExtra("level2Id", appInfo.getRefId());
            intent.putExtra("parentName", appInfo.getName());
            intent.putExtra("reportFlag", mReportFlag);
            intent.putExtra("detailLogTag", mDetailLogTag);
            if (assemblyInfo != null) {
                // 点击三级
                intent.putExtra("level3Id", assemblyInfo.getResId());
                intent.putExtra("categoryName", assemblyInfo.getAssName());
            } else {
                // 点击二级
                intent.putExtra("level3Id", -1);
                intent.putExtra("categoryName", appInfo.getName());
            }
            mContext.startActivity(intent);
        }

    }

    class CategoryLevel3Adapter extends BaseAdapter {

        private List<AssemblyInfoBto> mLevel3List;
        private AppInfoBto mAppInfo;


        public CategoryLevel3Adapter(List<AssemblyInfoBto> Level3List, AppInfoBto appInfo) {
            this.mLevel3List = Level3List;
            this.mAppInfo = appInfo;
        }


        @Override
        public int getCount() {
            return mLevel3List == null ? 0 : mLevel3List.size();
        }


        @Override
        public Object getItem(int arg0) {
            return null;
        }


        @Override
        public long getItemId(int arg0) {
            return 0;
        }


        @Override
        public View getView(int mPosition, View convertView, ViewGroup arg2) {

            convertView = LayoutInflater.from(mContext).inflate(R.layout.category_b_item_sub_text, null);
            TextView textView = (TextView) convertView.findViewById(R.id.category_grid_text);

            textView.setText(mLevel3List.get(mPosition).getAssName());
            textView.setOnClickListener(new MyOnClickListener(mLevel3List.get(mPosition), mAppInfo));
            return convertView;
        }

    }


    @Override
    public void convert(ViewHolder holder, AppInfoBto bean, int position) {
        switch (mLayoutId) {
        case R.layout.layout_category_view_a_item://style:1
            setViewA(holder, bean, position);
            break;
        case R.layout.layout_category_view_b_item://style:2
            setViewB(holder, bean, position);
            break;
        default://style:0  按照原来的方式展示
            setDefaultView(holder, bean, position);
            break;
        }
    }


    private void setViewA(ViewHolder holder, AppInfoBto bean, int position) {
        ImageView mCategoryIcon = holder.getView(R.id.category_style_a_icon);
        TextView mCategoryName = holder.getView(R.id.category_style_a_name);

        AsyncImageCache.from(mContext).displayImage(true, mCategoryIcon, R.drawable.category_icon_bg,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(bean.getImgUrl()), bean.getImgUrl()),
            false);
        mCategoryName.setText(bean.getName());
        holder.getConvertView().setOnClickListener(new MyOnClickListener(null, bean));
    }


    private void setViewB(ViewHolder holder, AppInfoBto bean, int position) {
        LinearLayout mLevel2 = holder.getView(R.id.level2);
        ImageView mLevel2Icon = holder.getView(R.id.level2_icon);
        TextView mLevel2Text = holder.getView(R.id.level2_text);
        MyGridView mLevel2Gridview = holder.getView(R.id.level2_gridview);

        AsyncImageCache.from(mContext).displayImage(true, mLevel2Icon, R.drawable.category_icon_bg,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(bean.getImgUrl()), bean.getImgUrl()),
            false);
        mLevel2Text.setText(bean.getName());
        mLevel2.setOnClickListener(new MyOnClickListener(null, bean));
        List<AssemblyInfoBto> Level3List = bean.getAssemblyList();
        mLevel2Gridview.setAdapter(new CategoryLevel3Adapter(Level3List, bean));
    }
    

    private void setDefaultView(ViewHolder holder, AppInfoBto bean, int position) {

        // find left
        LinearLayout mLeftLevel2 = holder.getView(R.id.left_level2);
        ImageView mLeftLevel2Icon = holder.getView(R.id.left_level2_icon);
        TextView mLeftLevel2Text = holder.getView(R.id.left_level2_text);
        TextView mLeftLevel2Arrow = holder.getView(R.id.left_level2_arrow);
        TextView mLeftLevel301 = holder.getView(R.id.left_level3_01);
        TextView mLeftLevel302 = holder.getView(R.id.left_level3_02);
        TextView mLeftLevel303 = holder.getView(R.id.left_level3_03);
        // find right
        LinearLayout mRightItem = holder.getView(R.id.right_item);
        LinearLayout mRightLevel2 = holder.getView(R.id.right_level2);
        ImageView mRightLevel2Icon = holder.getView(R.id.right_level2_icon);
        TextView mRightLevel2Text = holder.getView(R.id.right_level2_text);
        TextView mRightLevel2Arrow = holder.getView(R.id.right_level2_arrow);
        TextView mRightLevel301 = holder.getView(R.id.right_level3_01);
        TextView mRightLevel302 = holder.getView(R.id.right_level3_02);
        TextView mRightLevel303 = holder.getView(R.id.right_level3_03);

        // init left
        AppInfoBto leftAppInfo = mDatas.get(2 * position);

        AsyncImageCache.from(mContext).displayImage(
            true,
            mLeftLevel2Icon,
            R.drawable.category_icon_d_bg,
            new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(leftAppInfo.getImgUrl()), leftAppInfo
                .getImgUrl()), false);
        mLeftLevel2Text.setText(leftAppInfo.getName());

        String leftFgColor = leftAppInfo.getFgColor();
        if (!TextUtils.isEmpty(leftFgColor)) {
            try {
                mLeftLevel2Text.setTextColor(android.graphics.Color.parseColor(leftFgColor));
                mLeftLevel2Arrow.setTextColor(android.graphics.Color.parseColor(leftFgColor));
            } catch (IllegalArgumentException e) {
                LogHelper.trace("Exception_" + leftFgColor);
            }
        }

        List<AssemblyInfoBto> leftLevel3List = leftAppInfo.getAssemblyList();
        // 数组越界
        try {
            mLeftLevel301.setText(leftLevel3List.get(0).getAssName());
            mLeftLevel302.setText(leftLevel3List.get(1).getAssName());
            mLeftLevel303.setText(leftLevel3List.get(2).getAssName());
        } catch (IndexOutOfBoundsException e) {

        }
        // 设置左侧监听
        mLeftLevel2.setOnClickListener(new MyOnClickListener(null, leftAppInfo));
        mLeftLevel301.setOnClickListener(new MyOnClickListener(leftLevel3List.get(0), leftAppInfo));
        mLeftLevel302.setOnClickListener(new MyOnClickListener(leftLevel3List.get(1), leftAppInfo));
        mLeftLevel303.setOnClickListener(new MyOnClickListener(leftLevel3List.get(2), leftAppInfo));

        // init right
        if (2 * position + 1 < mDatas.size()) {
            mRightItem.setVisibility(View.VISIBLE);
            AppInfoBto rightAppInfo = mDatas.get(2 * position + 1);

            AsyncImageCache.from(mContext).displayImage(
                true,
                mRightLevel2Icon,
                R.drawable.category_icon_d_bg,
                new AsyncImageCache.NetworkImageGenerator(MarketUtils.getImgUrlKey(rightAppInfo.getImgUrl()),
                    rightAppInfo.getImgUrl()), false);
            mRightLevel2Text.setText(rightAppInfo.getName());

            String rightFgColor = rightAppInfo.getFgColor();
            if (!TextUtils.isEmpty(rightFgColor)) {
                try {
                    mRightLevel2Text.setTextColor(android.graphics.Color.parseColor(rightFgColor));
                    mRightLevel2Arrow.setTextColor(android.graphics.Color.parseColor(rightFgColor));
                } catch (IllegalArgumentException e) {
                    LogHelper.trace("Exception_" + rightFgColor);
                }
            }

            List<AssemblyInfoBto> rightLevel3List = rightAppInfo.getAssemblyList();
            // 数组越界
            try {
                mRightLevel301.setText(rightLevel3List.get(0).getAssName());
                mRightLevel302.setText(rightLevel3List.get(1).getAssName());
                mRightLevel303.setText(rightLevel3List.get(2).getAssName());
            } catch (IndexOutOfBoundsException e) {

            }

            // 设置右侧监听
            mRightLevel2.setOnClickListener(new MyOnClickListener(null, rightAppInfo));
            mRightLevel301.setOnClickListener(new MyOnClickListener(rightLevel3List.get(0), rightAppInfo));
            mRightLevel302.setOnClickListener(new MyOnClickListener(rightLevel3List.get(1), rightAppInfo));
            mRightLevel303.setOnClickListener(new MyOnClickListener(rightLevel3List.get(2), rightAppInfo));
        }

    }

}
