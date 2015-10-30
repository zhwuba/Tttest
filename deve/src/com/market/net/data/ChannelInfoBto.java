package com.market.net.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChannelInfoBto implements Serializable {

    @Expose
    @SerializedName("chnId")
    private String channelId;

    @Expose
    @SerializedName("name")
    private String channelName;

    @Expose
    @SerializedName("topicLst")
    private List<TopicInfoBto> topicList = new ArrayList<TopicInfoBto>();

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public List<TopicInfoBto> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<TopicInfoBto> topicList) {
        this.topicList = topicList;
    }

    public void addTopicInfo(TopicInfoBto topicInfo) {
        topicList.add(topicInfo);
    }

}
