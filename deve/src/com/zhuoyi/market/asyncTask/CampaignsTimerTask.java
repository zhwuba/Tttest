package com.zhuoyi.market.asyncTask;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.json.JSONObject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.market.account.constant.Constant;
import com.market.account.dao.UserInfo;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.netutil.HttpOperation;
import com.market.account.utils.MD5Util;
import com.zhuoyi.market.R;
import com.zhuoyi.market.Splash;

/**
 * 查询活动奖品是否发放
 * @author huangyanan
 */
public class CampaignsTimerTask extends TimerTask {

	public final static int CAMPAIGNS_RECEIVER = 99;
	private Context mContext;
	private SharedPreferences mSharedPreferences;
	private Handler mHandler;
	private int mHandlerWhat;
	
	public CampaignsTimerTask(Context context,Handler handler,int handlerWhat) {
		this.mHandler = handler;
		mContext = context;
		mHandlerWhat = handlerWhat;
	}

	@Override
	public void run() {
		queryGift(mHandler);
	}
	/**
	 * 在规定的时间内 才执行查询奖品操作
	 * @return
	 */
	private boolean isRightTime() {
		Time time = new Time();
		time.set(System.currentTimeMillis());
		int hour = time.hour;
		int minute = time.minute;
		if(hour >= 7 && hour < 24) {
			return true;
		}else{
			return false;
		}
	}

	
	private boolean hasGiftReceiver() {
		mSharedPreferences = mContext.getSharedPreferences(BaseActivity_Html5.HAS_GIFT_RECEIVER, mContext.MODE_PRIVATE);
		return mSharedPreferences.getBoolean("giftReceiver", true);
	}
	
	private void setGiftReceiver(boolean hasGiftReceiver) {
		mSharedPreferences = mContext.getSharedPreferences(BaseActivity_Html5.HAS_GIFT_RECEIVER, mContext.MODE_PRIVATE);
		Editor editor = mSharedPreferences.edit();
		editor.putBoolean("giftReceiver", hasGiftReceiver);
		editor.commit();
	}
	
	/**
	 * 查询奖品发放
	 * @param handler
	 */
	private void queryGift(Handler handler) {
		String openid = UserInfo.get_openid(mContext);
		if(!TextUtils.isEmpty(openid)) {
			if(hasGiftReceiver() && isRightTime()) {	/**	有奖品等待发放	**/
				String result = null;
				try {
					/**	查询接口	start**/
					String token = UserInfo.get_token(mContext);
					Map<String, String> params = new HashMap<String, String>();
					String signString = MD5Util.md5(openid + token + Constant.SIGNKEY);
					params.put("openid", openid);
					params.put("token", token);
					params.put("sign", signString);
					result = HttpOperation.postRequest(Constant.ZHUOYOU_REWARD_PUSH, params);
					/**	end	**/ 
//					{"result":0,"resend":false,"push":false}
					if(!TextUtils.isEmpty(result)) {
						JSONObject jsonObject = new JSONObject(result);
						int status = jsonObject.getInt("result");
						if(status == 0) {	/**	查询结果成功**/
							boolean push = jsonObject.getBoolean("push");
							boolean resend = jsonObject.getBoolean("resend");
							setGiftReceiver(resend);	//是否还去查询
							if(push) {
								/**	有奖品**/
								if(handler != null) {
									Message message = new Message();  
									message.what = mHandlerWhat;
									handler.sendMessage(message);  
								}
							}
						} 
					}
				} catch (Exception e) {
					e.fillInStackTrace();
				}
				
			}
		}
	
	}
}
