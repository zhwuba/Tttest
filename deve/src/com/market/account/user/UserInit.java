package com.market.account.user;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.market.account.dao.GetUserInfo;
import com.market.account.dao.UserInfo;
import com.market.account.dao.UserLogin;
import com.market.account.dao.GetUserInfo.GetUserInfoListener;
import com.market.account.dao.UserLogin.UserLoginListener;
import com.market.account.login.BaseActivity_Html5;
import com.market.account.receiver.AccountLoginReceiver;
import com.market.account.utils.PropertyFileUtils;
import com.zhuoyi.market.R;
import com.zhuoyi.market.constant.Constant;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.View;
import android.view.WindowManager.BadTokenException;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class UserInit {
    
    private Context mContext = null;
    private boolean fromUserCenter = false;
    
    public static final int ZHUOYOU_USER_INFO = 1000;
    public static final int UPDATE_USER_INFO = 1001;
    
    private boolean mSplashInitFinish = false;
    private boolean mMineviewFinish = false;
    private BroadcastReceiver accountLoginReceivernew;
    
    private TextView mRecode;

    private static List<Handler> mReceiverInfoHanders;
    
    private Handler mFirstTimeInHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case ZHUOYOU_USER_INFO:
                Bundle bundle = message.getData();
                final boolean fromUserCenter =bundle.getBoolean("fromUserCenter");
                final String desc = bundle.getString("desc");
                final String checkIn = bundle.getString("checkIn");
                final int recode = bundle.getInt("recode");
                if (!TextUtils.isEmpty(checkIn) && mContext != null) {
                    try {
                        updateCheckIn(false);
                        JSONObject jsonObject = new JSONObject(checkIn);
                        final int checkin5days = Integer.parseInt(jsonObject.getString("checkin5days"));
                        final int rewardType = jsonObject.getInt("reward_type");
                        final JSONObject historyJson = jsonObject.getJSONObject("checkin_history");
                        
                        final Handler handler = new Handler(); 
                        handler.postDelayed(new Runnable() { 
                            int repeatTime = 0;
                            
                            public void run() {
                                try {
                                    repeatTime++;
                                    if((!fromUserCenter && mSplashInitFinish) || (fromUserCenter && mMineviewFinish)) {
                                        try {
                                        	setRecode(recode);
                                            showSignInDialog(desc, checkin5days,rewardType,historyJson, checkIn);
                                        } catch(BadTokenException e) {
                                            if(repeatTime < 3) {
                                                handler.postDelayed(this,1000);
                                            }
                                        }
                                    } else if(repeatTime < 10) {
                                        handler.postDelayed(this,1000);
                                    }
                                } catch (JSONException e) {
                                    if(!TextUtils.isEmpty(desc)) {
//                                    	writeToFileLog("1   " + checkIn);
                                    	if(mContext != null) {
                                    		Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                                    	}
                                    }
                                    e.printStackTrace();
                                }
                            } 

                        }, 1000);
                        
                    } catch (JSONException e) {
                        if(!TextUtils.isEmpty(desc)) {
//                        	if(!TextUtils.isEmpty(checkIn)) {
//                        		writeToFileLog("2  " + checkIn);
//                        	} else 
//                        		writeToFileLog("TextUtils.isEmpty(checkIn) == true");
                            Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    } catch(Exception e) {
                        
                    }
                }
                break;
            }
        }
    }; 
    
    
    
    public UserInit(Context context, boolean fromUserCenter) {
        mContext = context;
        this.fromUserCenter = fromUserCenter;
        accountLoginReceivernew = new AccountLoginReceiver(mFirstTimeInHandler);
        registerAccountLoginReceiver();
    } 
    
    
    /**
     * 释放资源
     */
    public void releaseRes() {
        unregisterAccountLoginReceiver();
        if (mFirstTimeInHandler != null) {
            if (mFirstTimeInHandler.hasMessages(ZHUOYOU_USER_INFO)) {
                mFirstTimeInHandler.removeMessages(ZHUOYOU_USER_INFO);
            }
            mFirstTimeInHandler = null;
        }
        
        if(mReceiverInfoHanders != null) {
        	mReceiverInfoHanders.clear();
        	mReceiverInfoHanders = null;
        }
        
        mContext = null;
    }
    
    
    /**
     * 注册账户receiver
     */
    public void registerAccountLoginReceiver() {
        try {
            final IntentFilter filter = new IntentFilter();
            if(fromUserCenter)
            	filter.addAction("zhuoyou.android.account.USERCENTER_CHECKIN");
            else
            	filter.addAction("zhuoyou.android.account.SEND_USER_INFO");
            mContext.registerReceiver(accountLoginReceivernew, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    /**
     * 注销账户receiver
     */
    private void unregisterAccountLoginReceiver() {
        try {
            mContext.unregisterReceiver(accountLoginReceivernew);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 更新用户信息
     * @param newUserInfoJsonObject
     * @param context
     */
    public static void updateUserInfo(final JSONObject newUserInfoJsonObject, final Context context){
    	new Thread(){
    		@Override
    		public void run() {
    			super.run();
    			JSONObject userInfoJsonObject = UserInfo.getUserInfo(context);
    			if(userInfoJsonObject == null || context == null) return;
    			AccountManager accountManager = AccountManager.get(context);
    			Account[] accounts = accountManager.getAccountsByType(com.market.account.constant.Constant.ACCOUNT_TYPE);
    			try {
    				String nickName = newUserInfoJsonObject.has("nickname") ? newUserInfoJsonObject.getString("nickname") : null;
    				String userName = newUserInfoJsonObject.has("username") ? newUserInfoJsonObject.getString("username") : null;
    				String avatar = newUserInfoJsonObject.has("avatarurl") ? newUserInfoJsonObject.getString("avatarurl") : null;
    				String score = newUserInfoJsonObject.has("score") ? newUserInfoJsonObject.getString("score") : null;
    				String openid = newUserInfoJsonObject.has("openid") ? newUserInfoJsonObject.getString("openid"):null;
    				String gender = newUserInfoJsonObject.has("gender") ? newUserInfoJsonObject.getString("gender"):null;
    				String openqq = newUserInfoJsonObject.has("openqq") ? newUserInfoJsonObject.getString("openqq") : null;
    				String openweibo = newUserInfoJsonObject.has("openweibo") ? newUserInfoJsonObject.getString("openweibo") : null;
    				boolean canCheckIn = newUserInfoJsonObject.has("cancheckin")? newUserInfoJsonObject.getBoolean("cancheckin"): true;
    				
    				userInfoJsonObject.put("nickname", nickName)
    				.put("username", userName)
    				.put("logoUrl", avatar)
    				.put("recode", score)
    				.put("openid", openid)
    				.put("gender", gender)
    				.put("cancheckin", canCheckIn)
    				.put("openqq", openqq)
    				.put("openweibo", openweibo);
    				accountManager.setUserData(accounts[0], "userInfo", userInfoJsonObject.toString());
    				sendUserInfoUpdateMessage();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}.start();
        
    }
    
    
    /**
     * 告知相关页面刷新用户信息
     */
    private static void sendUserInfoUpdateMessage() {
    	if(mReceiverInfoHanders != null && mReceiverInfoHanders.size() > 0) {
    		for(Handler handler : mReceiverInfoHanders) {
    			if(handler != null) {
    				handler.sendEmptyMessage(UPDATE_USER_INFO);
    			}
    		}
    	}
    }
    
    
    /**
     * 注册接收用户信息更新Handler
     * @param handler
     */
    public static void registerUserInfoUpdate(Handler handler) {
    	if(mReceiverInfoHanders == null) {
    		mReceiverInfoHanders = new ArrayList<Handler>();
    	}
    	if(!mReceiverInfoHanders.contains(handler)) {
    		mReceiverInfoHanders.add(handler);
    	}
    }
    
    
    
    /**
     * 用户在本地登陆过,就去检查用户信息
     */
    public void checkIn(){
        if (UserInfo.getUserIsLogin(mContext)) {
            check_in();
        }
    }
    
    
    /**
     * 检查用户是否登录过
     */
    private void check_in() {
        GetUserInfo getUserInfo = new GetUserInfo(mContext);
        GetUserInfoListener getUserInfoListener = new GetUserInfoListener() {

            @Override
            public void OnGetUserInfoCallBack(String result) {
                if (!TextUtils.isEmpty(result) && mContext != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        int mResult = jsonObject.getInt("result");
                        
                        if (mResult == 0) {
                            //获取到用户信息成功
                            boolean canCheckIn = jsonObject.getBoolean("cancheckin");
                            if(canCheckIn) {
                            	Intent intent = new Intent("zhuoyou.android.account.USER_CHECK_IN");
                            	Bundle bundle = new Bundle();
                            	bundle.putBoolean("fromUserCenter", fromUserCenter);
                            	intent.putExtra("flag", bundle);
                                mContext.sendBroadcast(intent);  //签到动作
                            }
                            updateUserInfo(jsonObject, mContext);
                        } else if (mResult < 0) {
                            Toast.makeText(mContext, jsonObject.getString("desc"), Toast.LENGTH_SHORT).show();
                        } else if (mResult == 2) {
                            //用户信息过期
                            UserLogin userLogin = new UserLogin(mContext);
                            userLogin.setUserLoginListener(new UserLoginListener() {

                                @Override
                                public void OnUserLoginCallBack(String result) {
                                    JSONObject jsonObject;
                                    try {
                                        jsonObject = new JSONObject(result);
                                        int mResult = jsonObject.getInt("result");

                                        if (mResult == 0) {
                                            String userInfo = null;
                                            JSONObject userjsonObject = null;
                                            AccountManager mAccountManager = AccountManager.get(mContext);
                                            Account[] accounts = mAccountManager.getAccountsByType(com.market.account.constant.Constant.ACCOUNT_TYPE);
                                            if (accounts != null && accounts.length >= 1) {
                                                userInfo = mAccountManager.getUserData(accounts[0], "userInfo");
                                            }
                                            userjsonObject = new JSONObject(userInfo);
                                            userjsonObject.put("TOKEN", jsonObject.has("token") ? jsonObject.getString("token") : null);
                                            userjsonObject.put("openid", jsonObject.has("openid") ? jsonObject.getString("openid") : null);
                                            mAccountManager.setUserData(accounts[0], "userInfo", userjsonObject.toString());
                                            
                                            boolean canCheckIn = jsonObject.getBoolean("cancheckin");
                                            if(canCheckIn) {
                                                mContext.sendBroadcast(new Intent("zhuoyou.android.account.USER_CHECK_IN"));  //签到动作
                                            }
                                            updateUserInfo(jsonObject, mContext);
                                            
                                        } else {
                                            AccountManager mAccountManager = AccountManager.get(mContext);
                                            Account[] accounts = mAccountManager.getAccountsByType(com.market.account.constant.Constant.ACCOUNT_TYPE);
                                            if (accounts != null && accounts.length >= 1) {
                                                mAccountManager.removeAccount(accounts[0], new AccountManagerCallback<Boolean>() {
                                                    
                                                    @Override
                                                    public void run(AccountManagerFuture<Boolean> future) {
                                                        // don,t forget to delete the cache .eg:logo
                                                        BaseActivity_Html5.deleteUserLogo();
                                                    }
                                                }, new Handler());
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                            userLogin.execute();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        
        getUserInfo.setGetUserInfoListener(getUserInfoListener);
        if (getUserInfo != null && getUserInfo.getStatus() != AsyncTask.Status.RUNNING) {
            try {
                getUserInfo.execute();
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 更新登录信息
     * @param canCheckIn
     */
    private void updateCheckIn(boolean canCheckIn) {
        JSONObject userInfoJsonObject = UserInfo.getUserInfo(mContext);
        if(userInfoJsonObject == null) return;
        AccountManager accountManager = AccountManager.get(mContext);
        Account[] accounts = accountManager.getAccountsByType(com.market.account.constant.Constant.ACCOUNT_TYPE);
        try {
           userInfoJsonObject.put("cancheckin", canCheckIn);
           accountManager.setUserData(accounts[0], "userInfo", userInfoJsonObject.toString());
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
    
    
    /**
     * 显示签到成功Dialog
     * @param desc 签到失败后弹出Toast内容
     * @param checkIn5days 连续签到的天数
     * @param rewardType 是否赠送流量地区用户
     * @param signinHistory 历史赠送流量记录
     * @throws JSONException 
     */
    private void showSignInDialog(String desc,final int checkIn5days, int rewardType, final JSONObject signinHistory, String checkIn) throws JSONException {
        // 签到结果pop框
    	if(mContext == null) return;
        final Dialog dialog = new Dialog(mContext,R.style.SignInDialog);
        View signInResultView = View.inflate(mContext,R.layout.layout_signin_report_list, null);
        dialog.setContentView(signInResultView);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        
        final int totalDays = 5;
        final TextView descText = (TextView)signInResultView.findViewById(R.id.desc);
        ImageView[] iconList = new ImageView[totalDays];
        TextView[] dayList = new TextView[totalDays]; 
        TextView[] integralList = new TextView[totalDays]; 
        
        int[] iconResIdList = {R.id.imageView01,R.id.imageView02,R.id.imageView03,R.id.imageView04,R.id.imageView05};
        int[] dayResIdList = {R.id.day01,R.id.day02,R.id.day03,R.id.day04,R.id.day05};
        int[] integralResIdList = {R.id.integral01,R.id.integral02,R.id.integral03,R.id.integral04,R.id.integral05};
        
        for(int i=0;i<totalDays;i++) {
            iconList[i] = (ImageView)signInResultView.findViewById(iconResIdList[i]);
            dayList[i] = (TextView)signInResultView.findViewById(dayResIdList[i]);
            integralList[i] = (TextView)signInResultView.findViewById(integralResIdList[i]);
        }
        
        AnimationDrawable anim = (AnimationDrawable) mContext.getResources().getDrawable(R.anim.get_free_flow_anim);
        
        SparseIntArray signedinDaysIcons = new SparseIntArray();
        signedinDaysIcons.put(1, R.drawable.flow1_dark);
        signedinDaysIcons.put(2, R.drawable.flow2_dark);
        signedinDaysIcons.put(5, R.drawable.flow5_dark);
        signedinDaysIcons.put(10, R.drawable.flow10_dark);
        
        SparseIntArray signinDayIcons = new SparseIntArray();
        signinDayIcons.put(1, R.drawable.flow1);
        signinDayIcons.put(2, R.drawable.flow2);
        signinDayIcons.put(5, R.drawable.flow5);
        signinDayIcons.put(10, R.drawable.flow10);
        signinDayIcons.put(15, R.drawable.flow15);
        
        
        if(rewardType == 2) {
            //广东移动联通用户
            for(int i=0;i<totalDays;i++) {
                if(i < checkIn5days-1) {
                    //签到当天之前
                    iconList[i].setImageResource(signedinDaysIcons.get(signinHistory.getInt(""+i)));
                } else if(i == checkIn5days-1) {
                    //签到当天
                    int frameId = signinDayIcons.get(signinHistory.getInt(""+i));
                    if(frameId == 0) {
                        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
//                        writeToFileLog("3   " + checkIn);
                        return;
                    }
                    anim.addFrame(mContext.getResources().getDrawable(frameId), 0);
                    iconList[i].setImageDrawable(anim);
                } else if(i>checkIn5days-1 && i<totalDays-1) {
                    //签到当天与第五天之间
                    iconList[i].setImageResource(R.drawable.flowx);
                    dayList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                    integralList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                } else if(i == totalDays-1) {
                    //第五天
                    iconList[i].setImageResource(R.drawable.flow15);
                    dayList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                    integralList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                } 

            }
        } else {
            //普通用户
            descText.setText(mContext.getResources().getString(R.string.signin_dialog_desc01));
            for(int i=0;i<totalDays;i++) {
                if(i<totalDays-1) {
                    //前四天
                    if(i<checkIn5days) {
                        iconList[i].setImageResource(R.drawable.integral_got);
                    } else {
                        iconList[i].setImageResource(R.drawable.integral_none);
                        dayList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                        integralList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color)); 
                    }
                } else {
                    //第五天显示的图标不一样
                    if(checkIn5days == totalDays) {
                        iconList[i].setImageResource(R.drawable.integral_big_got);
                    } else {
                        iconList[i].setImageResource(R.drawable.integral_big_none);
                        dayList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                        integralList[i].setTextColor(mContext.getResources().getColor(R.color.signin_dialog_text_color));
                    }
                }
            }
        }
        
        dialog.show();
        
        if(rewardType == 2) {
            //广东用户签到,动画完成后再显示提示语
            int duration = 0; 
            for(int i=0;i<anim.getNumberOfFrames();i++){ 
                duration += anim.getDuration(i); 
            } 
            
            Handler handler = new Handler(); 
            handler.postDelayed(new Runnable() { 

                public void run() { 
                    if(checkIn5days == totalDays) {
                        //签到当天是第五天
                        descText.setText(mContext.getResources().getString(R.string.signin_dialog_desc03));
                    } else {
                        //非第五天
                        try {
                            descText.setText(mContext.getResources().getString(R.string.signin_dialog_desc02,signinHistory.getInt((checkIn5days-1)+"")));
                        } catch (NotFoundException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } 

            }, duration); 
        }
        
        anim.start();
    }
    
    /**
     * splash是否初始化完成
     * @param finish
     */
    public void setSplashInitFinish(boolean finish) {
        mSplashInitFinish = finish;
    }
    
    public void setMineviewFinish(boolean finish) {
    	mMineviewFinish = finish;
    }
    
    public void setTextview(TextView mTextView) {
    	mRecode = mTextView;
    }
    
    public void setRecode(int score) {
    	if(mRecode != null && mContext != null) {
    		mRecode.setText(mContext.getResources().getString(R.string.score) + score);
    	}
    }
    
    
	public void writeToFileLog(String content) {
		FileOutputStream stream = null;
		try {
			File path = new File(PropertyFileUtils.getSDPath() + Constant.download_dir_name + "/checkinLog/");

			File file = new File(PropertyFileUtils.getSDPath() + Constant.download_dir_name + "/checkinLog/log.txt");
			if (!path.exists())
				path.mkdir();
			if (!file.exists())
				file.createNewFile();
			stream = new FileOutputStream(file);
			byte[] buf = content.getBytes();
			stream.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
