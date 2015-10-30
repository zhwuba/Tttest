package com.market.view;

import java.util.Random;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zhuoyi.market.R;

public class SearchLoadingLayout extends LinearLayout
{
	private static final int[] mImages = { R.drawable.loading_under_text1, R.drawable.loading_under_text2,
			R.drawable.loading_under_text3, R.drawable.loading_under_text4, R.drawable.loading_under_text5 };
	private Random mRandom;
	private boolean isShowAnimation = false;
	public SearchLoadingLayout(Context context)
	{
		this(context, null);
	}

	public SearchLoadingLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		setOrientation(LinearLayout.VERTICAL);
		mRandom = new Random();
		setGravity(Gravity.CENTER);
	}

	public void showAnimation()
	{
		ImageView mGifImageView = new ImageView(getContext());
		LayoutParams gifParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		gifParams.gravity = Gravity.CENTER;
		mGifImageView.setLayoutParams(gifParams);
		try {
		    AnimationDrawable mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.loading_animation);
		    mGifImageView.setImageDrawable(mAnimationDrawable);
		    mAnimationDrawable.start();
        } catch (OutOfMemoryError e) {
            System.gc();
        }

		ImageView mTextImageView = new ImageView(getContext());
		LayoutParams txtParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mTextImageView.setLayoutParams(txtParams);
		
		try {
			mTextImageView.setBackgroundResource(mImages[mRandom.nextInt(mImages.length)]);
		} catch (OutOfMemoryError e) {
			System.gc();
		}

		addView(mGifImageView);
		addView(mTextImageView);
	}

	public void stopAnimation()
	{
		removeAllViews();
	}

	@Override
	public void setVisibility(int visibility)
	{
		super.setVisibility(visibility);
		if (visibility == View.GONE || visibility == View.INVISIBLE)
		{
			stopAnimation();
			isShowAnimation = false;
		}
		else if (visibility == View.VISIBLE && isShowAnimation == false)
		{
			showAnimation();
			isShowAnimation = true;
		}
	}

}
