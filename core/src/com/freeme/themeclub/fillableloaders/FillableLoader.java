/*
 * Copyright (C) 2015 Jorge Castillo Pérez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.freeme.themeclub.fillableloaders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.freeme.themeclub.fillableloaders.attributes.AttributeExtractorImpl;
import com.freeme.themeclub.fillableloaders.clippingtransforms.ClippingTransform;
import com.freeme.themeclub.fillableloaders.clippingtransforms.PlainClippingTransform;
import com.freeme.themeclub.fillableloaders.listener.OnStateChangeListener;
import com.freeme.themeclub.fillableloaders.svg.ConstrainedSvgPathParser;
import com.freeme.themeclub.fillableloaders.svg.SvgPathParser;
import com.freeme.themeclub.fillableloaders.utils.MathUtil;

import java.text.ParseException;

/**
 * This view is used to draw a fillable progress icon working with an SVG Path. The border
 * silhouette will be the one obtained from the path.
 *
 * The library has been motivated by the iOS project given below.
 *
 * @see <a href="https://github.com/poolqf/FillableLoaders.">poolqf/FillableLoaders</a>
 */
public class FillableLoader extends SurfaceView implements SurfaceHolder.Callback{

    private int strokeColor, fillColor, strokeWidth;
    private int originalWidth, originalHeight;
    private int strokeDrawingDuration, fillDuration;
    private ClippingTransform clippingTransform;

    private String svgPath;
    private PathData pathData;
    private Paint dashPaint;
    private Paint fillPaint;
    private int drawingState;
    private long initialTime;

    private int viewWidth;
    private int viewHeight;

    private Interpolator animInterpolator;
    private OnStateChangeListener stateChangeListener;

    private SurfaceHolder holder;
    private MyThread myThread;

    /**
     * Constructor for the {@link FillableLoaderBuilder} class.
     */
    FillableLoader(ViewGroup parent, ViewGroup.LayoutParams params, int strokeColor, int fillColor,
            int strokeWidth, int originalWidth, int originalHeight, int strokeDrawingDuration,
            int fillDuration, ClippingTransform transform, String svgPath) {

        super(parent.getContext());
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.strokeWidth = strokeWidth;
        this.strokeDrawingDuration = strokeDrawingDuration;
        this.fillDuration = fillDuration;
        this.clippingTransform = transform;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.svgPath = svgPath;

        init();
        parent.addView(this, params);

        holder = this.getHolder();
        holder.addCallback(this);
        myThread = new MyThread(holder);
    }

    public FillableLoader(Context context) {
        super(context);
        init();
    }

    public FillableLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();

        holder = this.getHolder();
        holder.addCallback(this);
        myThread = new MyThread(holder);
    }

    public FillableLoader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        init();

        holder = this.getHolder();
        holder.addCallback(this);
        myThread = new MyThread(holder);
    }

    private void initAttrs(AttributeSet attrs) {
        AttributeExtractorImpl.Builder extractorBuilder = new AttributeExtractorImpl.Builder();
        AttributeExtractorImpl extractor = extractorBuilder.with(getContext()).with(attrs).build();
        fillColor = extractor.getFillColor();
        strokeColor = extractor.getStrokeColor();
        strokeWidth = extractor.getStrokeWidth();
        originalWidth = extractor.getOriginalWidth();
        originalHeight = extractor.getOriginalHeight();
        strokeDrawingDuration = extractor.getStrokeDrawingDuration();
        fillDuration = extractor.getFillDuration();
        clippingTransform = extractor.getClippingTransform();

        extractor.recycleAttributes();
    }

    private void init() {
        drawingState = State.NOT_STARTED;

        initDashPaint();
        initFillPaint();

        animInterpolator = new DecelerateInterpolator();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initDashPaint() {
        dashPaint = new Paint();
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setAntiAlias(true);
        dashPaint.setStrokeWidth(strokeWidth);
        dashPaint.setColor(strokeColor);
    }

    private void initFillPaint() {
        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);
    }

    public void start() {
        checkRequirements();
        initialTime = System.currentTimeMillis();
        changeState(State.TRACE_STARTED);
        myThread.isRun = true;
        myThread.start();
    }

    private void checkRequirements() {
        checkOriginalDimensions();
        checkPath();
    }

    private void checkOriginalDimensions() {
        if (originalWidth <= 0 || originalHeight <= 0) {
            throw new IllegalArgumentException(
                    "You must provide the original image dimensions in order map the coordinates properly.");
        }
    }

    private void checkPath() {
        if (pathData == null) {
            throw new IllegalArgumentException(
                    "You must provide a not empty path in order to draw the view properly.");
        }
    }

    public void reset() {
        initialTime = 0;
        changeState(State.NOT_STARTED);
    }

    public void setToFinishedFrame() {
        initialTime = 1;
        changeState(State.FINISHED);
    }

    //    @Override 
    //    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    //        super.onSizeChanged(w, h, oldw, oldh);
    //        Log.e("yzy", "w = "+w);
    //        viewWidth = w;
    //        viewHeight = h;
    //        buildPathData();
    //    }

    //    @Override 
    //    protected void onDraw(Canvas canvas) {
    //        super.onDraw(canvas);
    //        if (!hasToDraw()) {
    //            return;
    //        }
    //
    //        long elapsedTime = System.currentTimeMillis() - initialTime;
    //
    //        float phase = MathUtil.constrain(0, 1, elapsedTime * 1f / strokeDrawingDuration);
    //        float distance = animInterpolator.getInterpolation(phase) * pathData.length;
    //
    //        dashPaint.setPathEffect(getDashPathForDistance(distance));
    //        canvas.drawPath(pathData.path, dashPaint);
    //
    //        if (isStrokeTotallyDrawn(elapsedTime)) {
    //            if (drawingState < State.FILL_STARTED) {
    //                changeState(State.FILL_STARTED);
    //            }
    //
    //            float fillPhase = MathUtil.constrain(
    //                    0, 1, (elapsedTime - strokeDrawingDuration) * 1f / fillDuration);
    //
    //            clippingTransform.transform(canvas, fillPhase, this);
    //            canvas.drawPath(pathData.path, fillPaint);
    //        }
    //
    //        if (hasToKeepDrawing(elapsedTime)) {
    //            ViewCompat.postInvalidateOnAnimation(this);
    //        } else {
    //            changeState(State.FINISHED);
    //        }
    //    }

    public boolean hasToDraw() {
        return !(drawingState == State.NOT_STARTED || pathData == null);
    }

    private PathEffect getDashPathForDistance(float distance) {
        return new DashPathEffect(new float[] { distance, pathData.length }, 0);
    }

    public boolean isStrokeTotallyDrawn(long elapsedTime) {
        return elapsedTime > strokeDrawingDuration;
    }

    private boolean hasToKeepDrawing(long elapsedTime) {
        return elapsedTime < strokeDrawingDuration + fillDuration;
    }

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        stateChangeListener = onStateChangeListener;
    }

    private void changeState(int state) {
        if (drawingState == state) {
            return;
        }

        drawingState = state;
        if (stateChangeListener != null) {
            stateChangeListener.onStateChange(state);
        }
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setOriginalDimensions(int originalWidth, int originalHeight) {
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
    }

    public void setStrokeDrawingDuration(int duration) {
        this.strokeDrawingDuration = duration;
    }

    public void setFillDuration(int duration) {
        this.fillDuration = duration;
    }

    public void setClippingTransform(ClippingTransform transform) {
        this.clippingTransform = transform == null ? new PlainClippingTransform() : transform;
    }

    public void setSvgPath(String svgPath) {
        if (svgPath == null || svgPath.length() == 0) {
            throw new IllegalArgumentException(
                    "You must provide a not empty path in order to draw the view properly.");
        }
        this.svgPath = svgPath;
        buildPathData();
    }

    private void buildPathData() {
        SvgPathParser parser = getPathParser();
        pathData = new PathData();
        try {
            pathData.path = parser.parsePath(svgPath);
        } catch (ParseException e) {
            pathData.path = new Path();
        }

        PathMeasure pm = new PathMeasure(pathData.path, true);
        while (true) {
            pathData.length = Math.max(pathData.length, pm.getLength());
            if (!pm.nextContour()) {
                break;
            }
        }
    }

    private SvgPathParser getPathParser() {
        ConstrainedSvgPathParser.Builder builder = new ConstrainedSvgPathParser.Builder();
        return builder.originalWidth(originalWidth)
                .originalHeight(originalHeight)
                .viewWidth(viewWidth)
                .viewHeight(viewHeight)
                .build();
    }

    class MyThread extends Thread{
        private SurfaceHolder holder;
        public boolean isRun ;
        public  MyThread(SurfaceHolder holder){
            this.holder =holder; 
            isRun = true;
        }
        @Override
        public void run(){
            if (!hasToDraw()) {
                return;
            }
            Canvas canvas = null;
            long elapsedTime;
            float phase;
            float distance;
            float fillPhase;
            while(isRun){
                try{
                    synchronized (holder){
                        elapsedTime = System.currentTimeMillis() - initialTime;

                        phase = MathUtil.constrain(0, 1, elapsedTime * 1f / strokeDrawingDuration);
                        distance = animInterpolator.getInterpolation(phase) * pathData.length;
                        canvas = holder.lockCanvas();
                        dashPaint.setPathEffect(getDashPathForDistance(distance));
                        canvas.drawPath(pathData.path, dashPaint);

                        if (isStrokeTotallyDrawn(elapsedTime)) {
                            if (drawingState < com.freeme.themeclub.fillableloaders.State.FILL_STARTED) {
                                changeState(com.freeme.themeclub.fillableloaders.State.FILL_STARTED);
                            }

                            fillPhase = MathUtil.constrain(
                                    0, 1, (elapsedTime - strokeDrawingDuration) * 1f / fillDuration);

                            clippingTransform.transform(canvas, fillPhase, FillableLoader.this);
                            canvas.drawPath(pathData.path, fillPaint);
                        }

                        if (hasToKeepDrawing(elapsedTime)) {
                            holder.unlockCanvasAndPost(canvas);
                        } else {
                            changeState(com.freeme.themeclub.fillableloaders.State.FINISHED);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        viewWidth = 350;
        viewHeight = 350;
        buildPathData();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        myThread.isRun = false;
    }
}
