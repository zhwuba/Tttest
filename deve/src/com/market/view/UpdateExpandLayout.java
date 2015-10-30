package com.market.view;

import org.apache.commons.logging.Log;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhuoyi.market.R;
import com.zhuoyi.market.utils.DensityUtil;
import com.zhuoyi.market.utils.LogHelper;


public class UpdateExpandLayout extends RelativeLayout implements OnClickListener {

	private int MAX_COLLAPSED_LINES = 1;
	private TextView mDescView;

	private TextView mEntryDetailView;
	private TextView mUninstallView;
	private TextView mIngoreView;
	private boolean mCollapsed = true; // Show short version as default..

	protected ImageView mExtendedArrow;

	private LinearLayout mExpandedViewBottom;
	private Drawable mExpandDrawable;
	private Drawable mCollapseDrawable;

	private String mFirstLineText;
	private String mContentText;
	private boolean mRelayout = true;

	/** 是否有底部菜单 **/
	private boolean mIsShowBottom = true;

	private boolean mBottomEnable = false;

	public boolean ismBottomEnable() {
		return mBottomEnable;
	}


	public void setmBottomEnable(boolean mBottomEnable) {
		this.mBottomEnable = mBottomEnable;
		refreshExpandedBottom();
	}

	public UpdateExpandLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		initView();
	}


	private void initView() {
		mDescView = (TextView) findViewById(R.id.update_content_text);
		mExtendedArrow = (ImageView) findViewById(R.id.update_content_expand_arrow);
		if(mIsShowBottom) {
			mExpandedViewBottom = (LinearLayout) findViewById(R.id.update_expand_view_bottom);
			mEntryDetailView = (TextView) findViewById(R.id.update_entry_deitail);
			mUninstallView = (TextView) findViewById(R.id.update_uninstall);
			mIngoreView = (TextView) findViewById(R.id.update_ignore);
		}

		if (mExpandDrawable == null) {
			mExpandDrawable = getResources().getDrawable(R.drawable.desc_more);
		}
		if (mCollapseDrawable == null) {
			mCollapseDrawable = getResources().getDrawable(R.drawable.desc_less);
		}

		mExtendedArrow.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// If no change, measure and return
		if (!mRelayout || getVisibility() == View.GONE) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		mRelayout = false;
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mDescView.setMaxLines(Integer.MAX_VALUE);
		if(mDescView.getLineCount() <= MAX_COLLAPSED_LINES) {
			mDescView.setMaxLines(MAX_COLLAPSED_LINES);
			if(!mIsShowBottom || mBottomEnable) {
				mExtendedArrow.setVisibility(View.GONE);
			} else if(!mBottomEnable) {
				mExtendedArrow.setVisibility(View.VISIBLE);
			}
			return;
		} else {
			mExtendedArrow.setVisibility(View.VISIBLE);
		}

		if (mCollapsed) {
			mDescView.setMaxLines(MAX_COLLAPSED_LINES);
			if(mIsShowBottom) {
				mExpandedViewBottom.setVisibility(View.GONE);
			} 
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}


	@Override
	public void onClick(View v) {
		if(mExtendedArrow.getVisibility() == View.GONE) {  //不可扩展,直接返回
			return;
		}
		mCollapsed = !mCollapsed;
		changeExpand(mCollapsed);

	}

	public void changeExpand(boolean collapse) {
		if(mCollapsed) {	//压缩
			mDescView.setMaxHeight(MAX_COLLAPSED_LINES);
			if(mIsShowBottom) {
				mExpandedViewBottom.setVisibility(View.GONE);
			}
		} else {		// 展开
			mDescView.setMaxHeight(Integer.MAX_VALUE);
			if(mIsShowBottom) {
				if(!mBottomEnable) {
					mExpandedViewBottom.setVisibility(View.VISIBLE);
				} else {
					mExpandedViewBottom.setVisibility(View.GONE);
				}
			}
		}
		setDescText();
		mExtendedArrow.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
	}


	private void setDescText() {
		mRelayout = true;
		if(mCollapsed) {
			mDescView.setText(mFirstLineText + mContentText);
		} else {
			if(TextUtils.isEmpty(mContentText)) {
				mDescView.setText(mFirstLineText);
			} else {
				mDescView.setText(mFirstLineText + "\n" + mContentText);
			}
		}
	}


	public boolean getCollapse() {
		return mCollapsed;
	}


	public void setCollapse(boolean collapse) {
		mCollapsed = collapse;
		changeExpand(mCollapsed);
	}


	public boolean collapseClick() {
		if(mDescView != null) {
			onClick(mDescView);
			return mCollapsed;
		}
		return false;
	}

	public void setFirstLineText(String firstLineText) {
		mFirstLineText = firstLineText;
	}


	public void setText(String text) {
		if(text.equals(mContentText)) {
			return;
		}
		mContentText = text;
		setDescText();		
	}

	public TextView getEntryDetailView() {
		return mEntryDetailView;
	}

	public TextView getUninstallView() {
		return mUninstallView;
	}

	public TextView getIgnoreView() {
		return mIngoreView;
	}

	public void isShowBottom(boolean isShow) {
		mIsShowBottom = isShow;
	}

	//展开状态,并且底部菜单可用时刷新
	public void refreshExpandedBottom() {
		if(mExpandedViewBottom.getVisibility() == View.VISIBLE) return;
		if(!mBottomEnable && !mCollapsed) {
			mExpandedViewBottom.setVisibility(View.VISIBLE);
		}
	}
	
	
	//是否可展开
	public boolean expandEnable() {
		if(mExtendedArrow.getVisibility() == View.VISIBLE) {
			return true;
		} else {
			return false;
		}
	}
}
