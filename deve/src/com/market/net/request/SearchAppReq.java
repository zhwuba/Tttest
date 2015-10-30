package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class SearchAppReq
{

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo;

    @Expose
    @SerializedName("key")
    private String keyword;

    @Expose
    @SerializedName("start")
    private int start;

    @Expose
    @SerializedName("pSize")
    private int pageSize;
    
    @Expose
    @SerializedName("fr")
    private String fr;

    public String getFrId()
    {
        return fr;
    }

    public void setFrId(String id)
    {
        this.fr = id;
    }
    
    public TerminalInfo getTerminalInfo()
    {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo)
    {
        this.terminalInfo = terminalInfo;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

}
