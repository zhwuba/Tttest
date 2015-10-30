package com.market.account.weibosdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboHttpException;
import com.sina.weibo.sdk.net.CustomRedirectHandler;
import com.sina.weibo.sdk.net.NetStateManager;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.NetworkHelper;
import com.sina.weibo.sdk.utils.Utility;

class HttpManager {
    private static final String TAG = "HttpManager";
    private static final String BOUNDARY = getBoundry();
    private static final String MP_BOUNDARY = "--" + BOUNDARY;
    private static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_GET = "GET";
    private static final int CONNECTION_TIMEOUT = 25000;
    private static final int SOCKET_TIMEOUT = 20000;
    private static final int BUFFER_SIZE = 8192;
    private static SSLSocketFactory sSSLSocketFactory;

    static {
        System.loadLibrary("weibosdkcore");
    }


    public static String openUrl(Context context, String url, String method, WeiboParameters params)
        throws WeiboException {
        HttpResponse response = requestHttpExecute(context, url, method, params);
        String ans = readRsponse(response);
        LogUtil.d("HttpManager", "Response : " + ans);
        return ans;
    }


    private static HttpResponse requestHttpExecute(Context context, String url, String method, WeiboParameters params) {
        HttpResponse response = null;
        try {
            HttpClient client = SinaHttpClient.getNewHttpClient();
            client.getParams().setParameter("http.route.default-proxy", NetStateManager.getAPN());

            HttpUriRequest request = null;
            ByteArrayOutputStream baos = null;

            if (method.equals("GET")) {
                url = url + "?" + params.encodeUrl();
                request = new HttpGet(url);
            } else if (method.equals("POST")) {
                HttpPost post = new HttpPost(url);
                request = post;

                baos = new ByteArrayOutputStream();
                if (params.hasBinaryData()) {
                    post.setHeader("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    buildParams(baos, params);
                } else {
                    Object value = params.get("content-type");
                    if ((value != null) && ((value instanceof String))) {
                        params.remove("content-type");
                        post.setHeader("Content-Type", (String) value);
                    } else {
                        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    }

                    String postParam = params.encodeUrl();
                    baos.write(postParam.getBytes("UTF-8"));
                }
                post.setEntity(new ByteArrayEntity(baos.toByteArray()));
                baos.close();
            } else if (method.equals("DELETE")) {
                request = new HttpDelete(url);
            }

            response = client.execute(request);
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();

            if (statusCode != 200) {
                String result = readRsponse(response);
                throw new WeiboHttpException(result, statusCode);
            }
        } catch (IOException e) {
            throw new WeiboException(e);
        }

        return response;
    }


    private static void setHttpCommonParam(Context context, WeiboParameters params) {
        String aid = "";
        if (!TextUtils.isEmpty(params.getAppKey())) {
            aid = Utility.getAid(context, params.getAppKey());
            if (!TextUtils.isEmpty(aid)) {
                params.put("aid", aid);
            }

        }

        String timestamp = getTimestamp();
        params.put("oauth_timestamp", timestamp);

        String token = "";
        Object accessToken = params.get("access_token");
        Object refreshToken = params.get("refresh_token");
        if ((accessToken != null) && ((accessToken instanceof String))) {
            token = (String) accessToken;
        } else if ((refreshToken != null) && ((refreshToken instanceof String))) {
            token = (String) refreshToken;
        }
        String oauthSign = getOauthSign(context, aid, token, params.getAppKey(), timestamp);
        params.put("oauth_sign", oauthSign);
    }


    private static void shutdownHttpClient(HttpClient client) {
        if (client != null)
            try {
                client.getConnectionManager().closeExpiredConnections();
            } catch (Exception localException) {
            }
    }


    public static String openUrl4RdirectURL(Context context, String url, String method, WeiboParameters params)
        throws WeiboException {
        DefaultHttpClient client = null;
        String result = "";
        try {
            client = (DefaultHttpClient) SinaHttpClient.getNewHttpClient();
            client.setRedirectHandler(new RedirectHandler() {
                public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                    LogUtil.d("HttpManager", "openUrl4RdirectURL isRedirectRequested method");
                    return false;
                }


                public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
                    LogUtil.d("HttpManager", "openUrl4RdirectURL getLocationURI method");
                    return null;
                }
            });
            setHttpCommonParam(context, params);

            HttpUriRequest request = null;
            client.getParams().setParameter("http.route.default-proxy", NetStateManager.getAPN());
            if (method.equals("GET")) {
                url = url + "?" + params.encodeUrl();
                LogUtil.d("HttpManager", "openUrl4RdirectURL GET url : " + url);
                HttpGet get = new HttpGet(url);
                request = get;
            } else if (method.equals("POST")) {
                HttpPost post = new HttpPost(url);
                LogUtil.d("HttpManager", "openUrl4RdirectURL POST url : " + url);
                request = post;
            }
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if ((statusCode == 301) || (statusCode == 302)) {
                String redirectURL = response.getFirstHeader("Location").getValue();
                LogUtil.d("HttpManager", "RedirectURL = " + redirectURL);
                return redirectURL;
            }
            if (statusCode == 200) {
                return readRsponse(response);
            }
            result = readRsponse(response);
            throw new WeiboHttpException(result, statusCode);
        } catch (IOException e) {
            throw new WeiboException(e);
        } finally {
            shutdownHttpClient(client);
        }
    }


    public static String openRedirectUrl4LocationUri(Context context, String url, String method, WeiboParameters params) {
        DefaultHttpClient client = null;
        CustomRedirectHandler redirectHandler = null;
        try {
            redirectHandler = new CustomRedirectHandler() {
                public boolean shouldRedirectUrl(String url) {
                    return true;
                }


                public void onReceivedException() {
                }
            };
            client = (DefaultHttpClient) SinaHttpClient.getNewHttpClient();
            client.setRedirectHandler(redirectHandler);

            setHttpCommonParam(context, params);

            HttpUriRequest request = null;
            client.getParams().setParameter("http.route.default-proxy", NetStateManager.getAPN());
            if (method.equals("GET")) {
                url = url + "?" + params.encodeUrl();
                HttpGet get = new HttpGet(url);
                request = get;
            } else if (method.equals("POST")) {
                HttpPost post = new HttpPost(url);
                request = post;
            }
            request.setHeader("User-Agent", NetworkHelper.generateUA(context));
            client.execute(request);
            return redirectHandler.getRedirectUrl();
        } catch (IOException e) {
            throw new WeiboException(e);
        } finally {
            shutdownHttpClient(client);
        }
    }


    public static synchronized String downloadFile(Context context, String url, String saveDir, String fileName)
        throws WeiboException {
        File savePathDir = new File(saveDir);
        if (!savePathDir.exists()) {
            savePathDir.mkdirs();
        }
        File filePath = new File(savePathDir, fileName);
        if (filePath.exists()) {
            return filePath.getPath();
        }

        if (!URLUtil.isValidUrl(url)) {
            return "";
        }

        HttpClient client = SinaHttpClient.getNewHttpClient();

        long tempFileLength = 0L;
        File tempFile = new File(saveDir, fileName + "_temp");
        try {
            if (tempFile.exists())
                tempFileLength = tempFile.length();
            else {
                tempFile.createNewFile();
            }
            HttpGet request = new HttpGet(url);
            request.setHeader("RANGE", "bytes=" + tempFileLength + "-");
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            long totalLength = 0L;
            long startPosition = 0L;
            if (statusCode == 206) {
                startPosition = tempFileLength;
                Header[] rangeHeaders = response.getHeaders("Content-Range");
                if ((rangeHeaders != null) && (rangeHeaders.length != 0)) {
                    String rangValue = rangeHeaders[0].getValue();
                    totalLength = Long.parseLong(rangValue.substring(rangValue.indexOf('/') + 1));
                }
            } else if (statusCode == 200) {
                startPosition = 0L;
                Header lengthHeader = response.getFirstHeader("Content-Length");
                if (lengthHeader != null)
                    totalLength = Integer.valueOf(lengthHeader.getValue()).intValue();
            } else {
                String result = readRsponse(response);
                throw new WeiboHttpException(result, statusCode);
            }

            InputStream inputStream = null;
            HttpEntity entity = response.getEntity();
            Header header = response.getFirstHeader("Content-Encoding");
            if ((header != null) && (header.getValue().toLowerCase().indexOf("gzip") > -1))
                inputStream = new GZIPInputStream(entity.getContent());
            else {
                inputStream = entity.getContent();
            }
            RandomAccessFile content = new RandomAccessFile(tempFile, "rw");
            content.seek(startPosition);

            byte[] sBuffer = new byte[1024];
            int readBytes = 0;

            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            content.close();
            inputStream.close();

            if ((totalLength == 0L) || (tempFile.length() < totalLength)) {
                tempFile.delete();
            } else {
                tempFile.renameTo(filePath);
                return filePath.getPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
            tempFile.delete();
        } finally {
            if (client != null) {
                client.getConnectionManager().closeExpiredConnections();
                client.getConnectionManager().closeIdleConnections(300L, TimeUnit.SECONDS);
            }
        }
        if (client != null) {
            client.getConnectionManager().closeExpiredConnections();
            client.getConnectionManager().closeIdleConnections(300L, TimeUnit.SECONDS);
        }

        return "";
    }


    private static void buildParams(OutputStream baos, WeiboParameters params) throws WeiboException {
        try {
            Set keys = params.keySet();

            for (Object key : keys) {
                Object value = params.get(key.toString());
                if ((value instanceof String)) {
                    StringBuilder sb = new StringBuilder(100);
                    sb.setLength(0);
                    sb.append(MP_BOUNDARY).append("\r\n");
                    sb.append("content-disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
                    sb.append(params.get(key.toString())).append("\r\n");

                    baos.write(sb.toString().getBytes());
                }

            }

            for (Object key : keys) {
                Object value = params.get(key.toString());
                if ((value instanceof Bitmap)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(MP_BOUNDARY).append("\r\n");
                    sb.append("content-disposition: form-data; name=\"").append(key)
                        .append("\"; filename=\"file\"\r\n");
                    sb.append("Content-Type: application/octet-stream; charset=utf-8\r\n\r\n");
                    baos.write(sb.toString().getBytes());

                    Bitmap bmp = (Bitmap) value;
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] bytes = stream.toByteArray();

                    baos.write(bytes);
                    baos.write("\r\n".getBytes());
                } else if ((value instanceof ByteArrayOutputStream)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(MP_BOUNDARY).append("\r\n");
                    sb.append("content-disposition: form-data; name=\"").append(key)
                        .append("\"; filename=\"file\"\r\n");
                    sb.append("Content-Type: application/octet-stream; charset=utf-8\r\n\r\n");
                    baos.write(sb.toString().getBytes());

                    ByteArrayOutputStream stream = (ByteArrayOutputStream) value;
                    baos.write(stream.toByteArray());
                    baos.write("\r\n".getBytes());
                    stream.close();
                }
            }
            baos.write(("\r\n" + END_MP_BOUNDARY).getBytes());
        } catch (IOException e) {
            throw new WeiboException(e);
        }
    }


    private static String readRsponse(HttpResponse response) throws WeiboException {
        if (response == null) {
            return null;
        }

        HttpEntity entity = response.getEntity();
        InputStream inputStream = null;
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try {
            inputStream = entity.getContent();
            Header header = response.getFirstHeader("Content-Encoding");
            if ((header != null) && (header.getValue().toLowerCase().indexOf("gzip") > -1)) {
                inputStream = new GZIPInputStream(inputStream);
            }

            int readBytes = 0;
            byte[] buffer = new byte[8192];
            while ((readBytes = inputStream.read(buffer)) != -1) {
                content.write(buffer, 0, readBytes);
            }
            String result = new String(content.toByteArray(), "UTF-8");
            LogUtil.d("HttpManager", "readRsponse result : " + result);
            return result;
        } catch (IOException e) {
            throw new WeiboException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (content != null)
                try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }


    private static String getBoundry() {
        StringBuffer sb = new StringBuffer();
        for (int t = 1; t < 12; t++) {
            long time = System.currentTimeMillis() + t;
            if (time % 3L == 0L)
                sb.append((char) (int) time % '\t');
            else if (time % 3L == 1L)
                sb.append((char) (int) (65L + time % 26L));
            else {
                sb.append((char) (int) (97L + time % 26L));
            }
        }
        return sb.toString();
    }


    private static SSLSocketFactory getSSLSocketFactory() {
        if (sSSLSocketFactory == null) {
            try {
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);

                Certificate cnCertificate = getCertificate("cacert_cn.cer");
                Certificate comCertificate = getCertificate("cacert_com.cer");
                keyStore.setCertificateEntry("cnca", cnCertificate);
                keyStore.setCertificateEntry("comca", comCertificate);

                sSSLSocketFactory = new SSLSocketFactory(keyStore);
                LogUtil.d("HttpManager", "getSSLSocketFactory noraml !!!!!");
            } catch (Exception e) {
                e.printStackTrace();

                sSSLSocketFactory = SSLSocketFactory.getSocketFactory();
                LogUtil.d("HttpManager", "getSSLSocketFactory error default !!!!!");
            }
        }
        return sSSLSocketFactory;
    }


    private static Certificate getCertificate(String name) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream certInput = HttpManager.class.getResourceAsStream(name);
        Certificate certificate;
        try {
            certificate = cf.generateCertificate(certInput);
        } finally {
            if (certInput != null) {
                certInput.close();
            }
        }
        return certificate;
    }


    private static String getTimestamp() {
        long timestamp = System.currentTimeMillis() / 1000L;
        return String.valueOf(timestamp);
    }


    private static String getOauthSign(Context context, String aid, String accessToken, String appKey, String timestamp) {
        StringBuilder part1 = new StringBuilder("");
        if (!TextUtils.isEmpty(aid)) {
            part1.append(aid);
        }
        if (!TextUtils.isEmpty(accessToken)) {
            part1.append(accessToken);
        }
        if (!TextUtils.isEmpty(appKey)) {
            part1.append(appKey);
        }

        return calcOauthSignNative(context, part1.toString(), timestamp);
    }


    private static native String calcOauthSignNative(Context paramContext, String paramString1, String paramString2);
}