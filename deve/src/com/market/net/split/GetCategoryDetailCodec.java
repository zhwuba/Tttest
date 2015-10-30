package com.market.net.split;

import java.util.HashMap;

import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.market.net.DataCodec;
import com.market.net.response.GetSoftGameDetailResp;

/**
 * 分类详情请求结果codec对象
 * @author JLu
 *
 */
public class GetCategoryDetailCodec  implements DataCodec {

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

			GetSoftGameDetailResp resp = gson.fromJson(bodyResult, GetSoftGameDetailResp.class);

			if(resp!=null && resp.getErrorCode()!=0)
				return map;

			map = new HashMap<String, Object>();
			map.put("categoryDetailResp", resp);
			map.put("errorCode", resp.getErrorCode());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return map;

	}

}
