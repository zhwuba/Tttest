package com.freeme.themeclub.wallpaper;

import com.freeme.themeclub.OuterFragment;
import com.freeme.themeclub.R;

import android.os.Bundle;

public class WallpaperFragment extends OuterFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentLayout(R.layout.outer_fragment_wallpaper);
        setInnerViewpagerId(R.id.inner_viewpager_wallpaper);
    }


    @Override
    public void addFragments() {
        mPageList.clear();
        mPageList.add(NewestWallpaperFragment.class.getName());
//        mPageList.add(EssenceWallpaperFragment.class.getName());
        mPageList.add(PopularWallpaperFragment.class.getName());
        mPageList.add(CategoryWallpaperFragment.class.getName());
    }
}
