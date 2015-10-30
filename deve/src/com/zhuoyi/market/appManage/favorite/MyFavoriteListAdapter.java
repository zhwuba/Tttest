package com.zhuoyi.market.appManage.favorite;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.market.statistics.ReportFlag;
import com.zhuoyi.market.R;
import com.zhuoyi.market.WebActivity;
import com.zhuoyi.market.appManage.db.FavoriteInfo;
import com.zhuoyi.market.appManage.db.WebAppDao;
import com.zhuoyi.market.commonInterface.DownloadCallBackInterface;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.AppOperatorUtils;
import com.zhuoyi.market.utils.MarketUtils;

public class MyFavoriteListAdapter extends BaseAdapter implements OnItemClickListener{

	private Context mContext;
	private List<FavoriteInfo> mFavoritelist;
	private boolean mRefreshIcon = true;
	private AsyncImageCache mAsyncImageCache;
	private WeakReference<DownloadCallBackInterface> mDownloadCallBack;
	
	private View mFavoriteFloatView;
	private View mFavoriteHideView;
	private int mStatusBarHeight;
	
	public MyFavoriteListAdapter(Context context,  DownloadCallBackInterface callback,
			List<FavoriteInfo> allFavoriteInfo) {
		mContext = context;
		mDownloadCallBack = new WeakReference<DownloadCallBackInterface>(callback);
		mFavoritelist = allFavoriteInfo;
		mAsyncImageCache = AsyncImageCache.from(mContext);
	}

	
	@Override
	public int getCount() {
		return mFavoritelist.size();
	}

	
	@Override
	public Object getItem(int position) {
		try {
		return mFavoritelist.get(position);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	
	@Override
	public long getItemId(int position) {
		return position;
	}

	
	public void setListData(List<FavoriteInfo> allFavoriteInfo) {
		this.mFavoritelist = allFavoriteInfo;
	}
	
	
	public void allowRefreshIcon(boolean status) {
		mRefreshIcon = status;
	}
	
	
	public boolean isAllowRefreshIcon() {
		return mRefreshIcon;
	}

	
	static class ViewHolder {
		TextView 	mAppName;
		ImageView 	mIcon;
		TextView 	mAppSize;
		TextView   mImageDownloadButton;
	}
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder ;
		String packageName = "";
		String versionCode = "";
		if(convertView==null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.favorite_list_item, parent,false);
			holder = new ViewHolder(); 			
			holder.mIcon = (ImageView) convertView.findViewById(R.id.list_icon); 
			holder.mAppName = (TextView) convertView.findViewById(R.id.list_app_name);
			holder.mAppSize = (TextView) convertView.findViewById(R.id.list_app_size);
			holder.mImageDownloadButton = (TextView) convertView.findViewById(R.id.install_button);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.mImageDownloadButton.setVisibility(View.VISIBLE);
		final FavoriteInfo favoriteInfo = mFavoritelist.get(position);
		holder.mAppName.setText(favoriteInfo.getAppName());
		holder.mAppSize.setText(favoriteInfo.getFileSizeSum());
		
		packageName = favoriteInfo.getAppPackageName();
		versionCode = favoriteInfo.getVersionCode();
				
		mAsyncImageCache.displayImage(mRefreshIcon,holder.mIcon, R.drawable.picture_bg1_big,
                new AsyncImageCache.NetworkImageGenerator(packageName,favoriteInfo.getIconUrl()), true);

		AppOperatorUtils.initBtnState(mContext, holder.mImageDownloadButton, packageName, Integer.parseInt(versionCode), holder.mIcon);
        holder.mImageDownloadButton.setOnClickListener(new AppOperatorUtils.CommonAppClick(mContext, favoriteInfo,
            mDownloadCallBack, ReportFlag.TOPIC_NULL, ReportFlag.FROM_FAVORITE, true));
		
		/*holder.mImageDownloadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String packageName = "";
				String appName = "";
				String filePath = "";
				String url = "";
				File apkFile;
				String md5 = "";
				int appId = 0;
				int verCode = 0;
				
				if(favoriteInfo==null)
					return;
				
				appId = favoriteInfo.getAppId();
				packageName = favoriteInfo.getAppPackageName();
				appName = favoriteInfo.getAppName();
				filePath = favoriteInfo.getLocalFilePath();
				md5 = favoriteInfo.getMd5();
				url = favoriteInfo.getUrl();
				apkFile = new File(filePath);
				if(v.getTag().toString().equals("downloading")){
	            	AppOperatorUtils.downBasePause(mContext,mDownloadCallBack, v, packageName, Integer.parseInt(favoriteInfo.getVersionCode()));
	                return;
	            } else if(v.getTag().toString().equals("open")) {
				    MarketUtils.openCurrentActivity(mContext, favoriteInfo.getAppPackageName());
					return;
				} else if (v.getTag().toString().equals("installing")) {
					return;
				}
				verCode = Integer.valueOf(favoriteInfo.getVersionCode());
				if(apkFile.exists()) {
					
					if (verCode <= MarketUtils.getApkFileVersionCode(mContext, filePath)) {
						MarketUtils.AppInfoManager.AppInstall(filePath, mContext, packageName, appName);
						return;
					} else
						apkFile.delete();							
				}
				
				if(MarketUtils.getAPNType(mContext) == -1) {
					Toast.makeText(mContext, mContext.getString(R.string.no_network), Toast.LENGTH_SHORT).show();
					return;
				}
				else {
					if(mDownloadCallBack != null && mDownloadCallBack.get() != null){
						mDownloadCallBack.get().startDownloadApp(
								packageName, 
								appName,
								filePath,
								md5, 
								url, 
								ReportFlag.TOPIC_NULL,
								ReportFlag.FROM_FAVORITE, 
								verCode, appId, 0);
						
						if (v instanceof TextView) {
                            TextView tV = (TextView) v;
                            tV.setBackgroundResource(R.drawable.common_installing_btn);
                            tV.setTextColor(mContext.getResources().getColor(R.color.common_app_installing_color));
                            tV.setText(R.string.down_noti_downloading_title);
                            tV.setTag("downloading");
                        }
					}
				}
			}
			
		});*/
		holder.mIcon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (favoriteInfo != null) {
                	int appId = favoriteInfo.getAppId();
                    WebAppDao webAppDao = new WebAppDao(mContext);
                    String webUrl = webAppDao.getWebUrl(appId);
                    if(!TextUtils.isEmpty(webUrl)){
                    	Intent localIntent = new Intent();
                    	localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    	localIntent.setClass(mContext, WebActivity.class);
                    	localIntent.putExtra("wbUrl", webUrl);
                    	localIntent.putExtra("from_path", ReportFlag.FROM_FAVORITE);
                    	localIntent.putExtra("titleName", mContext.getString(R.string.app_detail_name));
                    	v.getContext().startActivity(localIntent);
                    }else{
                    	MarketUtils.startDetailActivity( v.getContext(), appId, ReportFlag.FROM_FAVORITE, -1, null);
                    }
                }
			}
		});
		return convertView;
	}

	
	
	public int getInstalledApkVersionCode(String pName) {
		int versionCode = 0;
		try {
			PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(pName, PackageManager.GET_CONFIGURATIONS);
			versionCode = pinfo.versionCode;
		} catch (NameNotFoundException e) {

		}
		return versionCode;
	}

	
	public int getApkFileVersionCode(String apk_path) {
		PackageManager pm = mContext.getPackageManager();  

		PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path, 0);  
		if(packageInfo == null)
			return 0;
		return packageInfo.versionCode;
	}	

	
	public boolean isEqualsVersionCode(String packageName,String path) {
		boolean result = false;

		int versionCode2 = getInstalledApkVersionCode(packageName);
		int versionCode = getApkFileVersionCode(path);

		if (versionCode2 == versionCode) {
			result = true;
		}

		return result;
	}

	
	public byte[] createBitByteArray(Bitmap bitmap) {
		if (null == bitmap)
			return null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
		return os.toByteArray();
	}

	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

        int titleHeight = mContext.getResources().getDimensionPixelSize(R.dimen.title_heigh);
        int parentHeight = mContext.getResources().getDimensionPixelSize(R.dimen.dip10);

        int[] location = new int[2];
        v.getLocationInWindow(location);

        int needMargin = location[1] + parentHeight - titleHeight - mStatusBarHeight;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mFavoriteHideView.getLayoutParams();
        params.setMargins(params.rightMargin, needMargin, params.rightMargin, params.bottomMargin);
        mFavoriteHideView.setLayoutParams(params);
        if(mFavoriteFloatView.getVisibility() == View.GONE){
            mFavoriteFloatView.setVisibility(View.VISIBLE);
        } 
	}
	
}
