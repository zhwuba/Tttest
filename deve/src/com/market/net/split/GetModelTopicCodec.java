package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetModelTopicResp;

public class GetModelTopicCodec implements  DataCodec {

	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
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
			
			GetModelTopicResp detailResp = gson.fromJson(bodyResult, GetModelTopicResp.class);
			
			if(detailResp!=null && detailResp.getErrorCode()!=0)
				return map;
			
			map = new HashMap<String, Object>();
			map.put("modelTopicInfo", detailResp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
