package com.market.behaviorLog;

import org.json.JSONException;
import org.json.JSONObject;

import com.zhuoyi.market.appResident.MarketApplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;

/**
 * interface class about user behavior log module
 * 
 * @author Athlon
 *
 */
public class UserLogSDK {
    private static final String TAG = "UserLogSDK";
    
    private static void sendMessageToService(Context context, int msg, String des) {
        if (context == null) {
            context = MarketApplication.getRootContext();
        }
        
        if (!LogSettings.isLogSwitchOpen(context)) {
            //function switch is closed, do nothing
            return;
        }
        long millis = System.currentTimeMillis();
        //Log.i(TAG, "des=" + des + ", msg=" + msg + ", millis=" + millis);
        Handler handle = LogService.getLogHandler();
        if (handle == null) {
            Intent intent = new Intent(context, LogService.class);
            intent.putExtra(LogService.EXTRA_MSG, msg);
            intent.putExtra(LogService.EXTRA_MILLIS, millis);
            if (des != null) {
                intent.putExtra(LogService.EXTRA_DES, des);
            }
            context.startService(intent);
            
        } else {
            Message message = new Message();
            message.what = msg;
            Bundle data = new Bundle();
            data.putLong(LogService.EXTRA_MILLIS, millis);
            if (des != null) {
                data.putString(LogService.EXTRA_DES, des);
            }
            message.setData(data);
            
            handle.sendMessage(message);
            
        }
    }
    
    
    /**
     * called when application exit
     * @param context
     */
    public static void applicationExit(Context context) {
        sendMessageToService(context, LogService.MSG_APP_EXIT, null);
    }
    
    
    /**
     * called when wifi connect available
     * @param context
     */
    public static void wifiAvailabled(Context context) {
        sendMessageToService(context, LogService.MSG_WIFI_UPLOAD, null);
    }
    
    
    /**
     * when activity resume, called to record log
     * @param context
     * @param activityDes   description string
     */
    public static void logActivityEntry(Context context, String activityDes) {
        sendMessageToService(context, LogService.MSG_ACTIVITY_ENTRY, activityDes);
    }
    
    
    /**
     * when activity paused, called to record log
     * @param context
     * @param activityDes   description string
     */
    public static void logActivityExit(Context context, String activityDes) {
        sendMessageToService(context, LogService.MSG_ACTIVITY_EXIT, activityDes);
    }
    
    
    /**
     * called when entry ad exit, because entry ad is not activity, 
     * and it will effect the time record about active activity
     * @param context
     */
    public static void entryAdExit(Context context) {
        sendMessageToService(context, LogService.MSG_ENTRY_AD_EXIT, null);
    }
    
    
    /**
     * when view show, called to record log
     * @param context
     * @param viewDes
     */
    public static void logViewShowEvent(Context context, String viewDes) {
        sendMessageToService(context, LogService.MSG_VIEW_SHOW_EVENT, viewDes);
    }
    
    
    /**
     * when click view, called to record log
     * @param context
     * @param viewDes
     */
    public static void logViewClickEvent(Context context, String viewDes) {
        sendMessageToService(context, LogService.MSG_VIEW_CLICK_EVENT, viewDes);
    }
    
    
    /**
     * when event happen, called to record log
     * @param context
     * @param countDes
     */
    public static void logCountEvent(Context context, String countDes) {
        sendMessageToService(context, LogService.MSG_COUNT_EVENT, countDes);
    }
    
    
    /**
     * for get description of key
     * @param key  it must be one of following:<br/>
     * {@link LogDefined#ACTIVITY_MAIN}<br/>
     * {@link LogDefined#ACTIVITY_SOFT_ADVICE}<br/>
     * {@link LogDefined#ACTIVITY_SOFT_SORT}<br/>
     * {@link LogDefined#ACTIVITY_SOFT_RANK}<br/>
     * {@link LogDefined#ACTIVITY_GAME_ADVICE}<br/>
     * {@link LogDefined#ACTIVITY_GAME_SORT}<br/>
     * {@link LogDefined#ACTIVITY_GAME_RANK}<br/>
     * {@link LogDefined#ACTIVITY_NOVEL_HOT}<br/>
     * {@link LogDefined#ACTIVITY_NOVEL_NEW}<br/>
     * {@link LogDefined#ACTIVITY_BOY_JOY}<br/>
     * {@link LogDefined#ACTIVITY_GIRL_JOY}<br/>
     * {@link LogDefined#ACTIVITY_GUESS_JOY}<br/>
     * {@link LogDefined#ACTIVITY_MAIN_CLUB}<br/>
     * {@link LogDefined#ACTIVITY_FIND_CLUB}<br/>
     * {@link LogDefined#ACTIVITY_IN_GAME}<br/>
     * {@link LogDefined#ACTIVITY_IN_SOFT}<br/>
     * {@link LogDefined#ACTIVITY_GOTTA}<br/>
     * {@link LogDefined#COUNT_DOWNLOAD_VIEW}<br/>
     * {@link LogDefined#COUNT_FAVORITE_VIEW}<br/>
     * {@link LogDefined#COUNT_SETTING_VIEW}<br/>
     * {@link LogDefined#COUNT_UPDATE_VIEW}<br/>
     * @return description string
     */
    public static String getKeyDes(String key) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * for get description of app detail activity
     * @param id    appId
     * @param pkgName   package name
     * @param appName   application name
     * @return description string
     */
    public static String getAppDetailActivityDes(String id, String pkgName, String appName) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.ACTIVITY_APK_DETAIL);
            jo.put(LogDefined.KEY_ID, id);
            jo.put(LogDefined.KEY_PACKAGE_NAME, pkgName);
            jo.put(LogDefined.KEY_NAME, appName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * for get description of special detail activity
     * @param topicId   topicId of special
     * @param topicName topicName of special
     * @return description string
     */
    public static String getSpecialDetailActivityDes(String topicId, String topicName) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.ACTIVITY_CLUB_DETAIL);
            jo.put(LogDefined.KEY_ID, topicId);
            jo.put(LogDefined.KEY_NAME, topicName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * 
     * @param key   it must be one of following:<br/>
     * {@link LogDefined#ACTIVITY_GAME_CLASS}<br/>
     * {@link LogDefined#ACTIVITY_SOFT_CLASS}<br/>
     * @param secondClass   second class name
     * @param thirdClass    third class name
     * @return description string
     */
    public static String getClassDetailActivityDes(String key, String secondClass, String thirdClass) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
            jo.put(LogDefined.KEY_TWO_SORT, secondClass);
            jo.put(LogDefined.KEY_THIRD_SORT, thirdClass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * get description of ad about apk detail info
     * @param key   it must be one of following:<br/>
     * {@link LogDefined#VIEW_ENTRY_AD}<br/>
     * {@link LogDefined#VIEW_HOME_AD}<br/>
     * {@link LogDefined#COUNT_HOME_AD}<br/>
     * @param id    app id
     * @param pkgName   package name
     * @param appName   application name
     * @return description string
     */
    public static String getAdApkDetailDes(String key, String id, String pkgName, String appName) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
            jo.put(LogDefined.KEY_TYPE, LogDefined.CIK_TYPE_APK_DETAIL);
            jo.put(LogDefined.KEY_ID, id);
            jo.put(LogDefined.KEY_PACKAGE_NAME, pkgName);
            jo.put(LogDefined.KEY_NAME, appName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * get description of ad about special detail info
     * @param key   it must be one of following:<br/>
     * {@link LogDefined#VIEW_ENTRY_AD}<br/>
     * {@link LogDefined#VIEW_HOME_AD}<br/>
     * {@link LogDefined#COUNT_HOME_AD}<br/>
     * @param id    topic id
     * @param name  topic name
     * @return description string
     */
    public static String getAdSpecialDetailDes(String key, String id, String name) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
            jo.put(LogDefined.KEY_TYPE, LogDefined.CIK_TYPE_SPECAIL_DETAIL);
            jo.put(LogDefined.KEY_ID, id);
            jo.put(LogDefined.KEY_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * get description of ad about web detail info
     * @param key   it must be one of following:<br/>
     * {@link LogDefined#VIEW_ENTRY_AD}<br/>
     * {@link LogDefined#VIEW_HOME_AD}<br/>
     * {@link LogDefined#COUNT_HOME_AD}<br/>
     * @param name  web detail description name
     * @return description string
     */
    public static String getAdWebDetailDes(String key, String name) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
            jo.put(LogDefined.KEY_TYPE, LogDefined.CIK_TYPE_WEB_VIEW);
            jo.put(LogDefined.KEY_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    /**
     * get description of search hot word
     * @param key   it must be one of following:<br/>
     * {@link LogDefined#VIEW_SEARCH_KEY_WORD}<br/>
     * {@link LogDefined#COUNT_SEARCH_WORD}<br/>
     * {@link LogDefined#COUNT_SCROLL_WORD_CLICK}<br/>
     * @param word  search word
     * @return description string
     */
    public static String getSearchWordDes(String key, String word) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, key);
            jo.put(LogDefined.KEY_HOT_WORD, word);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static String getRankChildActivityDes(String title, int assemblyId) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.ACTIVITY_RANK_CHILD);
            jo.put(LogDefined.KEY_NAME, title);
            jo.put(LogDefined.KEY_ID, assemblyId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static String getFindChildActivityDes(int topicId) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.ACTIVITY_FIND_CHILD);
            jo.put(LogDefined.KEY_ID, topicId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static String getWallPaperClassActivityDes(String name, String code) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.ACTIVITY_WALL_PAPER_CLASS);
            jo.put(LogDefined.KEY_NAME, name);
            jo.put(LogDefined.KEY_CODE, code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static String getWallPaperDetailViewDes(String name, int id) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.VIEW_WALL_PAPER_DETAIL);
            jo.put(LogDefined.KEY_NAME, name);
            jo.put(LogDefined.KEY_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
    
    
    public static String getWallPaperDownDes(String name, int id) {
        JSONObject jo = new JSONObject();
        
        try {
            jo.put(LogDefined.KEY_KEY, LogDefined.COUNT_WALL_PAPER_DOWN);
            jo.put(LogDefined.KEY_NAME, name);
            jo.put(LogDefined.KEY_ID, id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return jo.toString();
    }
}
