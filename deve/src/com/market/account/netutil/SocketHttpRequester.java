package com.market.account.netutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;

import android.util.Log;

/**
 * 上传文件到服务器
 * 
 * @author Administrator
 * 
 */
public class SocketHttpRequester {

	public static String post(String path, Map<String, String> params)
			throws Exception {
		final String BOUNDARY = "---------------------------7da2137580612"; // 数据分隔?
		final String endline = "--" + BOUNDARY + "--\r\n";// 数据结束标志
		String result = null;

		StringBuilder textEntity = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {// 构?文本类型参数的实体数?
			textEntity.append("--");
			textEntity.append(BOUNDARY);
			textEntity.append("\r\n");
			textEntity.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"\r\n\r\n");
			textEntity.append(entry.getValue());
			textEntity.append("\r\n");
		}
		// 计算传输给服务器的实体数据?长度
		int dataLength = textEntity.toString().getBytes().length
				+ endline.getBytes().length;

		URL url = new URL(path);
		int port = url.getPort() == -1 ? 80 : url.getPort();
		// Socket socket = new Socket(InetAddress.getByName(url.getHost()),
		// port);
		Socket socket = new Socket();
		InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(url
				.getHost()), port);
		socket.connect(isa, 5000);
		OutputStream outStream = socket.getOutputStream();
		// 下面完成HTTP请求头的发?
		String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		String contenttype = "Content-Type: multipart/form-data; boundary="
				+ BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		String host = "Host: " + url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());

		// 写完HTTP请求头后根据HTTP协议再写?回车换行
		outStream.write("\r\n".getBytes());
		// 把所? 文本 类型的实体数据发送出?
		outStream.write(textEntity.toString().getBytes());
		// 下面发?数据结束标志，表示数据已经结?
		outStream.write(endline.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		if (null != reader && reader.readLine().indexOf("200") == -1) {// 读取web服务器返回的数据，判断请求码是否?00，如果不?00，代表请求失?
			Log.e("log_response", "response failed!");
			return null;
		} else {
			// */此处不应该打印，获取只是为了测试返回结果，正式发布版本前，要注掉此段代码

			String string;
			while (null != reader && ((string = reader.readLine()) != null)) {

				Log.e("log_response", string + "\n");
				if (string.startsWith("{")) {
					result = string;
				}

			}
			// */
		}
		outStream.flush();
		outStream.close();
		reader.close();
		socket.close();
		return new String(result.getBytes(), "utf-8").toString();
	}


	public static String postExternalFile(String path,
			Map<String, String> params, FormFile[] files) throws Exception {
		final String BOUNDARY = "---------------------------7da2137580612";
		final String endline = "--" + BOUNDARY + "--\r\n";

		int fileDataLength = 0;

		for (FormFile uploadFile : files) {
			StringBuilder fileExplain = new StringBuilder();
			fileExplain.append("--");
			fileExplain.append(BOUNDARY);
			fileExplain.append("\r\n");
			fileExplain.append("Content-Disposition: form-data;name=\""
					+ uploadFile.getParameterName() + "\";filename=\""
					+ uploadFile.getFilname() + "\"\r\n");
			fileExplain.append("Content-Type: " + uploadFile.getContentType()
					+ "\r\n\r\n");
			fileExplain.append("\r\n");
			fileDataLength += fileExplain.length();
			if (uploadFile.getInStream() != null) {
				fileDataLength += uploadFile.getFile().length();
			} else {
				fileDataLength += uploadFile.getData().length;
			}
		}
		StringBuilder textEntity = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			textEntity.append("--");
			textEntity.append(BOUNDARY);
			textEntity.append("\r\n");
			textEntity.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"\r\n\r\n");
			textEntity.append(entry.getValue());
			textEntity.append("\r\n");
		}

		int dataLength = textEntity.toString().getBytes().length
				+ fileDataLength + endline.getBytes().length;

		URL url = new URL(path);
		int port = url.getPort() == -1 ? 80 : url.getPort();
		// Socket socket = new Socket(InetAddress.getByName(url.getHost()),
		// port);
		Socket socket = new Socket();
		InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(url
				.getHost()), port);
		socket.connect(isa, 5000);
		OutputStream outStream = socket.getOutputStream();

		String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		String contenttype = "Content-Type: multipart/form-data; boundary="
				+ BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		String host = "Host: " + url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());

		outStream.write("\r\n".getBytes());

		outStream.write(textEntity.toString().getBytes());

		StringBuffer strBuf = new StringBuffer();

		for (FormFile uploadFile : files) {
			StringBuilder fileEntity = new StringBuilder();
			fileEntity.append("--");
			fileEntity.append(BOUNDARY);
			fileEntity.append("\r\n");
			fileEntity.append("Content-Disposition: form-data;name=\""
					+ uploadFile.getParameterName() + "\";filename=\""
					+ uploadFile.getFilname() + "\"\r\n");
			fileEntity.append("Content-Type: " + uploadFile.getContentType()
					+ "\r\n\r\n");
			outStream.write(fileEntity.toString().getBytes());
			if (uploadFile.getInStream() != null) {
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = uploadFile.getInStream().read(buffer, 0, 1024)) != -1) {
					outStream.write(buffer, 0, len);
				}
				uploadFile.getInStream().close();
			} else {
				outStream.write(uploadFile.getData(), 0,
						uploadFile.getData().length);
			}
			outStream.write("\r\n".getBytes());
		}

		outStream.write(endline.getBytes());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		if (null != reader && reader.readLine().indexOf("200") == -1) {
			return null;
		} else {
			String readStr;

			String contLenLable = "Content-Length: ";
			int contLength = 0;
			boolean readBody = false;
			while (null != reader && ((readStr = reader.readLine()) != null)) {
				if (readStr.startsWith(contLenLable)) {
					String lenStr = readStr.substring(contLenLable.length());
					contLength = Integer.parseInt(lenStr);
				}
				if (readStr.equals("")) {
					readBody = true;
				}
				if (contLength != 0 && readBody
						&& readStr.getBytes().length == contLength) {
					strBuf.append(readStr);
					break;
				}
			}

		}
		outStream.flush();
		outStream.close();
		reader.close();
		socket.close();
		return strBuf.toString();
	}


	public static String postExternalFile(String path,
			Map<String, String> params, FormFile file) throws Exception {
		if (file != null)
			return postExternalFile(path, params, new FormFile[] { file });
		else
			return postExternalFile(path, params, new FormFile[] {});
	}

}
