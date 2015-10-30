package com.freeme.themeclub.theme.onlinetheme;

import android.net.Uri;
import android.provider.BaseColumns;

public class ThemeDownload implements BaseColumns{

    public static final Uri URI = Uri.parse("content://com.freeme.themeclub.provider.themedownload/themedownload");
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String THEME_ID = "theme_id";
    public static final String IS_THEME = "is_theme";
    public static final String DOWNLOAD_ID = "download_id";
    public static final String PATH = "path";
    public static final String URL = "url";
    public static final String PACKAGE_NAME = "package_name";
    public static final String[] QUERY = {_ID,NAME,THEME_ID,IS_THEME,DOWNLOAD_ID,PATH,URL,PACKAGE_NAME};
}
