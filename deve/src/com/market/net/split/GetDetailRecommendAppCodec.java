package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetRecommendAppResp;

public class GetDetailRecommendAppCodec implements DataCodec
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

			//Log.e("shuaiqingDebug", "GetDetailRecommendAppCodec:"+bodyResult);
			Gson gson = new Gson();
			
			GetRecommendAppResp detailRecommendResp = gson.fromJson(bodyResult, GetRecommendAppResp.class);
			
			if(detailRecommendResp!=null && detailRecommendResp.getErrorCode()!=0)
				return map;
			
			map = new HashMap<String, Object>();
			map.put("detailRecommendInfo", detailRecommendResp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return map;
		
	}

}
