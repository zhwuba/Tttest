package com.zhuoyi.market.asyncTask;

import java.io.IOException;
import java.util.HashMap;

import com.zhuoyi.market.constant.Constant;
import com.market.net.DataCodecFactory;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.response.GetStartPageResp;
import com.market.net.utils.OpenUrlPostUtils;
import com.market.view.StartUpLayout;

import android.content.Context;
import android.text.TextUtils;

public class MarketStartUpAsyncTask extends MarketAsyncTask {

	private Context mContext;
	

	public MarketStartUpAsyncTask(Context context, String name, String group) {
		super(name, group);
		mContext = context;
	}
	
	@Override
	protected void run() {
		String result = "";
		HashMap<String, Object> map = null;
		String contents = SenderDataProvider.buildToJSONData(mContext,MessageCode.GET_STARET_PAGE,null);
		try {
            result = OpenUrlPostUtils.accessNetworkByPost(Constant.MARKET_URL, contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		if(!TextUtils.isEmpty(result)) {
            map = DataCodecFactory.fetchDataCodec(MessageCode.GET_STARET_PAGE).splitMySelfData(result);
            if(map != null && map.size() != 0) {
                GetStartPageResp info = (GetStartPageResp) map.get("startUpAdInfo");
                if (info != null)
                    StartUpLayout.onHandlerStartupImage(mContext, info);
            }
        }
	}

}
