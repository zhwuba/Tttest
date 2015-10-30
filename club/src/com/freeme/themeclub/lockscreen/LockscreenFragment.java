package com.freeme.themeclub.lockscreen;

import android.os.Bundle;

import com.freeme.themeclub.OuterFragment;
import com.freeme.themeclub.R;

public class LockscreenFragment extends OuterFragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFragmentLayout(R.layout.outer_fragment_lockscreen);
		setInnerViewpagerId(R.id.inner_viewpager_lockscreen);
	}
	
	@Override
	public void addFragments() {
	    mPageList.clear();
	    mPageList.add(PopularLockscreenFragment.class.getName());
	    mPageList.add(NewestLockscreenFragment.class.getName());
//        mPageList.add(EssenceLockscreenFragment.class.getName());
//        mPageList.add(CategoryLockscreenFragment.class.getName());
	}
}