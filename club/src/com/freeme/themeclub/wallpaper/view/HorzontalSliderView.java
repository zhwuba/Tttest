package com.freeme.themeclub.wallpaper.view;

import com.freeme.themeclub.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class HorzontalSliderView extends LinearLayout {
	
    public static interface SliderMoveListener {
        void movePercent(float movePercentFromCenter, boolean stopMove);
    }

    private SliderMoveListener mMoveListener = null;
    private int mSliderCenterPositionX = 0;
    private Drawable mSliderDrawable = null;
    private int mSliderLeft = 0;
    private int mSliderMaxLeft = 0;
    private int mSliderMinLeft = 0;
    private int mSliderStartMoveX = 0;

    public HorzontalSliderView(Context context) {
        this(context, null);
    }

    public HorzontalSliderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorzontalSliderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mSliderLeft = -1;
        mSliderMinLeft = -1;
        mSliderMaxLeft = -1;
        mSliderCenterPositionX = -1;
        mSliderStartMoveX = -1;
        
        // com.android.internal.R.styleable.ImageView
        // com.android.internal.R.styleable.ImageView_src
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorzontalSliderView);
        mSliderDrawable = a.getDrawable(R.styleable.HorzontalSliderView_src);
        a.recycle();
        
        if (mSliderDrawable == null) {
            throw new IllegalArgumentException("HorzontalSliderView() must have android:src attribute.");
        }
        mSliderDrawable.setBounds(0, 0, mSliderDrawable.getIntrinsicWidth(), mSliderDrawable.getIntrinsicHeight());
    }

    private int getCenterPostionForSlider() {
        if (mSliderCenterPositionX < 0) {
            mSliderMinLeft = getPaddingLeft();
            mSliderMaxLeft = getWidth() - mSliderDrawable.getIntrinsicWidth() - getPaddingRight();
            mSliderCenterPositionX = (mSliderMaxLeft + mSliderMinLeft) / 2;
        }
        return mSliderCenterPositionX;
    }

    private int getSliderCanMoveDistance() {
        return (mSliderMaxLeft - mSliderMinLeft) / 2;
    }

    private boolean inSliderDrawableArea(int x) {
        return (mSliderLeft <= x && x <= mSliderLeft + mSliderDrawable.getIntrinsicWidth());
    }

    private void updateSliderPostion(int pointerXMoveDistance, boolean stopMove) {
        int newSliderLeft = pointerXMoveDistance + getCenterPostionForSlider();
        if (newSliderLeft < mSliderMinLeft) {
        	newSliderLeft = mSliderMinLeft;
        }
        if (newSliderLeft > mSliderMaxLeft) {
        	newSliderLeft = mSliderMaxLeft;
        }
        if (mSliderLeft != newSliderLeft) {
            mSliderLeft = newSliderLeft;
            
            invalidate();
            if (mMoveListener != null) {
                mMoveListener.movePercent(
                		(float) pointerXMoveDistance / getSliderCanMoveDistance(), stopMove);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSliderLeft < 0) {
            mSliderLeft = getCenterPostionForSlider();
        }
        
        final int saveCount = canvas.getSaveCount();
        canvas.translate(mSliderLeft, getPaddingTop());
        mSliderDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN: {
	        	if (inSliderDrawableArea(x)) {
	                mSliderStartMoveX = x;
	        	}
	        	break;
	        }
	        case MotionEvent.ACTION_UP: {
	        	if (mSliderStartMoveX >= 0) {
	                updateSliderPostion(0, true);
	                mSliderStartMoveX = -1;
	            }
	        	break;
	        }
	        case MotionEvent.ACTION_MOVE: {
	        	if (mSliderStartMoveX >= 0) {
	                updateSliderPostion(x - mSliderStartMoveX, false);
	        	}
	        	break;
	        }
        }
        return true;
    }

    public void regeisterMoveListener(SliderMoveListener l) {
        mMoveListener = l;
    }
}