package com.freeme.themeclub;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {
    private GestureDetector mGestureDetector;

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = mGestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev) && intercept;
    }

    private class YScrollDetector extends SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            return Math.abs(distanceY) >= Math.abs(distanceX);
        }
    }
    
    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
     
     return 0;
    }
}
