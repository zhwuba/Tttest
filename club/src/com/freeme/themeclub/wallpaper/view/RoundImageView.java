package com.freeme.themeclub.wallpaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context) {
        super(context);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Path mRoundPath = new Path();
        mRoundPath.addRoundRect(new RectF(0, 0, getMeasuredWidth(), 
                getMeasuredHeight()), 15.0f, 15.0f, Direction.CW);
        canvas.clipPath(mRoundPath);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG));
        super.onDraw(canvas);
    }
}
