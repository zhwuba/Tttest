package com.zhuoyi.market.wallpaper;

import com.market.behaviorLog.UserLogSDK;
import com.market.net.MessageCode;
import com.market.view.CommonLoadingManager;
import com.market.view.CommonSubtitleView;
import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.MarketUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class WallpaperCategoryDetail extends Activity {
    
    private LinearLayout mParent = null;
    
    private CommonSubtitleView mCommonSubtitleView = null;
    private DisplayWallpaperView mCategoryDetail = null;
    
    private String mLogDes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	MarketUtils.setSatusBarTranslucent(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wallpaper_category);
        
        mParent = (LinearLayout) this.findViewById(R.id.wallpaper_category);
        mCommonSubtitleView = (CommonSubtitleView) this.findViewById(R.id.title);
        int[] wDisplay = getWindowDisplay();
        String name = this.getIntent().getStringExtra("display_name");
        mCommonSubtitleView.setSubtitleName(name);
        String code = this.getIntent().getStringExtra("display_code");
        mCategoryDetail = new DisplayWallpaperView(WallpaperView.TYPE_DISPLAY, DisplayWallpaperView.NET_TYPE_CATEGORY_DETAIL, wDisplay, MessageCode.GET_WALLPAPER_LIST_REQ, code);
        mCategoryDetail.getDataFirstIn();
        mParent.addView(mCategoryDetail.getView());
        
        mLogDes = UserLogSDK.getWallPaperClassActivityDes(name, code);
    }
    
    
	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		UserLogSDK.logActivityEntry(this, mLogDes);
	}
	
    
    @Override
    protected void onPause() {
        UserLogSDK.logActivityExit(this, mLogDes);
        
        super.onPause();
    }


    private int[] getWindowDisplay() {
        int[] display = {0,0};
        WindowManager wm = this.getWindowManager();
        display[0] = wm.getDefaultDisplay().getWidth();
        display[1] = wm.getDefaultDisplay().getHeight();
        return display;
    }
}
