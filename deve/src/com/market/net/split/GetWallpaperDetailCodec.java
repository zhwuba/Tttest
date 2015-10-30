package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetWallpaperDetailResp;
import com.market.net.response.GetWallpaperListResp;

public class GetWallpaperDetailCodec implements DataCodec
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
			
			GetWallpaperDetailResp listResp = gson.fromJson(bodyResult, GetWallpaperDetailResp.class);
			
			if(listResp!=null && listResp.getErrorCode()!=0)
				return map;
			
			map = new HashMap<String, Object>();
			map.put("wallpaperDetail", listResp);
			map.put("errorCode",1);	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
