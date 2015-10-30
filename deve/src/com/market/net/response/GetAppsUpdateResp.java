package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AppInfoBto;

public class GetAppsUpdateResp extends BaseInfo
{

    @Expose
    @SerializedName("appLst")
    private List<AppInfoBto> appList;

    @Expose
    @SerializedName("silentAppLst")
    private List<AppInfoBto> silentAppLst;
    
    @Expose
    @SerializedName("isForcedUp")
    private int isForcedUp;
    
    @Expose
    @SerializedName("isZeroFlow")
    private int isZeroFlow;
    
    @Expose
    @SerializedName("zeroFlowTime")
    private int zeroFlowTime;
    
    @Expose
    @SerializedName("spreadTime")
    private int spreadTime;
    
    public List<AppInfoBto> getSilentAppList() {
        return silentAppLst;
    }
    
    public void setSilentAppList(List<AppInfoBto> appList) {
        silentAppLst = appList;
    }
    
    public List<AppInfoBto> getAppList() {
        return appList;
    }

    public void setAppList(List<AppInfoBto> appList) {
        this.appList = appList;
    }

    public void setIsForcedUp(int isForcedUp) {
        this.isForcedUp = isForcedUp;
    }


    public int getIsForcedUp() {
		return isForcedUp;
	}
    
    
    public void setSpreadTime(int spreadTime) {
        this.spreadTime = spreadTime;
    }
    
    
    public int getSpreadTime() {
        return spreadTime;
    }
    
    
    public void setIsZeroFlow(int isZeroFlow) {
        this.isZeroFlow = isZeroFlow;
    }
    
    
    public int getIsZeroFlow() {
        return isZeroFlow;
    }
    
    
    public void setZeroFlowTime(int zeroFlowTime) {
        this.zeroFlowTime = zeroFlowTime;
    }
    
    
    public int getZeroFlowTime() {
        return zeroFlowTime;
    }
    
    
    
}
