package com.market.net.split;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.market.net.DataCodec;

public class ApkCheckSelfUpdateCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		// TODO Auto-generated method stub
		
		JSONObject jsonObject,bodyJSONObject;
		String headResult = "";
		String bodyResult = "";
		HashMap<String, Object> map = null;
		if (TextUtils.isEmpty(result))
				return null;
		
		try 
		{
			jsonObject = new JSONObject(result);
			headResult = jsonObject.getString("head");
			bodyResult = jsonObject.getString("body");
			bodyJSONObject = new JSONObject(bodyResult);
			if(bodyJSONObject!=null && bodyJSONObject.has("errorCode")&& bodyJSONObject.getInt("errorCode")==0)
			{
				map = new HashMap<String, Object>();
				if(bodyJSONObject.has("title"))
					map.put("title", bodyJSONObject.getString("title"));
				if(bodyJSONObject.has("content"))
					map.put("content", bodyJSONObject.getString("content"));
				if(bodyJSONObject.has("policy"))
					map.put("policy", bodyJSONObject.getInt("policy"));
				if(bodyJSONObject.has("pName"))
					map.put("pName",bodyJSONObject.getString("pName"));
				if(bodyJSONObject.has("ver"))
					map.put("ver",bodyJSONObject.getString("ver"));
				if(bodyJSONObject.has("fileUrl"))
					map.put("fileUrl",bodyJSONObject.getString("fileUrl"));
				if(bodyJSONObject.has("md5"))
					map.put("md5",bodyJSONObject.getString("md5"));
				if(bodyJSONObject.has("errorCode"))
					map.put("errorCode",bodyJSONObject.getInt("errorCode"));
				if(bodyJSONObject.has("totalSize"))
				    map.put("totalSize",bodyJSONObject.getLong("totalSize"));
			}			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
	}

}
