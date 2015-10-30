package com.zhuoyi.market.home;

import android.os.Handler;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class AdOnPageChangeListener implements OnPageChangeListener {
		
		private Handler mHandler;
		private boolean mIsScrolling = false;
		private ViewPagerScrollCallback mCallBack = null;
		public AdOnPageChangeListener(Handler handler)
		{
			mHandler = handler;
		}
		public AdOnPageChangeListener(Handler handler,ViewPagerScrollCallback callback)
		{
			mHandler = handler;
			mCallBack = callback;
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			if(state == 0) //idle
			{
				mIsScrolling = false;
				if(mCallBack!=null)
					mCallBack.handleScrollState(false);
			}
			else if(state == 1) // scrolling
			{
				mIsScrolling = true;
				if(mCallBack!=null)
					mCallBack.handleScrollState(true);
			}
			else if(state == 2) //init finished
			{
				
			}
		}
		public boolean isScrolling()
		{
			return  mIsScrolling;
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int position) 
		{
			mHandler.sendEmptyMessage(position);
		}
		
        public interface ViewPagerScrollCallback {
            public void handleScrollState(boolean status);
        }
}
