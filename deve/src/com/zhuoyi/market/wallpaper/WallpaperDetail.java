package com.zhuoyi.market.wallpaper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.market.behaviorLog.UserLogSDK;
import com.market.view.CommonLoadingManager;
import com.market.view.LoadingProgressDialog;
import com.zhuoyi.market.R;
import com.zhuoyi.market.ShareAppActivity;
import com.zhuoyi.market.commonInterface.ImageLoadedCallBack;
import com.zhuoyi.market.constant.Constant;
import com.zhuoyi.market.utils.AsyncImageCache;
import com.zhuoyi.market.utils.MarketUtils;
import com.zhuoyi.market.utils.AsyncImageCache.MD5;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WallpaperDetail extends Activity implements OnClickListener, ImageLoadedCallBack, AnimationListener {
    
    private Context mContext = null;

    private ImageView mBack = null;
    private ImageView mDownload = null;
    private TextView mSetting = null;
    private ImageView mShare = null;
    
    private FrameLayout mBackground = null;
    private LinearLayout mTitle = null;
    private LinearLayout mBar = null;
    
    private String mDownloadUrl = null;
    private String mDownloadName = null;
    private int mDownloadId = 0;
    private int[] mDisplay = {0, 0};
    
    private String mPicTag = null;
    private String mPicLocalPath = null;
    private Bitmap mPicBitmap = null;
    
    private AsyncImageCache mAsyncImageCache = null;
    
    private LoadingProgressDialog mLoadingProgressDialog = null;
    
    private boolean mUserDownload = false;
    private boolean mUserDownloadSuccess = false;
    private boolean mBackgroundFinish = false;
    private boolean mAnimFinish = true;
    
    private static final int MSG_DISMISS = 1;
    private static final int MSG_DOWNLOAD_TIP = 2;
    private static final int MSG_SETTING_FAIL_TIP = 3;
    private static final int MSG_SETTING_SUCCESS_TIP = 4;
    private static final int MSG_DOWNLOAD_START_TIP = 5;
    private static final int MSG_DOWNLOAD_SUCCESS_TIP = 6;
    private static final int MSG_DOWNLOADING_TIP = 7;
    private static final int MSG_SHARE_TIP = 8;
    private static final int MSG_DOWNLOAD_FAIL_TIP = 9;
    private static final int MSG_NET_TIP = 10;
    private Handler mHandler = new Handler() {
        
        @Override
        public void handleMessage(Message message) {
           switch (message.what) {
           case MSG_DISMISS:
               if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing())
                   mLoadingProgressDialog.dismiss();
               break;
           case MSG_DOWNLOAD_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_setting_download, Toast.LENGTH_SHORT).show();
               break;
           case MSG_SETTING_FAIL_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_setting_fail, Toast.LENGTH_SHORT).show();
               if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing())
                   mLoadingProgressDialog.dismiss();
               break;
           case MSG_SETTING_SUCCESS_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_setting_success, Toast.LENGTH_SHORT).show();
               if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing())
                   mLoadingProgressDialog.dismiss();
               break;
           case MSG_DOWNLOAD_START_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_download, Toast.LENGTH_SHORT).show();
               break;
           case MSG_DOWNLOAD_SUCCESS_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_download_success, Toast.LENGTH_SHORT).show();
               mUserDownloadSuccess = true;
               break;
           case MSG_DOWNLOADING_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_downloading, Toast.LENGTH_SHORT).show();
               break;
           case MSG_SHARE_TIP:
               Toast.makeText(WallpaperDetail.this, R.string.wallpaper_share_download, Toast.LENGTH_SHORT).show();
               if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing())
                   mLoadingProgressDialog.dismiss();
               break;
           case MSG_DOWNLOAD_FAIL_TIP:
        	   Toast.makeText(WallpaperDetail.this, R.string.wallpaper_download_fail, Toast.LENGTH_SHORT).show();
        	   break;
           case MSG_NET_TIP:
        	   Toast.makeText(WallpaperDetail.this, R.string.reserve_4, Toast.LENGTH_SHORT).show();
        	   break;
           }
        }
    };
    
    
    private String mLogDes;
    private String mDownLogDes;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_detail);
        
        mContext = this.getApplicationContext();
        mAsyncImageCache = AsyncImageCache.from(mContext);
        mAsyncImageCache.setImageLoadedCallBack("wallpaper", this);
        mLoadingProgressDialog = new LoadingProgressDialog(this);
        mLoadingProgressDialog.setIndeterminate(true);
        mLoadingProgressDialog.setCancelable(false);
        
        mBack = (ImageView) findViewById(R.id.wallpaper_detail_back);
        mBack.setOnClickListener(this);
        mDownload = (ImageView) findViewById(R.id.wallpaper_detail_download);
        mDownload.setOnClickListener(this);
        mSetting = (TextView) findViewById(R.id.wallpaper_detail_setting);
        mSetting.setOnClickListener(this);
        mShare = (ImageView) findViewById(R.id.wallpaper_detail_share);
        mShare.setOnClickListener(this);
        
        mBackground = (FrameLayout) findViewById(R.id.wallpaper_detail_background);
        mBackground.setOnClickListener(this);
        mTitle = (LinearLayout) findViewById(R.id.title);
        mBar = (LinearLayout) findViewById(R.id.bar);
        
        mDownloadUrl = this.getIntent().getStringExtra("download_url"); 
        mDownloadName = this.getIntent().getStringExtra("download_name");
        mDownloadId = this.getIntent().getIntExtra("download_id", 0);
        
        if (TextUtils.isEmpty(mDownloadName)) {
        	if (0 != mDownloadId) {
        		mDownloadName = mDownloadId + ".jpg";
        	} else {
        		mDownloadName = MD5.Md5(mDownloadUrl) + ".jpg";
        	}
        }
        
        mDisplay = getWindowDisplay();
        setBackgroundImage(mDownloadUrl, false);
        
        mPicTag = mDownloadUrl ;
        mPicLocalPath = getWallPaperPath(mPicTag);
        
        mLogDes = UserLogSDK.getWallPaperDetailViewDes(mDownloadName, mDownloadId);
        mDownLogDes = UserLogSDK.getWallPaperDownDes(mDownloadName, mDownloadId);
    }
    
    
	@Override
	protected void onResume() {
		CommonLoadingManager.get().showLoadingAnimation(this);
		super.onResume();
		
		UserLogSDK.logViewShowEvent(this, mLogDes);
	}
    
    
    private void setTitleAndBarVisible(boolean visible) {
    	
    	if (!mBackgroundFinish || !mAnimFinish) return;
    	
    	Animation bottomAnim = null;
    	Animation topAnim = null;
    	mAnimFinish = false;
    	if (visible) {
    		topAnim = AnimationUtils.loadAnimation(mContext, R.anim.from_top_in);
    		bottomAnim = AnimationUtils.loadAnimation(mContext, R.anim.from_bottom_in);
    		topAnim.setAnimationListener(this);
    		
    		mTitle.setAnimation(topAnim);
    		topAnim.start();
    		mTitle.setVisibility(View.VISIBLE);
    		
    		mBar.setAnimation(bottomAnim);
    		bottomAnim.start();
    		mBar.setVisibility(View.VISIBLE);
    	} else {
    		topAnim = AnimationUtils.loadAnimation(mContext, R.anim.from_top_out);
    		bottomAnim = AnimationUtils.loadAnimation(mContext, R.anim.from_bottom_out);
    		topAnim.setAnimationListener(this);
    		
    		mTitle.setAnimation(topAnim);
    		topAnim.start();
    		mTitle.setVisibility(View.INVISIBLE);
    		
    		mBar.setAnimation(bottomAnim);
    		bottomAnim.start();
    		mBar.setVisibility(View.INVISIBLE);
    	}
    }
    
    
    @Override
    public void finish(){
    	if (mAsyncImageCache != null)
            mAsyncImageCache.removeImageLoadedCallBack("wallpaper");
    	super.finish();
    }
    
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        removePicBitmapFromMemory();
        mAsyncImageCache = null;
        
        if (mPicBitmap != null) {
            if (!mPicBitmap.isRecycled()) {
                mPicBitmap.recycle();
            }
            mPicBitmap = null;
        }
    }
    

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.wallpaper_detail_back:
            WallpaperDetail.this.finish();
            break;
        case R.id.wallpaper_detail_download:
            downloadWallPaper();
            break;
        case R.id.wallpaper_detail_setting:
            setThisWallpaper();
            break;
        case R.id.wallpaper_detail_share:
            sharePicture();
            break;
        case R.id.wallpaper_detail_background:
        	if (mTitle.getVisibility() == View.VISIBLE || mBar.getVisibility() == View.VISIBLE) {
        		setTitleAndBarVisible(false);
        	} else {
        		setTitleAndBarVisible(true);
        	}
        	break;
        }
    }
    
    
    @Override
    public void onBackPressed() {
    	if (mTitle.getVisibility() != View.VISIBLE || mBar.getVisibility() != View.VISIBLE) {
    		setTitleAndBarVisible(true);
    	} else {
    		super.onBackPressed();
    	}
    }
    
    
    private void setBackgroundImage(String imageUrl, boolean isAlwaysShow) {

        mAsyncImageCache.displayImage(
                new AsyncImageCache.NetworkImage565Generator(imageUrl, imageUrl, "callBack"), 
                isAlwaysShow);
    }
    
    
    private void showBackgroundImage() {
    	
    	if (mPicBitmap == null)
    		mPicBitmap = getPicBitmapFromMemory();
        
        if (mPicBitmap == null && isPicExist(mPicLocalPath)) {
        	mPicBitmap = getBitmapFromLocal(mPicLocalPath);
        }
        
        if (mPicBitmap == null) return;

//    	HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
//    	ImageView image = new ImageView(mContext);
//    	image.setImageBitmap(mPicBitmap);
//    	
//    	scrollView.addView(image);
//    	mBackground.addView(scrollView);
        
        mBackground.setBackgroundDrawable(new BitmapDrawable(mPicBitmap));
        mBackgroundFinish = true;
    }
    
    
    private int[] getWindowDisplay() {
        int[] display = {0, 0};
        WindowManager wm = this.getWindowManager();
        display[0] = wm.getDefaultDisplay().getWidth();
        display[1] = wm.getDefaultDisplay().getHeight();
        return display;
    }
    
    
    private void downloadWallPaper() {
        UserLogSDK.logCountEvent(this, mDownLogDes);
        
    	if (MarketUtils.getAPNType(mContext) == -1) {
    		if (!mHandler.hasMessages(MSG_NET_TIP)) 
            	mHandler.sendEmptyMessage(MSG_NET_TIP);
    	} else if (mUserDownloadSuccess && isWallPaperDownloaded()) {
            if (!mHandler.hasMessages(MSG_DOWNLOAD_SUCCESS_TIP)) 
                mHandler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS_TIP);
        } else if (mUserDownload) {
            if (!mHandler.hasMessages(MSG_DOWNLOADING_TIP)) 
                mHandler.sendEmptyMessage(MSG_DOWNLOADING_TIP);
        } else {
            mUserDownload = true;
            mHandler.sendEmptyMessage(MSG_DOWNLOAD_START_TIP);
            if (isPicExist(mPicLocalPath)) {
                copyPicToDownload(mPicLocalPath, getWallPaperDownloadPath(mDownloadId+ "-" +mDownloadName));
            } else {
                setBackgroundImage(mDownloadUrl, true);
            }
        }
    }
    
    
    private void setThisWallpaper() {
        UserLogSDK.logViewClickEvent(this, mLogDes);
        
    	if (mPicBitmap == null)
    		mPicBitmap = getPicBitmapFromMemory();
        
        if (mPicBitmap == null && isPicExist(mPicLocalPath)) {
        	mPicBitmap = getBitmapFromLocal(mPicLocalPath);
        }
        
        if (mPicBitmap == null) {
        	mHandler.sendEmptyMessage(MSG_DOWNLOAD_TIP);
            return; 
        }
        
        mLoadingProgressDialog.setMessage(mContext.getString(R.string.wallpaper_setting));
        mLoadingProgressDialog.show();
        
        new Thread() {
            @Override
            public void run() {
                setWallPaperWithPic();
            }
        }.start();
    }
    
    
    private void setWallPaperWithPic() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            wm.setBitmap(mPicBitmap);
            mHandler.sendEmptyMessage(MSG_SETTING_SUCCESS_TIP);
        } catch (Exception e) {
            mHandler.sendEmptyMessage(MSG_SETTING_FAIL_TIP);
            e.printStackTrace();
        }
    }
    
    
    private void sharePicture() {
        
    	if (mPicBitmap == null)
    		mPicBitmap = getPicBitmapFromMemory();
        
        if (mPicBitmap == null && isPicExist(mPicLocalPath)) {
        	mPicBitmap = getBitmapFromLocal(mPicLocalPath);
        }
        
        if (mPicBitmap == null) {
        	mHandler.sendEmptyMessage(MSG_SHARE_TIP);
            return; 
        }
    	
//        mLoadingProgressDialog.setMessage(mContext.getString(R.string.wallpaper_sharing));
//        mLoadingProgressDialog.show();
        new Thread() {
            @Override
            public void run() { 
                String path = saveBitmap2Local(mPicBitmap);
//                mHandler.sendEmptyMessage(MSG_DISMISS);
                
                Intent intent = new Intent(WallpaperDetail.this, ShareAppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ShareAppActivity.INTENT_KEY_FROM_WALLPAPER, true);
                intent.putExtra(ShareAppActivity.INTENT_KEY_BITMAP_LOCAL_PATH, path);
                intent.putExtra(ShareAppActivity.INTENT_KEY_SHARE_URL, mDownloadUrl);
                startActivity(intent);
            }
            
        }.start();
    }
    

	public String saveBitmap2Local(Bitmap bm) {
		
		if (bm == null) return "";
		
		File file = new File(getSharePicPath());
		try {
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileOutputStream out = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 30, out);
			out.flush();
			out.close();
			return file.getPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
    private String getSharePicPath() {
        String picPath = MarketUtils.FileManage.getSDPath() 
            + Constant.download_cache_dir 
            +"wallpaper_share.jpg";
        
        return picPath;
    }
    
    
    private void copyPicToDownload(final String oldPath, final String newPath) {
        new Thread() {
            @Override
            public void run() {
                try {
                    int bytesum = 0;
                    int byteread = 0;
                    File oldfile = new File(oldPath);
                    if (oldfile.exists()) {
                        InputStream inStream = new FileInputStream(oldPath);
                        FileOutputStream fs = new FileOutputStream(newPath);
                        byte[] buffer = new byte[10 * 1024];
                        while ((byteread = inStream.read(buffer)) != -1) {
                            bytesum += byteread;
                            fs.write(buffer, 0, byteread);
                        }
                        inStream.close();
                        
                        File newFile = new File(newPath);
                        saveImageToDatabase(newFile);
                        
                        if (mHandler != null) 
                        	mHandler.sendEmptyMessage(MSG_DOWNLOAD_SUCCESS_TIP);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mHandler != null) 
                	mHandler.sendEmptyMessage(MSG_DOWNLOAD_FAIL_TIP);
            }
        }.start();
    }
    
    
    private boolean isWallPaperDownloaded() {
    	String picPath = getWallPaperDownloadPath(mDownloadId+ "-" +mDownloadName);
    	File file = new File(picPath);
    	if (file.exists()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    
    private String getWallPaperDownloadPath(String name) {
    	
        String picPath = MarketUtils.FileManage.getSDPath() 
            + Constant.download_path
            + "wallpaper";
        
        File destDir = new File(picPath);
        if (!destDir.exists()) {
        	destDir.mkdirs();
        }
        
        return picPath + "/" + name;
    }

    
    private String getWallPaperPath(String tag) {
        String picPath = MarketUtils.FileManage.getSDPath() 
            + Constant.download_cache_dir 
            +"cache_"
            + MD5.Md5(tag);
        
        return picPath;
    }
    
    
    private boolean isPicExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;

        File file = new File(filePath);
        if (file == null || !file.exists()) {
            return false;
        } else {
            return true;
        }
    }

    
    private Bitmap getPicBitmapFromMemory() {
    	
    	if (mAsyncImageCache == null || TextUtils.isEmpty(mPicTag)) return null;
        return mAsyncImageCache.getBitmapWithTag(mPicTag);
    }
    

    private void removePicBitmapFromMemory() {
        mAsyncImageCache.removeBitmapWithTag(mPicTag);
    }
    
    
    private Bitmap getBitmapFromLocal(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        try {
            return BitmapFactory.decodeFile(filePath);
        } catch (OutOfMemoryError e) {
        }
        return null;
    }
    
    
    public byte[] getBitmap2Bytes(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 30, baos);
        return baos.toByteArray();
    }


    @Override
    public void imageLoaded(String tag) {
        // TODO Auto-generated method stub
    	showBackgroundImage();
        if (!TextUtils.isEmpty(tag) && tag.equals(mDownloadUrl)) {
            if (mUserDownload) {
                copyPicToDownload(mPicLocalPath, getWallPaperDownloadPath(mDownloadId+ "-" +mDownloadName));
            }
        }
    }


	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub

		mAnimFinish = true;
	}


	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void saveImageToDatabase(File output) {
        if (output == null) return;

        ContentValues values = new ContentValues();
        long now = System.currentTimeMillis() / 1000;

//        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, output.getName());
        values.put(Images.Media.DATE_ADDED, now);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.ORIENTATION, 0);
        values.put(Images.Media.DATA, output.getAbsolutePath());
        values.put(Images.Media.SIZE, output.length());
//        values.put(Images.Media.WIDTH, mDisplay[0]);
//        values.put(Images.Media.HEIGHT, mDisplay[1]);

        this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
