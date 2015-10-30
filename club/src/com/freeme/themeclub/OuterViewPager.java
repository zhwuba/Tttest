package com.freeme.themeclub;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class OuterViewPager extends ViewPager {

	public OuterViewPager(Context context) {
		super(context);
	}
	
	public OuterViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override 
    public boolean onInterceptTouchEvent(MotionEvent motionEvent ) { 
		//getParent().requestDisallowInterceptTouchEvent(true);    
       // return super.dispatchTouchEvent(motionEvent);  
     return false;  
    }
	
	/*@Override
	  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

	    int height = 0;
	    for(int i = 0; i < getChildCount(); i++) {
	      View child = getChildAt(i);
	      child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	      int h = child.getMeasuredHeight();
	      if(h > height) height = h;
	    }

	    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	  }*/

}
