package com.zhuoyi.market.asyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import com.market.net.DataCodecFactory;
import com.market.net.utils.OpenUrlPostUtils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class MarketRequestTask extends MarketAsyncTask {

    private WeakReference<Handler> mHandler;
    private int mMsgWhat;
    private int mMessageCode = 0;
    private String mContents = "";
    private String mUrl = "";
    
    public MarketRequestTask(String name, String group, Handler handler,
            int msgWhat, String contents, String url, int msgCode) {
        super(name, group);
        
        mHandler = new WeakReference<Handler>(handler);  
        mMsgWhat = msgWhat;
        mContents = contents;
        
        mUrl = url;
        mMessageCode = msgCode;
    }
    
    
    @Override
    public boolean isTaskAlive() {
        if(mHandler != null && super.isTaskAlive()) {
            return true;
        }else {
            return false;
        }
        
    }
    
    
    @Override
    protected void run() {
        String result = "";
        HashMap<String, Object> map = null;
        int msgCode = -1;

        // contents = SenderDataProvider.buildToJSONData(mContext,mMessageCode,obj);// 103001

        try {
            result = OpenUrlPostUtils.accessNetworkByPost(mUrl, mContents);
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

        if(!isTaskAlive()) {
            mHandler = null;
            return;
        }
        
        try {
            Message msg = new Message();
            msg.what    = mMsgWhat;
            msg.arg1    = msgCode;
            msg.obj     = map;  
            Handler handler = mHandler.get();
            if(handler!=null) {
                handler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mHandler = null;
    }
}
