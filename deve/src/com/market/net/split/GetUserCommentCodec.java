package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetStaticSearchAppResp;
import com.market.net.response.GetUserCommentResp;

public class GetUserCommentCodec implements DataCodec {

	
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		JSONObject jsonObject,bodyJSONObject;
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
			
			GetUserCommentResp resp = gson.fromJson(bodyResult, GetUserCommentResp.class);
			
            if(resp!=null && resp.getErrorCode()!=0)
                return map;
            map = new HashMap<String, Object>();
            map.put("userCommentInfo", resp);
                    
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
		
		return map;
	}
	
}
