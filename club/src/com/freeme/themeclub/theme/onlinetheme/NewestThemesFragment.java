package com.freeme.themeclub.theme.onlinetheme;


import com.freeme.themeclub.theme.onlinetheme.util.MessageCode;

import android.os.Bundle;

public class NewestThemesFragment extends OnlineThemesFragment{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		isOnlineThemes = true;
		msgCode=MessageCode.GET_THEME_LIST_BY_TAG_REQ;
		sort="02";
		serialNum=1;
		loadAds=true;
		super.onCreate(savedInstanceState);
	}
   
}
