package com.market.net;

import android.content.Context;

public interface DataBuilder 
{
    public String buildToJson(Context context,int msgCode,Object obj);
}
