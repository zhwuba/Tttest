package com.market.download.service;

interface IDownloadService{
    void setManagerMsgHandler(IBinder msgHandler);
    void setClientMsgHandler(IBinder msgHandler);
    void addDownloadWithoutNotifyReady(String pkgName, String appName, String md5, String url, String topicId, String flag, int verCode, int appId, long totalSize);
    void addDownload(String pkgName, String appName, String md5, String url, String topicId, String flag, int verCode, int appId, long totalSize);
    void addDiffDownload(String pkgName, String appName, String md5, String url, String topicId, String flag, int verCode, int appId, String diffDownUrl, long totalSize, long diffPatchSize);
    void startDownload(String pkgName, int verCode);
    void pauseDownload(String pkgName, int verCode);
    void cancelDownload(String pkgName, int verCode, boolean delFile);
    int getDownloadingNumber();
    int getDownloadState(String pkgName, int verCode);
    void ignoreUpdate(String pkgName, int verCode);
}