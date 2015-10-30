package com.zhuoyi.market.appManage;

import android.content.Context;
import android.content.Intent;

import com.zhuoyi.market.appManage.download.MarketDownloadActivity;
import com.zhuoyi.market.appManage.download.StartDownloadActivity;
import com.zhuoyi.market.appManage.favorite.MarketFavoriteActivity;
import com.zhuoyi.market.appManage.update.MarketUpdateActivity;

/**
 * 下载管理、应用更新、应用收藏对外接口
 * @author dream.zhou
 *
 */
public class AppManageUtil {

    /**
     * 启动下载管理界面
     * @param context
     */
    public static void startDownloadActivity (Context context) {
        Intent intent = new Intent(context,MarketDownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * 启动下载管理界面，带icon下载显示的
     * @param context
     * @param iconUrl:icon的下载地址
     * @param pkgName:应用的包名
     */
    public static void startDownloadActivity (Context context, String iconUrl, String pkgName) {
        Intent intent = new Intent(context, MarketDownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("loadImageiconUrl", iconUrl);
        intent.putExtra("loadImagePkgName", pkgName);
        context.startActivity(intent);
    }
    
    /**
     * 获取启动下载界面的Intent
     * @param context
     * @return
     */
    public static Intent getStartDownloadActivityIntent(Context context) {
        Intent intent = new Intent(context, StartDownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        return intent;
    }
    
    
    /**
     * 启动更新管理界面
     * @param context
     */
    public static void startUpdateActivity (Context context) {
        Intent intent = new Intent(context, MarketUpdateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * 启动应用收藏界面
     * @param context
     */
    public static void startFavoriteActivity(Context context){
        Intent intent = new Intent(context, MarketFavoriteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
}
