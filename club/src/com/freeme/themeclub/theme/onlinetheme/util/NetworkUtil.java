package com.freeme.themeclub.theme.onlinetheme.util;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

public class NetworkUtil {

    public static String ENCODE_DECODE_KEY = "x_s0_s22";

    public static String accessNetworkByPost(String urlString, String contents)
            throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        boolean isPress = false;
        HttpURLConnection connection = null;

        try {
            byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"),
                    ENCODE_DECODE_KEY.getBytes());

            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("contentType", "utf-8");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", ""
                    + encrypted.length);
            out = new DataOutputStream(connection.getOutputStream());
            out.write(encrypted);
            out.flush();
            out.close();

            bis = new BufferedInputStream(connection.getInputStream());
            baf = new ByteArrayBuffer(1024);

            isPress = Boolean.valueOf(connection.getHeaderField("isPress"));

            int current = 0;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            if (baf.length() > 0) {
                byte unCompressByte[];
                byte[] decrypted;
                if (isPress) {
                    decrypted = DESUtil.decrypt(baf.toByteArray(),
                            ENCODE_DECODE_KEY.getBytes());
                    unCompressByte = ZipUtil.uncompress(decrypted);
                    line = new String(unCompressByte);
                } else {
                    decrypted = DESUtil.decrypt(baf.toByteArray(),
                            ENCODE_DECODE_KEY.getBytes());
                    line = new String(decrypted);
                }

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

    public static String buildHeadData(int msgCode) {
        String result = "";

        UUID deviceUUID = readUUID();
        Header header = new Header();
        header.setBasicVer((byte) 1);
        header.setLength(84);
        header.setType((byte) 1);
        header.setReserved((short) 0);
        header.setFirstTransaction(deviceUUID.getMostSignificantBits());
        header.setSecondTransaction(deviceUUID.getLeastSignificantBits());
        header.setMessageCode(msgCode);
        result = header.toString();

        return result;
    }
    
    private static UUID readUUID(){ 
        Object result = null,result1 = null,result2 = null; 
        try { 
        Class<?> classType = Class.forName("android.os.ServiceManager"); 
        Object invokeOperation = classType.newInstance(); 
        Method getMethod = classType.getMethod("getService", new Class[] {String.class}); 
        result = getMethod.invoke(invokeOperation, new Object[] {new String("TydNativeMisc")}); 
        
        Class<?> classType1 = Class.forName("com.freeme.internal.server.INativeMiscService$Stub"); 
        Method getMethod1 = classType1.getMethod("asInterface", new Class[] {IBinder.class}); 
        result1 = getMethod1.invoke(classType1, new Object[] {result}); 
        Class<?> classType2 = result1.getClass(); 
        Method getMethod2 = classType2.getMethod("getDeviceUUID", new Class[] {}); 
        result2 = getMethod2.invoke(result1, new Object[] {}); 

        } catch (Exception e) { 
             e.printStackTrace(); 
        } 
        return UUID.fromString(result2.toString()); 
    }

    public static int getAPNType(Context context) {
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String netString = networkInfo.getExtraInfo().toLowerCase();
            if (netString.equals("cmnet") || netString.equals("uninet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }
    
}
