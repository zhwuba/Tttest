package com.zhuoyi.market;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class UpdateDialogActivity extends Activity implements OnClickListener{

	private Button mLeftButton;
	private Button mRightButton;
	private TextView mTitleView;
	private TextView mContentView;

	private String mContents = "";
	private String mTitle = "";
	private int mUpdateType = -1;
	private String mDownloadUrl = "";
	private String mMd5 = "";
	private final int BACKGROUND_UPDATE = 4;
	private final int NO_UPDATE = 3;
	private final int TIP_UPDATE = 2;
	private final int FORCE_UPDATE = 1;
	private String mPName = "";
	@Override   
	protected void onCreate(Bundle savedInstanceState)
	{ 
		super.onCreate(savedInstanceState);
		mPName = getIntent().getStringExtra("packageName");
		mContents = getIntent().getStringExtra("myContents");
		mTitle = getIntent().getStringExtra("myTitle");
		mUpdateType = getIntent().getIntExtra("myUpdateType", -1);
		mDownloadUrl = getIntent().getStringExtra("downloadUrl");
		mMd5 = getIntent().getStringExtra("md5");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.my_tip_dialog); 
		
		setupView();
	}
	
	private void setupView()
	{
   
		mTitleView = (TextView) findViewById(R.id.dialog_title);
		mTitleView.setText(mTitle);
		
		mContentView = (TextView) findViewById(R.id.tip_text);
		mContentView.setText(mContents);
		
		mLeftButton = (Button) findViewById(R.id.tip_dialog_ok_button);
		mRightButton = (Button) findViewById(R.id.tip_dialog_cancel_button);

		mLeftButton.setOnClickListener(this);
		mRightButton.setOnClickListener(this);
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		Intent intent = getIntent();
		
		int id = v.getId();
		switch(id)
		{
			case R.id.tip_dialog_ok_button:			
				doButtonEvent();
				break;
				
			case R.id.tip_dialog_cancel_button:
				finish();
				if(mUpdateType==FORCE_UPDATE)
				{
					//强行退出

				    intent = new Intent(this, Splash.class);
				    intent.putExtra("isClose", true);
				    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    startActivity(intent);
                	
				}
				
				break;
				
			default:
				break; 
		} 
	}

	public void doButtonEvent()
	{
		finish();
		//Start Downlaod apk
	}
	
}