package com.zhuoyi.market;

import java.io.ByteArrayOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.market.behaviorLog.LogDefined;
import com.market.behaviorLog.UserLogSDK;
import com.market.view.CheckView;
import com.market.view.CheckView.OnCheckListener;
import com.zhuoyi.market.utils.MarketUtils;

public class CheckActivity extends FragmentActivity
{
	private TextView tvHint;
	private Button btnShare;
	private ImageView ivLogo;
	private CheckView mCheckView;
	private TextView tvResultHint;
	private TextView tvResultDetail;
	private LinearLayout llparent;
	private float check_over_margin_top = -1;
	private float action_bar_height = -1;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		MarketUtils.setSatusBarTranslucent(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		mCheckView = (CheckView) findViewById(R.id.cvProgress);
		ivLogo = (ImageView) findViewById(R.id.ivLogo);
		tvHint = (TextView) findViewById(R.id.tvHint);
		tvResultHint = (TextView) findViewById(R.id.tvResultHint);
		tvResultDetail = (TextView) findViewById(R.id.tvResultDetail);
		llparent = (LinearLayout) findViewById(R.id.llparent);
		btnShare = (Button) findViewById(R.id.btnShare);

		tvHint.setVisibility(View.INVISIBLE);
		check_over_margin_top = getResources().getDimension(R.dimen.check_view_result_margin_top);
		action_bar_height = getResources().getDimension(R.dimen.title_heigh);

		mCheckView.setOnCheckListener(new OnCheckListener()
		{

			@Override
			public void onStart(View view)
			{
				tvHint.setVisibility(View.VISIBLE);
			}

			@Override
			public void onOver(View view)
			{
				ivLogo.setVisibility(View.GONE);
				tvHint.setVisibility(View.GONE);

				final float animationMargin = mCheckView.getTop() - check_over_margin_top - action_bar_height;
				Animation translateIn = new TranslateAnimation(0, 0, animationMargin, 0);
				translateIn.setDuration(500);
				translateIn.setFillAfter(true);

				LinearLayout.LayoutParams params = (LayoutParams) mCheckView.getLayoutParams();
				params.setMargins(0, (int) (check_over_margin_top), 0, 0);
				mCheckView.setLayoutParams(params);
				mCheckView.startAnimation(translateIn);

				mHandler.postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						Animation translateIn = new TranslateAnimation(0, 0, animationMargin, 0);
						translateIn.setDuration(500);
						translateIn.setFillAfter(true);
						tvResultHint.setVisibility(View.VISIBLE);
						tvResultHint.startAnimation(translateIn);
					}
				}, 100);

				mHandler.postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						Animation translateIn = new TranslateAnimation(0, 0, animationMargin, 0);
						translateIn.setDuration(500);
						translateIn.setFillAfter(true);
						tvResultHint.setVisibility(View.VISIBLE);
						tvResultDetail.setVisibility(View.VISIBLE);
						tvResultDetail.startAnimation(translateIn);
					}
				}, 200);

				mHandler.postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						Animation translateIn = new TranslateAnimation(0, 0, animationMargin, 0);
						translateIn.setDuration(500);
						translateIn.setFillAfter(true);
						tvResultHint.setVisibility(View.VISIBLE);
						btnShare.setVisibility(View.VISIBLE);
						btnShare.startAnimation(translateIn);
					}
				}, 400);
			}
		});

		btnShare.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
			    btnShare.setEnabled(false);
				Intent intent = new Intent(CheckActivity.this, ShareAppActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(ShareAppActivity.INTENT_KEY_FROM_CHECK, true);
				intent.putExtra(ShareAppActivity.INTENT_KEY_BITMAP, Bitmap2Bytes(getScreenShot(llparent)));
				startActivity(intent);
			}
		});
	}

	public Bitmap getScreenShot(View view)
	{
		try
		{
			Bitmap bmp = Bitmap.createBitmap(view.getMeasuredWidth(), llparent.getMeasuredHeight(), Config.ARGB_8888);
			view.draw(new Canvas(bmp));
			return bmp;
		}
		catch (OutOfMemoryError e)
		{
			return null;
		}
	}

	public byte[] Bitmap2Bytes(Bitmap bm)
	{
		if (bm == null)
		{
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		UserLogSDK.logCountEvent(this, UserLogSDK.getKeyDes(LogDefined.COUNT_CHECK_VIEW));
		btnShare.setEnabled(true);
	}
	
	@Override
	protected void onDestroy()
	{
		if (mCheckView != null)
		{
			mCheckView.recycle();
		}
		
		super.onDestroy();
	}

}
