package com.zhuoyi.market.utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;
import com.market.net.data.AppInfoBto;
import com.market.net.data.CornerIconInfoBto;
import com.market.net.data.DiscoverAppInfoBto;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;

/**
 * {app列表显示/操作类}
 *  <br>
 * Create on : 2015-9-18 上午11:27:42<br>
 * @author pc<br>
 * @version zhuoyiStore v0.0.1
 * 
 */
public class AppOperatorUtils {

    private static final String STATE_PAUSED = "paused";
    private static final String STATE_DOWNLOADING = "downloading";
    private static final String STATE_OPEN = "open";
    private static final String STATE_INSTALLING = "installing";
    
    /**
     * {初始化安装按钮的状态}.
     * @param mContext
     * @param install
     * @param pName
     * @param vCode
     * @param icon
     */
    public static void initBtnState(Context mContext, TextView install, String pName, int vCode, Object icon) {
        boolean[] needData = isCurrentPacKageDownloading(mContext, pName, vCode);
        if (needData[0]) {
            install.setBackgroundResource(R.drawable.common_installing_btn);
            install.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
            if (needData[1]) {
                install.setText(R.string.dialog_proceed);
                install.setTag(STATE_PAUSED);
            } else {
                install.setText(R.string.down_noti_downloading_title);
                install.setTag(STATE_DOWNLOADING);
            }
        } else {
            if (MarketUtils.checkInstalled(mContext, pName)) {
                if (MarketUtils.isEqualsVersionCode(mContext, String.valueOf(vCode), pName)) {
                    install.setBackgroundResource(R.drawable.common_open_btn);
                    install.setTextColor(mContext.getResources().getColor(R.color.common_app_open_color));
                    install.setText(R.string.open);
                    install.setTag(STATE_OPEN);
                } else {
                    install.setBackgroundResource(R.drawable.common_update_btn);
                    install.setTextColor(mContext.getResources().getColor(R.color.common_app_update_color));
                    install.setText(R.string.update);
                    install.setTag(icon);
                }
            } else if (needData[2]) {
            	install.setBackgroundResource(R.drawable.common_installing_btn_normal);
                install.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
                install.setText(R.string.install_now);
                install.setTag(STATE_INSTALLING);
            } else {
                install.setBackgroundResource(R.drawable.common_install_btn);
                install.setTextColor(mContext.getResources().getColor(R.color.common_app_install_color));
                install.setText(R.string.install);
                install.setTag(icon);
            }
        }
    }

    
    /**
     * {检查app是否在下载列表中}.
     * @param mContext
     * @param pkgName
     * @param versionCode
     * @return
     */
    private static boolean[] isCurrentPacKageDownloading(Context mContext, String pkgName, int versionCode) {
        ConcurrentHashMap<String, DownloadEventInfo> mAllDownloadEvent = DownloadPool.getAllDownloadEvent(mContext);
        String name_version = getEventSignal(pkgName, versionCode);
        DownloadEventInfo eventInfo = mAllDownloadEvent.get(name_version);
        boolean[] data = { false, false, false};
        if (eventInfo != null) {
            if (eventInfo.getEventArray() != DownloadEventInfo.ARRAY_COMPLETE) {
                data[0] = true;
                if (eventInfo.getEventArray() == DownloadEventInfo.ARRAY_PAUSED) {
                    data[1] = true;
                }
            } else {
            	if (DownloadEventInfo.STATE_INSTALLING == eventInfo.getCurrState()) {
            		data[2] = true;
            	}
            }
        }
        return data;
    }

    private static String getEventSignal(String pkgName, int verCode) {
        return pkgName + Integer.toString(verCode);
    }

    
    /**
     * {暂停下载}.
     * @param context
     * @param mDownloadCallBack
     * @param v
     * @param pName
     * @param vCode
     */
    public static void downBasePause(Context context, WeakReference<DownloadCallBackInterface> mDownloadCallBack,
        View v, String pName, int vCode) {

        if (mDownloadCallBack != null && mDownloadCallBack.get() != null) {
            if (mDownloadCallBack.get().downloadPause(pName, vCode)) {
                if (v instanceof TextView) {
                    TextView tV = (TextView) v;
                    tV.setBackgroundResource(R.drawable.common_installing_btn);
                    tV.setTextColor(context.getResources().getColor(R.color.common_app_installing_color));
                    tV.setText(R.string.dialog_proceed);
                    tV.setTag("paused");
                }
            }
        }
    }
    
    
    /**
     * {设置下载app的icon动画}.
     * @param mDownloadCallBack
     * @param imageView
     * @param pacName
     * @param versionCode
     */
    public static void setDownloadAnim(WeakReference<DownloadCallBackInterface> mDownloadCallBack,ImageView imageView,String pacName, int versionCode) {
        DownloadCallBackInterface callback = null;
        if(mDownloadCallBack == null)
            return;
        if(mDownloadCallBack.get()==null)
            return;
        if(imageView == null)return;
        Drawable drawable = imageView.getDrawable();
        if(drawable == null){
            drawable = imageView.getBackground();
        }
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);
        callback =  mDownloadCallBack.get();
        if(callback!=null)
            callback.startIconAnimation(pacName, versionCode, drawable, location[0], location[1]);
    }

        
    /**
     * {处理安装按钮的OnClick事件}
     *  <br>
     * Create on : 2015-9-18 下午4:50:26<br>
     * @author pc<br>
     * @version zhuoyiStore v0.0.1
     * 
     */
    public static class CommonAppClick implements OnClickListener {
        private Context mContext;
        private Object mObj;
        private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
        private String mTopicId;
        private String mReportFlag;
        private String sdPath1 = "";
        private String packageName = "";
        private int mVersionCode;
        private String appName = "";
        private String downloadUrl = "";
        private String filePath = "";
        private String mMd5 = "";
        private CornerIconInfoBto mCornerIconInfo;
        private int mAppId;
        private String mActivityUrl = "";
        private long mFileSize;
        private boolean mIsFavorite = false; 

        public CommonAppClick(Context context,Object obj,WeakReference<DownloadCallBackInterface> mDownloadCallBack,
            String topicId,String reportFlag,boolean isFavorite) {
            this.mObj = obj;
            this.mContext = context;
            this.mDownloadCallBack = mDownloadCallBack;
            this.mTopicId = topicId;
            this.mReportFlag = reportFlag;
            this.mIsFavorite = isFavorite;
            setCommonParam();
        }

        @Override
        public void onClick(View v) {
            if (mObj == null) {
                return;
            }
            doClickInstallButton(v);
        }

        private void doClickInstallButton(View v) {
            sdPath1 = MarketUtils.FileManage.getSDPath();
            if(v.getTag().toString().equals(STATE_DOWNLOADING)){
                AppOperatorUtils.downBasePause(mContext,mDownloadCallBack, v, packageName, mVersionCode);
                return;
            } 
            
            if (v.getTag().toString().equals(STATE_INSTALLING)) {
            	return;
            }
            
            if (v.getTag().toString().equals(STATE_OPEN)) {
                MarketUtils.openCurrentActivity(mContext, packageName);
                return;
            }
            checkFavoriteApk();
            if(MarketUtils.getAPNType(mContext) == -1) {
                Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
                return;
            } 
            if (TextUtils.isEmpty(sdPath1)) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_sd_card),Toast.LENGTH_SHORT).show();
                return;
            } 
            doBaseDownload(v, packageName, appName, filePath, downloadUrl);
        }
        
        private void doBaseDownload(View v, String packageName, String appName, String filePath, String downloadUrl) {
            setDownloadParam();
            if(v.getTag() instanceof ImageView){
                if (mActivityUrl != null && !mActivityUrl.equals("")) {
                    mActivityUrl += "?apk_id=" + mAppId + "&activity_id=" + mCornerIconInfo.getType();
                    new WebAppDao(mContext).saveWebAppInfo(mAppId, mActivityUrl);
                }
                AppOperatorUtils.setDownloadAnim(mDownloadCallBack,(ImageView)v.getTag(),packageName,mVersionCode);
            }
            if (mDownloadCallBack != null && mDownloadCallBack.get() != null)
                mDownloadCallBack.get().startDownloadApp(packageName, appName, filePath, mMd5, downloadUrl,
                    mTopicId, mReportFlag, mVersionCode, mAppId, mFileSize);
            if(v instanceof TextView){
                TextView tV = (TextView)v; 
                tV.setBackgroundResource(R.drawable.common_installing_btn);
                tV.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
                tV.setText(R.string.down_noti_downloading_title);
                tV.setTag(STATE_DOWNLOADING);
            }
        }

        private void setCommonParam() {
            if (mObj instanceof AppInfoBto) {
                AppInfoBto appInfo = (AppInfoBto)mObj;
                packageName = appInfo.getPackageName();
                mVersionCode = appInfo.getVersionCode();
                appName = appInfo.getName();
                downloadUrl = appInfo.getDownUrl();
            } else if (mObj instanceof DiscoverAppInfoBto) {
                DiscoverAppInfoBto discoverAppInfo = (DiscoverAppInfoBto)mObj;
                packageName = discoverAppInfo.getPackageName();
                mVersionCode = discoverAppInfo.getVersionCode();
                appName = discoverAppInfo.getAppName();
                downloadUrl = discoverAppInfo.getDownloadUrl();
            } else if (mObj instanceof FavoriteInfo) {
                FavoriteInfo favoriteInfo = (FavoriteInfo)mObj;
                packageName = favoriteInfo.getAppPackageName();
                mVersionCode = Integer.parseInt(favoriteInfo.getVersionCode());
                appName = favoriteInfo.getAppName();
                downloadUrl = favoriteInfo.getUrl();
                filePath = favoriteInfo.getLocalFilePath();
            }
        }
        
        private void setDownloadParam() {
            if (mObj instanceof AppInfoBto) {
                AppInfoBto appInfo = (AppInfoBto)mObj;
                mMd5 = appInfo.getMd5();
                mAppId = appInfo.getRefId();
                mCornerIconInfo = appInfo.getCornerMarkInfo();
                mActivityUrl = appInfo.getActivityUrl();
                mFileSize = appInfo.getFileSize();
            } else if (mObj instanceof DiscoverAppInfoBto) {
                DiscoverAppInfoBto discoverAppInfo = (DiscoverAppInfoBto)mObj;
                mMd5 = discoverAppInfo.getMd5();
                mAppId = discoverAppInfo.getAppId();
                mCornerIconInfo = discoverAppInfo.getCornerMarkInfo();
                mActivityUrl = discoverAppInfo.getActivityUrl();
                mFileSize = discoverAppInfo.getFileSize();
            } else if (mObj instanceof FavoriteInfo) {
                FavoriteInfo favoriteInfo = (FavoriteInfo)mObj;
                mMd5 = favoriteInfo.getMd5();
                mAppId = favoriteInfo.getAppId();
            }
        }
        
        private void checkFavoriteApk() {
            if (mIsFavorite) {
                File apkFile = new File(filePath);
                if(apkFile.exists()) {
                    if (mVersionCode <= MarketUtils.getApkFileVersionCode(mContext, filePath)) {
                        MarketUtils.AppInfoManager.AppInstall(filePath, mContext, packageName, appName);
                        return;
                    } else
                        apkFile.delete();                           
                }
            }
        }
    }
}
