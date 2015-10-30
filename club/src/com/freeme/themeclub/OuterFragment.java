package com.freeme.themeclub;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freeme.themeclub.R;
import com.freeme.themeclub.individualcenter.IndividualThemeFragment;
import com.freeme.themeclub.theme.onlinetheme.util.NetworkUtil;
import com.freeme.themeclub.theme.onlinetheme.util.OnlineThemesUtils;

public abstract class OuterFragment extends Fragment implements LoadOuterData{
    public View contentView;
    public Activity mActivity;
    protected ViewPager mViewPager;
    public ArrayList<String> mPageList = new ArrayList<String>();
    private NavigationControlView navigationInner;
    public int outerFragmentLayout;
    private int innerViewpagerId;
    private MyViewPagerAdapter mMyViewPagerAdapter;
    private View refreshLayout;
    public void setInnerViewpagerId(int innerViewpagerId) {
        this.innerViewpagerId = innerViewpagerId;
    }

    public void setFragmentLayout(int fragmentLayout) {
        this.outerFragmentLayout = fragmentLayout;
    }

    public ViewPager getViewPager(){
        return mViewPager;
    }

    public LinearLayout getNavigation(){
        return navigationInner;
    }    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        contentView = inflater.inflate(outerFragmentLayout, container,false);

        navigationInner=(NavigationControlView)contentView.findViewById(R.id.navigationInner);
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
        mViewPager = (ViewPager) contentView.findViewById(innerViewpagerId);
        refreshLayout = contentView.findViewById(R.id.refresh_linearLayout_id);
        if(mViewPager==null){
            return null;
        }
        initUI();
        return contentView;
    }

    private void showRefresh(boolean flag){
        refreshLayout.setVisibility(flag?View.VISIBLE:View.GONE);
        navigationInner.setVisibility(flag?View.GONE:View.VISIBLE);
    }

    private void initUI(){
        if(NetworkUtil.getAPNType(getActivity())==-1){
            if(refreshLayout==null){
                initOuterFragment();
            }else{
                File file = new File(OnlineThemesUtils.getSDPath() + "/themes/" +"download/cache/listData/");
                if((file!=null && !file.exists())||(file!=null&&file.list().length==0)){
                    showRefresh(true);
                    contentView.findViewById(R.id.set_wlan).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent =  new Intent(Settings.ACTION_WIFI_SETTINGS);  
                            startActivity(intent);
                        }
                    });
                    contentView.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            showRefresh(false);
                            initUI();
                        }
                    });
                }else{
                    showRefresh(false);
                    initOuterFragment();
                }

            }
        }else{
            if(refreshLayout!=null){
                showRefresh(false);
            }
            initOuterFragment();
        }
    }

    private void initOuterFragment(){
        addFragments();
        mViewPager.setOffscreenPageLimit(mPageList.size());
        mMyViewPagerAdapter = new MyViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mMyViewPagerAdapter);
        mViewPager.setOnPageChangeListener(mMyViewPagerAdapter);
    }

    public abstract void addFragments();

    public void loadOuterData(){
        if(mMyViewPagerAdapter!=null){
            Fragment fragment = getFragmentManager().findFragmentByTag("android:switcher:" + innerViewpagerId + ":" + mViewPager.getCurrentItem());
            if(fragment instanceof BackScrollFragment ){
                BackScrollFragment bi = (BackScrollFragment) fragment;
                bi.loadData();
            }
        }

    }

    private class MyViewPagerAdapter extends FragmentPagerAdapter
    implements ViewPager.OnPageChangeListener {

        public MyViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int arg0) {
            return  Fragment.instantiate(mActivity, mPageList.get(arg0));
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public void onPageSelected(int position) {
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
}
