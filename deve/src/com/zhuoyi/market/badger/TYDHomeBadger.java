package com.zhuoyi.market.badger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class TYDHomeBadger extends ShortcutBadger {

	public TYDHomeBadger(Context context) {
		super(context);
	}

	@Override
	public void addBadge(int badgeCount) {
		Intent updateNumIntent = new Intent();
        String packageNameString = getContextPackageName();
        ComponentName cName = new ComponentName(packageNameString, getEntryActivityName());
        updateNumIntent.setAction("com.mediatek.action.UNREAD_CHANGED");
        updateNumIntent.putExtra("com.mediatek.intent.extra.UNREAD_COMPONENT", cName);
        updateNumIntent.putExtra("com.mediatek.intent.extra.UNREAD_NUMBER", badgeCount);
        updateNumIntent.putExtra("com.mediatek.intent.extra.UNREAD_TYPE", Integer.valueOf(0));
        mContext.sendBroadcast(updateNumIntent);
	}

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
