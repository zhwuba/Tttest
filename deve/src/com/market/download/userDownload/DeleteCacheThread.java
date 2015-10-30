package com.market.download.userDownload;

import java.io.File;

import android.content.Context;
import android.util.Log;

public class DeleteCacheThread extends Thread
{
  String mFilePath;
  Context mContext;

  public DeleteCacheThread(Context context,String path)
  {
    this.mFilePath = path;
    mContext = context;
  }
  public void deleteMyPathCacheFile(File root)
  {
		File files[] = root.listFiles();
		
		if (files != null)
		{
			for (File f : files)
			{
				if (f.isDirectory())
				{
					deleteMyPathCacheFile(f);
				}
				else
				{
					f.delete();	
					Log.e("cache", "delete:"+f.getName());
				}
			}
			root.delete();
		}
		
  }
  public void run()
  {
	  File file = new File(mFilePath);
	  if(file!=null&& file.exists())
	  {
		  deleteMyPathCacheFile(file);
		  file.delete();
	  }
  }
}
