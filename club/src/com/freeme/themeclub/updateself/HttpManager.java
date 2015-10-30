package com.freeme.themeclub.updateself;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.freeme.themeclub.R;
import com.freeme.themeclub.updateself.UpdateManager.DownloadRes;
import com.freeme.themeclub.updateself.UpdateManager.QueryRes;
import com.freeme.themeclub.updateself.Util.SdcardState;

public class HttpManager {
    public static final String TAG = "HttpManager";

    private UpdateSelfService mUpdateService;

    private static final int SURPLUS_SPACE_BYTES = 20 * 1024 * 1024; // 20M
                                                                     // surplus
                                                                     // space

    private static String ENCODE_DECODE_KEY = "x_s0_s22";

    HttpManager(UpdateSelfService service) {
        mUpdateService = service;
    }

    private int doGetFile(DownloadInfo downInfo) {
        long currSize = downInfo.getCurrDownloadSize();
        // currSize -= 1024;
        if (currSize < 0) {
            currSize = 0;
        }

        String path = Util.getDownloadPath();
        // String fileName = eventInfo.getAppName();

        // String filePath = path + "/" + fileName + ".apk.tmp";
        String filePath = downInfo.getDownloadFilePath();
        Util.logI(TAG, "doGetFile()", "url=" + downInfo.url + ", currSize=" + currSize + ", filePath=" + filePath);
        HttpURLConnection connection = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;

        // test
        // urlStr =
        // "http://apk.oo523.com/appstores/DownloadSingleAppImage?td=1001&lcd=480x800&os=android&nt=wifi&ver=65&cid=0&lac=0&mcc=0&mnc=0&mpm=i50&pf=1001&ram=4&rom=4&mod=i50.KB.V2.09&Batch=mt6575&no=i0000217&androidVersion=&lbyver=2.3.6&imsi=null&imei=864471010000089&imageId=C13435.png";

        try {
            URL url = new URL(downInfo.url);
            long fileSize = downInfo.totalSize;
            if (fileSize == 0) {
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                fileSize = connection.getContentLength();
                connection.disconnect();
                if (fileSize <= 1024) {
                    return DownloadRes.HTTP_ERROR;
                }

                downInfo.setTotalSize(fileSize);
                Util.saveDownloadInfo(mUpdateService, downInfo);
                Util.logV(TAG, "doGetFile()", "download file size = " + fileSize);
            }

            int checkResult = Util.checkSdcardIsAvailable(mUpdateService, fileSize + SURPLUS_SPACE_BYTES);
            if (checkResult == SdcardState.STATE_INSUFFICIENT) {
                Util.logV(TAG, "doGetFile()", "no enough space, return");
                return DownloadRes.NO_ENOUGH_SPACE;

            }
            if (checkResult == SdcardState.STATE_LOSE) {
                Util.logV(TAG, "doGetFile()", "sd card lost, return");
                return DownloadRes.SDCARD_LOST;
            }

            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setRequestProperty("Range", "bytes=" + currSize + "-" + fileSize);
            connection.setRequestMethod("GET");

            File ifolder = new File(path);
            if (!ifolder.exists()) {
                boolean cr = ifolder.mkdirs();
                Util.logV(TAG, "doGetFile()", "create download folder result = " + cr);
            }

            try {
                randomAccessFile = new RandomAccessFile(filePath, "rwd");
                randomAccessFile.seek(currSize);
            } catch (Exception e) {
                e.printStackTrace();
                connection.disconnect();
                randomAccessFile.close();
                return DownloadRes.SDCARD_LOST;
            }
            connection.setReadTimeout(26000);
            is = connection.getInputStream();

            byte[] buff = new byte[1024];
            int rc = 0;
            while ((rc = is.read(buff)) != -1) {
                try {
                    randomAccessFile.write(buff, 0, rc);
                    currSize += rc;

                } catch (Exception e) {
                    e.printStackTrace();
                    connection.disconnect();
                    randomAccessFile.close();
                    is.close();
                    return DownloadRes.SDCARD_LOST;
                }
            }
            Util.logV(TAG, "doGetFile()", "finish, currSize = " + currSize + "bytes.");
            connection.disconnect();
            randomAccessFile.close();
            is.close();
            if (currSize == fileSize) {
                return DownloadRes.DOWNLOAD_COMPLETE;
            } else {
                return DownloadRes.HTTP_ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }

                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Util.logV(TAG, "doGetFile()", "exception exit");
        return DownloadRes.HTTP_ERROR;
    }

    public int downloadUpdate(DownloadInfo downInfo) {
        return doGetFile(downInfo);
    }

    public int queryUpdate() {
        String content = getUpdateQueryRequestContent();
        String url = Custom.getUpdateQueryUrl();

        try {
            String responce = queryUpdateDoPost(url, content);
            NewUpdateInfo queryRes = parserUpdateQueryData(responce);

            if (queryRes == null) {
                return QueryRes.NO_NEW;
            }

            return QueryRes.FOUND_NEW;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return QueryRes.HTTP_ERROR;
    }

    private String getUpdateQueryRequestContent() {
        JSONObject contentJO = new JSONObject();

        // header
        String headerStr = null;
        JSONObject headerObject = new JSONObject();
        UUID uuid = UUID.randomUUID();
        try {
            headerObject.put("ver", 1);
            headerObject.put("type", 1);
            headerObject.put("msb", uuid.getMostSignificantBits());
            headerObject.put("lsb", uuid.getLeastSignificantBits());
            headerObject.put("mcd", 103001);
            headerStr = headerObject.toString();
            contentJO.put("head", headerStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (headerStr == null) {
            return null;
        }

        // body
        String bodyStr = null;
        JSONObject jsonTerminalInfo = new JSONObject();
        JSONObject jsonObjBody = new JSONObject();

        DisplayMetrics outMetrics = mUpdateService.getResources().getDisplayMetrics();

        // RAM
        String[] meminfoLabels = { "MemTotal:" };
        long[] meminfoValues = new long[1];
        meminfoValues[0] = -1;
        Class<?> proc = null;
        try {
            proc = Class.forName("android.os.Process");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method method = proc.getMethod("readProcLines", String.class, String[].class, long[].class);
            method.invoke(proc.newInstance(), "/proc/meminfo", meminfoLabels, meminfoValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long RAM = 0;
        if (meminfoValues[0] != -1) {
            // RAM = Long.toString(meminfoValues[0] / 1024) + "M";
            RAM = meminfoValues[0];
        }

        // package name, version code , version name
        String pkgName = mUpdateService.getPackageName();
        PackageInfo pkgInfo = Util.getPackageInfo(mUpdateService, pkgName);

        // imsi imei
        TelephonyManager tm = (TelephonyManager) mUpdateService.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null) {
            imsi = "123456789012345";
        }
        String imei = tm.getDeviceId();
        if (imei == null) {
            imei = "123456789012345";
        }

        try {
            jsonTerminalInfo.put("hman", android.os.Build.MANUFACTURER);
            jsonTerminalInfo.put("htype", android.os.Build.MODEL);
            jsonTerminalInfo.put("sWidth", outMetrics.widthPixels);
            jsonTerminalInfo.put("sHeight", outMetrics.heightPixels);
            jsonTerminalInfo.put("ramSize", RAM);
            jsonTerminalInfo.put("netType", Util.getNetworkType(mUpdateService));
            jsonTerminalInfo.put("chId", getRawData(mUpdateService, R.raw.cp));
            jsonTerminalInfo.put("osVer", android.os.Build.VERSION.RELEASE);
            // jsonTerminalInfo.put("appId", Custom.APP_ID);
            jsonTerminalInfo.put("appId", Custom.APP_ID);
            jsonTerminalInfo.put("apkVer", pkgInfo.versionCode);
            jsonTerminalInfo.put("pName", pkgName);
            jsonTerminalInfo.put("apkVerName", pkgInfo.versionName);
            jsonTerminalInfo.put("imsi", imsi);
            jsonTerminalInfo.put("imei", imei);
            jsonTerminalInfo.put("cpu", Build.HARDWARE);
            jsonObjBody.put("tInfo", jsonTerminalInfo);
            bodyStr = jsonObjBody.toString();
            contentJO.put("body", bodyStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (bodyStr == null) {
            return null;
        }

        return contentJO.toString();
    }
    
    public String getRawData(Context context, int id) {
        String td;
        InputStream is = context.getResources().openRawResource(id);
        DataInputStream dis = new DataInputStream(is);
        byte[] buffer = null;
        try {
            buffer = new byte[is.available()];
            dis.readFully(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                dis.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        td = new String(buffer).trim();
        return td;
    }

    public static class NewUpdateInfo {
        public static final int TYPE_FORCE_UPDATE = 1;
        public static final int TYPE_DIALOG_NEW = 2;
        public static final int TYPE_NO_NEW = 3;
        public static final int TYPE_UPDATE_BG = 4;

        public String dTitle;
        public String dContent;
        public int policy;
        public String md5;
        public int verCode;
        public String fileUrl;
    }

    private NewUpdateInfo parserUpdateQueryData(String result) {
        JSONObject jsonObject, bodyJSONObject;
        String headResult = "";
        String bodyResult = "";
        NewUpdateInfo response = null;
        if (TextUtils.isEmpty(result))
            return null;

        try {
            jsonObject = new JSONObject(result);
            headResult = jsonObject.getString("head");
            bodyResult = jsonObject.getString("body");
            bodyJSONObject = new JSONObject(bodyResult);
            if (bodyJSONObject != null && bodyJSONObject.has("errorCode") && bodyJSONObject.getInt("errorCode") == 0) {
                if (bodyJSONObject.getInt("policy") == 3)
                    return null;

                response = new NewUpdateInfo();
                response.dTitle = bodyJSONObject.getString("title");
                response.dContent = bodyJSONObject.getString("content");
                response.policy = bodyJSONObject.getInt("policy");
                // bodyJSONObject.getString("pName");
                response.verCode = bodyJSONObject.getInt("ver");
                response.fileUrl = bodyJSONObject.getString("fileUrl");
                response.md5 = bodyJSONObject.getString("md5");
                // bodyJSONObject.getInt("errorCode");

                String pkgName = mUpdateService.getPackageName();
                PackageInfo pkgInfo = Util.getPackageInfo(mUpdateService, pkgName);
                if (pkgInfo.versionCode > response.verCode) {
                    return null;
                }
                Util.saveNewInfo(mUpdateService, response);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return response;
    }

    public static String queryUpdateDoPost(String urlString, String contents) throws IOException {
        String line = "";
        DataOutputStream out = null;
        URL postUrl;

        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        boolean isPress = false;
        HttpURLConnection connection = null;

        try {
            byte[] encrypted = CryptUtil.encrypt(contents.getBytes("utf-8"), ENCODE_DECODE_KEY.getBytes());

            postUrl = new URL(urlString);
            connection = (HttpURLConnection) postUrl.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(20000);
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
                    Util.logI(TAG, "queryUpdateDoPost", "compress length:" + baf.length());
                    unCompressByte = ZipUtil.uncompress(baf.toByteArray());
                    Util.logI(TAG, "queryUpdateDoPost", "length:" + unCompressByte.length);
                    decrypted = CryptUtil.decrypt(unCompressByte, ENCODE_DECODE_KEY.getBytes());
                } else
                    decrypted = CryptUtil.decrypt(baf.toByteArray(), ENCODE_DECODE_KEY.getBytes());

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

    private String doGet(String url) {
        Util.logI(TAG, "doGet()", "url=" + url);
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
