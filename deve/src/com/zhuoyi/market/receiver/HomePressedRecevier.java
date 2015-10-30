package com.zhuoyi.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.market.view.CommonLoadingManager;
import com.market.behaviorLog.UserLogSDK;
import com.market.updateSelf.UpdateManager;
import com.zhuoyi.market.appResident.MarketApplication;

public class HomePressedRecevier extends BroadcastReceiver {

	public final static String TAG = "InnerRecevier";
	
	public final String SYSTEM_DIALOG_REASON_KEY = "reason";

	public final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";

	public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

	public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

	private OnHomePressListener mOnHomePressListener;
	

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
			String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
			if (reason != null) {
				if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
					if(mOnHomePressListener != null){
						CommonLoadingManager.get().setHomeKeyPressed();
						mOnHomePressListener.onHomePressed(context);
						
						MarketApplication.getInstance().applicationExit();
					}
				} 
			}
		}
	}
	
	
	/**
	 * 添加监听Home按下事件
	 * @param onHomePressListener
	 */
	public void setOnHomePressListener(OnHomePressListener onHomePressListener){
		this.mOnHomePressListener = onHomePressListener;
	}
	
	/**
	 * 按下Home键返回此接口事件
	 * @author huangyn
	 */
	public interface OnHomePressListener{
		public void onHomePressed(Context context);
	}
	
}