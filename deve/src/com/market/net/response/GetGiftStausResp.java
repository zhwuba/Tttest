package com.market.net.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetGiftStausResp extends BaseInfo
{

    @Expose
    @SerializedName("desc")
    private String desc;
    
    @Expose
    @SerializedName("action")
    private String action;
    
    @Expose
    @SerializedName("title")
    private String title;    
    
    @Expose
    @SerializedName("url")
    private String url;
    
    @Expose
    @SerializedName("status")
    private int status;    

    //desc
    public String getDesc()
    {
        return desc;
    }
    
    public void setDesc(String desc)
    {
        this.desc = desc;
    }     

    //action
    public void setAction(String action)
    {
        this.action = action;
    }
    
    public String getAction()
    {
        return action;
    }
    
    //title
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getTitle()
    {
        return title;
    }    

    //url
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    //status
    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }    

}
