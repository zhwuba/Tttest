package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.UserFeedbackResp;

public class GetUserFeedbackCodec implements DataCodec {

	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		HashMap<String, Object> map = null;

		if (TextUtils.isEmpty(result))
				return null;
		
		try {
			Gson gson = new Gson();
	
			JSONObject jsonObject = new JSONObject(result);
			String bodyResult = jsonObject.getString("body");
			
			if(TextUtils.isEmpty(bodyResult))
				return null;
			
			UserFeedbackResp resp = gson.fromJson(bodyResult, UserFeedbackResp.class);
			
            if(resp!=null && resp.getErrorCode()!=0)
                return map;
            map = new HashMap<String, Object>();
            map.put("userFeedbackResp", resp);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
		
		return map;
	}

}
