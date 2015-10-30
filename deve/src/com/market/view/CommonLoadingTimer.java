package com.market.view;

import com.zhuoyi.market.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CommonLoadingTimer extends View {

    // 圆内、圆环、文字Paint
    private Paint mCirclePaint = null;
    private Paint mRingPaint = null;
    private Paint mTextPaint = null;
    // 半径
    private float mRingRadius;
    // 圆心
    private int mXCenter;
    private int mYCenter;
    // 字体宽高
    private float mTxtWidth;
    private float mTxtHeight;
    // 进度
    private int mProgress = 0;
    // 最大进度
    private final static int TOTAL_PROGRESS = 100;
    // 圆内颜色和半径
    private final static int CIRCLE_COLOR = 0xffffffff;
    private float CIRCLE_RADIUS = 20;
    // 圆环颜色和半径
    private final static int RING_COLOR = 0xfff57325;
    private final static float RING_WIDTH = 5;
    //显示文字
    private String mText = null;


    public CommonLoadingTimer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initData(context);
    }


    public CommonLoadingTimer(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initData(context);
    }


    public CommonLoadingTimer(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initData(context);
    }


    private void initData(Context context) {
        CIRCLE_RADIUS = context.getResources().getDimension(R.dimen.dip10);
        mText = context.getResources().getString(R.string.loading_interface_close);
        mRingRadius = CIRCLE_RADIUS + RING_WIDTH / 2;
        initPaint();
    }


    private void initPaint() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(CIRCLE_COLOR);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mRingPaint = new Paint();
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(RING_COLOR);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingPaint.setStrokeWidth(RING_WIDTH);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setARGB(255, 245, 115, 37);
        mTextPaint.setTextSize(CIRCLE_RADIUS * 2 / 3);

        FontMetrics fm = mTextPaint.getFontMetrics();
        mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);

    }


    @Override
    protected void onDraw(Canvas canvas) {

        mXCenter = getWidth() / 2;
        mYCenter = getHeight() / 2;

        canvas.drawCircle(mXCenter, mYCenter, CIRCLE_RADIUS, mCirclePaint);

        if (mProgress > 0) {
            RectF oval = new RectF();
            oval.left = (mXCenter - mRingRadius);
            oval.top = (mYCenter - mRingRadius);
            oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
            oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
            canvas.drawArc(oval, -90, ((float) mProgress / TOTAL_PROGRESS) * 360, false, mRingPaint);
        }
        
        mTxtWidth = mTextPaint.measureText(mText, 0, mText.length());
        canvas.drawText(mText, mXCenter - mTxtWidth / 2, mYCenter
                + mTxtHeight / 4, mTextPaint);
    }


    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();
    }
}
