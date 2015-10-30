package com.zhuoyi.market;

import com.zhuoyi.market.appResident.MarketApplication;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ExitMarketDialog extends Dialog implements OnClickListener{
	private Context mContext;
	
	private TextView mTextView_logout;
	private TextView mTextView_exitmarket;
	private Button mButton_no_exit;
	private Button mButton_exit;
	
	private String content;
	
	public ExitMarketDialog(Context context) {
		super(context);
		this.mContext = context;
	}

	
	public ExitMarketDialog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.personalinfo_general_dialog);
		findView();
	}


	private void findView() {
		mTextView_logout = (TextView) findViewById(R.id.text_logout);
		mTextView_exitmarket = (TextView) findViewById(R.id.exit_withimg);
		mButton_exit = (Button) findViewById(R.id.cancel);
		mButton_no_exit = (Button) findViewById(R.id.ok);
		
		mTextView_logout.setVisibility(View.VISIBLE);
		
		if(TextUtils.isEmpty(content) || content.equals(" ")) {
			mTextView_exitmarket.setVisibility(View.VISIBLE);
			mTextView_logout.setText(R.string.toast_exit_message);
		} else {
			mTextView_logout.setText(content);
		}
		
		mButton_no_exit.setText(R.string.not_exit);
		mButton_exit.setText(R.string.exit);
		
		mButton_no_exit.setOnClickListener(this);
		mButton_exit.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			dismiss();
			break;
		case R.id.cancel:
		    MarketApplication.getInstance().applicationExit();
			
			((Activity)mContext).finish();
			break;
		default:
			break;
		}
	}
	
	
}
