package com.zhuoyi.market.badger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class HTCHomeBadger extends ShortcutBadger{

	public HTCHomeBadger(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addBadge(int badgeCount) {
		Intent updateIntent = new Intent("com.htc.launcher.action.UPDATE_SHORTCUT");
		updateIntent.putExtra("packagename", getContextPackageName());
		updateIntent.putExtra("count", badgeCount);
		mContext.sendBroadcast(updateIntent);

		Intent setNotificationIntent = new Intent("com.htc.launcher.action.SET_NOTIFICATION");
		ComponentName localComponentName = new ComponentName(getContextPackageName(), getEntryActivityName());
		setNotificationIntent.putExtra("com.htc.launcher.extra.COMPONENT", localComponentName.flattenToShortString());
		setNotificationIntent.putExtra("com.htc.launcher.extra.COUNT", badgeCount);
		mContext.sendBroadcast(setNotificationIntent);
	}

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
