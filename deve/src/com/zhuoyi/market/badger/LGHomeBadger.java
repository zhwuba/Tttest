package com.zhuoyi.market.badger;

import android.content.Context;
import android.content.Intent;


public class LGHomeBadger extends ShortcutBadger{
	
	private static final String INTENT_ACTION = "android.intent.action.BADGE_COUNT_UPDATE";
    private static final String INTENT_EXTRA_BADGE_COUNT = "badge_count";
    private static final String INTENT_EXTRA_PACKAGENAME = "badge_count_package_name";
    private static final String INTENT_EXTRA_ACTIVITY_NAME = "badge_count_class_name";

	public LGHomeBadger(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addBadge(int badgeCount) {
        Intent intent = new Intent(INTENT_ACTION);
        intent.putExtra(INTENT_EXTRA_BADGE_COUNT, badgeCount);
        intent.putExtra(INTENT_EXTRA_PACKAGENAME, getContextPackageName());
        intent.putExtra(INTENT_EXTRA_ACTIVITY_NAME, getEntryActivityName());
        mContext.sendBroadcast(intent);
    }

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
