package com.zhuoyi.market.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhuoyi.market.R;

@SuppressLint("InflateParams")
public class InflateTabIndicatorUtils
{
	public static View getTabIndicatorView(Context context,int imageViewId, int textViewId)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator, null);
		ImageView mImageView = (ImageView) tabIndicatorView.findViewById(R.id.img_indicator);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);

		mImageView.setImageResource(imageViewId);
		mTextView.setText(textViewId);
		return tabIndicatorView;
	}
	
	public static View getTabIndicatorView(Context context,int imageViewId, int textViewId,int num)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator, null);
		ImageView mImageView = (ImageView) tabIndicatorView.findViewById(R.id.img_indicator);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);
		TextView mtxt_bubble = (TextView) tabIndicatorView.findViewById(R.id.txt_bubble);
		
		
		mImageView.setBackgroundDrawable(context.getResources().getDrawable(imageViewId));
		mTextView.setText(textViewId);
		if (num > 0)
		{
			mtxt_bubble.setVisibility(View.VISIBLE);
			mtxt_bubble.setText(String.valueOf(num));
		}
		return tabIndicatorView;
	}

	public static View getTabIndicatorView(Context context,int textViewId)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator_top, null);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);
		mTextView.setText(textViewId);
		return tabIndicatorView;
	}
	public static View getTabIndicatorView(Context context,String textViewId)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator_top, null);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);
		mTextView.setText(textViewId);
		return tabIndicatorView;
	}
	public static View getOneTabIndicatorView(Context context,String textViewId)
	{
		View tabIndicatorView = LayoutInflater.from(context).inflate(R.layout.tab_indicator_top_one, null);
		TextView mTextView = (TextView) tabIndicatorView.findViewById(R.id.txt_indicator);
		mTextView.setText(textViewId);
		return tabIndicatorView;
	}
}
