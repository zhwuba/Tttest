package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetApkDetailResp;
import com.market.net.response.GetDiscoverResp;

public class GetDiscoverInfoCodec implements DataCodec
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
			
			GetDiscoverResp discoverResp = gson.fromJson(bodyResult, GetDiscoverResp.class);
			
			if(discoverResp!=null && discoverResp.getErrorCode()!=0)
				return map;
			
			map = new HashMap<String, Object>();
			map.put("discoverResp", discoverResp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
