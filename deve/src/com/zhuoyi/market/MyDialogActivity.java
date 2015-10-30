package com.zhuoyi.market;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.zhuoyi.market.utils.MarketUtils;

public class MyDialogActivity extends Activity implements OnClickListener{

	private Button mLeftButton;
	private Button mRightButton;
	private TextView mTitleView;
	private TextView mContentView;
	private String[] mReceive_string= new String[3];
	private int mCustomType = 0;
	private final int TYPE_DETAILS_UI = 3;
	private final int TYPE_CLOSED_ACTIVITY = 4;
	private final int TYPE_SIGNATURE = 1; 
	private final int TYPE_CLEAR_CACHE = 2;
	private boolean mIsShowAnimation = false; 
	@Override   
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		mCustomType = getIntent().getIntExtra("myCustomType", 0);	
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.my_tip_dialog); 
		setupView();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void setupView() {
		String title_string="";
		String content_string = "";  
		String temp_string = "";
		int end; 
		  
		//clearingLayout = (LinearLayout) findViewById(R.id.clearingLayout);
		//imageView = (ImageView) findViewById(R.id.imageView_ani);
		switch(mCustomType)     
		{
			case TYPE_SIGNATURE:
				temp_string = getIntent().getStringExtra("dialogParam");		
				mReceive_string = temp_string.split(",");//0 path 1 apkName 2 packageName
				if(mReceive_string[1].endsWith(".apk"))
				{
					end = mReceive_string[1].length()-4;
					title_string = mReceive_string[1].substring(0, end) + getResources().getString(R.string.notification_install_fault_content);
				}
				content_string = getResources().getString(R.string.Signature_errors);
				break;
			case TYPE_DETAILS_UI:
				title_string = getResources().getString(R.string.detail_delete_title);
				content_string = getResources().getString(R.string.detail_delete_info);
				break;
			case TYPE_CLOSED_ACTIVITY:
				title_string = getResources().getString(R.string.dialog_exit_title);
				content_string = getResources().getString(R.string.dialog_exit_message);
				break;
			default:  
				break;
		}
		   
		mTitleView = (TextView) findViewById(R.id.dialog_title);
		mTitleView.setText(title_string);
		
		mContentView = (TextView) findViewById(R.id.tip_text);
		mContentView.setText(content_string);
		
		mLeftButton = (Button) findViewById(R.id.tip_dialog_ok_button);
		mRightButton = (Button) findViewById(R.id.tip_dialog_cancel_button);

		mLeftButton.setOnClickListener(this);
		mRightButton.setOnClickListener(this);
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && mCustomType == TYPE_CLEAR_CACHE && mIsShowAnimation)
		{
			 
		}
	}
	@Override
	public void onBackPressed()
	{
		if(mIsShowAnimation)
			return;
		super.onBackPressed();
	}
	
	@Override
	public void onClick(View v) {
		
		int id = v.getId();
		switch(id)
		{
			case R.id.tip_dialog_ok_button:			
				doButtonEvent(mCustomType);
				break;
				
			case R.id.tip_dialog_cancel_button:
				finish();
				break;
				
			default:
				break; 
		} 
	}
	

	public void doButtonEvent(int type_id)
	{
		final int DELETE_DOWNLOAD_APK = 7; //for detail ui
		final int CLOSED_ALL_ACTIVITY = 1;
		switch(type_id)
		{
			case TYPE_SIGNATURE:				
				MarketUtils.AppInfoManager.AppUnInstall(mReceive_string[2], this);
				finish();					
				break;
			case TYPE_DETAILS_UI:
				finish();
				break;
			case TYPE_CLOSED_ACTIVITY:
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			default:
				break;
		}
	}
	
}