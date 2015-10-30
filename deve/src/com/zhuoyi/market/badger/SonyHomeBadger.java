package com.zhuoyi.market.badger;

import android.content.Context;
import android.content.Intent;


public class SonyHomeBadger extends ShortcutBadger{
	
	private static final String INTENT_ACTION = "com.sonyericsson.home.action.UPDATE_BADGE";
    private static final String INTENT_EXTRA_PACKAGE_NAME = "com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME";
    private static final String INTENT_EXTRA_MESSAGE = "com.sonyericsson.home.intent.extra.badge.MESSAGE";
    private static final String INTENT_EXTRA_SHOW_MESSAGE = "com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE";

	public SonyHomeBadger(Context context) {
		super(context);
	}

	@Override
	public void addBadge(int badgeCount) {
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA_PACKAGE_NAME, getContextPackageName());
        intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, getEntryActivityName());
        intent.putExtra(INTENT_EXTRA_MESSAGE, String.valueOf(badgeCount));
        intent.putExtra(INTENT_EXTRA_SHOW_MESSAGE, badgeCount > 0);
        mContext.sendBroadcast(intent);
    }

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
