package com.market.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.userDownload.DownloadManager;
import com.market.net.data.AppInfoBto;
import com.market.net.response.GetStartPageResp;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.LogHelper;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.gallery.AsyncImageLoader;

/**
 * 按home鍵退出，再進入市場，加載界面
 * 
 * @author dream.zhou
 * 
 */
public class CommonLoadingManager {
	
	private static CommonLoadingManager instance = null;
    private WindowManager mWm = null;
    private RelativeLayout mLayout = null;
    private CommonLoadingTimer mClose = null;
    private boolean mHaveLoadingInterface = false;
    private boolean mHomeKey = false;
    private boolean mMarketActivity = false;
    
    private CommonLoadingLayout mCommonLoadingLayout = null;
    
    private String mAdLogDes = null;
    
    private final static int TOTLE_TIME = 5000;
    private final static int FRAGMENT_TIME = 100;
    private int mTotleTimerCount = TOTLE_TIME / FRAGMENT_TIME;
    private int mCurrentTimerCount = 0;
    private boolean mFinished = false;
    private boolean mFinishedForInstall = true;

    private final static int LOADING_TIMER = 0;
    private final static int LOADING_TIMER_IN = 1;
    private final static int LOADING_TIMER_OUT = 2;
    private final static int LOADING_TIMER_ONCE = 3;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == LOADING_TIMER) {
                mCurrentTimerCount++;
                if (mClose != null) {
                    mClose.setProgress((int)(((float)mCurrentTimerCount / (float)mTotleTimerCount)*100));
                }
                if(mCurrentTimerCount < mTotleTimerCount){
                    loadingTimerThroughMessage();
                }else{
                    removeLoadingAnimation();
                }
            } else if (msg.what == LOADING_TIMER_IN) {
                mHomeKey = false;
            } else if (msg.what == LOADING_TIMER_OUT) {
                mHomeKey = true;
            } else if (msg.what == LOADING_TIMER_ONCE) {
                removeLoadingAnimation();
            }
        }
    };
    
    public synchronized static CommonLoadingManager get()
    {
    	if (instance == null)
    	{
    		instance = new CommonLoadingManager();
    	}
    	
    	return instance;
    }


    public void setMarketRunningForeground(boolean foreground) {
        if (foreground) {
            mMarketActivity = true;
            if(!mHomeKey)
                return;
            Message msg = new Message();
            msg.what = LOADING_TIMER_IN;
            if (!mHandler.hasMessages(LOADING_TIMER_IN)) {
                mHandler.sendMessageDelayed(msg, 200);
            }
        } else {
            mMarketActivity = false;
        }

    }


    public void setHomeKeyPressed() {
        if (mMarketActivity) {
            Message msg = new Message();
            msg.what = LOADING_TIMER_OUT;
            if (!mHomeKey && !mHandler.hasMessages(LOADING_TIMER_OUT)) {
                mHandler.sendMessageDelayed(msg, 250);
            }
        }

        if (!mFinished) {
            Message msg2 = new Message();
            msg2.what = LOADING_TIMER_ONCE;
            if (!mHandler.hasMessages(LOADING_TIMER_ONCE)) {
                mHandler.sendMessageDelayed(msg2, 60);
            }
        }
    }


    public void showLoadingAnimation(Context context) {
    	
        mMarketActivity = true;
        // 不是按home键返回该界面的不显示加载图片
        if (mHomeKey) {
            mHomeKey = false;
        } else {
            return;
        }

        // 有一个正在加载的图片不再去加载
        if (mHaveLoadingInterface)
            return;

        // 加载图片不存在，不再加载
        Drawable mLoadingImage = getStartupImage(context.getApplicationContext());
        if (mLoadingImage == null)
            return;
        
        mFinishedForInstall = false;

        DownloadManager.startServiceReportOffLineLog(context, ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_ENTRY_AD);
        
        mAdLogDes = getAdLogDes();
        if (mAdLogDes != null) {
            UserLogSDK.logViewShowEvent(MarketApplication.getRootContext(), mAdLogDes);
        }
        if (mWm == null) {
            mWm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        if (mLayout == null) {
            mLayout = (RelativeLayout) LayoutInflater.from(
            		context.getApplicationContext()).inflate(
                    R.layout.common_loading_interface, null);
            
            mCommonLoadingLayout = (CommonLoadingLayout) mLayout.findViewById(R.id.parent);
            mCommonLoadingLayout.setOnClickListener(new OnClickListener () {

                @Override
                public void onClick(View v) {
                    
                    GetStartPageResp startUpAdInfo = (GetStartPageResp) FrameInfoCache.getFrameInfoFromStorage("startUpAdInfo");
                    
                    if (startUpAdInfo != null) {
                        int type = startUpAdInfo.getAppInfoBto().getResType();
                        if (type == 1 || type == 2 || !TextUtils.isEmpty(startUpAdInfo.getAppInfoBto().getWebUrl())) {
                            MarketUtils.startActivityFromStartUpAd(v.getContext(), startUpAdInfo.getAppInfoBto(), true);
                            if (mAdLogDes != null) {
                                UserLogSDK.logViewClickEvent(MarketApplication.getRootContext(), mAdLogDes);
                            }
                            removeLoadingAnimation();
                        }
                    }
                }
                
            });
            
            mClose = (CommonLoadingTimer) mLayout.findViewById(R.id.close);
            mClose.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    removeLoadingAnimation();
                }

            });
        }

        mLayout.setBackgroundDrawable(mLoadingImage);

        LayoutParams mLayoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT, LayoutParams.TYPE_PHONE,
                LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, PixelFormat.TRANSPARENT);
        mWm.addView(mLayout, mLayoutParams);
        mHaveLoadingInterface = true;

        mFinished = false;
        mCurrentTimerCount = 0;
        loadingTimerThroughMessage();
    }
    
    
    private void loadingTimerThroughMessage() {
        if (mFinished) {
            if (mHandler.hasMessages(LOADING_TIMER)) {
                mHandler.removeMessages(LOADING_TIMER);
            }
            return;
        }
        Message msg = new Message();
        msg.what = LOADING_TIMER;
        if (!mHandler.hasMessages(LOADING_TIMER)) {
            mHandler.sendMessageDelayed(msg, FRAGMENT_TIME);
        }
    }


    private void removeLoadingAnimation() {
        mFinished = true;
        try {
            if (mWm != null && mLayout != null) {
                mWm.removeView(mLayout);
            }
        } catch (Exception e) {
            LogHelper.trace("CommonLoadingManager : " + e.toString());
        }
        mWm = null;
        mLayout = null;
        mCommonLoadingLayout = null;
        mClose = null;
        mHaveLoadingInterface = false;
        mFinishedForInstall = true;
        
        //for user behavior log
        UserLogSDK.entryAdExit(MarketApplication.getRootContext().getApplicationContext());
    }
    
    
    public boolean getLoadingInterfaceFinish() {
        return mFinishedForInstall;
    }


    private Drawable getStartupImage(Context context) {
    	return AsyncImageLoader.getStartupImageDrawable(context);
    }
    
    
    private String getAdLogDes() {
        GetStartPageResp startUpAdInfo = (GetStartPageResp) FrameInfoCache.getFrameInfoFromStorage("startUpAdInfo");
        if (startUpAdInfo == null) {
            return null;
        }
        
        String adLogDes = null;
        AppInfoBto appInfo = startUpAdInfo.getAppInfoBto();
        String webUrl = appInfo.getWebUrl();
        if (!TextUtils.isEmpty(webUrl)) {
            adLogDes = UserLogSDK.getAdWebDetailDes(LogDefined.VIEW_ENTRY_AD, appInfo.getName());
            
        } else {
            if (appInfo.getResType() == 2) {
                adLogDes = UserLogSDK.getAdSpecialDetailDes(LogDefined.VIEW_ENTRY_AD, Integer.toString(appInfo.getRefId()), appInfo.getName());
                
            } else if (appInfo.getResType() == 1) {
                adLogDes = UserLogSDK.getAdApkDetailDes(LogDefined.VIEW_ENTRY_AD, Integer.toString(appInfo.getRefId()), appInfo.getPackageName(), appInfo.getName());
                
            }
        }
        
        return adLogDes;
    }
}
