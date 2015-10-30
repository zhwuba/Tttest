package com.market.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.zhuoyi.market.R;

public class CheckView extends View
{
	public static final float PI = 3.1415926f;
	public static final String ORIGINAL_CIRCLE_COLOR = "#1dcda1";
	public static final String PROGRESS_CIRCLE_COLOR = "#7eee9a";
	public static final String SCORE_TEXT_COLOR = "#40bf6d";
	public static final int PROGRESS_START_DEGREE = -225;
	public static final int PROGRESS_ALL_DEGREE = 270;

	private float cirque_radius;
	private float shift_cirque_radius;
	private float progress_circle_radius;
	private int animation_radius;
	private int animation_hypetenuse;
	private int score_text_size;

	private Paint mPaint;
	private boolean isChecking = false;
	private boolean isOver = false;

	private int mAngle = 0;

	private int mAnimationAngle;
	private Bitmap animationBmp;
	private Bitmap circleShadow;

	private int score = 100;
	private int scoreAnimation = 0;
	
	private float mScale;
	private Rect src = new Rect();;
	private RectF dst = new RectF();

	private OnClickListener checkViewClickListener;

	private OnCheckListener mOnCheckListener;

	public interface OnCheckListener
	{
		void onStart(View view);

		void onOver(View view);
	}

	public CheckView(Context context)
	{
		this(context, null);
	}

	public CheckView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public CheckView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
		System.out.println("near: scale " + context.getResources().getDisplayMetrics().density);
	}

	private void init(Context context)
	{
		mScale = context.getResources().getDisplayMetrics().density;
		cirque_radius = context.getResources().getDimension(R.dimen.check_view_cirque_radius);
		progress_circle_radius = context.getResources().getDimension(R.dimen.check_view_progress_circle_radius);
		animation_radius = (int) context.getResources().getDimension(R.dimen.check_view_animation_radius);
		animation_hypetenuse = (int) context.getResources().getDimension(R.dimen.check_view_animation_hypetenuse);
		shift_cirque_radius = context.getResources().getDimension(R.dimen.check_view_cirque_shift_radius);
		score_text_size = (int) context.getResources().getDimension(R.dimen.check_view_score_text_size);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);  
        mPaint.setStrokeCap(Paint.Cap.ROUND);  
		mPaint.setColor(Color.parseColor(PROGRESS_CIRCLE_COLOR));
		mPaint.setStrokeWidth(progress_circle_radius);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mPaint.setTextSize(score_text_size * 1.5f);
		mPaint.setTextAlign(Paint.Align.CENTER);
		setClickable(true);
		setBackgroundResource(R.drawable.selector_checkview_start_bg);
		animationBmp = BitmapFactory.decodeResource(getResources(), R.drawable.check_magnifier);
		circleShadow = BitmapFactory.decodeResource(getResources(), R.drawable.check_view_shadow);
		src.left = 0;
		src.top = 0;
		src.bottom = circleShadow.getHeight();
		src.right = circleShadow.getWidth();
		dst.left = 0;
		dst.top = 0;
		dst.bottom = context.getResources().getDimensionPixelOffset(R.dimen.check_view_height);
		dst.right = context.getResources().getDimensionPixelOffset(R.dimen.check_view_width);
		
		setOnClickListener(myClickListener);
	}

	@Override
	public void setOnClickListener(OnClickListener l)
	{
		super.setOnClickListener(myClickListener);
		if (l != myClickListener)
		{
			checkViewClickListener = l;
		}
	}

	private OnClickListener myClickListener = new OnClickListener()
	{

		@Override
		public void onClick(final View v)
		{
			setClickable(false);
			postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					if (mOnCheckListener != null)
					{
						mOnCheckListener.onStart(v);
					}
					if (checkViewClickListener != null)
					{
						checkViewClickListener.onClick(v);
					}
					setBackgroundResource(R.drawable.check_ing_bg);
					isChecking = true;
					postInvalidate();
					post(runnnable);
				}
			}, 200);
		}
	};

	private Runnable runnnable = new Runnable()
	{
		private int progress = 0;

		@Override
		public void run()
		{
			progress++;
			setProgress(progress);
			if (mAngle <= PROGRESS_ALL_DEGREE)
			{
				postDelayed(runnnable, 100);
			}
		}
	};

	/**
	 * @param progress -> [0, 100]
	 */

	public void setProgress(int progress)
	{
		progress = progress < 0 ? 0 : progress;
		progress = progress > 100 ? 100 : progress;
		mAngle = progress * 270 / 100;
		// postInvalidate();
	}

	public void setOnCheckListener(OnCheckListener l)
	{
		this.mOnCheckListener = l;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		int centerX = getMeasuredWidth() / 2;
		int centerY = (int) (getMeasuredHeight() / 2 + mScale);
		int radius = (int) (getMeasuredWidth() / 2 - cirque_radius + progress_circle_radius / 2 + shift_cirque_radius);
		RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

		mPaint.setColor(Color.parseColor(ORIGINAL_CIRCLE_COLOR));
		mPaint.setStyle(Style.STROKE);
		
		canvas.drawArc(oval, PROGRESS_START_DEGREE, PROGRESS_ALL_DEGREE, false, mPaint);
		mPaint.setColor(Color.parseColor(PROGRESS_CIRCLE_COLOR));
		canvas.drawArc(oval, PROGRESS_START_DEGREE, mAngle, false, mPaint);
		canvas.drawBitmap(circleShadow, src, dst, null);

		if (isOver)
		{
			mPaint.setColor(Color.parseColor(SCORE_TEXT_COLOR));
			mPaint.setStyle(Style.FILL);
			FontMetrics mFontMetrics = mPaint.getFontMetrics();
			canvas.drawText(String.valueOf(scoreAnimation), centerX, centerY
					- (mFontMetrics.descent + mFontMetrics.ascent) / 2, mPaint);
			if (scoreAnimation != score)
			{
				scoreAnimation += 3;
				scoreAnimation = scoreAnimation > score ? score : scoreAnimation;
				postInvalidate();
			}
			else
			{
				post(new Runnable()
				{

					@Override
					public void run()
					{
						if (mOnCheckListener != null)
						{
							mOnCheckListener.onOver(CheckView.this);
							mOnCheckListener = null;
						}
					}
				});
			}
			return;
		}

		if (mAngle == PROGRESS_ALL_DEGREE)
		{
			postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					isChecking = false;
					isOver = true;
					setBackgroundResource(R.drawable.check_over);
				}
			}, 800);
		}

		if (isChecking)
		{
			int animationX = (int) (centerX + animation_radius * Math.cos((float) mAnimationAngle / 180 * PI) - animation_hypetenuse);
			int animationY = (int) (centerY + animation_radius * Math.sin((float) mAnimationAngle / 180 * PI) - animation_hypetenuse);
			mAnimationAngle = (mAnimationAngle + 4) % 360;
			canvas.drawBitmap(animationBmp, animationX, animationY, null);

			postInvalidate();
		}
	}
	
	public void recycle()
	{
		recycleBmp(circleShadow);
		recycleBmp(animationBmp);
		setBackgroundResource(0);
		circleShadow = null;
		animationBmp = null;
	}
	
	private void recycleBmp(Bitmap bmp)
	{
		if (bmp != null && !bmp.isRecycled())
		{
			bmp.recycle();
		}
	}

}
