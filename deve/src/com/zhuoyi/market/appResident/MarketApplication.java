package com.zhuoyi.market.appResident;

import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.zhuoyi.market.appdetail.AppDetailInfoActivity;
import com.zhuoyi.market.badger.ShortcutBadger;
import com.zhuoyi.market.cleanTrash.TrashService;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.constant.GetPublicPara;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.util.Util;
import com.market.net.data.AppInfoBto;
import com.market.net.response.GetMarketFrameResp;
import com.market.updateSelf.UpdateManager;
import com.market.view.CommonLoadingManager;
import com.zhuoyi.market.manager.MarketNotificationHelper;
import com.zhuoyi.market.receiver.HomePressedRecevier;
import com.zhuoyi.market.receiver.HomePressedRecevier.OnHomePressListener;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.CrashHandler;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;

public class MarketApplication extends Application implements OnHomePressListener{

    public static String mChannelID = "";
    public static String mCpID = "";
    public static String mUUID = "";
    private static GetMarketFrameResp mGetMarketFrameResp = null;
    private static List<AppInfoBto> appUpdateList = null;
    private static List<AppInfoBto> silentUpdateList = null;
    public static int splashCount = 0;
    private static MarketApplication mApplicationContext = null;

    private HomePressedRecevier mHomePressedRecevier;
    public static boolean isHomePressed = false;
    
    public MarketApplication() {
        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        
        if (currentapiVersion >= 14) {
            this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
    
                @Override
                public void onActivityResumed(Activity activity) {
                    if(mHomePressedRecevier == null){
                        registerReceiver();
                        mHomePressedRecevier.setOnHomePressListener(MarketApplication.this);
                        
                        /** 清除桌面角标提示    **/
                        ShortcutBadger mShortcutBadger = ShortcutBadger.getBadgerImpl(getApplicationContext());
                        if(mShortcutBadger != null) {
                            Util.setCompaignsNotifyFlag(getApplicationContext(), false);
                            mShortcutBadger.clearBadge();
                        }
                    }
                    if (isHomePressed) {
                        CommonLoadingManager.get().setMarketRunningForeground(true);
                        isHomePressed = false;
                    }
                }
    
                @Override
                public void onActivityPaused(Activity activity) {
                    
                }
    
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onActivityStarted(Activity activity) {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onActivityStopped(Activity activity) {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onActivityDestroyed(Activity activity) {
                    // TODO Auto-generated method stub
                    
                }

            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationContext = this;
        mUUID = GetPublicPara.getMyUUID(getApplicationContext());
        GetPublicPara.getPublicParaForPush(getApplicationContext(), "cp", "td");
        MarketNotificationHelper.get().init();

        SettingData.initializeParams(getApplicationContext());
        
        initImageCache();
        
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());

        Constant.initMarketUrl(getApplicationContext());
        
        Intent intent = new Intent(this, TrashService.class);
        intent.putExtra(TrashService.EXTRA_FROM_KEY, TrashService.FROM_MARKET_START);
        startService(intent);
    }
	
    
    public static MarketApplication getInstance() {
        return mApplicationContext;
    }
    
    
	/**
	 * 获取Application Context
	 * @return
	 */
    public static Context getRootContext() {
        return mApplicationContext;
    }
	
    
    public void applicationExit() {
        UserLogSDK.applicationExit(this);
        UpdateManager.installSelfUpdateApk(this);
        
        SettingData.resetDelayDownFlag();
    }
    
    
	/**
	 * 市场图片加载Cache初始化
	 */
	private void initImageCache() {
	    String cachePath = MarketUtils.FileManage.getSDPath();
        AsyncImageCache.setDiskCacheEnable(true);                       //Disk缓存默认关闭的
        AsyncImageCache.setDiskCacheDir(cachePath);                     //默认存储位置为/data/data/<package>/cache
        if(isHdpi()) {
            AsyncImageCache.setDiskCacheSize(1024 * 1024 * 60);               
            AsyncImageCache.setDiskCacheCount(500);                              
            AsyncImageCache.setMemoryCacheSize(1024 * 1024 * 14);
        } else {
            AsyncImageCache.setDiskCacheSize(1024 * 1024 * 40);         //默认10MB, 如果存储位置改成T卡, 可适当设大
            AsyncImageCache.setDiskCacheCount(400);                     //默认64, 如果存储位置改成T卡, 可适当设大
            AsyncImageCache.setMemoryCacheSize(1024 * 1024 * 8);        //默认heap memory * 1/8, 比如H1是128/8=16MB, 建议保持默认或调小
        }
	}
	
	/**
	 * 区分高端和低端手机
	 * @return
	 */
    private boolean isHdpi() {
        int dpi = getResources().getDisplayMetrics().densityDpi;
        if (dpi >= DisplayMetrics.DENSITY_HIGH)
            return true;
        else
            return false;
    }
	
	/**
	 * 保存框架信息
	 * @param resp
	 */
	public static void setMarketFrameResp(GetMarketFrameResp resp) {
	    mGetMarketFrameResp = resp;
	}
	
	/**
	 * 获取框架信息
	 * @return
	 */
	public static GetMarketFrameResp getMarketFrameResp() {
		if(mGetMarketFrameResp == null) {
			mGetMarketFrameResp = (GetMarketFrameResp)FrameInfoCache.getFrameInfoFromStorage("marketframe");
		}
	    return mGetMarketFrameResp;
	}
	
	/**
	 * 保存后台更新列表
	 * @param list
	 */
    public static void setSilentUpdateList(List<AppInfoBto> list) {
        silentUpdateList = list;
    }
    	
    /**
     * 获取后台更新列表
     * @return
     */
    public static List<AppInfoBto> getSilentUpdateList() {
        return silentUpdateList;
    }

    /**
     * 获取更新列表
     * @return
     */
    public static List<AppInfoBto> getAppUpdateList() {
        return appUpdateList;
    }

    /**
     * 保存更新列表
     * @param list
     */
    public static void setAppUpdateList(List<AppInfoBto> list) {
        appUpdateList = list;
    }

    /**
     * 清除更新列表
     */
    public static void clearAppUpdateList() {
        if (appUpdateList != null) {
            appUpdateList.clear();
            appUpdateList = null;
        }
    }

    /**
     * 通过package name获取更新列表中的单个应用
     * @param pkgName
     * @return
     */
    public static AppInfoBto getAppInfoBtoFromUpdateList(String pkgName) {
        AppInfoBto infoBto;
        for (int i = 0; appUpdateList != null && i < appUpdateList.size(); i++) {
            infoBto = appUpdateList.get(i);
            if (infoBto.getPackageName().equals(pkgName)) {
                return infoBto;
            }
        }

        return null;
    }

    /**
     * 通过package name删除更新列表中的某个应用
     * @param pkgName
     */
    public static void removeFromUpdateList(String pkgName) {
        AppInfoBto infoBto;
        for (int i = 0; appUpdateList != null && i < appUpdateList.size(); i++) {
            infoBto = appUpdateList.get(i);

            if (infoBto == null)
                return;

            if (infoBto.getPackageName().equals(pkgName)) {
                appUpdateList.remove(i);
                break;
            }
        }
    }
    
    
    /**
     * 注册监听Home键广播
     */
    public void registerReceiver() {
        mHomePressedRecevier = new HomePressedRecevier();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomePressedRecevier, filter);
    }
    

    @Override
    public void onHomePressed(Context context) {
        isHomePressed = true;
        if (mHomePressedRecevier != null) {
            unregisterReceiver(mHomePressedRecevier);
            mHomePressedRecevier = null;
        }
        
        /**
         * 设置详情返回不弹窗
         */
        AppDetailInfoActivity.setIsFromInner(true);
        CommonLoadingManager.get().setMarketRunningForeground(false);
    }
}
