package com.zhuoyi.market.utils.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.zhuoyi.market.appManage.AppManageUtil;
import com.market.download.userDownload.DownloadManager;
import com.market.net.response.Get3rdPartyDownloadResp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class DownloadFor3rdParty {

    private Context mContext = null;
    private static final String URL = "http://download-sogou.tt286.com/sougouSpeed/client.php";

    private static final int HANDLER_GET_DATA = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            HashMap<String, Object> map = null;
            if (msg.what == HANDLER_GET_DATA) {
                Get3rdPartyDownloadResp data = null;
                map = (HashMap<String, Object>) msg.obj;

                if (map != null && map.size() > 0) {
                    data = (Get3rdPartyDownloadResp) map.get("appData");
                }
                
                if (data != null) {
                    startDownloadApk(data.getDownloadUrl(), data.getIconUrl(),
                            data.getPkgName(), data.getAppName(),
                            data.getVerCode() , data.getSize());
                }
            }
        }
    };


    public DownloadFor3rdParty(Context context) {
        mContext = context;
    }


    public void getDataFromServer(final String key) {
        if (key == null)
            return;
        new Thread() {
            @Override
            public void run() {
                Get3rdPartyDownloadResp resp = null;
                try {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("keyId", key);

                    String result = postRequest(URL, map);
                    
                    if (!TextUtils.isEmpty(result)) {
                        Gson gson = new Gson();
                        resp = gson.fromJson(result, Get3rdPartyDownloadResp.class);
                    } else {
                        resp = null;
                    }
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                HashMap<String, Object> appData = new HashMap<String, Object>();
                appData.put("appData", resp);
                Message msg = new Message();
                msg.what = HANDLER_GET_DATA;
                msg.obj = appData;
                if (mHandler != null)
                    mHandler.sendMessage(msg);

            }
        }.start();
    }


    private String postRequest(String url, Map<String, String> rawParams) {
        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (String key : rawParams.keySet()) {
                params.add(new BasicNameValuePair(key, rawParams.get(key)));
            }

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(urlEncodedFormEntity);
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10 * 1000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
            HttpResponse httpResponse = httpClient.execute(post);

            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                return result;
            }
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

        }
        return null;
    }


    public void startDownloadApk(String url, String iconUrl, String pkgName,
            String appName, int verCode , long fileSize) {
        
        //暂时不做已安装或已下载处理
        //if (checkAppInstalled(mContext, pkgName, verCode)) return;
        
        DownloadManager.startServiceDownloadSougouApk(mContext, url, pkgName, appName, verCode, fileSize);

        AppManageUtil.startDownloadActivity(mContext, iconUrl, pkgName);
    }
    
    
    public boolean checkAppInstalled(Context context, String pName, int versionCode){

        if(context == null || pName == null){
            return false;
        }
        try {
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(pName, 0);
            if(pinfo == null)return false;
            if(versionCode > pinfo.versionCode){
                return false;
            }else{
                return true;
            }

        } catch (NameNotFoundException e) {
            return false;
        }
    }

}
