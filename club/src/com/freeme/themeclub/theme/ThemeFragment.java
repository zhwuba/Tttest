package com.freeme.themeclub.theme;

import android.os.Bundle;

import com.freeme.themeclub.OuterFragment;
import com.freeme.themeclub.R;
import com.freeme.themeclub.theme.onlinetheme.CategoryThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.EssenceThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.NewestThemesFragment;
import com.freeme.themeclub.theme.onlinetheme.PopularThemesFragment;

public class ThemeFragment extends OuterFragment{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFragmentLayout(R.layout.outer_fragment_theme);
        setInnerViewpagerId(R.id.inner_viewpager_theme);
    }

    @Override
    public void addFragments() {
        mPageList.clear();
        mPageList.add(NewestThemesFragment.class.getName());
//        mPageList.add(EssenceThemesFragment.class.getName());
        mPageList.add(PopularThemesFragment.class.getName());
        mPageList.add(CategoryThemesFragment.class.getName());
    }

}
