package com.zhuoyi.market.utils;

import com.market.featureOption.FeatureOption;

import android.util.Log;

public final class LogHelper
{
    private static String mLogTag = "LogHelper";

    private static final String CLASS_METHOD_LINE_FORMAT = "%s.%s()  Line:%d  (%s)";
    private static final String CLASS_METHOD2_LINE_FORMAT = "%s Line:%d (%s)";
    public static void trace() {
        if (FeatureOption.MARKET_LOG) {
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[3];// 从堆栈信息中获取当前被调用的方法信息
            String logText = String.format(CLASS_METHOD_LINE_FORMAT,
                    traceElement.getClassName(), traceElement.getMethodName(),
                    traceElement.getLineNumber(), traceElement.getFileName());
            Log.e(mLogTag, logText);// 打印Log
        }
    }
    
    public static void trace(String log) {
        if (FeatureOption.MARKET_LOG) {
            StackTraceElement traceElement = Thread.currentThread()
                    .getStackTrace()[3];
            String logText = String.format(CLASS_METHOD2_LINE_FORMAT,
                    log,traceElement.getLineNumber(), traceElement.getFileName());
            Log.e(mLogTag, logText);// 打印Log
        }
    }
    public static void dumpStack() {
        if (FeatureOption.MARKET_LOG) {
            new Exception(mLogTag).printStackTrace();
        }
    }
    
    public static void trace(String tag, String subTag, String func, String msg) {
    	if (!FeatureOption.MARKET_LOG) {
    		return;
    	}
    	
    	StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String logStr = "[" + subTag + "]" + traceElement.getLineNumber() + " line|" + func + "():" + msg;
        
        Log.i(tag, logStr);
    }
    
    private static final String MODULE_TAG = "tydMarketDown";
    
    public static void downloadTrace(String tag, String func, String msg) {
    	if (!FeatureOption.DOWNLOAD_LOG) {
    		return;
    	}
    	
    	StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String logStr = "[" + tag + "]" + traceElement.getLineNumber() + " line|" + func + "():" + msg;
        
        Log.i(MODULE_TAG, logStr);
    }
}
