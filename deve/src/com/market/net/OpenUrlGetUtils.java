package com.market.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

public class OpenUrlGetUtils
{
    public static String accessNetworkByGet(String url)
    {
        InputStream is = null;
        BufferedInputStream bis = null;
        ByteArrayBuffer baf = null;
        URLConnection ucon = null;
        String result = "";
        try 
        {
            URL myURL = new URL(url);
            ucon = myURL.openConnection();
            ucon.setConnectTimeout(15*1000);
            ucon.setReadTimeout(30*1000);
            
            is = ucon.getInputStream();
            bis = new BufferedInputStream(is);
            baf = new ByteArrayBuffer(1024);
            int current = 0;

            while ((current = bis.read()) != -1)
            {
                baf.append((byte) current);
            }
            result = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
            if (null !=result && result.equals("zero"))
            {
                result = null;
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            if (baf != null)
            {
                baf.clear();
                baf = null;
            }
    
            try {
                if (bis != null)
                {
                    bis.close();
                    bis = null;
                }

                if (is != null)
                {
                    is.close();
                    is = null;
                }
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;

    }
}
