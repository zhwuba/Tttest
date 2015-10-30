package com.freeme.themeclub.theme.onlinetheme;

import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

import android.os.Bundle;

public class PopularThemesFragment extends OnlineThemesFragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isOnlineThemes = true;
		msgCode=MessageCode.GET_THEME_LIST_BY_TAG_REQ;
		temp=1;
		serialNum=3;
		super.onCreate(savedInstanceState);
		
	}
}
