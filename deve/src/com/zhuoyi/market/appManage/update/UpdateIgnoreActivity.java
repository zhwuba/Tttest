package com.zhuoyi.market.appManage.update;


import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;


public class UpdateIgnoreActivity extends Activity {

	private IgnoreManagerView mIgnoreManagerView;
	private LinearLayout mIgnoreMainView;
	private CommonSubtitleView mCommonSubtitleView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_manager_ignore);
		mIgnoreMainView = (LinearLayout) findViewById(R.id.ignore_main_view);
		mCommonSubtitleView = (CommonSubtitleView) findViewById(R.id.ignore_title);
		mIgnoreManagerView = new IgnoreManagerView(getApplicationContext());
		mIgnoreManagerView.setTttleView(mCommonSubtitleView);
		mIgnoreMainView.addView(mIgnoreManagerView.getView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIgnoreManagerView.onResume();
	}

	
	
}
