package com.market.view;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.zhuoyi.market.appManage.AppManageUtil;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.R;
import com.market.download.userDownload.DownloadEventInfo;
import com.market.download.userDownload.DownloadPool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommonTitleDownloadView extends RelativeLayout {

    private Context mContext = null;
    private View mMainView = null;
    private ImageView mDownloadingAnim = null;
    private ImageView mDownloadedAnim = null;
    private TextView mDownloadingNum = null;
//    private Animation mDownloadingAnimation = null;
    private String ACTION_DOWNLOAD_REFRESH = "download.refresh";
    private int mOldDownloadNum = -1;
    private boolean mDownloading = false;
    private boolean mUninstall = true;
    
    private AnimationDrawable mAnimationDrawable = null;


    public CommonTitleDownloadView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public CommonTitleDownloadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    public CommonTitleDownloadView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView(context);
    }


    private void initView(Context context) {

        mContext = context;

        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mMainView = tLayoutInflater.inflate(R.layout.common_title_download_view, null);
        mMainView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // 点击下载按钮事件
                AppManageUtil.startDownloadActivity(mContext);
            }

        });

        mDownloadingAnim = (ImageView) mMainView
                .findViewById(R.id.title_downloading_anim);
        
        mAnimationDrawable = (AnimationDrawable) mDownloadingAnim.getBackground();

        mDownloadedAnim = (ImageView) mMainView
                .findViewById(R.id.title_downloaded_anim);

        mDownloadingNum = (TextView) mMainView
                .findViewById(R.id.title_downloaded_num);
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        this.addView(mMainView, params);
    }


    private int getDownloadNum() {
        ConcurrentHashMap<String, DownloadEventInfo> downloadEvents = DownloadPool
                .getAllDownloadEvent(mContext);
        Iterator iter = downloadEvents.entrySet().iterator();
        Map.Entry entry = null;
        DownloadEventInfo eventInfo = null;
        int num = 0;
        mDownloading = false;
        mUninstall = false;

        if (downloadEvents.size() <= 0) {
            return -1;
        }

        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            eventInfo = (DownloadEventInfo) entry.getValue();
            if (eventInfo.getEventArray() != DownloadEventInfo.ARRAY_COMPLETE) {
                mDownloading = true;
                if (eventInfo.getEventArray() != DownloadEventInfo.ARRAY_PAUSED) {
                    num++;
                }
            } else {
                if (!mUninstall) {
                    mUninstall = getUninstall(eventInfo.getPkgName(), eventInfo.getVersionCode());
                    if (mUninstall) {
                        File file = eventInfo.getApkFile();
                        if(file==null || !file.exists()) {
                            mUninstall = false;
                        }
                    }
                }
            }
        }
        
        return DownloadPool.getDownAndWaitNum();
//        return num;
    }
    
    
    /**
     * 是否已经安装
     * @param info
     * @return
     */
    private boolean getUninstall(String pkgName, int verCode) {

       int installVerCode = MarketUtils.getInstalledApkVersionCode(mContext, pkgName);
       if (verCode > installVerCode) {
           return true;
       } else {
           return false;
       }
    }


    public void setDownloadStatus() {
        int num = getDownloadNum();
        String tag = (String) mDownloadedAnim.getTag();
        if (num <= 0) {
            setDownloadNumVisibility(false);
            if (mDownloading) {
                if (!"stop".equals(tag)) {
                    mDownloadedAnim.setBackgroundResource(R.drawable.home_title_download_stop);
                    mDownloadedAnim.setTag("stop");
                }
            } else {
                if (mUninstall) {
                    if (!"uninstall".equals(tag)) {
                        mDownloadedAnim.setBackgroundResource(R.drawable.home_title_download_uninstall);
                        mDownloadedAnim.setTag("uninstall");
                    }
                } else {
                    if (!"download".equals(tag)) {
                        mDownloadedAnim.setBackgroundResource(R.drawable.home_title_download_anim);
                        mDownloadedAnim.setTag("download");
                    }
                }

                if (mOldDownloadNum > 0 && mContext != null && mDownloadedAnim.getVisibility() == View.VISIBLE) {
                    Animation downloadedAnimation = AnimationUtils
                            .loadAnimation(mContext, R.anim.downloaded_anim);
                    mDownloadedAnim.setAnimation(downloadedAnimation);
                    mDownloadedAnim.startAnimation(downloadedAnimation);
                }
            }

            if (mAnimationDrawable.isRunning())
                mAnimationDrawable.stop();
            
//            if (mDownloadingAnimation != null) {
//                mDownloadingAnimation.cancel();
//                mDownloadingAnim.clearAnimation();
//                mDownloadingAnimation = null;
//            }

        } else {
            
            mDownloadedAnim.clearAnimation();
            
            if (num <= 1) {
                setDownloadNumVisibility(false);
                if (!"download".equals(tag)) {
                    mDownloadedAnim.setBackgroundResource(R.drawable.home_title_download_anim);
                    mDownloadedAnim.setTag("download");
                }

            } else {
                setDownloadNumVisibility(true);
                if(num > 99)
                	mDownloadingNum.setText("...");
                else
                	mDownloadingNum.setText("" + num);
            }
            
            if (!mAnimationDrawable.isRunning())
                mAnimationDrawable.start();

//            if (mDownloadingAnimation == null) {
//                mDownloadingAnimation = AnimationUtils.loadAnimation(mContext,
//                        R.anim.downloading_anim);
//                mDownloadingAnim.startAnimation(mDownloadingAnimation);
//                mDownloadingAnim.invalidate();
//            }
        }

        mOldDownloadNum = num;
    }


    private void setDownloadNumVisibility(boolean visibility) {
        if (visibility) {
            if (mDownloadedAnim.getVisibility() != View.GONE)
                mDownloadedAnim.setVisibility(View.GONE);
            if (mDownloadingNum.getVisibility() != View.VISIBLE)
                mDownloadingNum.setVisibility(View.VISIBLE);
        } else {
            if (mDownloadedAnim.getVisibility() != View.VISIBLE)
                mDownloadedAnim.setVisibility(View.VISIBLE);
            if (mDownloadingNum.getVisibility() != View.GONE)
                mDownloadingNum.setVisibility(View.GONE);
        }
    }


    public void registeredReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DOWNLOAD_REFRESH);
        mContext.registerReceiver(mReceiver, filter);
    }


    public void unRegisteredReceiver() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
        
        if (mAnimationDrawable.isRunning())
            mAnimationDrawable.stop();
//        if (mDownloadingAnimation != null) {
//            mDownloadingAnimation.cancel();
//            mDownloadingAnim.clearAnimation();
//            mDownloadingAnimation = null;
//        }
        mContext = null;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(ACTION_DOWNLOAD_REFRESH)) {
                setDownloadStatus();
            }
        }

    };

}
