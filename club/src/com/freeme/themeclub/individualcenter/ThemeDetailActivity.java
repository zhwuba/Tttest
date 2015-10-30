package com.freeme.themeclub.individualcenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.AsyncImageCache;
import com.freeme.themeclub.R;
import com.freeme.themeclub.ShareUtil;
import com.freeme.themeclub.theme.onlinetheme.OnlineThemesDetailActivity;
import com.freeme.themeclub.theme.onlinetheme.PreviewGallery;
import android.support.v4.view.ViewPager;

import android.content.pm.IPackageDeleteObserver;

import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;

public class ThemeDetailActivity extends Activity implements View.OnClickListener{

    public final static String THEMENAME = "com.freeme.thememanager.themeName";
    public final static String PACKAGENAME = "com.freeme.thememanager.packageName";
    public final static String THEMEPATH = "com.freeme.thememanager.themePath";
    public final static String THEMEAUTHOR = "com.freeme.thememanager.themeAuthor";
    public final static String THEMEDESCRIPTION = "com.freeme.thememanager.themedecription";
    public final static String SELECTPOSITION = "com.freeme.thememanager.position";
    public final static String APPLY = "com.freeme.thememanager.apply";
    public static final String ACTION_DELETE_DOWNLOAD = "android.intent.action.DELETE_DOWNLOAD";
    private final String BiglauncherPackageName = "com.freeme.theme.bigluncher";

    private String[] previewNames = { Resources.THEME_PREVIEW_LAUNCHER,
            Resources.THEME_PREVIEW_LOCKSCREEN, Resources.THEME_PREVIEW_ICON,
            Resources.THEME_PREVIEW_APP,
            Resources.THEME_PREVIEW_OTHER_PREFIX + "01",
            Resources.THEME_PREVIEW_OTHER_PREFIX + "02",
            Resources.THEME_PREVIEW_OTHER_PREFIX + "03",
            Resources.THEME_PREVIEW_OTHER_PREFIX + "04",
            Resources.THEME_PREVIEW_OTHER_PREFIX + "05" };

    private String[] mThemeName;
    private String[] mPackageName;
    private String[] mThemePath;
    private String[] mThemeAuthor;
    private String[] mThemeDescription;
    private int mSelectedPos;

    private TextView themeName;
    private TextView themeSize;
    private TextView themeWriter;
    private TextView themeDiscription;
    private TextView themeVersion;

    private ImageView shareImage;
    private ImageView deleteImage;
    private Button applyBtn;
    private Button inUseBtn;

    private boolean mCanUninstall = false;
    private ProgressDialog mUninstallDialog;
    private AlertDialog mAlertDialog;
    private final static int MENU_ID_APPLY = Menu.FIRST;
    private final static int MENU_ID_DELETE = Menu.FIRST + 1;
    private final static int MENU_ID_SHARE = Menu.FIRST + 2;

    private AsyncImageCache mAsyncImageCache;
    private PreviewGallery mPreviewGallery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getActionBar().setDisplayShowHomeEnabled(false);
        this.getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_theme_detail);
        mAsyncImageCache = AsyncImageCache.from(this);
        findViews();
        initListener();

        Intent intent = getIntent();
        mThemeName = intent.getStringArrayExtra(THEMENAME);
        mPackageName = intent.getStringArrayExtra(PACKAGENAME);
        mThemePath = intent.getStringArrayExtra(THEMEPATH);
        mThemeAuthor = intent.getStringArrayExtra(THEMEAUTHOR);
        mSelectedPos = intent.getIntExtra(SELECTPOSITION, 0);
        mThemeDescription = intent.getStringArrayExtra(THEMEDESCRIPTION);

        mCanUninstall = canUninstallTheme(mPackageName[mSelectedPos]);

        setPreviewGalleryData(mSelectedPos);
        
        updateBottomBar();
    }

    private void findViews() {
        mPreviewGallery = (PreviewGallery) findViewById(R.id.particular_info);
        themeName = (TextView)findViewById(R.id.themename_tv);
        themeSize = (TextView)findViewById(R.id.size_tv);
        themeWriter = (TextView) findViewById(R.id.producers_tv);
        themeDiscription = (TextView) findViewById(R.id.res_intro);
        themeVersion = (TextView)findViewById(R.id.version_tv);

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

    private void setPreviewGalleryData(int pos) {

        setThemeIntroValues(pos);
        ArrayList<View> previewGalleryData = new ArrayList<View>();
        final ArrayList<String> previewList = new ArrayList<String>();
        BitmapDrawable drawable = null;
        int j = 0;
        for (int i = 0; i < 3; i++,j++) {
            drawable = getResources().getThemePreview(mThemePath[pos], previewNames[i]);
            if (drawable != null) {
                previewList.add(mThemePath[pos] + previewNames[i]);
                RelativeLayout pageLayout = (RelativeLayout) getLayoutInflater()
                        .inflate(R.layout.theme_preview_gallery_item, null);
                ImageView pageView = (ImageView) pageLayout
                        .findViewById(R.id.viewpager_item_iv);
                pageView.setImageDrawable(drawable);
                final int k = j;
                pageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        displayPreview(k,previewList);
                    }
                });
                previewGalleryData.add(pageLayout);
                mAsyncImageCache.displayImage(pageView, R.drawable.theme_detail_default,
                        new AsyncImageCache.ImageGenerator<BitmapDrawable>(
                                mThemePath[pos]+previewNames[i], drawable) {

                    @Override
                    public Bitmap generate() {
                        return mParams[0].getBitmap();
                    }

                });
            }
        }

        mPreviewGallery.setAdapter(new PreviewGallery.PreviewGalleryPagerAdapter(
                previewGalleryData));
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

    private void setThemeIntroValues(int pos) {
        themeName.setText(mThemeName[pos]);
        themeWriter.setText(mThemeAuthor[pos]);
        themeDiscription.setText(mThemeDescription[pos]);
        //themeVersion.setText(Build.KBSTYLE);
        File file = new File(mThemePath[pos]);
        if (file != null)
            themeSize.setText(Formatter.formatFileSize(this, file.length()));
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

            if (app.packageName.startsWith("com.freeme.theme.")) {
                Resources.ThemeInfo themeInfo = getResources().getThemeInfo(
                        app.applicationInfo.sourceDir, app.packageName);
                if (themeInfo.themePath.equals(configuration.skin)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case android.R.id.home:
            finish();
            break;
        case R.id.share_image:
            ShareUtil.shareText(this, getResources().getString(R.string.share_theme_message_left)+
                    Build.getFreemeOSLabel()+mThemeName[mSelectedPos]+getResources().getString(R.string.share_theme_message_right));
            break;
        case R.id.apply_button:
            Configuration configuration = getResources().getConfiguration();
            if(mThemePath[mSelectedPos].equals(configuration.skin)){
                Toast.makeText(this, R.string.theme_in_use, Toast.LENGTH_SHORT).show();
            }else{
                sendBroadcast(new Intent(
                        "com.android.soundRecorder.command.state.chanage"));
                sendBroadcast(new Intent(
                        "com.android.soundrecorder.command.theme.change"));
                Intent intent = new Intent();
                intent.putExtra(APPLY, mSelectedPos);
                setResult(1, intent);
                finish();
            }
            break;   
        case R.id.delete_image:

        case MENU_ID_DELETE:
            Builder builder = new Builder(this);
//            builder.setTitle(getResources().getString(R.string.delete_theme));
            builder.setMessage(getResources().getString(R.string.uninstall_theme_tips)
                    /*+ " " + mThemeName[mSelectedPos]*/);
            builder.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mUninstallDialog = new ProgressDialog(
                                    ThemeDetailActivity.this);
                            mUninstallDialog.setMessage(getResources()
                                    .getString(R.string.deleting));
                            mUninstallDialog.setIndeterminate(true);
                            mUninstallDialog.setCancelable(false);
                            mUninstallDialog.show();
                            PackageManager pManager = getPackageManager();
                            pManager.deletePackage(mPackageName[mSelectedPos],
                                    new IPackageDeleteObserver.Stub() {
                                public void packageDeleted(
                                        String packageName,
                                        int returnCode) {
                                    mUninstallDialog.cancel();
                                    setResult(2, null);
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
