package com.freeme.themeclub.wallpaper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.R;
import com.freeme.themeclub.ShareUtil;
import com.freeme.themeclub.wallpaper.base.IntentConstants;
import com.freeme.themeclub.wallpaper.base.ResourceConstants;
import com.freeme.themeclub.wallpaper.base.ResourceDetailActivity;
import com.freeme.themeclub.wallpaper.resource.Resource;
import com.freeme.themeclub.wallpaper.util.ImageCacheDecoder;
import com.freeme.themeclub.wallpaper.util.ImageUtils;
import com.freeme.themeclub.wallpaper.util.InputStreamLoader;
import com.freeme.themeclub.wallpaper.util.LogUtils;
import com.freeme.themeclub.wallpaper.util.OnlineUtils;
import com.freeme.themeclub.wallpaper.util.WallpaperDecoder;
import com.freeme.themeclub.wallpaper.util.WallpaperUtils;
import com.freeme.themeclub.wallpaper.view.HorzontalSliderView;
import com.freeme.themeclub.wallpaper.view.WallpaperView;

public class WallpaperDetailActivity extends ResourceDetailActivity{

    private static final String TAG = "WallpaperDetail";

    private int mResourceType;
    //*/
    protected String mLocalPath;
    protected View mPreviousItem;
    protected View mNextItem;
    //*/
    private boolean mIsLockscreen = false;
    private GestureDetector mGestureDetector;

    private HorzontalSliderView mSliderView;
    private View mTitleAreaView;
    private View mOperateBarView;
    private ImageView mPreviewMaskView;
    private TextView mDownloadBtn;
    private PopupWindow mPopupWindow;
    private WallpaperView mWallpaperView;

    private ImageView shareImage;
    private ImageView deleteImage;

    private ImageCacheDecoder mImageAsyncDecoder;

    private int mWallpaperHeight = 0;
    private int mWallpaperWidth = 0;

    private boolean mThumbnailModeOfWallpaperBeforePreview = false;
    protected String mTitle = null;

    private final int MenuDesktop = Menu.FIRST;
    private final int MenuLockscreen = Menu.FIRST + 1;
    private final int MenuBoth = Menu.FIRST + 2;
    private final int MenuCrop = Menu.FIRST + 3;

    private final String savePath = "/themes/download/";
    private ProgressDialog mDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mResourceSet.isEmpty()) {
            finish();
        } else {
            setContentView(R.layout.wallpaper_detail);
            mWallpaperView = (WallpaperView) findViewById(R.id.wallpaperView);
            mWallpaperView.regeisterSwitchListener(getWallpaperSwitchListener());
            //            mWallpaperView.setContainingBitmapSize(mWallpaperWidth, mWallpaperHeight);

            mSliderView = (HorzontalSliderView) findViewById(R.id.slider);
            mSliderView.regeisterMoveListener(getSliderMoveListener());
            //            mSliderView.setVisibility(needThinkOfRatio() ? View.VISIBLE : View.INVISIBLE);

            mGestureDetector = new GestureDetector(this, new WallpaperGestureListener());

            mImageAsyncDecoder = new WallpaperDecoder(3);

            //            int decoderScaledWidth = mWallpaperWidth;
            //            int decoderScaledHeight = mWallpaperHeight;
            //
            //            //*/ XXX 
            //            if (AppFeature.FEATURE_DETAIL_WALLPAPER_SCALE_VIEW) {
            //                // for 1080P, decode less large image to make system working well.
            //                Point p = ResourceHelper.calcSizeForDecoder(decoderScaledWidth, decoderScaledHeight);
            //                if (p != null) {
            //                    LogUtils.w(TAG, String.format("wallpaper bitmap scale to (width %d, height %d)", p.x, p.y));
            //                    decoderScaledWidth = p.x;
            //                    decoderScaledHeight = p.y;
            //                }
            //            }
            //            //*/
            //            mImageAsyncDecoder.setScaledSize(decoderScaledWidth, decoderScaledHeight);
            mImageAsyncDecoder.regeisterListener(getImageDecodingListener());

            initUI();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initWallpaperViewBitmap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mImageAsyncDecoder != null) {
            mImageAsyncDecoder.clean(false);
            mWallpaperView.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImageAsyncDecoder != null) {
            mImageAsyncDecoder.clean(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        if (mWallpaperView.hasBeenInitied()) {
            ret = mGestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mWallpaperView.autoSwitchCurreentWallpaper();
            }
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        if (mPreviewMaskView.getVisibility() == View.VISIBLE) {
            quitPreviewMode(true);
        } else {
            super.onBackPressed();
        }
    }

    private int mDisplayType = 0;

    Point size ;
    @Override
    protected void pickMetaData(Bundle metaData) {
        super.pickMetaData(metaData);
        mResourceType = mMetaData.getInt(IntentConstants.EXTRA_RESOURCE_FLAG, ResourceConstants.R_NONE);
        mIsLockscreen = (mResourceType == ResourceConstants.R_LOCKSCREEN_WALLPAER);

        size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mWallpaperWidth = size.x;
        mWallpaperHeight = size.y;

        //        mDisplayType = mMetaData.getInt(IntentConstants.EXTRA_DISPLAY_TYPE);
        //        if (needThinkOfRatio()) {
        //            mWallpaperWidth *= WallpaperUtils.WALLPAPER_SCREEN_SPAN;
        //        }
    }

    protected boolean needThinkOfRatio() {
        //        return (mDisplayType == ResourceConstants.DISPLAY_DOUBLE_FLAT);

        InputStreamLoader is = new InputStreamLoader(this, Uri.parse("file://" + mLocalPath));
        BitmapFactory.Options options = ImageUtils.getBitmapSize(is);
        is.close();
        //        int maxNeedWidth = (int) (1.1f * size.x);
        //        int maxNeedHeight = (int) (1.1f * size.y);
        mWallpaperWidth = options.outWidth;
        mWallpaperHeight = options.outHeight;
        //        return (options.outWidth > maxNeedWidth || options.outHeight > maxNeedHeight);
        return options.outWidth > options.outHeight;
    }
    Handler mHandler = new Handler();
    private void initUI() {
        //		setupNavigationButton();
        deleteImage=(ImageView) findViewById(R.id.delete_button);
        changeCurrentResource();
        // title
        mTitleAreaView = findViewById(R.id.titleArea);
        mTitleAreaView.setOnClickListener(null);
        // operation bar
        mOperateBarView = findViewById(R.id.operationBar);
        mOperateBarView.setOnClickListener(null);
        // preview mask
        mPreviewMaskView = (ImageView) findViewById(R.id.previewMask);
        mPreviewMaskView.setVisibility(View.INVISIBLE);
        mPreviewMaskView.setImageResource(mIsLockscreen ? R.drawable.wallpaper_detail_lockscreen_mask 
                : R.drawable.wallpaper_detail_desktop_mask);
        mPreviewMaskView.setScaleType(ImageView.ScaleType.FIT_XY);
        mPreviewMaskView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    quitPreviewMode(false);
                }
                return true;
            }
        });
        // home up
        findViewById(R.id.homeAsUp).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        // preview button
        //        findViewById(R.id.previewButton).setOnClickListener(new View.OnClickListener() {
        //            public void onClick(View v) {
        //                enterPreviewMode();
        //            }
        //        });
        // apply or download btn
        mDownloadBtn = (TextView) findViewById(R.id.downloadButton);
        mDownloadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) v;
                if (tv.getText() == getString(R.string.resource_apply)) {
                    Builder builder=new AlertDialog.Builder(WallpaperDetailActivity.this);
                    builder.setSingleChoiceItems(R.array.wallpaper_use, -1, 
                            new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                            case 0:
                                dialog.dismiss();
                                showDialog();
                                mHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        doApplyWallpaper(ResourceConstants.R_DESKTOP_WALLPAER, mLocalPath);
                                    }
                                }, 200);                                
                                break;
                            case 1:
                                dialog.dismiss();
                                showDialog();
                                mHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        doApplyWallpaper(ResourceConstants.R_LOCKSCREEN_WALLPAER,mLocalPath);     
                                    }
                                }, 200);
                                break;
                            case 2:
                                dialog.dismiss();
                                showDialog();
                                mHandler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        doApplyWallpaper(ResourceConstants.R_DESKTOP_WALLPAER | 
                                                ResourceConstants.R_LOCKSCREEN_WALLPAER,mLocalPath);  
                                    }
                                }, 200);

                                break;
                            case 3:
                                dialog.dismiss();
                                WallpaperUtils.cropAndApplyWallpaper(WallpaperDetailActivity.this, 
                                        0, mLocalPath, false, true);
                                break;
                            }

                        }
                    });
                    AlertDialog dialog=builder.create();
                    dialog.show();
                }
            }
        });
        // info btn
        View infoButton = findViewById(R.id.infoButton);
        infoButton.setVisibility(View.VISIBLE);
        infoButton.setEnabled(true);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View content = getLayoutInflater().inflate(R.layout.wallpaper_detail_info, null);
                Bundle information = getCurrentResourceInformation();
                if (information != null) {
                    ((TextView) content.findViewById(R.id.name))
                    .setText(information.getString(Resource.NAME));
                    ((TextView) content.findViewById(R.id.dimension))
                    .setText(information.getString(Resource.RESOLUTION));
                    ((TextView) content.findViewById(R.id.size))
                    .setText(ResourceHelper.getFormattedSize(Long
                            .parseLong(information.getString(Resource.SIZE))));
                    ((TextView) content.findViewById(R.id.modifiedTime))
                    .setText(ResourceHelper.getDateFormatByFormatSetting(
                            getApplicationContext(),
                            information.getLong(Resource.MODIFIED_TIME)));
                }
                new AlertDialog.Builder(WallpaperDetailActivity.this)//, AlertDialog.THEME_HOLO_LIGHT) // XXX
                .setTitle(R.string.resource_title)
                .setCancelable(true)
                .setView(content)
                .setPositiveButton(R.string.close, null)
                .show();
            }
        });

        shareImage=(ImageView) findViewById(R.id.share_button);
        shareImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ShareUtil.shareImage(WallpaperDetailActivity.this, 
                        getResources().getString(R.string.share_wallpaper_message),
                        mLocalPath);
            }
        });

        //        if (mLocalPath.startsWith("/system/media/wallpaper")) {
        //            deleteImage.setEnabled(false);
        //            deleteImage.setImageResource(R.drawable.delete_cannot_use_wallpaper2);
        //        }else{
        //            deleteImage.setEnabled(true);
        //            deleteImage.setImageResource(R.drawable.tab_delete2);
        //        }
        deleteImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean success=new File(mLocalPath).delete();
                String where = MediaStore.Images.Media.DATA + "='" + mLocalPath + "'"; 
                getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                if(success){
                    finish();
                }

                //                mDownLoadBtn.setVisibility(View.VISIBLE);
                //                mApplyBtn.setVisibility(View.GONE);
                //                deleteImage.setEnabled(false);
                //                deleteImage.setImageResource(R.drawable.delete_cannot_use_wallpaper);
            }
        });
        setResourceStatus();
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

    private void initWallpaperViewBitmap() {
        if (mImageAsyncDecoder != null) {
            mImageAsyncDecoder.setCurrentUseBitmapIndex(mResourceIndex);
            initWallpaperViewBitmap(1, false);
            initWallpaperViewBitmap(-1, false);
            initWallpaperViewBitmap(0, false);
            mWallpaperView.invalidate();
        }
    }

    private void initWallpaperViewBitmap(int offset, boolean async) {
        final Pair<String, Boolean> ret = cacheWallpaperResource(offset, async);

        final String imagePath = (ret != null) ? ret.first : null;
        final boolean useThumbnail = (ret != null) ? ret.second : false;

        Bitmap b = mImageAsyncDecoder.getBitmap(imagePath);
        int resIndex = mResourceIndex + offset;
        if (b != null || mWallpaperView.getUserGivenId(offset) != resIndex 
                || !mWallpaperView.showingDeterminateFg(offset)) {

            boolean validResource = (0 <= resIndex && resIndex < getGroupResources().size());
            mWallpaperView.setBitmapInfo(offset, b, resIndex, validResource, useThumbnail);
        }
    }

    private Pair<String, Boolean> cacheWallpaperResource(int offset, boolean async) {
        final Resource r = getAdjResource(offset);
        if (r != null) {
            int resIndex = mResourceIndex + offset;
            String localPath = r.getLocalPath();

            boolean useThumbnail = false;
            String ret = localPath;
            if (async) {
                InputStreamLoader is = new InputStreamLoader(this, Uri.parse("file://" + localPath));
                BitmapFactory.Options options = ImageUtils.getBitmapSize(is);
                is.close();
                mImageAsyncDecoder.clean(false);
                mImageAsyncDecoder.setScaledSize(options.outWidth, options.outHeight);
                mImageAsyncDecoder.decodeImageAsync(localPath, null, resIndex);
            } else {
                InputStreamLoader is = new InputStreamLoader(this, Uri.parse("file://" + localPath));
                BitmapFactory.Options options = ImageUtils.getBitmapSize(is);
                is.close();
                mImageAsyncDecoder.clean(false);
                mImageAsyncDecoder.setScaledSize(options.outWidth, options.outHeight);
                mImageAsyncDecoder.decodeLocalImage(localPath, resIndex, true);
            }

            return new Pair<String, Boolean>(ret, useThumbnail);
        }
        return null;
    }

    private Resource getAdjResource(int offsetFromCurrent) {
        final int index = mResourceIndex + offsetFromCurrent;
        List<Resource> set = getGroupResources();
        if (0 <= index && index < set.size()) {
            return set.get(index);
        }
        return null;
    }

    protected void setResourceStatus() {
        if (mDownloadBtn != null) {
            int resId;
            if ((new File(mLocalPath)).exists()) {
                resId = R.string.resource_apply;
                //                mMoreMenuBtn.setEnabled(true);
            } else {
                resId = R.string.resource_download;
                //                mMoreMenuBtn.setEnabled(false);
            }
            mDownloadBtn.setText(resId);
            mDownloadBtn.setEnabled(true);
        }

        if (mLocalPath.startsWith("/system/media/wallpaper")) {
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_wallpaper2);
        }else{
            deleteImage.setEnabled(true);
            deleteImage.setImageResource(R.drawable.tab_delete2);
        }
    }

    protected void updateNavigationState() {
        final int size = mResourceSet.get(mResourceGroup).size();
        //        mPreviousItem.setEnabled(mResourceIndex > 0);
        //        mNextItem.setEnabled(mResourceIndex < size - 1);
    }

    protected void changeCurrentResource() {
        requestResourceDetail(mResourceIndex);
        requestResourceDetail(mResourceIndex + 1);
        requestResourceDetail(mResourceIndex - 1);

        setResourceInfo();
        setResourceStatus();
        bindScreenView();

//        TextView title = (TextView) findViewById(R.id.title);
//        if (title != null) {
//            if(mTitle.length()>30){
//                mTitle=mTitle.substring(0, 30)+"...";
//            }
//            title.setText(mTitle);
//        }
    }

    protected Bundle getCurrentResourceInformation() {
        Resource r = getCurrentResource();
        return r != null ? r.getInformation() : null;
    }

    private List<Resource> getGroupResources() {
        return mResourceSet.get(mResourceGroup);
    }

    protected void requestResourceDetail(int resIndex) {
        Resource resource = getCurrentResource(resIndex);
    }

    protected Resource getCurrentResource() {
        return getCurrentResource(mResourceIndex);
    }

    protected Resource getCurrentResource(int resIndex) {
        List<Resource> datas = mResourceSet.get(mResourceGroup);
        if (0 <= resIndex && resIndex < datas.size()) {
            return datas.get(resIndex);
        }
        return null;
    }

    int decoderScaledWidth;
    int decoderScaledHeight;
    Point p;
    protected void setResourceInfo() {
        Bundle infos = getCurrentResourceInformation();
        if (infos != null) {
            mLocalPath = infos.getString("LOCAL_PATH");
            mTitle = infos.getString("NAME");
        }
        
        if (needThinkOfRatio()) {
            mSliderView.setVisibility(View.VISIBLE);
            mWallpaperWidth = size.x * WallpaperUtils.WALLPAPER_SCREEN_SPAN;
        }else{
            mSliderView.setVisibility(View.INVISIBLE);
            mWallpaperWidth = size.x;
        }
        mWallpaperView.setContainingBitmapSize(mWallpaperWidth, mWallpaperHeight);
        decoderScaledWidth = mWallpaperWidth;
        decoderScaledHeight = mWallpaperHeight;

        //*/ XXX 
        //        if (AppFeature.FEATURE_DETAIL_WALLPAPER_SCALE_VIEW) {
        //            // for 1080P, decode less large image to make system working well.
        //            p = ResourceHelper.calcSizeForDecoder(decoderScaledWidth, decoderScaledHeight);
        //            if (p != null) {
        //                decoderScaledWidth = p.x;
        //                decoderScaledHeight = p.y;
        //            }
        //        }
        //*/
        //        mImageAsyncDecoder.setScaledSize(decoderScaledWidth, decoderScaledHeight);
        mWallpaperWidth = size.x;
        mWallpaperHeight = size.y;
    
    }

    protected void bindScreenView() {
        initWallpaperViewBitmap();
    }

    protected void navigateToPreviousResource() {
        if (mResourceIndex > 0) {
            mResourceIndex -= 1;
            changeCurrentResource();
            updateNavigationState();
            mMetaData.putInt(IntentConstants.EXTRA_RESOURCE_INDEX, mResourceIndex);
        }
    }

    protected void navigateToNextResource() {
        final int size = mResourceSet.get(mResourceGroup).size();
        if (mResourceIndex < size - 1) {
            mResourceIndex += 1;
            changeCurrentResource();
            updateNavigationState();
            mMetaData.putInt(IntentConstants.EXTRA_RESOURCE_INDEX, mResourceIndex);
        }
    }

    private void updateTitleAndOperateBarState(boolean visiable) {
        mTitleAreaView.startAnimation(getAnimation(false, visiable, 0, -mTitleAreaView.getHeight()));
        mTitleAreaView.setVisibility(visiable ? View.VISIBLE : View.INVISIBLE);
        mOperateBarView.startAnimation(getAnimation(false, visiable, 0, mOperateBarView.getTop()));
        mOperateBarView.setVisibility(visiable ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateSliderViewState(boolean visiable) {
        if (needThinkOfRatio()) {
            final int visible = visiable ? View.VISIBLE : View.INVISIBLE;
            if (mSliderView.getVisibility() != visible) {
                mSliderView.startAnimation(getAnimation(true, visiable, 0, 0));
                mSliderView.setVisibility(visible);
            }
        } else {
            mSliderView.setVisibility(View.INVISIBLE);
        }
    }

    private void autoUpdateSliderViewState(boolean visiable) {
        if (!visiable || !mWallpaperView.isThumbnailScanMode()) {
            updateSliderViewState(visiable);
        }
    }

    private void doApplyWallpaper(int resourceType, String wallpaperPath) {
        Bitmap bitmap = null;
        //*/ XXX
        //if (!AppFeature.FEATURE_DETAIL_WALLPAPER_SCALE_VIEW) {
        bitmap = mImageAsyncDecoder.getBitmap(wallpaperPath);
        //}
        //*/
        boolean applyDesk = true;
        boolean applyLock = true;
        if ((resourceType & ResourceConstants.R_DESKTOP_WALLPAER) != 0) {
            applyDesk = WallpaperUtils.saveDeskWallpaperByDefaultDisplay(this, bitmap, 
                    Uri.fromFile(new File(wallpaperPath)),!needThinkOfRatio());
        }
        if ((resourceType & ResourceConstants.R_LOCKSCREEN_WALLPAER) != 0) {
            applyLock = WallpaperUtils.saveLockWallpaperByDisplay(this, bitmap, 
                    Uri.fromFile(new File(wallpaperPath)));
        }
        ResourceHelper.showThemeChangedToast(this, (applyDesk && applyLock));

        if (applyDesk && applyLock) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.android_slide_out_down);
        }
    }

    private void enterPreviewMode() {
        mThumbnailModeOfWallpaperBeforePreview = mWallpaperView.isThumbnailScanMode();
        if (mThumbnailModeOfWallpaperBeforePreview) {
            mWallpaperView.setScanMode(!mThumbnailModeOfWallpaperBeforePreview);
        }

        mPreviewMaskView.startAnimation(getAnimation(false, true, mPreviewMaskView.getWidth(), 0, 300L));
        mPreviewMaskView.setVisibility(View.VISIBLE);
        updateTitleAndOperateBarState(false);
        updateSliderViewState(false);
    }
    private void quitPreviewMode(boolean withAnim) {
        mPreviewMaskView.setVisibility(View.INVISIBLE);
        if (withAnim) {
            mPreviewMaskView.startAnimation(getAnimation(false, false, mPreviewMaskView.getWidth(), 0, 200L));
        }
        updateTitleAndOperateBarState(true);

        if (mThumbnailModeOfWallpaperBeforePreview) {
            mWallpaperView.setScanMode(mThumbnailModeOfWallpaperBeforePreview);
        } else {
            updateSliderViewState(true);
        }
    }

    private boolean isVisiableImagePath(String localPath) {
        return pointSameImage(getAdjResource(0), localPath) 
                || pointSameImage(getAdjResource(1), localPath) 
                || pointSameImage(getAdjResource(-1), localPath);
    }

    private static boolean pointSameImage(Resource res, String decodedImagePath) {
        if (res != null) {
            if (TextUtils.equals(res.getLocalPath(), decodedImagePath)) {
                return true;
            } else {
                return TextUtils.equals(getLocalThumbnailCachePath(res), decodedImagePath);
            }
        }
        return false;
    }

    private static String getLocalThumbnailCachePath(Resource res) {
        List<String> list = (res != null) ? res.getLocalThumbnails() : null;
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private HorzontalSliderView.SliderMoveListener getSliderMoveListener() {
        return new HorzontalSliderView.SliderMoveListener() {
            public void movePercent(float movePercentFromCenter, boolean stopMove) {
                mWallpaperView.updateCurrentWallpaperShowingArea(movePercentFromCenter, stopMove);
            }
        };
    }

    private ImageCacheDecoder.ImageDecodingListener getImageDecodingListener() {
        return new ImageCacheDecoder.ImageDecodingListener() {
            public void handleDecodingResult(boolean result, String localPath, String onlinePath) {
                if (result) {
                    if (isVisiableImagePath(localPath)) {
                        initWallpaperViewBitmap();
                    }
                } else {
                    if (TextUtils.equals(mLocalPath, localPath)) {
                        Toast.makeText(WallpaperDetailActivity.this, 
                                R.string.wallpaper_decoded_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    private WallpaperView.WallpaperSwitchListener getWallpaperSwitchListener() {
        return new WallpaperView.WallpaperSwitchListener() {
            public void switchNext() {
                navigateToNextResource();
            }

            public void switchNone() {
            }

            public void switchPrevious() {
                navigateToPreviousResource();
            }
        };
    }

    private Animation getAnimation(boolean alphaAnim, boolean forEnter, int offsetX, int offsetY) {
        return getAnimation(alphaAnim, forEnter, offsetX, offsetY, 200);
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

    private String checkInfoValue(String str) {
        if (TextUtils.isEmpty(str)) {
            str = getString(R.string.description_missed);
        }
        return str;
    }

    class WallpaperGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            updateSliderViewState(mWallpaperView.isThumbnailScanMode());
            mWallpaperView.setScanMode(!mWallpaperView.isThumbnailScanMode());
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            int moveX = (int) -distanceX;
            mWallpaperView.horizontalMove(moveX);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //            boolean shouldVisiable = mTitleAreaView.getVisibility() != View.VISIBLE;
            //            updateTitleAndOperateBarState(shouldVisiable);
            //            autoUpdateSliderViewState(shouldVisiable);
            enterPreviewMode();
            return true;
        }
    }
}
