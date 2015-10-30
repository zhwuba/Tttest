package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetRecommendAppReq
{

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo;

    @Expose
    @SerializedName("resId")
    private int resId;
    
    @Expose
    @SerializedName("label")
    private String label;
    
    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public TerminalInfo getTerminalInfo()
    {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo)
    {
        this.terminalInfo = terminalInfo;
    }

    public int getResId()
    {
        return resId;
    }

    public void setResId(int resId)
    {
        this.resId = resId;
    }

}
