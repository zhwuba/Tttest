package com.zhuoyi.market.manager;

import android.app.Notification;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhuoyi.market.appResident.MarketApplication;

public class MarketNotificationHelper
{
	public static String NOTIFY_TITLE = "NOTIFY_TITLE";
	public static String NOTIFY_TEXT = "NOTIFY_TEXT";

	private static MarketNotificationHelper instance;
	private Integer titleColor;
	private Integer textColor;

	private MarketNotificationHelper()
	{
	}

	public static synchronized MarketNotificationHelper get()
	{
		if (instance == null)
		{
			instance = new MarketNotificationHelper();
		}
		return instance;
	}

	public void init()
	{
		if (Looper.myLooper() != Looper.getMainLooper())
		{
			throw new RuntimeException("Can not init this method outside ui thread");
		}
		Notification notification = new Notification();
		notification.setLatestEventInfo(MarketApplication.getRootContext(), NOTIFY_TITLE, NOTIFY_TEXT, null);
		ViewGroup viewGroup = (ViewGroup) notification.contentView.apply(MarketApplication.getRootContext(), null);
		initData(viewGroup);
	}

	private void initData(ViewGroup viewGroup)
	{
		int length = viewGroup.getChildCount();
		for (int i = 0; i < length; i++)
		{
			View view = viewGroup.getChildAt(i);
			if (view instanceof TextView)
			{
				TextView tv = (TextView) view;
				if (tv.getText().equals(NOTIFY_TITLE))
				{
					titleColor = new Integer(tv.getTextColors().getDefaultColor());
				}
				else if (tv.getText().equals(NOTIFY_TEXT))
				{
					textColor = new Integer(tv.getTextColors().getDefaultColor());
				}

			}
			else if (view instanceof ViewGroup)
			{
				initData((ViewGroup) view);
			}
		}
	}

	public Integer getTitleColor()
	{
		return titleColor;
	}

	public Integer getTextColor()
	{
		return textColor;
	}
}
