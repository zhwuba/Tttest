package com.market.net.response;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseInfo implements Serializable
{
    @Expose
    @SerializedName("errorCode")
	int errorCode = -1;
    @Expose
    @SerializedName("errorMessage")
	String errorMessage = "";
	public void setErrorCode(int msgCode)
	{
		errorCode = msgCode;
	}
	public int getErrorCode()
	{
		return errorCode;
	}
	public void setErrorMessage(String msg)
	{
		errorMessage = msg;
	}
	public String getErrorMessage()
	{
		return errorMessage;
	}	 
}

