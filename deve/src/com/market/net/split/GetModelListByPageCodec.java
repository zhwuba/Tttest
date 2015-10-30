package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetApkListByPageResp;
import com.market.net.response.GetModelApkListByPageResp;

public class GetModelListByPageCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result)
	{
		JSONObject jsonObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;
		
		if (TextUtils.isEmpty(result)) {
				return null;
		}
		
		try  {			
			jsonObject = new JSONObject(result);

			bodyResult = jsonObject.getString("body");			
			
			if(TextUtils.isEmpty(bodyResult))
				return null;

			Gson gson = new Gson();
			
			GetModelApkListByPageResp listByPageResp = gson.fromJson(bodyResult, GetModelApkListByPageResp.class);
			
			if(listByPageResp!=null && listByPageResp.getErrorCode()!=0)
				return null;
			
			map = new HashMap<String, Object>();
			map.put("modelListByPage", listByPageResp);
			map.put("errorCode",1);	
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
