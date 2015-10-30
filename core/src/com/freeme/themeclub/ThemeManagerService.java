package com.freeme.themeclub;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.WallpaperManager;
import android.app.StatusBarManager;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.freeme.themeclub.core.R;

public class ThemeManagerService extends Service {
    private static final String TAG = "ThemeManagerService";

    private static final String THEME_TYPE_NORMAL = "normal";
    private static final String THEME_TYPE_CLASSIC = "classic";
    private static final String THEME_TYPE_SCENE = "scene";

    //*/add by zhangmingjun at 20140410 for the bigluncher
    private static final String THEME_TYPE_BIGLUNCHER = "bigluncher";
    //*/

    protected static final ComponentName NORMAL_LAUNCHER = new ComponentName(
            "com.freeme.home", "com.freeme.home.Launcher");

    protected static final ComponentName CLASSIC_LAUNCHER = new ComponentName(
            "com.freeme.launcher", "com.freeme.launcher.Launcher");

    protected static final ComponentName SCENE_LAUNCHER = new ComponentName(
            "com.freeme.scenelauncher", "com.freeme.scenelauncher.LauncherRoot");

    //*/add by zhangmingjun at 20140410 for the bigluncher
    protected static final ComponentName BIG_LAUNCHER = new ComponentName(
            "com.freeme.biglauncher",
            "com.freeme.biglauncher.ui.activity.HomeActivity");
    //*/

    private static final String SCENE_LAUNCHER_ACTION = "com.freeme.scenelauncher.action.SCENE";

    private static final String FRAMEWORK_PACKAGE_NAME = "android";    
    private static final String FRAMEWORK_PACKAGE_PATH = "/system/framework/framework-res.apk";

    private static final String KEY_WKS_TRANSITION_EFFECT = "workspace_transition_effect";
    private static final String KEY_APP_TRANSITION_EFFECT = "apps_transition_effect";
    private static final String LOCKSCREEN_PACKAGE = "lockscreen_package";

    private Handler mHandler;

    private StatusBarManager mStatusBarManager;

    private Object mLock = new Object();

    private boolean showing = false;

    WindowManager mWindowManager;
    View mPurdah;
    Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "service onCreate()");
        mContext = this;

        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);

        //        mPurdah = View.inflate(mContext, R.layout.purdah, null);
        mPurdah = View.inflate(mContext, R.layout.purdah_fade, null);
        
        mStatusBarManager = (StatusBarManager) mContext.getSystemService(
                Context.STATUS_BAR_SERVICE);

        mHandler = new Handler();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action;
        if (intent == null || (action = intent.getStringExtra("action")) == null) 
            return super.onStartCommand(intent, flags, startId);

        if (action.equals("change_theme"))
            changeTheme(intent);
        else if (action.equals("change_font"))
            changeFont(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        ((TextView)mPurdah.findViewById(R.id.purdah_text)).setText(R.string.purdah_text);
        super.onConfigurationChanged(newConfig);
    }

    private void changeTheme(Intent intent) {
        final String packageName = intent.getStringExtra("package_name");
        final String themePath = intent.getStringExtra("theme_path");

        if ((packageName != null) && (themePath != null)) {
            final Resources.ThemeInfo themeInfo = getResources()
                    .getThemeInfo(themePath, packageName);

            Configuration config = getResources().getConfiguration();
            if ((config != null) && !themePath.equals(config.skin)) {

                showPurdah(true);

                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        new ApplyThemeTask(mContext).execute(themeInfo);
                    }}, 500);

            }
        }
    }

    private void changeFont(Intent intent) {
        final String font = intent.getStringExtra("font");

        if ((font != null) && !font.equals(Configuration.getFont())) {

            showPurdah(true);

            mHandler.postDelayed(new Runnable() {
                public void run() {
                    IActivityManager am = ActivityManagerNative.getDefault();
                    try {
                        synchronized (mLock) {
                            Configuration config = am.getConfiguration();
                            config.font = font;

                            // Update system Properties, change system theme.
                            Log.d(TAG, "doInBackground() am.updateConfiguration() config = " + config);
                            if (!config.equals(am.getConfiguration())) {
                                am.updateConfiguration(config);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Update configuration for font changed failed.");
                        e.printStackTrace();
                    }

                    Intent intent = new Intent("android.intent.action.MAIN");
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);

                    hidePurdahDelayed(2*1000);

                }}, 500);
        }
    }

    private ComponentName checkLauncherComponent(Context context, ComponentName componentName)
    {
        Intent intent = new Intent();  
        intent.setComponent(componentName);        
        if(context.getPackageManager().resolveActivity(intent, 0) != null) {  
            return componentName;
        }
        else {
            intent.setComponent(NORMAL_LAUNCHER);        
            if(context.getPackageManager().resolveActivity(intent, 0) != null)   
                return NORMAL_LAUNCHER;

            intent.setComponent(CLASSIC_LAUNCHER);        
            if(context.getPackageManager().resolveActivity(intent, 0) != null)   
                return CLASSIC_LAUNCHER;

            intent.setComponent(SCENE_LAUNCHER);        
            if(context.getPackageManager().resolveActivity(intent, 0) != null)   
                return SCENE_LAUNCHER;
        }

        return null;
    }

    private void startLaucher(Context context, Resources.ThemeInfo themeInfo) {

        ComponentName componentName = NORMAL_LAUNCHER;
        if (android.text.TextUtils.isEmpty(themeInfo.themeType)
                || THEME_TYPE_NORMAL.equals(themeInfo.themeType))
            componentName = NORMAL_LAUNCHER;
        else if (THEME_TYPE_CLASSIC.equals(themeInfo.themeType))
            componentName = CLASSIC_LAUNCHER;
        else if (THEME_TYPE_SCENE.equals(themeInfo.themeType))
            componentName = SCENE_LAUNCHER;
        //*/add by zhangmingjun at 20140410 for the bigluncher
        else if (THEME_TYPE_BIGLUNCHER.equals(themeInfo.themeType))
            componentName = BIG_LAUNCHER;  

        if(THEME_TYPE_BIGLUNCHER.equals(themeInfo.themeType))
        {
            Intent intent = new Intent();
            intent.setComponent(componentName);

        }else
        {
            componentName = checkLauncherComponent(context, componentName);
        }
        if (componentName != null)
            setPreferedHome(context.getPackageManager(), componentName);

        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (THEME_TYPE_SCENE.equals(themeInfo.themeType)) {


            Intent intentFilter = new Intent(SCENE_LAUNCHER_ACTION);
            intentFilter.setPackage(themeInfo.packageName);
            List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intentFilter, 0);

            if (infos.size()>0)
            {
                String scenePackageName = infos.get(0).activityInfo.packageName;
                String sceneActivity = infos.get(0).activityInfo.name;
                ComponentName sceneComponentName = new ComponentName(scenePackageName,sceneActivity);

                intent.putExtra("scene_comp", sceneComponentName);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("scene_extra", 0);
        }

        context.startActivity(intent);

    }

    private void killProcess() {
        String[] FORCE_STOP_PACKAGE = { "com.freeme.home", "com.freeme.launcher","com.freeme.scenelauncher", "com.freeme.olderdesk", "com.android.launcher3", "com.android.launcher", "com.android.contacts"};

        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<PackageInfo> packs = mContext.getPackageManager()
                .getInstalledPackages(0);
        for (PackageInfo packageInfo : packs) {
            String pkgName = packageInfo.packageName;
            if (Arrays.asList(FORCE_STOP_PACKAGE).contains(pkgName))
                activityManager.forceStopPackage(pkgName);
        }
    }

    private void hidePurdahDelayed(long time) {
        mHandler.postDelayed(mClosePurdarRunnable, time);
    }

    public void setPreferedHome(PackageManager packageManager,
            ComponentName componentName) {
        Log.d(TAG, "setPreferedHome");
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addCategory(Intent.CATEGORY_HOME);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.GET_INTENT_FILTERS);
        int len = list.size();
        ComponentName[] componentNames = new ComponentName[len];

        int match = 0;
        for (int i = 0; i < len; i++) {
            ResolveInfo resolveInfo = list.get(i);
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            Log.d(TAG, "match activity: " + activityInfo.name);
            packageManager.clearPackagePreferredActivities(activityInfo.packageName);
            if (resolveInfo.match > match) {
                match = resolveInfo.match;
            }
            componentNames[i] = new ComponentName(activityInfo.packageName, activityInfo.name);
        }
        packageManager.addPreferredActivity(filter, match, componentNames, componentName);
    }

    private class ApplyThemeTask extends AsyncTask<Resources.ThemeInfo, Void, Void> {

        private Context mContext;

        public ApplyThemeTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Resources.ThemeInfo... params) {

            final Resources.ThemeInfo themeInfo = params[0];

            killProcess();

            IActivityManager am = ActivityManagerNative.getDefault();
            try {
                synchronized (mLock) {
                    Configuration config = am.getConfiguration();
                    config.skin = themeInfo.themePath;
                    config.font = themeInfo.font;

                    // Update system Properties, change system theme.
                    Log.d(TAG, "doInBackground() am.updateConfiguration() config = " + config);
                    if (!config.equals(am.getConfiguration())) {
                        am.updateConfiguration(config);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Update configuration for theme changed failed.");
                e.printStackTrace();
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    applyTheme(themeInfo);
                }
            }, 500);

            return null;
        }

        @Override
        protected void onPreExecute() {
            mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);

        }

        @Override
        protected void onPostExecute(Void unused) {
            mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);

            hidePurdahDelayed(4*1000);
        }

        private void applyTheme(Resources.ThemeInfo themeInfo) {

            // set wallpaper
            try {
                WallpaperManager mWallpaperManager = WallpaperManager
                        .getInstance(mContext);

                String liveWallpaperString = mContext.getResources()
                        .getThemeString("default_wallpaper_component");

                if ("default".equals(liveWallpaperString))
                    liveWallpaperString = mContext
                    .getResources()
                    .getString(
                            com.android.internal.R.string.default_wallpaper_component);

                // live wallpaper
                if (!TextUtils.isEmpty(liveWallpaperString) && !liveWallpaperString.equals("@0")) {
                    ComponentName liveWallpaperComponent = ComponentName
                            .unflattenFromString(liveWallpaperString);
                    mWallpaperManager.getIWallpaperManager()
                    .setWallpaperComponent(liveWallpaperComponent);
                } else {
                    // wallpaper
                    mWallpaperManager
                    .setResource(com.android.internal.R.drawable.default_wallpaper);
                }

                // lockwallpaper
                mWallpaperManager
                        .setLockscreenResource(com.android.internal.R.drawable.default_wallpaper_lockscreen);

            } catch (Exception e) {
                android.util.Log.e(TAG, e.toString());
            }

            // set lockscreen package
            String lockscreenPackage = mContext.getResources()
                    .getThemeString("lockscreen_package");
            if (!TextUtils.isEmpty(lockscreenPackage)) {
                Settings.System.putString(mContext.getContentResolver(),
                        LOCKSCREEN_PACKAGE, lockscreenPackage);
            } else {
                LockscreenInfo lockscreenInfo = new LockscreenInfo(mContext,
                        themeInfo.packageName, themeInfo.themePath);

                if (lockscreenInfo.isLockscreenPackage())
                    Settings.System.putString(mContext.getContentResolver(),
                            LOCKSCREEN_PACKAGE, themeInfo.packageName);
            }

            // send lockscreen wallpaper changed Broadcast [for spec lockscreen]
            //Intent intent = new Intent(WallpaperManager.ACTION_LOCKSCREEN_WALLPAPER_CHANGED);
            //mContext.sendBroadcast(intent);

            // set launcher effect type
            int value = mContext.getResources().getThemeInteger(
                    "workspace_transition_effect");
            if (value != Resources.THEME_VALUE_NOTFOUND) {
                Settings.System.putInt(mContext.getContentResolver(),
                        KEY_WKS_TRANSITION_EFFECT, value);
            }
            value = mContext.getResources().getThemeInteger(
                    "apps_transition_effect");
            if (value != Resources.THEME_VALUE_NOTFOUND) {
                Settings.System.putInt(mContext.getContentResolver(),
                        KEY_APP_TRANSITION_EFFECT, value);
            }

            // set weather widget style
            value = mContext.getResources().getThemeInteger(
                    "tydweather_widget_index");
            if (value != Resources.THEME_VALUE_NOTFOUND) {
                Settings.System.putInt(mContext.getContentResolver(),
                        "tydweather_widget_index", value);
            }
            value = mContext.getResources().getThemeInteger(
                    "tydweather_mini_widget_index");
            if (value != Resources.THEME_VALUE_NOTFOUND) {
                Settings.System.putInt(mContext.getContentResolver(),
                        "tydweather_mini_widget_index", value);
            }

            // set music widget style
            value = mContext.getResources().getThemeInteger(
                    "tydmusic_widget_index");
            if (value != Resources.THEME_VALUE_NOTFOUND) {
                Settings.System.putInt(mContext.getContentResolver(),
                        "tydmusic_widget_index", value);
            }

            // start Launcher
            startLaucher(mContext,themeInfo);
        }

    }

    public synchronized void showPurdah(boolean autoClose) {

        if (!showing)
        {
            WindowManager.LayoutParams mPurdahLayoutParams = new WindowManager.LayoutParams();
            mPurdahLayoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            mPurdahLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mPurdahLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            // adaptation for navigationbar
            //mPurdahLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mPurdahLayoutParams.type = WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL;
            mPurdahLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;

            mWindowManager.addView(mPurdah, mPurdahLayoutParams);
            showing = true;
        }

        if (autoClose)
            mPurdah.postDelayed(mClosePurdarRunnable, 15 * 1000);

    }

    public synchronized void hidePurdah() {
        if (!showing)
            return;

        Log.e("yzy", "hidePurdah");
        mPurdah.removeCallbacks(mClosePurdarRunnable);

        //        final ImageView purdahLeft = (ImageView) mPurdah.findViewById(R.id.purdah_left);
        //        final ImageView purdahRight = (ImageView) mPurdah.findViewById(R.id.purdah_right);

        //        Animation translateLeft = AnimationUtils.loadAnimation(mContext, R.anim.translate_left);
        //        Animation translateRight = AnimationUtils.loadAnimation(mContext, R.anim.translate_right);

        //        translateRight.setAnimationListener(new AnimationListener() {
        //    
        //            @Override
        //            public void onAnimationStart(Animation animation) {
        //                // TODO Auto-generated method stub
        //            }
        //    
        //            @Override
        //            public void onAnimationRepeat(Animation animation) {
        //                // TODO Auto-generated method stub
        //            }
        //    
        //            @Override
        //            public void onAnimationEnd(Animation animation) {
        //                mWindowManager.removeView(mPurdah);
        //                showing = false;
        //                
        //                stopSelf();
        //                System.exit(0);
        //            }
        //        });
        //        
        //        purdahLeft.startAnimation(translateLeft);
        //        purdahRight.startAnimation(translateRight);

        final ImageView purdahFade = (ImageView) mPurdah.findViewById(R.id.purdah_view);
        mWindowManager.removeView(mPurdah);
        showing = false;

        stopSelf();
        System.exit(0);

//        Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.purdah_fade_out);
//
//        fadeOut.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                
//            }
//        });
//
//        purdahFade.startAnimation(fadeOut);

    }
    private final Runnable mClosePurdarRunnable = new Runnable() {
        public void run() {
            hidePurdah();
        }
    };

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("hi, you can look this, that's great!!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}

