package com.market.account.login;


import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;
import com.market.account.utils.ProcessUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseAcvivity_web extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.layout_base_activity_web);
		
		LinearLayout baseView = (LinearLayout) findViewById(R.id.base_layout);
		MarketUtils.setTitleLayout(baseView, this.getApplicationContext());
	}

	@Override
	public void setContentView(int layoutResID) {
		LayoutInflater inflater = LayoutInflater.from(this);
		LinearLayout layout = ((LinearLayout) findViewById(R.id.layout_web_content));
		layout.addView(inflater.inflate(layoutResID, null,false),LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	}

	/**
	 * 设置左按钮是否显示
	 */
	 public void setLeftButtonEnable() {
		 ((TextView) findViewById(R.id.btn_back_id)).setVisibility(View.VISIBLE);
	 }
	 /**
	  * 设置左按钮的点击事件
	  */
	 public void setLeftButton(int strId, OnClickListener listener) {
	     TextView leftButton = (TextView) findViewById(R.id.btn_back_id);
		 //leftButton.setText(getResources().getString(strId));
		 leftButton.setOnClickListener(listener);
	 }

	 public void setTitleText(String title) {
		TextView tv = (TextView) findViewById(R.id.title_text);
		tv.setText(title);
	 }
	 
	 
	 public void setTitleEnable(boolean isEnable) {
		 TextView title = (TextView) findViewById(R.id.title_text);
		 if(isEnable){
			title.setVisibility(View.VISIBLE); 
		 }else{
			 title.setVisibility(View.GONE);
		 }
	 }
	 
	 public void setPresentShow(ImageView mImageView, boolean isShow) {
		 if(isShow)
			 mImageView.setVisibility(View.VISIBLE);
		 else
			 mImageView.setVisibility(View.GONE);
	 }
	 
	 
	 //去掉标题栏
	 public void setTitleBarGone() {
	     View view = findViewById(R.id.layout_web_title_bar);
	     view.setVisibility(View.GONE);
	 }
	 
	 
	 @Override
	 protected void onDestroy() {
		 if (!ProcessUtils.isMainProcess(this)) {
	    	 System.exit(0);
	     }	
		 super.onDestroy();
	 }
}
