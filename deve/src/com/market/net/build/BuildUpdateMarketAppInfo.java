package com.market.net.build;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhuoyi.market.constant.Constant;
import com.market.net.data.AppSnapshotBto;
import com.market.net.DataBuilder;
import com.market.net.SenderDataProvider;
import com.market.net.data.TerminalInfo;
import com.market.net.request.GetAppsUpdateReq;

public class BuildUpdateMarketAppInfo implements DataBuilder
{
    @Override
    public String buildToJson(Context context, int msgCode, Object obj)
    {
        // TODO Auto-generated method stub
        String result = "";

        String body = "";

        JSONObject jsObject = new JSONObject();

        if (context == null)
            return result;
        
        GetAppsUpdateReq appUpdateReq = (GetAppsUpdateReq)obj;
        
        if(appUpdateReq == null)
            appUpdateReq = new GetAppsUpdateReq();
        
        TerminalInfo terminalInfo = SenderDataProvider.generateTerminalInfo(context);
        
        appUpdateReq.setTerminalInfo(terminalInfo);
       
        appUpdateReq.setAppList(getInstallAppSnapshot(context));
        appUpdateReq.setFrId(Constant.CP_ID);
        
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        
        body = gson.toJson(appUpdateReq);

        try
        {
            jsObject.put("head", SenderDataProvider.buildHeadData(msgCode));
            
            jsObject.put("body", body);
            
            result = jsObject.toString();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    private List<AppSnapshotBto> getInstallAppSnapshot(Context context)
    {
        List<AppSnapshotBto> list = new ArrayList<AppSnapshotBto>();
        
        PackageManager pm = context.getPackageManager();
        
        List<PackageInfo> packages = pm.getInstalledPackages(0);        
        int length = packages.size();
        for (int i = 0; i < length; i++)
        {
            PackageInfo packageInfo = packages.get(i);
            
            if(packageInfo.packageName.contains("com.android")||packageInfo.packageName.contains("com.mediatek"))
                continue;
            AppSnapshotBto snapshot = new AppSnapshotBto();
            snapshot.setPackageName(packageInfo.packageName);
            snapshot.setVersionCode(packageInfo.versionCode);
            list.add(snapshot);
        }
        return list;
    }

}
