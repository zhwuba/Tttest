package com.freeme.themeclub.fillableloaders;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.freeme.themeclub.core.R;
import com.freeme.themeclub.fillableloaders.listener.OnStateChangeListener;

public class FillableLoaderPage2 extends FrameLayout implements OnStateChangeListener, ResettableView{

    private FillableLoader fillableLoader;

    public FillableLoaderPage2(Context context) {
        super(context);
    }

    public FillableLoaderPage2(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.fragment_fillable_loader_first_page, this);
        fillableLoader = (FillableLoader)findViewById(R.id.fillableLoader);
        fillableLoader.setZOrderOnTop(true);
        setupFillableLoader(0);
        reset();
    }

    private void setupFillableLoader(int pageNum) {
        fillableLoader.setSvgPath(Paths.FREEMEOS);
        fillableLoader.setOnStateChangeListener(this);
    }

    @Override
    public void onStateChange(int state) {
        //        if(state == State.FINISHED){
        //            System.exit(0);
        //        }
    }

    @Override 
    public void reset() {
        fillableLoader.reset();

        //We wait a little bit to start the animation, to not contaminate the drawing effect
        //by the activity creation animation.
        fillableLoader.postDelayed(new Runnable() {
            @Override 
            public void run() {
                fillableLoader.start();
            }
        }, 250);
    }

}
