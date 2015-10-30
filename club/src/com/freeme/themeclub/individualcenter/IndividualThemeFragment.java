package com.freeme.themeclub.individualcenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.freeme.themeclub.AsyncImageCache;
import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.CustomToast;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.NoScrollViewPager;
import com.freeme.themeclub.R;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.OpenableColumns;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class IndividualThemeFragment extends BackScrollFragment{
    private static final String THEME_TYPE_NORMAL = "normal";
    private static final String THEME_TYPE_CLASSIC = "classic";
    private static final String THEME_TYPE_SCENE = "scene";
    private static final String THEME_TYPE_BIGLUNCHER = "bigluncher";

    protected static final ComponentName NORMAL_LAUNCHER = new ComponentName(
            "com.freeme.launcher", "com.freeme.launcher.Launcher");

    protected static final ComponentName CLASSIC_LAUNCHER = new ComponentName(
            "com.android.launcher", "com.android.launcher2.Launcher");

    protected static final ComponentName SCENE_LAUNCHER = new ComponentName(
            "com.freeme.scenelauncher", "com.freeme.scenelauncher.LauncherRoot");

    protected static final ComponentName BIG_LAUNCHER = new ComponentName(
            "com.freeme.olderdesk",
            "com.freeme.olderdesk.ui.activity.HomeActivity");

    private static final String SCENE_LAUNCHER_ACTION = "com.freeme.scenelauncher.action.SCENE";

    private static final String FRAMEWORK_PACKAGE_NAME = "android";
    private static final String FRAMEWORK_PACKAGE_PATH = "/system/framework/framework-res.apk";

    private static final String KEY_WKS_TRANSITION_EFFECT = "workspace_transition_effect";
    private static final String KEY_APP_TRANSITION_EFFECT = "apps_transition_effect";
    private static final String LOCKSCREEN_PACKAGE = "lockscreen_package";

    public static final String ACTION_REFRESH_THEME = "android.intent.action.tyd_refresh_theme";

    public static final String ACTION_THEME_MANAGER_SERVICE = "freeme.intent.action.ThemeManagerService";

    public final static String APPLY = "com.freeme.thememanager.apply";

    protected static String[] mPreviewTitles = null;
    protected static String[] mPreviewThemePaths = null;
    protected static String[] mPreviewPackageNames = null;
    protected static String[] mPreviewAuthors = null;
    protected static String[] mPreviewVersions = null;
    protected static String[] mPreviewDiscription = null;

    private static ArrayList<Resources.ThemeInfo> mThemeInfos = new ArrayList<Resources.ThemeInfo>();
    private AsyncImageCache mAsyncImageCache;
    private Configuration config = null;
    private Handler mHandler;
    private BroadcastReceiver mRefreshTheme;
    private ThemeAdapter mAdapter;

    private NoScrollViewPager mViewPager;
    private LinearLayout navigationLayout;
    private Switch sevenThemeSwitch;

    private boolean lessThan7 = true;
    public ArrayList<Integer> sevenNum;
    public static int num=0;

    private boolean fresh;

    private boolean flag = true;

    private StatisticDBHelper mStatisticDBHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncImageCache = AsyncImageCache.from(this.getActivity());
        config = getResources().getConfiguration();
        initData();
        mStatisticDBHelper=StatisticDBHelper.getInstance(getActivity());

        mHandler = new Handler();
        mRefreshTheme = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                initData();
            }
        };
        getActivity().registerReceiver(mRefreshTheme,
                new IntentFilter(IndividualThemeFragment.ACTION_REFRESH_THEME));

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(ACTION_THEME_MANAGER_SERVICE);
                intent.setPackage("com.freeme.themeclub.core");
                if(getActivity()!=null){
                getActivity().startService(intent);
            }
            }
        });

        //        setHasOptionsMenu(true);
    }

    public void loadData(){
        if(!fresh){
            fresh=true;
            //            initData();
        }

    }

    private void resetSwitch(){
        sevenThemeSwitch.setChecked(getIfUse());
    }

    private void recordIfUse(boolean ifuse){
        SharedPreferences sp = getActivity().getSharedPreferences(
                "sevenTheme",Activity.MODE_PRIVATE); 
        Editor editor = sp.edit();
        editor.putBoolean("ifuse", ifuse);
        editor.commit();
    }

    private boolean getIfUse(){
        SharedPreferences sp = getActivity().getSharedPreferences(
                "sevenTheme",Activity.MODE_PRIVATE); 
        return sp.getBoolean("ifuse", false);
    }

    private void openSevenTheme(){
        Calendar startTime = Calendar.getInstance(Locale.getDefault());
        startTime.setTimeInMillis(System.currentTimeMillis());
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);
        startTime.add(Calendar.DAY_OF_YEAR, 1);
        Intent startIntent = new Intent("com.freeme.themeclub.seventheme.receiver");
        PendingIntent alarmSender = PendingIntent.getBroadcast(
                getActivity(), 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);  

        AlarmManager am = (AlarmManager) getActivity().
                getSystemService(Activity.ALARM_SERVICE); 
        am.setRepeating(AlarmManager.RTC_WAKEUP, 
                startTime.getTimeInMillis(), 24*60*60*1000, alarmSender);
    }

    private void closeSevenTheme(){
        Intent startIntent = new Intent("com.freeme.themeclub.seventheme.receiver");
        PendingIntent alarmSender = PendingIntent.getBroadcast(
                getActivity(), 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getActivity().
                getSystemService(Activity.ALARM_SERVICE); 
        am.cancel(alarmSender);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_individual_theme, container,
                false);

        sevenThemeSwitch = (Switch)contentView
                .findViewById(R.id.seven_theme_switch);
        resetSwitch();
        sevenThemeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (mThemeInfos.size() < 7) {
                        lessThan7 = true;
                        CustomToast.showToast(getActivity(), getActivity().getString(R.string.not_enough_seven),
                                3000);
                        buttonView.setChecked(false);
                    }else{
                        CustomToast.showToast(getActivity(), getActivity().getString(R.string.seven_theme_on),
                                3000);
                        lessThan7 = false;
                        recordIfUse(true);
                        openSevenTheme();
                        String name=getActivity().getString(R.string.seven_theme);
                        String infoStr =LocalUtil.saveStatisticInfo(LocalUtil.CLICK_ACTION_ID, LocalUtil.MINE_CLICK_SERVERTHEME, name,System.currentTimeMillis());
                        mStatisticDBHelper.intserStatisticdataToDB(infoStr);
                    }
                }else{
                    recordIfUse(false);
                    closeSevenTheme();
                    CustomToast.showToast(getActivity(), getActivity().getString(R.string.seven_theme_off),
                            3000);
                }
            }
        });

        GridView mGridView = (GridView) contentView
                .findViewById(R.id.grid_view);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {

                Intent intent = new Intent(getActivity(),
                        ThemeDetailActivity.class);
                intent.putExtra(ThemeDetailActivity.PACKAGENAME,
                        mPreviewPackageNames);
                intent.putExtra(ThemeDetailActivity.THEMENAME, mPreviewTitles);
                intent.putExtra(ThemeDetailActivity.THEMEPATH,
                        mPreviewThemePaths);
                intent.putExtra(ThemeDetailActivity.THEMEAUTHOR,
                        mPreviewAuthors);
                intent.putExtra(ThemeDetailActivity.THEMEDESCRIPTION,
                        mPreviewDiscription);
                intent.putExtra(ThemeDetailActivity.SELECTPOSITION, position);
                startActivityForResult(intent, 0);
            }
        });
        mAdapter = new ThemeAdapter(inflater, getActivity());
        mGridView.setAdapter(mAdapter);
        return contentView;
    }
    
    private Bitmap getThemeThumb(String packageName){
        Bitmap themeThumb = null;
        Cursor cursor = getActivity().getContentResolver().query(ThemeConstants.CONTENT_URI, 
                new String[]{ ThemeConstants.THUMBNAIL },
                ThemeConstants.PACKAGE_NAME + " = ?", new String[] { packageName },
                null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(ThemeConstants.THUMBNAIL);
            byte[] bytes =  cursor.getBlob(index);
            themeThumb = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        cursor.close();
        return themeThumb;
    }

    public class ThemeAdapter extends ArrayAdapter<Resources.ThemeInfo> {
        private LayoutInflater mInflater;
        private Context mContext;

        public ThemeAdapter(LayoutInflater inflater, Context context) {
            super(context, 0);
            mInflater = inflater;
            mContext = context;
        }

        public void setData(List<Resources.ThemeInfo> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        public View getView(final int position, View convertView,
                ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_view_item_theme, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
                holder.status = (ImageView) convertView
                        .findViewById(R.id.status);
                holder.text = (TextView) convertView.findViewById(R.id.text);
                holder.type = (ImageView) convertView.findViewById(R.id.type);
                holder.apply = (Button) convertView.findViewById(R.id.apply);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            
            Resources.ThemeInfo themeInfo = getItem(position);
            String path = themeInfo.themePath;
            String packageName = themeInfo.packageName;
            holder.icon.setImageBitmap(getThemeThumb(packageName));
            if (mContext.getResources().getThemePreview(path,
                    Resources.THEME_PREVIEW_THUMB) == null) {
                mAsyncImageCache.displayImage(
                        holder.icon,
                        R.drawable.theme_default,
                        new AsyncImageCache.GeneralImageGenerator(path,
                                BitmapFactory.decodeResource(
                                        mContext.getResources(),
                                        R.drawable.theme_default)));
            } else {
                mAsyncImageCache.displayImage(
                        holder.icon,
                        R.drawable.theme_default,
                        new AsyncImageCache.GeneralImageGenerator(path,
                                mContext.getResources()
                                .getThemePreview(path,
                                        Resources.THEME_PREVIEW_THUMB)
                                        .getBitmap()));
            }
            if (THEME_TYPE_SCENE.equals(themeInfo.themeType)) {
                holder.type.setImageResource(R.drawable.theme_type_scene);
                holder.type.setVisibility(View.VISIBLE);
            } else if (THEME_TYPE_CLASSIC.equals(themeInfo.themeType)) {
                holder.type.setImageResource(R.drawable.theme_type_classic);
                holder.type.setVisibility(View.VISIBLE);
            } else {
                holder.type.setVisibility(View.GONE);
            }

            holder.status.setImageResource(R.drawable.status_using);
            if ((config != null) && !path.equals(config.skin)) {
                holder.status.setVisibility(View.INVISIBLE);
            } else {
                holder.status.setVisibility(View.VISIBLE);
            }
            holder.text.setText(themeInfo.title);

            holder.apply.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    onApply(getActivity(),position);
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView icon;
            ImageView status;
            ImageView type;
            TextView text;
            Button apply;
            RelativeLayout deleteView;
        }
    }

    public void onApply(final Context context,final int pos) {
        Configuration config=context.getResources().getConfiguration();
        context.sendBroadcast(
                new Intent("com.android.soundRecorder.command.state.chanage"));
        context.sendBroadcast(
                new Intent("com.android.soundrecorder.command.theme.change"));

        final String packageName = mAdapter.getItem(pos).packageName;
        final String themePath = mAdapter.getItem(pos).themePath;
        if (config != null) {
            if(!themePath.equals(config.skin)){

                Intent intent = new Intent(ACTION_THEME_MANAGER_SERVICE);
                intent.setPackage("com.freeme.themeclub.core");
                intent.putExtra("action", "change_theme");
                intent.putExtra("package_name", packageName);
                intent.putExtra("theme_path", themePath);
                Log.e("install", " packageName= "+packageName);
                Log.e("install", "themePath = "+themePath);
                Log.e("yzy", "config"+themePath);
                context.startService(intent);
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if(context instanceof Activity){
                            if(context!=null){
                                ((Activity)context).finish();
                            }
                        }
                    }
                }, 500);
            }else{
                CustomToast.showToast(context, R.string.theme_in_use, 3000);
            }
        }
    }

    public static synchronized List<Resources.ThemeInfo> loadThemeInfos(Context context) {
        int position = 0;
        int columnIndex = 0;
        int themeCount;

        if (null != mThemeInfos) {
            mThemeInfos.clear();
        }

        if(context==null){
            return null;
        }
        final Configuration config = context.getResources().getConfiguration();
        Cursor cursor = context.getContentResolver().query(
                ThemeConstants.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return null;
        }
        for (cursor.moveToFirst(); (!cursor.isAfterLast()); cursor.moveToNext()) {
            columnIndex = cursor.getColumnIndex(ThemeConstants.PACKAGE_NAME);
            String packageName = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.THEME_PATH);
            String themePath = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.THEME_TYPE);
            String themeType = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.FONT);
            String font = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.TITLE);
            String title = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.DESCRIPTION);
            String description = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.AUTHOR);
            String author = cursor.getString(columnIndex);

            columnIndex = cursor.getColumnIndex(ThemeConstants.VERSION);
            String version = cursor.getString(columnIndex);

            if (title != null) {
                Resources.ThemeInfo themeInfo = new Resources.ThemeInfo();
                themeInfo.packageName = packageName;
                themeInfo.themePath = themePath;
                themeInfo.themeType = themeType;
                themeInfo.font = android.text.TextUtils.isEmpty(font) ? "default"
                        : font;
                themeInfo.title = title;
                themeInfo.description = description;
                themeInfo.author = author;
                themeInfo.version = version;

                if (themeInfo.packageName.equals(FRAMEWORK_PACKAGE_NAME))
                    themeInfo.title = android.os.Build.MODEL;

                mThemeInfos.add(themeInfo);

            } else {
                context.getContentResolver().delete(
                        ThemeConstants.CONTENT_URI,
                        ThemeConstants.PACKAGE_NAME + " = ?",
                        new String[] { packageName });
            }
        }

        Collections.sort(mThemeInfos, new Comparator<Resources.ThemeInfo>() {

            @Override
            public int compare(Resources.ThemeInfo lhs, Resources.ThemeInfo rhs) {

                //                if (lhs.themePath.equals(config.skin))
                //                    return -1;
                //                else if (rhs.themePath.equals(config.skin))
                //                    return 1;
                //                else 
                if (lhs.packageName.equals(FRAMEWORK_PACKAGE_NAME))
                    return -1;
                else if (rhs.packageName.equals(FRAMEWORK_PACKAGE_NAME))
                    return 1;
                else if (THEME_TYPE_SCENE.equals(lhs.themeType)
                        && !THEME_TYPE_SCENE.equals(rhs.themeType))
                    return 1;
                else if (!THEME_TYPE_SCENE.equals(lhs.themeType)
                        && THEME_TYPE_SCENE.equals(rhs.themeType))
                    return -1;
                else if (THEME_TYPE_CLASSIC.equals(lhs.themeType)
                        && !THEME_TYPE_CLASSIC.equals(rhs.themeType))
                    return 1;
                else if (!THEME_TYPE_CLASSIC.equals(lhs.themeType)
                        && THEME_TYPE_CLASSIC.equals(rhs.themeType))
                    return -1;

                else if (THEME_TYPE_BIGLUNCHER.equals(lhs.themeType)
                        && !THEME_TYPE_BIGLUNCHER.equals(rhs.themeType))
                    return 1;
                else if (!THEME_TYPE_BIGLUNCHER.equals(lhs.themeType)
                        && THEME_TYPE_BIGLUNCHER.equals(rhs.themeType))
                    return -1;

                else
                    return rhs.title.compareToIgnoreCase(lhs.title);
            }
        });

        themeCount = mThemeInfos.size();

        if (cursor != null) {
            cursor.close();
        }

        mPreviewTitles = new String[themeCount];
        mPreviewThemePaths = new String[themeCount];
        mPreviewPackageNames = new String[themeCount];
        mPreviewAuthors = new String[themeCount];
        mPreviewVersions = new String[themeCount];
        mPreviewDiscription = new String[themeCount];
        for (int i = 0; i < themeCount; i++) {
            mPreviewTitles[i] = mThemeInfos.get(i).title;
            mPreviewThemePaths[i] = mThemeInfos.get(i).themePath;
            mPreviewPackageNames[i] = mThemeInfos.get(i).packageName;
            mPreviewAuthors[i] = mThemeInfos.get(i).author;
            mPreviewVersions[i] = mThemeInfos.get(i).version;
            mPreviewDiscription[i] = mThemeInfos.get(i).description;
        }
        return mThemeInfos;
    }

    private LoadThemeInfosTask mLoadTask;
    private void initData() {
        if(mLoadTask != null && !mLoadTask.isCancelled()){
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
        mLoadTask=new LoadThemeInfosTask(getActivity());
        mLoadTask.executeOnExecutor(MainActivity.fixedThreadPool);
    }

    class LoadThemeInfosTask extends
    AsyncTask<Void, Void, List<Resources.ThemeInfo>> {
        public LoadThemeInfosTask(Context context) {
        }

        @Override
        protected List<Resources.ThemeInfo> doInBackground(Void... pramas) {

            return IndividualThemeFragment.this.loadThemeInfos(getActivity());
        }

        @Override
        protected void onPostExecute(List<Resources.ThemeInfo> result) {
            // ThemeManager.this.setListShown(true);
            if(result==null){
                return ;
            }
            if(getActivity()!=null){
                if(result.size()>=7){
                    lessThan7=false;
                    getActivity().invalidateOptionsMenu();
                }
                mAdapter.setData(result);
                mAdapter.notifyDataSetChanged();
                //                startIntent.putExtra("num", num);
                //                startIntent.putExtra("size", result.size());
                //                try {  
                //                    alarmSender = PendingIntent.getBroadcast(getActivity(), 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
                //                } catch (Exception e) {  
                //                    e.printStackTrace(); 
                //                }
                //                am = (AlarmManager) getActivity().getSystemService(Activity.ALARM_SERVICE); 
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        int pos = -1;
        switch (resultCode) {
        case 1:
            pos = data.getIntExtra(ThemeDetailActivity.APPLY, 0);
            break;

        case 2:
            if(getIfUse()&&(mAdapter.getCount()==7)){
                lessThan7=true;
                recordIfUse(false);
                resetSwitch();
                closeSevenTheme();
                CustomToast.showToast(getActivity(), getActivity().getResources().getString(
                        R.string.seven_theme_off),
                        3000);
            }
        }

        if (pos != -1 && pos < mAdapter.getCount()) {
            onApply(getActivity(),pos);
        }
    }

    @Override
    public void onDestroy() {
        if (mRefreshTheme != null)
            getActivity().unregisterReceiver(mRefreshTheme);
        mAsyncImageCache.stop();
        super.onDestroy();
    }

    public static class Theme7BroadcastReceiver2 extends BroadcastReceiver{
        int num;
        int size;
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("yzy", "onReceive2");
            num=intent.getIntExtra("num", 0);
            size=intent.getIntExtra("size", 0);
            SimpleDateFormat sdf=new SimpleDateFormat("HH");
            int now=Integer.parseInt(sdf.format(new Date()));
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if(now>=0&&now<5){
                if(pm.isScreenOn()){
                    Log.e("yzy", "onReceive3");
                    PendingIntent alarmSender = null;  
                    Intent startIntent = new Intent("com.freeme.themeclub.seventheme.receiver");  
                    startIntent.putExtra("num", num);
                    startIntent.putExtra("size", size);
                    try {  
                        alarmSender = PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
                    } catch (Exception e) {  
                        e.printStackTrace(); 
                    }
                    AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);  
                    am.set/*Exact*/(AlarmManager.RTC_WAKEUP , System.currentTimeMillis()+30*60*1000, alarmSender);
                }else{
                    Log.e("yzy", "onReceive4");
                    List<Resources.ThemeInfo> list=loadThemeInfos(context);
                    int temp=(int)(Math.random()*list.size());

                    Configuration config=context.getResources().getConfiguration();
                    context.sendBroadcast(
                            new Intent("com.android.soundRecorder.command.state.chanage"));
                    context.sendBroadcast(
                            new Intent("com.android.soundrecorder.command.theme.change"));
                    final String packageName = list.get(temp==0?1:temp).packageName;
                    final String themePath = list.get(temp==0?1:temp).themePath;
                    Intent intent2 = new Intent(ACTION_THEME_MANAGER_SERVICE);
                    intent2.setPackage("com.freeme.themeclub.core");
                    intent2.putExtra("action", "change_theme");
                    intent2.putExtra("package_name", packageName);
                    intent2.putExtra("theme_path", themePath);
                    Log.e("yzy", "config"+themePath);
                    Log.e("yzy", "list.size()="+size);
                    context.startService(intent2);
                } 
            }
        }
    }

}
