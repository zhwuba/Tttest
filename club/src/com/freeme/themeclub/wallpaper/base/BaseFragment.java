package com.freeme.themeclub.wallpaper.base;

import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import com.freeme.themeclub.BackScrollFragment;


public class BaseFragment extends BackScrollFragment {

    private boolean mVisiableForUser = false;

    public boolean isVisiableForUser() {
        return mVisiableForUser;
    }
    protected void onVisiableChanged(boolean visiable) {
        mVisiableForUser = visiable;
    }
    public List<Integer> onFragmentCreateOptionsMenu(Menu menu) {
        return new ArrayList<Integer>();
    }
    public void onFragmentPrepareOptionsMenu(Menu menu, boolean isCurrentPage) {
    }
    public void onFragmentOptionsItemSelected(MenuItem item) {
    }
}