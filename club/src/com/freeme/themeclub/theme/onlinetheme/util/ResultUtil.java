package com.freeme.themeclub.theme.onlinetheme.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class ResultUtil {

    private static final String TAG = "ResultUtil";

    public static final String WALLPAPER_ID = "id";
    public static final String WALLPAPER_NAME = "name";
    public static final String WALLPAPER_THUMB_URL = "dnUrlS";
    public static final String WALLPAPER_ORIGNAL_URL = "dnUrlX";
    public static final String WALLPAPER_DOWNLOAD_COUNT = "dnCnt";
    public static final String WALLPAPER_MODIFY_TIME = "modifyTime";


    public static List<Map<String, Object>> splitWallpaperDetailListJSON(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result))
        {
            return list;
        }

        String jsonString = null;
        try
        {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        try
        {
            object = new JSONObject(jsonString);
            object = object.getJSONObject("wallPaper");
            list = new ArrayList<Map<String, Object>>();

            map = new HashMap<String, Object>();

            map.put(WALLPAPER_ID, object.get("id"));
            map.put(WALLPAPER_NAME, object.get("name"));
            map.put(WALLPAPER_THUMB_URL, object.get("dnUrlS"));
            map.put(WALLPAPER_ORIGNAL_URL, object.get("dnUrlX"));
            map.put(WALLPAPER_DOWNLOAD_COUNT, object.get("dnCnt"));
            map.put(WALLPAPER_MODIFY_TIME, object.get("modifyTime"));
            list.add(map);

        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }   
        return list;
    }

    public static List<Map<String, Object>> splitThemeServerListData(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result))
        {
            return list;
        }

        String jsonString = null;
        try
        {
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }
        catch (JSONException e1)
        {
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try
        {
            object = new JSONObject(jsonString);
            array = object.getJSONArray("themeList");
            list = new ArrayList<Map<String, Object>>();

            for (int i = 0; i < array.length(); i++)
            {
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put("id", object.get("id"));
                map.put("name", object.get("name"));
                map.put("author", object.get("author"));
                map.put("ver", object.get("ver"));
                map.put("type", object.get("type"));
                map.put("size", object.get("size"));
                map.put("packageName", object.get("packageName"));
                map.put("logoUrl", object.get("logoUrl"));
                map.put("brief", object.get("brief"));
                map.put("description", object.get("description"));
                map.put("dnCnt", object.get("dnCnt"));
                map.put("subType", object.get("subType"));
                map.put("name", object.get("name"));

                JSONArray jsonArray;
                JSONObject jsonObjct;
                List<String> previewList = new ArrayList<String>();
                jsonArray = object.getJSONArray("previewList");
                for(int j = 0; j < jsonArray.length(); j++){
                    jsonObjct = jsonArray.getJSONObject(j);
                    previewList.add(jsonObjct.getString("url"));
                }
                map.put("previewList", previewList);
                map.put("dnUrl", object.get("dnUrl"));
                map.put("subType", object.get("subType"));
                list.add(map);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }	
        return list;
    }

    public static List<Map<String, Object>> splitLockScreenServerListData(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try{
            object = new JSONObject(jsonString);
            array = object.getJSONArray("screenList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++){
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put("id", object.get("id"));
                map.put("name", object.get("name"));
                map.put("author", object.get("author"));
                map.put("ver", object.get("ver"));
                map.put("size", object.get("size"));
                map.put("packageName", object.get("packageName"));
                map.put("logoUrl", object.get("logoUrl"));
                map.put("brief", object.get("brief"));
                map.put("description", object.get("description"));
                map.put("dnCnt", object.get("dnCnt"));
                map.put("name", object.get("name"));

                JSONArray jsonArray;
                JSONObject jsonObjct;
                List<String> previewList = new ArrayList<String>();
                jsonArray = object.getJSONArray("previewList");
                for(int j = 0; j < jsonArray.length(); j++){
                    jsonObjct = jsonArray.getJSONObject(j);
                    previewList.add(jsonObjct.getString("url"));
                }
                map.put("previewList", previewList);
                map.put("subType", object.get("subType"));
                map.put("dnUrl", object.get("dnUrl"));
                list.add(map);
            }
        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }	
        return list;
    }

    public static List<Map<String, Object>> splitCategoryServerListData(String result,boolean hasShow){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;

        try{
            object = new JSONObject(jsonString);
            array = object.getJSONArray("beautifyTypeList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++){
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put("name", object.get("name"));
                map.put("code", object.get("code"));
                map.put("dnUrls",  object.get("dnUrls"));
                if(hasShow){
                    map.put("beautifyShow",  object.get("beautifyShow"));
                }
                map.put("beautifySort",  object.get("beautifySort"));

                list.add(map);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        //        Collections.sort(list, new Comparator<Map<String, Object>>() {
        //
        //            @Override
        //            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
        //
        //                if ((Integer)lhs.get("beautifySort")<(Integer)rhs.get("beautifySort"))
        //                    return -1;
        //                return 1;
        //            }
        //        });
        return list;
    }

    public static Map<String, Object> splitADroiADServerData(String result){
        Map<String, Object> map = null;
        if (TextUtils.isEmpty(result)) {
            return map;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return map;
        }
        JSONObject object;
        try{
            object = new JSONObject(jsonString);
            object = object.getJSONObject("advertisingSwitch");
            map = new HashMap<String, Object>();

            map.put("flag", object.get("flag"));
            map.put("timePeriod", object.get("timePeriod"));

        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }   
        return map;
    }

    public static List<Map<String, Object>> splitADServerListData(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)) {
            return list;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try{
            object = new JSONObject(jsonString);
            array = object.getJSONArray("advertisingList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++){
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put("adverName", object.get("adverName"));
                map.put("adverUrl", object.get("adverUrl"));
                map.put("subType", object.get("subType"));
                map.put("subId", object.get("subId"));
                list.add(map);
            }
        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }   
        return list;
    }

    public static List<Map<String, Object>> splitThemeDetailServerListData(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)){
            return list;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        try{
            object = new JSONObject(jsonString);
            object = object.getJSONObject("theme");
            list = new ArrayList<Map<String, Object>>();

            map = new HashMap<String, Object>();

            map.put("id", object.get("id"));
            map.put("name", object.get("name"));
            map.put("author", object.get("author"));
            map.put("ver", object.get("ver"));
            map.put("size", object.get("size"));
            map.put("packageName", object.get("packageName"));
            map.put("logoUrl", object.get("logoUrl"));
            map.put("brief", object.get("brief"));
            map.put("description", object.get("description"));
            map.put("dnCnt", object.get("dnCnt"));
            map.put("name", object.get("name"));

            JSONArray jsonArray;
            JSONObject jsonObjct;
            List<String> previewList = new ArrayList<String>();
            jsonArray = object.getJSONArray("previewList");
            for(int j = 0; j < jsonArray.length(); j++){
                jsonObjct = jsonArray.getJSONObject(j);
                previewList.add(jsonObjct.getString("url"));
            }
            map.put("previewList", previewList);
            map.put("dnUrl", object.get("dnUrl"));
            list.add(map);

        }catch (Exception e) {
            e.printStackTrace();
        } 
        return list;
    }

    public static List<Map<String, Object>> splitScreenDetailServerListData(String result){
        List<Map<String, Object>> list = null;
        if (TextUtils.isEmpty(result)){
            return list;
        }

        String jsonString = null;
        try{
            JSONObject jsonObject = new JSONObject(result.trim());
            jsonString = (String) jsonObject.getString("body");
        }catch (JSONException e1){
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        try{
            object = new JSONObject(jsonString);
            object = object.getJSONObject("screen");
            list = new ArrayList<Map<String, Object>>();

            map = new HashMap<String, Object>();

            map.put("id", object.get("id"));
            map.put("name", object.get("name"));
            map.put("author", object.get("author"));
            map.put("ver", object.get("ver"));
            map.put("size", object.get("size"));
            map.put("packageName", object.get("packageName"));
            map.put("logoUrl", object.get("logoUrl"));
            map.put("brief", object.get("brief"));
            map.put("description", object.get("description"));
            map.put("dnCnt", object.get("dnCnt"));
            map.put("name", object.get("name"));

            JSONArray jsonArray;
            JSONObject jsonObjct;
            List<String> previewList = new ArrayList<String>();
            jsonArray = object.getJSONArray("previewList");
            for(int j = 0; j < jsonArray.length(); j++){
                jsonObjct = jsonArray.getJSONObject(j);
                previewList.add(jsonObjct.getString("url"));
            }
            map.put("previewList", previewList);
            map.put("dnUrl", object.get("dnUrl"));
            list.add(map);

        }catch (Exception e) {
            e.printStackTrace();
        } 
        return list;
    }
}
