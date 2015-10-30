package com.zhuoyi.market.cleanTrash;

import java.util.ArrayList;

import com.zhuoyi.market.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;

public class TrashAnimView extends View {
    
    private static final long ANIM_FRAME_MILLIS = 20;
    private static final float START_ANIM_ANGLE = 275;
    private static final float NORMAL_ARC_ANGLE = 350;
    private static final float MIN_ARC_ANGLE = 60;
    
    private Context mContext;
    
    private float animCircleRadius;
    private Paint animPaint;
    private Paint animPointPaint;
    private float startAngle;
    private float animArcAngle;
    private float animPaintWidth;
    
    private int mGreenColor;
    private int mBlackColor;
    private int mWhiteColor;
    private float mSizeTextSize;
    private float mDesTextSize;
    private float mTextInterval;
    
    private String checkingStr;
    private String adviceCleanStr;
    private String cleanningStr;
    private String trashSizeStr;
    private long sizeBytes;
    
    private Paint mPaint;
    
//    private Bitmap circleBitMap;
    private Bitmap cleanUpBitMap;
    private Bitmap trashBitMap;
    
    private Bitmap tmpCircleBitMap;
    
    private float circleBitWidth;
//    private float circleBitHeight;
    
//    private int circleDegree;
    
    public static final int STATUS_CHECKING = 1;
    public static final int STATUS_SELECT_TRASH = 2;
    public static final int STATUS_CLEANNING = 3;
    public static final int STATUS_CLEAN_UP = 4;
    
    private boolean animSizeText = false;
    
    public int displayStatus;
    
    public TrashAnimView(Context context) {
        this(context, null);
    }
    
    public TrashAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrashAnimView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }
    
    
    private void init() {
        mGreenColor = mContext.getResources().getColor(R.color.trash_text_green);
        mBlackColor = mContext.getResources().getColor(R.color.trash_title_text);
        mSizeTextSize = mContext.getResources().getDimension(R.dimen.trash_anim_size_text_size);
        mDesTextSize = mContext.getResources().getDimension(R.dimen.trash_title_text_size);
        mTextInterval = mContext.getResources().getDimension(R.dimen.trash_anim_view_text_interval);
        circleBitWidth = mContext.getResources().getDimension(R.dimen.trash_anim_view_circle_width);
        mWhiteColor = Color.parseColor("#FFFFFF");
        
        checkingStr = mContext.getString(R.string.trash_checking);
        adviceCleanStr = mContext.getString(R.string.trash_advice_clean);
        cleanningStr = mContext.getString(R.string.trash_cleanning);
        trashSizeStr = "0M";
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Style.FILL);
        
//        circleBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.trash_anim_circle);
        cleanUpBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.trash_anim_clean_up);
        trashBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.trash_anim_trash);
        
//        circleBitWidth = circleBitMap.getWidth();
//        circleBitHeight = circleBitMap.getHeight();
//        circleDegree = 0;
        initAnimPaint();
        startAngle = START_ANIM_ANGLE;
        animArcAngle = NORMAL_ARC_ANGLE;
        
        displayStatus = STATUS_CHECKING;
        
        animCircleRadius = circleBitWidth / 2;
    }
    
    
    public void setTrashSize(long size) {
        sizeBytes = size;
        trashSizeStr = getAnimViewSizeText(size);
        initSizeAnimArray();
    }
    
    
    private ArrayList<String> mSizeAnimStrArray = new ArrayList<String>();
    
    private void initSizeAnimArray() {
        if (!animSizeText) {
            return;
        }
        animSizeText = false;
        
        mSizeAnimStrArray.clear();
        long frameSize = sizeBytes / 20;      //20 frame
        long animSize = frameSize;
        String lastFrameSizeStr = null;
        String frameSizeStr = null;
        while (animSize <= sizeBytes) {
            frameSizeStr = getAnimViewSizeText(animSize);
            if (lastFrameSizeStr == null || !frameSizeStr.equals(lastFrameSizeStr)) {
                mSizeAnimStrArray.add(frameSizeStr);
            }
            
            animSize += frameSize;
        }
    }
    
    
    private String getAnimViewSizeText(long size) {
        String sizeStr = null;
        if (size < TrashControl.KB_BYTES) {
            sizeStr = Long.toString(size) + "B";
        } else if (size < TrashControl.MB_BYTES) {
            long kbSize = size / TrashControl.KB_BYTES;
            sizeStr = Long.toString(kbSize) + "K";
        } else if (size < TrashControl.GB_BYTES) {
            long mbSize = size / TrashControl.MB_BYTES;
            sizeStr = Long.toString(mbSize) + "M";
        } else {
            float gbSize = ((float)(size * 100 / TrashControl.GB_BYTES)) / 100;
            sizeStr = Float.toString(gbSize) + "G";
        }
        
        return sizeStr;
    }
    
    
    public void displayCheckingAnim() {
        displayStatus = STATUS_CHECKING;
//        circleDegree = 0;
        startAngle = START_ANIM_ANGLE;
        animArcAngle = NORMAL_ARC_ANGLE;
        
        postInvalidate();
    }
    
    
    public void displaySelcectStatus(long trashSize) {
        displayStatus = STATUS_SELECT_TRASH;
//        startAngle = START_ANIM_ANGLE;
//        animArcAngle = NORMAL_ARC_ANGLE;
        
        animSizeText = true;
        setTrashSize(trashSize);
        postInvalidate();
    }
    
    
    public void displayCleanningAnim() {
        displayStatus = STATUS_CLEANNING;
//      circleDegree = 0;
        startAngle = START_ANIM_ANGLE;
        animArcAngle = NORMAL_ARC_ANGLE;
        
        postInvalidate();
    }
    
    
    public void displayCleanUpStatus() {
        displayStatus = STATUS_CLEAN_UP;
//        startAngle = START_ANIM_ANGLE;
//        animArcAngle = NORMAL_ARC_ANGLE;
        
        postInvalidate();
    }
    
    
    private boolean animArcAngleAddFlag = false;
    
    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
//            circleDegree += 20;
//            if (circleDegree >= 360) {
//                circleDegree -= 360;
//            }
            
            if (animArcAngle >= NORMAL_ARC_ANGLE - 20) {
                animArcAngleAddFlag = false;
            } else if (animArcAngle <= MIN_ARC_ANGLE) {
                animArcAngleAddFlag = true;
            }
            
            
            if (animArcAngleAddFlag) {
                animArcAngle += 5;
                startAngle += 5;
            } else {
                animArcAngle -= 5;
                startAngle += 15;
            }
            
//            startAngle += 20;
            if (startAngle >= 360) {
                startAngle -= 360;
            }
            
            postInvalidate();
        }
    };
    
    
    private void checkCircleFull() {
        if (startAngle != START_ANIM_ANGLE || animArcAngle != NORMAL_ARC_ANGLE) {
            this.postDelayed(fillRunnable, ANIM_FRAME_MILLIS * 2);
        }
    }
    
    
    private Runnable fillRunnable = new Runnable() {
        @Override
        public void run() {
            animArcAngle += 10;
            if (animArcAngle > NORMAL_ARC_ANGLE) {
                animArcAngle = NORMAL_ARC_ANGLE;
            }
            
            float pointAngle = startAngle + animArcAngle + 5;
            if (pointAngle >= 360) {
                pointAngle -= 360;
            }
            
            if (pointAngle == 270) {
                if (startAngle != START_ANIM_ANGLE) {
                    startAngle -= 10;
                    if (startAngle < 0) {
                        startAngle += 360;
                    }
                    
                    if (startAngle == (START_ANIM_ANGLE - 5)) {
                        startAngle = START_ANIM_ANGLE;
                    }
                }
            } else if (startAngle != START_ANIM_ANGLE) {
                startAngle += 10;
                if (startAngle >= 360) {
                    startAngle -= 360;
                } else if (startAngle == (START_ANIM_ANGLE + 5)) {
                    startAngle = START_ANIM_ANGLE;
                }
            }
            
            postInvalidate();
        }
    };
    

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;
        
        //画背景白色实心圆
        mPaint.setColor(mWhiteColor);
        canvas.drawCircle(centerX, centerY, centerY, mPaint);
        
        //画绿色image图片
        if (displayStatus == STATUS_CHECKING) {
            drawCircle(canvas, centerX, centerY);
//            Bitmap bmp = rotateBitmap(circleBitMap, circleDegree);
//            canvas.drawBitmap(bmp, centerX - (bmp.getWidth() / 2), centerY - (bmp.getHeight() / 2), mPaint);
            
            //画size字符串
            mPaint.setColor(mGreenColor);
            mPaint.setTextSize(mSizeTextSize);
            FontMetrics fontMetrics = mPaint.getFontMetrics();
            float desTextCenterY = centerY - (fontMetrics.descent + fontMetrics.ascent) / 2 + mTextInterval;
            canvas.drawText(trashSizeStr, centerX, centerY - (fontMetrics.descent + fontMetrics.ascent) / 2, mPaint);
            //画描述字符串
            mPaint.setColor(mBlackColor);
            mPaint.setTextSize(mDesTextSize);
            fontMetrics = mPaint.getFontMetrics();
            desTextCenterY -= fontMetrics.descent + fontMetrics.ascent;
            canvas.drawText(checkingStr, centerX, desTextCenterY, mPaint);
            //循环
            postDelayed(animRunnable, ANIM_FRAME_MILLIS);
            
        } else if (displayStatus == STATUS_SELECT_TRASH) {
//            startAngle = START_ANIM_ANGLE;
//            animArcAngle = NORMAL_ARC_ANGLE;
            drawCircle(canvas, centerX, centerY);
//            canvas.drawBitmap(circleBitMap, centerX - (circleBitWidth / 2), centerY - (circleBitHeight / 2), mPaint);
            //画size字符串
            mPaint.setColor(mGreenColor);
            mPaint.setTextSize(mSizeTextSize);
            FontMetrics fontMetrics = mPaint.getFontMetrics();
            float desTextCenterY = centerY - (fontMetrics.descent + fontMetrics.ascent) / 2 + mTextInterval;
            if (mSizeAnimStrArray.size() == 0) {
                canvas.drawText(trashSizeStr, centerX, centerY - (fontMetrics.descent + fontMetrics.ascent) / 2, mPaint);
            } else {
                canvas.drawText(mSizeAnimStrArray.remove(0), centerX, centerY - (fontMetrics.descent + fontMetrics.ascent) / 2, mPaint);
                postInvalidateDelayed(ANIM_FRAME_MILLIS);
            }
            
            //画描述字符串
            mPaint.setColor(mBlackColor);
            mPaint.setTextSize(mDesTextSize);
            fontMetrics = mPaint.getFontMetrics();
            desTextCenterY -= fontMetrics.descent + fontMetrics.ascent;
            canvas.drawText(adviceCleanStr, centerX, desTextCenterY, mPaint);
            
            checkCircleFull();
            
        } else if (displayStatus == STATUS_CLEANNING) {
            Animation anim = getAnimation();
            if (anim != null && !anim.hasEnded()) {
                startAngle = START_ANIM_ANGLE;
                animArcAngle = NORMAL_ARC_ANGLE;
//                canvas.drawBitmap(circleBitMap, centerX - (circleBitWidth / 2), centerY - (circleBitHeight / 2), mPaint);
            } else {
                //循环
//                Bitmap bmp = rotateBitmap(circleBitMap, circleDegree);
//                canvas.drawBitmap(bmp, centerX - (bmp.getWidth() / 2), centerY - (bmp.getHeight() / 2), mPaint);
            }
            drawCircle(canvas, centerX, centerY);
            //画清理图片
            float bmpCenterX = centerX - (trashBitMap.getWidth() / 2);
            float bmpCenterY = centerY - (trashBitMap.getHeight() / 2);
            float desTextCenterY = centerY + (trashBitMap.getHeight() / 2) + mTextInterval;
            canvas.drawBitmap(trashBitMap, bmpCenterX, bmpCenterY, mPaint);
            //画描述字符串
            mPaint.setColor(mBlackColor);
            mPaint.setTextSize(mDesTextSize);
            FontMetrics fontMetrics = mPaint.getFontMetrics();
            desTextCenterY -= fontMetrics.descent + fontMetrics.ascent;
            canvas.drawText(cleanningStr, centerX, desTextCenterY, mPaint);
            
            postDelayed(animRunnable, ANIM_FRAME_MILLIS * 2);
            
        } else if (displayStatus == STATUS_CLEAN_UP) {
//            startAngle = START_ANIM_ANGLE;
//            animArcAngle = NORMAL_ARC_ANGLE;
            drawCircle(canvas, centerX, centerY);
//            canvas.drawBitmap(circleBitMap, centerX - (circleBitWidth / 2), centerY - (circleBitHeight / 2), mPaint);
            //画清理完毕图片
            float bmpCenterX = centerX - (cleanUpBitMap.getWidth() / 2);
            float bmpCenterY = centerY - (cleanUpBitMap.getHeight() / 2);
            canvas.drawBitmap(cleanUpBitMap, bmpCenterX, bmpCenterY, mPaint);
            
            checkCircleFull();
        }
        
//        drawCircle(canvas, centerX, centerY);
    }
    
    
//    public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
//        if (degrees == 0 || null == bitmap) {
//            return bitmap;
//        }
//        recycleBmp(tmpCircleBitMap);
//        
//        Matrix matrix = new Matrix();
//        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
//        tmpCircleBitMap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        return tmpCircleBitMap;
//    }
    
    
    public void recycle() {
//        recycleBmp(circleBitMap);
        recycleBmp(cleanUpBitMap);
        recycleBmp(trashBitMap);
        recycleBmp(tmpCircleBitMap);
        setBackgroundResource(0);
        
//        circleBitMap = null;
        cleanUpBitMap = null;
        trashBitMap = null;
        tmpCircleBitMap = null;
    }
    
    
    private void recycleBmp(Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }
    
    
    private void drawCircle(Canvas canvas, int centerX, int centerY) {
        RectF rect = new RectF(centerX - animCircleRadius + animPaintWidth / 2,
                               centerY - animCircleRadius + animPaintWidth / 2,
                               centerX + animCircleRadius - animPaintWidth / 2,
                               centerY + animCircleRadius - animPaintWidth / 2);
            canvas.drawArc(rect, startAngle, animArcAngle, false, animPaint);
        float pointAngle = startAngle + animArcAngle + 5;
        if (pointAngle >= 360) {
            pointAngle -= 360;
        }
        float mathAngle = pointAngle % 90;
        
        float pointSin = (float)Math.sin(Math.toRadians(mathAngle)) * (animCircleRadius - animPaintWidth / 2);
        float pointCos = (float)Math.cos(Math.toRadians(mathAngle)) * (animCircleRadius - animPaintWidth / 2);
        
        float pointCenterX = 0;
        float pointCenterY = 0;
        if (pointAngle >= 0 && pointAngle < 90) {
            pointCenterY = centerY + pointSin;
            pointCenterX = centerX + pointCos;
            
        } else if (pointAngle >= 90 && pointAngle < 180) {
            pointCenterY = centerY + pointCos;
            pointCenterX = centerX - pointSin;
            
        } else if (pointAngle >= 180 && pointAngle < 270) {
            pointCenterY = centerY - pointSin;
            pointCenterX = centerX - pointCos;
            
        } else if (pointAngle >= 270 && pointAngle < 360) {
            pointCenterY = centerY - pointCos;
            pointCenterX = centerX + pointSin;
            
        }
        
        canvas.drawCircle(pointCenterX, pointCenterY, animPaintWidth / 2, animPointPaint);
        
    }
    
    
    private void initAnimPaint() {
        SweepGradient sg = new SweepGradient(animArcAngle, animArcAngle, new int[] {this.mGreenColor, Color.parseColor("#FFFFFFFF")}, null);
        animPaintWidth = mContext.getResources().getDimension(R.dimen.trash_anim_view_anim_paint_width);
        animPaint = new Paint();
        animPaint.setAntiAlias(true);
        animPaint.setStrokeJoin(Paint.Join.ROUND);
        animPaint.setStrokeCap(Paint.Cap.ROUND);
        animPaint.setStyle(Style.STROKE);
        animPaint.setStrokeWidth(animPaintWidth);
        animPaint.setColor(mGreenColor);
        animPaint.setShader(sg);
        
        animPointPaint = new Paint();
        animPointPaint.setAntiAlias(true);
        animPointPaint.setStyle(Style.FILL);
        animPointPaint.setColor(mGreenColor);
        
    }
}
