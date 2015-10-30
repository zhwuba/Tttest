package com.market.view;

import java.io.File;
import java.util.HashMap;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.download.userDownload.DownloadManager;
import com.market.net.MessageCode;
import com.market.net.SenderDataProvider;
import com.market.net.data.AppInfoBto;
import com.market.net.response.GetStartPageResp;
import com.market.net.utils.StartNetReqUtils;
import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.appResident.MarketApplication;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.FrameInfoCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.gallery.AsyncImageLoader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class StartUpLayout extends RelativeLayout
{

	private final static int HANDLER_STARTUP_IMAGE = 0;
	private BitmapDrawable startupDrawable;
	private FrameLayout flBackground;
	private Context mContext = null;

	private Handler mHandler = new Handler()
	{
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case HANDLER_STARTUP_IMAGE:
					HashMap<String, Object> map = (HashMap<String, Object>) msg.obj;
					if (map == null)
						break;

					final GetStartPageResp startUpAdInfo = (GetStartPageResp) map.get("startUpAdInfo");
					if (startUpAdInfo == null)
						break;

					new Thread()
					{
						@Override
						public void run()
						{
							onHandlerStartupImage(MarketApplication.getRootContext(), startUpAdInfo);
						}
					}.start();
					break;
			}
		}
	};

	public StartUpLayout(Context context)
	{
		this(context, null);
	}

	public StartUpLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public StartUpLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context)
	{
		mContext = context;
	}

	private String mAdLogDes = null;
	
	public void initStartupLayout()
	{
		LayoutInflater tLayoutInflater = LayoutInflater.from(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		View mMainView = tLayoutInflater.inflate(R.layout.main_welcome, null);
		flBackground = (FrameLayout) mMainView.findViewById(R.id.flparent);
		TextView tvVersion = (TextView) mMainView.findViewById(R.id.version);
		LinearLayout llCopyRight = (LinearLayout) mMainView.findViewById(R.id.copyright);
		LinearLayout llloading = (LinearLayout) mMainView.findViewById(R.id.loading);

		startupDrawable = (BitmapDrawable) AsyncImageLoader.getStartupImageDrawable(mContext.getApplicationContext());

		if (startupDrawable == null)
		{
			Bitmap bm = null;
			try {
				bm = MarketUtils.read565Bitmap(mContext.getApplicationContext(), R.drawable.startup_bg);
				startupDrawable = new BitmapDrawable(this.getResources(), bm);
				flBackground.setBackgroundDrawable(startupDrawable);
			} catch (Exception e) {
				e.printStackTrace();
			}
			bm = null;
		}
		else
		{
			llloading.setVisibility(View.INVISIBLE);
			tvVersion.setVisibility(View.INVISIBLE);
			llCopyRight.setVisibility(View.INVISIBLE);
			flBackground.setBackgroundDrawable(startupDrawable);
			
			DownloadManager.startServiceReportOffLineLog(mContext, ReportFlag.ACTION_VIEW_COLUMN, ReportFlag.FROM_ENTRY_AD);
			mAdLogDes = getAdLogDes();
	        if (mAdLogDes != null) {
	            UserLogSDK.logViewShowEvent(MarketApplication.getRootContext(), mAdLogDes);
	        }
		}

		tvVersion.setText(getVersionText());

		// if (!mSystemDisplay.contains("koobee") || startupDrawable != null) // 21 floor
		// mCopyRight.setVisibility(View.INVISIBLE);
		// 加载页，不再显示版权信息
		llCopyRight.setVisibility(View.INVISIBLE);

		this.addView(mMainView, params);
	}

	/**
	 * 获取版本信息
	 * 
	 * @return
	 */
	public String getVersionText()
	{
		String versionName = null;
		int version = -1;
		try
		{
			// Get the package info
			PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			versionName = pi.versionName;
			version = pi.versionCode;
		}
		catch (Exception e)
		{
			Log.e("VersionInfo", "Exception", e);
		}

		if (versionName != null && version != -1)
		{
			return ("V" + versionName + "_" + version);
		}
		return "";
	}

	/**
	 * 释放资源
	 */
	public void releaseRes()
	{
		try
		{
			startupDrawable.setCallback(null);
			startupDrawable.getBitmap().recycle();
			flBackground.setBackgroundDrawable(null);
			this.removeAllViews();
		}
		catch (Exception e)
		{

		}
		if (mHandler != null)
		{
			if (mHandler.hasMessages(HANDLER_STARTUP_IMAGE))
			{
				mHandler.removeMessages(HANDLER_STARTUP_IMAGE);
				mHandler = null;
			}
		}
		mContext = null;
		flBackground = null;
		startupDrawable = null;
	}

	/**
	 * 获取加载页广告
	 * 
	 * @param context
	 * @param startUpAdinfo
	 */
	public static void onHandlerStartupImage(Context context, GetStartPageResp startUpAdinfo)
	{
		String imageUrl = "";
		String prevImageUrl = "";
		if (context == null || startUpAdinfo.getImgUrl() == null)
		{
			return;
		}
		imageUrl = startUpAdinfo.getImgUrl().toString();
		prevImageUrl = MarketUtils.getUpdateStartupImageFileName(context);

		Log.e("hyn", "imageUrl" + imageUrl + "\n prevImageUrl" + prevImageUrl);
		if (TextUtils.isEmpty(imageUrl))
			return;

		String filePath = Environment.getExternalStorageDirectory().toString() + Constant.download_cache_dir
				+ prevImageUrl.hashCode();

		File file = new File(filePath);

		if (file != null && file.exists())
		{
			if (filterImgUrl(imageUrl).equals(filterImgUrl(prevImageUrl)))
				return;
		}

		if (AsyncImageLoader.mIsDownloadStartupImage)
			return;
		if (AsyncImageLoader.downloadImgToFileSys(imageUrl, "" + imageUrl.hashCode()))
		{
			MarketUtils.setNextUpdateTime(context, MarketUtils.KEY_GET_IMAGE_NEXT, System.currentTimeMillis());
			if (!TextUtils.isEmpty(prevImageUrl))
			{
				if (file != null && file.exists() && !imageUrl.equals(prevImageUrl))
					file.delete();
			}
			MarketUtils.setUpdateStartupImageFileName(context, imageUrl);
			FrameInfoCache.saveFrameInfoToStorage(startUpAdinfo, "startUpAdInfo");
		}
	}

	/**
	 * 获取加载页广告标识
	 * 
	 * @param imageUrl
	 * @return
	 */
	public static String filterImgUrl(String imageUrl)
	{
		if (!TextUtils.isEmpty(imageUrl))
		{
			int index = imageUrl.indexOf("img");
			/** 每次下发的图片地址可能不一样，截取url相同的部分作为key **/
			if (index != -1)
			{
				imageUrl = imageUrl.substring(index);
			}
		}
		return imageUrl;
	}

	/**
	 * 联网获取加载页广告
	 */
	public void updateStartUpImage()
	{
		long nextUpdateTime = MarketUtils.getNextUpdateTime(mContext.getApplicationContext(),
				MarketUtils.KEY_GET_IMAGE_NEXT);
		if (System.currentTimeMillis() >= nextUpdateTime)
		{
			String contens = SenderDataProvider.buildToJSONData(mContext.getApplicationContext(),
					MessageCode.GET_STARET_PAGE, null);
			StartNetReqUtils.execMarketRequest(mHandler, HANDLER_STARTUP_IMAGE, MessageCode.GET_STARET_PAGE, contens);
		}
	}

	
	private String getAdLogDes() {
        GetStartPageResp startUpAdInfo = (GetStartPageResp) FrameInfoCache.getFrameInfoFromStorage("startUpAdInfo");
        if (startUpAdInfo == null) {
            return null;
        }
        
        String adLogDes = null;
        AppInfoBto appInfo = startUpAdInfo.getAppInfoBto();
        String webUrl = appInfo.getWebUrl();
        if (!TextUtils.isEmpty(webUrl)) {
            adLogDes = UserLogSDK.getAdWebDetailDes(LogDefined.VIEW_ENTRY_AD, appInfo.getName());
            
        } else {
            if (appInfo.getResType() == 2) {
                adLogDes = UserLogSDK.getAdSpecialDetailDes(LogDefined.VIEW_ENTRY_AD, Integer.toString(appInfo.getRefId()), appInfo.getName());
                
            } else if (appInfo.getResType() == 1) {
                adLogDes = UserLogSDK.getAdApkDetailDes(LogDefined.VIEW_ENTRY_AD, Integer.toString(appInfo.getRefId()), appInfo.getPackageName(), appInfo.getName());
                
            }
        }
        
        return adLogDes;
    }
}
