package com.zhuoyi.market.home;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager
{

	private boolean isScrollable = true;

	public MyViewPager(Context context)
	{
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (!isScrollable)
		{
			return true;
		}
		return super.onTouchEvent(ev);
	}

	public boolean isScrollble()
	{
		return isScrollable;
	}

	public void setScrollable(boolean scrollable)
	{
		this.isScrollable = scrollable;
	}
}
