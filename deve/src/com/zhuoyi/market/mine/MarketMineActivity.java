package com.zhuoyi.market.mine;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.view.CommonLoadingManager;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

public class MarketMineActivity extends Activity {

	private MineView mMineView;
	private LinearLayout mMineLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mine);
		initView();
	}


	private void initView() {
		
		mMineLayout = (LinearLayout) findViewById(R.id.market_mine);
		mMineView = new MineView(this);
		mMineView.entryView();
		mMineLayout.addView(mMineView.getRootView());
	}


	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		mMineView.onResume();
		
		//for record user behavior log
        UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_MINE_VIEW));
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMineView.freeViewResource();
	}

	
}
