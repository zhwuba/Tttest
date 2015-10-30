package com.freeme.themeclub.theme.onlinetheme;

import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

import android.os.Bundle;

public class EssenceThemesFragment extends OnlineThemesFragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isOnlineThemes = true;
		msgCode=MessageCode.GET_THEME_LIST_BY_TAG_REQ;
		bout="1";
		sort="02";
		serialNum=2;
		super.onCreate(savedInstanceState);
	}
}
