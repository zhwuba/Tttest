package com.market.view;

import com.zhuoyi.market.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MultiButton extends RelativeLayout {
    
    private View mMainView = null;
    private OnMultiBtnClickListener mMultiBtnClickListener = null;
    private Button mLeftBtn = null;
    private Button mMiddleBtn = null;
    private Button mRightBtn = null;
    
    public final static int LEFT = 0;
    public final static int MIDDLE = 1;
    public final static int RIGHT = 2;
    
    private int mCurSelBtn = LEFT;

    public MultiButton(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public MultiButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public MultiButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView(context);
    }
    
    /**
     * 初始化View
     * @param context
     */
    private void initView(Context context) {
        
        LayoutInflater tLayoutInflater = LayoutInflater.from(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        mMainView = tLayoutInflater.inflate(R.layout.multi_button_layout, null);
        mLeftBtn = (Button)mMainView.findViewById(R.id.multi_btn_left);
        mLeftBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mCurSelBtn == LEFT) return;
                if (mMultiBtnClickListener != null) {
                    mLeftBtn.setBackgroundResource(R.drawable.setting_button_selected);
                    mMiddleBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mRightBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mCurSelBtn = LEFT;
                    mMultiBtnClickListener.onBtnClick(MultiButton.this, LEFT);
                }
            }
            
        });
        mMiddleBtn = (Button)mMainView.findViewById(R.id.multi_btn_middle);
        mMiddleBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mCurSelBtn == MIDDLE) return;
                if (mMultiBtnClickListener != null) {
                    mLeftBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mMiddleBtn.setBackgroundResource(R.drawable.setting_button_selected);
                    mRightBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mCurSelBtn = MIDDLE;
                    mMultiBtnClickListener.onBtnClick(MultiButton.this, MIDDLE);
                }
            }
            
        });
        mRightBtn = (Button)mMainView.findViewById(R.id.multi_btn_right);
        mRightBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mCurSelBtn == RIGHT) return;
                if (mMultiBtnClickListener != null) {
                    mLeftBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mMiddleBtn.setBackgroundResource(R.drawable.setting_button_unselected);
                    mRightBtn.setBackgroundResource(R.drawable.setting_button_selected);
                    mCurSelBtn = RIGHT;
                    mMultiBtnClickListener.onBtnClick(MultiButton.this, RIGHT);
                }
            }
            
        });
        this.addView(mMainView, params);
    }
    
    /**
     * 初始button
     * @param initBtn
     */
    public void setInitBtn(int initBtn) {
        
        if (initBtn < LEFT || initBtn > RIGHT) return;
        
        switch (initBtn) {
        case LEFT:
            mLeftBtn.setBackgroundResource(R.drawable.setting_button_selected);
            mMiddleBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            mRightBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            break;
        case MIDDLE:
            mLeftBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            mMiddleBtn.setBackgroundResource(R.drawable.setting_button_selected);
            mRightBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            break;
        case RIGHT:
            mLeftBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            mMiddleBtn.setBackgroundResource(R.drawable.setting_button_unselected);
            mRightBtn.setBackgroundResource(R.drawable.setting_button_selected);
            break;
        }
        mCurSelBtn = initBtn;
    }
    
    /**
     * 三个button名字
     * @param leftName 左边button名字
     * @param middleName 中间button名字
     * @param rightName 右边button名字
     */
    public void setBtnName(String leftName, String middleName, String rightName) {
        if (mLeftBtn != null) {
            if (!TextUtils.isEmpty(leftName)) {
                mLeftBtn.setText(leftName); 
            } else {
                mLeftBtn.setText("");
            }
        }
        
        if (mMiddleBtn != null) {
            if (!TextUtils.isEmpty(middleName)) {
                mMiddleBtn.setText(middleName); 
            } else {
                mMiddleBtn.setText("");
            }
        }
        
        if (mRightBtn != null) {
            if (!TextUtils.isEmpty(rightName)) {
                mRightBtn.setText(rightName); 
            } else {
                mRightBtn.setText("");
            }
        }
    }
    
    /**
     * 三个button点击回调监听
     * @param btnListener
     */
    public void setOnMultiBtnClickListener(OnMultiBtnClickListener btnListener) {
        mMultiBtnClickListener = btnListener;
    }
    
    /**
     * 三个button点击回调接口
     * @author dream.zhou
     *
     */
    public interface OnMultiBtnClickListener {
        public void onBtnClick(View view, int curBtn);
    }
}
