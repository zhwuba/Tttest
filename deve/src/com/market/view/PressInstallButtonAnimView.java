package com.market.view;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.zhuoyi.market.R;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PressInstallButtonAnimView extends RelativeLayout {

	private List<ImageView> mImageView = new ArrayList<ImageView>();
	private Context mContext = null;
	private final int ANIM_TIME = 800;
	private final int SET_VIEW_GONE = 0;
	private RelativeLayout mySelf = null;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message message) {
			if(message.what == SET_VIEW_GONE){
				mySelf.removeView(mImageView.get(0));
				mImageView.remove(0);
				if(mImageView.size() == 0){
					setMyVisibility(false);
				}
			}
		}
	};
	
	public PressInstallButtonAnimView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	
	public PressInstallButtonAnimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	
	public PressInstallButtonAnimView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initData(context);
	}
	
	private void initData(Context context){
		
		mContext = context;
		mySelf = this;
		setMyVisibility(false);
	}
	
	public void startDownloadAnim(String pacName, int versionCode, Drawable drawable,int fromX,int toX,int fromY,int toY){
		
		if(isCurrentPacKageDownloading(pacName,versionCode))return;

		setMyVisibility(true);
		TranslateAnimation translateAnimation = new TranslateAnimation(fromX, toX, fromY, toY);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.setDuration(ANIM_TIME);
		animationSet.addAnimation(AnimationUtils.loadAnimation(mContext,R.anim.download_anim));
		animationSet.addAnimation(translateAnimation);
		
		LayoutParams mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ImageView image = new ImageView(mContext);
		image.setBackgroundDrawable(drawable);
		mImageView.add(image);
		mySelf.addView(image, mLayoutParams);

		mHandler.sendEmptyMessageDelayed(SET_VIEW_GONE, ANIM_TIME);
		image.startAnimation(animationSet);
		
	}
	
	private String getEventSignal(String pkgName, int verCode){
		return pkgName + Integer.toString(verCode);
	}
	
	private boolean isCurrentPacKageDownloading(String pkgName, int versionCode){
	    ConcurrentHashMap<String, DownloadEventInfo> mAllDownloadEvent = DownloadPool.getAllDownloadEvent(mContext);
		String name_version = getEventSignal(pkgName,versionCode);
		if(mAllDownloadEvent.containsKey(name_version))return true;
		return false;
	}
	
	private void setMyVisibility(boolean visibility){
		if(visibility){
			mySelf.setVisibility(View.VISIBLE);
		}else{
			mySelf.setVisibility(View.GONE);
		}
	}
}
