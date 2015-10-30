package com.zhuoyi.market;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.market.account.login.BaseActivity_Html5;
import com.market.account.utils.EncoderAndDecoder;
import com.market.view.AnimLayout;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.mm.sdk.platformtools.Util;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.wxapi.ShareToWeibo;
@SuppressWarnings("static-access")
@TargetApi(11)
public class ShareAppActivity extends Activity{

	public static final String INTENT_KEY_SHARE_URL = "shareUrl";
	public static final String INTENT_KEY_APP_NAME = "appName";
	public static final String INTENT_KEY_ICON_URL = "iconUrl";
	public static final String INTENT_KEY_BITMAP = "bitmap";
	public static final String INTENT_KEY_BITMAP_LOCAL_PATH = "bitmap_local_path";
	public static final String INTENT_KEY_FROM_CHECK = "from_check";
	public static final String INTENT_KEY_FROM_MINE = "from_mine";
	public static final String INTENT_KEY_FROM_WALLPAPER = "from_wallpaper";
	
    private static final String WeChat_APP_ID = "wxa25e8139f70d5e44";
    private static final String MARKET_ICON_URL = "http://newmarket.kk874.com/logo/droi_market.png";
	private GridView gridView;
	private ShareAppsAdapter adapter;
	private String mShareStr = null;
	private String mShareUrl = null;
	private String mIconUrl = null;
	private String mBitmapLocalPath = null;
	private String mAppName;
	private boolean mFromCheck = false; //是否分享应用体检
	private boolean mFromMine = false;
	private boolean mFromWallpaper = false;
	private Bitmap mThumb;
	private String iconPath = Environment.getExternalStorageDirectory().toString()
            + Constant.download_cache_dir + "market_check_screenshot.png";
	
	private Tencent mTencent;
	private IWXAPI mWXApi;
	private boolean mIsQQInstalled = false;
	private boolean mIsWeiXinInstalled = false;
	private ShareToWeibo mShareToWeibo;
	
	private byte [] mImageBis; //详情页面传递过来的图片字节组
	private boolean mIsNeedRecyle = true;
	private boolean mIsShareWeibo = false;
	
	private AnimLayout mAnimLayout = null;
	
	/**
	 * <code>mShareCall</code> - {微信分享回调用返回activity时关闭activity}.
	 */
	private BroadcastReceiver mShareCall = new BroadcastReceiver(){

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            finish();
        }
	    
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		mFromWallpaper = intent.getBooleanExtra(INTENT_KEY_FROM_WALLPAPER, false);
		if (mFromWallpaper) {
		    getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , 
		            WindowManager.LayoutParams. FLAG_FULLSCREEN);
		    
		    mBitmapLocalPath = intent.getStringExtra(INTENT_KEY_BITMAP_LOCAL_PATH);
		}
		
		setContentView(R.layout.layout_share_dialog);
		setDialogParam();

		mFromCheck = intent.getBooleanExtra(INTENT_KEY_FROM_CHECK, false);
		mFromMine = intent.getBooleanExtra(INTENT_KEY_FROM_MINE, false);
		trimStringData(intent);
		gridView = (GridView)findViewById(R.id.share_app_grid);
		
		if(mFromMine) {
			((TextView) findViewById(R.id.share_qr_text)).setVisibility(View.VISIBLE);
			((ImageView)findViewById(R.id.share_qr_img)).setVisibility(View.VISIBLE);
		}
		if (mAppName == null) {
		    mAppName = getString(R.string.market_about);
		}
		if(mShareUrl == null) { //分享市场+应用体检 //体检换mShareStr
			mShareUrl = getString(R.string.share_market_url) + Constant.td;
			if (mFromCheck) { //分享应用体检
			    mShareStr = getString(R.string.check_activity_share_str) + " " + mShareUrl + " " + getString(R.string.come_from) +getAppName() + ")";
            } else { //分享市场
                mShareStr = getString(R.string.share_market) + " " + mShareUrl + " " + getString(R.string.come_from) +getAppName() + ")";
            }
		} else if (mFromWallpaper) {//分享壁纸
			mShareStr = getString(R.string.wallpaper_share_str) + " " + mShareUrl + " " + getString(R.string.come_from) +getAppName() + ")";
		} else { //分享应用
			mShareStr = getString(R.string.share_other_app) + " ["+ mAppName +"] " + mShareUrl; 
		}
		mImageBis = intent.getByteArrayExtra(INTENT_KEY_BITMAP);  
		if (mImageBis != null) { //应用详情页获取，如果详情页图标已加载完成，则直接使用，否则需重新下载图标
			mThumb = BitmapFactory.decodeByteArray(mImageBis, 0, mImageBis.length);  
		} else if (mFromWallpaper && !TextUtils.isEmpty(mBitmapLocalPath)) {
			mThumb = getBitmapFromLocal(mBitmapLocalPath);
			if (mThumb == null) {
				mThumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
			}
		}else {
			mBitmapLocalPath = MARKET_ICON_URL;
			mThumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
		}
		
		getShareableAppsInfo();
		adapter = new ShareAppsAdapter();
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new MyOnItemClickListener());
		
		if (mTencent == null) {
	        mTencent = Tencent.createInstance("1101189560", this);
	    }
		if (mWXApi == null) {
			mWXApi= WXAPIFactory.createWXAPI(this, WeChat_APP_ID, false);
			mWXApi.registerApp(WeChat_APP_ID);
		}
		registerReceiver(mShareCall, new IntentFilter(
            BaseActivity_Html5.SHARE_CALLBACK));
		
		mAnimLayout = (AnimLayout) findViewById(R.id.anim_layout);
		mAnimLayout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (mAnimLayout != null && mAnimLayout.getAnimFinish()) {
                    mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_out, true);
                    mAnimLayout.setCloseActivity(ShareAppActivity.this);
                }
                return true;
            }
		    
		});
		mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_in, true);
		mAnimLayout.setCloseActivity(null);
	}
	
	private void trimStringData(Intent intent){
	    String shareUrl = (String)intent.getExtras().get(INTENT_KEY_SHARE_URL);
        String appName = (String)intent.getExtras().get(INTENT_KEY_APP_NAME);
        String iconUrl = (String)intent.getExtras().get(INTENT_KEY_ICON_URL);
        if (shareUrl != null) {
            mShareStr = shareUrl.trim();
        }
        if (appName != null) {
            mAppName = appName.trim();
        }
        if (iconUrl != null) {
            mIconUrl = iconUrl.trim();
        }
	}

	/** 设置dialog宽度满屏，居底部显示 */
	private void setDialogParam() {
		Window window = getWindow();
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = LayoutParams.FILL_PARENT;
		layoutParams.height = layoutParams.FILL_PARENT;
		layoutParams.gravity = Gravity.BOTTOM;
		window.setAttributes(layoutParams);
	}
	
	private List<ResolveInfo> getShareApp(){
		Intent filterIntent = new Intent(Intent.ACTION_SEND, null);
		PackageManager pm = getApplication().getPackageManager();
		filterIntent.addCategory(Intent.CATEGORY_DEFAULT);
		filterIntent.setType("text/plain");
		List<ResolveInfo> resolveInfos = new ArrayList<ResolveInfo>();
		resolveInfos = pm.queryIntentActivities(filterIntent,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
		return resolveInfos;
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		if (!mIsShareWeibo) {
		    finish();
        }
	}
	
	
    @Override
    public void onBackPressed() {
        if (mAnimLayout != null && mAnimLayout.getAnimFinish()) {
            mAnimLayout.setAndStartAnimById(R.anim.fade_bottom_out, true);
            mAnimLayout.setCloseActivity(this);
        }
    }
    
	
	@Override
	protected void onDestroy() {

		super.onDestroy();
		if (mIsNeedRecyle) {
			if (mThumb != null) {
				mThumb.recycle();
				mThumb = null;
			}
		}
		
		if (mAnimLayout != null) {
		    mAnimLayout.releaseRes();
		    mAnimLayout = null;
		}
		if (mTencent != null) {
            mTencent.releaseResource();
        }
		unregisterReceiver(mShareCall);
	}
	
	private void getShareableAppsInfo() {
		List<ResolveInfo> resolveInfos = getShareApp();
		for(int i=0;i<resolveInfos.size();i++) {
			ResolveInfo resolveInfo = resolveInfos.get(i);
			String name = resolveInfo.activityInfo.name;
			if(name.equals("com.tencent.mm.ui.tools.ShareImgUI")) {
				mIsWeiXinInstalled = true;
			}
			if(name.equals("com.tencent.mobileqq.activity.JumpActivity")) {
				mIsQQInstalled = true;
			}
		}
	}
	
	/** 分享到QQ好友 */
	private void shareToQQFriend(){
		if (!mIsQQInstalled) {
			return;
		}
    	final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT); 
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, mAppName); 
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  mShareStr); 
	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  mShareUrl); 
	    if (mIconUrl != null) { //分享应用
	    	params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,mIconUrl);  
		} else if(mFromCheck){ //应用体检
		    String saveCheckBitmap = saveCheckBitmap(mThumb);
		    if (TextUtils.isEmpty(saveCheckBitmap)) { //缓存图片失败
		        Toast.makeText(ShareAppActivity.this,getString(R.string.save_bitmap_fail),Toast.LENGTH_SHORT).show();
                return;
            }
		    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,saveCheckBitmap);  
		} else if (mFromWallpaper) {
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mBitmapLocalPath);  
		} else { //分享市场
	        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,MARKET_ICON_URL);  
        }
	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  getResources().getString(R.string.market_about));
	    doShareToQQ(params);
    }
	
	private void doShareToQQ(final Bundle params) {
	    ThreadManager.getMainHandler().post(new Runnable() {

            @Override
            public void run() {
                if (null != mTencent) {
                    mTencent.shareToQQ(ShareAppActivity.this, params, new BaseUiListener()); 
                }
            }
        });
    }
	
	/** 分享到QQ空间 */
    private void shareToQQzone() {  
    	if (!mIsQQInstalled) {
			return;
		}
        final Bundle params = new Bundle();  
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);  
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mAppName);  
        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,  mShareStr);  
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,  mShareUrl); 
        ArrayList<String> imageUrls = new ArrayList<String>();  
        if (mIconUrl != null) { //分享应用
        	imageUrls.add(mIconUrl);  
        	params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);  
		} else if(mFromCheck){ //应用体检
		    String saveCheckBitmap = saveCheckBitmap(mThumb);
            if (TextUtils.isEmpty(saveCheckBitmap)) { //缓存图片失败
                Toast.makeText(ShareAppActivity.this,getString(R.string.save_bitmap_fail),Toast.LENGTH_SHORT).show();
                return;
            }
		    imageUrls.add(saveCheckBitmap); 
		    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);   
        } else if (mFromWallpaper) {
        	imageUrls.add(mBitmapLocalPath); 
		    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);   
        } else { //分享市场
			imageUrls.add(MARKET_ICON_URL);  
			params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);  
		}
        params.putInt(QzoneShare.SHARE_TO_QQ_EXT_INT,  QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);    
        mTencent.shareToQzone(ShareAppActivity.this, params, new BaseUiListener());  
    } 
    
    private class BaseUiListener implements IUiListener {  
        @Override  
        public void onComplete(Object response) {  
            doComplete((JSONObject)response);  
        }  
        protected void doComplete(JSONObject values) {  
        	Toast.makeText(ShareAppActivity.this,getString(R.string.share_success),Toast.LENGTH_SHORT).show();  
        }  
        @Override  
        public void onError(UiError e) {  
            Toast.makeText(ShareAppActivity.this,  
                getString(R.string.share_fail)+":"+e.errorMessage,Toast.LENGTH_SHORT).show();
        }  
        @Override  
        public void onCancel() {  
        	Toast.makeText(ShareAppActivity.this,getString(R.string.share_cancel),Toast.LENGTH_SHORT).show();  
        }  
    } 
    
    
    private Bitmap getBitmapFromLocal(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        try {
            return BitmapFactory.decodeFile(filePath);
        } catch (OutOfMemoryError e) {
        }
        return null;
    }
    
    
    /**
	 * {保存市场logo图片，设置里分享QQ时需要用到本地图片路径}
	 * @param bm
	 * @return
	 */
	public String saveCheckBitmap(Bitmap bm) {
		File p = new File(Environment.getExternalStorageDirectory().toString()+Constant.download_cache_dir);
		if (!p.exists()) {
			p.mkdirs();
		}
		File f = new File(iconPath);
		if (f.exists()) {
			return f.getPath();
		}
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
			return f.getPath();
		} catch (Exception e) {
			e.printStackTrace();
			System.gc();
		} 
		return "";
	}
    /** QQ分享---end */
    
    
    /** 微博分享 */
    private void shareToSinaBlog() {  
    	mShareToWeibo = new ShareToWeibo(this);
		if (mIconUrl == null || mImageBis != null || mFromWallpaper) {
			doShareWeiBo(mThumb);
		} else {
			if(Build.VERSION.SDK_INT >= 11) {
				new DownLoadImage(false,true).executeOnExecutor(MarketUtils.getDataReqExecutor());
			} else {
				new DownLoadImage(false,true).execute();
			}
		}
    } 
    
    
	private void doShareWeiBo(Bitmap bm){
	    mShareToWeibo.setmBitmap(bm);
        mShareToWeibo.setmDesc(mShareStr);
        mShareToWeibo.setmWebUrl(mShareUrl);
        mShareToWeibo.shareToSinaWeibo(ShareToWeibo.SHARE_ACHIV_TEXT);
    }
    /** 微博分享---end */
    
    
	/**
	 * {分享到微信}
	 * @param isFriend true：分享到朋友圈   false：分享到微信好友
	 */
	private void shareToWX(boolean isFriend) {
		if (!mIsWeiXinInstalled) {
			return;
		}
		if (mIconUrl == null || mImageBis != null || mFromWallpaper) {
			doShareWeChat(isFriend,mThumb);
		} else {
			if(Build.VERSION.SDK_INT >= 11) {
				new DownLoadImage(isFriend,false).executeOnExecutor(MarketUtils.getDataReqExecutor());
			} else {
				new DownLoadImage(isFriend,false).execute();
			}
		}
	}
	
	private void doShareWeChat(boolean isFriend,Bitmap bm){
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = getShareUrl();
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = isFriend?mShareStr:mAppName;
		msg.description = mShareStr;
		if (mFromCheck || mFromWallpaper) {
		    int scaleHeight = bm.getHeight() * 100 / bm.getWidth();
	        Bitmap thumbBmp = Bitmap.createScaledBitmap(bm, 100, scaleHeight, true);
	        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 缩略图
        }else {
            msg.setThumbImage(bm);
        }
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("webpage");
		req.message = msg;
		req.scene = isFriend?SendMessageToWX.Req.WXSceneTimeline:SendMessageToWX.Req.WXSceneSession;
		mWXApi.sendReq(req);
	}
	
	private class DownLoadImage extends AsyncTask<String, Integer,Bitmap> {
		
		private boolean isFriend;
		private boolean isWeiBo;
		
		public DownLoadImage(boolean isFriend,boolean isWeiBo) {
			this.isFriend = isFriend;
			this.isWeiBo = isWeiBo;
		}
		
        protected Bitmap doInBackground(String... urls) {
            Bitmap tmpBitmap = null;  
            try { 
            InputStream is = new java.net.URL(mIconUrl).openStream(); 
            tmpBitmap = BitmapFactory.decodeStream(is); 
            is.close();
            } catch (Exception e) { 
            e.printStackTrace(); 
            } 
            return tmpBitmap; 
            
        }
        protected void onPostExecute(Bitmap result) {
        	if (isWeiBo) {
				doShareWeiBo(result);
			} else {
				doShareWeChat(isFriend,result);
			}
        	/*if (result != null) {
				result.recycle();
			}*/
        }
    }
	
	//微信分享---end
	
	private String getAppName() {
		String appName = "";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			appName = pi.applicationInfo.loadLabel(getPackageManager()).toString();
			if (appName == null || appName.length() <= 0) {
				return "";
			}
		} catch(Exception e) {

		}
		return appName;
	}
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	class ShareAppsAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		
		public ShareAppsAdapter() {
			inflater = LayoutInflater.from(ShareAppActivity.this);
		}
		
		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.layout_share_item, null);
			ImageView appIcon = (ImageView) convertView.findViewById(R.id.share_app_icon);
			TextView appName = (TextView) convertView.findViewById(R.id.share_app_name);
			showSysShareApps(appIcon,appName,position);
			return convertView;
		}
		
		private void showSysShareApps(ImageView appIcon,TextView appName,int position) {
			switch (position) {
			case 0:
				appName.setText(R.string.weixin);
				if (mIsWeiXinInstalled) {
					appIcon.setImageResource(R.drawable.share_wx_light);
				}else {
					appIcon.setImageResource(R.drawable.share_wx_gray);
				}
				break;
			case 1:
				appName.setText(R.string.time_line);
				if (mIsWeiXinInstalled) {
					appIcon.setImageResource(R.drawable.share_circle_light);
				}else {
					appIcon.setImageResource(R.drawable.share_circle_gray);
				}
				break;
			case 2:
				appName.setText(R.string.qq);
				if (mIsQQInstalled) {
					appIcon.setImageResource(R.drawable.share_qq_light);
				}else {
					appIcon.setImageResource(R.drawable.share_qq_gray);
				}
				break;
			case 3:
				appName.setText(R.string.qzone);
				if (mIsQQInstalled) {
					appIcon.setImageResource(R.drawable.share_qzone_light);
				}else {
					appIcon.setImageResource(R.drawable.share_qzone_gray);
				}
				break;
			case 4:
				appName.setText(R.string.sina_weibo);
				appIcon.setImageResource(R.drawable.share_weibo_light);
				break;
			default:
				break;
			}
		}
		
	}
	class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
		    if (mAnimLayout == null || !mAnimLayout.getAnimFinish()) {
                return;
            }
			mIsNeedRecyle = false;
			mIsShareWeibo = false;
			switch (position) {
			case 0://微信好友
				shareToWX(false);
				break;
			case 1: //朋友圈
				shareToWX(true);
				break;
			case 2://QQ好友
				shareToQQFriend();
				break;
			case 3://QQ空间
				shareToQQzone();
				break;
			case 4:
				mIsShareWeibo = true;
				shareToSinaBlog();
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 分享到微信的网页
	 * @param url
	 * @return
	 * @throws JSONException
	 */
	private String getShareUrl() {
		String shareUrl = Constant.MARKET_APP_SHARE_URL;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("downloadUrl", mShareUrl);
			jsonObject.put("appName", mAppName);
			jsonObject.put("iconUrl", mIconUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		shareUrl += ("?share_info=" + EncoderAndDecoder.encrypt(jsonObject.toString()));
		return shareUrl;
	}
	
}