package com.zhuoyi.market.badger;

import android.content.Context;
import android.content.Intent;

public class SamsungHomeBadger extends ShortcutBadger{

	public SamsungHomeBadger(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addBadge(int badgeCount) {
		Intent localIntent1 = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		localIntent1.putExtra("badge_count", badgeCount);
		localIntent1.putExtra("badge_count_package_name", getContextPackageName());
		localIntent1.putExtra("badge_count_class_name", getEntryActivityName());
		mContext.sendBroadcast(localIntent1);
	}

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
