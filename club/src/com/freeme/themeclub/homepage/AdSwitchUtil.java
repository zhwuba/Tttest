package com.freeme.themeclub.homepage;


import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class AdSwitchUtil {
    private static final String TAG = AdSwitchUtil.class.getSimpleName();

    private static final String URLString = "http://onlinebiz.oo523.com:2011";
    
    private static final String sharesf_name = "com.droi.Adswitch";
    private static final String key_deviceUUID = "device_UUID";
    private static final String key_request = "request_string";
    private static final String key_time = "last_time";
    private static final String algorithm = "DES/ECB/NoPadding";
    private static final String ENCODE_DECODE_KEY = "x_s0_s22";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static SharedPreferences sharePrefs;

    private static String queryUpdate(Context mCtx) {
        String content = getUpdateQueryRequestContent(mCtx);
        String responce = null;
        try {
            Log.i(TAG, "content:" + content);
            responce = queryUpdateDoPost(URLString, content);
            Log.i(TAG, "responce:" + responce);
        } catch (Exception e) {
        }
        return responce;
    }
    
    public static int getAdSwitch(final Context mCtx,final Handler mHandler){
        int flag = 0;
        
        if(sharePrefs == null){ sharePrefs = mCtx.getSharedPreferences(sharesf_name, Context.MODE_PRIVATE); }
        String lastLinkTime = sharePrefs.getString(key_time, "1970-12-12 12:12:12"); 
        String request = sharePrefs.getString(key_request, "");
        flag = getFlagByRequest(request);
        boolean isNeedLink = true; 
        try {
            Date lastDate = sdf.parse(lastLinkTime);
            Date nowData = new Date();
            int lastDay = lastDate.getDay();
            int nowDay = nowData.getDay();
            long timePer = nowData.getTime() - lastDate.getTime();
            if( timePer < 60*60*1000 ){
                isNeedLink  = false;
            }
            if (nowDay != lastDay) {
                flag = 0;
            }
        } catch (ParseException e) {
        }
        
        if(isNeedLink == false){
            mHandler.sendEmptyMessage(flag);        
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String request = queryUpdate(mCtx);
                    int newFlag = getFlagByRequest(request);
                    mHandler.sendEmptyMessage(newFlag); 
                    if(!TextUtils.isEmpty(request)){
                        Editor edit = sharePrefs.edit();
                        edit.putString(key_time, sdf.format(new Date()));
                        edit.putString(key_request, request);
                        edit.commit();
                    }
                }
            }).start();
        }
        Log.i(TAG,"ad_switch:"+flag);
        return flag;
    }
    
    //"body":"{\"advertisingSwitch\":{\"timePeriod\":\"0-24\"},"errorCode": 0}"}
    private static int getFlagByRequest(String JsonRes){
        int flag = 0;
        try {
            JSONObject resJson = new JSONObject(JsonRes);
            String bodyStr = resJson.getString("body");
            JSONObject bodyJson = new JSONObject(bodyStr);
            int errorCode = bodyJson.getInt("errorCode");
            if(errorCode == 0){
                String strSwitch = bodyJson.getString("advertisingSwitch");
                JSONObject swtichJson = new JSONObject(strSwitch);
                String timePer = swtichJson.getString("timePeriod");
                int devIndex = timePer.indexOf("-");
                if(devIndex > 0){
                    String houS = timePer.substring(0, devIndex);
                    String houE = timePer.substring(devIndex+1, timePer.length());
                    int startHour = Integer.valueOf(houS);
                    int endhour = Integer.valueOf(houE);
                    int nowHour = new Date().getHours();
                    if(startHour <= nowHour && nowHour < endhour){
                        flag = 1;
                    }else{
                        flag = 0;
                    }
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    private static String getUpdateQueryRequestContent(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null) { imsi = "noSimCard";   }
        String imei = tm.getDeviceId();
        if (imei == null) { imei = "123456789012345";   }
        
        JSONObject contentJO = new JSONObject();
        JSONObject headerObject = new JSONObject();
        JSONObject jsonTerminalInfo = new JSONObject();
        try {
            //head
            UUID uuid = UUID.randomUUID();
            headerObject.put("ver", 1);
            headerObject.put("type", 1);
            headerObject.put("msb", uuid.getMostSignificantBits());
            headerObject.put("lsb", uuid.getLeastSignificantBits());
            headerObject.put("mcd", 110003);
            String headerStr = headerObject.toString();
            contentJO.put("head", headerStr);
            // body
            jsonTerminalInfo.put("imei", imei);
            jsonTerminalInfo.put("imsi", imsi);
            jsonTerminalInfo.put("product", "mhzx");
            jsonTerminalInfo.put("channel", "droi");
            jsonTerminalInfo.put("project", "droi001");
            String bodyStr = jsonTerminalInfo.toString();
            contentJO.put("body", bodyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return contentJO.toString();
    }

    private static String queryUpdateDoPost(String urlString, String contents)  throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;
        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        HttpURLConnection connection = null;

        try {
            byte[] encrypted = encrypt(contents.getBytes("utf-8"),ENCODE_DECODE_KEY.getBytes());

            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("contentType", "utf-8");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", ""+ encrypted.length);
            out = new DataOutputStream(connection.getOutputStream());
            out.write(encrypted);
            out.flush();
            out.close();
            bis = new BufferedInputStream(connection.getInputStream());
            baf = new ByteArrayBuffer(1024);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            if (baf.length() > 0) {
                byte[] decrypted;
                decrypted = decrypt(baf.toByteArray(),ENCODE_DECODE_KEY.getBytes());
                line = new String(decrypted);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.disconnect();
            if (bis != null)
                bis.close();
            if (baf != null)
                baf.clear();
        }
        return line.trim();
    }
    
    
    
    public static byte[] encrypt(byte[] src, byte[] key) throws Exception {
        src = padding(src, (byte) 0);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        DESKeySpec dks = new DESKeySpec(key);
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(1, securekey);
        return cipher.doFinal(src);
    }

    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(2, securekey);
        return cipher.doFinal(src);
    }
    
    private static byte[] padding(byte[] sourceBytes, byte b) {
        int paddingSize = 8 - (sourceBytes.length % 8);
        byte[] paddingBytes = new byte[paddingSize];
        for (int i = 0; i < paddingBytes.length; ++i) {
            paddingBytes[i] = b;
        }
        sourceBytes = addAll(sourceBytes, paddingBytes);
        return sourceBytes;
    }
    
    public static byte[] addAll(byte[] array1, byte[] array2){
        if (array1 == null)
           return clone(array2);
         if (array2 == null) {
           return clone(array1);
        }
        byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
         System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
         return joinedArray;
    }
    
    public static byte[] clone(byte[] array) {
        if (array == null) {
            return null;
        }
        return ((byte[]) (byte[]) array.clone());
    }
}
