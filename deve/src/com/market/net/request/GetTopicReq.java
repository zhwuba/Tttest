package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetTopicReq
{

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo = new TerminalInfo();

    @Expose
    @SerializedName("topicId")
    private int topicId = -1;


    private int extension;
    
    @Expose
    @SerializedName("path")
    private String clickPath;
    
    private String item_name;
    
    private int topicIndex;
    
    private int channelIndex;

    @Expose
    @SerializedName("start")
    private int start=0;

    @Expose
    @SerializedName("fixed")
    private int fixedLength;

    @Expose
    @SerializedName("marketId")
    private String marketId;
    
    
    public String getItem_name()
    {
        return item_name;
    }

    public void setItem_name(String item_name)
    {
        this.item_name = item_name;
    }

    public TerminalInfo getTerminalInfo()
    {
        return terminalInfo;
    }

    public void setTerminalInfo(TerminalInfo terminalInfo)
    {
        this.terminalInfo = terminalInfo;
    }

    public int getTopicId()
    {
        return topicId;
    }

    public int getTopicIndex()
    {
        return topicIndex;
    }

    public void setTopicIndex(int topicIndex)
    {
        this.topicIndex = topicIndex;
    }

    public void setTopicId(int topicId)
    {
        this.topicId = topicId;
    }

    public int getExtension()
    {
        return extension;
    }

    public void setExtension(int extension)
    {
        this.extension = extension;
    }

    public int getChannelIndex()
    {
        return channelIndex;
    }

    public void setChannelIndex(int channelIndex)
    {
        this.channelIndex = channelIndex;
    }

    public String getClickPath()
    {
        return clickPath;
    }

    public void setClickPath(String clickPath)
    {
        this.clickPath = clickPath;
    }
    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getFixedLength()
    {
        return fixedLength;
    }

    public void setFixedLength(int fixedLength)
    {
        this.fixedLength = fixedLength;
    }

    public String getMarketId()
    {
        return marketId;
    }

    public void setMarketId(String marketId)
    {
        this.marketId = marketId;
    }
    
}
