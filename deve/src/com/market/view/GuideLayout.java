package com.market.view;

import com.zhuoyi.market.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GuideLayout extends RelativeLayout implements ViewChangeListener{
    
    private Context mContext = null;
    private RelativeLayout mMainView = null;
    private LinearLayout mPointLayout = null;
    private MyGroupView mMyGroupView = null;
    private Button mStartupBtn;
    private int mCount;
    private int mCurrentItem;
    private ImageView mImgs[];
    private RelativeLayout mScrollGuideLayout[];
    private final int mGuideImageId[] = { R.drawable.guide1, R.drawable.guide2, R.drawable.guide3};
    
    private OnGuideClickListener mOnGuideClickListener = null;
    
    public GuideLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public GuideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public GuideLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView(context);
    }
    
    
    private void initView (Context context) {
        mContext = context;
        
        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mMainView = (RelativeLayout) tLayoutInflater.inflate(R.layout.layout_guide, this , true);
        
        mMyGroupView = (MyGroupView) mMainView.findViewById(R.id.ScrollLayout);
        mMyGroupView.SetOnViewChangeListener(this);
        mPointLayout = (LinearLayout) mMainView.findViewById(R.id.point);
        mStartupBtn = (Button) mMainView.findViewById(R.id.startBtn);
        mStartupBtn.setOnClickListener(new OnClickListener () {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mOnGuideClickListener != null)
                    mOnGuideClickListener.onClick();
            }
            
        });
        mCount = mMyGroupView.getChildCount();
        mScrollGuideLayout = new RelativeLayout[mCount];
        mImgs = new ImageView[mCount];

        Bitmap bm = null;
        BitmapDrawable bd = null;
        
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565; 
        opt.inPurgeable = true;          
        opt.inInputShareable = true; 
        
        for (int i = 0; i < mCount; i++) {
            mImgs[i] = (ImageView) mPointLayout.getChildAt(i);
            mImgs[i].setEnabled(true);
            mImgs[i].setTag(i);
            mScrollGuideLayout[i] = (RelativeLayout) mMyGroupView
                    .getChildAt(i);
            bm = BitmapFactory.decodeResource(this.getResources(), mGuideImageId[i], opt);
            bd = new BitmapDrawable(this.getResources(), bm);
            mScrollGuideLayout[i].setBackgroundDrawable(bd);
        }
        mCurrentItem = 0;
        mImgs[mCurrentItem].setEnabled(false);
    }
    
    
    @Override
    public void OnViewChange(int view) {
        // TODO Auto-generated method stub
        if (view < 0 || view > mCount - 1 || mCurrentItem == view) {
            return;
        }
        mImgs[mCurrentItem].setEnabled(true);
        mImgs[view].setEnabled(false);
        mCurrentItem = view;
    }
    
    
    public void releaseRes () {
        mContext = null;
        BitmapDrawable bd = null;
        for (int i = 0; i < mCount; i++) {
            if(mScrollGuideLayout[i] != null){
                bd = (BitmapDrawable) mScrollGuideLayout[i].getBackground();
                mScrollGuideLayout[i].setBackgroundResource(0);
            }
            if(bd != null){
                bd.setCallback(null);
                bd.getBitmap().recycle();
                bd = null;
            }
        }
        
        if (mStartupBtn != null) {
        	mStartupBtn.setBackgroundResource(0);
        }
        mStartupBtn = null;
        System.gc();
    }
    
    
    public void setOnGuideClickListener (OnGuideClickListener l) {
        mOnGuideClickListener = l;
    }
    
    
    public interface OnGuideClickListener {
        public void onClick ();
    }

}
