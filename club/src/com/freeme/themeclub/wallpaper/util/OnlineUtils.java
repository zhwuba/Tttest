package com.freeme.themeclub.wallpaper.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.view.PagerAdapter;
import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;

public class OnlineUtils {
    public static String TAG = "OnlineUtils";

    private static final String FOLDERPATH = "/themes/";

    public static final String MyClickPosition = "com.tydtech.wallpaperchooser.online.position";
    public static final String MyListData = "com.tydtech.wallpaperchooser.online.MyListData";
    public static final String MySetSelect = "com.tydtech.wallpaperchooser.online.MySetSelect";

    public static final String WALLPAPER_ID = "id";
    public static final String WALLPAPER_NAME = "name";
    public static final String WALLPAPER_THUMB_URL = "dnUrlS";
    public static final String WALLPAPER_ORIGNAL_URL = "dnUrlX";
    public static final String WALLPAPER_DOWNLOAD_COUNT = "dnCnt";
    public static final String WALLPAPER_MODIFY_TIME = "modifyTime";

    public static final String TOPIC_CODE = "code";
    public static final String TOPIC_NAME = "name";
    public static final String TOPIC_THUMB_URL = "dnUrl";

    public static final int NarrowSreen = 0;
    public static final int WideSreen = 1;

    public static int getNetWorkType(Context context) {
        final int NO_NETWORK = -1;
        int netWorkType = NO_NETWORK;
        try {
            ConnectivityManager connectManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectManager == null) {
                return NO_NETWORK;
            }
            NetworkInfo[] infos = connectManager.getAllNetworkInfo();
            if (infos == null) {
                return NO_NETWORK;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if (infos[i].isConnected() && infos[i].isAvailable()) {
                    netWorkType = infos[i].getType();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return netWorkType;
    }

    public static String getSDPath() {
        String sdPath = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdPath = Environment.getExternalStorageDirectory().toString();
        }
        return sdPath;
    }

    public static void saveStringData(String data, String fileName) {
        File saveFile = null;
        String sdPath = getSDPath();
        if (TextUtils.isEmpty(sdPath) || TextUtils.isEmpty(data))
            return;
        String savePath = sdPath + FOLDERPATH + "download/cache/data/";
        saveFile = new File(savePath);
        if (saveFile != null && !saveFile.exists()) {
            saveFile.mkdirs();
        }
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(savePath
                    + fileName));
            if (osw == null)
                return;
            osw.write(data, 0, data.length());
            osw.flush();
            osw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public static boolean saveBitmap(Bitmap data,String fileName){
        File saveFile = null;
        String sdPath = getSDPath();
        if (TextUtils.isEmpty(sdPath) || data == null)
            return false;
        String savePath = sdPath + FOLDERPATH + "download/";
        saveFile = new File(savePath);
        if (saveFile != null && !saveFile.exists()) {
            saveFile.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savePath + fileName);
            data.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getStringData(String fileName) {
        String data = "";
        BufferedReader br = null;
        String sdPath = getSDPath();
        if (TextUtils.isEmpty(sdPath))
            return data;
        String savePath = sdPath + FOLDERPATH + "download/cache/data/";
        File saveFile = new File(savePath);
        if (saveFile != null && !saveFile.exists())
            saveFile.mkdirs();
        String temp = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(
                    saveFile + "/" + fileName)));
            while ((temp = br.readLine()) != null) {
                data = data + temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static List<Map<String, Object>> splitThumbListJSON(String result){
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
            array = object.getJSONArray("wallPaperList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++)
            {
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put(WALLPAPER_ID, object.get("id"));
                map.put(WALLPAPER_NAME, object.get("name"));
                map.put(WALLPAPER_THUMB_URL, object.get("wallpaperBeautifyImageS"));
                map.put(WALLPAPER_ORIGNAL_URL, object.get("dnUrlX"));
                map.put(WALLPAPER_DOWNLOAD_COUNT, object.get("dnCnt"));
                map.put(WALLPAPER_MODIFY_TIME, object.get("modifyTime"));
                list.add(map);
            }
        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }	
        return list;
    }

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

    public static List<Map<String, Object>> splitSpecialListJSON(String result){
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
            Log.v(TAG, "e1"+e1.toString());
            e1.printStackTrace();
            return list;
        }
        Map<String, Object> map;
        JSONObject object;
        JSONArray array = null;
        try
        {
            object = new JSONObject(jsonString);
            array = object.getJSONArray("topicList");
            list = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < array.length(); i++)
            {
                map = new HashMap<String, Object>();

                object = array.getJSONObject(i);
                map.put(TOPIC_CODE, object.get("code"));
                map.put(TOPIC_NAME, object.get("name"));
                map.put(TOPIC_THUMB_URL, object.get("dnUrl"));
                list.add(map);
            }
        }catch (Exception e) {
            Log.v(TAG, "e"+e.toString());
            e.printStackTrace();
        }	
        return list;
    }

    public static boolean checkIsDownLoaded(String name){
        String path = getSDPath() + FOLDERPATH + "download/";
        try {
            File mPagerFile = new File(path + name);
            return mPagerFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    } 

    public static boolean scrollButtom(View view , View flag) {
        boolean loading = (((ScrollView) view).getChildAt(0)
                .getMeasuredHeight() <= view.getHeight() + view.getScrollY())
                && flag.getVisibility() == View.GONE;
        return loading;
    }

    public static String[] getAvailableResolutionForThisDevice(Context context, String[] resolutions){
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        String height = Integer.toString(outMetrics.heightPixels);
        String[] availableResolutions = new String[2];

        for(int i=0, j=0; i<resolutions.length; i++){
            if(resolutions[i].startsWith(height)){
                availableResolutions[j] = resolutions[i];
                j++;
                if(j==2){
                    break;
                }
            }
        }

        return availableResolutions;
    }

    public static String getAvailableTopicResolution(Context context, String[] resolutions){
        DisplayMetrics outMetrics = context.getResources().getDisplayMetrics();
        if(outMetrics.heightPixels==800 && outMetrics.widthPixels==480){
            return resolutions[0];
        }else if(outMetrics.heightPixels==854 && outMetrics.widthPixels==480){
            return resolutions[0];
        }else if(outMetrics.heightPixels==960 && outMetrics.widthPixels==540){
            return resolutions[1];
        }else if(outMetrics.heightPixels==1280 && outMetrics.widthPixels==720){
            return resolutions[2];
        }else if(outMetrics.heightPixels==1920 && outMetrics.widthPixels==1080){
            return resolutions[3];   		
        }else{
            return resolutions[0];
        }
    }


    public static class MyPagerAdapter extends PagerAdapter {
        private List<View> dataList;

        public MyPagerAdapter(ArrayList<View> dataList) {
            this.dataList = dataList;
        }

        public int getCount() {
            return dataList.size();
        }

        public void destroyItem(View container,int position, Object object){
            ((ViewGroup) container).removeView((View) object);
            object = null;
        }

        public Object instantiateItem(View container, int position) {
            ((ViewGroup) container).addView(dataList.get(position), 0);
            return dataList.get(position);
        }

        public boolean isViewFromObject(View container, Object object) {
            return container == (object);
        }
    }
    public static class WallpaperThumbAdapter extends BaseAdapter {
        private Context mContext;
        private List<Map<String, Object>> mDataList;
        private LayoutInflater mLayoutInflater;
        private AsyncImageCache mAsyncImageCache;
        private Resources res;
        private boolean mNarrowSreen = true;
        private boolean mFirst = true;

        public WallpaperThumbAdapter(Context context,
                List<Map<String, Object>> dataList,AsyncImageCache mAsyncImageCache) {
            this.mContext = context;
            this.mDataList = dataList;
            this.mAsyncImageCache = mAsyncImageCache;
            mLayoutInflater = LayoutInflater.from(mContext);
            res = mContext.getResources();
        }

        public void setSreen(boolean flag){
            mNarrowSreen = flag;
        }

        public void setFlagFirst(){
            mFirst = true;
        }

        public void setAdapterData(List<Map<String, Object>> dataList) {
            this.mDataList = dataList;
        }

        public List<Map<String, Object>> getAdapterData() {
            return mDataList;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ThumbHolder mThumbHolder = null;
            if(convertView==null){
                convertView=mLayoutInflater.inflate(R.layout.wallpaper_thumb_griditem, null);
                mThumbHolder=new ThumbHolder();

                mThumbHolder.previewThumb = (ImageView) convertView
                        .findViewById(R.id.preview_thumb_item);
                mThumbHolder.status = (ImageView) convertView
                        .findViewById(R.id.thumb_status_iv);

                convertView.setTag(mThumbHolder);
            }else{
                mThumbHolder=(ThumbHolder) convertView.getTag();
            }

            if(mFirst || parent.getChildCount() != position){
                mFirst = false;
                return convertView;
            }
            if(!mNarrowSreen)
                mThumbHolder.previewThumb.setLayoutParams(new LayoutParams(
                        res.getDimensionPixelSize(R.dimen.wide_thumb_preview_w), 
                        res.getDimensionPixelSize(R.dimen.wide_thumb_preview_h)));

            Map<String, Object> thumbItem = mDataList.get(position);
            String thumbUrl = (String) thumbItem.get(WALLPAPER_THUMB_URL);
            mAsyncImageCache.displayImage(mThumbHolder.previewThumb,
                    R.drawable.wallpaper_no_default,
                    res.getDimensionPixelSize(mNarrowSreen ? R.dimen.thumb_preview_w : R.dimen.wide_thumb_preview_w),
                    res.getDimensionPixelSize(mNarrowSreen ? R.dimen.thumb_preview_h : R.dimen.wide_thumb_preview_h),
                    new AsyncImageCache.NetworkImageGenerator(thumbUrl,
                            thumbUrl));
            mThumbHolder.status.setVisibility(OnlineUtils.checkIsDownLoaded(
                    thumbItem.get(WALLPAPER_ID)+(String)thumbItem.get(WALLPAPER_NAME))?View.VISIBLE:View.GONE);

            return convertView;
        }

        class ThumbHolder {
            ImageView previewThumb;
            ImageView status;
        }
    }
}
