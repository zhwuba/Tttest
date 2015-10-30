package com.freeme.themeclub.wallpaper.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ShowAllGridView extends GridView {

    public ShowAllGridView(Context context) {
        super(context);
    }
    public ShowAllGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public ShowAllGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expanSpec=MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >>2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expanSpec);
    }
}
