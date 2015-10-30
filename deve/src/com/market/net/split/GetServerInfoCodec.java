package com.market.net.split;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.market.net.DataCodec;

public class GetServerInfoCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		// TODO Auto-generated method stub
		JSONObject jsonObject,bodyJSONObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;
		String serverLstString = "";
		JSONArray jsonArray = null;
		if (TextUtils.isEmpty(result))
				return null;
		
		try 
		{
			jsonObject = new JSONObject(result);
			//headResult = jsonObject.getString("head");
			bodyResult = jsonObject.getString("body");
			
			if(TextUtils.isEmpty(bodyResult))
				return null;
			
			bodyJSONObject = new JSONObject(bodyResult);
			if(bodyJSONObject!=null && bodyJSONObject.has("errorCode")&& bodyJSONObject.getInt("errorCode")==0)
			{
				
				map = new HashMap<String, Object>();
				map.put("errorCode",bodyJSONObject.getInt("errorCode"));
				
				if(bodyJSONObject.has("serverLst"))
				{
					serverLstString = bodyJSONObject.getString("serverLst");
				}
				else
					return null;
				
				jsonArray = new JSONArray(serverLstString);
				JSONObject object = null;
				int modId = -1;
				for (int i = 0; i < jsonArray.length(); i++)
				{
					object = jsonArray.getJSONObject(i);
					modId = object.getInt("modId");
					if(modId == 1)
					{
						map.put("marketServ","http://"+object.getString("ip")+":"+object.getString("port"));
					}
					else if(modId == 2)
					{
						map.put("totalDataServ","http://"+object.getString("ip")+":"+object.getString("port"));
					}
					else if(modId == 3)
					{
						map.put("selfUpdateServ","http://"+object.getString("ip")+":"+object.getString("port"));
					}
				}
				
			}			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
	}

}
