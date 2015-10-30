package com.market.updateSelf;

import java.util.HashMap;

import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.utils.StartNetReqUtils;
import com.zhuoyi.market.MarketDialog;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.AppManageUtil;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.manager.MarketNotificationManager;
import com.zhuoyi.market.utils.MarketUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class SelfUpdateManager {
    
    private Context mContext = null;
    private boolean isChecking = false;//用于标识当前是否正在检查版本，防止重复检查
    private boolean mNewestTip = false;//最新版本提醒
    private boolean mStayMarket = false;//强制更新不关闭市场（设置里面使用）
    
    public static final int SELF_UPDATE_TIP_TYPE_N = -1;//不提示
    public static final int SELF_UPDATE_TIP_TYPE_0 = 0;//弹出框提示
    public static final int SELF_UPDATE_TIP_TYPE_1 = 1;//状态栏提示
    
    private int mTipType = SELF_UPDATE_TIP_TYPE_0;//提示方式：-1=不提示；0=弹出框；1=状态栏.默认弹出框
    
    public static final int SELF_UPDATE_TYPE_F = -2;//获取数据失败
    public static final int SELF_UPDATE_TYPE_1 = 1;//强制更新
    public static final int SELF_UPDATE_TYPE_2 = 2;//提示更新
    public static final int SELF_UPDATE_TYPE_3 = 3;//不更新
    public static final int SELF_UPDATE_TYPE_4 = 4;//后台更新
    
    private static final int SELF_UPDATE = 0;
    private static final int SELF_UPDATE_RESPONSE = 1;
    
    public String mCurFrom = null;
    public static final String SELF_UPDATE_REQ_FROM_SPLASH_CREATE = "splash.create";
    public static final String SELF_UPDATE_REQ_FROM_SPLASH_RESUME = "splash.resume";
    public static final String SELF_UPDATE_REQ_FROM_SETTING_IN = "setting.in";
    public static final String SELF_UPDATE_REQ_FROM_SETTING_USER = "setting.user";
    public static final String SELF_UPDATE_REQ_FROM_WIFI_CHANGE = "wifi.change";
    
    public static final long TIME_1_HOUR = 60 * 60 * 1000;
    public static final long TIME_4_HOUR = 4 * 60 * 60 * 1000;
    public static final long TIME_24_HOUR = 24 * 60 * 60 * 1000;
    
    private String mTitle = null;
    private String mContent = null;
    private int mType = -1;
    private int mVersionCode = 0;
    private String mDownloadUrl = null;
    private String mMd5 = null;
    private long mTotalSize = 0;
    private SelfUpdateInterface mCallBack = null;
    
    private static SelfUpdateManager mySelf = null;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            HashMap<String, Object> map = null;
            
            switch(msg.what) {
            case SELF_UPDATE_RESPONSE:
                isChecking = false;
                map = (HashMap<String, Object>) msg.obj;
                Log.e("SelfUpdateManager","map="+map);
                if (map == null) {
                    //回调，刷新
                    if (mCallBack != null)
                        mCallBack.updateSelf(SELF_UPDATE_TYPE_F);
                    
                    newestTip();
                    break;
                }
                
                saveNextReqSelfUpdateInfo(mCurFrom);

                if(map.containsKey("policy"))
                	mType = (Integer) map.get("policy");
                if(map.containsKey("title"))
                	mTitle = map.get("title").toString();
                if(map.containsKey("content"))
                	mContent = map.get("content").toString();
                if(map.containsKey("ver"))
                	mVersionCode = Integer.parseInt(map.get("ver").toString());
                if(map.containsKey("fileUrl"))
                	mDownloadUrl = map.get("fileUrl").toString();
                if(map.containsKey("md5"))
                	mMd5 = map.get("md5").toString();
                if(map.containsKey("totalSize"))
                    mTotalSize = (Long)map.get("totalSize");
                
                if (TextUtils.isEmpty(mMd5) || TextUtils.isEmpty(mDownloadUrl) || mType == SELF_UPDATE_TYPE_3) {
                  //回调，刷新
                    if (mCallBack != null)
                        mCallBack.updateSelf(SELF_UPDATE_TYPE_F);
                    
                    newestTip();
                    break;
                }

                //自更新信息保存到本地
                UpSelfStorage.saveSelfUpdateInfo(MarketApplication.getRootContext(), mTitle, mContent, mVersionCode, mType, mDownloadUrl, mMd5, mTotalSize);
                
                //回调，刷新
                if (mCallBack != null)
                    mCallBack.updateSelf(mType);
                
                //根据不同更新类型来做处理
                doDiffThingWithGetData();
                break;
            case SELF_UPDATE:
                UpdateManager.startDownloadUpdate(mContext.getApplicationContext(), mDownloadUrl, mMd5, mVersionCode, mTotalSize);
                break;
            }
        }
    };
    
    
    /**
     * 初始化
     * @param context
     */
    public SelfUpdateManager(Context context) {
        mContext = context;
    }
    
    
    /**
     * wifi变化接收广播检查更新使用单利模式
     * @param context
     * @return
     */
    public static SelfUpdateManager get(Context context) {
        if (mySelf == null) {
            mySelf = new  SelfUpdateManager(context);
        }
        return mySelf;
    }
    
    
    /**
     * 设置回调，刷新
     * @param callBack
     */
    public void setSelfUpdateInterface(SelfUpdateInterface callBack) {
        mCallBack = callBack;
    }
    
    
    /**
     * 设置提示类型：-1=不提示；0=弹出框；1=状态栏
     * @param tipType
     */
    public void setTipType(int tipType) {
        mTipType = tipType;
    }
    
    
    /**
     * 强制更新转成提示更新-不关闭市场（给设置里面使用）
     * @param stay
     */
    public void setMustUpdate2TipUpdate(boolean stay) {
        mStayMarket = stay;
    }

    
    /**
     * 已经是最新版本是否提示用户
     * @param newestTip
     */
    public void setNewestTip(boolean newestTip) {
        mNewestTip = newestTip;
    }
    
    
    /**
     * 提示已经是最新版本
     */
    private void newestTip() {
        if(mNewestTip)
            Toast.makeText(MarketApplication.getRootContext(), MarketApplication.getRootContext().getString(R.string.latest_version), Toast.LENGTH_SHORT).show();
    }
    
    
    /**
     * 获取是否正在获取更新数据
     * @return
     */
    public boolean getChecking() {
        return isChecking;
    }
    
    
    /**
     * 释放资源
     */
    public void releaseRes() {
        if (mHandler != null) {
            if (mHandler.hasMessages(SELF_UPDATE_RESPONSE)) {
                mHandler.removeMessages(SELF_UPDATE_RESPONSE);
            }
            if (mHandler.hasMessages(SELF_UPDATE)) {
                mHandler.removeMessages(SELF_UPDATE);
            }
            mHandler = null;
        }
        
        mCallBack = null;
        mContext = null;
    }
    
    
    /**
     * 向服务器获取自更新数据
     * 检查自更新时机：启动页关闭时检查，Splash获取onResume（按时间间隔来检查），进入设置红点检查，用户手动点击检查，wifi连接检查
     * @param from 该次请求从哪里来（splash.create--splash.resume--setting.in--setting.user--wifi.change）
     */
    public void selfUpdateRequest(String from) {
        
        if (isChecking) return;
        if (!needReqSelfUpdateInfo(from)) return;
        
        isChecking = true;
        mCurFrom = from;
        String contents = SenderDataProvider.buildToJSONData(mContext.getApplicationContext(),MessageCode.APK_CHECK_SELF_UPDATE,null);
        StartNetReqUtils.execApkSelfUpdateRequest(
                mHandler,
                SELF_UPDATE_RESPONSE,
                MessageCode.APK_CHECK_SELF_UPDATE,
                contents);
    }
    
    
    /**
     * 判断是否联网检查自更新，时间间隔为一天
     * @return
     */
    private boolean needReqSelfUpdateInfo(String key) {
        
        long currentTime = System.currentTimeMillis();
        long nextUpdateTime = MarketUtils.getNextUpdateVersionTime(MarketApplication.getRootContext(), key);
        
        Log.e("SelfUpdateManager","currentTime="+currentTime);
        Log.e("SelfUpdateManager","nextUpdateTime="+nextUpdateTime);
        
        //如果在oncreate已经提示过了，onresume中就不再提示
        if (SELF_UPDATE_REQ_FROM_SPLASH_RESUME.equals(key)) {
            long nextTime = MarketUtils.getNextUpdateVersionTime(MarketApplication.getRootContext(), SELF_UPDATE_REQ_FROM_SPLASH_CREATE);
            if (nextTime > currentTime || nextTime < 0) {
                saveNextReqSelfUpdateInfo(key);
                return false;
            }
        }
        
        if(currentTime > nextUpdateTime){
            return true;
        }else{
            return false;
        }
    }
    
    
    private void saveNextReqSelfUpdateInfo(String key) {
        Log.e("SelfUpdateManager","key="+key);
        if (TextUtils.isEmpty(key)) return;
        
        long nextTime = System.currentTimeMillis();
        Log.e("SelfUpdateManager","nextTime="+nextTime);
        //进入市场1个小时请求一次；返回市场24个小时请求一次；wifi连接4个小时请求一次
        if (SELF_UPDATE_REQ_FROM_SPLASH_CREATE.equals(key)) {
            nextTime = nextTime + TIME_1_HOUR;
        } else if (SELF_UPDATE_REQ_FROM_SPLASH_RESUME.equals(key)) {
            nextTime = nextTime + TIME_24_HOUR;
        } else if (SELF_UPDATE_REQ_FROM_WIFI_CHANGE.equals(key)) {
            nextTime = nextTime + TIME_4_HOUR;
        } else {
            //进入设置和用户手动请求，每次都执行
        } 
        Log.e("SelfUpdateManager","nextTime_need="+nextTime);
        MarketUtils.setNextUpdateVersionTime(MarketApplication.getRootContext(), key, nextTime);
    }
    
    
    /**
     * 根据不同的更新类型来处理
     */
    private void doDiffThingWithGetData() {
        switch (mType) {
        case SELF_UPDATE_TYPE_1:
        case SELF_UPDATE_TYPE_2:
            //强制更新和提示更新
            showUpdateTip();
            break;
        case SELF_UPDATE_TYPE_3:
            //不更新
            newestTip();
            break; 
        case SELF_UPDATE_TYPE_4:
            //后台更新
            UpdateManager.updateServiceStartDown(MarketApplication.getRootContext());
            break;
        }
    }
    
    /**
     * 根据提示类型展示给用户
     */
    public void showUpdateTip() {
        switch (mTipType) {
        case SELF_UPDATE_TIP_TYPE_0:
            //弹出框提示
            if (mContext == null || mHandler == null) break;
            
            int type = mType;
            if (mStayMarket)
                type =  SELF_UPDATE_TYPE_2;

            MarketDialog dialog = new MarketDialog(mContext, R.style.MyMarketDialog, mHandler, SELF_UPDATE, mContent, mTitle, type);
            try {
                dialog.show();
            }catch(Exception e) {
                return;
            }
            break;
        case SELF_UPDATE_TIP_TYPE_1:
            //状态栏提示
            Intent i = AppManageUtil.getStartDownloadActivityIntent(mContext);
            i.putExtra("updateSelf", true);
            
            MarketNotificationManager manager = MarketNotificationManager.get();
            String btnStr = mContext.getString(R.string.notify_tip_user_update);
            manager.notifyWifiUpdte(""+mVersionCode, mTitle, mContent, btnStr, i);
            break;
        case SELF_UPDATE_TIP_TYPE_N:
        default:
            //不提醒
            break;
        }
    }
    
    
    /**
     * 初始化本地数据,防止重复请求数据
     * @param title
     * @param content
     * @param type
     * @param version
     * @param downloadUrl
     * @param md5
     */
    public void setLocalData(String title, String content, int type, int version, String downloadUrl, String md5) {
        mTitle = title;
        mContent = content;
        mType = type;
        mVersionCode = version;
        mDownloadUrl = downloadUrl;
        mMd5 = md5;
    }

    
    /**
     * 自更新检查出来有新版本回调，用来刷新界面
     * @author dream.zhou
     *
     */
    public interface SelfUpdateInterface {
        public void updateSelf(int type);
    }
}
