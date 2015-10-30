package com.zhuoyi.market.appManage.favorite;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

public class MarketFavoriteEditActivity extends Activity {
	private CommonSubtitleView mCommonSubtitleView;
	private RelativeLayout mRelativeLayout;
	private FavoriteEditView mFavoriteEditView;
	
	public static boolean Log_Flag = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favorite_edit_manage_view);
		mCommonSubtitleView = (CommonSubtitleView) findViewById(R.id.title);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.favorite_edit_main);
		
		mFavoriteEditView = new FavoriteEditView(getApplicationContext());
		mRelativeLayout.addView(mFavoriteEditView.getView());
		setEditFavoriteView();
	}

	
	private void setEditFavoriteView() {
		TextView mTextView = mCommonSubtitleView.getRightTextView();
		mTextView.setVisibility(View.VISIBLE);
		mTextView.setBackgroundResource(R.drawable.favorite_delete_bg);
		mTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Message msg = new Message();
				msg.what = FavoriteEditView.REFRESH_VIEW;
				mFavoriteEditView.mHandler.sendMessage(msg);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log_Flag = true;
	}
}
