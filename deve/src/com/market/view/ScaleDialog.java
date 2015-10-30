package com.market.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.zhuoyi.market.R;

public class ScaleDialog extends Dialog {

	private Animation mScaleIn,mScaleOut;
	private Context mContext;
	private View mView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView.startAnimation(mScaleOut);
	}

	
	
	@Override
	public void dismiss() {
		mView.startAnimation(mScaleIn);
		super.dismiss();
	}


	@Override
	public void cancel() {
		mView.startAnimation(mScaleIn);
		super.cancel();
	}



	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		mView = view;
	}



	public ScaleDialog(Context context, int theme) {
		super(context, theme);
		mContext = context;
		mScaleIn = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_in);  
		mScaleOut = AnimationUtils.loadAnimation(context, R.anim.dialog_scale_out); 
	
		mScaleIn.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		mScaleOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	
}
