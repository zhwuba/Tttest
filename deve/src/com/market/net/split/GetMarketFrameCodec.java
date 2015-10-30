package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetMarketFrameResp;

public class GetMarketFrameCodec implements DataCodec {
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		JSONObject jsonObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;

		try {			
			jsonObject = new JSONObject(result);

			bodyResult = jsonObject.getString("body");			

			if(TextUtils.isEmpty(bodyResult)) {
				return map;
			}

			Gson gson = new Gson();

			GetMarketFrameResp marketFrameResp = gson.fromJson(bodyResult, GetMarketFrameResp.class);

			if(marketFrameResp==null) {
				return map;
			}
			
			if(marketFrameResp!=null && marketFrameResp.getErrorCode()!=0) {
				return map;
			}


			map = new HashMap<String, Object>();
			map.put("marketFrame", marketFrameResp);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return map;

	}

}
