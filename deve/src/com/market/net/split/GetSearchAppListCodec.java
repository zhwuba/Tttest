package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.SearchAppResp;

public class GetSearchAppListCodec implements DataCodec {
	@Override
	public HashMap<String, Object> splitMySelfData(String result) {
		JSONObject jsonObject;
		String bodyResult = "";
		HashMap<String, Object> map = null;

		if (TextUtils.isEmpty(result))
				return null;
		
		try {
			Gson gson = new Gson();
	
			jsonObject = new JSONObject(result);

			bodyResult = jsonObject.getString("body");
			
			if(TextUtils.isEmpty(bodyResult))
				return null;
			
			//Log.e("shuaiqingDebug","GetSearchAppListCodec: "+bodyResult);
			SearchAppResp resp = gson.fromJson(bodyResult, SearchAppResp.class);
			
            if(resp!=null && resp.getErrorCode()!=0)
                return map;
            map = new HashMap<String, Object>();
            map.put("searchAppListInfo", resp);
            map.put("errorCode",1);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
		
		return map;
	}

}
