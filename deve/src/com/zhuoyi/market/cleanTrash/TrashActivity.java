package com.zhuoyi.market.cleanTrash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.SharePreferenceUtils;

public class TrashActivity extends Activity {

    private TrashAnimView mTrashAnimView;
    private TextView mCleanOverTextView;
    private ExpandableListView mExpandableListView;
    private TrashExpandableAdapter mExpandableAdapter;
    private LinearLayout mBottomLL;
    private Button mBottomBtn;
    
    private float mAnimViewMarginTop;
    private float mAnimViewAdjustMarginHeight;
    
    private TrashControl mTrashControl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.trash_activity);
        
        mTrashControl = TrashControl.get(MarketApplication.getRootContext());
        mTrashControl.setActivityHandler(mHandler);
        
        CommonSubtitleView titleView = (CommonSubtitleView) findViewById(R.id.trashTitleView);
        titleView.setBackBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSplash();
                finish();
            }
        });
        
        mTrashAnimView = (TrashAnimView) findViewById(R.id.trashAnimView);
        mCleanOverTextView = (TextView) findViewById(R.id.trashCleanOverTv);
        mExpandableListView = (ExpandableListView) findViewById(R.id.trashListView);
        mBottomLL = (LinearLayout) findViewById(R.id.bottom_ll);
        mBottomBtn = (Button) findViewById(R.id.trashBottomBtn);
        
        mBottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCleanningStatus();
                
            }
        });
        
        mExpandableAdapter = new TrashExpandableAdapter(this, mTrashControl);
        mExpandableListView.setDrawingCacheEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setAdapter(mExpandableAdapter);
        
        mAnimViewAdjustMarginHeight = getResources().getDimension(R.dimen.trash_anim_view_adjust_view_height);
        mAnimViewMarginTop = getResources().getDimension(R.dimen.trash_anim_view_margin_nomal);
        
        MarketNotificationManager.get().cancelTrashCleanNotify();
        displayCheckingStatus();
        /** 从通知进垃圾清理 */
        clearNotify();
    }

    /**
     * {清除垃圾清理的红点通知}.
     * 市场打开的情况下, 通知Splash清除tab上的红点/通知管理界面清楚垃圾清理红点提示(如果最后停留在管理界面)
     * 市场关闭的情况下,直接保存清理参数
     */
    private void clearNotify() {
        boolean hasCleardMobile = SharePreferenceUtils.hasCleardMobile();
        if (hasCleardMobile) {
            return;
        }
        SharePreferenceUtils.setCleardMobile();
        if (Splash.getHandler() != null) {
            Intent intent = new Intent("com.zhuoyi.removeclearnotify");
            sendBroadcast(intent);
        }
        
    }

	@Override
	protected void onResume() {
//		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		//for record user behavior log
        UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_TRASH_VIEW));
	}

    @Override
    protected void onDestroy() {
        mTrashAnimView.recycle();
        mTrashAnimView.clearAnimation();
        mCleanOverTextView.clearAnimation();
        mExpandableListView.clearAnimation();
        mBottomLL.clearAnimation();
        super.onDestroy();
    }
    
    
    public static final int MSG_CHECK_TRASH_FINISH = 1;
    public static final int MSG_TRASH_CLEAN_FINISH = 2;
    public static final int MSG_TRASH_SIZE_CHANGED = 3;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch(what) {
            case MSG_CHECK_TRASH_FINISH:
                //check trash info finished, display the trash info
                if (mTrashControl.getTotalTrashSize() == 0) {
                    displayNothingToCleanStatus();
                } else {
                    displayTrashSelectStatus();
                }
                mExpandableAdapter.notifyDataSetChanged();
                break;
                
            case MSG_TRASH_CLEAN_FINISH:
                //trash clean finished, display clean up
                displayCleanUpStatus();
                break;
                
            case MSG_TRASH_SIZE_CHANGED:
                //all trash size has changed, change bottom button display
                long trashSize = mTrashControl.getSelectedTrashSize();
                syncBottomBtnText();
                
                if (trashSize == 0) {
                    mBottomLL.setVisibility(View.GONE);
                    break;
                } else {
                    mBottomLL.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
        
    };
    
    
    private void syncBottomBtnText() {
        long trashSize = mTrashControl.getSelectedTrashSize();
        String sizeStr = mTrashControl.getDisplaySizeText(trashSize);
        String btnText = TrashActivity.this.getString(R.string.trash_bottom_btn_text, sizeStr);
        mBottomBtn.setText(btnText);
    }
    
    
    private void displayCleanningStatus() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.trash_bottom_out);
        animation.setFillAfter(true);
        animation.setDuration(500);
        LinearLayout.LayoutParams params = (LayoutParams) mTrashAnimView.getLayoutParams();
        params.setMargins(0, (int)(mAnimViewAdjustMarginHeight + mAnimViewMarginTop), 0, 0);
        mTrashAnimView.setLayoutParams(params);
        mTrashAnimView.displayCleanningAnim();
        
        mBottomLL.setVisibility(View.GONE);
        animation.setAnimationListener(new GoneViewAnimListener(mBottomLL));
        mBottomLL.startAnimation(animation);
        
        mTrashControl.deleteSelectTrashFile();
        animation = AnimationUtils.loadAnimation(this, R.anim.trash_bottom_out);
        animation.setFillAfter(true);
        animation.setDuration(1000);
        mCleanOverTextView.setVisibility(View.GONE);
        mExpandableListView.setVisibility(View.GONE);
        animation.setAnimationListener(new GoneViewAnimListener(mExpandableListView));
        mExpandableListView.startAnimation(animation);
        
        Animation translateDown = new TranslateAnimation(0, 0, - mAnimViewAdjustMarginHeight, 0);
        translateDown.setDuration(1000);
        translateDown.setFillEnabled(true);
        translateDown.setFillAfter(true);
        mTrashAnimView.startAnimation(translateDown);
        
    }
    
    
    private void displayCheckingStatus() {
        LinearLayout.LayoutParams params = (LayoutParams) mTrashAnimView.getLayoutParams();
        params.setMargins(0, (int)mAnimViewMarginTop, 0, 0);
        mTrashAnimView.setLayoutParams(params);
        
        mTrashAnimView.displayCheckingAnim();
        mCleanOverTextView.setVisibility(View.GONE);
        mExpandableListView.setVisibility(View.VISIBLE);
        mBottomLL.setVisibility(View.GONE);
        
        startCheckTrash();
    }
    
    
    private void displayCleanUpStatus() {
        LinearLayout.LayoutParams params = (LayoutParams) mTrashAnimView.getLayoutParams();
        params.setMargins(0, (int)(mAnimViewAdjustMarginHeight + mAnimViewMarginTop), 0, 0);
        mTrashAnimView.setLayoutParams(params);
        
        mTrashAnimView.displayCleanUpStatus();
        mCleanOverTextView.setVisibility(View.VISIBLE);
        mExpandableListView.setVisibility(View.GONE);
        mBottomLL.setVisibility(View.GONE);
        
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.trash_bottom_in);
        animation.setFillAfter(true);
        animation.setDuration(500);
        mCleanOverTextView.startAnimation(animation);
    }
    
    
    private void displayNothingToCleanStatus() {
        displayCleanUpStatus();
        
    }
    
    
    private void displayTrashSelectStatus() {
        long totalSize = mTrashControl.getTotalTrashSize();
        LinearLayout.LayoutParams params = (LayoutParams) mTrashAnimView.getLayoutParams();
        params.setMargins(0, (int)mAnimViewMarginTop, 0, 0);
        mTrashAnimView.setLayoutParams(params);
        mTrashAnimView.displaySelcectStatus(totalSize);
        syncBottomBtnText();
        
        mCleanOverTextView.setVisibility(View.GONE);
        mExpandableListView.setVisibility(View.VISIBLE);
        mBottomLL.setVisibility(View.VISIBLE);
        
        mExpandableListView.refreshDrawableState();
        mBottomLL.refreshDrawableState();
    }
    
    
    private class GoneViewAnimListener implements Animation.AnimationListener {
        
        private View goneView;
        
        GoneViewAnimListener(View view) {
            goneView = view;
        }
        
        @Override
        public void onAnimationStart(Animation animation) {
            goneView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            goneView.setVisibility(View.GONE);
            goneView.clearAnimation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    
    private void startCheckTrash() {
        long minCheckTime = 5 * 1000;
        mTrashControl.checkTrashStatus(minCheckTime, false, null);
    }
    
    
    @Override
    public void onBackPressed() {
        startSplash();
        this.finish();
    }
    
    
    private void startSplash() {
        Intent intent = new Intent(this, Splash.class);
        SharedPreferences settings = getSharedPreferences(Splash.PREFS_NAME, 0);
        Editor editor = settings.edit();
        editor.putBoolean(Splash.FIRST_RUN, false);
        editor.commit();
        if(Splash.getHandler() == null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("showLoadingUI", false);
            startActivity(intent);
        }
    }
}
