package com.market.view;

import com.zhuoyi.market.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 1、此布局放在最上层，最先响应触屏事件，返回条件不成立，不影响下层布局的响应事件
 * 
 * *****************************************************************************
 * **实现原理：在activity的布局最上层放置此布局，如果点击位置（横坐标）小于SLIDE_BACK_POSITION（默认20）
 * **就会触发返回消息，然后root view跟随手指移动，当到达屏幕1/3（横坐标）位置，touch up之后就会出发finish()，
 * **关闭activity，如果未到达1/3 root view就回到初始位置
 * *****************************************************************************
 * 
 * 2、需要设置当前activity背景透明，或者半透明，不然看不到下面的activity
 * **"@style/slideBbackStyle"
 * 
 * 3、需要把要返回的activity传递过来，从view中获取的context不是activity的context
 * **setCurActivity(Activity)
 * 
 * 4、如果按返回键需要效果，请在onBackPressed()中调用此布局的onBackPressed()方法即可
 * 
 * @author dream.zhou
 *
 */
public class SlideBackLayout extends RelativeLayout {
    
    private Activity mActivity = null;
    private boolean isSlideBack = false;
    private int mScreenWidth = 0;
    private int mXMove = 0;
    
    private View mPressView = null;
    private Scroller mScroller = null;
    private boolean mNeedFinishActivity = false;
    
    //root view
    private View mParent = null;
    
    //滑动返回成立点击位置
    private final static int SLIDE_BACK_POSITION = 20;
    
    public SlideBackLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }


    public SlideBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }


    public SlideBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }
    
    /**
     * 获取屏幕宽度，滑动1/3屏幕宽度，关闭activity
     * @param context
     */
    private void init(Context context) {
        
        mScroller = new Scroller(context);
        
        mPressView = new View(context);
        mPressView.setBackgroundResource(R.drawable.slide_back_press_bg);
        mPressView.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(SLIDE_BACK_POSITION, RelativeLayout.LayoutParams.MATCH_PARENT);
        this.addView(mPressView, lp);
        
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            mScreenWidth = wm.getDefaultDisplay().getWidth();
        } catch (Exception e) {
            mScreenWidth = 0;
        }
    }
    
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            if (mParent == null)
                mParent = this.getRootView();
        }
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            //当前点击的位置，是不是需要滑动关闭activity
            int downX = (int) event.getX();
            if (downX < SLIDE_BACK_POSITION && mScreenWidth > 0 && mActivity != null) {
                isSlideBack = true;
                mPressView.setVisibility(View.VISIBLE);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (isSlideBack) {
                //root view 跟随手指向右侧移动
                int moveX = (int) event.getX();
                int parentX = mParent.getScrollX();
                mXMove = parentX - moveX;
                mParent.scrollTo(mXMove, 0);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (isSlideBack) {
                //达到关闭activity条件，关闭activity，否则滑动到原来位置
                int offset = Math.abs(mXMove);
                if (offset > (mScreenWidth*1/3)) {
                    mNeedFinishActivity = true;
                    startTouchUpAnim(-offset, offset - mScreenWidth + 1);
                } else {
                    mNeedFinishActivity = false;
                    startTouchUpAnim(-offset, offset);
                    mPressView.setVisibility(View.GONE);
                }
            }
            isSlideBack = false;
            break;
        }
        
        //滑动返回条件不成立，响应下面的view触屏事件
        if (isSlideBack) {
            return true;
        } else { 
            return super.onTouchEvent(event);
        }
    }
    
    
    @Override  
    public void computeScroll() {  
     
        if (mScroller.computeScrollOffset()) {  
            mParent.scrollTo(mScroller.getCurrX(), 0);  
            postInvalidate();  

            if (mScroller.isFinished() && mNeedFinishActivity) {  
                finishActivity();
            }  
        }  
    } 
    
    
    /**
     * 动画效果
     * @param offset：偏移量，正值=向左移动偏移量；负值=向右移动偏移量
     * @param distance：需要移动距离，正值=向左移动；负值=向右移动
     */
    private void startTouchUpAnim(int offset, int distance) {
        mScroller.startScroll(offset, 0, distance, 0, Math.abs(distance));
        postInvalidate();
    }

    
    /**
     * 设置要关闭的activity
     * @param a
     */
    public void setCurActivity(Activity a) {
        mActivity = a;
    }
    
    
    /**
     * 关闭activity
     */
    private void finishActivity() {
        
        if (mPressView != null) {
            this.removeView(mPressView);
            mPressView = null;
        }
        
        if (mActivity != null) {
            mActivity.finish();
            mActivity = null;
        }
        
        mParent = null;
    }
    
    
    /**
     * 按返回键
     */
    public void onBackPressed() {
        mNeedFinishActivity = true;
        startTouchUpAnim(-0, 1 - mScreenWidth);
    }
}
