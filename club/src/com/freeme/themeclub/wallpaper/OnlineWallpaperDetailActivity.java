package com.freeme.themeclub.wallpaper;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONObject;
import android.os.Environment;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;
import com.freeme.themeclub.ShareUtil;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.base.ResourceConstants;
import com.freeme.themeclub.wallpaper.util.ImageUtils;
import com.freeme.themeclub.wallpaper.util.InputStreamLoader;
import com.freeme.themeclub.wallpaper.util.NetworkUtil;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;
import com.freeme.themeclub.wallpaper.util.WallpaperUtils;
import com.freeme.themeclub.wallpaper.view.HorzontalSliderView;
import com.freeme.themeclub.wallpaper.view.HorzontalSliderView.SliderMoveListener;

public class OnlineWallpaperDetailActivity extends Activity implements OnClickListener,SliderMoveListener{
    private ViewPager mDetailPager;
//    private ImageView mPreviewMask;
    private LinearLayout mTitleLayout;
    private TextView mTitleText;
    private LinearLayout mOperateBarView;
    private Button mDownLoadBtn;
    private Button mApplyBtn;
    private ImageView shareImage;
    private ImageView deleteImage;
    private Runnable mCallback;
    ArrayList<Map<String, Object>> mListData;
    private ArrayList<View> mPagerViews = new ArrayList<View>();
    int mCurrentPosition;
    private AsyncImageCache mAsyncImageCache;
    private int flag;
    private PopupWindow mPopupWindow;
    private Resources resources;
    private DisplayMetrics outMetrics;
    private final String savePath = "/themes/download/";
    private int mSetSelect;
    private HorzontalSliderView mSlideView;

    private final int MenuDesktop = Menu.FIRST;
    private final int MenuLockscreen = Menu.FIRST + 1;
    private final int MenuBoth = Menu.FIRST + 2;
    private final int MenuCrop = Menu.FIRST + 3;

    private ProgressDialog mDialog = null;

    private StatisticDBHelper mStatisticDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_online_datail);
        mAsyncImageCache = AsyncImageCache.from(this);
        resources = getResources();
        outMetrics = resources.getDisplayMetrics();
        mCallback = new MyCallBack();

        mStatisticDBHelper=StatisticDBHelper.getInstance(OnlineWallpaperDetailActivity.this);

        getData(getIntent());
        initViews();
        setViewPagerData();
        mSlideView.regeisterMoveListener(this);

        specialCase();
        updataInfoData(mCurrentPosition,null);
    }

    @SuppressWarnings("unchecked")
    private void getData(Intent intent){
        mSetSelect = intent.getIntExtra(OnlineUtils.MySetSelect, OnlineUtils.NarrowSreen);
        mCurrentPosition = intent.getIntExtra(OnlineUtils.MyClickPosition, 0);
        mListData = (ArrayList<Map<String,Object>>)intent.getSerializableExtra(OnlineUtils.MyListData);
        flag = intent.getIntExtra(IntentConstants.EXTRA_RESOURCE_FLAG, -1);
    }

    private void specialCase(){
        if(mCurrentPosition == 0){
            ImageView pageOne = (ImageView)mPagerViews.get(0).findViewById(R.id.viewpager_item_iv);
            String pageOneUrl = mListData.get(0).get(OnlineUtils.WALLPAPER_ORIGNAL_URL)+"";
            mAsyncImageCache.displayImage(pageOne,
                    pageOne.getDrawable(),
                    new AsyncImageCache.NetworkImageGenerator(pageOneUrl,pageOneUrl),mCallback);

        }
    }
    @Override
    protected void onDestroy() {
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    private void setViewPagerData() {
        mPagerViews.clear();
        FrameLayout pageItem;
        ImageView itemView;
        LayoutInflater mInflater = LayoutInflater.from(this);
        String thumbUrl;
        LayoutParams l = new LayoutParams(2*outMetrics.widthPixels, outMetrics.heightPixels);
        l.gravity = Gravity.CENTER;
        for (int i = 0; i < mListData.size(); i++) {
            pageItem = (FrameLayout)mInflater.inflate(R.layout.viewpager_item, null);
            itemView = (ImageView)pageItem.findViewById(R.id.viewpager_item_iv);
            if(mSetSelect == OnlineUtils.WideSreen){
                itemView.setLayoutParams(l);
            }

            if(i==mCurrentPosition||i==mCurrentPosition-1||i==mCurrentPosition+1){
                setImageBitmap(itemView, i);
            }
            //            thumbUrl = mListData.get(i).get(OnlineUtils.WALLPAPER_THUMB_URL)+"";
            //            itemView.setImageBitmap(mAsyncImageCache.getCacheImage(
            //                    resources.getDimensionPixelSize(mSetSelect != OnlineUtils.WideSreen ? R.dimen.thumb_preview_w : R.dimen.wide_thumb_preview_w),
            //                    resources.getDimensionPixelSize(mSetSelect != OnlineUtils.WideSreen ? R.dimen.thumb_preview_h : R.dimen.wide_thumb_preview_h),
            //                    thumbUrl));
            itemView.setOnClickListener(this);
            mPagerViews.add(pageItem);
        }
        mPagerViews.get(mCurrentPosition).findViewById(R.id.detail_loading_pb).setVisibility(View.VISIBLE);
        mDetailPager.setAdapter(new OnlineUtils.MyPagerAdapter(mPagerViews));
        mDetailPager.setCurrentItem(mCurrentPosition);
    }
    private void initViews() {
        mDetailPager = (ViewPager)findViewById(R.id.online_detail_vp);
        mSlideView = (HorzontalSliderView)findViewById(R.id.online_slider);
        mSlideView.setVisibility(mSetSelect == OnlineUtils.WideSreen ? View.VISIBLE : View.INVISIBLE); 
//        mPreviewMask = (ImageView)findViewById(R.id.online_previewMask);
//        mPreviewMask.setImageResource(flag == ResourceConstants.R_LOCKSCREEN_WALLPAER ? 
//                R.drawable.wallpaper_detail_lockscreen_mask:
//                    R.drawable.wallpaper_detail_desktop_mask);
//        mPreviewMask.setScaleType(ScaleType.FIT_XY);
//        mPreviewMask.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    mPreviewMask.setVisibility(View.INVISIBLE);
//                    mSlideView.setVisibility(View.VISIBLE);
//                    updateTitleAndOperateBarState(true);
//                }
//                return true;
//            }
//        });
        mTitleLayout = (LinearLayout)findViewById(R.id.online_titleArea);
        mTitleText = (TextView)findViewById(R.id.online_title);
        mOperateBarView = (LinearLayout)findViewById(R.id.online_operationBar);
        mDownLoadBtn = (Button)findViewById(R.id.online_downloadButton);
        mApplyBtn = (Button)findViewById(R.id.online_applyButton);
        shareImage=(ImageView) findViewById(R.id.share_button);
        deleteImage=(ImageView) findViewById(R.id.delete_button);
        setListeners();
    }
    private void setListeners() {
        findViewById(R.id.online_homeAsUp).setOnClickListener(this);
        findViewById(R.id. online_title).setOnClickListener(this);
        findViewById(R.id.online_infoButton).setOnClickListener(this);
        mDownLoadBtn.setOnClickListener(this);
        mApplyBtn.setOnClickListener(this);
        shareImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        mDetailPager.setOnClickListener(this);
        mDetailPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mDownLoadBtn.setEnabled(false);
                mCurrentPosition = position;
                setImageBitmap(mPagerViews.get(position).findViewById(R.id.viewpager_item_iv),position);

                mPagerViews.get(position).findViewById(R.id.detail_loading_pb).setVisibility(View.VISIBLE);
                updataInfoData(position,null);
                String detailUrl = mListData.get(position).get(OnlineUtils.WALLPAPER_ORIGNAL_URL)+"";
                ImageView mItemImage = (ImageView)mPagerViews.get(position).findViewById(R.id.viewpager_item_iv);
                mItemImage.setOnClickListener(OnlineWallpaperDetailActivity.this);
                mAsyncImageCache.displayImage(mItemImage,mItemImage.getDrawable(),
                        new AsyncImageCache.NetworkImageGenerator(detailUrl,detailUrl),mCallback);
                if(position>0){
                    setImageBitmap(mPagerViews.get(position-1).findViewById(R.id.viewpager_item_iv),position-1);
                }
                if(position<mListData.size()-1){
                    setImageBitmap(mPagerViews.get(position+1).findViewById(R.id.viewpager_item_iv),position+1);
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
            }
        });
    }

    private void setImageBitmap(View itemView,int i){
        String thumbUrl = mListData.get(i).get(OnlineUtils.WALLPAPER_THUMB_URL)+"";
        if(((ImageView)itemView).getDrawable()==null){
            Bitmap bitmap = mAsyncImageCache.getCacheImage(
                    resources.getDimensionPixelSize(/*mSetSelect != OnlineUtils.WideSreen ? R.dimen.thumb_preview_w : */R.dimen.wide_thumb_preview_w),
                    resources.getDimensionPixelSize(/*mSetSelect != OnlineUtils.WideSreen ? R.dimen.thumb_preview_h :*/ R.dimen.wide_thumb_preview_h),
                    thumbUrl);
            ((ImageView)itemView).setImageBitmap(crop(bitmap));
        }

    }

    private  Bitmap crop(Bitmap bitmap){
        if(bitmap==null){
            return bitmap;
        }
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int x = (width-width/(size.y/height))/2;
        Bitmap dest = Bitmap.createScaledBitmap(
                bitmap, outMetrics.widthPixels, height*outMetrics.widthPixels/width, false);
        return dest; 
    }

    private class MyCallBack implements Runnable{
        @Override
        public void run() {
            mPagerViews.get(mCurrentPosition).findViewById(R.id.detail_loading_pb).setVisibility(View.GONE);
            mDownLoadBtn.setEnabled(true);
            mDownLoadBtn.setBackgroundResource(R.drawable.tab_button_big);
            mDownLoadBtn.setTextColor(Color.WHITE);
            shareImage.setEnabled(true);
            shareImage.setImageResource(R.drawable.share_wallpaper);
        }
    }

    private void postDownloadTimes(int id){
        new PostDownloadTimesTask().execute(id);
    }

    private class PostDownloadTimesTask extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... params) {
            int wallpaperId = params[0];
            JSONObject paraInfo = new JSONObject();
            try {
                paraInfo.put("id", wallpaperId);

                JSONObject jsObject = new JSONObject();
                jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_WALLPAPER_DOWNLOAD_TIMES_BY_TAG_REQ));

                jsObject.put("body", paraInfo.toString());
                String contents = jsObject.toString();
                String url = MessageCode.SERVER_URL;
                NetworkUtil.accessNetworkByPost(url, contents);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    Handler mHandler = new Handler();

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
        case R.id.online_homeAsUp:
            setResult(100);
            finish();
            break;
        case R.id.online_title:
            finish();
            break;
        case R.id.online_infoButton:
            if (mListData == null)
                return;
            View detailInfo = LayoutInflater.from(this).inflate(R.layout.wallpaper_online_detail_info, null);
            updataInfoData(mCurrentPosition, detailInfo);
            new Builder(this).setTitle(R.string.resource_title).
            setCancelable(true).setView(detailInfo).
            setPositiveButton(R.string.close, null).
            show();
            break;
        case R.id.viewpager_item_iv:
//            mPreviewMask.startAnimation(getAnimation(false, true, mPreviewMask.getWidth(), 0, 300L));
//            mPreviewMask.setVisibility(View.VISIBLE);
            boolean flag = mSlideView.getVisibility()== View.VISIBLE?true:false;
            if(mSlideView.getVisibility()== View.VISIBLE){
                mSlideView.setVisibility(View.INVISIBLE);
                updateTitleAndOperateBarState(false);
            }else{
                mSlideView.setVisibility(View.VISIBLE);
                updateTitleAndOperateBarState(true);
            }
            break;
        case R.id.online_downloadButton:
            if(mListData == null)
                return;
            Map<String, Object> mCurrentMap = mListData.get(mCurrentPosition);
            if(OnlineUtils.saveBitmap(mAsyncImageCache.getCacheImage(mCurrentMap.get("dnUrlX")+""), 
                    ((Integer)(mCurrentMap.get("id")))+(String)mCurrentMap.get("name"))){
                addItemToDatabase(mCurrentMap);
                new UpdateDncntTask((Integer)mCurrentMap.get("id")).execute();
                Toast.makeText(this, R.string.download_success, Toast.LENGTH_SHORT).show();
                mDownLoadBtn.setVisibility(View.GONE);
                mApplyBtn.setVisibility(View.VISIBLE);
                deleteImage.setEnabled(true);
                deleteImage.setImageResource(R.drawable.tab_delete2);
                postDownloadTimes((Integer)mCurrentMap.get("id"));
            }else{
                Toast.makeText(this, R.string.download_fail, Toast.LENGTH_SHORT).show();
            }

            break;
        case R.id.online_applyButton:
            Builder builder=new AlertDialog.Builder(this);
            builder.setSingleChoiceItems(R.array.wallpaper_use, -1, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                    case 0:
                        dialog.dismiss();
                        showDialog();
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                applyWallpaper(ResourceConstants.R_DESKTOP_WALLPAER);
                            }
                        }, 200);
                        break;
                    case 1:
                        dialog.dismiss();
                        showDialog();
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                applyWallpaper(ResourceConstants.R_LOCKSCREEN_WALLPAER);

                            }
                        }, 200);

                        break;
                    case 2:
                        dialog.dismiss();
                        showDialog();
                        mHandler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                applyWallpaper(ResourceConstants.R_DESKTOP_WALLPAER | ResourceConstants.R_LOCKSCREEN_WALLPAER);
                            }
                        }, 200);
                        break;
                    case 3:
                        dialog.dismiss();
                        WallpaperUtils.cropAndApplyWallpaper(OnlineWallpaperDetailActivity.this, 
                                0, getFileUrl(), false, true);
                        break;

                    }

                }
            });
            AlertDialog dialog=builder.create();
            dialog.show();
            break;

        case R.id.share_button:
            ShareUtil.shareText(this, getResources().getString(R.string.share_wallpaper_message)+"\n"+
                    (""+mListData.get(mCurrentPosition).get("dnUrlX")).replace(" ", "%20"));

            String name=mListData.get(mCurrentPosition).get("name").toString();
            String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.WALL_CLICK_SHARE, name,System.currentTimeMillis());
            mStatisticDBHelper.intserStatisticdataToDB(infoStr);

            break;
        case R.id.delete_button:
            AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(
                    OnlineWallpaperDetailActivity.this);
            deleteBuilder.setMessage( getResources().getString(R.string.delete_wallpaper_tips))
            .setNegativeButton(getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    dialog.dismiss();
                }
            })
            .setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    new File(getFileUrl()).delete();
                    mDownLoadBtn.setVisibility(View.VISIBLE);
                    mApplyBtn.setVisibility(View.GONE);
                    deleteImage.setEnabled(false);
                    deleteImage.setImageResource(R.drawable.delete_cannot_use_wallpaper2);
                    String where = MediaStore.Images.Media.DATA + "='" + getFileUrl() + "'"; 
                    getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                }
            });
            deleteBuilder.create().show();
            break;
        default:
            break;
        }
    }

    private void addItemToDatabase(Map<String, Object> mCurrentMap){
        String sdPath = OnlineUtils.getSDPath();
        if(TextUtils.isEmpty(sdPath))return;
        ContentValues values = new ContentValues();
        long time = System.currentTimeMillis();
        String title = mCurrentMap.get("name")+"";
        File mFile = new File(sdPath + "/themes/download/" + title );

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
                Uri.parse("file://" + Environment.getExternalStorageDirectory()+ "/themes/download" )));
    }

    private String getFileUrl() {
        return OnlineUtils.getSDPath()+savePath+mListData.get(mCurrentPosition).get("id")+mListData.get(mCurrentPosition).get("name");
    }

    private void updataInfoData(int position,View detailInfo) {
        Map<String, Object> infoMap = mListData.get(position);
        String title = infoMap.get(OnlineUtils.WALLPAPER_NAME)+"";
        boolean isDownloaded = OnlineUtils.checkIsDownLoaded(infoMap.get(OnlineUtils.WALLPAPER_ID)+title);
        if(title.length()>30){
            title=title.substring(0, 30)+"...";
        }
//        mTitleText.setText(title);

        mDownLoadBtn.setVisibility(isDownloaded ? View.GONE : View.VISIBLE);
        mApplyBtn.setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        deleteImage.setEnabled(isDownloaded ? true : false);
        if(deleteImage.isEnabled()){
            deleteImage.setImageResource(R.drawable.tab_delete2);
        }else{
            deleteImage.setImageResource(R.drawable.delete_cannot_use_wallpaper2);
        }
        if(detailInfo == null) return;

        ((TextView)detailInfo.findViewById(R.id.online_name)).setText(infoMap.get(OnlineUtils.WALLPAPER_NAME)+"");
        ((TextView)detailInfo.findViewById(R.id.online_download)).setText(infoMap.get(OnlineUtils.WALLPAPER_DOWNLOAD_COUNT)+"");
        //((TextView)detailInfo.findViewById(R.id.online_size)).setText(infoMap.get(""));
        ((TextView)detailInfo.findViewById(R.id.online_modifiedTime)).setText(infoMap.get(OnlineUtils.WALLPAPER_MODIFY_TIME)+"");
    }
    private void updateTitleAndOperateBarState(boolean visiable) {
        mTitleLayout.startAnimation(getAnimation(false, visiable, 0, -mTitleLayout.getHeight(),200));
        mTitleLayout.setVisibility(visiable ? View.VISIBLE : View.INVISIBLE);
        mOperateBarView.startAnimation(getAnimation(false, visiable, 0, mOperateBarView.getTop(),200));
        mOperateBarView.setVisibility(visiable ? View.VISIBLE : View.INVISIBLE);
    }
    @Override
    public void onBackPressed() {
        if(mSlideView.getVisibility() != View.VISIBLE){
            mSlideView.setVisibility(View.VISIBLE);
            updateTitleAndOperateBarState(true);
        }else {
            super.onBackPressed();
        }
    }
    private Animation getAnimation(boolean alphaAnim, boolean forEnter, int offsetX, int offsetY, long duration) {
        Animation anim;
        if (alphaAnim) {
            float startAlpha = forEnter ? 0 : 1;
            anim = new AlphaAnimation(startAlpha, 1 - startAlpha);
        } else {
            int startX = (forEnter) ? offsetX : 0;
            int endX = offsetX - startX;
            int startY = (forEnter) ? offsetY : 0;
            int endY = offsetY - startY;
            anim = new TranslateAnimation(startX, endX, startY, endY);
        }
        anim.setDuration(duration);
        return anim;
    }

    private void applyWallpaper(int flag) {
        if(mListData == null)
            return;
        Map<String, Object> mCurrentMap = mListData.get(mCurrentPosition);
        Bitmap bitmap = mAsyncImageCache.getCacheImage(mCurrentMap.get(OnlineUtils.WALLPAPER_ORIGNAL_URL)+"");
        boolean applyDesk = true;
        boolean applyLock = true;
        if ((flag & ResourceConstants.R_DESKTOP_WALLPAER) != 0) {
            applyDesk = WallpaperUtils.saveDeskWallpaperByDefaultDisplay(this, bitmap, 
                    null, mSetSelect != OnlineUtils.WideSreen ? true : false);
        }
        if ((flag & ResourceConstants.R_LOCKSCREEN_WALLPAER) != 0) {
            applyLock = WallpaperUtils.saveLockWallpaperByDisplay(this, bitmap, null);
        }
        ResourceHelper.showThemeChangedToast(this, (applyDesk && applyLock));
        if (applyDesk && applyLock) {
            setResult(100);
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.android_slide_out_down);
        }
    }

    public void showDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDialog = null;
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getResources().getString(R.string.is_applying_wallpaper));
        mDialog.setIndeterminate(false);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void cancelDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    @Override
    public void movePercent(float movePercentFromCenter, boolean stopMove) {
        if(movePercentFromCenter > 1)movePercentFromCenter = 1;
        if(movePercentFromCenter < -1)movePercentFromCenter = -1;
        ImageView mCurrentView = (ImageView)mPagerViews.get(mCurrentPosition).findViewById(R.id.viewpager_item_iv);

        mCurrentView.layout(
                (int)((-0.5-movePercentFromCenter/2)*outMetrics.widthPixels),
                mCurrentView.getTop(), 
                (int)((2+(-0.5-movePercentFromCenter/2)) * outMetrics.widthPixels), 
                mCurrentView.getBottom());
    }


    class UpdateDncntTask extends AsyncTask<Void, Void, Void>{

        int mWallpaperId;

        public UpdateDncntTask(int wallpaperId){
            super();

            mWallpaperId = wallpaperId;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                int msgCode = MessageCode.UPDATE_DNCNT_BY_TAG_REQ;
                JSONObject paraInfo = new JSONObject();
                paraInfo.put("id", mWallpaperId);


                JSONObject jsObject = new JSONObject();
                jsObject.put("head", NetworkUtil.buildHeadData(msgCode));
                jsObject.put("body", paraInfo.toString());
                String contents = jsObject.toString();
                String url = MessageCode.SERVER_URL;
                String result = NetworkUtil.accessNetworkByPost(url, contents);
            }catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            setResult(100);
        }
        return super.onKeyDown(keyCode, event);
    }

}
