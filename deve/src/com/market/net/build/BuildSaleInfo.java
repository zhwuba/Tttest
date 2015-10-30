package com.market.net.build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.market.net.data.AppSnapshotBto;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.GetDataStatusReq;
import com.market.net.response.GetMarketFrameResp;
import com.zhuoyi.market.appResident.MarketApplication;

public class BuildSaleInfo implements DataBuilder
{
    @Override
    public String buildToJson(Context context, int msgCode, Object obj)
    {
        // TODO Auto-generated method stub
        String result = "";

        String body = "";

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式

        Date time = new Date();
        
        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;
        
        GetDataStatusReq dataStatusReq = (GetDataStatusReq)obj;
        
        if(dataStatusReq == null)
            dataStatusReq = new GetDataStatusReq();
        
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        GetMarketFrameResp marketResp = MarketApplication.getMarketFrameResp();
        if (marketResp != null) {
	        dataStatusReq.setMarketId(marketResp.getMarketId());
	        dataStatusReq.setTerminalInfo(terminalInfo);
	
	        dataStatusReq.setActTime(df.format(time));
	        
	        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	        
	        body = gson.toJson(dataStatusReq);
	
	        try
	        {
	            jsObject.put("head", SenderDataProvider.buildHeadData(msgCode));
	            
	            jsObject.put("body", body);
	            
	            result = jsObject.toString();
	            
	        } catch (JSONException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
        }
        return result;
    }
    private List<AppSnapshotBto> getInstallAppSnapshot(Context context)
    {
        List<AppSnapshotBto> list = new ArrayList<AppSnapshotBto>();
        
        PackageManager pm = context.getPackageManager();
        
        List<PackageInfo> packages = pm.getInstalledPackages(0);        

        for (int i = 0; i < packages.size(); i++)
        {
            PackageInfo packageInfo = packages.get(i);
            
            if(packageInfo.packageName.contains("com.android")||packageInfo.packageName.contains("com.mediatek"))
                continue;
            try
            {
                AppSnapshotBto snapshot = new AppSnapshotBto();
                snapshot.setPackageName(packageInfo.packageName);
                snapshot.setVersionCode(packageInfo.versionCode);
                list.add(snapshot);
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }
        }
       // Log.e("shuaiqingDebug", "list size:"+list.size());
        return list;
    }

}
