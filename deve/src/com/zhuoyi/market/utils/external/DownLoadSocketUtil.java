package com.zhuoyi.market.utils.external;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.market.download.userDownload.DownloadManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;


public class DownLoadSocketUtil {

	/**
	 * http://127.0.0.1:23456/query?type=details&partner=sogou&id=com.index1370228486373&appname=
	 * B.A.W&size=4461568&vc=0&vn=2.2.140228&icon=http%3A%2F%2Fp18.qhimg.com%2Ft010bf39d35a90d30bd.png
	 * &downurl=http%3A%2F%2Fwap.sogou.com%2Fapp%2Fredir.jsp%3Fappdown%3D1%26u%3DGOGCkIs8MWg6eJodBIrd3
	 * g0HugIzEHeN0gUlhbO3PB1J57tzeUjUAaOgPKuRb4oX755xkgjlzMxR2EtXk4GXDSPlz_RaiRL3zp0vLaesQr2-_q3Cjvy8
	 * 8DnCUIrC82OEUQ-VEmH4xIsF2D6xRegW8QM6r8Tk2laMvoW23pm_VYrqkthDm8_KTasuLRwbjN3lW7P3h0ZwNo79MNBWq5p
	 * i1clICPFuWwbrpM4IeeC3g87t7s3Z_qaLxMVvLvrq-_S5cki5TvxGr7c.%26docid%3D-5887071516245045574%26sour
	 * ceid%3D-4702237120198351676%26w%3D2030%26semob_app_name%3DB.A.W&flag=1&from=2&callback=callme&callback=jsonp2
	 */

	private Context mContext;
	private static ServerSocket mServerSocket;
	private static Handler mHandler;

	private String mDownloadInfo;
	private Thread mThread;

	public DownLoadSocketUtil(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}


	public void startServerSocket() {
		try {
			if(mServerSocket == null || !mServerSocket.isBound()) {
				mServerSocket = new ServerSocket(34567);
				mThread = new Thread(new Runnable() {

					@Override
					public void run() {
						while(true) {
							listenerBrowser();
						}

					}
				});
				mThread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void listenerBrowser() {

		InputStream inputStream = null;
		OutputStream outputStream = null;
		BufferedInputStream bufferedInputStream = null;
		PrintStream out = null;
		try {
			Socket socket = mServerSocket.accept();
			inputStream = socket.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufReader = new BufferedReader(inputStreamReader);
			// 从InputStream当中读取客户端所发送的数据  
			String str = null;
			if(!socket.isClosed() && !TextUtils.isEmpty(str = bufReader.readLine())) {  
				mDownloadInfo = str;
				if(!TextUtils.isEmpty(mDownloadInfo)) {
					mDownloadInfo = mDownloadInfo.split(" ")[1];
					Map<String,String> map = getDownloadParamsFromUrl(mDownloadInfo.toString());
					if(mHandler != null) {
						Message msg = mHandler.obtainMessage();
						msg.what = DownloadManager.EVENT_APK_WEB_REQUEST;
						msg.obj = map;
						mHandler.sendMessage(msg);
					}
					outputStream = socket.getOutputStream();
					out = new PrintStream(outputStream);
					out.println("HTTP/1.1 200 OK");  
					out.println();  
				}
			}  
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(inputStream != null) {
					inputStream.close();
				}
				if(bufferedInputStream != null) {
					inputStream.close();
				}
				if(outputStream != null) {
					outputStream.close();
				}
				if(out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	/**
	 * /query?type=details&partner=sogou&id=com.index1370228486373&appname=B.A.W&
	 * size=4461568&vc=0&vn=2.2.140228&icon=http://p18.qhimg.com/t010bf39d35a90d30bd
	 * .png&downurl=http://wap.sogou.com/app/redir.jsp?appdown=1&u=GOG
	 */
	private Map<String, String> getDownloadParamsFromUrl(String urlString){
		if(TextUtils.isEmpty(urlString)) return null;
		Map<String,String> map = new HashMap<String, String>();
//		urlString = URLDecoder.decode(urlString, "UTF-8");
		urlString = urlString.substring(urlString.indexOf("?") + 1);
		String[] parentArray = urlString.split("&");
		for (String string : parentArray) {
			String[] childArray = string.split("=", 2);
			if(childArray != null && childArray.length == 2) {
				map.put(childArray[0], childArray[1]);
			}
		}

		return map;
	}

	/**
	 * 根据参数下载apk
	 * @param map
	 */
	public static void downloadApk(Map<String, String> map, Context context) {
		if(map != null) {
			String pkgName = map.get("id");
			String iconUrl = map.get("icon");
			String downloadUrl = map.get("downurl");
			String appName = map.get("appname");
			String verCode = map.get("vc");
			String fileSize = map.get("size");
			try {
				pkgName = URLDecoder.decode(pkgName, "UTF-8");
				iconUrl = URLDecoder.decode(iconUrl, "UTF-8");
				appName = URLDecoder.decode(appName, "UTF-8");
				downloadUrl = URLDecoder.decode(downloadUrl, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			int vc = 0;
			long fz = 0;
			try {
				vc = Integer.parseInt(verCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				fz = Long.parseLong(fileSize);
			} catch (Exception e) {
				e.printStackTrace();
			}
			DownloadFor3rdParty downloadFor3rdParty = new DownloadFor3rdParty(context);
			downloadFor3rdParty.startDownloadApk(downloadUrl, iconUrl, pkgName, appName, vc , fz);
		}
		
		
	}

	private void setHttpResponse(URL url) {
	}
}
