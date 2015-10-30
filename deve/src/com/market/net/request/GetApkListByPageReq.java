package com.market.net.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.market.net.data.TerminalInfo;

public class GetApkListByPageReq
{

    @Expose
    @SerializedName("tInfo")
    private TerminalInfo terminalInfo;
    
	@Expose
	@SerializedName("sWidth")
	private short screenWidth;

	@Expose
	@SerializedName("sHeight")
	private short screenHeight;

	@Expose
	@SerializedName("assId")
	private int assemblyId;

	@Expose
	@SerializedName("start")
	private int start;

	@Expose
	@SerializedName("fixed")
	private int fixedLength;

	@Expose
	@SerializedName("marketId")
	private String marketId;

	@Expose
	@SerializedName("assIds")
	private int[] assemblyIds;

	public int[] getAssemblyIds() {
		return assemblyIds;
	}

	/**
	 * 
	 * @param assemblyIds 分类全部ID
	 */
	public void setAssemblyIds(int[] assemblyIds) {
		this.assemblyIds = assemblyIds;
	}

	public String getMarketId()
	{
		return marketId;
	}

	public void setMarketId(String marketId)
	{
		this.marketId = marketId;
	}

	public int getAssemblyId()
	{
		return assemblyId;
	}

	public void setAssemblyId(int assemblyId)
	{
		this.assemblyId = assemblyId;
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

	public short getScreenWidth()
	{
		return screenWidth;
	}

	public void setScreenWidth(short screenWidth)
	{
		this.screenWidth = screenWidth;
	}

	public short getScreenHeight()
	{
		return screenHeight;
	}

	public void setScreenHeight(short screenHeight)
	{
		this.screenHeight = screenHeight;
	}
	
	
    public TerminalInfo getTerminalInfo() {
      return terminalInfo;
    }
    

    public void setTerminalInfo(TerminalInfo terminalInfo) {
      this.terminalInfo = terminalInfo;
    }
}
