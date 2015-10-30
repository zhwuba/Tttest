package com.zhuoyi.market.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class XmlParseUrlUtil {

	//	MARKET_URL = "http://market.zhuoyi.co:2560";
	//	public static final String TOTAL_URL = "http://data.zhuoyi.co:2540";
	//    public static final String SELF_UPDATE_URL 

	private final static String MARKET = "market";
	public final static String MARKET_TAG = "market-url";
	public final static String TOTAL_TAG = "total-url";
	public final static String SELF_UPDATE_TAG = "self-update-url";

	public final static String ACCOUNT_TAG = "account-url";


	public static void saveUrlToXML(Map<String,String> map,OutputStream out){
		XmlSerializer serializer = Xml.newSerializer();
		try {
			serializer.setOutput(out,"UTF-8");
			serializer.startDocument("UTF-8", true);
			serializer.startTag(null, MARKET);
			for(Map.Entry<String, String> entry : map.entrySet()){
				serializer.startTag(null, entry.getKey());
				serializer.text(entry.getValue());
				serializer.endTag(null, entry.getKey());
			}
			serializer.endTag(null, MARKET);
			serializer.endDocument();
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public static Map<String,String> getMarketURLFromXml(InputStream input) throws IOException{
		Map<String, String> map = new HashMap<String, String>();
		String urlTAG = "";
		String urlText = ""; 
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(input, "UTF-8");
			int eventType = parser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT){
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if(!parser.getName().equals(XmlParseUrlUtil.MARKET)){
						
						urlTAG = parser.getName();
						urlText = parser.nextText();
						if(!TextUtils.isEmpty(urlTAG) && !TextUtils.isEmpty(urlText)){
							map.put(urlTAG, urlText);
						}
					}
					break;
				case XmlPullParser.END_TAG:
						
					break;
				default:
					break;
				}
				eventType = parser.next();
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		return map;
	}
	
}
