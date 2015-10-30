package com.zhuoyi.market.home;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.market.account.login.BaseHtmlActivity;
import com.market.account.netutil.HttpOperation;
import com.market.account.netutil.JavaScriptOperation;
import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.userDownload.DownloadManager;
import com.market.net.data.AppInfoBto;
import com.market.net.data.AssemblyInfoBto;
import com.market.net.data.TopicInfoBto;
import com.market.net.response.GetGiftStausResp;
import com.market.net.response.GetMarketFrameResp;
import com.market.statistics.ReportFlag;
import com.market.view.AdViewPager;
import com.zhuoyi.market.R;
import com.zhuoyi.market.WebActivity;
import com.zhuoyi.market.adapter.ViewPagerAdapter;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.appdetail.AppDetailInfoActivity;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.home.AdOnPageChangeListener.ViewPagerScrollCallback;
import com.zhuoyi.market.topic.TopicInfoActivity;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.gallery.BitmapUtiles;

public class HomeAdView implements OnClickListener, ViewPagerScrollCallback {


    private Context mContext;
    private View mView;
    private LinearLayout mDotLayout;
    private AdViewPager mAdViewPager;
    private boolean mSpecialSuspensionButtonsShow = false;
    private ViewPagerAdapter mViewPagerAdapter;
    private HomeListAdapter mHomeListAdapter;
    private Handler mAutoChangeHandler; 
    private Runnable mRunnable;
    private ArrayList<View> mViewList;
    private List<AppInfoBto> mAdInfoList;

    private int mCurrentPosition = 0;
    private static final int AD_IMAGE_COUNT = 6;
    private ImageView mDots[] = new ImageView[AD_IMAGE_COUNT];
    private ImageView   mADImage[] = new ImageView[AD_IMAGE_COUNT];
    private int mAdImgStates[] = new int[AD_IMAGE_COUNT];
    private static final int AD_DOWN_NOT_OVER = 0;
    private static final int AD_DOWN_SUCCESSED = 1;
    private static final int AD_DOWN_FAILED = -1;
    private static final int AD_REDOWN_FAILED = -2;
    private boolean isAutoscroll = false;
    private boolean isHomeShow = false;
    
    private boolean mViewPagerScrolling = false;
    public static int mHeignt;
    private boolean mInitFinish = false;
      
    public HomeAdView(Context context) {
        mContext = context;
        LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
        mView = tLayoutInflater.inflate(R.layout.layout_ad_header, null);
    }
    
    public View getMyView() {
        return mView;
    }
    
    
    public void freeViewResource() {
        BitmapDrawable bd;
        if (mRunnable != null && mAutoChangeHandler!=null) {
            mAutoChangeHandler.removeCallbacks(mRunnable);
            mAutoChangeHandler = null;
        }
        
        if(mHomeListAdapter!=null) {
            mHomeListAdapter.freeImageCache();
        }
        for(int i=0;mADImage!=null&&i<mADImage.length;i++) {
            bd = (BitmapDrawable)mADImage[i].getBackground();
            if(bd!=null && mAdImgStates[i]==AD_DOWN_SUCCESSED) {     
                bd.setCallback(null);
               /* if(bd.getBitmap()!=null)
                    bd.getBitmap().recycle();*/

           }   
        }

        mContext = null;
    }
    
    private void findViews() {
        mAdViewPager = (AdViewPager)mView.findViewById(R.id.ad_viewpager);
        int height = mContext.getResources().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams layout= mAdViewPager.getLayoutParams();
        if(height <= 800) {
            
            final float SCALE = mContext.getResources().getDisplayMetrics().density;        
            int valuePixels = (int) (SCALE * 130f);
            layout.height = valuePixels;
            mAdViewPager.setLayoutParams(layout);
   
        }

        
        mHeignt= layout.height;

        mDotLayout = (LinearLayout)mView.findViewById(R.id.dot_layout);
        LinearLayout.LayoutParams lp;
        for(int i=0;i<mDots.length;i++) {
            ImageView imageView = new ImageView(mContext);
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 15, 0);

            imageView.setLayoutParams(lp);

            imageView.setBackgroundResource(R.drawable.page_indicator_unfocused);
            mDots[i] = imageView;
            
            mDotLayout.addView(mDots[i]);
        }
        mDots[0].setImageResource(R.drawable.page_indicator_focused);
        mViewList = new ArrayList<View>();         
     
        for(int i=0;i<AD_IMAGE_COUNT;i++) {
            mViewList.add(LayoutInflater.from(mContext).inflate(R.layout.layout_ad_itmes, null)); 
            mADImage[i] = (ImageView)mViewList.get(i).findViewById(R.id.itmes_bg);
            mADImage[i].setTag(i);
            mADImage[i].setOnClickListener(this);
            mADImage[i].setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            mADImage[i].setScaleType(ScaleType.FIT_XY);
        }
        
        if(mViewPagerAdapter==null) 
            mViewPagerAdapter = new ViewPagerAdapter(mViewList);
        
        mAdViewPager.setAdapter(mViewPagerAdapter);
        mAdViewPager.setOnPageChangeListener(new AdOnPageChangeListener(mScrollAdHandler,this));
        mAdViewPager.setOnClickListener(this);
        mCurrentPosition = 498;
        mAdViewPager.setCurrentItem(mCurrentPosition);
        
    }
    
    
    private void initViews() {
    
        mRunnable = new Runnable() {
            boolean fromLeftToRight = true;
            @Override
            public void run() {

                if(mViewPagerScrolling) {
                    // nothing
                } else {
                    if(mCurrentPosition == mViewPagerAdapter.getCount()-1)
                        fromLeftToRight = false;
                    else if(mCurrentPosition==0)
                        fromLeftToRight = true;

                    if(fromLeftToRight)
                        mCurrentPosition++;
                    else
                        mCurrentPosition--;

                }

                int showTime = 0; 
                final int adPosition = mCurrentPosition%AD_IMAGE_COUNT;
                try {
                    //=====第adPosition页的停留展示时间
                    showTime = mAdInfoList.get(adPosition).getShowTime() * 1000;
                    if(showTime == 0) {
                        showTime = 3000;
                    }
                } catch(IndexOutOfBoundsException e) {
                    showTime = 0;
                    mAdImgStates[adPosition] = AD_REDOWN_FAILED;
                } catch(NullPointerException e) {
                    showTime = 3000;
                }

                if(mAdImgStates[adPosition] == AD_DOWN_NOT_OVER) { 
                    //第adPosition页图片的下载动作还没完成
                    if(fromLeftToRight)
                        mCurrentPosition--;
                    else
                        mCurrentPosition++;
                    showTime = 1000;
                } else {
                    if(mAdViewPager != null); {
                        mAdViewPager.setCurrentItem(mCurrentPosition,true);
                        isAutoscroll = true;
                    }
                    if(mAdImgStates[adPosition] == AD_DOWN_FAILED) {
                        //======首次下载失败的图片重新下载一次
                        new Thread() {
                            @Override
                            public void run() {
                                final Bitmap bitmap = downloadAdImages(mAdInfoList.get(adPosition));
                                if (mAutoChangeHandler != null) {
	                                mAutoChangeHandler.post(new Runnable() {
	                                    public void run() {
	                                        if(bitmap != null) {
	                                            BitmapDrawable drawable = new BitmapDrawable(bitmap);
	                                            mADImage[adPosition].setBackgroundDrawable(drawable);   
	                                            //                                       bitmap.recycle();
	                                            mAdImgStates[adPosition] = AD_DOWN_SUCCESSED;
	                                        } else {
	                                            mAdImgStates[adPosition] = AD_REDOWN_FAILED;
	                                        }
	                                    }
	                                });
                                }
                            }
                        }.start();
                    }
                }
                if (mAutoChangeHandler != null)
                	mAutoChangeHandler.postDelayed(mRunnable, showTime); 
            }
        };
        
        mAutoChangeHandler = new Handler();
        
    }
    
    public void notifyDataSetChanged(String pacName) {
        mHomeListAdapter.notifyDataSetChanged();
    }   

    public void enterAdView() {
        if(mInitFinish)
            return;
        mInitFinish = true;
        
        findViews();
        initViews();
        
    }
    

    private Handler mScrollAdHandler = new Handler() {
        
        public void handleMessage(Message msg) {
            int position = msg.what;
             if(position>2) {  
                 position = position%AD_IMAGE_COUNT;  
                }  
            for(int i=0;i<mDots.length;i++) {
                mDots[i].setImageResource(R.drawable.page_indicator_unfocused);
            }

            mDots[position].setImageResource(R.drawable.page_indicator_focused);
            mCurrentPosition = msg.what;
            
            AppInfoBto info = null;
            if (mAdInfoList != null && mAdInfoList.size() > 0 && position < mAdInfoList.size()) {
                info = mAdInfoList.get(position);
            }
            adForStatistics(info, isAutoscroll, !mSpecialSuspensionButtonsShow, isHomeShow);
            isAutoscroll = false;
            
        }
    };

    private void startActivityFromAd(int index) {       
        Intent intent = null;
        int refId = 0;
        String topicName = "";
        String imageName = "";
        String briefDesc = "";
        String imageUrl = "";
        int versionCode;
        if (mAdInfoList!=null && mAdInfoList.size()>0 && index < AD_IMAGE_COUNT) {
            AppInfoBto mAppInfoBto =  mAdInfoList.get(index);
            
            String webUrl = mAppInfoBto.getWebUrl();
            if (webUrl != null && !webUrl.equals("")) {
                intent = new Intent(mContext, WebActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("wbUrl", webUrl);
                intent.putExtra("titleName", mAppInfoBto.getName());
                mContext.startActivity(intent);
                
                //for record user behavior log
                UserLogSDK.logViewClickEvent(mContext, UserLogSDK.getAdWebDetailDes(LogDefined.VIEW_HOME_AD, mAppInfoBto.getName()));
            } else {
                intent = new Intent(mContext, AppDetailInfoActivity.class);
                refId = mAppInfoBto.getRefId();
                topicName = mAppInfoBto.getName();
                briefDesc = mAppInfoBto.getBriefDesc();
                imageName = "HTTP_AD_"+mAppInfoBto.getImgUrl().hashCode();
                imageUrl = mAppInfoBto.getImgUrl();
                versionCode = mAppInfoBto.getVersionCode();
                if(mAppInfoBto.getResType()==2) {
                    intent.setClass(mContext, TopicInfoActivity.class); 
                    intent.putExtra("mCID", refId);
                    intent.putExtra("position", index);
                    intent.putExtra("mTopicName", topicName);
                    intent.putExtra("mTopicInfo", briefDesc);
                    intent.putExtra("mTopicImage", imageName);
                    intent.putExtra("imageUrl", imageUrl);
                    intent.putExtra("version_code", versionCode);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("app_integral", mAppInfoBto.getIntegral());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                    
                    //for record user behavior log
                    String logDes = UserLogSDK.getAdSpecialDetailDes(LogDefined.VIEW_HOME_AD, Integer.toString(refId), topicName);
                    UserLogSDK.logViewClickEvent(mContext, logDes);
                    
                    
                } else if(mAppInfoBto.getResType()==1) {
                    MarketUtils.startAppDetailActivity(mContext, mAppInfoBto, ReportFlag.FROM_HOME_AD, -1);
                    //DownloadManager.startServiceReportOffLineLog(mContext, ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_HOME_AD);
                    DownloadManager.startServiceReportHomeAdLog(mContext, mAppInfoBto.getPackageName());
                    
                    //for record user behavior log
                    String logDes = UserLogSDK.getAdApkDetailDes(LogDefined.VIEW_HOME_AD,
                                                                Integer.toString(mAppInfoBto.getRefId()),
                                                                mAppInfoBto.getPackageName(),
                                                                mAppInfoBto.getName());
                    UserLogSDK.logViewClickEvent(mContext, logDes);
                    
                }
                
            }
        }
    }

    
    //开始滚动广告栏
    private void autoChangeHandler() {
        if(mAdInfoList!=null) {
            final int firstShowTime = mAdInfoList.get(0).getShowTime()*1000;
            
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(firstShowTime == 0 ? 3000 : firstShowTime);
                        while(mAdImgStates[0] == AD_DOWN_NOT_OVER) {
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mAutoChangeHandler!=null) {
                        mAutoChangeHandler.post(mRunnable);
                    }
                };
            }.start();
        }
        
    }
    
    @Override
    public void onClick(View v) {
        int index = -1;
        if(v.getTag()!=null) {
            try {
                index = (Integer)v.getTag();
                
                startActivityFromAd(index);
                
            } catch(Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    public void setAdData(GetMarketFrameResp resp) {
        try {
            List<TopicInfoBto> topicList =  resp.getChannelList().get(0).getTopicList();
            if (topicList.size()==0) {
                return;
            }
            List<AssemblyInfoBto> assemblyList = topicList.get(0).getAssemblyList();
            AssemblyInfoBto adInfo = assemblyList.get(0);
            if(adInfo!=null) {
                mAdInfoList = adInfo.getAppInfoList();
                autoChangeHandler();  //开始滚动广告栏

                new DownloadAdImgTask().execute(adInfo); //开始下载广告图片
            }
            adForStatistics();
        } catch (NullPointerException e) {
            
        } catch (IndexOutOfBoundsException e) {
            
        }
    }
    

//    public void startRequestRecomendHome() {
//        if(mHandler!=null) {
//            mHandler.sendEmptyMessage(UPDATE_HOME_MSG);
//        }
//    }
    @Override
    public void handleScrollState(boolean scrolling) {
        mViewPagerScrolling = scrolling;    
    }
    
    /**
     * 下载单张广告图片的方法
     * @param adInfo 
     * @return
     */
    private Bitmap downloadAdImages(AppInfoBto adInfo) {
        String fileName = null;
        Bitmap bitmap=null;
        String sdPath = MarketUtils.FileManage.getSDPath();
        String filePath;

        String url =  adInfo.getImgUrl();

        String key = MarketUtils.getImgUrlKey(url);
        if(key != null) {
            fileName = "HTTP_AD_" + key.hashCode(); 
        } else {
            return null;
        }

        bitmap = BitmapUtiles.convertFileToBitmap(fileName);
        if(bitmap == null) {
            if(!TextUtils.isEmpty(sdPath)) {
                filePath = sdPath+ Constant.download_path+"download/cache/image/" + fileName;
                File file = new File(filePath);
                if(file.exists()) {
                    file.delete();
                }
            }  
            try {
                InputStream inputStream = new URL(url).openStream();
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565; 
                opt.inPurgeable = true;          
                opt.inInputShareable = true; 
                bitmap = BitmapFactory.decodeStream(inputStream,null,opt);
                inputStream.close();
                if(bitmap!=null) {
                    BitmapUtiles.saveBitmapToFile(fileName, bitmap);
                }
            } catch(OutOfMemoryError e) {
                System.gc();
                bitmap = null;
                e.printStackTrace();
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        }
        return bitmap;
    }
    
    
    class DownloadAdImgTask extends AsyncTask<AssemblyInfoBto, Object, Void> {
        protected Void doInBackground(AssemblyInfoBto... params) {
            AssemblyInfoBto advInfo =  params[0];
            List<AppInfoBto> adInfoList = advInfo.getAppInfoList();
            int adNum = advInfo.getAppInfoListSize();
            if(adInfoList==null)
                 return null;
            
            for(int i=0;i<adNum;i++) {
                Bitmap bitmap = downloadAdImages(adInfoList.get(i));
                publishProgress(bitmap,i);
            }
            
            return null;
        } 
        
        @Override
        protected void onProgressUpdate(Object... values) {
            Bitmap bitmap = (Bitmap)values[0];
            int position = (Integer)values[1];
            BitmapDrawable drawable;
            if(bitmap!=null) {
                drawable = new BitmapDrawable(bitmap);
                mADImage[position].setBackgroundDrawable(drawable);   
//               bitmap.recycle();
                mAdImgStates[position] = AD_DOWN_SUCCESSED;
            } else {
                mAdImgStates[position] = AD_DOWN_FAILED;
            } 
        }
    }
    
    
    
    public interface GiftTipCallBack {
        public void tellSplashShowPage(int page);
    }
    
    
    /**
     * 是否在首页:true=在首页 false=切换到其它页面
     * @param show
     */
    public void setHomeShow(boolean show) {
        isHomeShow = show;
    }
    
    
    /**
     * 统计
     */
    public void adForStatistics() {
        AppInfoBto info = null;
        int position = mCurrentPosition % AD_IMAGE_COUNT;
        if (mAdInfoList != null && mAdInfoList.size() > 0 && position < mAdInfoList.size()) {
            info = mAdInfoList.get(position);
        }
        adForStatistics(info, true, !mSpecialSuspensionButtonsShow, true);
    }
    
    
    /**
     * 广告栏位数据统计
     * @param info 当前广告栏信息
     * @param scrollAuto 是否是自动滑:true=自动滑 false=手动滑
     * @param adShow 首页列表广告栏是否能看到:true=广告栏能看到 false=广告栏看不到
     * @param homeShow 是否在首页:true=在首页 false=切换到其它页面
     */
    private void adForStatistics(AppInfoBto info, boolean scrollAuto, boolean adShow, boolean homeShow) {
        if (info == null || !homeShow || !adShow) return;
        
        String logKey = null;
        if (scrollAuto) {
            logKey = LogDefined.COUNT_HOME_AD;
        } else {
            logKey = LogDefined.VIEW_HOME_AD;
        }
        
        String adLogDes = null;
        String webUrl = info.getWebUrl();
        if (!TextUtils.isEmpty(webUrl)) {
            adLogDes = UserLogSDK.getAdWebDetailDes(logKey, info.getName());
            
        } else {
            if (info.getResType() == 2) {
                adLogDes = UserLogSDK.getAdSpecialDetailDes(logKey, Integer.toString(info.getRefId()), info.getName());
                
            } else if (info.getResType() == 1) {
                adLogDes = UserLogSDK.getAdApkDetailDes(logKey, Integer.toString(info.getRefId()), info.getPackageName(), info.getName());
                
            }
        }
        
        if (adLogDes == null) {
            return;
        }
        if (scrollAuto) {
            UserLogSDK.logCountEvent(mContext, adLogDes);
            
        } else {
            UserLogSDK.logViewShowEvent(mContext, adLogDes);
            
        }
    }
    
}
