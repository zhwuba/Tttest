package com.freeme.themeclub.individualcenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.util.AsyncImageCache;
import com.freeme.themeclub.R;
import com.freeme.themeclub.ShareUtil;
import com.freeme.themeclub.theme.onlinetheme.PreviewGallery;

import android.text.format.Formatter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.support.v4.view.ViewPager;

public class LockscreenDetailActivity extends Activity implements View.OnClickListener{
    public final static String PACKAGENAME = "com.freeme.thememanager.packageName";
    public final static String LOCKSCREENPATH = "com.freeme.thememanager.lockscreenPath";
    public final static String LOCKSCREENTITLE = "com.freeme.thememanager.lockscreentitle";
    public final static String SELECTPOSITION = "com.freeme.thememanager.position";
    private static final String LOCKSCREEN_PACKAGE = "tyd_lockscreen_package";
    private static final String LOCKSCREEN_DEFAULT = "android";
    public final static String APPLY_INDEX = "lockscreen.apply";
    public final static int APPLY_OK = 1;
    private final int MENU_APPLY = Menu.FIRST;

    private PreviewGallery previewGallery;
    private String[] packageNames;
    private String[] lockscreenPaths;
    private String[] lockscreenTitles;
    private int selectPosition;

    private LinearLayout lockscreenInfo;
    private TextView lockscreenName;
    private TextView lockscreenSize;
    private TextView lockscreenVersion;
    private TextView lockscreenAuthor;
    private TextView lockscreenIntro;

    private ImageView shareImage;
    private ImageView deleteImage;
    private Button applyBtn;
    private Button inUseBtn;

    private AsyncImageCache mAsyncImageCache;
    private boolean mCanUninstall = false;
    private ProgressDialog mUninstallDialog;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAsyncImageCache = AsyncImageCache.from(this);
        this.getActionBar().setDisplayShowHomeEnabled(false);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_lockscreen_detail);
        setTitle(R.string .lockscreen_detail);
        findViews();
        initListener();
        Intent intent = getIntent();
        packageNames = intent.getStringArrayExtra(PACKAGENAME);
        lockscreenPaths = intent.getStringArrayExtra(LOCKSCREENPATH);
        lockscreenTitles = intent.getStringArrayExtra(LOCKSCREENTITLE);
        selectPosition = intent.getIntExtra(SELECTPOSITION, 0);
        mCanUninstall = canUninstallTheme(packageNames[selectPosition]);
        updatePreview();

        updateBottomBar();
    }

    private void findViews() {
        previewGallery = (PreviewGallery) findViewById(R.id.particular_info);
        lockscreenName = (TextView)findViewById(R.id.lockscreen_name_tv);
        lockscreenSize = (TextView)findViewById(R.id.lockscreen_size_tv);
        //        lockscreenVersion = (TextView) lockscreenInfo
        //                .findViewById(R.id.lockscreen_version_tv);
        lockscreenAuthor=(TextView) findViewById(R.id.producers_tv);
        lockscreenIntro=(TextView) findViewById(R.id.res_intro);
        shareImage=(ImageView) findViewById(R.id.share_image);
        deleteImage=(ImageView) findViewById(R.id.delete_image);
        applyBtn=(Button) findViewById(R.id.apply_button);
        inUseBtn=(Button) findViewById(R.id.in_use_button);
    }


    private void initListener(){
        shareImage.setOnClickListener(this);
        deleteImage.setOnClickListener(this);
        applyBtn.setOnClickListener(this);
        inUseBtn.setOnClickListener(this);
        inUseBtn.setEnabled(false);
    }


    private void updatePreview() {
        //mTitlebarTitle.setText(lockscreenTitles[selectPosition]);
        setPreviewAdapter();
    }

    @Override
    public void onDestroy() {
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void setPreviewAdapter() {
        ArrayList<View> dataList = new ArrayList<View>();
        getInfoValues();
        //dataList.add(lockscreenInfo);
        RelativeLayout pageLayout = (RelativeLayout) getLayoutInflater()
                .inflate(R.layout.theme_preview_gallery_item, null);
        ImageView imageView = (ImageView) pageLayout
                .findViewById(R.id.viewpager_item_iv);
        //imageView.setImageDrawable(new LockscreenInfo(this, packageNames[selectPosition],
        //        lockscreenPaths[selectPosition]).getPreview());
        LockscreenInfo info = new LockscreenInfo(this, packageNames[selectPosition], lockscreenPaths[selectPosition]);
        if(info.getPreview()==null){
            mAsyncImageCache.displayImage(imageView, R.drawable.theme_detail_default,
                    new AsyncImageCache.GeneralImageGenerator(
                            info.getPackagePath() + "_preview", 
                            BitmapFactory.decodeResource(getResources(), R.drawable.theme_detail_default)));

        }else{
            mAsyncImageCache.displayImage(imageView, R.drawable.theme_detail_default,
                    new AsyncImageCache.GeneralImageGenerator(
                            info.getPackagePath() + "_preview", info.getPreview().getBitmap()));
        }
        final ArrayList<String> previewList = new ArrayList<String>();
        previewList.add(info.getPackagePath() + "_preview");
        imageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                displayPreview(0,previewList);
            }
        });
        dataList.add(pageLayout);
        previewGallery.setAdapter(new PreviewGallery.PreviewGalleryPagerAdapter(dataList));
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

    private void getInfoValues() {
        lockscreenName.setText(lockscreenTitles[selectPosition]);
        lockscreenAuthor.setText(R.string.no_name);
        lockscreenIntro.setText(lockscreenTitles[selectPosition]);
        File file = new File(lockscreenPaths[selectPosition]);
        if (file != null) {
            lockscreenSize
            .setText(Formatter.formatFileSize(this, file.length()));
        }
        //        lockscreenVersion.setText(Build.VERSION.FREEMEOS);
    }

    private void updateBottomBar(){
        if(mCanUninstall){
            deleteImage.setEnabled(true);
            deleteImage.setImageResource(R.drawable.tab_delete);
        }else{
            deleteImage.setEnabled(false);
            deleteImage.setImageResource(R.drawable.delete_cannot_use_theme);
        }
    }


    private boolean canUninstallTheme(String packageName) {
        if ("android".equals(packageName)) {
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

            if(getLockscreenPackage().equals(packageNames[selectPosition])){
               return false;
            }
        }
        return true;
    }

    public String getLockscreenPackage() {
        if(this == null){
            return LOCKSCREEN_DEFAULT;
        }
        String result = Settings.System.getString(getContentResolver(), LOCKSCREEN_PACKAGE);
        return android.text.TextUtils.isEmpty(result) ? LOCKSCREEN_DEFAULT
                : result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case android.R.id.home:
            finish();
            break;
        case R.id.share_image:
            ShareUtil.shareText(this, getResources().getString(R.string.share_lockscreen_message_left)+
                    Build.getFreemeOSLabel()+lockscreenTitles[selectPosition]+getResources().getString(R.string.share_lockscreen_message_right));
            break;
        case R.id.apply_button:
            if(getLockscreenPackage().equals(packageNames[selectPosition])){
                Toast.makeText(this, R.string.in_use, Toast.LENGTH_SHORT).show();
            }else{
                sendBroadcast(new Intent(
                        "com.android.soundrecorder.command.theme.change"));
                sendBroadcast(new Intent("com.android.music.command.theme.change"));
                Intent intent = new Intent();
                intent.putExtra(APPLY_INDEX, selectPosition);
                setResult(APPLY_OK, intent);
                finish();
            }
            break;
        case R.id.delete_image:
            if(getLockscreenPackage().equals(packageNames[selectPosition])){
                Toast.makeText(this, R.string.lockscreen_is_using, Toast.LENGTH_SHORT).show();
            }else{
                Builder builder = new Builder(this);
//                builder.setTitle(getResources().getString(R.string.delete_lockscreen));
                builder.setMessage(getResources().getString(R.string.uninstall_lockscreen_tips)
                        /*+ " " + lockscreenTitles[selectPosition]*/);
                builder.setPositiveButton(
                        getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mUninstallDialog = new ProgressDialog(
                                        LockscreenDetailActivity.this);
                                mUninstallDialog.setMessage(getResources()
                                        .getString(R.string.deleting));
                                mUninstallDialog.setIndeterminate(true);
                                mUninstallDialog.setCancelable(false);
                                mUninstallDialog.show();
                                PackageManager pManager = getPackageManager();
                                pManager.deletePackage(packageNames[selectPosition],
                                        new IPackageDeleteObserver.Stub() {
                                    public void packageDeleted(
                                            String packageName,
                                            int returnCode) {
                                        mUninstallDialog.cancel();
                                        finish();
                                    }
                                }, 0);
                            }
                        });
                builder.setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAlertDialog.dismiss();
                            }
                        });
                mAlertDialog = builder.create();
                mAlertDialog.show();
            }

        default:
            break;
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
}
