package com.market.net.utils;

import android.os.Build;
import android.os.Handler;

import com.zhuoyi.market.asyncTask.MarketAsyncTask;
import com.zhuoyi.market.asyncTask.MarketRequestTask;
import com.zhuoyi.market.asyncTask.TaskManager;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;

public class StartNetReqUtils 
{
	
    public static void execApkSelfUpdateRequest(Handler handler, int msgWhat,int messageCode,String contents)
    {
    	if(Build.VERSION.SDK_INT >= 11) {
    		new RequestAsyncTask(handler, msgWhat,contents).executeOnExecutor(MarketUtils.getDataReqExecutor(),
    				Constant.SELF_UPDATE_URL, messageCode);
    	} else {
    		new RequestAsyncTask(handler, msgWhat,contents).execute(
    				Constant.SELF_UPDATE_URL, messageCode);
    	}
    }

    public static void execMarketRequest(Handler handler,int msgWhat,int messageCode,String contents)
    {
    	if(Build.VERSION.SDK_INT >= 11) {
    		new RequestAsyncTask(handler, msgWhat,contents).executeOnExecutor(MarketUtils.getDataReqExecutor(),
    				Constant.MARKET_URL,messageCode);
    	} else {
    		new RequestAsyncTask(handler, msgWhat,contents).execute(
    				Constant.MARKET_URL,messageCode);
    	}
    }

    public static void execListByPageRequest(Handler handler,
            int msgWhat,int messageCode,String contents)
    {
        if (handler != null) {
            MarketAsyncTask task = new MarketRequestTask(handler.toString(), "list", handler, msgWhat, contents, Constant.MARKET_URL, messageCode);
            TaskManager.getInstance().startTask(task, true);
        }
       /* try {
        	if(Build.VERSION.SDK_INT >= 11) {
        		new RequestAsyncTask(handler, msgWhat, contents).executeOnExecutor(AppStoreUtils.getDataReqExecutor(),
        				Constant.MARKET_URL, messageCode);
        	} else {
        		new RequestAsyncTask(handler, msgWhat, contents).execute(
        				Constant.MARKET_URL, messageCode);
        	}
        } catch (RejectedExecutionException e) {
            LogHelper.trace();
        }*/
    }
}
