package com.freeme.themeclub.individualcenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.freeme.themeclub.MainActivity;
import com.freeme.themeclub.OuterFragment;
import com.freeme.themeclub.R;

public class IndividualcenterFragment extends OuterFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentLayout(R.layout.outer_fragment_individual);
        setInnerViewpagerId(R.id.inner_viewpager_individual);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        updateTabPos();
        return v;
    }

    public void updateTabPos(){
        Intent intent = getActivity().getIntent();
        if(intent != null){
            String action = intent.getAction();
            if(action != null){ 
                if("android.settings.WALLPAPER_SETTINGS_KBSTYLE".equals(action)||
                        "android.settings.WALLPAPER_SETTINGS".equals(action)){
                    mViewPager.setCurrentItem(2);
                }
            }
        }
    }

    @Override
    public void addFragments() {
        mPageList.clear();
        mPageList.add(IndividualThemeFragment.class.getName());
        mPageList.add(IndividualLockScreenFragment.class.getName());
        mPageList.add(IndividualWallpaperFragment.class.getName());
        mPageList.add(IndividualFontFragment.class.getName());
    }

}