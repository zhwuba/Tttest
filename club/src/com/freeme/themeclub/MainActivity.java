package com.freeme.themeclub;

import java.util.ArrayList;
import java.util.concurrent.Executor;

//import com.freeme.themeclub.homepage.HomepageFragment;
import com.freeme.themeclub.individualcenter.IndividualThemeFragment;
import com.freeme.themeclub.individualcenter.IndividualcenterFragment;
import com.freeme.themeclub.lockscreen.LockscreenFragment;
import com.freeme.themeclub.mine.MineFragment;
import com.freeme.themeclub.network.NetworkUtil;
import com.freeme.themeclub.resourcemanager.ResourceManagerActivity;
import com.freeme.themeclub.search.SearchActivity;
import com.freeme.themeclub.statisticsdata.LocalUtil;
import com.freeme.themeclub.statisticsdata.db.StatisticDBHelper;
import com.freeme.themeclub.theme.ThemeFragment;
import com.freeme.themeclub.updateself.UpdateManager;
import com.freeme.themeclub.wallpaper.WallpaperFragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
    private ActionBar mActionBar;
    private OuterViewPager mViewPager;
    private ArrayList<String> mPageList = new ArrayList<String>();
    private MyViewPagerAdapter mViewPagerAdapter;
    private Menu mMenu;
    private static final int MENU_ID_SEARCH = Menu.FIRST;
    private static final int MENU_ID_RESOURCE_MANAGER = Menu.FIRST+1;
    public static final String POSITION = "position";

    public static Executor fixedThreadPool = AsyncTask.THREAD_POOL_EXECUTOR; 

    private StatisticDBHelper mStatisticDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetworkState();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        mStatisticDBHelper = StatisticDBHelper.getInstance(MainActivity.this);
        saveStartNewsPageTime();
        super.onPostCreate(savedInstanceState);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuItem item = menu.add(0, MENU_ID_SEARCH, 1, getString(
                R.string.search));
        item.setIcon(R.drawable.search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        
        MenuItem item2 = menu.add(0, MENU_ID_RESOURCE_MANAGER, 2, getString(
                R.string.resource_manager_title));
        item2.setIcon(R.drawable.resource_manager);
        item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ID_SEARCH:
            Intent intent = new Intent(this,SearchActivity.class);
            intent.putExtra(SearchActivity.SEARCH_TYPE, getTitle());
            startActivity(intent);
            break;
            
        case MENU_ID_RESOURCE_MANAGER:
            int position = mViewPager.getCurrentItem();
            if(position == 3){
                startResourceManagerActivity(0);
            }else{
                startResourceManagerActivity(position);
            }
            
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveStartNewsPageTime() {
        LocalUtil.StatisticInfo mInfo = new LocalUtil.StatisticInfo();
        mInfo.ac_id = LocalUtil.START_ACTION_ID;
        mInfo.s_dt = System.currentTimeMillis();
        String infoStr = LocalUtil.infoToJsonStr(mInfo);
        mStatisticDBHelper.intserStatisticdataToDB(infoStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatisticDBHelper.saveInfosToFileFromDB(MainActivity.this);
        mStatisticDBHelper.cleanup();
    }
    private void checkNetworkState() {
        if (NetworkUtil.isWifiConnected(this)) {
            initUI();
            UpdateManager.updateServiceQueryNew(MainActivity.this);
            return;
        } else {
            initUI();
            startResourceManagerActivity(0);
        }
    }

    private void loadData(int position){
        Fragment fragment = getFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + position);
        if(fragment instanceof LoadOuterData){
            LoadOuterData bsf = (LoadOuterData) fragment;
            bsf.loadOuterData();
        }
    }

    private class MyViewPagerAdapter extends FragmentPagerAdapter implements
    ViewPager.OnPageChangeListener {
        Context mContext;

        public MyViewPagerAdapter(Context context,
                FragmentManager fragmentManager) {
            super(fragmentManager);
            mContext = context;
        }

        public void load(int position){
            if(position==mViewPager.getCurrentItem()){
                loadData(position);
            }

        }

        @Override
        public Fragment getItem(int arg0) {
            return Fragment.instantiate(mContext, mPageList.get(arg0));
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public void onPageSelected(int arg0) {
            Tab currentTab = mActionBar.getTabAt(arg0);
            mActionBar.selectTab(currentTab);
            setTitle(currentTab.getText());
            loadData(arg0);
            updateMenu(arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
    
    private void updateMenu(int position){
        if(mMenu != null){
            if(position == 3){
                mMenu.findItem(MENU_ID_SEARCH).setVisible(false);
            }else{
                mMenu.findItem(MENU_ID_SEARCH).setVisible(true);
            }
        }
    }

    private final TabListener mMyBarTabListener = new TabListener() {
        @Override
        public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        }

        @Override
        public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
            if (mViewPager != null){
                mViewPager.setCurrentItem(arg0.getPosition());
            }
        }

        @Override
        public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        }
    };

    private void initUI(){
        setContentView(R.layout.activity_main);
        setTitle(getResources().getString(R.string.theme_title));
        mActionBar=null;
        mPageList.clear();
        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS_IP);
        //        mActionBar.addTab(mActionBar.newTab()
        //                .setText(R.string.homepage_title)
        //                .setIcon(getResources().getDrawable(R.drawable.tab_homepage))
        //                .setTabListener(mMyBarTabListener));
        mActionBar.addTab(mActionBar.newTab()
                .setText(R.string.theme_title)
                .setIcon(getResources().getDrawable(R.drawable.tab_theme))
                .setTabListener(mMyBarTabListener));
        mActionBar.addTab(mActionBar.newTab()
                .setText(R.string.lockscreen_title)
                .setIcon(getResources().getDrawable(R.drawable.tab_lockscreen))
                .setTabListener(mMyBarTabListener));
        mActionBar.addTab(mActionBar.newTab()
                .setText(R.string.wallpaper_title)
                .setIcon(getResources().getDrawable(R.drawable.tab_wallpaper))
                .setTabListener(mMyBarTabListener));
        mActionBar.addTab(mActionBar.newTab()
                .setText(R.string.individual_title)
                .setIcon(getResources().getDrawable(R.drawable.tab_individual))
                .setTabListener(mMyBarTabListener));

        //        mPageList.add(HomepageFragment.class.getName());
        mPageList.add(ThemeFragment.class.getName());
        mPageList.add(LockscreenFragment.class.getName());
        mPageList.add(WallpaperFragment.class.getName());
        mPageList.add(MineFragment.class.getName());

        mViewPager = (OuterViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(mPageList.size());

        mViewPagerAdapter = new MyViewPagerAdapter(this,
                getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mViewPagerAdapter);

        updateTabPos();
    }

    public void updateTabPos(){
        Intent intent = getIntent();
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if("android.intent.action.THEME_MANAGER".equals(action)){
                    startResourceManagerActivity(0);
                }else if("android.settings.WALLPAPER_SETTINGS_KBSTYLE".equals(action)||
                        "android.settings.WALLPAPER_SETTINGS".equals(action)){
                    startResourceManagerActivity(2);
                }
            }else{
                if(intent.getBooleanExtra("fromNotification", false)){
                    startResourceManagerActivity(0);
                }
            }
        }
    }
    
    private void startResourceManagerActivity(int position){
        Intent intent = new Intent(this,ResourceManagerActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }
    
    
}
