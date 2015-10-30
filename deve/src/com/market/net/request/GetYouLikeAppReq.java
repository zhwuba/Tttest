package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;


public class GetYouLikeAppReq
{

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo;

    @Expose
    @SerializedName("marketId")
    private String marketId;

    public String getMarketId()
    {
        return marketId;
    }

    public void setMarketId(String marketId)
    {
        this.marketId = marketId;
    }

    public TerminalInfo getTerminalInfo()
    {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo)
    {
        this.terminalInfo = terminalInfo;
    }

}
