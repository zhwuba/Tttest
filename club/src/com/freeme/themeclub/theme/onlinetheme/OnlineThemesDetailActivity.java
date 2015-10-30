package com.freeme.themeclub.theme.onlinetheme;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.android.internal.util.AsyncImageCache;
import android.os.PowerManager;
import android.os.FileUtils;
import android.os.ServiceManager;

import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.MyGridView;
import com.freeme.themeclub.R;
import com.freeme.themeclub.ShareUtil;
import com.freeme.themeclub.YouLikeAdapter;
import com.freeme.themeclub.individualcenter.LockscreenInfo;
import com.freeme.themeclub.individualcenter.ThemeConstants;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.DownloadReceiver;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesFragment.OnlineThemesMyAdapter;
import com.freeme.themeclub.theme.onlinetheme.download.BackInstallService;
import com.freeme.themeclub.theme.onlinetheme.download.DownloadManagerHelper;
import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.PreferencesUtils;
import com.freeme.themeclub.theme.onlinetheme.util.ResultUtil;

import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
public class OnlineThemesDetailActivity extends Activity implements View.OnClickListener{

    private ArrayList<Map<String, Object>> mListData;
    public ArrayList<Map<String, Object>> mListData2;
    private AsyncImageCache mAsyncImageCache;   
    private DownloadManager mDownloadManager;
    private long mDownloadId;
    private DownloadManagerHelper mDownloadManagerHelper;
    private DownloadChangeObserver mDownloadObserver;
    private Map<String, Object> mThemeInfo;
    private PackageChangeReceiver mPackageChangeReceiver;
    private InstallReceiver mInstallReceiver;
    private DownloadReceiver completeReceiver;

    private final int DIALOG_THEME_IS_USING = 0;
    private final int DIALOG_UNINSTALL = 1;
    private String mLocalFilePath = null;
    private String mPackageName = null;

    private ProgressBar mProgressBar;
    private Handler mHandler;
    private ProgressDialog mDialog = null;
    private ProgressDialog mUninstallDialog = null;
    private ProgressDialog mInstallDialog = null;
    private RelativeLayout mDetailContent;
    private FrameLayout mErrorLayout;
    private LinearLayout mLoadingLayout;
    private ArrayList<String> mImageUrl;
    private ImageView[] mImageViews;
    private LinearLayout mDetailImagesLayout;
    private PreviewGallery mPreviewGallery;
    private LinearLayout mIntroLayout;
    private MyGridView youLikeGrid;
    private YouLikeAdapter mAdapter;

    private TextView themeName;
    private TextView themeSize;
    private TextView themeWriter;
    private TextView themeVersion;
    private TextView themeDownloadTimes;
    private TextView themeBrief;

    private ImageView shareImage;
    private ImageView deleteImage;
    private Button downloadBtn;
    private Button applyBtn;
    private TextView cancelBtn;
    private Button inUseBtn;

    private Configuration config = null;

    private AlertDialog mAlertDialog;
    private int mThemeListId;
    private int mListSize;
    private static final int MENU_ID_SHARE = Menu.FIRST;
    private static final int MENU_ID_DOWNLOAD = Menu.FIRST+1;
    private static final int MENU_ID_UNINSTALL = Menu.FIRST + 2;
    private static final int MENU_ID_INSTALL = Menu.FIRST + 3;
    private static final int MENU_ID_CANCLE = Menu.FIRST + 4;

    public static final String ACTION_DELETE_DOWNLOAD = "android.intent.action.DELETE_DOWNLOAD";
    public static final String ACTION_THEME_MANAGER_SERVICE = "freeme.intent.action.ThemeManagerService";

    private boolean isDownloaded;
    enum MenuState {
        MENU_STATE_NONE, MENU_STATE_DOWNLOADING, MENU_STATE_DOWNLOADED, MENU_STATE_INSTALLED, 
    }
    private MenuState mMenuState = MenuState.MENU_STATE_NONE;

    private boolean isOnlineLockscreen;

    private static final String LOCKSCREEN_PACKAGE = "tyd_lockscreen_package";
    private static final String LOCKSCREEN_DEFAULT = "android";

    public static final String KEY_THEME_LOCKSCREEN_FUN_UX_VALUE = "key_theme_lockscreen_fun_ux_value";
    private static final int MSG_LCOKSREEN_WALLPAPER_CHANGE = 10;

    public static final String FUN_UX_DIR = "fun_ux";
    public static final String FUN_UX_DEFAULT_NAME = "fun_ux.ux";
    public static final String FUN_UX_ASSET_NAME = "w.ux";
    private File mFunUXDir;

    private final String BiglauncherPackageName = "com.freeme.theme.bigluncher";

    private StatisticDBHelper mStatisticDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.theme_detail);
        mAsyncImageCache = AsyncImageCache.from(this);
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        mDownloadManagerHelper = new DownloadManagerHelper(mDownloadManager);
        mPackageChangeReceiver = new PackageChangeReceiver();
        mInstallReceiver = new InstallReceiver();
        completeReceiver = new DownloadReceiver();

        mStatisticDBHelper=StatisticDBHelper.getInstance(OnlineThemesDetailActivity.this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(OnlineThemesDetailActivity.ACTION_DELETE_DOWNLOAD);
        registerReceiver(completeReceiver, filter);

        config = getResources().getConfiguration();
        mListData = (ArrayList<Map<String, Object>>) getIntent()
                .getSerializableExtra("mlistData");
        mListData2 = new ArrayList<Map<String, Object>>();

        mThemeListId = getIntent().getIntExtra("list_id", 0);
        if(mListData != null){
            mThemeInfo = mListData.get(mThemeListId);
        }
        if(getIntent().getBooleanExtra("isOnlineLockscreen", false)){
            isOnlineLockscreen = true;
            setTitle(R.string.lockscreen_detail);
        }
        new GetOnlineThemeData().execute();
        setContentView(isOnlineLockscreen?
                R.layout.activity_detail_online_lockscreen:
                    R.layout.activity_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mHandler = new MyHandler();
        mDownloadObserver = new DownloadChangeObserver();
        mDownloadId = PreferencesUtils.getLong(this, ((Integer)mThemeInfo.get("id")).toString());
        mPackageName = (String)mThemeInfo.get("packageName");
        isDownloaded=(Boolean)mThemeInfo.get("isDownloaded");
        mLocalFilePath=OnlineThemesUtils.getSDPath()+"/themes/"+mThemeInfo.get("name")+".apk";
        findView();
        initListener();
        updateDetailsImageView(((List<String>)mThemeInfo.get("previewList")));

        if(isDownloaded){
            mMenuState = MenuState.MENU_STATE_INSTALLED;
        }else{
            mMenuState=MenuState.MENU_STATE_NONE;
        }
        updateBottomBar();
    }

    private void updateView(){
        int[] bytesAndStatus = mDownloadManagerHelper.getBytesAndStatus(mDownloadId);
        mHandler.sendMessage(mHandler.obtainMessage(0, bytesAndStatus[0],
                bytesAndStatus[1], bytesAndStatus[2]));
    }


    @Override
    public void onDestroy() {
        getContentResolver().unregisterContentObserver(mDownloadObserver);
        unregisterReceiver(mPackageChangeReceiver);
        unregisterReceiver(mInstallReceiver);

        unregisterReceiver(completeReceiver);
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    private void findView() {
        mLoadingLayout = (LinearLayout) findViewById(R.id.loadinglayout);
        mPreviewGallery = (PreviewGallery) findViewById(R.id.particular_info);
        mIntroLayout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.viewpager_firstpager, null);
        if(isOnlineLockscreen){
            mIntroLayout.findViewById(R.id.producers_layout).setVisibility(View.GONE);
        }
        themeName = (TextView) /*mIntroLayout.*/findViewById(R.id.themename_tv);
        themeName.setText((String)mThemeInfo.get("name"));
        themeSize = (TextView)/* mIntroLayout.*/findViewById(R.id.size_tv);       
        themeSize.setText(Formatter.formatFileSize(this, (Integer)(mThemeInfo.get("size"))));
        themeWriter = (TextView) /*mIntroLayout.*/findViewById(R.id.producers_tv);
        themeWriter.setText((String)mThemeInfo.get("author"));
        themeBrief = (TextView)findViewById(R.id.res_intro);
        themeBrief.setText((String)mThemeInfo.get("brief"));
        themeVersion = (TextView) mIntroLayout.findViewById(R.id.version_tv);
        //themeVersion.setText(Build.KBSTYLE);
        themeDownloadTimes = (TextView) /*mIntroLayout.*/findViewById(R.id.download_count_tv);
        themeDownloadTimes.setText(mThemeInfo.get("dnCnt")+"");
        mErrorLayout = (FrameLayout) findViewById(R.id.error_layout);
        mDetailContent = (RelativeLayout) findViewById(R.id.particular_info_detail_scrollview);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_id);

        shareImage=(ImageView) findViewById(R.id.share_image);
        deleteImage=(ImageView) findViewById(R.id.delete_image);
        downloadBtn=(Button) findViewById(R.id.download_button);
        applyBtn=(Button) findViewById(R.id.apply_button);
        cancelBtn=(TextView) findViewById(R.id.cancel_txt);
        inUseBtn=(Button) findViewById(R.id.in_use_button);
        youLikeGrid=(MyGridView) findViewById(R.id.theme_grid_youlike);
        youLikeGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                    long arg3) {
                Intent intent = new Intent(OnlineThemesDetailActivity.this,
                        OnlineThemesDetailActivity.class);
                if (null != mAdapter && null != mListData && mListData.size() > 0) {
                    intent.putExtra("list_id", position);
                    intent.putExtra("mlistData", mListData2);
                    intent.putExtra("isOnlineLockscreen", isOnlineLockscreen);

                    if(isOnlineLockscreen){
                        String name=mListData2.get(position).get("name").toString();
                        String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, 
                                LocalUtil.LOCKS_CLICK_YOULICK, name,System.currentTimeMillis());
                        mStatisticDBHelper.intserStatisticdataToDB(infoStr);
                    }else{
                        String name=mListData2.get(position).get("name").toString();
                        String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, 
                                LocalUtil.THEME_CLICK_YOULICK, name,System.currentTimeMillis());
                        mStatisticDBHelper.intserStatisticdataToDB(infoStr);
                    }
                }
                startActivity(intent);
                finish();

            }
        });
        if (mListSize == 0 || mThemeListId == mListSize - 1) {
            //mFuncSoftkey.setEnabled(false);
        }
    }

    private void initListener(){
        shareImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        downloadBtn.setOnClickListener(this);
        mProgressBar.setOnClickListener(this);
        applyBtn.setOnClickListener(this);
        inUseBtn.setOnClickListener(this);
        inUseBtn.setEnabled(false);
        mPreviewGallery.setOnClickListener(this);
        findViewById(R.id.particular_info_detail_scrollview).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateView();
        getContentResolver().registerContentObserver(
                DownloadManagerHelper.CONTENT_URI, true, mDownloadObserver);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mPackageChangeReceiver, filter);

        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(BackInstallService.ACTION_START_INSTALL);
        installFilter.addAction(BackInstallService.ACTION_INSTALL_FAIL);
        installFilter.addAction(BackInstallService.ACTION_INSTALL_SUCCESS);
        registerReceiver(mInstallReceiver, installFilter);

        updateView();
    }

    @Override
    protected void onPause() {
        super.onPause();

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

        mDialog.setMessage(getResources().getString(R.string.get_info));
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void cancelDialog() {
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.cancel();
        }
    }

    private void AppUninstall(String packageName, Context ctx) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        if(!OnlineThemesUtils.checkInstalled(this, packageName)){
            return;
        }

        mUninstallDialog = null;
        mUninstallDialog = new ProgressDialog(this);
        mUninstallDialog.setMessage(getResources().getString(
                R.string.uninstalling));
        mUninstallDialog.setIndeterminate(true);
        mUninstallDialog.setCancelable(false);
        mUninstallDialog.show();

        PackageManager pm = ctx.getPackageManager();
        PackageDeleteObserver observer = new PackageDeleteObserver();
        pm.deletePackage(packageName, observer, 0);
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            final int UNINSTALL_COMPLETE = 5;
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            mHandler.sendMessage(msg);
        }
    }

    private boolean isUsingTheme() {
        Configuration config = this.getResources().getConfiguration();
        List<PackageInfo> allApps = new ArrayList<PackageInfo>();
        PackageManager pmg = getPackageManager();
        allApps = pmg.getInstalledPackages(0);
        for (PackageInfo app : allApps) {
            if (app.packageName.equals(mPackageName)
                    ) {
                Resources.ThemeInfo themeInfo = this.getResources()
                        .getThemeInfo(app.applicationInfo.sourceDir,
                                app.packageName);
                if (themeInfo.themePath.equals(config.skin)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isUsingLockscreen() {
        String usingPackage = Settings.System.getString(getContentResolver(), "tyd_lockscreen_package");        if(mPackageName != null && mPackageName.equals(usingPackage)){
            return true;
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_THEME_IS_USING:
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    OnlineThemesDetailActivity.this);
            builder.setMessage(
                    getResources().getString(isOnlineLockscreen ? 
                            R.string.lockscreen_is_using : R.string.theme_is_using))
                            .setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
            return builder.create();
        case DIALOG_UNINSTALL:
            AlertDialog.Builder uninstallBuilder = new AlertDialog.Builder(
                    OnlineThemesDetailActivity.this);
            uninstallBuilder.setMessage(isOnlineLockscreen ? 
                    getResources().getString(R.string.uninstall_lockscreen_tips) : 
                        getResources().getString(R.string.uninstall_theme_tips))
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
                    if(!OnlineThemesUtils.checkSystemApp(OnlineThemesDetailActivity.this, mPackageName)){
                        AppUninstall(mPackageName, OnlineThemesDetailActivity.this);
                        Intent intent = new Intent(ACTION_DELETE_DOWNLOAD);
                        intent.putExtra("id", (Integer)mThemeInfo.get("id"));
                        sendBroadcast(intent);
                    }else{
                        Toast.makeText(OnlineThemesDetailActivity.this, getResources().getString(R.string.system_app_tips), Toast.LENGTH_SHORT).show();
                    }
                    if(mDownloadId != -1){
                        mDownloadManager.remove(mDownloadId);
                    }
                    updateView();
                }
            });
            return uninstallBuilder.create();
        default:
            break;

        }
        return super.onCreateDialog(id);
    }
    ArrayList<View> listViews;
    private void updateDetailsImageView(final List<String> previewList) {
        int i = 0;
        int count = previewList.size();
        RelativeLayout.LayoutParams lp;
        RelativeLayout layout;
        mImageViews = new ImageView[count];
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();
        listViews = new ArrayList<View>();
        for (i = 0; i < count; i++) {
            layout = (RelativeLayout) getLayoutInflater().inflate(
                    R.layout.viewpager_item_online_themes, null);
            ImageView mTempImageView = (ImageView) layout
                    .findViewById(R.id.viewpager_item_iv);
            mTempImageView.setBackgroundResource(R.drawable.theme_detail_default);
            mAsyncImageCache.displayImage(mTempImageView,
                    R.drawable.theme_detail_default,
                    new AsyncImageCache.NetworkImageGenerator(previewList.get(i),
                            previewList.get(i)));
            mImageViews[i] = mTempImageView;
            final int j = i;
            mTempImageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    displayPreview(j,previewList);
                }
            });
            listViews.add(layout);
        }
        PreviewGallery.PreviewGalleryPagerAdapter pagerAdapter=new PreviewGallery.PreviewGalleryPagerAdapter();
        pagerAdapter.setData(listViews);
        //        mPreviewGallery.removeAllViews();
        mPreviewGallery.setAdapter(pagerAdapter);

    }

    private void displayPreview(int position,List<String> previewList){
        final Dialog dialog = new Dialog(this,R.style.Dialog_Fullscreen);
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        dialog.getWindow().getAttributes().width = p.x;
        dialog.getWindow().getAttributes().height = p.y;
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(
                R.layout.layout_theme_preview, null);
        ViewPager viewPager = (ViewPager) layout.findViewById(R.id.theme_preview_viewpager);
        ArrayList<View> listViews = new ArrayList<View>();
        for(int i = 0;i<previewList.size();i++){
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    dialog.cancel();
                }
            });
            mAsyncImageCache.displayImage(imageView,
                    R.drawable.theme_detail_default,
                    new AsyncImageCache.NetworkImageGenerator(previewList.get(i),
                            previewList.get(i)));
            listViews.add(imageView);
        }
        PreviewGallery.PreviewGalleryPagerAdapter pagerAdapter = 
                new PreviewGallery.PreviewGalleryPagerAdapter();
        pagerAdapter.setData(listViews);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);

        dialog.setContentView(layout);
        dialog.show();
    }



    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
            case 0:
                int status = (Integer) msg.obj;
                if (DownloadManagerHelper.isDownloading(status)) {
                    downloadBtn.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    cancelBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress(0);
                    mMenuState = MenuState.MENU_STATE_DOWNLOADING;

                    if (msg.arg2 >= 0) {
                        mProgressBar.setMax(msg.arg2);
                        mProgressBar.setProgress(msg.arg1);
                    }
                } else {
                    //                    mProgressBar.setVisibility(View.GONE);
                    //                    cancelBtn.setVisibility(View.GONE);
                    //                    downloadBtn.setVisibility(View.VISIBLE);


                    if (status == DownloadManager.STATUS_FAILED) {
                        mMenuState = MenuState.MENU_STATE_NONE;
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        mProgressBar.setMax(100);
                        mProgressBar.setProgress(100);
                        //                        isDownloaded=true;
                        if(isDownloaded){
                            mMenuState = MenuState.MENU_STATE_DOWNLOADED;
                        }/*else{
                            mMenuState = MenuState.MENU_STATE_NONE;
                        }*/

                        if(mLocalFilePath == null || mPackageName==null){
                            mLocalFilePath = OnlineThemesUtils.getSDPath()+"/themes/"+mThemeInfo.get("name")+".apk";
                            mPackageName = OnlineThemesUtils.getApkFileInfo(mLocalFilePath, OnlineThemesDetailActivity.this);
                        }
                        if(OnlineThemesUtils.checkInstalled(OnlineThemesDetailActivity.this, mPackageName)){
                            mMenuState = MenuState.MENU_STATE_INSTALLED;
                        }
                    } else {
                        if(OnlineThemesUtils.checkInstalled(OnlineThemesDetailActivity.this, mPackageName)){
                            if(isDownloaded){
                                mMenuState = MenuState.MENU_STATE_INSTALLED;
                            }else{
                                mMenuState = MenuState.MENU_STATE_NONE;
                            }

                        }else{
                            mMenuState = MenuState.MENU_STATE_NONE;
                        }
                    }
                }
                updateBottomBar();
                break;
            case 10:
                Intent intent = new Intent("WallpaperManager.ACTION_LOCKSCREEN_WALLPAPER_CHANGED");
                sendBroadcast(intent);
                break;
            case 5:
                updateView();
            }

        }
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(mHandler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

    }

    class PackageChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();;
            if(packageName.equals(mPackageName)){
                if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
                    if(mUninstallDialog!=null&&mUninstallDialog.isShowing()){
                        mUninstallDialog.dismiss();
                    }
                }
                updateView();				 
            }			
        }    	
    }

    class InstallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent intent) {
            long downloadId = intent.getLongExtra("downloadId", -1);
            String action = intent.getAction();
            if(downloadId == mDownloadId){
                if(action.equals(BackInstallService.ACTION_START_INSTALL)){
                    mInstallDialog = null;
                    mInstallDialog = new ProgressDialog(OnlineThemesDetailActivity.this);
                    mInstallDialog.setMessage(getResources().getString(
                            R.string.online_installing));
                    mInstallDialog.setIndeterminate(true);
                    mInstallDialog.setCancelable(false);
                    mInstallDialog.show();
                }else if(action.equals(BackInstallService.ACTION_INSTALL_SUCCESS)){
                    if(mInstallDialog!=null && mInstallDialog.isShowing()){
                        mInstallDialog.dismiss();
                        mMenuState = MenuState.MENU_STATE_INSTALLED;
                        updateBottomBar();
                    }
                    //                    getContentResolver().delete(ThemeDownload.URI, ThemeDownload.DOWNLOAD_ID+"="+downloadId,null);
                    //                    deleteAPK(mLocalFilePath);

                }else{
                    if(mInstallDialog!=null && mInstallDialog.isShowing()){
                        mInstallDialog.dismiss();
                    }
                }
                updateView();
            }			
        }

    }

    private void deleteAPK(String path){
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.share_image:
            ShareUtil.shareText(this, isOnlineLockscreen?getResources().getString(
                    R.string.share_lockscreen_message_left)+Build.getFreemeOSLabel()+
                    mThemeInfo.get("name")+getResources().getString(
                            R.string.share_lockscreen_message_right):getResources().getString(
                                    R.string.share_theme_message_left)+Build.getFreemeOSLabel()+
                                    mThemeInfo.get("name")+getResources().getString(
                                            R.string.share_theme_message_right));

            if(isOnlineLockscreen){
                String name=mThemeInfo.get("name").toString();
                String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.LOCKS_CLICK_SHARE, name,System.currentTimeMillis());
                mStatisticDBHelper.intserStatisticdataToDB(infoStr);
            }else{
                String name=mThemeInfo.get("name").toString();
                String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.THEME_CLICK_SHARE, name,System.currentTimeMillis());
                mStatisticDBHelper.intserStatisticdataToDB(infoStr);
            }
            break;
        case R.id.download_button:
            if(OnlineThemesUtils.getSDPath() == null){
                Toast.makeText(this, R.string.cardException, Toast.LENGTH_SHORT).show();
            }
            File folder = new File("themes");
            if (!folder.exists() || !folder.isDirectory()) {
                folder.mkdirs();
            }

            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse((String)mThemeInfo.get("dnUrl")));
            request.setDestinationInExternalPublicDir("themes",
                    mThemeInfo.get("name")+".apk");
            request.setTitle((String)mThemeInfo.get("name"));
            request.setDescription(mThemeInfo.get("name")+"");
            //            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION);
            request.setVisibleInDownloadsUi(false);

            request.setMimeType("application/vnd.android.package-archive");
            mDownloadId = mDownloadManager.enqueue(request);
            PreferencesUtils.putLong(this, ((Integer)mThemeInfo.get("id")).toString(),
                    mDownloadId);

            ContentValues values = new ContentValues();
            values.put(ThemeDownload.NAME, mThemeInfo.get("name")+"");
            values.put(ThemeDownload.DOWNLOAD_ID,mDownloadId);
            values.put(ThemeDownload.IS_THEME,isOnlineLockscreen?0:1);
            values.put(ThemeDownload.THEME_ID,Integer.parseInt(mThemeInfo.get("id")+""));
            values.put(ThemeDownload.PATH, OnlineThemesUtils.getSDPath()+"/themes/"+mThemeInfo.get("name")+".apk");
            values.put(ThemeDownload.URL, mThemeInfo.get("dnUrl")+"");
            values.put(ThemeDownload.PACKAGE_NAME, mPackageName);
            getContentResolver().insert(ThemeDownload.URI, values);

            updateView();
            OnlineThemesUtils.postDownloadTimes(Integer.parseInt(mThemeInfo.get("id")+""), isOnlineLockscreen?0:1);

            break;
        case R.id.delete_image:  
            if (isUsingTheme() || isUsingLockscreen()) {
                showDialog(DIALOG_THEME_IS_USING);
            }else{
                showDialog(DIALOG_UNINSTALL);
            }
            break;
        case R.id.apply_button:
            onApply();
            break;

        case R.id.progressbar_id:
            mDownloadManager.remove(mDownloadId);
            getContentResolver().delete(ThemeDownload.URI, 
                    ThemeDownload.DOWNLOAD_ID+"="+mDownloadId,null);
            deleteAPK(mLocalFilePath);
            updateView();
            break;
        }

    }

    public File saveMyBitmap(Bitmap bitmap,String bitName){ 
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/themeclub";
        File dir = new File(file_path);
        if (!dir.exists()) 
            dir.mkdirs();
        File f = new File(dir, "to_share.png");

        FileOutputStream fOut = null;  
        try {  
            f.createNewFile();
            fOut = new FileOutputStream(f); 
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        } catch (Exception e) {  
            e.printStackTrace(); 
        }  

        try { 
            fOut.flush();
            fOut.close();  
        } catch (Exception e) {  
            e.printStackTrace(); 
        } 
        return f;
    } 

    private void chmodFileAccess(String filePath) {
        FileUtils.setPermissions(
                filePath,
                FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IRWXO,
                -1, -1);
    }

    private void initFunUXDir() {
        try {
            if (mFunUXDir.exists()) {
                mFunUXDir.delete();
                Process p = Runtime.getRuntime().exec("rm -rf " + mFunUXDir.getAbsolutePath());
                p.waitFor();
            }

            mFunUXDir.mkdirs();
            chmodFileAccess(mFunUXDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean extractUXObject(Context context, String packageName, String assetName,
            String uxPath, String uxName) {

        ContentResolver resolver = context.getContentResolver();
        Settings.System.putString(resolver, KEY_THEME_LOCKSCREEN_FUN_UX_VALUE, "");

        initFunUXDir(); 

        String fileName = uxPath + File.separator + uxName;
        Log.w("yzy_fileName", "fileName"+fileName);

        try {
            Context mPackageContext = context.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY);
            InputStream is = mPackageContext.getResources().getAssets()
                    .open(assetName);
            FileOutputStream fos = new FileOutputStream(fileName);
            byte[] buffer = new byte[10240];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        chmodFileAccess(fileName);

        return true;
    }

    class ApplyLockscreenTask extends AsyncTask<LockscreenInfo, String, String>{  
        boolean mCanceled = false;  
        ProgressDialog dialog;  
        String message;
        boolean uxLockscreen = false;

        public ApplyLockscreenTask(Context context){  
            message =context.getString(R.string.apply);
            dialog = new ProgressDialog(context);  
            dialog.setMessage(message + "...");
            dialog.setCancelable(false);  
            dialog.show();  
        }  

        @Override  
        protected String doInBackground(LockscreenInfo... pramas) {  
            LockscreenInfo lockscreenInfo = pramas[0];  
            publishProgress(lockscreenInfo.getTitle());

            mFunUXDir = new File(getFilesDir(), FUN_UX_DIR);

            // extract ux
            String uxPath = mFunUXDir.getAbsolutePath();
            String uxName = FUN_UX_DEFAULT_NAME;
            uxLockscreen = extractUXObject(OnlineThemesDetailActivity.this, lockscreenInfo.getPackageName(), FUN_UX_ASSET_NAME, uxPath, uxName);

            // wallpaper
            Bitmap bitmap = lockscreenInfo.getLockscreenWallpaper();

            if (bitmap !=null)
            {
                try {
                    WallpaperManager mWallpaperManager = WallpaperManager
                            .getInstance(OnlineThemesDetailActivity.this);

                    mWallpaperManager.setLockscreenBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                mHandler.sendEmptyMessage(MSG_LCOKSREEN_WALLPAPER_CHANGE);
            }
            return null;  
        }

        @Override
        protected void onProgressUpdate(String... values) {
            dialog.setMessage(message + " " + values[0]);
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(String result){   
            if(dialog!=null){
                dialog.dismiss();
            }
            if(mAdapter!=null){
                mAdapter.notifyDataSetInvalidated();
            }
//            locknow();
        }  
    } 

    private void locknow() {
        /*IPowerManager mIPowerManager = IPowerManager.Stub
                .asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        try {
            mIPowerManager.goToSleep(SystemClock.uptimeMillis(), 0);
        } catch (RemoteException localRemoteException) {
            //            Log.w(TAG, localRemoteException.toString());
        }*/

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);  
        pm.goToSleep(SystemClock.uptimeMillis()); 
    }
    public String getLockscreenPackage() {
        if(this == null){
            return LOCKSCREEN_DEFAULT;
        }
        String result = Settings.System.getString(getContentResolver(), LOCKSCREEN_PACKAGE);
        return android.text.TextUtils.isEmpty(result) ? LOCKSCREEN_DEFAULT
                : result;
    }

    public void onApply() {
        if(isOnlineLockscreen){
            LockscreenInfo lockscreenInfo = new LockscreenInfo(this, mPackageName, mLocalFilePath);
            String lockscreenPackage = lockscreenInfo.getPackageName();
            if(getLockscreenPackage().equals(lockscreenPackage)){
                Toast.makeText(this, R.string.in_use, Toast.LENGTH_SHORT).show();
                return ;
            }
            Settings.System.putString(getContentResolver(),
                    LOCKSCREEN_PACKAGE, lockscreenPackage);
            new ApplyLockscreenTask(this).execute(lockscreenInfo);
        }else{
            sendBroadcast(
                    new Intent("com.android.soundRecorder.command.state.chanage"));
            sendBroadcast(
                    new Intent("com.android.soundrecorder.command.theme.change"));
            if (config != null) {
                if( !config.skin.equals(getThemePath(mPackageName))){
                    Intent intent = new Intent(ACTION_THEME_MANAGER_SERVICE);
                    intent.putExtra("action", "change_theme");
                    intent.putExtra("package_name", mPackageName);
                    intent.putExtra("theme_path", getThemePath(mPackageName));
                    intent.setPackage("com.freeme.themeclub.core");
                    startService(intent);

                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            setResult(10000);
                            finish();
                        }
                    }, 500);
                }else{
                    Toast.makeText(this, R.string.theme_in_use, Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    private String getThemePath(String packageName){
        String themePath = null;
        Cursor cursor = getContentResolver().query(ThemeConstants.CONTENT_URI, 
                new String[]{ ThemeConstants.THEME_PATH },
                ThemeConstants.PACKAGE_NAME + " = ?", new String[] { packageName },
                null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(ThemeConstants.THEME_PATH);
            themePath = cursor.getString(index);
        }
        cursor.close();
        return themePath;
    }

    private boolean canUninstallTheme(String packageName) {
        if ("android".equals(packageName) || BiglauncherPackageName.equals(packageName)) {
            return false;
        }
        Configuration configuration = getResources().getConfiguration();
        List<PackageInfo> allApps = new ArrayList<PackageInfo>();
        PackageManager packageManager = getPackageManager();
        allApps = packageManager.getInstalledPackages(0);
        for (PackageInfo app : allApps) {

            if (!app.packageName.equals(packageName))
                continue;

            if ((app.applicationInfo.flags & app.applicationInfo.FLAG_SYSTEM) > 0) {  
                return false;
            }  

            /*if (app.isThemePackage == 1) {
                Resources.ThemeInfo themeInfo = getResources().getThemeInfo(
                        app.applicationInfo.sourceDir, app.packageName);
                if (themeInfo.themePath.equals(configuration.skin)) {
                    return false;
                }
            }*/
        }
        return true;
    }

    private void updateBottomBar(){
        if(mMenuState == MenuState.MENU_STATE_NONE){
            downloadBtn.setVisibility(View.VISIBLE);
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
            applyBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            inUseBtn.setVisibility(View.GONE); 
        }else if(mMenuState == MenuState.MENU_STATE_DOWNLOADING){
            downloadBtn.setVisibility(View.GONE);
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
            applyBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.VISIBLE);
            inUseBtn.setVisibility(View.GONE);
        }else if(mMenuState == MenuState.MENU_STATE_DOWNLOADED){
            downloadBtn.setVisibility(View.GONE);
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
            applyBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.VISIBLE);
            inUseBtn.setVisibility(View.GONE);
            //            applyBtn.setVisibility(View.VISIBLE);
            //            downloadBtn.setVisibility(View.GONE);
            //            deleteImage.setEnabled(true);
            //            deleteImage.setImageResource(R.drawable.tab_delete);
            //            cancelBtn.setVisibility(View.GONE);
            //            inUseBtn.setVisibility(View.GONE);
        }else if(mMenuState == MenuState.MENU_STATE_INSTALLED){
            applyBtn.setVisibility(View.VISIBLE);
            downloadBtn.setVisibility(View.GONE);
            deleteImage.setEnabled(true);
            deleteImage.setImageResource(R.drawable.tab_delete);
            cancelBtn.setVisibility(View.GONE);
            inUseBtn.setVisibility(View.GONE);
        }else{
            downloadBtn.setVisibility(View.GONE);
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
            applyBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            inUseBtn.setVisibility(View.VISIBLE);
        }
        if(!canUninstallTheme(mPackageName)){
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
        }
    }

    public class GetOnlineThemeData extends
    AsyncTask<Object, Object, List<Map<String, Object>>> {
        protected List<Map<String, Object>> doInBackground(Object... params) {
            String result = null;
            List<Map<String, Object>> list = null;
            try {
                JSONObject paraInfo = new JSONObject();
                paraInfo.put("mf", "");
                paraInfo.put("lcd", OnlineThemesUtils
                        .getAvailableResolutionForThisDevice(
                                OnlineThemesDetailActivity.this,
                                getResources().getStringArray(
                                        R.array.resolution_array))[0]);
                paraInfo.put("ver", OnlineThemesFragment.THEME_VERSION);
                if(isOnlineLockscreen){
                    paraInfo.put("ver", "v600");
                }
                paraInfo.put("type", "01");
                // paraInfo.put("dev", "X9");
                paraInfo.put("sort", "01");
                paraInfo.put("bout", "0");
                paraInfo.put("from", 0);
                paraInfo.put("to",3);
                paraInfo.put("subType", mThemeInfo.get("subType"));

                JSONObject jsObject = new JSONObject();
                if(isOnlineLockscreen){
                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_LOCKSCREEN_LIST_BY_TAG_REQ));
                }else{
                    jsObject.put("head", NetworkUtil.buildHeadData(MessageCode.GET_THEME_LIST_BY_TAG_REQ));
                }
                jsObject.put("body", paraInfo.toString());
                String contents = jsObject.toString();
                String url = MessageCode.SERVER_URL;
                result = NetworkUtil.accessNetworkByPost(url, contents);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (result != null) {
                if (isOnlineLockscreen) {
                    list = ResultUtil.splitLockScreenServerListData(result);
                } else{
                    list = ResultUtil.splitThemeServerListData(result);
                } 
            }

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    long downloadId = PreferencesUtils.getLong(OnlineThemesDetailActivity.this,
                            ((Integer) list.get(i).get("id")).toString());
                    list.get(i).put(
                            "isDownloaded",
                            OnlineThemesUtils.checkInstalled(OnlineThemesDetailActivity.this,
                                    (String) list.get(i).get("packageName")));
                }
            }
            return list;
        }

        protected void onPostExecute(List<Map<String, Object>> list) {
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    mListData2.add(list.get(i));
                }
                mAdapter = new YouLikeAdapter(OnlineThemesDetailActivity.this, mListData2);
                youLikeGrid.setAdapter(mAdapter);
                super.onPostExecute(list);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("yzy", "----------------------------------ACTION_DOWNLOAD_COMPLETE");
            Log.w("yzy", intent.getAction());
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                long completeDownloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (mAdapter != null && mListData2 != null
                        && mListData2.size() > 0)
                    for (Map<String, Object> map : mListData2) {
                        long downloadId = PreferencesUtils.getLong(OnlineThemesDetailActivity.this,
                                ((Integer) map.get("id")).toString());

                        Log.w("yzy", "completeDownloadId==="+completeDownloadId);
                        Log.w("yzy", "downloadId==="+downloadId);
                        if (downloadId == completeDownloadId) {
                            if (hasDownloaded(downloadId)) {
                                map.put("isDownloaded", true);
                                mAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                if (mAdapter != null && mListData != null
                        && mListData.size() > 0)
                    for (Map<String, Object> map : mListData) {
                        long downloadId = PreferencesUtils.getLong(OnlineThemesDetailActivity.this,
                                ((Integer) map.get("id")).toString());

                        //                        Log.w("yzy", "completeDownloadId==="+completeDownloadId);
                        //                        Log.w("yzy", "downloadId==="+downloadId);
                        if (downloadId == completeDownloadId) {
                            //                            Log.w("yzy", "downloadId == completeDownloadId");
                            if (hasDownloaded(downloadId)) {
                                map.put("isDownloaded", true);
                                mAdapter.notifyDataSetChanged();
                                mMenuState=MenuState.MENU_STATE_DOWNLOADED;
                                updateBottomBar();
                            }
                            break;
                        }
                    }

            } else {
                int id = intent.getIntExtra("id", -1);
                for (Map<String, Object> map : mListData2) {
                    if ((Integer) map.get("id") == id) {
                        map.put("isDownloaded", false);
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    private boolean hasDownloaded(long downloadId) {
        int status = mDownloadManagerHelper.getStatusById(downloadId);
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            return true;
        } else {
            return false;
        }
    }


}
