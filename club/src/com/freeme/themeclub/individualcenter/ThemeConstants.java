package com.freeme.themeclub.individualcenter;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Define data elements and base columns for theme provider.
 */
public class ThemeConstants implements BaseColumns {

	public static final String AUTHORITY = "com.freeme.thememanager.ThemeProvider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/theme");

	public static final String PACKAGE_NAME = "package_name";

	public static final String THEME_PATH = "theme_path";

	public static final String THEME_TYPE = "theme_type";
	
	public static final String FONT = "font";
	
	public static final String TITLE = "title";

	public static final String DESCRIPTION = "description";
	
	public static final String AUTHOR = "author";

	public static final String VERSION = "version";
	
	public static final String THUMBNAIL = "thumbnail";
	
}
