package com.market.view;

import com.zhuoyi.market.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class SingleButton extends ImageView implements OnClickListener {
    
    private OnSingleBtnClickListener mSingleBtnClickListener = null;
    public final static int CLOSE = 0;
    public final static int OPEN = 1;
    private int mCurState = CLOSE;
    private boolean isBtnClickEnable= true;
    private String mIdentification = "";

    public SingleButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }


    public SingleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }


    public SingleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        setOnClickListener(this);
    }
    
    
    public void setSingleBtnState(int state) {
        if (state == OPEN) {
            mCurState = OPEN;
            setBackgroundResource(R.drawable.select);
        } else if(state == CLOSE) {
            mCurState = CLOSE;
            setBackgroundResource(R.drawable.unselect);
        }
    }
    
    
    public void setBtnClickEnable(boolean enable) {
        isBtnClickEnable = enable;
    }
    
    
    public void setBtnIdentification(String identification) {
        mIdentification = identification;
    }
    
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (!isBtnClickEnable) return;
        
        if (mSingleBtnClickListener != null) {
            
            if (mCurState == OPEN) {
                setSingleBtnState(CLOSE);
            } else if (mCurState == CLOSE) {
                setSingleBtnState(OPEN); 
            }
            mSingleBtnClickListener.onBtnClick(SingleButton.this, mIdentification, mCurState);
        }
    }
    
    /**
     * button点击回调监听
     * @param btnListener
     */
    public void setOnSingleBtnClickListener(OnSingleBtnClickListener btnListener) {
        mSingleBtnClickListener = btnListener;
    }
    
    /**
     * button点击回调接口
     * @author dream.zhou
     *
     */
    public interface OnSingleBtnClickListener {
        public void onBtnClick(View view, String identification, int state);
    }
}
