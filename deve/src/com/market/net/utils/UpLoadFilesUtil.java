package com.market.net.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.market.account.utils.GetPublicParams;
import com.market.download.util.NetworkType;
import com.market.net.data.UploadInfo;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;

@SuppressLint("ParserError")
public class UpLoadFilesUtil {

	public static final String TAG = "UpLoadFilesUtil";

//	public static final String BUG_REPORT_URL = "http://bugreport.tt286.com:7890/debug_report";
	public static final String BUG_REPORT_URL = "http://bugreport-apk.oo523.com:5800/index.php?m=ReportInterface&a=getReportData";

	public static String postReport(Context context,
			Map<String, String> params, File[] files) {
		if (files == null || files.length == 0) {
			return null;
		}
		String fileIndexs = "";
		String prefix = "--";
		String boundary = UUID.randomUUID().toString(); 
		String end = "\r\n";
		String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
		HttpURLConnection conn = null;
		for (int i = 0; i < files.length; i++) {
			try {
				URL url = new URL(BUG_REPORT_URL);
				conn = (HttpURLConnection) url.openConnection();

				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);
				conn.setConnectTimeout(10 * 1000);
				conn.setReadTimeout(10 * 1000);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Charset", HTTP.UTF_8);
				conn.setRequestProperty("Connection", "keep-alive");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("Accept", accept);

				StringBuilder textEntity = new StringBuilder();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					textEntity.append(prefix);
					textEntity.append(boundary);
					textEntity.append(end);
					textEntity.append("Content-Disposition: form-data; name=\""
							+ entry.getKey() + "\"\r\n\r\n");
					textEntity.append(entry.getValue());
					textEntity.append(end);
				}

				DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
				dos.write(textEntity.toString().getBytes());

				if (files[i] == null
						|| (files[i].length() > (1024 * 1024) && !NetworkType.isWifiAvailable(context))) {
					continue;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(prefix);
				sb.append(boundary);
				sb.append(end);

				sb.append("Content-Disposition: form-data; name=\"reportfile\"; filename=\""
						+ files[i].getName() + "\"" + end);
				sb.append("Content-Type: application/octet-stream; charset="
						+ HTTP.UTF_8 + end);
				sb.append(end);
				dos.write(sb.toString().getBytes());

				InputStream is = new FileInputStream(files[i]);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.writeBytes(end);

				byte[] end_data = (prefix + boundary + prefix + end).getBytes();
				dos.write(end_data); 
				int res = conn.getResponseCode();  
				if (res == HttpStatus.SC_OK) {
					InputStream input = conn.getInputStream();
					InputStreamReader inputReader = new InputStreamReader(input,
							HTTP.UTF_8);
					BufferedReader br = new BufferedReader(inputReader);
					String result = br.readLine();
					input.close();
					dos.close();
					JSONObject jsonResult;
					try {
						jsonResult = new JSONObject(result);
						if (jsonResult.getInt("result") == 0) {
							fileIndexs += i + ","; 
						}else{
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				conn.disconnect();
			}
		}
		return fileIndexs;
	}

	public static Map<String,String> getUploadparams(Context context, UploadInfo uploadInfo) {
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<String, String> publicParams =  GetPublicParams.getPublicParaForPush(context, context.getPackageName(), R.raw.td);
		String imsi = publicParams.get("imsi");
		String imei = publicParams.get("imei");
		String androidVersion = publicParams.get("androidVersion");
		map.put("packagename",uploadInfo.getPackageName());
		map.put("appname",uploadInfo.getAppName());
		map.put("versioncode",uploadInfo.getVersionCode() + "");
		map.put("versionname",uploadInfo.getVersionName());
		map.put("model",uploadInfo.getMobileType());
		map.put("brand",uploadInfo.getManufacturer());
		map.put("lcd",uploadInfo.getResolution());
		map.put("imei",imei);
		map.put("lmsi",imsi);
		map.put("android",androidVersion);
		map.put("ram",GetPublicParams.getTotalMemory(context));
		map.put("td", MarketApplication.mChannelID);
		publicParams = null;
		return map;
	}

}
