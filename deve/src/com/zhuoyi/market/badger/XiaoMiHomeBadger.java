package com.zhuoyi.market.badger;

import android.content.Context;
import android.content.Intent;

public class XiaoMiHomeBadger extends ShortcutBadger {

	public XiaoMiHomeBadger(Context context) {
		super(context);
	}

	@Override
	public void addBadge(int badgeCount) {
		Intent localIntent1 = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
		localIntent1.putExtra("android.intent.extra.update_application_component_name", "com.example.shortcutbadger/.MainActivity");
		if(badgeCount>0) {
			if(badgeCount>99) {
				badgeCount = 99;
				localIntent1.putExtra("android.intent.extra.update_application_message_text", badgeCount+"+");
			} else {
				localIntent1.putExtra("android.intent.extra.update_application_message_text", badgeCount+"");
			}
			mContext.sendBroadcast(localIntent1);
			return;
		}
		
	}

	@Override
	public void clearBadge() {
		addBadge(0);
	}

}
