package com.freeme.themeclub.wallpaper.view;

import com.freeme.themeclub.R;
import com.freeme.themeclub.wallpaper.util.MoveAnimation;
import com.freeme.themeclub.wallpaper.util.WallpaperUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedRotateDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class WallpaperView extends View {
	
    class WallpaperBitmap {
        
        private Rect canvasVisiableArea = null;
        public Bitmap determinateBitmap = null;
        private AnimatedRotateDrawable determinateFgDrawable = null;
        
        private int id = 0;
        private Drawable inderterminateDrawable = null;
        private String inderterminateText = null;
        private final float inderterminateTextSize;
        private boolean initialized = false;
        
        private int left = 0;
        private int right = 0;
        private int top = 0;
        private int bottom = 0;
        
        private float horizontalRatio = 0;
        private float verticalRatio = 0;
        
        private Rect mTmpRect = null;
        private WallpaperHandler scheduleHandler = null;
        private boolean showDeterminateFgImage = false;
        public boolean showIndeterminateBitmap = false;
        

        public WallpaperBitmap() {
        	final Resources r = getResources();
        	
            mTmpRect = new Rect();
            inderterminateDrawable = r.getDrawable(R.drawable.wallpaper_loading);
            inderterminateDrawable.setBounds(0, 0, inderterminateDrawable.getIntrinsicWidth(), 
            		inderterminateDrawable.getIntrinsicHeight());
            
            inderterminateText = r.getString(R.string.wallpaper_loading_text);
            inderterminateTextSize = r.getDimension(R.dimen.wallpaper_loading_text_size);
            
            showIndeterminateBitmap = true;
            
            determinateFgDrawable = (AnimatedRotateDrawable) getResources().getDrawable(R.drawable.loading);
            determinateFgDrawable.setBounds(0, 0, determinateFgDrawable.getIntrinsicWidth(), 
            		determinateFgDrawable.getIntrinsicHeight());
            scheduleHandler = new WallpaperHandler();
            determinateFgDrawable.setCallback(scheduleHandler);
        }
        
        private int getDefaultLeftPostion() {
            return (mContainingBitmapNeedWidth - getShowingWidth()) / 2;
        }

        private Rect getDrawingArea() {
            init();
            mTmpRect.left = (int) (0.5f + left * horizontalRatio);
            mTmpRect.right = (int) (0.5f + right * horizontalRatio);
            mTmpRect.top = (int) (0.5f + top * verticalRatio);
            mTmpRect.bottom = (int) (0.5f + bottom * verticalRatio);
            return mTmpRect;
        }

        private int getShowingWidth() {
            return mThumbnailMode ? mContainingBitmapNeedWidth : getWidth();
        }

        private void horizontallyMoveOffset(int offset) {
            if (offset != 0) {
                left += offset;
                right += offset;
                invalidate();
            }
        }

        private void init() {
            if (!initialized && getWidth() > 0) {
                left = getDefaultLeftPostion();
                right = left + getShowingWidth();
                top = 0;
                bottom = mContainingBitmapNeedHeight;
                initialized = true;
            }
        }

        public void draw(Canvas canvas, Rect visiableArea) {
            canvasVisiableArea = visiableArea;
            if (canvasVisiableArea.right > 0 
            		&& canvasVisiableArea.left < mScreenSize.x 
            		&& canvasVisiableArea.bottom > 0 
            		&& canvasVisiableArea.top < mScreenSize.y) {
            	if (determinateBitmap != null) {
                	canvas.drawBitmap(determinateBitmap, getDrawingArea(), canvasVisiableArea, null);
                    if (showDeterminateFgImage) {
                        canvas.save();
                        int dx = canvasVisiableArea.left 
                        		+ (canvasVisiableArea.width() - determinateFgDrawable.getIntrinsicWidth()) / 2;
                        int dy = canvasVisiableArea.top 
                        		+ (canvasVisiableArea.height() - determinateFgDrawable.getIntrinsicHeight()) / 2;
                        canvas.translate(dx, dy);
                        determinateFgDrawable.draw(canvas);
                        determinateFgDrawable.start();
                        canvas.restore();
                    }
                } else {
                	if (showIndeterminateBitmap)  {
                    	canvas.save();
                        int dw = inderterminateDrawable.getIntrinsicWidth();
                        int dh = inderterminateDrawable.getIntrinsicHeight();
                        int tw = canvasVisiableArea.left + (canvasVisiableArea.width() - dw) / 2;
                        int th = canvasVisiableArea.top + (canvasVisiableArea.height() - dh - 15) / 2;
                        canvas.translate(tw, th);
                        inderterminateDrawable.draw(canvas);
                        canvas.translate(0, 15);
                        Paint paint = new Paint();
                        paint.setTextSize(inderterminateTextSize);
                        paint.setColor(-1);
                        canvas.drawText(inderterminateText, 0, dh, paint);
                        canvas.restore();
                    }
                }
                if (!showDeterminateFgImage && determinateFgDrawable.isRunning()) {
                    determinateFgDrawable.stop();
                }
            }
        }

        public int getThumbnailHeight() {
            return (mContainingBitmapNeedHeight * getWidth()) / mContainingBitmapNeedWidth;
        }

        public void reset() {
            initialized = false;
            determinateFgDrawable.stop();
        }

        public void setBitmap(Bitmap b) {
            if (determinateBitmap != b) {
                horizontalRatio = 1;
                verticalRatio = 1;
                if (b != null 
                		&& (b.getWidth() != mContainingBitmapNeedWidth || b.getHeight() != mContainingBitmapNeedHeight)) {
                    horizontalRatio = (1.0F * (float) b.getWidth()) / mContainingBitmapNeedWidth;
                    verticalRatio = (1.0F * (float) b.getHeight()) / mContainingBitmapNeedHeight;
                    
                    Log.i("decoder", (new StringBuilder()).append("bitmap size is not match: (")
                    		.append(b.getWidth()).append(", ")
                    		.append(b.getHeight()).append(")")
                    		.append(" needed: (").append(mContainingBitmapNeedWidth)
                    		.append(", ").append(mContainingBitmapNeedHeight).append(")").toString());
                }
                determinateBitmap = b;
                reset();
            }
        }

        public void setDrawingArea(int l, int t, int r, int b) {
            left = l;
            top = t;
            right = r;
            bottom = b;
            invalidate();
        }

        public void udpateShowingArea(float movePercentFromCenter, boolean stopMove) {
            if(movePercentFromCenter > 1)movePercentFromCenter = 1;
            if(movePercentFromCenter < -1)movePercentFromCenter = -1;
            int newLeft = (int) (((float) (mContainingBitmapNeedWidth - getShowingWidth()) * (1.0f + movePercentFromCenter)) / 2.0f);
            int oldLeft = left;
            if (stopMove) {
                (new MoveAnimation() {
                    public void onMove(int moveStep) {
                        horizontallyMoveOffset(moveStep);
                    }
                }).start(newLeft - oldLeft);
            } else {
                horizontallyMoveOffset(newLeft - oldLeft);
            }
        }
        
        class WallpaperHandler extends Handler implements Drawable.Callback {
        	
        	public WallpaperHandler() {
	            if (getLooper() != Looper.getMainLooper()) {
	                throw new RuntimeException("You must create WallpaperHander in main thread.");
	            }
	        }
        	
	        public void invalidateDrawable(Drawable who) {
	            if (canvasVisiableArea != null) {
	                Rect rect = who.getBounds();
	                int dx = canvasVisiableArea.left + (canvasVisiableArea.width() - rect.width()) / 2;
	                int dy = canvasVisiableArea.top + (canvasVisiableArea.height() - rect.height()) / 2;
	                invalidate(dx, dy, dx + rect.width(), dy + rect.height());
	            }
	        }
	
	        public void scheduleDrawable(Drawable who, Runnable what, long when) {
	            if (who != null && what != null) {
	                postAtTime(what, who, when);
	            }
	        }
	
	        public void unscheduleDrawable(Drawable who, Runnable what) {
	            if (who != null && what != null) {
	                removeCallbacks(what, who);
	            }
	        }
        }
    }

    public static interface WallpaperSwitchListener {
        void switchNext();
        void switchNone();
        void switchPrevious();
    }

    
    private final WallpaperBitmap mCurrentWallpaper;
    private final WallpaperBitmap mNextWallpaper;
    private final WallpaperBitmap mPreviousWallpaper;
    
    private int mContainingBitmapNeedHeight = 0;
    private int mContainingBitmapNeedWidth = 0;
    private Rect mCurrentVisiableArea = null;
    private Point mScreenSize = null;
    private WallpaperSwitchListener mSwitchListener = null;
    private boolean mThumbnailMode = false;

    public WallpaperView(Context context) {
        this(context, null);
    }

    public WallpaperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPreviousWallpaper = new WallpaperBitmap();
        mCurrentWallpaper = new WallpaperBitmap();
        mNextWallpaper = new WallpaperBitmap();
        mCurrentVisiableArea = null;
    }

    private Rect getDefaultVisiableArea() {
        final int verticalPadding = mThumbnailMode 
        		? getThumbnailModeVerticalPadding() 
        		: 0;
        return new Rect(0, verticalPadding, 
        		getWidth(), getHeight() - verticalPadding);
    }

    private int getThumbnailModeVerticalPadding() {
        return (getHeight() - mCurrentWallpaper.getThumbnailHeight()) / 2;
    }

    private WallpaperBitmap getWallpaperBitmap(int pos) {
        WallpaperBitmap wb;
        if (pos < 0) {
        	wb = mPreviousWallpaper;
        } else if (pos > 0) {
        	wb = mNextWallpaper;
        } else {
        	wb = mCurrentWallpaper;
        }
        return wb;
    }

    private void resetWallpaperShowingState() {
        mPreviousWallpaper.reset();
        mCurrentWallpaper.reset();
        mNextWallpaper.reset();
        mCurrentVisiableArea = getDefaultVisiableArea();
    }

    public void autoSwitchCurreentWallpaper() {
        int moveOffset = mCurrentVisiableArea.left;
        if (moveOffset != 0) {
            int moveDistance = Math.abs(moveOffset);
            int width = mCurrentVisiableArea.width();
            int makeAnimDistance = moveDistance;
            int makeAnimDirection = -moveOffset / moveDistance;
            if (moveDistance > 0.15F * width 
            		&& (moveOffset > 0 && (mPreviousWallpaper.determinateBitmap != null || mPreviousWallpaper.showIndeterminateBitmap) 
            				|| moveOffset < 0 && (mNextWallpaper.determinateBitmap != null || mNextWallpaper.showIndeterminateBitmap))) {
            	makeAnimDistance = width - moveDistance;
            	makeAnimDirection = moveOffset / moveDistance;
            }
            (new MoveAnimation() {
            	@Override
                public void onFinish(int totalMoveOffset) {
                    int currentLeft = mCurrentVisiableArea.left;
                    if (currentLeft >= 0) {
                    	if (currentLeft > 0) {
                            if (mSwitchListener != null) {
                                mSwitchListener.switchPrevious();
                            }
                        } else if (mSwitchListener != null) {
                            mSwitchListener.switchNone();
                        }
                    } else {
                    	if (mSwitchListener != null) {
                            mSwitchListener.switchNext();
                    	}
                    }
                    resetWallpaperShowingState();
                    invalidate();
                }

                public void onMove(int offset) {
                    final Rect rect = mCurrentVisiableArea;
                    rect.left += offset;
                    rect.right += offset;
                    invalidate();
                }
            }).start(makeAnimDirection * makeAnimDistance);
        }
    }

    public int getUserGivenId(int pos) {
        return getWallpaperBitmap(pos).id;
    }

    public boolean hasBeenInitied() {
        return (mCurrentVisiableArea != null && getWidth() != 0);
    }

    public void horizontalMove(int distanceX) {
        if (distanceX != 0) {
            Rect rect = mCurrentVisiableArea;
            rect.left += distanceX;
            rect.right += distanceX;
            invalidate();
        }
    }

    public boolean isThumbnailScanMode() {
        return mThumbnailMode;
    }

    private Rect mTmpRect = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCurrentVisiableArea == null) {
            mCurrentVisiableArea = getDefaultVisiableArea();
        }
        if (0 + mCurrentVisiableArea.left > 0) {
            Rect rect = mTmpRect;
            rect.right = 0 + mCurrentVisiableArea.left;
            rect.left = rect.right - mCurrentVisiableArea.width();
            rect.top = mCurrentVisiableArea.top;
            rect.bottom = rect.top + mCurrentVisiableArea.height();
            mPreviousWallpaper.draw(canvas, rect);
        }
        mCurrentWallpaper.draw(canvas, mCurrentVisiableArea);
        if (0 + mCurrentVisiableArea.right <= getWidth()) {
            Rect rect1 = mTmpRect;
            rect1.left = 0 + mCurrentVisiableArea.right;
            rect1.right = rect1.left + mCurrentVisiableArea.width();
            rect1.top = mCurrentVisiableArea.top;
            rect1.bottom = rect1.top + mCurrentVisiableArea.height();
            mNextWallpaper.draw(canvas, rect1);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics dm = WallpaperUtils.getScreenDisplayMetrics(mContext);
        setMeasuredDimension(dm.widthPixels, dm.heightPixels);
    }

    public void regeisterSwitchListener(WallpaperSwitchListener l) {
        mSwitchListener = l;
    }

    public void reset() {
        setBitmapInfo(0, null, 0x80000000, false, false);
        setBitmapInfo(1, null, 0x80000000, false, false);
        setBitmapInfo(-1, null, 0x80000000, false, false);
    }

    public void setBitmapInfo(int pos, Bitmap b, int userGivenId, 
    		boolean showIndeterminate, boolean showDeterminateFg) {
        WallpaperBitmap wb = getWallpaperBitmap(pos);
        wb.setBitmap(b);
        wb.showIndeterminateBitmap = showIndeterminate;
        wb.showDeterminateFgImage = showDeterminateFg;
        wb.id = userGivenId;
    }

    public void setContainingBitmapSize(int width, int height) {
        mContainingBitmapNeedWidth = width;
        mContainingBitmapNeedHeight = height;
        if (mScreenSize == null) {
            mScreenSize = new Point();
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            mScreenSize.x = dm.widthPixels;
            mScreenSize.y = dm.heightPixels;
        }
    }

    public void setScanMode(boolean showThumbnail) {
        if (showThumbnail != mThumbnailMode) {
            int verticalMoveOffset = getThumbnailModeVerticalPadding();
            if (mThumbnailMode) {
            	verticalMoveOffset = -verticalMoveOffset;
            }
            if (verticalMoveOffset != 0) {
                (new MoveAnimation() {
                	@Override
                    public void onFinish(int totalMoveOffset) {
                        mThumbnailMode = !mThumbnailMode;
                        resetWallpaperShowingState();
                        invalidate();
                    }

                    public void onMove(int offset) {
                        final Rect rect = mCurrentVisiableArea;
                        rect.top += offset;
                        rect.bottom -= offset;
                        int bH = mContainingBitmapNeedHeight;
                        int bW = mContainingBitmapNeedWidth;
                        int vH = rect.height();
                        int vW = rect.width();
                        int l = (bW - (vW * bH) / vH) / 2;
                        int r = l + (vW * bH) / vH;
                        mCurrentWallpaper.setDrawingArea(l, 0, r, bH);
                    }
                }).start(verticalMoveOffset, 5, 10);
            }
        }
    }

    public boolean showingDeterminateFg(int pos) {
        WallpaperBitmap wb = getWallpaperBitmap(pos);
        return (wb.determinateBitmap != null && wb.showDeterminateFgImage);
    }

    public void updateCurrentWallpaperShowingArea(float movePercentFromCenter, boolean stopMove) {
        mCurrentWallpaper.udpateShowingArea(movePercentFromCenter, stopMove);
    }
}