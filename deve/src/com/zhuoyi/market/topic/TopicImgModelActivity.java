package com.zhuoyi.market.topic;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.market.behaviorLog.UserLogSDK;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

public class TopicImgModelActivity extends Activity {

	private CommonSubtitleView mTitleBar;
	private LinearLayout mLinearLayout;
	private TopicView mTopicView = null;
    private boolean mIsFirstEntryTopic = true;
    private Intent intent;
    private String mLogDes;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_topic_main);
		findViews();
	}
    
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
    }
    

	@Override
    protected void onDestroy() {
	    if(mTopicView!=null)
	        mTopicView.freeViewResource();
        super.onDestroy();
        
    }

	
	private void findViews() {
		intent = getIntent();
		mTitleBar = (CommonSubtitleView) findViewById(R.id.title_bar);
		mTitleBar.setSubtitleName(intent.getStringExtra("titleName"));
		mTitleBar.showSearchBtn(true);
		mLinearLayout = (LinearLayout)findViewById(R.id.main);
		mTopicView = new TopicView(getApplicationContext());
		mLinearLayout.addView(mTopicView.getMyView(), new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		if(mIsFirstEntryTopic)
			mTopicView.entryTopicView(intent.getIntExtra("topicId", -1));
		mIsFirstEntryTopic = false;
	}

	
	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		String logDes = intent.getStringExtra("logDes");
		if (logDes != null) {
		    mLogDes = logDes;
		}
		
		UserLogSDK.logActivityEntry(getApplicationContext(), mLogDes);
	}


    @Override
    protected void onPause() {
        UserLogSDK.logActivityExit(getApplicationContext(), mLogDes);
        
        super.onPause();
    }
}
