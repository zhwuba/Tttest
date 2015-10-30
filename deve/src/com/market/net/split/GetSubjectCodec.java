package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetSubjectResp;

public class GetSubjectCodec implements DataCodec {

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
			
			GetSubjectResp listResp = gson.fromJson(bodyResult, GetSubjectResp.class);
			
			map = new HashMap<String, Object>();
			map.put("subjectResp", listResp);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return map;
	}

}
