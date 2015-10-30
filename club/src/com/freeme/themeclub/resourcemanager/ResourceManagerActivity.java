package com.freeme.themeclub.resourcemanager;

import java.util.ArrayList;

import com.freeme.themeclub.BackScrollFragment;
import com.freeme.themeclub.BannerInterface;
import com.freeme.themeclub.FragmentPagerAdapter;
import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.NavigationControlView;
import com.freeme.themeclub.R;
import com.freeme.themeclub.individualcenter.IndividualFontFragment;
import com.freeme.themeclub.individualcenter.IndividualLockScreenFragment;
import com.freeme.themeclub.individualcenter.IndividualThemeFragment;
import com.freeme.themeclub.individualcenter.IndividualWallpaperFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.view.ViewPager;

public class ResourceManagerActivity extends Activity {

    protected ViewPager mViewPager;
    public ArrayList<String> mPageList = new ArrayList<String>();
    private NavigationControlView navigationInner;
    public int outerFragmentLayout;
    private int innerViewpagerId;
    private MyViewPagerAdapter mMyViewPagerAdapter;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int position = intent.getIntExtra(MainActivity.POSITION, 0);
        setContentView(R.layout.outer_fragment_individual);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = getBaseContext();

        navigationInner=(NavigationControlView)findViewById(R.id.navigationInner);
        int count=navigationInner.getChildCount();
        for(int i=0;i<count;i++){
            final int m=i;
            navigationInner.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(m);
                }
            });
        }

        addFragments();
        mViewPager = (ViewPager)findViewById(R.id.inner_viewpager_individual);

        mViewPager.setOffscreenPageLimit(mPageList.size());
        mMyViewPagerAdapter = new MyViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mMyViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mMyViewPagerAdapter);
        mViewPager.setCurrentItem(position);
    }

    public void addFragments() {
        mPageList.add(IndividualThemeFragment.class.getName());
        mPageList.add(IndividualLockScreenFragment.class.getName());
        mPageList.add(IndividualWallpaperFragment.class.getName());
        mPageList.add(IndividualFontFragment.class.getName());
    }

    private class MyViewPagerAdapter extends FragmentPagerAdapter
    implements ViewPager.OnPageChangeListener {

        public MyViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return  super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int arg0) {
            return  Fragment.instantiate(ResourceManagerActivity.this, mPageList.get(arg0));
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public void onPageSelected(final int position) {
            ((RadioButton)navigationInner.getChildAt(position)).setChecked(true);
            Fragment fragment = getFragmentManager().findFragmentByTag("android:switcher:" + innerViewpagerId + ":" + position);
            if(fragment instanceof BackScrollFragment){
                BackScrollFragment bsf = (BackScrollFragment) fragment;
                bsf.loadData();
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

}
