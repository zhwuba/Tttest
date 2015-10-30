package com.freeme.themeclub.theme.onlinetheme;

import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.R;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RadioGroup.LayoutParams;

public class PreviewGallery extends LinearLayout {

    private ViewPager mViewPager;
    private PageIndicator mPageIndicator;
    

    public PreviewGallery(Context context) {
        super(context);
    }

    public PreviewGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mViewPager = (ViewPager) findViewById(R.id.preview_view_pager);
        mPageIndicator = (PageIndicator) findViewById(R.id.preview_page_indicator);

        mViewPager.setOffscreenPageLimit(10);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(
                R.dimen.viewpager_margin));

        mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mPageIndicator.onScrollFinish(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                if (positionOffset > 0.00129) {
                    postInvalidate();
                }
            }

        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mViewPager.onTouchEvent(event);
    }

    public void setAdapter(PagerAdapter adapter) {
        mViewPager.setAdapter(adapter);
        mViewPager.getAdapter().notifyDataSetChanged();
        mPageIndicator.setCount(adapter.getCount());

        if (adapter.getCount()>1)
            setCurrentItem(0);
    }

    public void setCurrentItem(int item) {
        mViewPager.setCurrentItem(item);
    }

    public static class PreviewGalleryPagerAdapter extends PagerAdapter {
        public List<View> dataList;
        public PreviewGalleryPagerAdapter() {

        }

        public PreviewGalleryPagerAdapter(ArrayList<View> dataList2) {
            dataList = dataList2;
        }
        
        public void setData(ArrayList<View> dataList2){
            dataList = dataList2;
            notifyDataSetChanged();
        }

        public int getCount() {
            return dataList.size();
        }

        public void destroyItem(View container,int position, Object object){
            ((ViewGroup) container).removeView((View) object);
            object = null;
        }

        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(dataList.get(position), 0,
                    new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
            return dataList.get(position);
        }

        public boolean isViewFromObject(View container, Object object) {
            return container == (object);
        }
    }
}
