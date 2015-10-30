package com.market.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

public class CommonLoadingLayout extends RelativeLayout {

    public CommonLoadingLayout(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }


    public CommonLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }


    public CommonLoadingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
       return true;
    }
}
