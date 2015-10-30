package com.market.view;  
  
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.zhuoyi.market.R;
      
public class AnimTextView extends TextSwitcher implements ViewSwitcher.ViewFactory {  

    public Context mContext;  

    private Rotate3dAnimation mInUp;  
    private Rotate3dAnimation mOutUp;  
      
//    private Rotate3dAnimation mInDown;  
//    private Rotate3dAnimation mOutDown;  
      
    public AnimTextView(Context context) {  
        this(context, null);  
        // TODO Auto-generated constructor stub  
    }  
  
    public AnimTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        // TODO Auto-generated constructor stub  
        mContext = context;  
        init();  
    }  
  
    private void init() {  
        // TODO Auto-generated method stub  
        setFactory(this);  
        mInUp = createAnim(0, 0 , true, true);  
        mOutUp = createAnim(0, 0, false, true);  
//        mInDown = createAnim(0, 0 , true , false);  
//        mOutDown = createAnim(0, 0, false, false);  

        setInAnimation(mInUp);  
        setOutAnimation(mOutUp);  
    } 
    
    
    public void releaseRes () {
        clearAnimation();
        mInUp = null;
        mOutUp = null;
        mContext = null;
    }
    
      
    private Rotate3dAnimation createAnim(float start, float end, boolean turnIn, boolean turnUp){  
        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, turnIn, turnUp);  
        rotation.setDuration(400);  
        rotation.setFillAfter(false);  
        rotation.setInterpolator(new AccelerateInterpolator());  
        return rotation;  
    }  
   
    @Override  
    public View makeView() {  
        // TODO Auto-generated method stub  
    	
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		TextView v = (TextView)tLayoutInflater.inflate(R.layout.anim_textview, null);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        v.setLayoutParams(lp);
        return v;  
    }  

//        public void previous(){  
//            if(getInAnimation() != mInDown){  
//                setInAnimation(mInDown);  
//            }  
//            if(getOutAnimation() != mOutDown){  
//                setOutAnimation(mOutDown);  
//            }  
//        }  
  
        public void next(){  
            if(getInAnimation() != mInUp){  
                setInAnimation(mInUp);  
            }  
            if(getOutAnimation() != mOutUp){  
                setOutAnimation(mOutUp);  
            }  
        }  
          
         class Rotate3dAnimation extends Animation {  
                private final float mFromDegrees;  
                private final float mToDegrees;  
                private float mCenterX;  
                private float mCenterY;  
                private final boolean mTurnIn;  
                private final boolean mTurnUp;  
                private Camera mCamera;  
      
                public Rotate3dAnimation(float fromDegrees, float toDegrees, boolean turnIn, boolean turnUp) {  
                    mFromDegrees = fromDegrees;  
                    mToDegrees = toDegrees;  
                    mTurnIn = turnIn;  
                    mTurnUp = turnUp;  
                }  
      
                @Override  
                public void initialize(int width, int height, int parentWidth, int parentHeight) {  
                    super.initialize(width, height, parentWidth, parentHeight);  
                    mCamera = new Camera();  
                    mCenterY = getHeight() / 2;  
                    mCenterX = getWidth() / 2;  
                }  
                  
                @Override  
                protected void applyTransformation(float interpolatedTime, Transformation t) {  
                    final float fromDegrees = mFromDegrees;  
                    float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);  
      
                    final float centerX = mCenterX ;  
                    final float centerY = mCenterY ;  
                    final Camera camera = mCamera;  
                    final int derection = mTurnUp ? 1: -1;  
      
                    final Matrix matrix = t.getMatrix();  
      
                    camera.save();  
                    if (mTurnIn) {  
                        camera.translate(0.0f, derection *mCenterY * (interpolatedTime - 1.0f), 0.0f);  
                    } else {  
                        camera.translate(0.0f, derection *mCenterY * (interpolatedTime), 0.0f);  
                    }  
                    camera.rotateX(degrees);  
                    camera.getMatrix(matrix);  
                    camera.restore();  
      
                    matrix.preTranslate(-centerX, -centerY);  
                    matrix.postTranslate(centerX, centerY);  
                }  
         }  
    }  
