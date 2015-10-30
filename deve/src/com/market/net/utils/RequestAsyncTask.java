package com.market.net.utils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.market.net.DataCodecFactory;

public class RequestAsyncTask extends AsyncTask<Object, Object, String> {
	private WeakReference<Handler> mHandler;
	private int mMsgWhat;
	private int mMessageCode = 0;
	private String mContents = "";
	
	
	public RequestAsyncTask(Handler handler,int msgWhat,String contents) {
		mHandler = new WeakReference<Handler>(handler);  
		mMsgWhat = msgWhat;
		mContents = contents;
	}
	
	
	protected String doInBackground(Object... params) {
        String result = "";
        String url = "";
        HashMap<String, Object> map = null;
        Handler handler = null;
        int msgCode = -1;

        url = (String) params[0];

        mMessageCode = (Integer) params[1];

       // contents = SenderDataProvider.buildToJSONData(mContext,mMessageCode,obj);// 103001

        try {
            result = OpenUrlPostUtils.accessNetworkByPost(url, mContents);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!TextUtils.isEmpty(result)) {
            map = DataCodecFactory.fetchDataCodec(mMessageCode).splitMySelfData(result);
            if(map==null || map.size()==0) {
                //  get data fails;
                msgCode = -1;
            } else if(map.containsKey("errorCode")) {
                msgCode = Integer.valueOf(map.get("errorCode").toString());
            }
        }

        if(isCancelled()) {
        	return null;
        }
        
        try {
            Message msg = new Message();
            msg.what    = mMsgWhat;
            msg.arg1    = msgCode;
            msg.obj     = map;	
            if(mHandler!=null)
                handler = mHandler.get();
            if(handler!=null)
                handler.sendMessage(msg);           
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
	}

	
	@Override
	protected void onCancelled() {
		mHandler = null;
		super.onCancelled();
	}
	
	@Override
	protected void onPostExecute(String result) {	
		super.onPostExecute(result);		
	}

}
