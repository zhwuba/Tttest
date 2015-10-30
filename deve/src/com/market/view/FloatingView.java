package com.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhuoyi.market.R;

/**
 * 自定义浮动view(用于红包,活动)
 * @author JLu
 *
 */
public class FloatingView extends RelativeLayout {
	private Context mContext;
	
	public FloatingView(Context context) {
		super(context);
		mContext = context;
		initView();
	}
	
    
    public FloatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    
    public FloatingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initView();
    }
    
    private void initView() {
    	
    	ViewTreeObserver vto = this.getViewTreeObserver();
    	vto.addOnPreDrawListener(new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
            	int scrWidth = getMeasuredWidth();
            	int scrHeight = getMeasuredHeight();
            	ImageView floatingView = (ImageView)getChildAt(0);
            	int viewWidth = floatingView.getMeasuredWidth();
//            	int viewHeight = floatingView.getMeasuredHeight();
            	int viewHeight = mContext.getResources().getDimensionPixelSize(R.dimen.floating_view_size);
            	
            	if(scrWidth!=0 && viewWidth!=0) {
            		RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) floatingView.getLayoutParams();
                	lParams.leftMargin = scrWidth - viewWidth;
            		lParams.topMargin = getHeight() - lParams.height;
                	floatingView.setLayoutParams(lParams);
            		floatingView.setOnTouchListener(new MyOnTouchListener(scrWidth-viewWidth,scrHeight-viewHeight));
            		FloatingView.this.getViewTreeObserver().removeOnPreDrawListener(this);    
            	} 
            	
				return true;
			}
		});
    	
    }
    
	class MyOnTouchListener implements OnTouchListener {
    	private int xDown,yDown;
        private int xDistance,yDistance;
        private boolean isMoving = false;
        private int xDelta;
        private int yDelta; 
        private int touchSlop = 2; //点击的误差范围,超过该值不响应点击事件
        private int[] location = new int[2];
        private int screenWidth;
        
        private int xBound,yBound,titleHeight;
        private RelativeLayout.LayoutParams lParams;
        
        
        public MyOnTouchListener(int xBound, int yBound) {
        	this.xBound = xBound;
        	this.yBound = yBound;
        	this.titleHeight = getResources().getDimensionPixelSize(R.dimen.title_heigh);
        	touchSlop = getResources().getDimensionPixelSize(R.dimen.floating_view_touch_slop);
        	screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        }

		@Override
		public boolean onTouch(View view, MotionEvent event) {

            final int X = (int) event.getRawX();  
            final int Y = (int) event.getRawY();  
            if(lParams == null) {
            	lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
            }
            
            switch (event.getAction() & MotionEvent.ACTION_MASK) {  
            case MotionEvent.ACTION_DOWN:  
                isMoving = false;
                xDown = X;
                yDown = Y;
                  
                xDelta = xDown - lParams.leftMargin;
                yDelta = yDown - lParams.topMargin;
                break;  
            case MotionEvent.ACTION_MOVE:  
                isMoving = true;
                xDistance = X - xDelta;
                yDistance = Y - yDelta;
                
                if(xDistance < 0) {
                	xDistance = 0;
                } else if(xDistance > xBound) {
                	xDistance = xBound;
                }
                
                if(yDistance < titleHeight) {
                	yDistance = titleHeight;
                } else if(yDistance > yBound) {
                	yDistance = yBound;
                }
                
                if (yDistance > getHeight() - lParams.height)
                {
                	yDistance = getHeight() - lParams.height;
                }
                
                lParams.leftMargin = xDistance; 
                lParams.topMargin = yDistance;  
                view.setLayoutParams(lParams);  
                break;  
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:  
                if(Math.abs(X-xDown)<touchSlop && Math.abs(Y-yDown)<touchSlop) {
                    isMoving = false;
                }
                xDistance = X - xDelta;
                yDistance = Y - yDelta;
                
                if(xDistance < 0) 
                {
                	xDistance = 0;
                } 
                else if(xDistance > xBound) 
                {
                	xDistance = xBound;
                }
                
                if(yDistance < titleHeight) 
                {
                	yDistance = titleHeight;
                } 
                else if(yDistance > yBound) 
                {
                	yDistance = yBound;
                }
                
                if (yDistance > getHeight() - lParams.height)
                {
                	yDistance = getHeight() - lParams.height;
                }
                
            	view.getLocationOnScreen(location);
            	int viewWidth = view.getMeasuredWidth();
            	Animation translateAnimation;
            	if (location[0] + viewWidth / 2 < screenWidth / 2)
				{
					lParams.leftMargin = 0;
					translateAnimation = new TranslateAnimation(xDistance, 0, 0, 0);
	                translateAnimation.setDuration((int) (Math.abs(xDistance) * 1.5));  
				}
				else
				{
					lParams.leftMargin = screenWidth - viewWidth;
					translateAnimation = new TranslateAnimation(xDistance - screenWidth + viewWidth, 0, 0, 0);
	                translateAnimation.setDuration((int) (Math.abs(xDistance - screenWidth + viewWidth) * 1.5));  
				}	
                lParams.topMargin = yDistance;  
                view.setLayoutParams(lParams); 
                view.startAnimation(translateAnimation);
                break;  
            }  
            return isMoving;
        }
    	
    }

}
