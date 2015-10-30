package com.market.net.split;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetStartPageResp;

public class GetStartupPageCodec implements DataCodec
{
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		// TODO Auto-generated method stub
		JSONObject jsonObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;
		
		if (TextUtils.isEmpty(result))
				return null;
		
		try 
		{
			Gson gson = new Gson();
			jsonObject = new JSONObject(result);
			bodyResult = jsonObject.getString("body");
	        if(TextUtils.isEmpty(bodyResult))
	            return null;
			
			GetStartPageResp info = gson.fromJson(bodyResult, GetStartPageResp.class);
			if(info == null)
                return null;

			map = new HashMap<String, Object>();
			map.put("startUpAdInfo", info);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
		
		return map;
	}

}
