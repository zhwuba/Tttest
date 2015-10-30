package com.zhuoyi.market.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.OverScroller;

public class ScrollLinearLayout extends LinearLayout
{
	private static final int SCROLL_TIME_DELAY = 4000;
	private static final int ACTION_MOVE_DISTANCE = 40;
	private OverScroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mTouchSlop;
	private int mMinimumVelocity;
	private int mMaximumVelocity;

	private int mVelocity;
	private float mLastMotionY;
	private int mScrollStartY;

	private int childTotalHeight;

	private int viewHeight;

	private boolean isLvSlideToTop = true;

	private boolean isSlideAction = false;

	private OnScrollStateListener svl;

	public ScrollLinearLayout(Context context)
	{
		this(context, null);
	}

	public ScrollLinearLayout(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ScrollLinearLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	void init(Context context)
	{
		mScroller = new OverScroller(getContext());
		setFocusable(true);
		setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
		setWillNotDraw(false);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	public interface OnScrollStateListener
	{
		void onScrollChanged(float velocity, boolean isSlideToTop, boolean isSlideToBottom);
	}

	public void setOnScrollListener(OnScrollStateListener l)
	{
		svl = l;
	}

	private void obtainVelocityTracker(MotionEvent event)
	{
		if (mVelocityTracker == null)
		{
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker()
	{
		if (mVelocityTracker != null)
		{
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset())
		{
			if (mScroller.getCurrY() <= childTotalHeight - viewHeight && mScroller.getCurrY() >= 0)
			{
				if ((mScroller.getCurrY() > getScrollY() || isLvSlideToTop) && svl != null)
				{
					svl.onScrollChanged(mScroller.getCurrVelocity(), false, false);
				}
				scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
				postInvalidate();
			}
			else if (mScroller.getCurrY() > childTotalHeight - viewHeight)
			{
				scrollTo(mScroller.getCurrX(), childTotalHeight - viewHeight);
				if (svl != null)
				{
					svl.onScrollChanged(mScroller.getCurrVelocity(), false, true);
				}
			}
			else
			{
				scrollTo(mScroller.getCurrX(), 0);
				if (svl != null)
				{
					svl.onScrollChanged(mScroller.getCurrVelocity(), true, false);
				}
			}
		}
	}

	public void fling(int velocityY)
	{
		if (getChildCount() > 0)
		{
			mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0, childTotalHeight - viewHeight);
			invalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		super.onLayout(changed, l, t, r, b);
		int childCount = getChildCount();
		childTotalHeight = getChildAt(childCount - 1).getBottom();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		viewHeight = getHeight();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
		{
			return super.onInterceptTouchEvent(event) || isSlideAction;
		}
		return super.onInterceptTouchEvent(event);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0)
		{
			return false;
		}

		obtainVelocityTracker(event);
		final int action = event.getAction();
		final float y = event.getY();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				if (!mScroller.isFinished())
				{
					mScroller.abortAnimation();
				}
				isSlideAction = false;
				mScrollStartY = getScrollY();
				mLastMotionY = y;
				mVelocity = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				final int deltaY = (int) (mLastMotionY - y);
				mLastMotionY = y;
				if (deltaY < 0)
				{
					if (getScrollY() + deltaY >= 0)
					{
						scrollBy(0, deltaY);
						if (svl != null && isLvSlideToTop)
						{
							svl.onScrollChanged(0, false, false);
						}
					}
					else if (getScrollY() != 0)
					{
						scrollBy(0, -getScrollY());
						if (svl != null)
						{
							svl.onScrollChanged(0, true, false);
						}
					}
				}
				else if (deltaY > 0)
				{
					if (deltaY + getScrollY() < childTotalHeight - viewHeight)
					{
						if (svl != null)
						{
							svl.onScrollChanged(0, false, false);
						}
						scrollBy(0, deltaY);
					}
					else if (childTotalHeight - viewHeight - getScrollY() != 0)
					{
						scrollBy(0, childTotalHeight - viewHeight - getScrollY());
						if (svl != null)
						{
							svl.onScrollChanged(0, false, true);
						}
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) velocityTracker.getYVelocity();
				if (isLvSlideToTop == false && initialVelocity > mMinimumVelocity)
				{
					mVelocity = initialVelocity;
				}
				else if (Math.abs(initialVelocity) > mMinimumVelocity && getChildCount() > 0)
				{
					fling(-initialVelocity);
				}
				releaseVelocityTracker();
				break;
		}
		return true;
	}

	public void setLvSlideToTop(boolean slideToTop)
	{
		if (isLvSlideToTop != slideToTop)
		{
			isLvSlideToTop = slideToTop;
			if (isLvSlideToTop == true && mVelocity!= 0)
			{
				fling(-mVelocity / 4);
			}
		}
	}

	@Override
	public void scrollBy(int x, int y)
	{
		if (!isSlideAction && Math.abs(getScrollY() - mScrollStartY) >= ACTION_MOVE_DISTANCE)
		{
			isSlideAction = true;
		}
		if (y < 0 && !isLvSlideToTop)
		{
			return;
		}
		super.scrollBy(x, y);

	}

	@Override
	public void scrollTo(int x, int y)
	{
		if (!isSlideAction && Math.abs(getScrollY() - mScrollStartY) >= ACTION_MOVE_DISTANCE)
		{
			isSlideAction = true;
		}
		if (getScrollY() > y && !isLvSlideToTop)
		{
			return;
		}
		super.scrollTo(x, y);
	}
}
