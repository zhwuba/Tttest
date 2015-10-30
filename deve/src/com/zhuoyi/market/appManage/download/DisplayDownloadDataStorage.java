package com.zhuoyi.market.appManage.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.market.download.common.DownBaseInfo;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.appResident.MarketApplication;

import android.text.TextUtils;

/**
 * 下载管理数据管理
 * @author dream.zhou
 *
 */
public class DisplayDownloadDataStorage {
    
    private ArrayList<DisplayDownloadEventInfo> mDownloadInfo = new ArrayList<DisplayDownloadEventInfo>();
    private ArrayList<DisplayDownloadEventInfo> mCompleteInfo = new ArrayList<DisplayDownloadEventInfo>();
    private WebAppDao mWebAppDao = null;
    
    public DisplayDownloadDataStorage() {
    	mWebAppDao = new WebAppDao(MarketApplication.getRootContext());
    }
    
    
    public void freeResource() {
    	mWebAppDao = null;
    }
    
    
    /**
     * 获取下载中的应用个数
     * @return
     */
    public int getDownloadSize() {
        if (mDownloadInfo == null) {
            return 0;
        } else {
            return mDownloadInfo.size();
        }
    }
    
    
    /**
     * 获取下载完成的个数
     * @return
     */
    public int getCompleteSize() {
        if (mCompleteInfo == null) {
            return 0;
        } else {
            return mCompleteInfo.size();
        }
    } 
    
    
    /**
     * 下载中是否有数据
     * @return
     */
    public boolean isDownload() {
        if (getDownloadSize() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * 下载完成是否有数据
     * @return
     */
    public boolean isComplete() {
        if (getCompleteSize() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    
    /**
     * 获取下载中的某项展示数据
     * @param position
     * @return
     */
    public DisplayDownloadEventInfo getDownloadInfo(int position) {
        if (mDownloadInfo == null || position >= mDownloadInfo.size()  || position < 0) {
            return null;
        } else {
            return mDownloadInfo.get(position);
        }
    }
    
    
    /**
     * 获取下载完成的某项展示数据
     * @param position
     * @return
     */
    public DisplayDownloadEventInfo getCompleteInfo(int position) {
        if (mCompleteInfo == null || position >= mCompleteInfo.size()  || position < 0) {
            return null;
        } else {
            return mCompleteInfo.get(position);
        }
    }
    
    
    /**
     * 获取下载中的某项数据
     * @param position
     * @return
     */
    public DownloadEventInfo getDownloadEventInfo(int position) {
        if (mDownloadInfo == null || position >= mDownloadInfo.size()  || position < 0) {
            return null;
        } else {
            return mDownloadInfo.get(position).getDownloadEventInfo();
        }
    }
    
    
    /**
     * 获取下载完成的某项数据
     * @param position
     * @return
     */
    public DownloadEventInfo getCompleteEventInfo(int position) {
        if (mCompleteInfo == null || position >= mCompleteInfo.size()  || position < 0) {
            return null;
        } else {
            return mCompleteInfo.get(position).getDownloadEventInfo();
        }
    } 
    
    
    /**
     * 添加数据到下载中
     * @param info
     */
    public void addData2Download(DisplayDownloadEventInfo info) {
        if (mDownloadInfo == null || info == null) {
            return;
        } 
        
        insertListWithSort(mDownloadInfo, info);
    }
    
    
    /**
     * 添加数据到下载中
     * @param info
     */
    public void addData2Complete(DisplayDownloadEventInfo info) {
        if (mCompleteInfo == null || info == null) {
            return;
        } 
        
        //应用未安装且安装已经删除，该数据为废数据，不再需要
        if (!info.getDisplayInstalled()) {
            if (!info.getDisplayFileExist()) {
            	if (mWebAppDao != null)
                    mWebAppDao.removeWebAppInfo(info.getDownloadEventInfo().getAppId());
                return;
            }
        }
        
        insertListWithSort(mCompleteInfo, info);
    }
    
    
    /**
     * 添加数据到下载中
     * @param info
     */
    public void addData2Download(DownloadEventInfo eventInfo) {
        if (mDownloadInfo == null || eventInfo == null) {
            return;
        }
        
        DisplayDownloadEventInfo info = new DisplayDownloadEventInfo(eventInfo);
        info.initDisplayInfo(true);
        
        insertListWithSort(mDownloadInfo, info);
    } 
    
    
    /**
     * 添加数据到下载中
     * @param info
     */
    public void addData2Complete(DownloadEventInfo eventInfo) {
        if (mCompleteInfo == null || eventInfo == null) {
            return;
        } 
        
        //应用未安装且安装已经删除，该数据为废数据，不再需要
        DisplayDownloadEventInfo info = new DisplayDownloadEventInfo(eventInfo);
        info.setDisplayInstalled();
        if (!info.getDisplayInstalled()) {
            info.setDisplayFileExist();
            if (!info.getDisplayFileExist()) {
            	if (mWebAppDao != null)
                    mWebAppDao.removeWebAppInfo(eventInfo.getAppId());
                return;
            }
        }
        info.initDisplayInfo(false);
        
        insertListWithSort(mCompleteInfo, info);
    } 
    
    
    /**
     * 添加数据到下载列表或者下载完成列表，如果存在不再添加
     * @param eventInfo
     */
    public void addData2DisplayList(DownloadEventInfo eventInfo) {
        if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
            if (!isCompleteExistTheInfo(eventInfo)) {
                this.addData2Complete(eventInfo);
            }
        }else if (eventInfo.getCurrState()!= DownBaseInfo.STATE_CANCEL 
                && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_BACKGROUND
                && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_UPDATE) {
            if (!isDownlodExistTheInfo(eventInfo)) {
                this.addData2Download(eventInfo);
            }
        }
    }
    
    
    /**
     * 更新下载数据
     * @param eventInfo
     */
    public void updateDownloadInfo(DownloadEventInfo eventInfo) {
        
        int position = this.getDownloadPosition(eventInfo);
        if (position < 0) return;
        DisplayDownloadEventInfo displayInfo = mDownloadInfo.get(position);
        displayInfo.setDownloadEventInfo(eventInfo);
        displayInfo.refreshDisplayInfo(true);
    }
    
    
    /**
     * 更新下载完成数据
     * @param eventInfo
     */
    public void updateCompleteInfo(DownloadEventInfo eventInfo) {
        
        int position = this.getCompletePosition(eventInfo);
        if (position < 0) return;
        DisplayDownloadEventInfo displayInfo = mCompleteInfo.get(position);
        this.removeFromComplete(position);
        displayInfo.setDownloadEventInfo(eventInfo);
        displayInfo.refreshDisplayInfo(false);
        insertListWithSort(mCompleteInfo, displayInfo);
    } 
    
    
    /**
     * 暂停下载数据
     * @param eventInfo
     */
    public void pauseDownloadInfo(String pkgName, int verCode) {
        
        int position = this.getDownloadPosition(pkgName, verCode);
        if (position < 0) return;
        DisplayDownloadEventInfo displayInfo = mDownloadInfo.get(position);
        displayInfo.getDownloadEventInfo().downloadPause();
    }
    
    
    /**
     * 下载完成
     */
    public void downloadComplete(DownloadEventInfo eventInfo) {
        if (eventInfo == null) return;
 
        int position = this.getCompletePosition(eventInfo);
        if (position < 0) {
        	this.addData2Complete(eventInfo);
        	this.removeFromDownload(eventInfo.getPkgName(), eventInfo.getVersionCode());
        }
    }
    
    
    /**
     * 删除下载中的应用
     * @param pkgName
     * @param verCode
     * @return
     */
    public String delDownload(String pkgName, int verCode) {
        int position = this.getDownloadPosition(pkgName, verCode);
        DisplayDownloadEventInfo display = this.getDownloadInfo(position);
        if (display == null) return "";
        
        String result = display.getDisplayAppName();
        this.removeFromDownload(display.getDownloadEventInfo());
        return result;
    }
    

    /**
     * 删除下载完成中的应用
     * @param pkgName
     * @param verCode
     * @return
     */
    public String delComplete(String pkgName, int verCode) {
        int position = this.getCompletePosition(pkgName, verCode);
        DisplayDownloadEventInfo display = this.getCompleteInfo(position);
        if (display == null) return "";
        
        String result = display.getDisplayAppName();
        this.removeFromComplete(display.getDownloadEventInfo());
        return result;
    }    

    
    /**
     * 获取下载应用在列表中的位置
     * @param eventInfo
     * @return
     */
    public int getDownloadPosition(DownloadEventInfo eventInfo) {
        if (mDownloadInfo == null || eventInfo == null) {
            return -1;
        }
        int position = getDownloadPosition(eventInfo.getPkgName(), eventInfo.getVersionCode());
        return position;
    }
    
    
    /**
     * 获取安装完成应用在列表中的位置
     * @param eventInfo
     * @return
     */
    public int getCompletePosition(DownloadEventInfo eventInfo) {
        if (mCompleteInfo == null || eventInfo == null) {
            return -1;
        }
        int position = getCompletePosition(eventInfo.getPkgName(), eventInfo.getVersionCode());
        return position;
    } 
    
    
    /**
     * 获取下载应用在列表中的位置
     * @param eventInfo
     * @return
     */
    public int getDownloadPosition(String pkgName, int verCode) {
        if (mDownloadInfo == null || TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        int position = getPositionInList(mDownloadInfo, pkgName, verCode);
        return position;
    }
    
    
    /**
     * 获取安装完成应用在列表中的位置
     * @param eventInfo
     * @return
     */
    public int getCompletePosition(String pkgName, int verCode) {
        if (mCompleteInfo == null || TextUtils.isEmpty(pkgName)) {
            return -1;
        }
        int position = getPositionInList(mCompleteInfo, pkgName, verCode);
        return position;
    }

    
    /**
     * 下载中列表中是否存在数据
     * @param info
     */
    public boolean isDownlodExistTheInfo (DisplayDownloadEventInfo info) {
        if (mDownloadInfo == null || info == null) {
            return false;
        } 
        
        DownloadEventInfo eventInfo = info.getDownloadEventInfo();
        if (isExistInList(mDownloadInfo, eventInfo.getPkgName(), eventInfo.getVersionCode())) {
            return true;
        } else {
            return false;
        }
    } 

    
    /**
     * 下载完成列表中是否存在数据
     * @param info
     */
    public boolean isCompleteExistTheInfo (DisplayDownloadEventInfo info) {
        if (mCompleteInfo == null || info == null) {
            return false;
        } 
        
        DownloadEventInfo eventInfo = info.getDownloadEventInfo();
        if (isExistInList(mCompleteInfo, eventInfo.getPkgName(), eventInfo.getVersionCode())) {
            return true;
        } else {
            return false;
        }
    } 
    
    
    /**
     * 下载中列表中是否存在数据
     * @param info
     */
    public boolean isDownlodExistTheInfo (DownloadEventInfo info) {
        if (mDownloadInfo == null || info == null) {
            return false;
        } 
        
        if (isExistInList(mDownloadInfo, info.getPkgName(), info.getVersionCode())) {
            return true;
        } else {
            return false;
        }
    } 
    
    
    /**
     * 下载完成列表中是否存在数据
     * @param info
     */
    public boolean isCompleteExistTheInfo (DownloadEventInfo info) {
        if (mCompleteInfo == null || info == null) {
            return false;
        } 

        if (isExistInList(mCompleteInfo, info.getPkgName(), info.getVersionCode())) {
            return true;
        } else {
            return false;
        }
    } 
    
    
    /**
     * 删除某个下载中的应用数据
     * @param position
     */
    private void removeFromDownload(int position) {
        if (mDownloadInfo == null || position >= mDownloadInfo.size() || position < 0) {
            return;
        }
        
        mDownloadInfo.remove(position);
    }
    
    
    /**
     * 删除某个下载完成的应用数据
     * @param position
     */
    private void removeFromComplete(int position) {
        if (mCompleteInfo == null || position >= mCompleteInfo.size()  || position < 0) {
            return;
        }
        
        mCompleteInfo.remove(position);
    }

    
    /**
     * 删除某个下载中的应用数据
     * @param pkgName
     * @param verCode
     */
    public void removeFromDownload(String pkgName, int verCode) {
        int position = this.getPositionInList(mDownloadInfo, pkgName, verCode);
        removeFromDownload(position);
    }
    
    
    /**
     * 删除某个下载完成的应用数据
     * @param pkgName
     * @param verCode
     */
    public void removeFromComplete(String pkgName, int verCode) {
        int position = this.getPositionInList(mCompleteInfo, pkgName, verCode);
        removeFromComplete(position);
    } 
    
    
    /**
     * 删除某个下载中的应用数据
     * @param pkgName
     * @param verCode
     */
    public void removeFromDownload(DownloadEventInfo info) {
        if (info == null) return;
        
        if (mWebAppDao != null)
            mWebAppDao.removeWebAppInfo(info.getAppId());
        
        removeFromDownload(info.getPkgName(), info.getVersionCode());
    }
    
    
    /**
     * 删除某个下载完成的应用数据
     * @param pkgName
     * @param verCode
     */
    public void removeFromComplete(DownloadEventInfo info) {
        if (info == null) return;
        
        if (mWebAppDao != null)
            mWebAppDao.removeWebAppInfo(info.getAppId());
        
        removeFromComplete(info.getPkgName(), info.getVersionCode());
    } 
    
    
    /**
     * 清空下载数据
     */
    public void clearDownload() {
        if (mDownloadInfo == null) {
            return;
        }
        
        mDownloadInfo.clear();
    } 
    
    
    /**
     * 清空下载完成数据
     */
    public void clearComplete(boolean clearAll) {
        if (mCompleteInfo == null) {
            return;
        }
        
        if (clearAll && mWebAppDao != null) {
        	int size = mCompleteInfo.size();
        	for (int i=0; i<size; i++) {
        		mWebAppDao.removeWebAppInfo(mCompleteInfo.get(i).getDownloadEventInfo().getAppId());
        	}
        }
        
        mCompleteInfo.clear();
    }
    
    
    /**
     * 重置数据
     */
    public void reInitAllInfo() {
        this.clearComplete(false);
        this.clearDownload();
        this.initAllInfo();
    }    
    
    
    /**
     * 列表中是否已经存在改应用
     * @param infoList
     * @param pkgName
     * @param versionCode
     * @return
     */
    private boolean isExistInList(ArrayList<DisplayDownloadEventInfo> infoList, String pkgName, int versionCode) {
        if (infoList == null || TextUtils.isEmpty(pkgName)) return false;
        int size = infoList.size();
        if (size > 0) {
            int position = getPositionInList(infoList, pkgName, versionCode);
            if (position < 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    
    
    /**
     * 获取某个应用
     * @param infoList
     * @param pkgName
     * @param versionCode
     * @return
     */
    private int getPositionInList (ArrayList<DisplayDownloadEventInfo> infoList, String pkgName, int versionCode) {
        if (infoList == null || TextUtils.isEmpty(pkgName)) return -1;
        
        ArrayList<DisplayDownloadEventInfo> infoClone = (ArrayList<DisplayDownloadEventInfo>) infoList.clone();
        int size = infoClone.size();
        if (size > 0) {
            DownloadEventInfo info = null;
            for (int i=0; i<size; i++) {
                info = infoClone.get(i).getDownloadEventInfo();
                if (pkgName.equals(info.getPkgName()) && versionCode == info.getVersionCode()) {
                    return i;
                }
            }
            return -1;
        } else {
            return -1;
        }
    }  
    
    
    /**
     * 初始化数据
     */
    private void initAllInfo() {
        ConcurrentHashMap<String, DownloadEventInfo> allDownloadEvent = DownloadPool.getAllDownloadEvent(MarketApplication.getRootContext());
        if (allDownloadEvent != null && allDownloadEvent.size() <=0) {
        	if (mWebAppDao != null)
                mWebAppDao.removeAllWebAppInfo();
            return;
        }
        Iterator iter = allDownloadEvent.entrySet().iterator();
        Map.Entry entry = null;
        DownloadEventInfo eventInfo = null;
        while(iter.hasNext()){
            entry = (Map.Entry)iter.next(); 
            eventInfo = (DownloadEventInfo)entry.getValue();
            if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
                this.addData2Complete(eventInfo);
            } else if (eventInfo.getCurrState() != DownBaseInfo.STATE_CANCEL 
                    && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_BACKGROUND
                    && eventInfo.getEventArray() != DownloadEventInfo.ARRAY_UPDATE) {
                this.addData2Download(eventInfo);
            } else {
            	if (mWebAppDao != null)
                    mWebAppDao.removeWebAppInfo(eventInfo.getAppId());
            }
        }
    }
    
    
    private void insertListWithSort(ArrayList<DisplayDownloadEventInfo> sortList, DisplayDownloadEventInfo insertInfo) {
        int sortSize = sortList.size();
        DownloadEventInfo sortInfo = null;
        boolean downloadFinish = insertInfo.getDownloadEventInfo().getEventArray() == DownloadEventInfo.ARRAY_COMPLETE ? true:false;
        for (int i=0; i<sortSize; i++) {
            sortInfo = sortList.get(i).getDownloadEventInfo();
            if (downloadFinish) {
                if (sortInfo.getEventArray() == DownloadEventInfo.ARRAY_COMPLETE) {
                    if (insertInfo.getDisplayInstalled() && !sortList.get(i).getDisplayInstalled()){
                        continue;
                    }
                    
                    if (sortInfo.getSortTime() < insertInfo.getDownloadEventInfo().getSortTime()) {
                        sortList.add(i, insertInfo);
                        return;
                    }
                }
            } else {
                if (sortInfo.getEventArray() != DownloadEventInfo.ARRAY_COMPLETE) {
                    if (sortInfo.getSortTime() > insertInfo.getDownloadEventInfo().getSortTime()) {
                        sortList.add(i, insertInfo);
                        return;
                    }
                } 
            }
        }
        
        sortList.add(insertInfo);
    }
    
    
    public String getActivityUrl(int appId) {
    	if (mWebAppDao != null && appId > 0) {
    		return mWebAppDao.getWebUrl(appId);
    	} else {
    		return null;
    	}
    }
}
