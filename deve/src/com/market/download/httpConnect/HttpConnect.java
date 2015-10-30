package com.market.download.httpConnect;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import com.market.download.util.Util;
import com.market.net.utils.DESUtil;
import com.market.updateSelf.ZipUtil;
import com.zhuoyi.market.constant.Constant;

public class HttpConnect {
    private static final String TAG = "HttpConnect";
    
    public static String doPost(String urlString, String contents)
            throws IOException {
        Util.log(TAG, "doPost", "url:" + urlString + ", content:" + contents);
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        boolean isPress = false;
        HttpURLConnection connection = null;
        // String url =
        // "http://101.95.97.178:9093";//"http://joyreachapp.cn:2578";//

        try {
            byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"), Constant.ENCODE_DECODE_KEY.getBytes());
            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(35000);
            connection.setRequestMethod("POST");
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("contentType", "utf-8");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", "" + encrypted.length);

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
                    Util.log(TAG, "doPost", "compress length:" + baf.length());
                    decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
                    unCompressByte = ZipUtil.uncompress(decrypted);
                    Util.log(TAG, "doPost", "length:" + unCompressByte.length);
                    line = new String(unCompressByte);
                } else {
                    decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
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
    
    
    public static String doGet(String url) {
        Util.log(TAG, "doGet()", "url=" + url);
        String result = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        URLConnection ucon = null;
        try {
            URL myURL = new URL(url);
            ucon = myURL.openConnection();
            ucon.setConnectTimeout(10000);
            ucon.setReadTimeout(15000);
            is = ucon.getInputStream();
            bis = new BufferedInputStream(is);
            baf = new ByteArrayBuffer(1024);
            int current = 0;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            result = EncodingUtils.getString(baf.toByteArray(), "UTF-8");

            baf.clear();
            bis.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (baf != null) {
                baf.clear();
            }
            try {
                if (bis != null) {
                    bis.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
