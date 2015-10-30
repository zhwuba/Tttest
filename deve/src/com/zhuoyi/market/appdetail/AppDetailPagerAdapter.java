package com.zhuoyi.market.appdetail;

import java.util.ArrayList;
import java.util.List;

import com.zhuoyi.market.adapter.ViewPagerAdapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;


public class AppDetailPagerAdapter extends PagerAdapter {

	private ArrayList<String> mTitles;
	
	public AppDetailPagerAdapter(ArrayList<View> array_list, ArrayList<String> titles) {
		mViews = array_list;
		mCount = mViews.size();
		if(titles != null) {
			mTitles = titles;
		}
	}

	private List<View> mViews;
	private int mCount;


	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {

		return arg0 == arg1;
	}


	@Override
	public int getCount() {

		return mCount;
	}


	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViews.get(position));

	}


	@Override
	public CharSequence getPageTitle(int position) {
		return mTitles.get(position);
	}


	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}


	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = mViews.get(position);
		container.addView(view);
		return view;
	}

}
