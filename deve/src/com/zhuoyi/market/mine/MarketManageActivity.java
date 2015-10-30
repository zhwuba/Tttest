package com.zhuoyi.market.mine;

import com.zhuoyi.market.ExitMarketDialog;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.LogHelper;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MarketManageActivity extends Activity {

	private ManageView mManageView;
	private ExitMarketDialog mExitDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManageView = new ManageView(getApplicationContext());
		mManageView.entryView();
		mExitDialog = new ExitMarketDialog(this, R.style.MyMarketDialog);
		setContentView(mManageView.getRootView());

        View baseView = findViewById(R.id.base_layout);
		MarketUtils.setBaseLayout(baseView, this.getApplicationContext());
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		mManageView.onResume();
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManageView.freeViewResource();
		if(mExitDialog != null && mExitDialog.isShowing()) {
			mExitDialog.dismiss();
		}
		
	}
	
	@Override
	public void onBackPressed() {
		try {
			if (!isFinishing()) {
				String exitTip = MarketUtils.getSharedPreferencesString(getBaseContext(), MarketUtils.KEY_EXIT_MARKET, null);
				mExitDialog.setContent(exitTip);
				mExitDialog.show();
			}
		} catch (Exception e) {
			LogHelper.trace();
		}
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(mExitDialog != null && mExitDialog.isShowing())
			mExitDialog.dismiss();
	}
	
	
}
