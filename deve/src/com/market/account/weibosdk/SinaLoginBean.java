/**
 * 
 */
package com.market.account.weibosdk;

/**
 * @author sunlei
 * 
 */
public class SinaLoginBean
{
    public String getExpires_in()
    {
        return expires_in;
    }
    public void setExpires_in(String expires_in)
    {
        this.expires_in = expires_in;
    }
    public String getAccess_token()
    {
        return access_token;
    }
    public void setAccess_token(String access_token)
    {
        this.access_token = access_token;
    }
    public String getUid()
    {
        return uid;
    }
    public void setUid(String uid)
    {
        this.uid = uid;
    }
    public String getRefresh_token()
    {
        return refresh_token;
    }
    public void setRefresh_token(String refresh_token)
    {
        this.refresh_token = refresh_token;
    }
    private String expires_in;
    private String access_token;
    private String uid;
    private String refresh_token;
   
}
