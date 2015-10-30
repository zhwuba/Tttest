package com.zhuoyi.market.asyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.market.net.response.BaseInfo;

public class MarketFrameInfoTask extends MarketAsyncTask {

	
	private String mFrameName;
	private String mFilePath;
	private BaseInfo mFrameResp;
	private String mVersionCode;
	
	public MarketFrameInfoTask(String name, String group) {
		super(name, group);
		mFrameName = name;
		mFilePath = group;
	}

	
	public MarketFrameInfoTask(String name, String group, BaseInfo frameResp, String versionCode) {
		super(name, group);
		mFrameName = name;
		mFilePath = group;
		mFrameResp = frameResp;
		mVersionCode = versionCode;
	}
	
	@Override
	protected void run() {
		File file = new File(mFilePath);
		if(!file.exists()) {
			file.mkdirs();
		}
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			File frameFile = new File(file, mFrameName + mVersionCode);
			File frameTmpFile = new File(file, mFrameName + mVersionCode + ".tmp");
			fos = new FileOutputStream(frameTmpFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(mFrameResp);
			oos.flush();
			
			frameFile.delete();
			frameTmpFile.renameTo(frameFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
		    if(oos != null) {
		        try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		    }
		    
		    if(fos != null) {
		        try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
		    }
		}
	}

}
