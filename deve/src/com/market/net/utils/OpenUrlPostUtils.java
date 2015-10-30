package com.market.net.utils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.LogHelper;


public class OpenUrlPostUtils {
    
    public OpenUrlPostUtils()
	{
		
	}
	
	public static byte[] addAll(byte[] array1, byte[] array2)
	{
		if (array1 == null)
	       return clone(array2);
	    if (array2 == null) {
	       return clone(array1);
	    }
	    byte[] joinedArray = new byte[array1.length + array2.length];
	    System.arraycopy(array1, 0, joinedArray, 0, array1.length);
	    System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
	    return joinedArray;
	}

	public static byte[] clone(byte[] array)
	{
	   if (array == null) {
	      return null;
	   }
	   return ((byte[])(byte[])array.clone());
	}
	public static String accessNetworkByPost(String urlString,String contents,boolean maxTime) throws IOException
	{
	       String line = "";
	        DataOutputStream out = null;
	        URL postUrl;
	        
	        BufferedInputStream bis = null;
	        ByteArrayBuffer baf = null;
	        boolean isPress = false;
	        HttpURLConnection connection = null;
	        //String url = "http://101.95.97.178:9093";//"http://joyreachapp.cn:2578";//
	    
	        try
	        {       
	            byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"), Constant.ENCODE_DECODE_KEY.getBytes());

	            postUrl = new URL(urlString);   
	            connection = (HttpURLConnection) postUrl.openConnection();
	            connection.setDoOutput(true);
	            connection.setDoInput(true);
	            if(maxTime)
	            {
	                connection.setConnectTimeout(10000);
	                connection.setReadTimeout(20000);
	            }
	            else
	            {
	                connection.setConnectTimeout(8000);
	                connection.setReadTimeout(10000);
	            }
	            connection.setRequestMethod("POST");
	            connection.setInstanceFollowRedirects(true);
	            connection.setRequestProperty("contentType", "utf-8");
	            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
	            connection.setRequestProperty("Content-Length", ""+encrypted.length);
	            
	            out = new DataOutputStream(connection.getOutputStream());
	            out.write(encrypted);
	            out.flush();
	            out.close();

	            bis = new BufferedInputStream(connection.getInputStream());
	            baf = new ByteArrayBuffer(1024);
	            
	            isPress = Boolean.valueOf(connection.getHeaderField("isPress"));
	            
	            int current = 0;

	            while ((current = bis.read()) != -1)
	            {
	                baf.append((byte) current);
	                
	            }
	            
	            if(baf.length()>0)
	            {
	                byte unCompressByte[];
	                byte[] decrypted;
	                if(isPress)
	                {              
	                    //Log.e("shuaiqingDe@@@@", "compress length:"+baf.length());
	                    decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
	                    unCompressByte = ZipUtil.uncompress(decrypted);
	                   // Log.e("shuaiqingDe@@@@", "length:"+unCompressByte.length);
	                    line = new String(unCompressByte); 
	                }
	                else
	                {
	                    decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
	                    line = new String(decrypted); 
	                }
	                
	                              
	            }
	        
	        }
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	        finally
	        {
	            if(connection!=null)
	                connection.disconnect();
	            if(bis!=null)
	                bis.close();
	            if(baf!=null)
	                baf.clear();
	        }
	        return line.trim();    

	}
	public static String accessNetworkByPost(String urlString,String contents) throws IOException
	{
		String line = "";
		DataOutputStream out = null;
		URL postUrl;
		
		BufferedInputStream bis = null;
		ByteArrayBuffer baf = null;
		boolean isPress = false;
		HttpURLConnection connection = null;
		//String url = "http://101.95.97.178:9093";//"http://joyreachapp.cn:2578";//
	
		try
		{		
			byte[] encrypted = DESUtil.encrypt(contents.getBytes("utf-8"), Constant.ENCODE_DECODE_KEY.getBytes());

			postUrl = new URL(urlString);	
			connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(20000);
			connection.setRequestMethod("POST");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("contentType", "utf-8");
			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", ""+encrypted.length);
			
			out = new DataOutputStream(connection.getOutputStream());
			out.write(encrypted);
			out.flush();
			out.close();

			bis = new BufferedInputStream(connection.getInputStream());
			baf = new ByteArrayBuffer(1024);
			
			isPress = Boolean.valueOf(connection.getHeaderField("isPress"));
			
			int current = 0;

			while ((current = bis.read()) != -1)
			{
				baf.append((byte) current);
				
			}
			
			if(baf.length()>0)
			{
				byte unCompressByte[];
				byte[] decrypted;
				if(isPress)
				{
				   // Log.e("shuaiqingDe@@@@", "compress length:"+baf.length());				
					decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
					unCompressByte = ZipUtil.uncompress(decrypted);
                   // Log.e("shuaiqingDe@@@@", "length:"+unCompressByte.length);
                    line = new String(unCompressByte); 
				}
				else
				{
				    decrypted = DESUtil.decrypt(baf.toByteArray(), Constant.ENCODE_DECODE_KEY.getBytes());
				    line = new String(decrypted);   
				}
				
							
			}
		
		} catch (OutOfMemoryError e){
		    LogHelper.trace();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		finally
		{
			if(connection!=null)
				connection.disconnect();
			if(bis!=null)
				bis.close();
			if(baf!=null)
				baf.clear();
		}
		return line.trim();
		
	}
	

}
