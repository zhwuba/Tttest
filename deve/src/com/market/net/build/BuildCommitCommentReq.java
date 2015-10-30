package com.market.net.build;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.CommitUserCommentReq;

public class BuildCommitCommentReq implements DataBuilder{

	@Override
	public String buildToJson(Context context, int msgCode, Object obj) {

        // TODO Auto-generated method stub
        String result = "";

        String body = "";

        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;
        
        CommitUserCommentReq commitUserCommentReq = (CommitUserCommentReq)obj;
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        commitUserCommentReq.setTerminalInfo(terminalInfo);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        body = gson.toJson(commitUserCommentReq);
        try
        {
            jsObject.put("head", SenderDataProvider.buildHeadData(msgCode));
            
            jsObject.put("body", body);
            
            result = jsObject.toString();
            
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    
		
	}

}