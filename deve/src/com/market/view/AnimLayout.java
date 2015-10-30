package com.market.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class AnimLayout extends RelativeLayout {
    
    private Context mContext = null;
    private Animation mAnim = null;
    private Activity mActivity = null;
    
    private boolean mAnimFinish = true;
    private View mySelf = null;
    
    public AnimLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }


    public AnimLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }


    public AnimLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }
    
    /**
     * 初始化
     * @param context
     */
    private void init(Context context) {
        mContext = context;
        mySelf = this;
    }
    
    /**
     * 设置动画xml id
     * @param animId 动画xml id
     * @param setListener 是否设置动画监听事件
     */
    public void setAnimById(int animId, boolean setListener) {
        try {
            mAnim = AnimationUtils.loadAnimation(mContext, animId);
        } catch (Exception e) {
            mAnim = null;
        }
        
        if (setListener) {
            this.setAnimListener();
        }
    }
    
    /**
     * 设置并启动动画
     * @param animId 动画xml id
     * @param setListener 是否设置动画监听事件
     */
    public void setAndStartAnimById(int animId, boolean setListener) {
        try {
            mAnim = AnimationUtils.loadAnimation(mContext, animId);
        } catch (Exception e) {
            mAnim = null;
        }
        
        if (setListener) {
            this.setAnimListener();
        }
        
        this.startAnim();
    }
    
    /**
     * 设置动画
     * @param anim 动画
     * @param setListener 是否设置动画监听事件
     */
    public void setAnim(Animation anim, boolean setListener) {
        mAnim = anim;
        if (setListener) {
            this.setAnimListener();
        }
    }
    
    /**
     * 设置并启动动画
     * @param anim 动画
     * @param setListener 是否设置动画监听事件
     */
    public void setAndStartAnim(Animation anim, boolean setListener) {
        mAnim = anim;
        
        if (setListener) {
            this.setAnimListener();
        }
        
        this.startAnim();
    }
    
    /**
     * 启动动画
     */
    public void startAnim() {
        if (mAnim != null)
            this.startAnimation(mAnim);
    }
    
    /**
     * 设置动画监听事件
     */
    public void setAnimListener() {
        
        if (mAnim != null)
            mAnim.setAnimationListener(new AnimationListener() {
    
                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    mAnimFinish = true;
                    if (mActivity != null) {
                        if (mySelf != null) {
                            mySelf.setVisibility(View.GONE);
                        }
                        mActivity.finish();
                        mActivity = null;
                    }
                }
    
                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub
                    mAnimFinish = false;
                }
                
            });
    }
    
    /**
     * 设置需要关闭的Activity
     */
    public void setCloseActivity(Activity activity) {
        mActivity = activity;
    }
    
    
    /**
     * 释放资源
     */
    public void releaseRes() {
        mContext = null;
        mAnim = null;
        mActivity = null;
    }
    
    
    public boolean getAnimFinish() {
        return mAnimFinish;
    }
}
