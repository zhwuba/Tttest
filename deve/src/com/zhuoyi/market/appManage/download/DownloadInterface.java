package com.zhuoyi.market.appManage.download;

import java.util.List;

public interface DownloadInterface {
    /**
     *download start
     **/
    public boolean downloadStart(String pacName, int verCode);
    
    
    /**
     *download pause 
     */
    public boolean downloadPause(String pkgName, int verCode);
    
    
    /**
     *download delete 
     */
    public boolean downloadDeleteItem(String pkgName, int verCode, boolean delItemFile);
    
    
    /**
     *download all 
     */
    public void downloadDeleteAll(List<String> pkgName, List<Integer> verCode, boolean delItemFile);
}

