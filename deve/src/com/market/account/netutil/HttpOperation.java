package com.market.account.netutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.market.account.weibosdk.SinaHttpClient;

/**
 * @author sunlei
 */
public class HttpOperation {

    /** httpClient . */
    private static final String JSON_KEY_NAME = "microohclient_requestkey";
    private static final String AUTH_TOKEN_KEY = "microohclient_authtoken_key";
    private static final String AUTH_TOKEN_VALUE = "21232f297a57a5a743894a0e";


    /**
     * @param url .
     * @return String
     * @throws Exception .
     */
    public static String getRequest(String url) throws Exception {
    	HttpClient httpClient = SinaHttpClient.getNewHttpClient();
    	
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(get);
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	httpClient.getConnectionManager().shutdown();
        }

        return null;
    }


    /**
     * @param url .
     * @param rawParams .
     * @return String
     * @throws Exception
     */
    public static String postRequest(String url, Map<String, String> rawParams) {
        // httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
        // 60000);

        try {
            HttpPost post = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (String key : rawParams.keySet()) {
                params.add(new BasicNameValuePair(key, rawParams.get(key)));
            }

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(urlEncodedFormEntity);
            HttpClient httpClient = new DefaultHttpClient();
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15 * 1000);
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15 * 1000);
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


    /**
     * @param url .
     * @param requestJSONStr .
     * @return String.
     */
    public static String postRequestJSONFormat(String url, String requestJSONStr) {
        HashMap<String, String> mapParams = new HashMap<String, String>();
        mapParams.put(JSON_KEY_NAME, requestJSONStr);
        mapParams.put(AUTH_TOKEN_KEY, AUTH_TOKEN_VALUE);

        return postRequest(url, mapParams);
    }
    
}
