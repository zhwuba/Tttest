package com.market.net.response;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.KeyWordInfoBto;

public class GetAssociativeWordResp extends BaseInfo
{

    @Expose
    @SerializedName("assWords")
    private List<KeyWordInfoBto> assWords;

    public List<KeyWordInfoBto> getAssWords()
    {
        return assWords;
    }

    public void setAppList(List<KeyWordInfoBto> assWords)
    {
        this.assWords = assWords;
    }

}
