package com.market.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class AdViewPager extends ViewPager {
    private boolean mCanScroll;

    private GestureDetector mGestureDetector;
    View.OnTouchListener mGestureListener;
    private float mDownPosX=0,mDownPosY=0;
    public AdViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
       // mGestureDetector = new GestureDetector(new YScrollDetector());
        mCanScroll = true;
    }
    
    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
    	
		return super.dispatchKeyEvent(event);
	}
	@Override
	public boolean onTouchEvent(MotionEvent p_event) {
		if (p_event.getAction() == MotionEvent.ACTION_MOVE
				&& getParent() != null) {
			Log.d("DEBUG", "intercept move event");
			getParent().requestDisallowInterceptTouchEvent(true);
		}

		return super.onTouchEvent(p_event);
	}
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
    	float gap = 6.0f;
    	if(ev.getAction()==MotionEvent.ACTION_DOWN)
    	{
    		mDownPosX = ev.getX();
    		mDownPosY = ev.getY();
    		mCanScroll = false;
    	}
        if(ev.getAction() == MotionEvent.ACTION_MOVE)
        {
        	float x = Math.abs(mDownPosX-ev.getX());
        	float y = Math.abs(mDownPosY-ev.getY());
        	if(x>gap && x>y)
        	{
        		mCanScroll = true;
        	}
        	else
        		mCanScroll = false;
        }
        if(ev.getAction() == MotionEvent.ACTION_UP)
        {
        }
        	
    	return super.dispatchTouchEvent(ev);
    }
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	if(ev.getAction()==MotionEvent.ACTION_DOWN)
    	{
    	}
        if(ev.getAction() == MotionEvent.ACTION_MOVE)
        {
        	if(mCanScroll)
        	 	return true;
        }
        if(ev.getAction() == MotionEvent.ACTION_UP)
        {
        }
        return super.onInterceptTouchEvent(ev);// && mGestureDetector.onTouchEvent(ev);
    }
}
