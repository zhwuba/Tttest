package com.freeme.themeclub.banner;

import com.freeme.themeclub.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class BannerIndicator extends LinearLayout {
    private Drawable mNormalDrawable;
    private Drawable mFocusedDrawable;

    private static final int VIEW_MARGIN = 12;

    private int mCount;
    private Context mContext = null;

    public BannerIndicator(Context context) {
        this(context, null);
    }

    public BannerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mNormalDrawable = this.getResources().getDrawable(R.drawable.banner_unselected);
        mFocusedDrawable = this.getResources().getDrawable(R.drawable.banner_selected);
    }

    /**
     * Set the number of page.
     * 
     * @param count
     *            The page of allapps.
     */
    public void setCount(int count, int currentIndex) {
        this.mCount = count;
        generateIndicators();
        updateIndicator(currentIndex);
    }

    public void onScrollFinish(int currentIndex) {
        updateIndicator(currentIndex);
    }

    /**
     * Generate the view of page.
     */
    public void generateIndicators() {
        this.removeAllViews();
        for (int i = 0; i < this.mCount; i++) {
            ImageView imageView = new ImageView(mContext);
            // setTextViewStyle(textView);
            LinearLayout.LayoutParams parms = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            parms.weight = 1;
            if (mCount < 16) {
                parms.leftMargin = VIEW_MARGIN;
                parms.rightMargin = VIEW_MARGIN;
            }

            this.addView(imageView, parms);
        }
    }

    /**
     * Update the page indicators if items changed.
     * 
     * @param currentIndex
     */
    public void updateIndicator(int currentIndex) {
        for (int i = 0; i < this.mCount; i++) {
            final int index = i;
            ImageView imageView = (ImageView) this.getChildAt(i);
            if (currentIndex == i) {
                imageView.setBackgroundDrawable(mFocusedDrawable);
            } else {
                imageView.setBackgroundDrawable(mNormalDrawable);
            }
        }
    }

    public void updateIndicators(int total, int level) {
        if (total != mCount) {
            mCount = total;
            generateIndicators();
        }
        updateIndicator(level);
    }
}
