package com.freeme.themeclub.theme.onlinetheme;

import com.freeme.themeclub.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ImageView;

public class PageIndicator extends LinearLayout{
	private Drawable mNormalDrawable;
	private Drawable mFocusedDrawable;

	private static final int VIEW_MARGIN = 5;

	private int mCount;
	private int mCurrentIndex;
	private LayoutParams layoutParams;

	public PageIndicator(Context context) {
		this(context, null);
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		//*/added by daichengyuan at 20130916 for the style of indicator
		layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.indicator_width),
				getResources().getDimensionPixelSize(R.dimen.indicator_hight));
		layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.indicator_margin);
		layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.indicator_margin);
		//*/
		mNormalDrawable = this.getResources().getDrawable(R.drawable.indicator_normal);
		mFocusedDrawable = this.getResources().getDrawable(R.drawable.indicator_selected);
	}

	public void setCount(int count,int currentIndex) {
		this.mCount = count;
		this.mCurrentIndex= currentIndex;
		generateIndicators();
		updateIndicator(currentIndex);
	}

	public void onScrollFinish(int currentIndex) {
		updateIndicator(currentIndex);
	}
	
	public void setCount(int count) {
		this.mCount = count;
		generateIndicators();
		updateIndicator(mCurrentIndex);
	}
	
	public void generateIndicators() {
		this.removeAllViews();
		for (int i = 0; i < this.mCount; i++) {
			ImageView imageView = new ImageView(getContext());
			LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//			parms.weight = 1;
			//*/added by daichengyuan at 20130914 for optimize pageindicator
			imageView.setImageResource(R.drawable.page_indicator);
			//*/
			if(mCount < 16){
				parms.leftMargin = VIEW_MARGIN;
				parms.rightMargin = VIEW_MARGIN;
			}
			
			this.addView(imageView, parms);
		}
	}

	public void updateIndicator(int currentIndex) {
		for (int i = 0; i < this.mCount; i++) {
			//*/modified by daichengyuan at 20130914 for optimize pageindicator
			//final int index = i;
			ImageView imageView = (ImageView) this.getChildAt(i);
			imageView.setLayoutParams(layoutParams);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setSelected(i==currentIndex);
			//if (currentIndex == i) {
			//	imageView.setBackgroundResource(R.drawable.indicator_selected);
			//} else {
			//	imageView.setBackgroundResource(R.drawable.indicator_normal);
			//}
			//*/
		}
	}


}
