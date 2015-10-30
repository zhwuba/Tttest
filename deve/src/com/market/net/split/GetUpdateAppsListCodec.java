package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetAppsUpdateResp;

public class GetUpdateAppsListCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result)
	{
		// TODO Auto-generated method stub
		JSONObject jsonObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;
		
		if (TextUtils.isEmpty(result))
				return null;
		try 
		{			
			jsonObject = new JSONObject(result);

			bodyResult = jsonObject.getString("body");			
			
			if(TextUtils.isEmpty(bodyResult))
				return map;

			Gson gson = new Gson();
			
			GetAppsUpdateResp appsUpdateResp = gson.fromJson(bodyResult, GetAppsUpdateResp.class);
			
			if(appsUpdateResp!=null && appsUpdateResp.getErrorCode()!=0)
				return map;
			
			map = new HashMap<String, Object>();
			map.put("appsUpdate", appsUpdateResp);
			map.put("errorCode", appsUpdateResp.getErrorCode());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
