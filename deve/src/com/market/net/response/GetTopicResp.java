package com.market.net.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.data.TopicInfoBto;

public class GetTopicResp extends BaseInfo
{

    @Expose
    @SerializedName("assLst")
    private List<AssemblyInfoBto> assemblyList;

    @Expose
    @SerializedName("topicLst")
    private List<TopicInfoBto> topicList;

    public List<AssemblyInfoBto> getAssemblyList()
    {
        return assemblyList;
    }

    public void setAssemblyList(List<AssemblyInfoBto> assemblyList)
    {
        this.assemblyList = assemblyList;
    }

    public List<TopicInfoBto> getTopicList()
    {
        return topicList;
    }

    public void setTopicList(List<TopicInfoBto> topicList)
    {
        this.topicList = topicList;
    }

    public void addAssemblyInfo(AssemblyInfoBto assemblyInfo)
    {
        if (assemblyList == null)
        {
            assemblyList = new ArrayList<AssemblyInfoBto>();
        }
        assemblyList.add(assemblyInfo);
    }

    public void addTopicInfo(TopicInfoBto topicInfo)
    {
        if (topicList == null)
        {
            topicList = new ArrayList<TopicInfoBto>();
        }
        topicList.add(topicInfo);
    }

}
