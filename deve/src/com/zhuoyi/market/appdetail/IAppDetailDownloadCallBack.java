package com.zhuoyi.market.appdetail;

import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;


public interface IAppDetailDownloadCallBack extends DownloadCallBackInterface {
	 public boolean onCancelDownload(String pkgName, int verCode);
	 
	 public boolean onResumeDownload(String pkgName, int verCode);
	 
}
