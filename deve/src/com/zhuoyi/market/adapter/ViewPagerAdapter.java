package com.zhuoyi.market.adapter;

import java.util.ArrayList;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ViewPagerAdapter extends PagerAdapter {
	
	private ArrayList<View> myArrayList;
	private int mCount; 
	public ViewPagerAdapter(ArrayList<View> array_list)
	{
		myArrayList = array_list;
		mCount = 1000;//myArrayList.size()+1;
	}
	
	@Override
	public void destroyItem(View v, int position, Object obj)
	{
		// TODO Auto-generated method stub
		if (position >= myArrayList.size()-1)
		{   
            int newPosition = position % myArrayList.size();   
            position = newPosition;   
            ((ViewPager) v).removeView(myArrayList.get(position));   
      
		}   
	}

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mCount;//myArrayList.size();
	}

	@Override
	public Object instantiateItem(View v, int position) {
        if (position >= myArrayList.size()-1) {   
               int newPosition = position%myArrayList.size();   
                  
               position = newPosition;   
              // mCount++;   
           }   

       try {   
           ((ViewPager) v).addView(myArrayList.get(position),0);   
       } catch (Exception e) {   
       }   
		return myArrayList.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return super.getItemPosition(object);
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub

	}
	public ArrayList<View> getArrayList()
	{
		return myArrayList;
	}
	public void setArrayList(ArrayList<View> array_list)
	{
		myArrayList = array_list;
	}
}